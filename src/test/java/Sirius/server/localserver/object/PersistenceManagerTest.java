/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   mscholl
 * @version  $Revision$, $Date$
 */
public class PersistenceManagerTest {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(PersistenceManagerTest.class);

    public static final String STMT_SEL_ATTR_STRING = "SELECT * FROM cs_attr_string "
        + "WHERE class_id = ? and object_id = ?";
    public static final String STMT_SEL_ATTR_MAP = "SELECT * FROM cs_all_attr_mapping "
        + "WHERE class_id = ? AND object_id = ?";

    private static final int DEFAULT_ID = 77777777;
    private static final int DEFAULT_CLASS_ID = 88888888;
    private static final int DEFAULT_OBJECT_ID = 99999999;

    private static DBServer server;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PersistenceManagerTest object.
     */
    public PersistenceManagerTest() {
    }

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
        org.apache.log4j.PropertyConfigurator.configure(p);
        final InputStream is = PersistenceManagerTest.class.getResourceAsStream("runtime.properties");
        server = new DBServer(new ServerProperties(is));
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
     * Test of deleteMetaObject method, of class PersistenceManager.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testDeleteMetaObject() throws Exception {
        System.out.println("deleteMetaObject");
    }

    /**
     * Test of updateMetaObject method, of class PersistenceManager.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testUpdateMetaObject() throws Exception {
        System.out.println("TEST updateMetaObject");
    }

    /**
     * Test of updateArrayObjects method, of class PersistenceManager.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testUpdateArrayObjects() throws Exception {
        System.out.println("TEST updateArrayObjects");
    }

    /**
     * Test of insertMetaObjectArray method, of class PersistenceManager.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testInsertMetaObjectArray() throws Exception {
        System.out.println("TEST insertMetaObjectArray");
    }

    /**
     * Test of insertMetaObject method, of class PersistenceManager.
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Ignore
    @Test
    public void testInsertMetaObject() throws Exception {
        System.out.println("TEST insertMetaObject");
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testInsertIndex_NoAttr() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_NoAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index present", indexPresent(mo, 0, 0));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testInsertIndex_MO1StringAttr() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1StringAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 1, 0));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testInsertIndex_MO2StringAttr() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_2StringAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID,
                "val1",
                "val2");
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 2, 0));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testInsertIndex_MO1ObjAttr() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1ObjAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 0, 1));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testInsertIndex_MO2ObjAttr() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_2ObjAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 0, 2));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testInsertIndex_MO2StringAttr2ObjAttr() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_2String2ObjectAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID,
                "val1",
                "val2");
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 2, 2));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testUpdateIndex_MO2StringAttr() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("updateIndex", MetaObject.class);
        method.setAccessible(true);
        final Method methodInsert = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        methodInsert.setAccessible(true);
        final MetaObject moOld = createMO_2StringAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID,
                "val1",
                "val2");
        methodInsert.invoke(pm, moOld);
        final DefaultMetaObject mo = (DefaultMetaObject)createMO_2StringAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID,
                "NEWval1",
                "NEWval2");
        for (final ObjectAttribute oa : mo.getAttribs()) {
            oa.setChanged(true);
        }
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 2, 0));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testUpdateIndex_MO2ObjAttr2ndChanged() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("updateIndex", MetaObject.class);
        method.setAccessible(true);
        final Method methodInsert = pm.getClass().getDeclaredMethod("insertIndex", MetaObject.class);
        methodInsert.setAccessible(true);
        final MetaObject moOld = createMO_2ObjAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID);
        methodInsert.invoke(pm, moOld);
        final MetaObject mo = createMO_2ObjAttr2ndChanged(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID,
                DEFAULT_OBJECT_ID - 13);
        method.invoke(pm, mo);
        assertTrue("index not present", indexPresent(mo, 0, 1));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testDeleteIndex_NoIndex() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("deleteIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1StringAttr(-1, -1);
        method.invoke(pm, mo);
        assertTrue("index present", indexPresent(mo, 0, 0));
    }

    /**
     * DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    @Test
    public void testDeleteIndex_ArbitraryIndexes() throws Throwable {
        System.out.println("TEST " + getCurrentMethodName());
        final PersistenceManager pm = new PersistenceManager(server);
        final Method method = pm.getClass().getDeclaredMethod("deleteIndex", MetaObject.class);
        method.setAccessible(true);
        final MetaObject mo = createMO_1StringAttr(
                DEFAULT_CLASS_ID,
                DEFAULT_OBJECT_ID);
        method.invoke(pm, mo);
        assertTrue("index present", indexPresent(mo, 0, 0));
    }

    /**
     * TODO: cs_all_attr_mapping tests
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private MetaObject createMO_NoAttr(final int classId, final int objectId) throws Exception {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(classId);
        mai1.setId(DEFAULT_ID);
        dO.addAttribute(new ObjectAttribute(
                mai1,
                DEFAULT_OBJECT_ID,
                "OA_val1",
                Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai1,
                DEFAULT_OBJECT_ID,
                "OA_val2",
                Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private MetaObject createMO_1StringAttr(final int classId,
            final int objectId) throws Exception {
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
                mai1,
                DEFAULT_OBJECT_ID,
                "OA_val1",
                Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2,
                DEFAULT_OBJECT_ID,
                "OA_val2",
                Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     * @param   val1      DOCUMENT ME!
     * @param   val2      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private MetaObject createMO_2StringAttr(final int classId,
            final int objectId, final String val1, final String val2) throws Exception {
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
                mai1,
                DEFAULT_OBJECT_ID,
                val1,
                Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2,
                DEFAULT_OBJECT_ID,
                val2,
                Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private MetaObject createMO_1ObjAttr(final int classId,
            final int objectId) throws Exception {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(DEFAULT_CLASS_ID);
        mai1.setId(DEFAULT_ID);
        mai1.setForeignKey(true);
        mai1.setIndexed(true);
        mai1.setForeignKeyClassId(classId + 1);
        final MemberAttributeInfo mai2 = new MemberAttributeInfo();
        mai2.setClassId(DEFAULT_CLASS_ID);
        mai2.setId(DEFAULT_ID + 1);
        mai2.setForeignKeyClassId(classId + 2);
        final DefaultMetaObject m1 = new DefaultMetaObject(
                new DefaultObject(objectId + 1, classId + 1),
                "NODOMAIN");
        final DefaultMetaObject m2 = new DefaultMetaObject(
                new DefaultObject(objectId + 2, classId + 2),
                "NODOMAIN");
        dO.addAttribute(new ObjectAttribute(
                mai1,
                DEFAULT_OBJECT_ID,
                m1,
                Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2,
                DEFAULT_OBJECT_ID,
                m2,
                Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private MetaObject createMO_2ObjAttr(final int classId,
            final int objectId) throws Exception {
        final DefaultObject dO = new DefaultObject(objectId, classId);
        dO.setDummy(false);
        final MemberAttributeInfo mai1 = new MemberAttributeInfo();
        mai1.setClassId(DEFAULT_CLASS_ID);
        mai1.setId(DEFAULT_ID);
        mai1.setForeignKey(true);
        mai1.setIndexed(true);
        mai1.setForeignKeyClassId(classId + 1);
        final MemberAttributeInfo mai2 = new MemberAttributeInfo();
        mai2.setClassId(DEFAULT_CLASS_ID);
        mai2.setId(DEFAULT_ID + 1);
        mai2.setIndexed(true);
        mai2.setForeignKey(true);
        mai2.setForeignKeyClassId(classId + 2);
        final DefaultMetaObject m1 = new DefaultMetaObject(
                new DefaultObject(objectId + 1, classId + 1),
                "NODOMAIN");
        final DefaultMetaObject m2 = new DefaultMetaObject(
                new DefaultObject(objectId + 2, classId + 2),
                "NODOMAIN");
        dO.addAttribute(new ObjectAttribute(
                mai1,
                DEFAULT_OBJECT_ID,
                m1,
                Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2,
                DEFAULT_OBJECT_ID,
                m2,
                Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId       DOCUMENT ME!
     * @param   objectId      DOCUMENT ME!
     * @param   attrObjectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private MetaObject createMO_2ObjAttr2ndChanged(final int classId,
            final int objectId, final int attrObjectId) throws Exception {
        final DefaultMetaObject mo = (DefaultMetaObject)createMO_2ObjAttr(
                classId,
                objectId);
        final ObjectAttribute oa = mo.getAttribs()[1];
        oa.setValue(
            new DefaultMetaObject(
                new DefaultObject(attrObjectId,
                    ((MetaObject)oa.getValue()).getClassID()),
                "NODOMAIN"));
        mo.getAttribs()[1].setChanged(true);
        return mo;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     * @param   val1      DOCUMENT ME!
     * @param   val2      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private MetaObject createMO_2String2ObjectAttr(
            final int classId,
            final int objectId,
            final String val1,
            final String val2) throws Exception {
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
        mai3.setForeignKeyClassId(classId + 1);
        final MemberAttributeInfo mai4 = new MemberAttributeInfo();
        mai4.setClassId(DEFAULT_CLASS_ID);
        mai4.setId(DEFAULT_ID + 3);
        mai4.setIndexed(true);
        mai4.setForeignKey(true);
        mai4.setForeignKeyClassId(classId + 2);
        dO.addAttribute(new ObjectAttribute(
                mai1,
                DEFAULT_OBJECT_ID,
                val1,
                Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai2,
                DEFAULT_OBJECT_ID,
                val2,
                Policy.createWIKIPolicy()));
        final DefaultMetaObject m3 = new DefaultMetaObject(
                new DefaultObject(objectId + 1, classId + 1),
                "NODOMAIN");
        final DefaultMetaObject m4 = new DefaultMetaObject(
                new DefaultObject(objectId + 2, classId + 2),
                "NODOMAIN");
        dO.addAttribute(new ObjectAttribute(
                mai3,
                DEFAULT_OBJECT_ID,
                m3,
                Policy.createWIKIPolicy()));
        dO.addAttribute(new ObjectAttribute(
                mai4,
                DEFAULT_OBJECT_ID,
                m4,
                Policy.createWIKIPolicy()));
        final DefaultMetaObject mo = new DefaultMetaObject(dO, "NODOMAIN");
        return mo;
    }
    /**
     * TODO: perform finer grained test where the values are checked, too.
     *
     * @param   mo          DOCUMENT ME!
     * @param   stringRows  DOCUMENT ME!
     * @param   objectRows  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean indexPresent(final MetaObject mo, final int stringRows,
            final int objectRows) {
        final Connection con = server.getActiveDBConnection().getConnection();
        final int classId = mo.getClassID();
        final int objectId = mo.getID();
        PreparedStatement stmtAttrString = null;
        PreparedStatement stmtAttrMap = null;
        ResultSet rsStr = null;
        ResultSet rsMap = null;
        try {
            stmtAttrString = con.prepareStatement(STMT_SEL_ATTR_STRING);
            stmtAttrString.setInt(1, classId);
            stmtAttrString.setInt(2, objectId);
            stmtAttrMap = con.prepareStatement(STMT_SEL_ATTR_MAP);
            stmtAttrMap.setInt(1, classId);
            stmtAttrMap.setInt(2, objectId);
            rsStr = stmtAttrString.executeQuery();
            rsMap = stmtAttrMap.executeQuery();
            int attrStringRows = 0;
            while (rsStr.next()) {
                boolean found = false;
                for (final ObjectAttribute oa : mo.getAttribs()) {
                    final MemberAttributeInfo mai = oa.getMai();
                    if (mai.isIndexed()) {
                        if (
                            oa.getValue().equals(rsStr.getString("string_val"))
                                    && (mai.getId() == rsStr.getInt("attr_id"))) {
                            found = true;
                        }
                    }
                }
                if (!found) {
                    return false;
                }
                ++attrStringRows;
            }
            int attrMapRows = 0;
            while (rsMap.next()) {
                final int classID = rsMap.getInt("class_id");
                final int objectID = rsMap.getInt("object_id");
                final int attrClassID = rsMap.getInt("attr_class_id");
                final int attrObjectID = rsMap.getInt("attr_object_id");
                if ((classID < attrClassID) && (objectID < attrObjectID)) {
                    ++attrMapRows;
                }
            }
            return (attrStringRows == stringRows) && (attrMapRows == objectRows);
        } catch (final SQLException e) {
            LOG.error("could not check for indexes", e);
        } finally {
            closeResultSets(rsStr, rsMap);
            closeStatements(stmtAttrString, stmtAttrMap);
        }
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rs  DOCUMENT ME!
     */
    private void closeResultSet(final ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (final SQLException e) {
                LOG.warn("could not close resultset: " + rs, e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  rss  DOCUMENT ME!
     */
    private void closeResultSets(final ResultSet... rss) {
        for (final ResultSet rs : rss) {
            closeResultSet(rs);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  s  DOCUMENT ME!
     */
    private void closeStatement(final Statement s) {
        if (s != null) {
            try {
                s.close();
            } catch (final SQLException e) {
                LOG.warn("could not close statement: " + s, e);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  ss  DOCUMENT ME!
     */
    private void closeStatements(final Statement... ss) {
        for (final Statement s : ss) {
            closeStatement(s);
        }
    }
}
