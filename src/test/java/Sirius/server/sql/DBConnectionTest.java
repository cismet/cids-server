/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.localserver.user.UserStoreTest;
import Sirius.server.property.ServerProperties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Properties;

import de.cismet.tools.ScriptRunner;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class DBConnectionTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            DBConnectionTest.class);
    private static final String TEST = "TEST ";             // NOI18N
    private static final DBClassifier DB_CLASSIFIER = new DBClassifier(
            "jdbc:postgresql://kif:5432/cids_reference_db", // NOI18N
            "postgres",                                     // NOI18N
            "x",                                            // NOI18N
            "org.postgresql.Driver");                       // NOI18N

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Throwable {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        PropertyConfigurator.configure(p);
        final ServerProperties props = new ServerProperties(DBConnectionTest.class.getResourceAsStream(
                    "/Sirius/server/localserver/object/runtime.properties")); // NOI18N
        final DBConnectionPool pool = new DBConnectionPool(props);
        final ScriptRunner runner = new ScriptRunner(pool.getConnection().getConnection(), false, false);
        final InputStream configAttrTestData = DBConnectionTest.class.getResourceAsStream(
                "/Sirius/server/localserver/user/configAttrTestData.sql");    // NOI18N
        final InputStream historyServerTestData = DBConnectionTest.class.getResourceAsStream(
                "/Sirius/server/localserver/history/HistoryServerTest.sql");  // NOI18N
        final BufferedReader cfgAttrReader = new BufferedReader(new InputStreamReader(configAttrTestData));
        final BufferedReader historyReader = new BufferedReader(new InputStreamReader(historyServerTestData));

        try {
            runner.runScript(cfgAttrReader);
            runner.runScript(historyReader);
        } finally {
            cfgAttrReader.close();
            historyReader.close();
        }
    }

    /**
     * DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() {
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
    public void testCharToBool() {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final String message = "char was: ";
        char c = 't';
        assertTrue(message + c, DBConnection.charToBool(c));
        c = 'T';
        assertTrue(message + c, DBConnection.charToBool(c));
        c = 'Z';
        assertFalse(message + c, DBConnection.charToBool(c));
        c = 'x';
        assertFalse(message + c, DBConnection.charToBool(c));
        c = '1';
        assertFalse(message + c, DBConnection.charToBool(c));
    }

    /**
     * DOCUMENT ME!
     */
    @Test
    public void testStringToBool() {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final String message = "String was: ";
        final String[] trues = new String[] { "t", "T", "tT", "Tt", "T23asdjk", "t32987tjng§", "T.yjflsajg" };
        for (final String s : trues) {
            assertTrue(message + s, DBConnection.stringToBool(s));
        }
        final String[] falses = new String[] { "a", "A", "Aasdf", "afdg4rgf", "..fdas", "///", "\\", "\t", " " };
        for (final String s : falses) {
            assertFalse(message + s, DBConnection.stringToBool(s));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testSubmitInternalQueryOK() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }

        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1 = null;
        try {
            set1 = con.submitInternalQuery(DBConnection.DESC_VERIFY_USER_PW, "admin", "cismet");
            if (set1.next()) {
                assertEquals("not exactly one user found", 1, set1.getInt(1));
            } else {
                fail("illegal resultset state");
            }
        } finally {
            DBConnection.closeResultSets(set1);
        }

        try {
            set1 = con.submitInternalQuery(DBConnection.DESC_FETCH_CONFIG_ATTR_KEY_ID, "abc");
            if (set1.next()) {
                assertEquals("not exactly one key found", 1, set1.getInt(1));
            } else {
                fail("illegal resultset state");
            }
        } finally {
            DBConnection.closeResultSets(set1);
        }

        try {
            set1 = con.submitInternalQuery(DBConnection.DESC_FETCH_CONFIG_ATTR_DOMAIN_VALUE, 1, 1);
            if (set1.next()) {
                assertEquals("not exactly one value found", "alphabeth", set1.getString(1));
            } else {
                fail("illegal resultset state");
            }
        } finally {
            DBConnection.closeResultSets(set1);
        }

        try {
            set1 = con.submitInternalQuery(DBConnection.DESC_FETCH_CONFIG_ATTR_UG_VALUE, 1, 1, 1);
            if (set1.next()) {
                assertEquals("not exactly one value found", "alphabeth2", set1.getString(1));
            } else {
                fail("illegal resultset state");
            }
        } finally {
            DBConnection.closeResultSets(set1);
        }

        try {
            set1 = con.submitInternalQuery(DBConnection.DESC_FETCH_CONFIG_ATTR_USER_VALUE, 1, 1, 1, 1);
            if (set1.next()) {
                assertEquals("not exactly one value found", "alphabeth3", set1.getString(1));
            } else {
                fail("illegal resultset state");
            }
        } finally {
            DBConnection.closeResultSets(set1);
        }

        try {
            set1 = con.submitInternalQuery(DBConnection.DESC_FETCH_DOMAIN_ID_FROM_DOMAIN_STRING, "LOCAL");
            if (set1.next()) {
                assertEquals("not exactly one value found", 1, set1.getInt(1));
            } else {
                fail("illegal resultset state");
            }
        } finally {
            DBConnection.closeResultSets(set1);
        }

        try {
            set1 = con.submitInternalQuery(DBConnection.DESC_FETCH_HISTORY, 1, 1);
            if (set1.next() && set1.next() && set1.next()) {
                if (set1.next()) {
                    fail("too many result rows");
                }
            } else {
                fail("too less result rows");
            }
        } finally {
            DBConnection.closeResultSets(set1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testSubmitInternalUpdateOK() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }

        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        int result = -1;
        Timestamp now = new Timestamp(System.currentTimeMillis());

        result = con.submitInternalUpdate(
                DBConnection.DESC_INSERT_HISTORY_ENTRY,
                1,
                1,
                1,
                1,
                now,
                "testdata");
        assertEquals("unexpected result state", 1, result);

        PreparedStatement ps = con.getConnection().prepareStatement("select * from cs_history where valid_from = ?");
        ResultSet rs = null;
        try {
            ps.setObject(1, now);
            rs = ps.executeQuery();
            assertTrue("no result found", rs.next());
            assertFalse("too many results found", rs.next());
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        now = new Timestamp(System.currentTimeMillis());
        result = con.submitInternalUpdate(
                DBConnection.DESC_INSERT_HISTORY_ENTRY,
                1,
                1,
                null,
                null,
                now,
                "testdata");
        assertEquals("unexpected result state", 1, result);

        ps = con.getConnection()
                    .prepareStatement(
                            "select * from cs_history where valid_from = ? and ug_id is null and usr_id is null");
        try {
            ps.setObject(1, now);
            rs = ps.executeQuery();
            assertTrue("no result found", rs.next());
            assertFalse("too many results found", rs.next());
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubmitInternalQueryInvalidDescriptor() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1 = null;
        try {
            set1 = con.submitInternalQuery("x", "admin", "sb");
        } finally {
            DBConnection.closeResultSets(set1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubmitInternalQueryTooManyParams() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1 = null;
        try {
            set1 = con.submitInternalQuery(
                    "verify_user_password",
                    "admin",
                    "sb",
                    "sb");
        } finally {
            DBConnection.closeResultSets(set1);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSubmitInternalQueryTooLessParams() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1 = null;
        try {
            set1 = con.submitInternalQuery(
                    "verify_user_password",
                    "admin");
        } finally {
            DBConnection.closeResultSets(set1);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitQuery_String_ObjectArr() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitQuery_int_ObjectArr() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitQuery_Query() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitUpdate_Query() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitUpdate_String_ObjectArr() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitUpdate_int_ObjectArr() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testFetchStatement_String() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testFetchStatement_int() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testGetStatementCache() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testExecuteQuery() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitUpdateBatch_int_ObjectArr() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testSubmitUpdateBatch_String_ObjectArr() {
        if (LOG.isInfoEnabled()) {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }
}
