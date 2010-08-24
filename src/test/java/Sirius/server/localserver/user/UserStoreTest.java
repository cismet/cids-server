/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.user;

import Sirius.server.newuser.User;
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

import java.util.Properties;

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
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        PropertyConfigurator.configure(p);
        props = new ServerProperties(
                UserStoreTest.class.getResourceAsStream(
                    "/Sirius/server/localserver/object/" // NOI18N
                            + "runtime.properties"));    // NOI18N
        pool = new DBConnectionPool(props);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  Exception DOCUMENT ME!
     */
    @AfterClass
    public static void tearDownClass() throws Throwable {
        pool.shutdown();
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
}
