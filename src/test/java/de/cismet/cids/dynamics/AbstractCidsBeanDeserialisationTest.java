package de.cismet.cids.dynamics;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
//@RunWith(PowerMockRunner.class)
@RunWith(DataProviderRunner.class)
//@PowerMockRunnerDelegate(DataProviderRunner.class)
//@PrepareForTest(IdGenerator.class)
public abstract class AbstractCidsBeanDeserialisationTest {

    protected final static Logger LOGGER = Logger.getLogger(AbstractCidsBeanDeserialisationTest.class);

    /**
     * Generated with deduplicate = true, omit null values = true and
     * SerializationFeature.INDENT_OUTPUT = false
     */
    protected final static String NORMALISED_ENTITIES = "de/cismet/cids/integrationtests/normalisedentities/";

    /**
     * Generated with deduplicate = false, omit null values = true and
     * SerializationFeature.INDENT_OUTPUT = false
     */
    protected final static String UNFORMATTED_ENTITIES = "de/cismet/cids/integrationtests/entities/";

    /**
     * Generated with deduplicate = true, omit null values = true and
     * SerializationFeature.INDENT_OUTPUT = true
     */
    protected final static String FORMATTED_ENTITIES = "de/cismet/cids/dynamics/entities/";

    protected final static ArrayList<String> CIDS_BEANS_JSON_FORMATTED = new ArrayList<String>();
    protected final static ArrayList<String> CIDS_BEANS_JSON_UNFORMATTED = new ArrayList<String>();
    protected final static ArrayList<String> CIDS_BEANS_JSON_NORMALISED = new ArrayList<String>();

    protected static ArrayList<String> initCidsBeansJson(final String entitiesPackage) throws Exception {

        final ArrayList<String> cidsBeansJson = new ArrayList<String>();
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resources;

        try {
            resources = classLoader.getResource(entitiesPackage);

            final Scanner scanner = new Scanner((InputStream) resources.getContent()).useDelimiter("\\n");
            while (scanner.hasNext()) {
                final String jsonFile = entitiesPackage + scanner.next();
                LOGGER.info("loading cids entity from json file " + jsonFile);
                try {

                    final String entity = IOUtils.toString(classLoader.getResourceAsStream(jsonFile), "UTF-8");
                    cidsBeansJson.add(entity);

                } catch (Exception ex) {
                    LOGGER.error("could not load cids entities from url " + jsonFile, ex);
                    throw ex;
                }
            }

            LOGGER.info(cidsBeansJson.size() + "CIDS_BEANS_JSON entities loaded");
            return cidsBeansJson;

        } catch (Exception ex) {
            LOGGER.error("could not locate entities json files: " + ex.getMessage(), ex);
            throw ex;
        }
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        final Properties log4jProperties = new Properties();
        log4jProperties.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        log4jProperties.put("log4j.appender.Remote.remoteHost", "localhost");
        log4jProperties.put("log4j.appender.Remote.port", "4445");
        log4jProperties.put("log4j.appender.Remote.locationInfo", "true");
        log4jProperties.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(log4jProperties);

        CidsBean.intraObjectCacheMapper.disable(SerializationFeature.INDENT_OUTPUT);
        CidsBean.mapper.disable(SerializationFeature.INDENT_OUTPUT);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        CidsBean.intraObjectCacheMapper.enable(SerializationFeature.INDENT_OUTPUT);
        CidsBean.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Before
    public void setUpMethod() throws Exception {
    }

    @After
    public void tearDownMethod() throws Exception {
    }

    @DataProvider
    public final static String[] getCidsBeansJson() throws Exception {
        if (CIDS_BEANS_JSON_FORMATTED.isEmpty()) {
            CIDS_BEANS_JSON_FORMATTED.addAll(initCidsBeansJson(FORMATTED_ENTITIES));
        }

        return CIDS_BEANS_JSON_FORMATTED.toArray(new String[CIDS_BEANS_JSON_FORMATTED.size()]);
    }

    @DataProvider
    public final static String[] getCidsBeansJsonUnformatted() throws Exception {
        if (CIDS_BEANS_JSON_UNFORMATTED.isEmpty()) {
            CIDS_BEANS_JSON_UNFORMATTED.addAll(initCidsBeansJson(UNFORMATTED_ENTITIES));
        }

        return CIDS_BEANS_JSON_UNFORMATTED.toArray(new String[CIDS_BEANS_JSON_UNFORMATTED.size()]);
    }

    @DataProvider
    public final static String[] getCidsBeansJsonNormalised() throws Exception {
        if (CIDS_BEANS_JSON_NORMALISED.isEmpty()) {
            CIDS_BEANS_JSON_NORMALISED.addAll(initCidsBeansJson(NORMALISED_ENTITIES));
        }

        return CIDS_BEANS_JSON_NORMALISED.toArray(new String[CIDS_BEANS_JSON_NORMALISED.size()]);
    }
}
