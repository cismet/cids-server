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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import de.cismet.remotetesthelper.RemoteTestHelperService;

import de.cismet.remotetesthelper.ws.rest.RemoteTestHelperClient;

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

    private static final String TEST_DB_NAME = "history_server_test_db";
    private static final RemoteTestHelperService SERVICE = new RemoteTestHelperClient();

    private static User user;
    private static ServerProperties properties;

    //~ Instance fields --------------------------------------------------------

    private DBServer server;

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
     * @throws  Throwable              Exception DOCUMENT ME!
     * @throws  IllegalStateException  DOCUMENT ME!
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

        if (!Boolean.valueOf(SERVICE.initCidsSystem(TEST_DB_NAME))) {
            throw new IllegalStateException("cannot initilise test db");
        }

        final InputStream is = HistoryServerTest.class.getResourceAsStream("runtime.properties");
        properties = new ServerProperties(is);
        user = new User(1, "dummy", "LOCAL", new UserGroup(1, "dummy", "LOCAL"));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable              Exception DOCUMENT ME!
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Throwable {
        if (!Boolean.valueOf(SERVICE.dropDatabase(TEST_DB_NAME))) {
            throw new IllegalStateException("could not drop test db");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @Before
    public void setUp() throws Throwable {
        final DBServer conProvider = new DBServer(properties);
        final Connection con = conProvider.getConnectionPool().getDBConnection().getConnection();
        final ScriptRunner runner = new ScriptRunner(con, true, false);
        runner.runScript(new BufferedReader(
                new InputStreamReader(HistoryServerTest.class.getResourceAsStream("HistoryServerTest.sql"))));
        con.close();
        conProvider.shutdown();

        server = new DBServer(properties);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @After
    public void tearDown() throws Throwable {
        server.shutdown();
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
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @Test
    public void testGetHistory() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());

        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test1'");
        set.next();
        int classId = set.getInt(1);

        HistoryObject[] history = server.getHistoryServer().getHistory(classId, 1, user, 0);

        assertNull(
            "received history which is there, but as the test class is not history enabled null was expected",
            history);

        set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        classId = set.getInt(1);

        history = server.getHistoryServer().getHistory(classId, 1, user, 0);

        assertTrue("invalid amount of history objects: " + history.length, history.length == 3);
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:13:46.025").getTime()), history[0].getValidFrom());
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:13:46.024").getTime()), history[1].getValidFrom());
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:13:46.023").getTime()), history[2].getValidFrom());

        history = server.getHistoryServer().getHistory(classId, 1, user, 2);

        assertTrue("invalid amount of history objects: " + history.length, history.length == 2);
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:13:46.025").getTime()), history[0].getValidFrom());
        assertEquals(new Date(Timestamp.valueOf("2010-12-04 15:13:46.024").getTime()), history[1].getValidFrom());

        set = con.createStatement().executeQuery("select id from cs_class where name like 'test4'");
        set.next();
        classId = set.getInt(1);

        // test the initHistory
        history = server.getHistoryServer().getHistory(classId, 1, user, 3);
        assertTrue("invalid amount of history objects: " + history.length, history.length == 1);

        PreparedStatement ps = server.getConnectionPool()
                    .getDBConnection()
                    .getConnection()
                    .prepareStatement(
                        "select count(*) from cs_history where ug_id = 1 and usr_id = 1 and class_id = "
                        + classId
                        + " and object_id = 1");
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("one result found", rs.next());
            assertEquals("count is not 1", 1, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        // test getHistory again to ensure no another init is done
        history = server.getHistoryServer().getHistory(classId, 1, user, 3);
        assertTrue("invalid amount of history objects: " + history.length, history.length == 1);

        ps = server.getConnectionPool().getDBConnection().getConnection()
                    .prepareStatement(
                            "select count(*) from cs_history where ug_id = 1 and usr_id = 1 and class_id = "
                            + classId
                            + " and object_id = 1");
        rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("one result found", rs.next());
            assertEquals("count is not 1", 1, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        con.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  HistoryException  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testGetHistoryInvalidClassId() throws HistoryException {
        server.getHistoryServer().getHistory(Integer.MIN_VALUE, 1, user, 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testGetHistoryNoReadPerm() throws Exception {
        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test3'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        server.getHistoryServer().getHistory(classId, 1, user, 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testGetHistoryInvalidObjectId() throws Exception {
        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        server.getHistoryServer().getHistory(classId, Integer.MAX_VALUE, user, 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testGetHistoryNullUser() throws Exception {
        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        server.getHistoryServer().getHistory(classId, 1, null, 1);
    }

    /**
     * this method is private and shouldn't even exist in the history server since the db server should provide valid
     * metaobjects. this test also needs valid metaobjects so we test the get first to be sure it works.
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testGetMetaObject() throws Throwable {
        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);

        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        final MetaObject mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        assertNotNull("received no metaobject", mo);
        assertNotNull("mo has no metaclass set", mo.getMetaClass());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testGetMetaObjectInvalidClassId() throws Throwable {
        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);

        try {
            getMoMethod.invoke(server.getHistoryServer(), Integer.MAX_VALUE, 1, user);
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getCause();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testGetMetaObjectInvalidObjectId() throws Throwable {
        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);

        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        try {
            getMoMethod.invoke(server.getHistoryServer(), classId, Integer.MAX_VALUE, user);
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getCause();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testGetMetaObjectNullUser() throws Throwable {
        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);

        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        try {
            getMoMethod.invoke(server.getHistoryServer(), classId, 1, null);
        } catch (InvocationTargetException invocationTargetException) {
            throw invocationTargetException.getCause();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testHasHistory() throws Exception {
        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);

        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test4'");
        set.next();
        int classId = set.getInt(1);

        MetaObject mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);
        assertFalse("hasHistory said that history is there but there isn't", server.getHistoryServer().hasHistory(mo));
        DBConnection.closeResultSets(set);

        set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        classId = set.getInt(1);

        mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);
        assertTrue("hasHistory said that history is not there but there is", server.getHistoryServer().hasHistory(mo));
        DBConnection.closeResultSets(set);

        // this is to check whether history disabled classes with history deliver a positive result, too
        set = con.createStatement().executeQuery("select id from cs_class where name like 'test1'");
        set.next();
        classId = set.getInt(1);

        mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);
        assertTrue("hasHistory said that history is not there but there is", server.getHistoryServer().hasHistory(mo));
        DBConnection.closeResultSets(set);
        con.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  HistoryException  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testHasHistoryNullMO() throws HistoryException {
        server.getHistoryServer().hasHistory(null);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testInitHistory() throws Exception {
        // history not enaabled, do nothing
        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test5'");
        set.next();
        int classId = set.getInt(1);
        DBConnection.closeResultSets(set);

        final Date now = new Date(System.currentTimeMillis());
        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);
        MetaObject mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        server.getHistoryServer().initHistory(mo, user, now);

        PreparedStatement ps = server.getConnectionPool()
                    .getDBConnection()
                    .getConnection()
                    .prepareStatement(
                        "select count(*) from cs_history where ug_id = 1 and usr_id = 1 and class_id = "
                        + classId
                        + " and object_id = 1");
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("not one result found", rs.next());
            assertEquals("count is not 0", 0, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        // initHistory
        set = con.createStatement().executeQuery("select id from cs_class where name like 'test4'");
        set.next();
        classId = set.getInt(1);
        DBConnection.closeResultSets(set);

        mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        server.getHistoryServer().initHistory(mo, user, now);

        ps = server.getConnectionPool().getDBConnection().getConnection()
                    .prepareStatement(
                            "select count(*) from cs_history where ug_id = 1 and usr_id = 1 and class_id = "
                            + classId
                            + " and object_id = 1");
        rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("not one result found", rs.next());
            assertEquals("count is not 1", 1, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        // initHistory already done, nothing changes
        set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        classId = set.getInt(1);
        DBConnection.closeResultSets(set);

        mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        server.getHistoryServer().initHistory(mo, user, now);

        ps = server.getConnectionPool().getDBConnection().getConnection()
                    .prepareStatement(
                            "select count(*) from cs_history where class_id = "
                            + classId
                            + " and object_id = 1");
        rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("not one result found", rs.next());
            assertEquals("count is not 3", 3, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        con.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  HistoryException  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testInitHistoryNullMO() throws HistoryException {
        server.getHistoryServer().initHistory(null, user, new Date());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test(expected = HistoryException.class)
    public void testInitHistoryNullDate() throws Exception {
        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test5'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);
        final MetaObject mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        server.getHistoryServer().initHistory(mo, user, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testEnqueueEntry() throws Exception {
        // history not enaabled, do nothing
        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test5'");
        set.next();
        int classId = set.getInt(1);
        DBConnection.closeResultSets(set);

        final Date now = new Date(System.currentTimeMillis());
        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);
        MetaObject mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        // we need another instance because we will shutdown the executor to ensure it has finished all the jobs
        HistoryServer history = new HistoryServer(server);
        history.enqueueEntry(mo, user, now);

        // wait for the executor to finish
        final Field executorField = history.getClass().getDeclaredField("executor");
        executorField.setAccessible(true);
        ExecutorService executor = (ExecutorService)executorField.get(history);
        executor.shutdown();
        assertTrue("executor did not terminate in time", executor.awaitTermination(2, TimeUnit.SECONDS));

        PreparedStatement ps = server.getConnectionPool()
                    .getDBConnection()
                    .getConnection()
                    .prepareStatement(
                        "select count(*) from cs_history where ug_id = 1 and usr_id = 1 and class_id = "
                        + classId
                        + " and object_id = 1");
        ResultSet rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("not one result found", rs.next());
            assertEquals("count is not 0", 0, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        final PreparedStatement pSgetHistory = server.getConnectionPool()
                    .getDBConnection()
                    .getConnection()
                    .prepareStatement("select * from cs_history where valid_from = ? and class_id = ?");
        rs = null;
        try {
            pSgetHistory.setObject(1, new Timestamp(now.getTime()));
            pSgetHistory.setInt(2, classId);
            rs = pSgetHistory.executeQuery();
            assertFalse("result found but nothing should be there", rs.next());
        } finally {
            DBConnection.closeResultSets(rs);
        }

        // enqueue entry, no initialisation is done, non-null user
        set = con.createStatement().executeQuery("select id from cs_class where name like 'test4'");
        set.next();
        classId = set.getInt(1);
        DBConnection.closeResultSets(set);

        mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        // we need another instance because we will shutdown the executor to ensure it has finished all the jobs
        history = new HistoryServer(server);
        history.enqueueEntry(mo, user, now);

        // wait for the executor to finish
        executor = (ExecutorService)executorField.get(history);
        executor.shutdown();
        assertTrue("executor did not terminate in time", executor.awaitTermination(2, TimeUnit.SECONDS));

        ps = server.getConnectionPool().getDBConnection().getConnection()
                    .prepareStatement(
                            "select count(*) from cs_history where ug_id = 1 and usr_id = 1 and class_id = "
                            + classId
                            + " and object_id = 1");
        rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("not one result found", rs.next());
            assertEquals("count is not 1", 1, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        rs = null;
        try {
            pSgetHistory.setObject(1, new Timestamp(now.getTime()));
            pSgetHistory.setInt(2, classId);
            rs = pSgetHistory.executeQuery();
            assertTrue("no result found", rs.next());
            // check if user info is present and correct
            assertEquals("wrong ug_id", user.getUserGroup().getId(), rs.getInt("ug_id"));
            assertEquals("wrong usr_id", user.getId(), rs.getInt("usr_id"));
            assertFalse("too many results", rs.next());
        } finally {
            DBConnection.closeResultSets(rs);
        }

        // another enqueue, null user
        set = con.createStatement().executeQuery("select id from cs_class where name like 'test2'");
        set.next();
        classId = set.getInt(1);
        DBConnection.closeResultSets(set);

        mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        // we need another instance because we will shutdown the executor to ensure it has finished all the jobs
        history = new HistoryServer(server);
        history.enqueueEntry(mo, null, now);

        // wait for the executor to finish
        executor = (ExecutorService)executorField.get(history);
        executor.shutdown();
        assertTrue("executor did not terminate in time", executor.awaitTermination(2, TimeUnit.SECONDS));

        ps = server.getConnectionPool().getDBConnection().getConnection()
                    .prepareStatement(
                            "select count(*) from cs_history where class_id = "
                            + classId
                            + " and object_id = 1");
        rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("not one result found", rs.next());
            assertEquals("count is not 4", 4, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        rs = null;
        try {
            pSgetHistory.setObject(1, new Timestamp(now.getTime()));
            pSgetHistory.setInt(2, classId);
            rs = pSgetHistory.executeQuery();
            assertTrue("no result found", rs.next());
            // check if no user info is present
            assertNull("wrong ug_id", rs.getObject("ug_id"));
            assertNull("wrong usr_id", rs.getObject("usr_id"));
            assertFalse("too many results", rs.next());
        } finally {
            DBConnection.closeResultSets(rs);
        }

        // another enqueue entry, no initialisation is done, non-null user, anonymous option enabled
        set = con.createStatement().executeQuery("select id from cs_class where name like 'test6'");
        set.next();
        classId = set.getInt(1);
        DBConnection.closeResultSets(set);

        mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        // we need another instance because we will shutdown the executor to ensure it has finished all the jobs
        history = new HistoryServer(server);
        history.enqueueEntry(mo, user, now);

        // wait for the executor to finish
        executor = (ExecutorService)executorField.get(history);
        executor.shutdown();
        assertTrue("executor did not terminate in time", executor.awaitTermination(2, TimeUnit.SECONDS));

        ps = server.getConnectionPool().getDBConnection().getConnection()
                    .prepareStatement(
                            "select count(*) from cs_history where class_id = "
                            + classId
                            + " and object_id = 1");
        rs = null;
        try {
            rs = ps.executeQuery();
            assertTrue("not one result found", rs.next());
            assertEquals("count is not 2", 2, rs.getInt(1));
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(ps);
        }

        rs = null;
        try {
            pSgetHistory.setObject(1, new Timestamp(now.getTime()));
            pSgetHistory.setInt(2, classId);
            rs = pSgetHistory.executeQuery();
            assertTrue("no result found", rs.next());
            // check if no user info is present
            assertNull("wrong ug_id", rs.getObject("ug_id"));
            assertNull("wrong usr_id", rs.getObject("usr_id"));
            assertFalse("too many results", rs.next());
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(pSgetHistory);
        }

        con.close();
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEnqueueEntryNullMO() throws Exception {
        server.getHistoryServer().enqueueEntry(null, user, new Date());
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test(expected = IllegalArgumentException.class)
    public void testEnqueueEntryNullDate() throws Exception {
        final Connection con = server.getConnectionPool().getDBConnection().getConnection();
        final ResultSet set = con.createStatement().executeQuery("select id from cs_class where name like 'test5'");
        set.next();
        final int classId = set.getInt(1);
        DBConnection.closeResultSets(set);
        con.close();

        final Method getMoMethod = HistoryServer.class.getDeclaredMethod(
                "getMetaObject",
                int.class,
                int.class,
                User.class);
        getMoMethod.setAccessible(true);
        final MetaObject mo = (MetaObject)getMoMethod.invoke(server.getHistoryServer(), classId, 1, user);

        server.getHistoryServer().enqueueEntry(mo, user, null);
    }
}
