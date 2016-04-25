package de.cismet.cids.integrationtests;

import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.CidsBeanInfo;
import static de.cismet.cids.integrationtests.TestEnvironment.REST_SERVER_CONTAINER;
import static de.cismet.cids.integrationtests.TestEnvironment.SERVER_CONTAINER;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import de.cismet.cidsx.client.connector.RESTfulInterfaceConnector;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.testcontainers.containers.DockerComposeContainer;

/**
 * CidsBean vs MetaObject remote serialisation / deserialisation Tests
 * <br>
 * <strong>cids-server</strong>:<br>
 * MetaObject | MetaObject<br>
 * <br><br>
 * <strong>cids-server-rest-legac</strong>:<br>
 * MetaObject->CidsBean->JSON | JSON->CidsBean->MetaObject
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@RunWith(DataProviderRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RESTfulInterfaceTest extends TestBase {

    protected final static Logger LOGGER = Logger.getLogger(RESTfulInterfaceTest.class);

    /**
     * The static cids Reference Container is reused for all @test Methods! To
     * avoid an unnecessary start of the container, it is initialized in the
     * initcidsRefContainer() operation that checks if integration tests are
     * enabled.
     */
    protected static DockerComposeContainer dockerEnvironment = null;

    protected final static String ENTITIES_JSON_PACKAGE = "de/cismet/cids/integrationtests/entities/";
    protected final static ArrayList<String> CIDS_BEANS_JSON = new ArrayList<String>();

    protected static RESTfulInterfaceConnector restConnector = null;
    protected static RESTfulSerialInterfaceConnector legacyConnector = null;
    protected static User user = null;
    protected static Properties properties = null;
    protected static boolean connectionFailed = false;

    /**
     * This ClassRule is executed only once before any test run (@Test method)
     * starts. It checks whether the cids testing enviroment is enabled or not.
     * If the testing enviroment is enabled, it creates a new managed
     * ComposeContainer that provides access to a dockerized cids
     * integrationtests system.
     *
     * @return ComposeContainer or dummy ClassRule
     */
    @ClassRule
    public static TestRule initCidsRefContainer() {
        LOGGER.info("@ClassRule initCidsRefContainer(): activating cids Integration Tests: "
                + !TestEnvironment.isIntegrationTestsDisabled());

        // check if integration tests are enabled in current maven profile
        if (TestEnvironment.isIntegrationTestsDisabled()) {
            // return Dummy ClassRule that skips the test
            return TestEnvironment.SKIP_INTEGRATION_TESTS;
        } else {

            try {
                // create new ComposeContainer
                dockerEnvironment = TestEnvironment.createDefaultDockerEnvironment();
                // Important: return the container instance. Otherwise start/stop
                // of the container is not called!
                properties = TestEnvironment.getProperties();
                return dockerEnvironment;
            } catch (Exception ex) {
                LOGGER.fatal("could not initialize docker interation test environment, integration tests disabled!", ex);
                return TestEnvironment.SKIP_INTEGRATION_TESTS;
            }
        }
    }

    /**
     * Static helper method for loading local cids beans instances.
     * <strong>Warning:</strong> If cids-reference.sql
     * (docker-volumes/cids-integrationtests) changes, the JSON files have to be
     * updated, too!
     *
     * @throws Exception
     */
    protected static void initCidsBeansJson() throws Exception {
        if (CIDS_BEANS_JSON.isEmpty() && TestEnvironment.isIntegrationTestsEnabled()) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resources;

            try {
                resources = classLoader.getResource(ENTITIES_JSON_PACKAGE);

                final Scanner scanner = new Scanner((InputStream) resources.getContent()).useDelimiter("\\n");
                while (scanner.hasNext()) {
                    final String jsonFile = ENTITIES_JSON_PACKAGE + scanner.next();
                    LOGGER.debug("loading cids entity from json file " + jsonFile);
                    try {

                        final String entity = IOUtils.toString(classLoader.getResourceAsStream(jsonFile), "UTF-8");
                        CIDS_BEANS_JSON.add(entity);

                    } catch (Exception ex) {
                        LOGGER.error("could not load cids entities from url " + jsonFile, ex);
                        throw ex;
                    }
                }

                LOGGER.info(CIDS_BEANS_JSON.size() + " CIDS_BEANS_JSON entities loaded");

            } catch (Exception ex) {
                LOGGER.error("could not locate entities json files: " + ex.getMessage(), ex);
                throw ex;
            }
        } else {
            LOGGER.warn("CIDS_BEANS_JSON already initialised");
        }
    }

    /**
     * Executed only once after @ClassRule. Assumes that testcontainers docker
     * compose started the required cids containers and checks if the cids
     * services are up and rennung. Initializes legacy and rest connectors.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        LOGGER.debug("beforeClass(): cids Integration Tests activated, loading properties");
        Assert.assertNotNull("TestEnvironment created", properties);

        // check container creation succeeded 
        Assert.assertNotNull("cidsRefContainer sucessfully created", dockerEnvironment);

        Assert.assertFalse("connecting to cids integrationtest docker containers could not be established",
                connectionFailed);

        try {

            final String brokerUrl = TestEnvironment.getCallserverUrl(
                    dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))));

            // check connection to legacy server (broker f.k.a. callserver)
            if (!TestEnvironment.pingHostWithPost(
                    dockerEnvironment.getServiceHost(
                            SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(
                            SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("broker.port", "9986"))),
                    "/callserver/binary/getUserGroupNames",
                    12)) {
                connectionFailed = true;
                throw new Exception(brokerUrl + " did not answer after 12 retries");
            }

            LOGGER.info("connecting to cids reference docker legacy server: " + brokerUrl);
            legacyConnector = new RESTfulSerialInterfaceConnector(brokerUrl);

            final String restServerUrl = "http://"
                    + dockerEnvironment.getServiceHost(REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890")))
                    + ":"
                    + dockerEnvironment.getServicePort(REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890")))
                    + "/";

            // check connection to rest server (cids-server-rest-legacy)
            if (!TestEnvironment.pingHostWithGet(
                    dockerEnvironment.getServiceHost(
                            REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890"))),
                    dockerEnvironment.getServicePort(
                            REST_SERVER_CONTAINER,
                            Integer.parseInt(properties.getProperty("restservice.port", "8890"))),
                    "/service/ping",
                    12)) {
                connectionFailed = true;
                throw new Exception(restServerUrl + " did not answer after 6 retries");
            }

            LOGGER.info("connecting to cids reference docker rest server: " + restServerUrl);
            restConnector = new RESTfulInterfaceConnector(restServerUrl);

            // authenticate user in rest connector
            user = restConnector.getUser(properties.getProperty("usergroupDomain", "CIDS_REF"),
                    properties.getProperty("usergroup", "Administratoren"),
                    properties.getProperty("userDomain", "CIDS_REF"),
                    properties.getProperty("username", "admin"),
                    properties.getProperty("password", "cismet"));

            LOGGER.info("sucessfully authenticated cids user: " + user.toString());

        } catch (Exception e) {

            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Unfortunately, this data provicer method is called before the @ClassRule
     * and before @BeforeClass! Therefore intitialisation of CidsBeans from JSON
     * (located in test resources package) is delegated to initCidsBeansJson();
     *
     * @return
     * @throws Exception
     */
    @DataProvider
    public final static String[] getCidsBeansJson() throws Exception {

        if (TestEnvironment.isIntegrationTestsEnabled()) {
            initCidsBeansJson();
            return CIDS_BEANS_JSON.toArray(new String[CIDS_BEANS_JSON.size()]);
        } else {
            // UGLY HACK for DataProviderRunner compatibility:
            // return dummy array to avoid 'java.lang.IllegalArgumentException: 
            // Could not create test methods using probably 'null' or 'empty' dataprovider'
            // when tests are disabled.
            return new String[]{"this string is never used but DataProviderRunner expects it"};
        }
    }

    @Before
    public void beforeTest() {
        Assert.assertNotNull("cids legacy server connection successfully established", legacyConnector);
        Assert.assertNotNull("cids rest server connection successfully established", restConnector);
        Assert.assertNotNull("user authenticated", user);
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void test01getAndCompareMetaObjects(final String cidsBeanJson) throws Exception {
        LOGGER.debug("testing getAndCompareMetaObjects");

        try {
            final CidsBean cidsBeanFromJson = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject metaObjectFromJson = cidsBeanFromJson.getMetaObject();

            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " from legacy server");
            final MetaObject metaObjectFromLegacyServer = legacyConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            final CidsBean cidsBeanFromLegacyServer = metaObjectFromLegacyServer.getBean();

            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " from rest server");
            final MetaObject metaObjectFromRestServer = restConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            final CidsBean cidsBeanFromRestServer = metaObjectFromRestServer.getBean();

            this.compareCidsBeanProperties(cidsBeanFromJson,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromRestServer);

            this.compareAll(metaObjectFromJson,
                    metaObjectFromLegacyServer,
                    metaObjectFromRestServer,
                    cidsBeanFromJson,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromRestServer);

        } catch (AssertionError ae) {
            LOGGER.error("getAndCompareCidsBeans test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getAndCompareCidsBeans: " + ex.getMessage(), ex);
            throw ex;
        }

        LOGGER.info("getAndCompareMetaObjects test passed");
    }

    protected void compareCidsBeanProperties(final CidsBean cidsBeanFromJson,
            final CidsBean cidsBeanFromLegacyServer,
            final CidsBean cidsBeanFromRestServer) {
        for (final String propertyName : cidsBeanFromJson.getPropertyNames()) {
            final Object propertyFromJson = cidsBeanFromJson.getProperty(propertyName);
            final Object propertyFromLegacyServer = cidsBeanFromLegacyServer.getProperty(propertyName);
            final Object propertyFromRestServer = cidsBeanFromRestServer.getProperty(propertyName);

            if (propertyFromJson != null) {
                Assert.assertNotNull("property " + propertyName + " FromLegacyServer is not null",
                        propertyFromLegacyServer);
                Assert.assertNotNull("property " + propertyName + " FromRestServer is not null",
                        propertyFromRestServer);

                if (CidsBean.class.isAssignableFrom(propertyFromJson.getClass())) {

                    Assert.assertTrue("property " + propertyName + " FromLegacyServer is a CidsBean",
                            CidsBean.class.isAssignableFrom(propertyFromLegacyServer.getClass()));
                    Assert.assertTrue("property " + propertyName + " FromRestServer is a CidsBean",
                            CidsBean.class.isAssignableFrom(propertyFromRestServer.getClass()));

                    final CidsBean cidsBeanPropertyFromJson = (CidsBean) propertyFromJson;
                    final CidsBean cidsBeanPropertyLegacyServer = (CidsBean) propertyFromLegacyServer;
                    final CidsBean cidsBeanPropertyFromRestServer = (CidsBean) propertyFromRestServer;

                    this.compareCidsBeans(cidsBeanPropertyFromJson,
                            cidsBeanPropertyLegacyServer,
                            cidsBeanPropertyFromRestServer);

                    this.compareCidsBeanProperties(cidsBeanPropertyFromJson,
                            cidsBeanPropertyLegacyServer,
                            cidsBeanPropertyFromRestServer);
                } else if (Collection.class.isAssignableFrom(propertyFromJson.getClass())
                        && !((Collection) propertyFromJson).isEmpty()) {

                    Assert.assertTrue("property " + propertyName + " FromLegacyServer is a CidsBean Collection",
                            Collection.class.isAssignableFrom(propertyFromLegacyServer.getClass()));
                    Assert.assertTrue("property " + propertyName + " FromRestServer is a CidsBean Collection",
                            Collection.class.isAssignableFrom(propertyFromRestServer.getClass()));

                    final Collection<CidsBean> cidsBeanCollectionPropertyFromJson
                            = (Collection<CidsBean>) propertyFromJson;
                    final Collection<CidsBean> cidsBeanCollectionPropertyFromLegacyServer
                            = (Collection<CidsBean>) propertyFromLegacyServer;
                    final Collection<CidsBean> cidsBeanCollectionPropertyFromRestServer
                            = (Collection<CidsBean>) propertyFromRestServer;

                    Assert.assertEquals("cidsBeanCollectionPropertyLegacyServer server matches size",
                            cidsBeanCollectionPropertyFromJson.size(),
                            cidsBeanCollectionPropertyFromLegacyServer.size());
                    Assert.assertEquals("cidsBeanCollectionPropertyFromRestServer server matches size",
                            cidsBeanCollectionPropertyFromJson.size(),
                            cidsBeanCollectionPropertyFromRestServer.size());

                    final Iterator<CidsBean> cidsBeanCollectionPropertyIteratorFromJson
                            = cidsBeanCollectionPropertyFromJson.iterator();
                    final Iterator<CidsBean> cidsBeanCollectionPropertyIteratorFromLegacyServer
                            = cidsBeanCollectionPropertyFromLegacyServer.iterator();
                    final Iterator<CidsBean> cidsBeanCollectionPropertyIteratorFromRestServer
                            = cidsBeanCollectionPropertyFromRestServer.iterator();

                    while (cidsBeanCollectionPropertyIteratorFromJson.hasNext()
                            && cidsBeanCollectionPropertyIteratorFromLegacyServer.hasNext()
                            && cidsBeanCollectionPropertyIteratorFromRestServer.hasNext()) {

                        final CidsBean collectionCidsBeanFromJson
                                = cidsBeanCollectionPropertyIteratorFromJson.next();
                        final CidsBean collectionCidsBeanFromLegacyServer
                                = cidsBeanCollectionPropertyIteratorFromLegacyServer.next();
                        final CidsBean collectionCidsBeanFromRestServer
                                = cidsBeanCollectionPropertyIteratorFromRestServer.next();

                        this.compareCidsBeans(collectionCidsBeanFromJson,
                                collectionCidsBeanFromLegacyServer,
                                collectionCidsBeanFromRestServer);

                        this.compareCidsBeanProperties(collectionCidsBeanFromJson,
                                collectionCidsBeanFromLegacyServer,
                                collectionCidsBeanFromRestServer);
                    }

                } else {

                    Assert.assertTrue("property " + propertyName + " FromLegacyServer is a " + propertyFromJson.getClass(),
                            propertyFromJson.getClass().isAssignableFrom(propertyFromLegacyServer.getClass()));
                    Assert.assertTrue("property " + propertyName + " FromRestServer is a " + propertyFromJson.getClass(),
                            propertyFromJson.getClass().isAssignableFrom(propertyFromRestServer.getClass()));

                    // java.sql.Date object comparision does not work
                    // probably due to fix implemented in #164
                    if (java.sql.Date.class.isAssignableFrom(propertyFromJson.getClass())) {
                        Assert.assertEquals("cidsBean.property(" + propertyName + ") from legacy server matches",
                                propertyFromJson.toString(),
                                propertyFromLegacyServer.toString());
                        Assert.assertEquals("cidsBean.getProperty(" + propertyName + ") from rest server matches",
                                propertyFromJson.toString(),
                                propertyFromRestServer.toString());

                    } else {
                        Assert.assertEquals("cidsBean.property(" + propertyName + ") from legacy server matches",
                                propertyFromJson,
                                propertyFromLegacyServer);
                        Assert.assertEquals("cidsBean.getProperty(" + propertyName + ") from rest server matches",
                                propertyFromJson,
                                propertyFromRestServer);
                    }
                }
            }
        }
    }

    /**
     * Compares getMetaObject vs getCidsBean vs MetaObject & CidsBeans retrieved
     * from Legacy REST and Pure REST Servers and deserilaized from local JSON
     * files.
     *
     * @param metaObjectFromJson
     * @param metaObjectFromLegacyServer
     * @param metaObjectFromRestServer
     * @param cidsBeanFromJson
     * @param cidsBeanFromLegacyServer
     * @param cidsBeanFromRestServer
     */
    protected void compareAll(final MetaObject metaObjectFromJson,
            final MetaObject metaObjectFromLegacyServer,
            final MetaObject metaObjectFromRestServer,
            final CidsBean cidsBeanFromJson,
            final CidsBean cidsBeanFromLegacyServer,
            final CidsBean cidsBeanFromRestServer) throws AssertionError {

        LOGGER.debug("comparing MetaObjects");
        this.compareMetaObjects(metaObjectFromJson,
                metaObjectFromLegacyServer,
                metaObjectFromRestServer);

        LOGGER.debug("comparing CidsBeans");
        this.compareCidsBeans(cidsBeanFromJson,
                cidsBeanFromLegacyServer,
                cidsBeanFromRestServer);

        LOGGER.debug("comparing MetaObjects from CidsBean.getMetaObject()");
        this.compareMetaObjects(cidsBeanFromJson.getMetaObject(),
                cidsBeanFromLegacyServer.getMetaObject(),
                cidsBeanFromRestServer.getMetaObject());

        LOGGER.debug("comparing CidsBean from CidsBean.getMetaObject().getCidsBean()");
        this.compareCidsBeans(cidsBeanFromJson.getMetaObject().getBean(),
                cidsBeanFromLegacyServer.getMetaObject().getBean(),
                cidsBeanFromRestServer.getMetaObject().getBean());
    }

    /**
     * Helper method to compare remote / local meta objects
     *
     * @param metaObjectFromJson
     * @param metaObjectFromLegacyServer
     * @param metaObjectFromRestServer
     * @throws AssertionError
     */
    protected void compareMetaObjects(final MetaObject metaObjectFromJson,
            final MetaObject metaObjectFromLegacyServer,
            final MetaObject metaObjectFromRestServer) throws AssertionError {

        Assert.assertEquals("metaObject.getClassID() from legacy server matches",
                metaObjectFromJson.getClassID(),
                metaObjectFromLegacyServer.getClassID());
        Assert.assertEquals("metaObject.getClassID() from rest server matches",
                metaObjectFromJson.getClassID(),
                metaObjectFromRestServer.getClassID());

        Assert.assertEquals("metaObject.getClassKey() from legacy server matches",
                metaObjectFromJson.getClassKey(),
                metaObjectFromLegacyServer.getClassKey());
        Assert.assertEquals("metaObject.getClassKey() from rest server matches",
                metaObjectFromJson.getClassKey(),
                metaObjectFromRestServer.getClassKey());

        Assert.assertEquals("metaObject.getComplexEditor() from legacy server matches",
                metaObjectFromJson.getComplexEditor(),
                metaObjectFromLegacyServer.getComplexEditor());
        Assert.assertEquals("metaObject.getComplexEditor() from rest server matches",
                metaObjectFromJson.getComplexEditor(),
                metaObjectFromRestServer.getComplexEditor());

        Assert.assertEquals("metaObject.getDescription() from legacy server matches",
                metaObjectFromJson.getDescription(),
                metaObjectFromLegacyServer.getDescription());
        Assert.assertEquals("metaObject.getDescription() from rest server matches",
                metaObjectFromJson.getDescription(),
                metaObjectFromRestServer.getDescription());

        Assert.assertEquals("metaObject.getDomain() from legacy server matches",
                metaObjectFromJson.getDomain(),
                metaObjectFromLegacyServer.getDomain());
        Assert.assertEquals("metaObject.getDomain() from rest server matches",
                metaObjectFromJson.getDomain(),
                metaObjectFromRestServer.getDomain());

        Assert.assertEquals("metaObject.getEditor() from legacy server matches",
                metaObjectFromJson.getEditor(),
                metaObjectFromLegacyServer.getEditor());
        Assert.assertEquals("metaObject.getEditor() from rest server matches",
                metaObjectFromJson.getEditor(),
                metaObjectFromRestServer.getEditor());

        Assert.assertEquals("metaObject.getGroup() from legacy server matches",
                metaObjectFromJson.getGroup(),
                metaObjectFromLegacyServer.getGroup());
        Assert.assertEquals("metaObject.getGroup() from rest server matches",
                metaObjectFromJson.getGroup(),
                metaObjectFromRestServer.getGroup());

        Assert.assertEquals("metaObject.getID() from legacy server matches",
                metaObjectFromJson.getID(),
                metaObjectFromLegacyServer.getID());
        Assert.assertEquals("metaObject.getID() from rest server matches",
                metaObjectFromJson.getID(),
                metaObjectFromRestServer.getID());

        Assert.assertEquals("metaObject.getId() from legacy server matches",
                metaObjectFromJson.getId(),
                metaObjectFromLegacyServer.getId());
        Assert.assertEquals("metaObject.getId() from rest server matches",
                metaObjectFromJson.getId(),
                metaObjectFromRestServer.getId());

        Assert.assertEquals("metaObject.getKey() from legacy server matches",
                metaObjectFromJson.getKey(),
                metaObjectFromLegacyServer.getKey());
        Assert.assertEquals("metaObject.getKey() from rest server matches",
                metaObjectFromJson.getKey(),
                metaObjectFromRestServer.getKey());

        Assert.assertEquals("metaObject.getName() from legacy server matches",
                metaObjectFromJson.getName(),
                metaObjectFromLegacyServer.getName());
        Assert.assertEquals("metaObject.getName() from rest server matches",
                metaObjectFromJson.getName(),
                metaObjectFromRestServer.getName());

        // FIXME: Property Strings do not match -> Array Helper Object Ids are lost after deserialization
        // See Issue #165
//        LOGGER.debug(metaObjectFromJson.getPropertyString());
//        LOGGER.debug(metaObjectFromLegacyServer.getPropertyString());
//        Assert.assertEquals("metaObject.getPropertyString() from legacy server matches",
//                metaObjectFromJson.getPropertyString(),
//                metaObjectFromLegacyServer.getPropertyString());
//        Assert.assertEquals("metaObject.getPropertyString() from rest server matches",
//                metaObjectFromJson.getPropertyString(),
//                metaObjectFromRestServer.getPropertyString());
        Assert.assertEquals("metaObject.getRenderer() from legacy server matches",
                metaObjectFromJson.getRenderer(),
                metaObjectFromLegacyServer.getRenderer());
        Assert.assertEquals("metaObject.getRenderer() from rest server matches",
                metaObjectFromJson.getRenderer(),
                metaObjectFromRestServer.getRenderer());

        Assert.assertEquals("metaObject.getSimpleEditor() from legacy server matches",
                metaObjectFromJson.getSimpleEditor(),
                metaObjectFromLegacyServer.getSimpleEditor());
        Assert.assertEquals("metaObject.getSimpleEditor() from rest server matches",
                metaObjectFromJson.getSimpleEditor(),
                metaObjectFromRestServer.getSimpleEditor());

        Assert.assertEquals("metaObject.getStatus() from legacy server matches",
                metaObjectFromJson.getStatus(),
                metaObjectFromLegacyServer.getStatus());
        Assert.assertEquals("metaObject.getStatus() from rest server matches",
                metaObjectFromJson.getStatus(),
                metaObjectFromRestServer.getStatus());

        // FIXME: DebugStrings Strings do not match -> Array Helper Object Ids are lost after deserialization
        // See Issue #165
        //        Assert.assertEquals("metaObject.getStatusDebugString() from legacy server matches",
        //                metaObjectFromJson.getStatusDebugString(),
        //                metaObjectFromLegacyServer.getStatusDebugString());
        //        Assert.assertEquals("metaObject.getStatusDebugString() from rest server matches",
        //                metaObjectFromJson.getStatusDebugString(),
        //                metaObjectFromRestServer.getStatusDebugString());
    }

    /**
     * Helper method to compare remote / local cids beans
     *
     * @param cidsBeanFromJson
     * @param cidsBeanFromLegacyServer
     * @param cidsBeanFromRestServer
     * @throws AssertionError
     */
    protected void compareCidsBeans(final CidsBean cidsBeanFromJson,
            final CidsBean cidsBeanFromLegacyServer,
            final CidsBean cidsBeanFromRestServer) throws AssertionError {

        final CidsBeanInfo beanInfoFromJson = cidsBeanFromJson.getCidsBeanInfo();
        final String cidsBeanJson = cidsBeanFromJson.toJSONString(true);

        final CidsBeanInfo beanInfoFromLegacyServer = cidsBeanFromLegacyServer.getCidsBeanInfo();
        final String cidsBeanJsonFromLegacyServer = cidsBeanFromLegacyServer.toJSONString(true);

        final CidsBeanInfo beanInfoFromRestServer = cidsBeanFromRestServer.getCidsBeanInfo();
        final String cidsBeanJsonFromRestServer = cidsBeanFromRestServer.toJSONString(true);

        Assert.assertEquals("JsonObjectKey key from legacy server matches",
                beanInfoFromJson.getJsonObjectKey(),
                beanInfoFromLegacyServer.getJsonObjectKey());
        Assert.assertEquals("JsonObjectKey key from rest server matches",
                beanInfoFromJson.getJsonObjectKey(),
                beanInfoFromRestServer.getJsonObjectKey());

        Assert.assertEquals("cidsBean.getPrimaryKeyFieldname() from legacy server matches",
                cidsBeanFromJson.getPrimaryKeyFieldname(),
                cidsBeanFromLegacyServer.getPrimaryKeyFieldname());
        Assert.assertEquals("cidsBean.getPrimaryKeyFieldname from rest server matches",
                cidsBeanFromJson.getPrimaryKeyFieldname(),
                cidsBeanFromRestServer.getPrimaryKeyFieldname());

        Assert.assertEquals("cidsBean.getPrimaryKeyValue() from legacy server matches",
                cidsBeanFromJson.getPrimaryKeyValue(),
                cidsBeanFromLegacyServer.getPrimaryKeyValue());
        Assert.assertEquals("cidsBean.getPrimaryKeyValue from rest server matches",
                cidsBeanFromJson.getPrimaryKeyValue(),
                cidsBeanFromRestServer.getPrimaryKeyValue());

        Assert.assertArrayEquals("cidsBean.getPropertyNames() from legacy server matches",
                cidsBeanFromJson.getPropertyNames(),
                cidsBeanFromLegacyServer.getPropertyNames());
        Assert.assertArrayEquals("cidsBean.getPropertyNames from rest server matches",
                cidsBeanFromJson.getPropertyNames(),
                cidsBeanFromRestServer.getPropertyNames());

        Assert.assertEquals("cidsBean.hasArtificialChangeFlag() from legacy server matches",
                cidsBeanFromJson.hasArtificialChangeFlag(),
                cidsBeanFromLegacyServer.hasArtificialChangeFlag());
        Assert.assertEquals("cidsBean.hasArtificialChangeFlag from rest server matches",
                cidsBeanFromJson.hasArtificialChangeFlag(),
                cidsBeanFromRestServer.hasArtificialChangeFlag());

        Assert.assertEquals("cidsBean.hasObjectReadPermission(user) from legacy server matches",
                cidsBeanFromJson.hasObjectReadPermission(user),
                cidsBeanFromLegacyServer.hasObjectReadPermission(user));
        Assert.assertEquals("cidsBean.hasObjectReadPermission(user) from rest server matches",
                cidsBeanFromJson.hasObjectReadPermission(user),
                cidsBeanFromRestServer.hasObjectReadPermission(user));

        Assert.assertEquals("cidsBean.hasObjectWritePermission(user) from legacy server matches",
                cidsBeanFromJson.hasObjectWritePermission(user),
                cidsBeanFromLegacyServer.hasObjectWritePermission(user));
        Assert.assertEquals("cidsBean.hasObjectWritePermission(user) from rest server matches",
                cidsBeanFromJson.hasObjectWritePermission(user),
                cidsBeanFromRestServer.hasObjectWritePermission(user));

        Assert.assertEquals("cidsBean.getHasWritePermission(user) from legacy server matches",
                cidsBeanFromJson.getHasWritePermission(user),
                cidsBeanFromLegacyServer.getHasWritePermission(user));
        Assert.assertEquals("cidsBean.getHasWritePermission(user) from rest server matches",
                cidsBeanFromJson.getHasWritePermission(user),
                cidsBeanFromRestServer.getHasWritePermission(user));

        // --> compareCidsBeanProperties()
//        for (String property : cidsBeanFromJson.getPropertyNames()) {
//            Assert.assertEquals("cidsBean.property(" + property + ") from legacy server matches",
//                    cidsBeanFromJson.getProperty(property),
//                    cidsBeanFromLegacyServer.getProperty(property));
//            Assert.assertEquals("cidsBean.getProperty(" + property + ") from rest server matches",
//                    cidsBeanFromJson.getProperty(property),
//                    cidsBeanFromRestServer.getProperty(property));
//        }
        Assert.assertEquals("cidsBean.toObjectString from legacy server matches",
                cidsBeanFromJson.toObjectString(),
                cidsBeanFromLegacyServer.toObjectString());
        Assert.assertEquals("cidsBean.toObjectStringtoObjectString from rest server matches",
                cidsBeanFromJson.toObjectString(),
                cidsBeanFromRestServer.toObjectString());

        Assert.assertEquals("cidsBean.hashCode() from legacy server matches",
                cidsBeanFromJson.hashCode(),
                cidsBeanFromLegacyServer.hashCode());
        Assert.assertEquals("cidsBean.hashCode from rest server matches",
                cidsBeanFromJson.hashCode(),
                cidsBeanFromRestServer.hashCode());

        Assert.assertEquals("JSON from legacy server matches",
                cidsBeanJson,
                cidsBeanJsonFromLegacyServer);
        Assert.assertEquals("JSON from rest server matches",
                cidsBeanJson,
                cidsBeanJsonFromRestServer);

        // ->  metaObject.getDebugString();
        // FIXME: DebugStrings Strings do not match -> Array Helper Object Ids are lost after deserialization
        // See Issue #165
//        Assert.assertEquals("cidsBean.getMOString from legacy server matches",
//                cidsBeanFromJson.getMOString(),
//                cidsBeanFromLegacyServer.getMOString());
//        Assert.assertEquals("cidsBean.getMOString from rest server matches",
//                cidsBeanFromJson.getMOString(),
//                cidsBeanFromRestServer.getMOString());
    }
}
