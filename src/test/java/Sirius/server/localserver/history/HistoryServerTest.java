/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.history;

import Sirius.server.localserver.DBServer;
import Sirius.server.middleware.types.HistoryObject;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnection;

import org.apache.log4j.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Field;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import de.cismet.tools.ScriptRunner;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public class HistoryServerTest {

    //~ Static fields/initializers ---------------------------------------------

    private static DBServer server;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HistoryServerTest object.
     */
    public HistoryServerTest() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Throwable {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(p);
        final InputStream is = HistoryServerTest.class.getResourceAsStream("runtime.properties");
        server = new DBServer(new ServerProperties(is));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Throwable {
        server.shutdown();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Before
    public void setUp() throws Exception {
        final ScriptRunner runner = new ScriptRunner(server.getConnectionPool().getConnection().getConnection(),
                false,
                false);
        runner.runScript(new BufferedReader(
                new InputStreamReader(HistoryServerTest.class.getResourceAsStream("HistoryServerTest.sql"))));
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
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetHistory() throws Exception {
        System.out.println("TEST " + getCurrentMethodName());

        HistoryObject[] history = server.getHistoryServer().getHistory(1, 1, new UserGroup(1, "dummy", "dummy"), 0);

        assertTrue("invalid amount of history objects: " + history.length, history.length == 3);
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:14:27.873").getTime()), history[0].getValidFrom());
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:13:46.023").getTime()), history[1].getValidFrom());
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:12:49.553").getTime()), history[2].getValidFrom());

        history = server.getHistoryServer().getHistory(1, 1, new UserGroup(1, "dummy", "dummy"), 1);

        assertTrue("invalid amount of history objects: " + history.length, history.length == 1);
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:14:27.873").getTime()), history[0].getValidFrom());

        history = server.getHistoryServer().getHistory(1, 1, new UserGroup(1, "dummy", "dummy"), 2);

        assertTrue("invalid amount of history objects: " + history.length, history.length == 2);
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:14:27.873").getTime()), history[0].getValidFrom());
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:13:46.023").getTime()), history[1].getValidFrom());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */

    @Test
    public void testEnqueueEntryValidUser() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());

        final HistoryServer historyServer = new HistoryServer(server.getConnectionPool(), server.getClassCache());
        final MetaObject object = server.getClass(1).getEmptyInstance();
        object.setMetaClass(server.getClass(1));
        final Date now = new Date(System.currentTimeMillis());

        historyServer.enqueueEntry(object, new User(1, "", "", new UserGroup(1, "", "")), now);

        // wait for the executor to finish
        final Field executorField = historyServer.getClass().getDeclaredField("executor");
        executorField.setAccessible(true);
        final ExecutorService executor = (ExecutorService)executorField.get(historyServer);
        executor.shutdown();
        assertTrue("executor did not terminate in time", executor.awaitTermination(2, TimeUnit.SECONDS));

        final PreparedStatement ps = server.getConnectionPool()
                    .getConnection()
                    .getConnection()
                    .prepareStatement("select * from cs_history where valid_from = ?");
        ResultSet rs = null;
        try {
            ps.setObject(1, new Timestamp(now.getTime()));
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
    @Test
    public void testEnqueueEntryNullUser() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());

        final HistoryServer historyServer = new HistoryServer(server.getConnectionPool(), server.getClassCache());
        final MetaObject object = server.getClass(1).getEmptyInstance();
        object.setMetaClass(server.getClass(1));
        
        final Date now = new Date(System.currentTimeMillis());

        historyServer.enqueueEntry(object, null, now);

        // wait for the executor to finish
        final Field executorField = historyServer.getClass().getDeclaredField("executor");
        executorField.setAccessible(true);
        final ExecutorService executor = (ExecutorService)executorField.get(historyServer);
        executor.shutdown();
        assertTrue("executor did not terminate in time", executor.awaitTermination(2, TimeUnit.SECONDS));

        final PreparedStatement ps = server.getConnectionPool()
                    .getConnection()
                    .getConnection()
                    .prepareStatement(
                        "select * from cs_history where valid_from = ? and ug_id is null and usr_id is null");
        ResultSet rs = null;
        try {
            ps.setObject(1, new Timestamp(now.getTime()));
            rs = ps.executeQuery();
            assertTrue("no result found", rs.next());
            assertFalse("too many results found", rs.next());
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }
    }
}
