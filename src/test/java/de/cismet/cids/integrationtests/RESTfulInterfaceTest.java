package de.cismet.cids.integrationtests;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import static de.cismet.cids.dynamics.AbstractCidsBeanDeserialisationTest.ENTITIES_JSON_PACKAGE;
import de.cismet.cids.dynamics.CidsBean;
import static de.cismet.cids.integrationtests.TestEnvironment.REST_SERVER_CONTAINER;
import static de.cismet.cids.integrationtests.TestEnvironment.SERVER_CONTAINER;
import de.cismet.cids.server.ws.rest.RESTfulSerialInterfaceConnector;
import de.cismet.cidsx.client.connector.RESTfulInterfaceConnector;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.Vector;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.testcontainers.containers.DockerComposeContainer;

/**
 * CidsBean vs MetaObject Tests
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@RunWith(DataProviderRunner.class)
public class RESTfulInterfaceTest extends TestBase {

    protected final static Logger LOGGER = Logger.getLogger(RESTfulInterfaceTest.class);

    /**
     * The static cids Reference Container is reused for all @test Methods! To
     * avoid an unnecessary start of the container, it is initialized in the
     * initcidsRefContainer() operation that checks if integration tests are
     * enabled.
     */
    protected static DockerComposeContainer dockerEnvironment = null;

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

    protected static RESTfulInterfaceConnector restConnector = null;
    protected static RESTfulSerialInterfaceConnector legacyConnector = null;
    protected static User user = null;
    protected static Properties properties = null;
    protected static boolean connectionFailed = false;
    protected final static ArrayList<String> CIDS_BEANS_JSON = new ArrayList<String>();

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
     * Unfortunately, this data provicer methos is called before the @ClassRule
     * and before @BeforeClass! Therfore intitialisation of CidsBeans from JSON
     * (located in test resources package) is delegated to initCidsBeansJson();
     *
     * @return
     * @throws Exception
     */
    @DataProvider
    public final static String[] getCidsBeansJson() throws Exception {
        initCidsBeansJson();
        return CIDS_BEANS_JSON.toArray(new String[CIDS_BEANS_JSON.size()]);
    }

    @Before
    public void beforeTest() {
        Assert.assertNotNull("cids legacy server connection successfully established", legacyConnector);
        Assert.assertNotNull("cids rest server connection successfully established", restConnector);
        Assert.assertNotNull("user authenticated", user);
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void getAndCompareMetaObjects(final String cidsBeanJson) throws Exception {
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

            
            Assert.assertEquals("class key from legacy server matches", 
                    cidsBeanFromJson.getCidsBeanInfo().getClassKey(), 
                    cidsBeanFromLegacyServer.getCidsBeanInfo().getClassKey());
            Assert.assertEquals("class key from rest server matches", 
                    cidsBeanFromJson.getCidsBeanInfo().getClassKey(), 
                    cidsBeanFromRestServer.getCidsBeanInfo().getClassKey());


        } catch (AssertionError ae) {
            LOGGER.error("getAndCompareCidsBeans test failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }

        LOGGER.info("getAndCompareMetaObjects test passed");
    }
}
