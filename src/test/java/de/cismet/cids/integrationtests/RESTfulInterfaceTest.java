package de.cismet.cids.integrationtests;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.impls.domainserver.OfflineMetaClassCacheService;
import Sirius.server.middleware.types.MetaClass;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
    protected static final Properties PROPERTIES = TestEnvironment.getProperties();

    protected static RESTfulInterfaceConnector restConnector = null;
    protected static RESTfulSerialInterfaceConnector legacyConnector = null;
    protected static User user = null;
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
            LOGGER.debug("CIDS_BEANS_JSON already initialised");
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
        Assert.assertNotNull("TestEnvironment created", PROPERTIES);

        // check container creation succeeded 
        Assert.assertNotNull("cidsRefContainer sucessfully created", dockerEnvironment);

        Assert.assertFalse("connecting to cids integrationtest docker containers could not be established",
                connectionFailed);

        try {

            final String brokerUrl = TestEnvironment.getCallserverUrl(dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                    Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))));

            // check connection to legacy server (broker f.k.a. callserver)
            if (!TestEnvironment.pingHostWithPost(dockerEnvironment.getServiceHost(SERVER_CONTAINER,
                    Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))),
                    dockerEnvironment.getServicePort(SERVER_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("broker.port", "9986"))),
                    "/callserver/binary/getUserGroupNames",
                    12)) {
                connectionFailed = true;
                throw new Exception(brokerUrl + " did not answer after 12 retries");
            }

            LOGGER.info("connecting to cids reference docker legacy server: " + brokerUrl);
            legacyConnector = new RESTfulSerialInterfaceConnector(brokerUrl);

            final String restServerUrl = "http://"
                    + dockerEnvironment.getServiceHost(REST_SERVER_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("restservice.port", "8890")))
                    + ":"
                    + dockerEnvironment.getServicePort(REST_SERVER_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("restservice.port", "8890")))
                    + "/";

            // check connection to rest server (cids-server-rest-legacy)
            if (!TestEnvironment.pingHostWithGet(dockerEnvironment.getServiceHost(REST_SERVER_CONTAINER,
                    Integer.parseInt(PROPERTIES.getProperty("restservice.port", "8890"))),
                    dockerEnvironment.getServicePort(REST_SERVER_CONTAINER,
                            Integer.parseInt(PROPERTIES.getProperty("restservice.port", "8890"))),
                    "/service/ping",
                    12)) {
                connectionFailed = true;
                throw new Exception(restServerUrl + " did not answer after 6 retries");
            }

            LOGGER.info("connecting to cids reference docker rest server: " + restServerUrl);
            restConnector = new RESTfulInterfaceConnector(restServerUrl);

            // authenticate user in rest connector
            user = restConnector.getUser(PROPERTIES.getProperty("usergroupDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("usergroup", "Administratoren"),
                    PROPERTIES.getProperty("userDomain", "CIDS_REF"),
                    PROPERTIES.getProperty("username", "admin"),
                    PROPERTIES.getProperty("password", "cismet"));

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

    /**
     * This is madness: @DataProviderClass does not support MetaClass, not
     * Interger Arrays not Integer Lists:
     * initializationError(de.cismet.cids.integrationtests.RESTfulInterfaceTest)
     * Time elapsed: 0.005 sec java.lang.Exception: Dataprovider method
     * 'getMetaClassIds' must either return Object[][], Object[], String[],
     * List<List<Object>> or List<Object>
     *
     * has to return String[]
     *
     * @return String[]
     * @throws Exception
     */
    @DataProvider
    public final static String[] getMetaClassIds() throws Exception {

        if (TestEnvironment.isIntegrationTestsEnabled()) {
            final Collection<MetaClass> metaClasses
                    = OfflineMetaClassCacheService.getInstance().getAllClasses(PROPERTIES.getProperty("userDomain", "CIDS_REF")).values();

            // 'MetaClass' is not supported as parameter type of test methods. 
            // Supported types are primitive types and their wrappers, case-sensitive 
            // 'Enum' values, 'String's, and types having a single 'String' parameter constructor.
            final String[] metaClassIds = new String[metaClasses.size()];
            int i = 0;
            for (MetaClass metaClass : metaClasses) {
                metaClassIds[i] = String.valueOf(metaClass.getId());
                i++;
            }
            return metaClassIds;

        } else {
            return new String[]{"-1"};
        }

    }

    @Before
    public void beforeTest() {
        Assert.assertNotNull("cids legacy server connection successfully established", legacyConnector);
        Assert.assertNotNull("cids rest server connection successfully established", restConnector);
        Assert.assertNotNull("user authenticated", user);
    }

    @Test
    @UseDataProvider("getMetaClassIds")
    public void test00getAndCompareMetaClasses(final Integer classId) throws Exception {

        LOGGER.debug("testing getAndCompareMetaClasses (" + classId + ")");

        final MetaClass metaClassFromJson
                = OfflineMetaClassCacheService.getInstance().getMetaClass(
                        PROPERTIES.getProperty("userDomain", "CIDS_REF"),
                        classId);

        LOGGER.debug("retrieving meta class "
                + metaClassFromJson.getKey()
                + " from legacy server");

        final MetaClass metaClassFromLegacyServer = legacyConnector.getClass(user,
                metaClassFromJson.getID(), metaClassFromJson.getDomain());

        LOGGER.debug("retrieving meta class "
                + metaClassFromJson.getKey()
                + " from rest server");

        final MetaClass metaClassFromRestServer = restConnector.getClass(user,
                metaClassFromJson.getID(), metaClassFromJson.getDomain());

        this.compareMetaClasses(metaClassFromJson,
                metaClassFromLegacyServer,
                metaClassFromRestServer);

        LOGGER.info("testing getAndCompareMetaClasses (" + classId + ") passed!");
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void test01getAndCompareMetaObjects(final String cidsBeanJson) throws Exception {
        LOGGER.debug("testing getAndCompareMetaObjects");

        String name = "";

        try {
            final CidsBean cidsBeanFromJson = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject metaObjectFromJson = cidsBeanFromJson.getMetaObject();

            name = (Arrays.asList(cidsBeanFromJson.getPropertyNames()).contains("name")
                    && cidsBeanFromJson.getProperty("name") != null)
                    ? cidsBeanFromJson.getProperty("name").toString()
                    : cidsBeanFromJson.getCidsBeanInfo().getObjectKey();

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
                    cidsBeanFromRestServer,
                    name);

            this.compareAll(metaObjectFromJson,
                    metaObjectFromLegacyServer,
                    metaObjectFromRestServer,
                    cidsBeanFromJson,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromRestServer);

        } catch (AssertionError ae) {
            LOGGER.error("getAndCompareCidsBeans(" + name + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during getAndCompareCidsBeans(" + name + "): " + ex.getMessage(), ex);
            throw ex;
        }

        LOGGER.info("getAndCompareMetaObjects(" + name + ") test passed");
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void test02updateAndCompareSimplePropertiesLegacy(final String cidsBeanJson) throws Exception {
        LOGGER.debug("testing updateAndCompareSimpleProperties");
        String name = null;

        // make sure that classes are up to date before trying to make updates
        if (!OfflineMetaClassCacheService.getInstance().isOnline()) {
            OfflineMetaClassCacheService.getInstance().updateFromServer(user, legacyConnector);
        }

        try {
            final CidsBean cidsBeanFromJson = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            final MetaObject metaObjectFromJson = cidsBeanFromJson.getMetaObject();

            name = (Arrays.asList(cidsBeanFromJson.getPropertyNames()).contains("name")
                    && cidsBeanFromJson.getProperty("name") != null)
                    ? cidsBeanFromJson.getProperty("name").toString()
                    : cidsBeanFromJson.getCidsBeanInfo().getObjectKey();

            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " (" + cidsBeanFromJson.getProperty("name") + ") from legacy server");
            MetaObject metaObjectFromLegacyServer = legacyConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            CidsBean cidsBeanFromLegacyServer = metaObjectFromLegacyServer.getBean();

            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " (" + cidsBeanFromJson.getProperty("name") + ") from rest server");
            MetaObject metaObjectFromRestServer = restConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            CidsBean cidsBeanFromRestServer = metaObjectFromRestServer.getBean();

            final long time = System.currentTimeMillis();
            name = "Lucky Casino";

            cidsBeanFromJson.setProperty("name", name);
            cidsBeanFromLegacyServer.setProperty("name", name);
            cidsBeanFromRestServer.setProperty("name", name);

            cidsBeanFromJson.setProperty("anzahl_sitzplaetze", 666);
            cidsBeanFromLegacyServer.setProperty("anzahl_sitzplaetze", 666);
            cidsBeanFromRestServer.setProperty("anzahl_sitzplaetze", 666);

            cidsBeanFromJson.setProperty("alkohol_ausschank", false);
            cidsBeanFromLegacyServer.setProperty("alkohol_ausschank", false);
            cidsBeanFromRestServer.setProperty("alkohol_ausschank", false);

            cidsBeanFromJson.setProperty("verbandsnummer", "QWERTZ");
            cidsBeanFromLegacyServer.setProperty("verbandsnummer", "QWERTZ");
            cidsBeanFromRestServer.setProperty("verbandsnummer", "QWERTZ");

            cidsBeanFromJson.setProperty("genehmigung_bis", new java.sql.Date(time));
            cidsBeanFromLegacyServer.setProperty("genehmigung_bis", new java.sql.Date(time));
            cidsBeanFromRestServer.setProperty("genehmigung_bis", new java.sql.Date(time));

            cidsBeanFromJson.setProperty("letze_aenderung", new java.sql.Timestamp(time));
            cidsBeanFromLegacyServer.setProperty("letze_aenderung", new java.sql.Timestamp(time));
            cidsBeanFromRestServer.setProperty("letze_aenderung", new java.sql.Timestamp(time));

            this.compareCidsBeanProperties(cidsBeanFromJson,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromRestServer,
                    name);

            this.compareAll(metaObjectFromJson,
                    metaObjectFromLegacyServer,
                    metaObjectFromRestServer,
                    cidsBeanFromJson,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromRestServer);

            LOGGER.info("persisting meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " (" + cidsBeanFromJson.getProperty("name") + ") to legacy server");

            legacyConnector.updateMetaObject(user,
                    metaObjectFromJson,
                    metaObjectFromJson.getDomain());

            // CidsBean PerstService Lookup failed!
            //final CidsBean persistedCidsBeanFromLegacyServer = cidsBeanFromLegacyServer.persist();
            //final MetaObject persistedMetaObjectFromLegacyServer = persistedCidsBeanFromLegacyServer.getMetaObject();
            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " (" + cidsBeanFromJson.getProperty("name") + ") from legacy server");

            metaObjectFromLegacyServer = legacyConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            cidsBeanFromLegacyServer = metaObjectFromLegacyServer.getBean();

            LOGGER.debug("retrieving meta object "
                    + cidsBeanFromJson.getCidsBeanInfo().getJsonObjectKey()
                    + " (" + cidsBeanFromJson.getProperty("name") + ") from rest server");
            metaObjectFromRestServer = restConnector.getMetaObject(user,
                    metaObjectFromJson.getID(),
                    metaObjectFromJson.getClassID(), metaObjectFromJson.getDomain());
            cidsBeanFromRestServer = metaObjectFromRestServer.getBean();

            this.compareCidsBeanProperties(cidsBeanFromJson,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromRestServer,
                    name);

            this.compareAll(metaObjectFromJson,
                    metaObjectFromLegacyServer,
                    metaObjectFromRestServer,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromLegacyServer,
                    cidsBeanFromRestServer);

        } catch (AssertionError ae) {
            LOGGER.error("updateAndCompareSimpleProperties(" + name + ") test failed with: " + ae.getMessage(), ae);
            throw ae;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during updateAndCompareSimpleProperties(" + name + ") : " + ex.getMessage(), ex);
            throw ex;
        }

        LOGGER.info("updateAndCompareSimpleProperties(" + name + ") test passed!");
    }

    protected void compareCidsBeanProperties(final CidsBean cidsBeanFromJson,
            final CidsBean cidsBeanFromLegacyServer,
            final CidsBean cidsBeanFromRestServer,
            final String name) {
        for (final String propertyName : cidsBeanFromJson.getPropertyNames()) {
            final Object propertyFromJson = cidsBeanFromJson.getProperty(propertyName);
            final Object propertyFromLegacyServer = cidsBeanFromLegacyServer.getProperty(propertyName);
            final Object propertyFromRestServer = cidsBeanFromRestServer.getProperty(propertyName);

            if (propertyFromJson != null) {
                Assert.assertNotNull("cidsBean[" + name + "].getProperty(" + propertyName + ") FromLegacyServer is not null",
                        propertyFromLegacyServer);
                Assert.assertNotNull("cidsBean[" + name + "].getProperty(" + propertyName + ") FromRestServer is not null",
                        propertyFromRestServer);

                if (CidsBean.class.isAssignableFrom(propertyFromJson.getClass())) {

                    Assert.assertTrue("cidsBean[" + name + "].getProperty(" + propertyName + ") FromLegacyServer is a CidsBean",
                            CidsBean.class.isAssignableFrom(propertyFromLegacyServer.getClass()));
                    Assert.assertTrue("cidsBean[" + name + "].getProperty(" + propertyName + ") FromRestServer is a CidsBean",
                            CidsBean.class.isAssignableFrom(propertyFromRestServer.getClass()));

                    final CidsBean cidsBeanPropertyFromJson = (CidsBean) propertyFromJson;
                    final CidsBean cidsBeanPropertyLegacyServer = (CidsBean) propertyFromLegacyServer;
                    final CidsBean cidsBeanPropertyFromRestServer = (CidsBean) propertyFromRestServer;

                    this.compareCidsBeans(cidsBeanPropertyFromJson,
                            cidsBeanPropertyLegacyServer,
                            cidsBeanPropertyFromRestServer);

                    this.compareCidsBeanProperties(cidsBeanPropertyFromJson,
                            cidsBeanPropertyLegacyServer,
                            cidsBeanPropertyFromRestServer,
                            name);

                } else if (Collection.class.isAssignableFrom(propertyFromJson.getClass())
                        && !((Collection) propertyFromJson).isEmpty()) {

                    Assert.assertTrue("cidsBean[" + name + "].getProperty(" + propertyName + ") FromLegacyServer is a CidsBean Collection",
                            Collection.class.isAssignableFrom(propertyFromLegacyServer.getClass()));
                    Assert.assertTrue("cidsBean[" + name + "].getProperty(" + propertyName + ") FromRestServer is a CidsBean Collection",
                            Collection.class.isAssignableFrom(propertyFromRestServer.getClass()));

                    final Collection<CidsBean> cidsBeanCollectionPropertyFromJson
                            = (Collection<CidsBean>) propertyFromJson;
                    final Collection<CidsBean> cidsBeanCollectionPropertyFromLegacyServer
                            = (Collection<CidsBean>) propertyFromLegacyServer;
                    final Collection<CidsBean> cidsBeanCollectionPropertyFromRestServer
                            = (Collection<CidsBean>) propertyFromRestServer;

                    Assert.assertEquals("cidsBean[" + name + "].cidsBeanCollectionPropertyLegacyServer server matches size",
                            cidsBeanCollectionPropertyFromJson.size(),
                            cidsBeanCollectionPropertyFromLegacyServer.size());
                    Assert.assertEquals("cidsBean[" + name + "].cidsBeanCollectionPropertyFromRestServer server matches size",
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
                                collectionCidsBeanFromRestServer,
                                name);
                    }

                } else {

                    Assert.assertTrue("cidsBean[" + name + "].getProperty(" + propertyName + ") FromLegacyServer is a " + propertyFromJson.getClass(),
                            propertyFromJson.getClass().isAssignableFrom(propertyFromLegacyServer.getClass()));
                    Assert.assertTrue("cidsBean[" + name + "].getProperty(" + propertyName + ") FromRestServer is a " + propertyFromJson.getClass(),
                            propertyFromJson.getClass().isAssignableFrom(propertyFromRestServer.getClass()));

                    // java.sql.Date object comparision does not work
                    // probably due to fix implemented in #164
                    if (java.sql.Date.class.isAssignableFrom(propertyFromJson.getClass())) {
                        Assert.assertEquals("cidsBean[" + name + "].getProperty(" + propertyName + ") from legacy server matches",
                                propertyFromJson.toString(),
                                propertyFromLegacyServer.toString());
                        Assert.assertEquals("cidsBean[" + name + "].getProperty(" + propertyName + ") from rest server matches",
                                propertyFromJson.toString(),
                                propertyFromRestServer.toString());

                        // java.sql.Timestamp object comparision does not work
                        // nanoseconds not serialized despite of SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
                        // see also http://stackoverflow.com/questions/27847203/is-it-possible-to-use-jackson-with-nanoseconds-value
                    } else if (java.sql.Timestamp.class.isAssignableFrom(propertyFromJson.getClass())) {
                        Assert.assertEquals("cidsBean[" + name + "].getProperty(" + propertyName + ") from legacy server matches",
                                ((java.sql.Timestamp) propertyFromJson).getTime(),
                                ((java.sql.Timestamp) propertyFromLegacyServer).getTime());
                        Assert.assertEquals("cidsBean[" + name + "].getProperty(" + propertyName + ") from rest server matches",
                                ((java.sql.Timestamp) propertyFromJson).getTime(),
                                ((java.sql.Timestamp) propertyFromRestServer).getTime());
                    } else {
                        Assert.assertEquals("cidsBean[" + name + "].getProperty(" + propertyName + ") from legacy server matches",
                                propertyFromJson,
                                propertyFromLegacyServer);
                        Assert.assertEquals("cidsBean[" + name + "].getProperty(" + propertyName + ") from rest server matches",
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

        final int pk = metaObjectFromJson.getId();
        final String name = metaObjectFromJson.getAttribute("name") != null
                ? metaObjectFromJson.getAttribute("name").toString()
                : String.valueOf(pk);

        Assert.assertEquals("metaObject[" + name + "].getClassID() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getClassID(),
                metaObjectFromLegacyServer.getClassID());
        Assert.assertEquals("metaObject[" + name + "].getClassID() from rest server matches (" + pk + ")",
                metaObjectFromJson.getClassID(),
                metaObjectFromRestServer.getClassID());

        Assert.assertEquals("metaObject[" + name + "].getClassKey() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getClassKey(),
                metaObjectFromLegacyServer.getClassKey());
        Assert.assertEquals("metaObject[" + name + "].getClassKey() from rest server matches (" + pk + ")",
                metaObjectFromJson.getClassKey(),
                metaObjectFromRestServer.getClassKey());

        Assert.assertEquals("metaObject[" + name + "].getComplexEditor() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getComplexEditor(),
                metaObjectFromLegacyServer.getComplexEditor());
        Assert.assertEquals("metaObject[" + name + "].getComplexEditor() from rest server matches (" + pk + ")",
                metaObjectFromJson.getComplexEditor(),
                metaObjectFromRestServer.getComplexEditor());

        Assert.assertEquals("metaObject[" + name + "].getDescription() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getDescription(),
                metaObjectFromLegacyServer.getDescription());
        Assert.assertEquals("metaObject[" + name + "].getDescription() from rest server matches (" + pk + ")",
                metaObjectFromJson.getDescription(),
                metaObjectFromRestServer.getDescription());

        Assert.assertEquals("metaObject[" + name + "].getDomain() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getDomain(),
                metaObjectFromLegacyServer.getDomain());
        Assert.assertEquals("metaObject[" + name + "].getDomain() from rest server matches (" + pk + ")",
                metaObjectFromJson.getDomain(),
                metaObjectFromRestServer.getDomain());

        Assert.assertEquals("metaObject[" + name + "].getEditor() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getEditor(),
                metaObjectFromLegacyServer.getEditor());
        Assert.assertEquals("metaObject[" + name + "].getEditor() from rest server matches (" + pk + ")",
                metaObjectFromJson.getEditor(),
                metaObjectFromRestServer.getEditor());

        Assert.assertEquals("metaObject[" + name + "].getGroup() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getGroup(),
                metaObjectFromLegacyServer.getGroup());
        Assert.assertEquals("metaObject[" + name + "].getGroup() from rest server matches (" + pk + ")",
                metaObjectFromJson.getGroup(),
                metaObjectFromRestServer.getGroup());

        Assert.assertEquals("metaObject[" + name + "].getID() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getID(),
                metaObjectFromLegacyServer.getID());
        Assert.assertEquals("metaObject[" + name + "].getID() from rest server matches (" + pk + ")",
                metaObjectFromJson.getID(),
                metaObjectFromRestServer.getID());

        Assert.assertEquals("metaObject[" + name + "].getId() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getId(),
                metaObjectFromLegacyServer.getId());
        Assert.assertEquals("metaObject[" + name + "].getId() from rest server matches (" + pk + ")",
                metaObjectFromJson.getId(),
                metaObjectFromRestServer.getId());

        Assert.assertEquals("metaObject[" + name + "].getKey() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getKey(),
                metaObjectFromLegacyServer.getKey());
        Assert.assertEquals("metaObject[" + name + "].getKey() from rest server matches (" + pk + ")",
                metaObjectFromJson.getKey(),
                metaObjectFromRestServer.getKey());

        Assert.assertEquals("metaObject[" + name + "].getName() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getName(),
                metaObjectFromLegacyServer.getName());
        Assert.assertEquals("metaObject[" + name + "].getName() from rest server matches (" + pk + ")",
                metaObjectFromJson.getName(),
                metaObjectFromRestServer.getName());

        // FIXME: Property Strings do not match -> Array Helper Object Ids are lost after deserialization
        // See Issue #165
//        LOGGER.debug(metaObjectFromJson.getPropertyString());
//        LOGGER.debug(metaObjectFromLegacyServer.getPropertyString());
//        Assert.assertEquals("metaObject["+name+"].getPropertyString() from legacy server matches ("+pk+")",
//                metaObjectFromJson.getPropertyString(),
//                metaObjectFromLegacyServer.getPropertyString());
//        Assert.assertEquals("metaObject["+name+"].getPropertyString() from rest server matches ("+pk+")",
//                metaObjectFromJson.getPropertyString(),
//                metaObjectFromRestServer.getPropertyString());
        Assert.assertEquals("metaObject[" + name + "].getRenderer() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getRenderer(),
                metaObjectFromLegacyServer.getRenderer());
        Assert.assertEquals("metaObject[" + name + "].getRenderer() from rest server matches (" + pk + ")",
                metaObjectFromJson.getRenderer(),
                metaObjectFromRestServer.getRenderer());

        Assert.assertEquals("metaObject[" + name + "].getSimpleEditor() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getSimpleEditor(),
                metaObjectFromLegacyServer.getSimpleEditor());
        Assert.assertEquals("metaObject[" + name + "].getSimpleEditor() from rest server matches (" + pk + ")",
                metaObjectFromJson.getSimpleEditor(),
                metaObjectFromRestServer.getSimpleEditor());

        Assert.assertEquals("metaObject[" + name + "].getStatus() from legacy server matches (" + pk + ")",
                metaObjectFromJson.getStatus(),
                metaObjectFromLegacyServer.getStatus());
        Assert.assertEquals("metaObject[" + name + "].getStatus() from rest server matches (" + pk + ")",
                metaObjectFromJson.getStatus(),
                metaObjectFromRestServer.getStatus());

        // FIXME: DebugStrings Strings do not match -> Array Helper Object Ids are lost after deserialization
        // See Issue #165
        //        Assert.assertEquals("metaObject["+name+"].getStatusDebugString() from legacy server matches ("+pk+")",
        //                metaObjectFromJson.getStatusDebugString(),
        //                metaObjectFromLegacyServer.getStatusDebugString());
        //        Assert.assertEquals("metaObject["+name+"].getStatusDebugString() from rest server matches ("+pk+")",
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

        final int pk = cidsBeanFromJson.getPrimaryKeyValue();
        final String name = (Arrays.asList(cidsBeanFromJson.getPropertyNames()).contains("name")
                && cidsBeanFromJson.getProperty("name") != null)
                ? cidsBeanFromJson.getProperty("name").toString()
                : beanInfoFromJson.getJsonObjectKey();

        Assert.assertEquals("cidsBean[" + name + "] JsonObjectKey key from legacy server matches (" + pk + ")",
                beanInfoFromJson.getJsonObjectKey(),
                beanInfoFromLegacyServer.getJsonObjectKey());
        Assert.assertEquals("cidsBean[" + name + "] JsonObjectKey key from rest server matches (" + pk + ")",
                beanInfoFromJson.getJsonObjectKey(),
                beanInfoFromRestServer.getJsonObjectKey());

        Assert.assertEquals("cidsBean[" + name + "].getPrimaryKeyFieldname() from legacy server matches (" + pk + ")",
                cidsBeanFromJson.getPrimaryKeyFieldname(),
                cidsBeanFromLegacyServer.getPrimaryKeyFieldname());
        Assert.assertEquals("cidsBean[" + name + "].getPrimaryKeyFieldname from rest server matches (" + pk + ")",
                cidsBeanFromJson.getPrimaryKeyFieldname(),
                cidsBeanFromRestServer.getPrimaryKeyFieldname());

        Assert.assertEquals("cidsBean[" + name + "].getPrimaryKeyValue() from legacy server matches (" + pk + ")",
                cidsBeanFromJson.getPrimaryKeyValue(),
                cidsBeanFromLegacyServer.getPrimaryKeyValue());
        Assert.assertEquals("cidsBean[" + name + "].getPrimaryKeyValue from rest server matches (" + pk + ")",
                cidsBeanFromJson.getPrimaryKeyValue(),
                cidsBeanFromRestServer.getPrimaryKeyValue());

        Assert.assertArrayEquals("cidsBean[" + name + "].getPropertyNames() from legacy server matches (" + pk + ")",
                cidsBeanFromJson.getPropertyNames(),
                cidsBeanFromLegacyServer.getPropertyNames());
        Assert.assertArrayEquals("cidsBean[" + name + "].getPropertyNames from rest server matches (" + pk + ")",
                cidsBeanFromJson.getPropertyNames(),
                cidsBeanFromRestServer.getPropertyNames());

        Assert.assertEquals("cidsBean[" + name + "].hasArtificialChangeFlag() from legacy server matches (" + pk + ")",
                cidsBeanFromJson.hasArtificialChangeFlag(),
                cidsBeanFromLegacyServer.hasArtificialChangeFlag());
        Assert.assertEquals("cidsBean[" + name + "].hasArtificialChangeFlag from rest server matches (" + pk + ")",
                cidsBeanFromJson.hasArtificialChangeFlag(),
                cidsBeanFromRestServer.hasArtificialChangeFlag());

        Assert.assertEquals("cidsBean[" + name + "].hasObjectReadPermission(user) from legacy server matches (" + pk + ")",
                cidsBeanFromJson.hasObjectReadPermission(user),
                cidsBeanFromLegacyServer.hasObjectReadPermission(user));
        Assert.assertEquals("cidsBean[" + name + "].hasObjectReadPermission(user) from rest server matches (" + pk + ")",
                cidsBeanFromJson.hasObjectReadPermission(user),
                cidsBeanFromRestServer.hasObjectReadPermission(user));

        Assert.assertEquals("cidsBean[" + name + "].hasObjectWritePermission(user) from legacy server matches (" + pk + ")",
                cidsBeanFromJson.hasObjectWritePermission(user),
                cidsBeanFromLegacyServer.hasObjectWritePermission(user));
        Assert.assertEquals("cidsBean[" + name + "].hasObjectWritePermission(user) from rest server matches (" + pk + ")",
                cidsBeanFromJson.hasObjectWritePermission(user),
                cidsBeanFromRestServer.hasObjectWritePermission(user));

        // Permission API not implemented in REST Server (cismet/cids-server-rest#50). Related integration tests disabled!
//        Assert.assertEquals("cidsBean["+name+"].getHasWritePermission(user) from legacy server matches ("+pk+")",
//                cidsBeanFromJson.getHasWritePermission(user),
//                cidsBeanFromLegacyServer.getHasWritePermission(user));
//        Assert.assertEquals("cidsBean["+name+"].getHasWritePermission(user) from rest server matches ("+pk+")",
//                cidsBeanFromJson.getHasWritePermission(user),
//                cidsBeanFromRestServer.getHasWritePermission(user));
        // --> compareCidsBeanProperties()
//        for (String property : cidsBeanFromJson.getPropertyNames()) {
//            Assert.assertEquals("cidsBean["+name+"].property(" + property + ") from legacy server matches ("+pk+")",
//                    cidsBeanFromJson.getProperty(property),
//                    cidsBeanFromLegacyServer.getProperty(property));
//            Assert.assertEquals("cidsBean["+name+"].getProperty(" + property + ") from rest server matches ("+pk+")",
//                    cidsBeanFromJson.getProperty(property),
//                    cidsBeanFromRestServer.getProperty(property));
//        }
        Assert.assertEquals("cidsBean[" + name + "].toObjectString from legacy server matches (" + pk + ")",
                cidsBeanFromJson.toObjectString(),
                cidsBeanFromLegacyServer.toObjectString());
        Assert.assertEquals("cidsBean[" + name + "].toObjectStringtoObjectString from rest server matches (" + pk + ")",
                cidsBeanFromJson.toObjectString(),
                cidsBeanFromRestServer.toObjectString());

        Assert.assertEquals("cidsBean[" + name + "].hashCode() from legacy server matches (" + pk + ")",
                cidsBeanFromJson.hashCode(),
                cidsBeanFromLegacyServer.hashCode());
        Assert.assertEquals("cidsBean[" + name + "].hashCode from rest server matches (" + pk + ")",
                cidsBeanFromJson.hashCode(),
                cidsBeanFromRestServer.hashCode());

        Assert.assertEquals("cidsBean[" + name + "] JSON from legacy server matches (" + pk + ")",
                cidsBeanJson,
                cidsBeanJsonFromLegacyServer);
        Assert.assertEquals("cidsBean[" + name + "] JSON from rest server matches (" + pk + ")",
                cidsBeanJson,
                cidsBeanJsonFromRestServer);

        // ->  metaObject["+name+"].getDebugString();
        // FIXME: DebugStrings Strings do not match -> Array Helper Object Ids are lost after deserialization
        // See Issue #165
//        Assert.assertEquals("cidsBean["+name+"].getMOString from legacy server matches ("+pk+")",
//                cidsBeanFromJson.getMOString(),
//                cidsBeanFromLegacyServer.getMOString());
//        Assert.assertEquals("cidsBean["+name+"].getMOString from rest server matches ("+pk+")",
//                cidsBeanFromJson.getMOString(),
//                cidsBeanFromRestServer.getMOString());
    }

    protected void compareMetaClasses(final MetaClass metaClassFromJson,
            final MetaClass metaClassFromLegacyServer,
            final MetaClass metaClassFromRestServer) throws AssertionError {

        final int pk = metaClassFromJson.getID();
        final String name = metaClassFromJson.getName();

        Assert.assertEquals("metaClass[" + name + "].getID()() from rest server matches (" + pk + ")",
                metaClassFromJson.getID(),
                metaClassFromRestServer.getID());
        Assert.assertEquals("metaClass[" + name + "].getID() from legacy server matches (" + pk + ")",
                metaClassFromJson.getID(),
                metaClassFromLegacyServer.getID());

        Assert.assertEquals("metaClass[" + name + "].getId()() from rest server matches (" + pk + ")",
                metaClassFromJson.getId(),
                metaClassFromRestServer.getId());
        Assert.assertEquals("metaClass[" + name + "].getId() from legacy server matches (" + pk + ")",
                metaClassFromJson.getId(),
                metaClassFromLegacyServer.getId());

        Assert.assertEquals("metaClass[" + name + "].getId()() from rest server matches (" + pk + ")",
                metaClassFromJson.getComplexEditor(),
                metaClassFromRestServer.getComplexEditor());
        Assert.assertEquals("metaClass[" + name + "].getComplexEditor() from legacy server matches (" + pk + ")",
                metaClassFromJson.getComplexEditor(),
                metaClassFromLegacyServer.getComplexEditor());

        Assert.assertEquals("metaClass[" + name + "].getId()() from rest server matches (" + pk + ")",
                metaClassFromJson.getDescription(),
                metaClassFromRestServer.getDescription());
        Assert.assertEquals("metaClass[" + name + "].getDescription() from legacy server matches (" + pk + ")",
                metaClassFromJson.getDescription(),
                metaClassFromLegacyServer.getDescription());

        Assert.assertEquals("metaClass[" + name + "].getDomain()() from rest server matches (" + pk + ")",
                metaClassFromJson.getDomain(),
                metaClassFromRestServer.getDomain());
        Assert.assertEquals("metaClass[" + name + "].getDomain() from legacy server matches (" + pk + ")",
                metaClassFromJson.getDomain(),
                metaClassFromLegacyServer.getDomain());

        Assert.assertEquals("metaClass[" + name + "].getEditor()() from rest server matches (" + pk + ")",
                metaClassFromJson.getEditor(),
                metaClassFromRestServer.getEditor());
        Assert.assertEquals("metaClass[" + name + "].getEditor() from legacy server matches (" + pk + ")",
                metaClassFromJson.getEditor(),
                metaClassFromLegacyServer.getEditor());

        Assert.assertEquals("metaClass[" + name + "].getGetDefaultInstanceStmnt()() from rest server matches (" + pk + ")",
                metaClassFromJson.getGetDefaultInstanceStmnt(),
                metaClassFromRestServer.getGetDefaultInstanceStmnt());
        Assert.assertEquals("metaClass[" + name + "].getGetDefaultInstanceStmnt() from legacy server matches (" + pk + ")",
                metaClassFromJson.getGetDefaultInstanceStmnt(),
                metaClassFromLegacyServer.getGetDefaultInstanceStmnt());

        Assert.assertEquals("metaClass[" + name + "].getGetInstanceStmnt()() from rest server matches (" + pk + ")",
                metaClassFromJson.getGetInstanceStmnt(),
                metaClassFromRestServer.getGetInstanceStmnt());
        Assert.assertEquals("metaClass[" + name + "].getGetInstanceStmnt() from legacy server matches (" + pk + ")",
                metaClassFromJson.getGetInstanceStmnt(),
                metaClassFromLegacyServer.getGetInstanceStmnt());

        Assert.assertEquals("metaClass[" + name + "].getGetInstanceStmnt()() from rest server matches (" + pk + ")",
                metaClassFromJson.getGetInstanceStmnt(),
                metaClassFromRestServer.getGetInstanceStmnt());
        Assert.assertEquals("metaClass[" + name + "].getGetInstanceStmnt() from legacy server matches (" + pk + ")",
                metaClassFromJson.getGetInstanceStmnt(),
                metaClassFromLegacyServer.getGetInstanceStmnt());

        Assert.assertEquals("metaClass[" + name + "].getAttributePolicy() from rest server matches (" + pk + ")",
                metaClassFromJson.getAttributePolicy().toString(),
                metaClassFromRestServer.getAttributePolicy().toString());
        Assert.assertEquals("metaClass[" + name + "].getAttributePolicy() from legacy server matches (" + pk + ")",
                metaClassFromJson.getAttributePolicy().toString(),
                metaClassFromLegacyServer.getAttributePolicy().toString());

        Assert.assertEquals("metaClass[" + name + "].getGroup() from rest server matches (" + pk + ")",
                metaClassFromJson.getGroup(),
                metaClassFromRestServer.getGroup());
        Assert.assertEquals("metaClass[" + name + "].getGroup() from legacy server matches (" + pk + ")",
                metaClassFromJson.getGroup(),
                metaClassFromLegacyServer.getGroup());

        Assert.assertEquals("metaClass[" + name + "].getJavaClass() from rest server matches (" + pk + ")",
                metaClassFromJson.getJavaClass(),
                metaClassFromRestServer.getJavaClass());
        Assert.assertEquals("metaClass[" + name + "].getJavaClass() from legacy server matches (" + pk + ")",
                metaClassFromJson.getJavaClass(),
                metaClassFromLegacyServer.getJavaClass());

        Assert.assertEquals("metaClass[" + name + "].getKey() from rest server matches (" + pk + ")",
                metaClassFromJson.getKey(),
                metaClassFromRestServer.getKey());
        Assert.assertEquals("metaClass[" + name + "].getKey() from legacy server matches (" + pk + ")",
                metaClassFromJson.getKey(),
                metaClassFromLegacyServer.getKey());

        Assert.assertEquals("metaClass[" + name + "].getName() from rest server matches (" + pk + ")",
                metaClassFromJson.getName(),
                metaClassFromRestServer.getName());
        Assert.assertEquals("metaClass[" + name + "].getName() from legacy server matches (" + pk + ")",
                metaClassFromJson.getName(),
                metaClassFromLegacyServer.getName());

        Assert.assertEquals("metaClass[" + name + "].getPolicy().toString() from rest server matches (" + pk + ")",
                metaClassFromJson.getPolicy().toString(),
                metaClassFromRestServer.getPolicy().toString());
        Assert.assertEquals("metaClass[" + name + "].getPolicy().toString() from legacy server matches (" + pk + ")",
                metaClassFromJson.getPolicy().toString(),
                metaClassFromLegacyServer.getPolicy().toString());

        Assert.assertEquals("metaClass[" + name + "].getPrimaryKey() from rest server matches (" + pk + ")",
                metaClassFromJson.getPrimaryKey(),
                metaClassFromRestServer.getPrimaryKey());
        Assert.assertEquals("metaClass[" + name + "].getPrimaryKey() from legacy server matches (" + pk + ")",
                metaClassFromJson.getPrimaryKey(),
                metaClassFromLegacyServer.getPrimaryKey());

        Assert.assertEquals("metaClass[" + name + "].getRenderer() from rest server matches (" + pk + ")",
                metaClassFromJson.getRenderer(),
                metaClassFromRestServer.getRenderer());
        Assert.assertEquals("metaClass[" + name + "].getRenderer() from legacy server matches (" + pk + ")",
                metaClassFromJson.getRenderer(),
                metaClassFromLegacyServer.getRenderer());

        Assert.assertEquals("metaClass[" + name + "].getSQLFieldNames() from rest server matches (" + pk + ")",
                metaClassFromJson.getSQLFieldNames(),
                metaClassFromRestServer.getSQLFieldNames());
        Assert.assertEquals("metaClass[" + name + "].getSQLFieldNames() from legacy server matches (" + pk + ")",
                metaClassFromJson.getSQLFieldNames(),
                metaClassFromLegacyServer.getSQLFieldNames());

        Assert.assertEquals("metaClass[" + name + "].getSimpleEditor() from rest server matches (" + pk + ")",
                metaClassFromJson.getSimpleEditor(),
                metaClassFromRestServer.getSimpleEditor());
        Assert.assertEquals("metaClass[" + name + "].getSimpleEditor() from legacy server matches (" + pk + ")",
                metaClassFromJson.getSimpleEditor(),
                metaClassFromLegacyServer.getSimpleEditor());

        Assert.assertEquals("metaClass[" + name + "].getTableName() from rest server matches (" + pk + ")",
                metaClassFromJson.getTableName(),
                metaClassFromRestServer.getTableName());
        Assert.assertEquals("metaClass[" + name + "].getTableName() from legacy server matches (" + pk + ")",
                metaClassFromJson.getTableName(),
                metaClassFromLegacyServer.getTableName());

        Assert.assertEquals("metaClass[" + name + "].isArrayElementLink() from rest server matches (" + pk + ")",
                metaClassFromJson.isArrayElementLink(),
                metaClassFromRestServer.isArrayElementLink());
        Assert.assertEquals("metaClass[" + name + "].isArrayElementLink() from legacy server matches (" + pk + ")",
                metaClassFromJson.isArrayElementLink(),
                metaClassFromLegacyServer.isArrayElementLink());

        Assert.assertEquals("metaClass[" + name + "].isIndexed() from rest server matches (" + pk + ")",
                metaClassFromJson.isIndexed(),
                metaClassFromRestServer.isIndexed());
        Assert.assertEquals("metaClass[" + name + "].isIndexed() from legacy server matches (" + pk + ")",
                metaClassFromJson.isIndexed(),
                metaClassFromLegacyServer.isIndexed());

        final ClassAttribute[] metaClassAttributesFromJson = metaClassFromJson.getAttribs();
        final ClassAttribute[] metaClassAttributesFromRestServer = metaClassFromRestServer.getAttribs();
        final ClassAttribute[] metaClassAttributesFromLegacyServer = metaClassFromLegacyServer.getAttribs();

        Assert.assertEquals("metaClass[" + name + "].getAttribs().length from rest server matches (" + pk + ")",
                metaClassAttributesFromJson.length,
                metaClassAttributesFromRestServer.length);
        Assert.assertEquals("metaClass[" + name + "].getAttribs().length from legacy server matches (" + pk + ")",
                metaClassAttributesFromJson.length,
                metaClassAttributesFromLegacyServer.length);

        for (int i = 0; i < metaClassAttributesFromJson.length; i++) {
            this.compareMetaClassAttributes(
                    metaClassAttributesFromJson[i],
                    metaClassAttributesFromLegacyServer[i],
                    metaClassAttributesFromRestServer[i],
                    pk);
        }

        final MetaObject metaObjectFromJson = metaClassFromJson.getEmptyInstance();
        final MetaObject metaObjectFromRestServer = metaClassFromRestServer.getEmptyInstance();
        final MetaObject metaObjectFromLegacyServer = metaClassFromLegacyServer.getEmptyInstance();

        this.compareMetaObjects(metaObjectFromJson,
                metaObjectFromLegacyServer,
                metaObjectFromRestServer);

        final CidsBean cidsBeanFromJson = metaObjectFromJson.getBean();
        final CidsBean cidsBeanFromRestServer = metaObjectFromRestServer.getBean();
        final CidsBean cidsBeanFromLegacyServer = metaObjectFromLegacyServer.getBean();

        this.compareCidsBeans(cidsBeanFromJson,
                cidsBeanFromLegacyServer,
                cidsBeanFromRestServer);
    }

    protected void compareMetaClassAttributes(final ClassAttribute metaClassAttributeFromJson,
            final ClassAttribute metaClassAttributeFromLegacyServer,
            final ClassAttribute metaClassAttributeFromRestServer,
            final int pk) throws AssertionError {

        final String name = metaClassAttributeFromJson.getName();

        Assert.assertEquals("metaClassAttribute[" + name + "].getClassID() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getClassID(),
                metaClassAttributeFromRestServer.getClassID());
        Assert.assertEquals("metaClassAttribute[" + name + "].getClassID() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getClassID(),
                metaClassAttributeFromLegacyServer.getClassID());

        Assert.assertEquals("metaClassAttribute[" + name + "].getClassKey() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getClassKey(),
                metaClassAttributeFromRestServer.getClassKey());
        Assert.assertEquals("metaClassAttribute[" + name + "].getClassKey() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getClassKey(),
                metaClassAttributeFromLegacyServer.getClassKey());

        Assert.assertEquals("metaClassAttribute[" + name + "].getDescription() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getDescription(),
                metaClassAttributeFromRestServer.getDescription());
        Assert.assertEquals("metaClassAttribute[" + name + "].getDescription() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getDescription(),
                metaClassAttributeFromLegacyServer.getDescription());

        Assert.assertEquals("metaClassAttribute[" + name + "].getID() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getID(),
                metaClassAttributeFromRestServer.getID());
        Assert.assertEquals("metaClassAttribute[" + name + "].getID() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getID(),
                metaClassAttributeFromLegacyServer.getID());

        Assert.assertEquals("metaClassAttribute[" + name + "].getJavaType() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getJavaType(),
                metaClassAttributeFromRestServer.getJavaType());
        Assert.assertEquals("metaClassAttribute[" + name + "].getJavaType() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getJavaType(),
                metaClassAttributeFromLegacyServer.getJavaType());

        Assert.assertEquals("metaClassAttribute[" + name + "].getKey() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getKey(),
                metaClassAttributeFromRestServer.getKey());
        Assert.assertEquals("metaClassAttribute[" + name + "].getKey() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getKey(),
                metaClassAttributeFromLegacyServer.getKey());

        Assert.assertEquals("metaClassAttribute[" + name + "].getPermissions().toString() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getPermissions().toString(),
                metaClassAttributeFromRestServer.getPermissions().toString());
        Assert.assertEquals("metaClassAttribute[" + name + "].getPermissions().toString() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getPermissions().toString(),
                metaClassAttributeFromLegacyServer.getPermissions().toString());

        Assert.assertEquals("metaClassAttribute[" + name + "].getTypeID() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getTypeID(),
                metaClassAttributeFromRestServer.getTypeID());
        Assert.assertEquals("metaClassAttribute[" + name + "].getTypeID() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getTypeID(),
                metaClassAttributeFromLegacyServer.getTypeID());

        Assert.assertEquals("metaClassAttribute[" + name + "].getTypeId() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getTypeId(),
                metaClassAttributeFromRestServer.getTypeId());
        Assert.assertEquals("metaClassAttribute[" + name + "].getTypeId() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getTypeId(),
                metaClassAttributeFromLegacyServer.getTypeId());

        Assert.assertEquals("metaClassAttribute[" + name + "].getValue() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getValue(),
                metaClassAttributeFromRestServer.getValue());
        Assert.assertEquals("metaClassAttribute[" + name + "].getValue() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getValue(),
                metaClassAttributeFromLegacyServer.getValue());

        Assert.assertEquals("metaClassAttribute[" + name + "].isArray() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isArray(),
                metaClassAttributeFromRestServer.isArray());
        Assert.assertEquals("metaClassAttribute[" + name + "].isArray() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isArray(),
                metaClassAttributeFromLegacyServer.isArray());

        Assert.assertEquals("metaClassAttribute[" + name + "].isChanged() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isChanged(),
                metaClassAttributeFromRestServer.isChanged());
        Assert.assertEquals("metaClassAttribute[" + name + "].isChanged() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isChanged(),
                metaClassAttributeFromLegacyServer.isChanged());

        Assert.assertEquals("metaClassAttribute[" + name + "].isOptional() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isOptional(),
                metaClassAttributeFromRestServer.isOptional());
        Assert.assertEquals("metaClassAttribute[" + name + "].isOptional() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isOptional(),
                metaClassAttributeFromLegacyServer.isOptional());

        Assert.assertEquals("metaClassAttribute[" + name + "].isPrimaryKey() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isPrimaryKey(),
                metaClassAttributeFromRestServer.isPrimaryKey());
        Assert.assertEquals("metaClassAttribute[" + name + "].isPrimaryKey() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isPrimaryKey(),
                metaClassAttributeFromLegacyServer.isPrimaryKey());

        Assert.assertEquals("metaClassAttribute[" + name + "].isSubstitute() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isSubstitute(),
                metaClassAttributeFromRestServer.isSubstitute());
        Assert.assertEquals("metaClassAttribute[" + name + "].isSubstitute() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isSubstitute(),
                metaClassAttributeFromLegacyServer.isSubstitute());

        Assert.assertEquals("metaClassAttribute[" + name + "].isVisible() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isVisible(),
                metaClassAttributeFromRestServer.isVisible());
        Assert.assertEquals("metaClassAttribute[" + name + "].isVisible() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.isVisible(),
                metaClassAttributeFromLegacyServer.isVisible());

        Assert.assertEquals("metaClassAttribute[" + name + "].referencesObject() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.referencesObject(),
                metaClassAttributeFromRestServer.referencesObject());
        Assert.assertEquals("metaClassAttribute[" + name + "].referencesObject() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.referencesObject(),
                metaClassAttributeFromLegacyServer.referencesObject());

        Assert.assertEquals("metaClassAttribute[" + name + "].getOptions().size() from rest server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getOptions().size(),
                metaClassAttributeFromRestServer.getOptions().size());
        Assert.assertEquals("metaClassAttribute[" + name + "].getOptions().size() from legacy server matches (classId: " + pk + ")",
                metaClassAttributeFromJson.getOptions().size(),
                metaClassAttributeFromLegacyServer.getOptions().size());

        final Map<String, String> metaClassAttributeFromJsonOptions
                = metaClassAttributeFromJson.getOptions();
        final Map<String, String> metaClassAttributeFromRestServerOptions
                = metaClassAttributeFromRestServer.getOptions();
        final Map<String, String> metaClassAttributeFromLegacyServerOptions
                = metaClassAttributeFromLegacyServer.getOptions();

        for (String key : metaClassAttributeFromJsonOptions.keySet()) {
            Assert.assertEquals("metaClassAttribute[" + name + "].getOptions(" + key + ") from rest server matches (classId: " + pk + ")",
                    metaClassAttributeFromJsonOptions.get(key),
                    metaClassAttributeFromRestServerOptions.get(key));
            Assert.assertEquals("metaClassAttribute[" + name + "].getOptions(" + key + ") from legacy server matches (classId: " + pk + ")",
                    metaClassAttributeFromJsonOptions.get(key),
                    metaClassAttributeFromLegacyServerOptions.get(key));
        }
    }

    protected void compareObjectAttributes(final ObjectAttribute objectAttributeFromJson,
            final ObjectAttribute objectAttributeFromLegacyServer,
            final ObjectAttribute objectAttributeFromRestServer,
            final int pk) throws AssertionError {

        final String name = objectAttributeFromJson.getName();

        Assert.assertEquals("objectAttribute[" + name + "].getClassID() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getClassID(),
                objectAttributeFromRestServer.getClassID());
        Assert.assertEquals("objectAttribute[" + name + "].getClassID() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getClassID(),
                objectAttributeFromLegacyServer.getClassID());

        Assert.assertEquals("objectAttribute[" + name + "].getClassKey() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getClassKey(),
                objectAttributeFromRestServer.getClassKey());
        Assert.assertEquals("objectAttribute[" + name + "].getClassKey() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getClassKey(),
                objectAttributeFromLegacyServer.getClassKey());

        Assert.assertEquals("objectAttribute[" + name + "].getDescription() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getDescription(),
                objectAttributeFromRestServer.getDescription());
        Assert.assertEquals("objectAttribute[" + name + "].getDescription() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getDescription(),
                objectAttributeFromLegacyServer.getDescription());

        Assert.assertEquals("objectAttribute[" + name + "].getID() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getID(),
                objectAttributeFromRestServer.getID());
        Assert.assertEquals("objectAttribute[" + name + "].getID() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getID(),
                objectAttributeFromLegacyServer.getID());

        Assert.assertEquals("objectAttribute[" + name + "].getJavaType() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getJavaType(),
                objectAttributeFromRestServer.getJavaType());
        Assert.assertEquals("objectAttribute[" + name + "].getJavaType() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getJavaType(),
                objectAttributeFromLegacyServer.getJavaType());

        Assert.assertEquals("objectAttribute[" + name + "].getKey() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getKey(),
                objectAttributeFromRestServer.getKey());
        Assert.assertEquals("objectAttribute[" + name + "].getKey() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getKey(),
                objectAttributeFromLegacyServer.getKey());

        Assert.assertEquals("objectAttribute[" + name + "].getPermissions().toString() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getPermissions().toString(),
                objectAttributeFromRestServer.getPermissions().toString());
        Assert.assertEquals("objectAttribute[" + name + "].getPermissions().toString() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getPermissions().toString(),
                objectAttributeFromLegacyServer.getPermissions().toString());

        Assert.assertEquals("objectAttribute[" + name + "].getTypeId() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.getTypeId(),
                objectAttributeFromRestServer.getTypeId());
        Assert.assertEquals("objectAttribute[" + name + "].getTypeId() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.getTypeId(),
                objectAttributeFromLegacyServer.getTypeId());

//        Assert.assertEquals("objectAttribute[" + name + "].getValue() from rest server matches (classId: " + pk + ")",
//                objectAttributeFromJson.getValue(),
//                objectAttributeFromRestServer.getValue());
//        Assert.assertEquals("objectAttribute[" + name + "].getValue() from legacy server matches (classId: " + pk + ")",
//                objectAttributeFromJson.getValue(),
//                objectAttributeFromLegacyServer.getValue());
        Assert.assertEquals("objectAttribute[" + name + "].isArray() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isArray(),
                objectAttributeFromRestServer.isArray());
        Assert.assertEquals("objectAttribute[" + name + "].isArray() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isArray(),
                objectAttributeFromLegacyServer.isArray());

        Assert.assertEquals("objectAttribute[" + name + "].isChanged() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isChanged(),
                objectAttributeFromRestServer.isChanged());
        Assert.assertEquals("objectAttribute[" + name + "].isChanged() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isChanged(),
                objectAttributeFromLegacyServer.isChanged());

        Assert.assertEquals("objectAttribute[" + name + "].isOptional() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isOptional(),
                objectAttributeFromRestServer.isOptional());
        Assert.assertEquals("objectAttribute[" + name + "].isOptional() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isOptional(),
                objectAttributeFromLegacyServer.isOptional());

        Assert.assertEquals("objectAttribute[" + name + "].isPrimaryKey() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isPrimaryKey(),
                objectAttributeFromRestServer.isPrimaryKey());
        Assert.assertEquals("objectAttribute[" + name + "].isPrimaryKey() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isPrimaryKey(),
                objectAttributeFromLegacyServer.isPrimaryKey());

        Assert.assertEquals("objectAttribute[" + name + "].isSubstitute() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isSubstitute(),
                objectAttributeFromRestServer.isSubstitute());
        Assert.assertEquals("objectAttribute[" + name + "].isSubstitute() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isSubstitute(),
                objectAttributeFromLegacyServer.isSubstitute());

        Assert.assertEquals("objectAttribute[" + name + "].isVisible() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isVisible(),
                objectAttributeFromRestServer.isVisible());
        Assert.assertEquals("objectAttribute[" + name + "].isVisible() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isVisible(),
                objectAttributeFromLegacyServer.isVisible());

        Assert.assertEquals("objectAttribute[" + name + "].isStringCreateable() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isStringCreateable(),
                objectAttributeFromRestServer.isStringCreateable());
        Assert.assertEquals("objectAttribute[" + name + "].isStringCreateable() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isStringCreateable(),
                objectAttributeFromLegacyServer.isStringCreateable());

        Assert.assertEquals("objectAttribute[" + name + "].isVirtualOneToManyAttribute() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isVirtualOneToManyAttribute(),
                objectAttributeFromRestServer.isVirtualOneToManyAttribute());
        Assert.assertEquals("objectAttribute[" + name + "].isStringCreateable() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isVirtualOneToManyAttribute(),
                objectAttributeFromLegacyServer.isVirtualOneToManyAttribute());

        Assert.assertEquals("objectAttribute[" + name + "].isVisible() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.isVisible(),
                objectAttributeFromRestServer.isVisible());
        Assert.assertEquals("objectAttribute[" + name + "].isVisible() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.isVisible(),
                objectAttributeFromLegacyServer.isVisible());

        Assert.assertEquals("objectAttribute[" + name + "].referencesObject() from rest server matches (classId: " + pk + ")",
                objectAttributeFromJson.referencesObject(),
                objectAttributeFromRestServer.referencesObject());
        Assert.assertEquals("objectAttribute[" + name + "].referencesObject() from legacy server matches (classId: " + pk + ")",
                objectAttributeFromJson.referencesObject(),
                objectAttributeFromLegacyServer.referencesObject());

        final MemberAttributeInfo maiFromJson = objectAttributeFromJson.getMai();
        final MemberAttributeInfo maiFromRestServer = objectAttributeFromRestServer.getMai();
        final MemberAttributeInfo maiFromLegacyServer = objectAttributeFromLegacyServer.getMai();

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getArrayKeyFieldName from rest server matches (classId: " + pk + ")",
                maiFromJson.getArrayKeyFieldName(),
                maiFromRestServer.getArrayKeyFieldName());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getArrayKeyFieldName from legacy server matches (classId: " + pk + ")",
                maiFromJson.getArrayKeyFieldName(),
                maiFromLegacyServer.getArrayKeyFieldName());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getClassId() from rest server matches (classId: " + pk + ")",
                maiFromJson.getClassId(),
                maiFromRestServer.getClassId());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getClassId() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getClassId(),
                maiFromLegacyServer.getClassId());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getComplexEditor() from rest server matches (classId: " + pk + ")",
                maiFromJson.getComplexEditor(),
                maiFromRestServer.getComplexEditor());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getComplexEditor() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getComplexEditor(),
                maiFromLegacyServer.getComplexEditor());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getDefaultValue() from rest server matches (classId: " + pk + ")",
                maiFromJson.getDefaultValue(),
                maiFromRestServer.getDefaultValue());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getDefaultValue() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getDefaultValue(),
                maiFromLegacyServer.getDefaultValue());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getEditor() from rest server matches (classId: " + pk + ")",
                maiFromJson.getEditor(),
                maiFromRestServer.getEditor());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getEditor() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getEditor(),
                maiFromLegacyServer.getEditor());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getFieldName() from rest server matches (classId: " + pk + ")",
                maiFromJson.getFieldName(),
                maiFromRestServer.getFieldName());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getFieldName() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getFieldName(),
                maiFromLegacyServer.getFieldName());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getForeignKeyClassId() from rest server matches (classId: " + pk + ")",
                maiFromJson.getForeignKeyClassId(),
                maiFromRestServer.getForeignKeyClassId());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getForeignKeyClassId() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getForeignKeyClassId(),
                maiFromLegacyServer.getForeignKeyClassId());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getFromString() from rest server matches (classId: " + pk + ")",
                maiFromJson.getFromString(),
                maiFromRestServer.getFromString());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getFromString() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getFromString(),
                maiFromLegacyServer.getFromString());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getId() from rest server matches (classId: " + pk + ")",
                maiFromJson.getId(),
                maiFromRestServer.getId());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getId() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getId(),
                maiFromLegacyServer.getId());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getJavaclassname() from rest server matches (classId: " + pk + ")",
                maiFromJson.getJavaclassname(),
                maiFromRestServer.getJavaclassname());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getJavaclassname() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getJavaclassname(),
                maiFromLegacyServer.getJavaclassname());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getKey() from rest server matches (classId: " + pk + ")",
                maiFromJson.getKey(),
                maiFromRestServer.getKey());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getKey() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getKey(),
                maiFromLegacyServer.getKey());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getName() from rest server matches (classId: " + pk + ")",
                maiFromJson.getName(),
                maiFromRestServer.getName());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getName() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getName(),
                maiFromLegacyServer.getName());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getPosition() from rest server matches (classId: " + pk + ")",
                maiFromJson.getPosition(),
                maiFromRestServer.getPosition());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getPosition() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getPosition(),
                maiFromLegacyServer.getPosition());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getRenderer() from rest server matches (classId: " + pk + ")",
                maiFromJson.getRenderer(),
                maiFromRestServer.getRenderer());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getRenderer() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getRenderer(),
                maiFromLegacyServer.getRenderer());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getToString() from rest server matches (classId: " + pk + ")",
                maiFromJson.getToString(),
                maiFromRestServer.getToString());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getToString() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getToString(),
                maiFromLegacyServer.getToString());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().getTypeId() from rest server matches (classId: " + pk + ")",
                maiFromJson.getTypeId(),
                maiFromRestServer.getTypeId());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().getTypeId() from legacy server matches (classId: " + pk + ")",
                maiFromJson.getTypeId(),
                maiFromLegacyServer.getTypeId());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().isArray() from rest server matches (classId: " + pk + ")",
                maiFromJson.isArray(),
                maiFromRestServer.isArray());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().isArray() from legacy server matches (classId: " + pk + ")",
                maiFromJson.isArray(),
                maiFromLegacyServer.isArray());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().isExtensionAttribute() from rest server matches (classId: " + pk + ")",
                maiFromJson.isExtensionAttribute(),
                maiFromRestServer.isExtensionAttribute());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().isExtensionAttribute() from legacy server matches (classId: " + pk + ")",
                maiFromJson.isExtensionAttribute(),
                maiFromLegacyServer.isExtensionAttribute());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().isForeignKey() from rest server matches (classId: " + pk + ")",
                maiFromJson.isForeignKey(),
                maiFromRestServer.isForeignKey());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().isForeignKey() from legacy server matches (classId: " + pk + ")",
                maiFromJson.isForeignKey(),
                maiFromLegacyServer.isForeignKey());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().isIndexed() from rest server matches (classId: " + pk + ")",
                maiFromJson.isIndexed(),
                maiFromRestServer.isIndexed());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().isIndexed() from legacy server matches (classId: " + pk + ")",
                maiFromJson.isIndexed(),
                maiFromLegacyServer.isIndexed());

        Assert.assertEquals("objectAttribute[" + name + "].getMai().isOptional() from rest server matches (classId: " + pk + ")",
                maiFromJson.isOptional(),
                maiFromRestServer.isIndexed());
        Assert.assertEquals("objectAttribute[" + name + "].getMai().isOptional() from legacy server matches (classId: " + pk + ")",
                maiFromJson.isOptional(),
                maiFromLegacyServer.isOptional());
    }
}