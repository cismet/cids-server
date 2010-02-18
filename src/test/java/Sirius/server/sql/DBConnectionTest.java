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
package Sirius.server.sql;

import java.sql.ResultSet;
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
 * @author martin.scholl@cismet.de
 */
public class DBConnectionTest
{
    private static final transient Logger LOG = Logger.getLogger(
            DBConnectionTest.class);
    private static final String TEST = "TEST "; // NOI18N
    private static final DBClassifier DB_CLASSIFIER = new DBClassifier(
                "jdbc:postgresql://kif:5432/wuli_server_dev", // NOI18N
                "postgres", // NOI18N
                "x", // NOI18N
                "org.postgresql.Driver"); // NOI18N

    @BeforeClass
    public static void setUpClass()
    {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        PropertyConfigurator.configure(p);
    }

    @AfterClass
    public static void tearDownClass()
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

    @Test
    public void testCharToBool()
    {
        if(LOG.isInfoEnabled())
        {
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

    @Test
    public void testStringToBool()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final String message = "String was: ";
        final String[] trues = new String[]
        {
            "t", "T", "tT", "Tt", "T23asdjk", "t32987tjng§", "T.yjflsajg"
        };
        for(final String s : trues)
        {
            assertTrue(message + s, DBConnection.stringToBool(s));
        }
        final String[] falses = new String[]
        {
            "a", "A", "Aasdf", "afdg4rgf", "..fdas", "///", "\\", "\t", " "
        };
        for(final String s : falses)
        {
            assertFalse(message + s, DBConnection.stringToBool(s));
        }
    }

    @Test
    public void testSubmitInternalQueryOK() throws Throwable
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1= null;
        try
        {
            set1 = con.submitInternalQuery(
                    "verify_user_password", "admin", "sb");
            if(set1.next())
            {
                assertEquals("not exactly one user found", 1, set1.getInt(1));
            }else
            {
                fail("illegal resultset state");
            }
        }finally
        {
            DBConnection.closeResultSets(set1);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSubmitInternalQueryInvalidDescriptor() throws Throwable
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1= null;
        try
        {
            set1 = con.submitInternalQuery("x", "admin", "sb");
        }finally
        {
            DBConnection.closeResultSets(set1);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSubmitInternalQueryTooManyParams() throws Throwable
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1= null;
        try
        {
            set1 = con.submitInternalQuery(
                    "verify_user_password", "admin", "sb", "sb");
        }finally
        {
            DBConnection.closeResultSets(set1);
        }
    }

    @Test(expected=IllegalArgumentException.class)
    public void testSubmitInternalQueryTooLessParams() throws Throwable
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info(TEST + getCurrentMethodName());
        }
        final DBConnection con = new DBConnection(DB_CLASSIFIER);
        ResultSet set1= null;
        try
        {
            set1 = con.submitInternalQuery(
                    "verify_user_password", "admin");
        }finally
        {
            DBConnection.closeResultSets(set1);
        }
    }

    @Ignore
    @Test
    public void testSubmitQuery_String_ObjectArr()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testSubmitQuery_int_ObjectArr()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testSubmitQuery_Query()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testSubmitUpdate_Query()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testSubmitUpdate_String_ObjectArr()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testSubmitUpdate_int_ObjectArr()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testFetchStatement_String()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testFetchStatement_int()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testGetStatementCache()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testExecuteQuery()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testSubmitUpdateBatch_int_ObjectArr()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }

    @Ignore
    @Test
    public void testSubmitUpdateBatch_String_ObjectArr()
    {
        if(LOG.isInfoEnabled())
        {
            LOG.info("TEST " + getCurrentMethodName());
        }
    }
}