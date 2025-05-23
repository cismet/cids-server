/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class ClassAttributeTest {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassAttributeTest object.
     */
    public ClassAttributeTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        configureLog4J();
    }

    public static void configureLog4J() {
        final ConfigurationBuilder<BuiltConfiguration> builder = new DefaultConfigurationBuilder<>();

        builder.setStatusLevel(Level.DEBUG);
        builder.setConfigurationName("DynamicConfig");

        // Define appenders
        final AppenderComponentBuilder socketAppender = builder.newAppender("Remote", "Socket")
                    .addAttribute("host", "localhost")
                    .addAttribute("port", 4445);
        socketAppender.add(builder.newLayout("JsonLayout"));
        builder.add(socketAppender);

        // Define root logger
        final RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);

        rootLogger.add(builder.newAppenderRef("Remote"));

        builder.add(rootLogger);

        // Build and apply the configuration
        LoggerContext ctx = (LoggerContext)LogManager.getContext(false);
        final Configuration conf = builder.build();
        ctx.start(conf);
    }
    
    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * DOCUMENT ME!
     */
    @Before
    public void setUp() {
    }

    /**
     * DOCUMENT ME!
     */
    @After
    public void tearDown() {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getCurrentMethodName() {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testGetOptions() {
        System.out.println("TEST " + getCurrentMethodName());

        final ClassAttribute attr = new ClassAttribute(null, 0, null, 0, null);

        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertTrue("options not empty", attr.getOptions().isEmpty());

        attr.setValue("keyA");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyA"));

        attr.setValue("keyA, keyB, keyC");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly three options present", 3, attr.getOptions().size());
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyA"));
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyB"));
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyC"));

        attr.setValue("keyA=valueA");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));

        attr.setValue("keyA=");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyA"));

        attr.setValue("keyA=   ");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyA"));

        attr.setValue("keyA =valueA");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));

        attr.setValue("keyA= valueA");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));

        attr.setValue("keyA = valueA");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));

        attr.setValue(" keyA = valueA ");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));

        attr.setValue("keyA         =valueA       ");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));

        attr.setValue("keyA         ==       ");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyA"));

        attr.setValue("keyA         = =       ");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "=", attr.getOptions().get("keyA"));

        attr.setValue("keyA         =    ===valueA");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "===valueA", attr.getOptions().get("keyA"));

        attr.setValue("keyA         =valueA  ===valueA");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly one option present", 1, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA  ===valueA", attr.getOptions().get("keyA"));

        attr.setValue("  keyA   ,   keyB=valueB");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly two options present", 2, attr.getOptions().size());
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyA"));
        assertEquals("invalid option kv pair", "valueB", attr.getOptions().get("keyB"));

        attr.setValue("keyA   ,    keyB   =    valueB    ,   keyC");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly three options present", 3, attr.getOptions().size());
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyA"));
        assertEquals("invalid option kv pair", "valueB", attr.getOptions().get("keyB"));
        assertEquals("invalid option kv pair", "", attr.getOptions().get("keyC"));

        attr.setValue("keyA = valueA, keyB = valueB");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly two options present", 2, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));
        assertEquals("invalid option kv pair", "valueB", attr.getOptions().get("keyB"));

        attr.setValue("keyA = valueA , key B , keyC = valueC");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly three options present", 3, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));
        assertEquals("invalid option kv pair", "", attr.getOptions().get("key B"));
        assertEquals("invalid option kv pair", "valueC", attr.getOptions().get("keyC"));

        attr.setValue("keyA = valueA, keyB = valueB, keyC = valueC, keyD = valueD, keyE = valueE");
        assertNotNull("class attr option map shall never be null", attr.getOptions());
        assertEquals("not exactly five options present", 5, attr.getOptions().size());
        assertEquals("invalid option kv pair", "valueA", attr.getOptions().get("keyA"));
        assertEquals("invalid option kv pair", "valueB", attr.getOptions().get("keyB"));
        assertEquals("invalid option kv pair", "valueC", attr.getOptions().get("keyC"));
        assertEquals("invalid option kv pair", "valueD", attr.getOptions().get("keyD"));
        assertEquals("invalid option kv pair", "valueE", attr.getOptions().get("keyE"));
    }
}
