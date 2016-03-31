package de.cismet.cids.dynamics;

import Sirius.server.middleware.types.MetaClass;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import de.cismet.cidsx.server.api.types.CidsClass;
import de.cismet.cidsx.server.api.types.legacy.CidsClassFactory;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
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
public class CidsBeanDeserialisationTest {

    protected final static Logger LOGGER = Logger.getLogger(CidsBeanDeserialisationTest.class);
    protected final static String ENTITIES_JSON_PACKAGE = "de/cismet/cids/dynamics/entities/";
    protected final static ArrayList<String> CIDS_BEANS_JSON = new ArrayList<String>();

    public CidsBeanDeserialisationTest() throws Exception {

    }

    protected static void initCidsBeansJson() throws Exception {
        if (CIDS_BEANS_JSON.isEmpty()) {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resources;

            try {
                resources = classLoader.getResource(ENTITIES_JSON_PACKAGE);

                final Scanner scanner = new Scanner((InputStream) resources.getContent()).useDelimiter("\\n");
                while (scanner.hasNext()) {
                    final String jsonFile = ENTITIES_JSON_PACKAGE + scanner.next();
                    LOGGER.info("loading cids entity from json file " + jsonFile);
                    try {

                        final String entity = IOUtils.toString(classLoader.getResourceAsStream(jsonFile), "UTF-8");
                        CIDS_BEANS_JSON.add(entity);

                    } catch (Exception ex) {
                        LOGGER.error("could not load cids entities from url " + jsonFile, ex);
                        throw ex;
                    }
                }

                LOGGER.info(CIDS_BEANS_JSON.size() + "CIDS_BEANS_JSON entities loaded");

            } catch (Exception ex) {
                LOGGER.error("could not locate entities json files: " + ex.getMessage(), ex);
                throw ex;
            }
        } else {
            LOGGER.warn("CIDS_BEANS_JSON already initialised");
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
        
        initCidsBeansJson();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    @Before
    public void setUpMethod() throws Exception {
    }

    @After
    public void tearDownMethod() throws Exception {
    }

    @DataProvider
    public final static String[] getCidsBeansJson() throws Exception {
        initCidsBeansJson();
        return CIDS_BEANS_JSON.toArray(new String[CIDS_BEANS_JSON.size()]);
    }

    @Test
    @UseDataProvider("getCidsBeansJson")
    public void testDeserializeCidsBean(final String cidsBeanJson) throws Exception {
        try {
            final CidsBean cidsBean = CidsBean.createNewCidsBeanFromJSON(true, cidsBeanJson);
            LOGGER.debug("testDeserializeCidsBean: " + cidsBean.getPrimaryKeyValue());
            //Assert.assertEquals(cidsBean.toJSONString(true), cidsBeanJson);
        } catch (AssertionError ae) {
            LOGGER.error("testDeserializeCidsBean failed with: " + ae.getMessage());
            throw ae;
        } catch (Exception ex) {

            LOGGER.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
