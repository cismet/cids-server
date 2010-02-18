/*
 *  Copyright (C) 2010 mscholl
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Sirius.server.localserver.user;

import Sirius.server.newuser.User;
import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnectionPool;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mscholl
 */
public class UserStoreTest
{
    private static final transient Logger LOG = Logger.getLogger(
            UserStoreTest.class);
    private static final String TEST = "TEST ";

    private static ServerProperties props;
    private static DBConnectionPool pool;

    @BeforeClass
    public static void setUpClass() throws Throwable
    {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        PropertyConfigurator.configure(p);
        props = new ServerProperties(UserStoreTest.class.getResourceAsStream(
                "/Sirius/server/localserver/object/" // NOI18N
                + "runtime.properties")); // NOI18N
        pool = new DBConnectionPool(props);
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    private String getCurrentMethodName()
    {
        return new Throwable().getStackTrace()[1].getMethodName();
    }

    @Ignore
    @Test
    public void testGetUsers()
    {
    }

    @Ignore
    @Test
    public void testGetUserGroups()
    {
    }

    @Ignore
    @Test
    public void testGetMemberships()
    {
    }

    @Ignore
    @Test
    public void testChangePassword() throws Exception
    {
    }

    @Ignore
    @Test
    public void testValidateUser()
    {
    }

    @Test
    public void testValidateUserPassword_OK() throws Throwable
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final UserStore us = new UserStore(pool, props);
        assertTrue("invalid user + password",
                us.validateUserPassword(new User(-1, "admin", null), "sb"));
    }

    @Test
    public void testValidateUserPassword_invalidPW() throws Throwable
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final UserStore us = new UserStore(pool, props);
        assertFalse("valid user + password",
                us.validateUserPassword(new User(-1, "admin", null), "s"));
    }

    @Test
    public void testValidateUserPassword_invalidUser() throws Throwable
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final UserStore us = new UserStore(pool, props);
        assertFalse("valid user + password",
                us.validateUserPassword(new User(-1, "invalid", null), "s"));
    }
}