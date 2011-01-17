/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.user;

import de.cismet.remotetesthelper.RemoteTestHelperService;
import de.cismet.remotetesthelper.ws.rest.RemoteTestHelperClient;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnectionPool;

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

import java.util.Properties;

import de.cismet.tools.ScriptRunner;

import static org.junit.Assert.*;

/**
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class UserStoreTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(
            UserStoreTest.class);
    private static final String TEST = "TEST ";
    private static final String TEST_DB_NAME = "user_store_test_db";
    private static final RemoteTestHelperService service = new RemoteTestHelperClient();

    private static ServerProperties props;
    private static DBConnectionPool pool;

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @BeforeClass
    public static void setUpClass() throws Throwable {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender"); // NOI18N
        p.put("log4j.appender.Remote.remoteHost", "localhost");                // NOI18N
        p.put("log4j.appender.Remote.port", "4445");                           // NOI18N
        p.put("log4j.appender.Remote.locationInfo", "true");                   // NOI18N
        p.put("log4j.rootLogger", "ALL,Remote");                               // NOI18N
        PropertyConfigurator.configure(p);

        if (!Boolean.valueOf(service.initCidsSystem(TEST_DB_NAME))) {
            throw new IllegalStateException("cannot initilise test db");
        }

        props = new ServerProperties(UserStoreTest.class.getResourceAsStream(
                    "/Sirius/server/localserver/user/runtime.properties"));  // NOI18N
        pool = new DBConnectionPool(props);
        final ScriptRunner runner = new ScriptRunner(pool.getDBConnection().getConnection(), true, false);
        final InputStream schemaStream = UserStoreTest.class.getResourceAsStream(
                "/Sirius/server/sql/cs_config_attr_schema.sql");               // NOI18N
        final InputStream scriptStream = UserStoreTest.class.getResourceAsStream(
                "/Sirius/server/localserver/user/configAttrTestData.sql");     // NOI18N
        final BufferedReader schemaReader = new BufferedReader(new InputStreamReader(schemaStream));
        final BufferedReader scriptReader = new BufferedReader(new InputStreamReader(scriptStream));
        try {
            runner.runScript(schemaReader);
            runner.runScript(scriptReader);
        } finally {
            schemaReader.close();
            scriptReader.close();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Throwable {
        pool.shutdown();

        if (!Boolean.valueOf(service.dropDatabase(TEST_DB_NAME))) {
            throw new IllegalStateException("could not drop test db");
        }
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
    @Ignore
    @Test
    public void testGetUsers() {
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testGetUserGroups() {
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testGetMemberships() {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testChangePassword() throws Exception {
    }

    /**
     * DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testValidateUser() {
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testValidateUserPassword_OK() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final UserStore us = new UserStore(pool, props);
        assertTrue("invalid user + password",
            us.validateUserPassword(new User(-1, "admin", null), "cismet"));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testValidateUserPassword_invalidPW() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final UserStore us = new UserStore(pool, props);
        assertFalse("valid user + password",
            us.validateUserPassword(new User(-1, "admin", null), "s"));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testValidateUserPassword_invalidUser() throws Throwable {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final UserStore us = new UserStore(pool, props);
        assertFalse("valid user + password",
            us.validateUserPassword(new User(-1, "invalid", null), "s"));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Test
    public void testGetConfigAttr() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info(TEST + getCurrentMethodName());
        }
        final UserStore us = new UserStore(pool, props);
        final UserGroup adminGroup = new UserGroup(1, "Administratoren", "LOCAL");
        final User admin = new User(1, "admin", "LOCAL", adminGroup);
        String result = us.getConfigAttr(admin, "abc");
        assertEquals("alphabeth3", result);
        result = us.getConfigAttr(admin, "cba");
        assertNull(result);
        final User group = new User(-1, "admin", "LOCAL", adminGroup);
        result = us.getConfigAttr(group, "abc");
        assertEquals("alphabeth2", result);
        final UserGroup domainGroup = new UserGroup(-1, "", "LOCAL");
        final User domain = new User(-1, "", "", domainGroup);
        result = us.getConfigAttr(domain, "abc");
        assertEquals("alphabeth", result);
        result = us.getConfigAttr(null, "abc");
        assertNull(result);
        result = us.getConfigAttr(domain, null);
        assertNull(result);
        result = us.getConfigAttr(domain, "abcd");
        assertNull(result);
    }
}
