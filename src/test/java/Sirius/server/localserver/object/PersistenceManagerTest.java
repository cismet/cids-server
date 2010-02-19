/*
 * PersistenceManagerTest.java, encoding: UTF-8
 *
 * Copyright (C) by:
 *
 *----------------------------
 * cismet GmbH
 * Altenkesslerstr. 17
 * Gebaeude D2
 * 66115 Saarbruecken
 * http://www.cismet.de
 *----------------------------
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * See: http://www.gnu.org/licenses/lgpl.txt
 *
 *----------------------------
 * Author:
 * martin.scholl@cismet.de
 *----------------------------
 *
 * Created on 2009
 *
 */

package Sirius.server.localserver.object;

import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.permission.Policy;
import Sirius.server.property.ServerProperties;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Logger;
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
public class PersistenceManagerTest
{
    private static final transient Logger LOG =
            Logger.getLogger(PersistenceManagerTest.class);

    public static final String STMT_SEL_ATTR_STRING =
            "SELECT * FROM cs_attr_string "
            + "WHERE class_id = ? and object_id = ?";
    public static final String STMT_SEL_ATTR_MAP =
            "SELECT * FROM cs_all_attr_mapping "
            + "WHERE class_id = ? AND object_id = ?";

    private static final int DEFAULT_ID = 77777777;
    private static final int DEFAULT_CLASS_ID = 88888888;
    private static final int DEFAULT_OBJECT_ID = 99999999;

    private static DBServer server;

    public PersistenceManagerTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Throwable
    {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        p.put("log4j.appender.Remote.remoteHost", "localhost");
        p.put("log4j.appender.Remote.port", "4445");
        p.put("log4j.appender.Remote.locationInfo", "true");
        p.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(p);
        final InputStream is = PersistenceManagerTest.class
                .getResourceAsStream("runtime.properties");
        server = new DBServer(new ServerProperties(is));
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

    /**
     * Test of deleteMetaObject method, of class PersistenceManager.
     */
    @Ignore
    @Test
    public void testDeleteMetaObject() throws Exception
    {
        System.out.println("deleteMetaObject");
    }

    /**
     * Test of updateMetaObject method, of class PersistenceManager.
     */
    @Ignore
    @Test
    public void testUpdateMetaObject() throws Exception
    {
        System.out.println("TEST updateMetaObject");
    }

    /**
     * Test of updateArrayObjects method, of class PersistenceManager.
     */
    @Ignore
    @Test
    public void testUpdateArrayObjects() throws Exception
    {
        System.out.println("TEST updateArrayObjects");
    }

    /**
     * Test of insertMetaObjectArray method, of class PersistenceManager.
     */
    @Ignore
    @Test
    public void testInsertMetaObjectArray() throws Exception
    {
        System.out.println("TEST insertMetaObjectArray");
    }

    /**
     * Test of insertMetaObject method, of class PersistenceManager.
     */
    @Ignore
    @Test
    public void testInsertMetaObject() throws Exception
    {
        System.out.println("TEST insertMetaObject");
    }

    @Test
    public void testInsertIndex_NoAttr() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_NoAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index present", indexPresent(mo, 0, 0));
    }

    @Test
    public void testInsertIndex_MO1StringAttr() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1StringAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 1, 0));
    }

    @Test
    public void testInsertIndex_MO2StringAttr() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_2StringAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID, "val1", "val2");
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 2, 0));
    }

    @Test
    public void testInsertIndex_MO1ObjAttr() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1ObjAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 0, 1));
    }

    @Test
    public void testInsertIndex_MO2ObjAttr() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_2ObjAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 0, 2));
    }

    @Test
    public void testInsertIndex_MO2StringAttr2ObjAttr() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_2String2ObjectAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID, "val1", "val2");
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 2, 2));
    }

    @Test
    public void testUpdateIndex_MO2StringAttr() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("updateIndex", MetaObject.class);
        method.setAccessible(true);
        final Method methodInsert = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        methodInsert.setAccessible(true);
        final MetaObject moOld = createMO_2StringAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID, "val1", "val2");
        methodInsert.invoke(pm, moOld);
        final DefaultMetaObject mo = (DefaultMetaObject)createMO_2StringAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID, "NEWval1", "NEWval2");
        for(final ObjectAttribute oa : mo.getAttribs())
        {
            oa.setChanged(true);
        }
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 2, 0));
    }

    @Test
    public void testUpdateIndex_MO2ObjAttr2ndChanged() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("updateIndex", MetaObject.class);
        method.setAccessible(true);
        final Method methodInsert = pm.getClass()
                .getDeclaredMethod("insertIndex", MetaObject.class);
        methodInsert.setAccessible(true);
        final MetaObject moOld = createMO_2ObjAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID);
        methodInsert.invoke(pm, moOld);
        final MetaObject mo = createMO_2ObjAttr2ndChanged(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID, DEFAULT_OBJECT_ID - 13);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 0, 1));
    }

    @Test
    public void testDeleteIndex_NoIndex() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("deleteIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1StringAttr(-1, -1);
        method.invoke(pm, mo);
        assertTrue("index present", indexPresent(mo, 0, 0));
    }

    @Test
    public void testDeleteIndex_ArbitraryIndexes() throws Throwable
    {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass()
                .getDeclaredMethod("deleteIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1StringAttr(
                DEFAULT_CLASS_ID, DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index present", indexPresent(mo, 0, 0));
    }

    // TODO: cs_all_attr_mapping tests

    private MetaObject createMO_NoAttr(final int classId, final int objectId)
            throws
            Exception
    {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(classId);
        mai1.setId(DEFAULT_ID);
        dO.addAttribute(new ObjectAttribute(
                mai1, DEFAULT_OBJECT_ID, "OA_val1", Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai1, DEFAULT_OBJECT_ID, "OA_val2", Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    private MetaObject createMO_1StringAttr(final int classId,
            final int objectId) throws
            Exception
    {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(DEFAULT_CLASS_ID);
        mai1.setId(DEFAULT_ID);
        mai1.setIndexed(true);
        final MemberAttributeInfo mai2 = new MemberAttributeInfo();
        mai2.setClassId(DEFAULT_CLASS_ID);
        mai2.setId(DEFAULT_ID + 1);
        dO.addAttribute(new ObjectAttribute(
                mai1, DEFAULT_OBJECT_ID, "OA_val1", Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2, DEFAULT_OBJECT_ID, "OA_val2", Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    private MetaObject createMO_2StringAttr(final int classId,
            final int objectId, final String val1, final String val2) throws
            Exception
    {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(DEFAULT_CLASS_ID);
        mai1.setId(DEFAULT_ID);
        mai1.setIndexed(true);
        final MemberAttributeInfo mai2 = new MemberAttributeInfo();
        mai2.setClassId(DEFAULT_CLASS_ID);
        mai2.setId(DEFAULT_ID + 1);
        mai2.setIndexed(true);
        dO.addAttribute(new ObjectAttribute(
                mai1, DEFAULT_OBJECT_ID, val1, Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2, DEFAULT_OBJECT_ID, val2, Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    private MetaObject createMO_1ObjAttr(final int classId,
            final int objectId) throws
            Exception
    {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(DEFAULT_CLASS_ID);
        mai1.setId(DEFAULT_ID);
        mai1.setForeignKey(true);
        mai1.setIndexed(true);
        final MemberAttributeInfo mai2 = new MemberAttributeInfo();
        mai2.setClassId(DEFAULT_CLASS_ID);
        mai2.setId(DEFAULT_ID + 1);
        final DefaultMetaObject m1 = new DefaultMetaObject(
                new DefaultObject(objectId + 1, classId + 1), "NODOMAIN");
        final DefaultMetaObject m2 = new DefaultMetaObject(
                new DefaultObject(objectId + 2, classId + 2), "NODOMAIN");
        dO.addAttribute(new ObjectAttribute(
                mai1, DEFAULT_OBJECT_ID, m1, Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2, DEFAULT_OBJECT_ID, m2, Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    private MetaObject createMO_2ObjAttr(final int classId,
            final int objectId) throws
            Exception
    {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(DEFAULT_CLASS_ID);
        mai1.setId(DEFAULT_ID);
        mai1.setForeignKey(true);
        mai1.setIndexed(true);
        final MemberAttributeInfo mai2 = new MemberAttributeInfo();
        mai2.setClassId(DEFAULT_CLASS_ID);
        mai2.setId(DEFAULT_ID + 1);
        mai2.setIndexed(true);
        mai2.setForeignKey(true);
        final DefaultMetaObject m1 = new DefaultMetaObject(
                new DefaultObject(objectId + 1, classId + 1), "NODOMAIN");
        final DefaultMetaObject m2 = new DefaultMetaObject(
                new DefaultObject(objectId + 2, classId + 2), "NODOMAIN");
        dO.addAttribute(new ObjectAttribute(
                mai1, DEFAULT_OBJECT_ID, m1, Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2, DEFAULT_OBJECT_ID, m2, Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    private MetaObject createMO_2ObjAttr2ndChanged(final int classId,
            final int objectId, final int attrObjectId) throws
            Exception
    {
        final DefaultMetaObject mo = (DefaultMetaObject)createMO_2ObjAttr(
                classId, objectId);
        final ObjectAttribute oa = mo.getAttribs()[1];
        oa.setValue(new DefaultMetaObject(
                new DefaultObject(attrObjectId,
                ((MetaObject)oa.getValue()).getClassID()), "NODOMAIN"));
        mo.getAttribs()[1].setChanged(true);
        return mo;
    }

    private MetaObject createMO_2String2ObjectAttr(final int classId,
            final int objectId, final String val1, final String val2) throws
            Exception
    {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(DEFAULT_CLASS_ID);
        mai1.setId(DEFAULT_ID);
        mai1.setIndexed(true);
        final MemberAttributeInfo mai2 = new MemberAttributeInfo();
        mai2.setClassId(DEFAULT_CLASS_ID);
        mai2.setId(DEFAULT_ID + 1);
        mai2.setIndexed(true);
        final MemberAttributeInfo mai3 = new MemberAttributeInfo();
        mai3.setClassId(DEFAULT_CLASS_ID);
        mai3.setId(DEFAULT_ID + 2);
        mai3.setForeignKey(true);
        mai3.setIndexed(true);
        final MemberAttributeInfo mai4 = new MemberAttributeInfo();
        mai4.setClassId(DEFAULT_CLASS_ID);
        mai4.setId(DEFAULT_ID + 3);
        mai4.setIndexed(true);
        mai4.setForeignKey(true);
        dO.addAttribute(new ObjectAttribute(
                mai1, DEFAULT_OBJECT_ID, val1, Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2, DEFAULT_OBJECT_ID, val2, Policy.createWIKIPolicy()));
        final DefaultMetaObject m3 = new DefaultMetaObject(
                new DefaultObject(objectId + 1, classId + 1), "NODOMAIN");
        final DefaultMetaObject m4 = new DefaultMetaObject(
                new DefaultObject(objectId + 2, classId + 2), "NODOMAIN");
        dO.addAttribute(new ObjectAttribute(
                mai3, DEFAULT_OBJECT_ID, m3, Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai4, DEFAULT_OBJECT_ID, m4, Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    // TODO: perform finer grained test where the values are checked, too.
    private boolean indexPresent(final MetaObject mo, final int stringRows,
            final int objectRows)
    {
        final Connection con = server.getActiveDBConnection().getConnection();
        final int classId = mo.getClassID();
        final int objectId = mo.getID();
        PreparedStatement stmtAttrString = null;
        PreparedStatement stmtAttrMap = null;
        ResultSet rsStr = null;
        ResultSet rsMap = null;
        try
        {
            stmtAttrString = con.prepareStatement(STMT_SEL_ATTR_STRING);
            stmtAttrString.setInt(1, classId);
            stmtAttrString.setInt(2, objectId);
            stmtAttrMap = con.prepareStatement(STMT_SEL_ATTR_MAP);
            stmtAttrMap.setInt(1, classId);
            stmtAttrMap.setInt(2, objectId);
            rsStr = stmtAttrString.executeQuery();
            rsMap = stmtAttrMap.executeQuery();
            int attrStringRows = 0;
            while(rsStr.next())
            {
                boolean found = false;
                for(final ObjectAttribute oa : mo.getAttribs())
                {
                    final MemberAttributeInfo mai = oa.getMai();
                    if(mai.isIndexed())
                    {
                        if(oa.getValue().equals(rsStr.getString("string_val"))
                                && mai.getId() == rsStr.getInt("attr_id"))
                        {
                            found = true;
                        }
                    }
                }
                if(!found)
                {
                    return false;
                }
                ++attrStringRows;
            }
            int attrMapRows = 0;
            while(rsMap.next())
            {
                final int classID = rsMap.getInt("class_id");
                final int objectID = rsMap.getInt("object_id");
                final int attrClassID = rsMap.getInt("attr_class_id");
                final int attrObjectID = rsMap.getInt("attr_object_id");
                if(classID < attrClassID && objectID < attrObjectID)
                {
                    ++attrMapRows;
                }
            }
            return attrStringRows == stringRows && attrMapRows == objectRows;
        }catch(final SQLException e)
        {
            LOG.error("could not check for indexes", e);
        }finally
        {
            closeResultSets(rsStr, rsMap);
            closeStatements(stmtAttrString, stmtAttrMap);
        }
        return false;
    }

    private void closeResultSet(final ResultSet rs)
    {
        if(rs != null)
        {
            try
            {
                rs.close();
            }catch(final SQLException e)
            {
                LOG.warn("could not close resultset: " + rs, e);
            }
        }
    }

    private void closeResultSets(final ResultSet... rss)
    {
        for(final ResultSet rs : rss)
        {
            closeResultSet(rs);
        }
    }

    private void closeStatement(final Statement s)
    {
        if(s != null)
        {
            try
            {
                s.close();
            }catch(final SQLException e)
            {
                LOG.warn("could not close statement: " + s, e);
            }
        }
    }

    private void closeStatements(final Statement... ss)
    {
        for(final Statement s : ss)
        {
            closeStatement(s);
        }
    }
}
