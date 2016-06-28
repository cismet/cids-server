/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.AbstractShutdownable;
import Sirius.server.ServerExitError;
import Sirius.server.Shutdown;
import Sirius.server.localserver._class.ClassCache;
import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.Permission;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.sql.DBConnection;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.QueryParametrizer;
import Sirius.server.sql.SQLTools;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public final class ObjectFactory extends Shutdown {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(ObjectFactory.class);

    //~ Instance fields --------------------------------------------------------

    private ClassCache classCache;
    private DBConnectionPool conPool;
    private DatabaseMetaData dbMeta = null;
    private HashSet primaryKeys;
    private final String dialect;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Factory.
     *
     * @param  conPool     DOCUMENT ME!
     * @param  classCache  DOCUMENT ME!
     */
    public ObjectFactory(final DBConnectionPool conPool, final ClassCache classCache) {
        this.classCache = classCache;
        this.conPool = conPool;
        this.dialect = Lookup.getDefault().lookup(DialectProvider.class).getDialect();

        try {
            this.dbMeta = conPool.getConnection().getMetaData();
            this.primaryKeys = new HashSet(50, 20);
            initPrimaryKeys();
        } catch (final Exception e) {
            LOG.error("failed to retrieve db meta data", e); // NOI18N
        }

        addShutdown(new AbstractShutdownable() {

                @Override
                protected void internalShutdown() throws ServerExitError {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("shutting down ObjectFactory"); // NOI18N
                    }

                    primaryKeys.clear();
                }
            });
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   objectId  DOCUMENT ME!
     * @param   classId   DOCUMENT ME!
     * @param   usr       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getObject(final int objectId, final int classId, final User usr)
            throws SQLException {
        return getObject(objectId, classId, usr, new HashMap<String, Sirius.server.localserver.object.Object>(), 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId            DOCUMENT ME!
     * @param   classId             DOCUMENT ME!
     * @param   usr                 DOCUMENT ME!
     * @param   ohm                 DOCUMENT ME!
     * @param   byPassCacheCounter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object getObject(final int objectId,
            final int classId,
            final User usr,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm,
            final int byPassCacheCounter) throws SQLException {
        final Sirius.server.localserver.object.Object o = getObject(objectId, classId, ohm, byPassCacheCounter);
        if (o != null) {
            setAttributePermissions(o, usr);
        }
        return o;
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////
     *
     * @param   objectId  DOCUMENT ME!
     * @param   classId   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getObject(final int objectId, final int classId)
            throws SQLException {
        return getObject(objectId, classId, new HashMap<String, Sirius.server.localserver.object.Object>(), 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId            DOCUMENT ME!
     * @param   classId             DOCUMENT ME!
     * @param   ohm                 DOCUMENT ME!
     * @param   byPassCacheCounter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object getObject(final int objectId,
            final int classId,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm,
            final int byPassCacheCounter) throws SQLException {
        return getObject(objectId, classId, false, ohm, byPassCacheCounter);
    }

    /**
     * ////////////////////////////////////////////////////////////////////////////////
     *
     * @param   objectId                 DOCUMENT ME!
     * @param   classId                  DOCUMENT ME!
     * @param   allowLightweightObjects  DOCUMENT ME!
     * @param   ohm                      DOCUMENT ME!
     * @param   byPassCacheCounter       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object getObject(final int objectId,
            final int classId,
            final boolean allowLightweightObjects,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm,
            final int byPassCacheCounter) throws SQLException {
        if (LOG != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(
                    "Sirius.server.localserver.object.Object getObject(objectId=" // NOI18N
                            + objectId
                            + ",classId="                                         // NOI18N
                            + classId
                            + ")",                                                // NOI18N
                    new CurrentStackTrace());
            }
        }
        final String ohmKey = objectId + "@" + classId;
        if ((ohm == null) || !ohm.containsKey(ohmKey)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("creating cache for " + ohmKey);
            }
            final Sirius.server.localserver._class.Class c = classCache.getClass(classId);

            if (c == null) {
                return null;
            }

            final java.lang.Object[] param = new java.lang.Object[1];
            param[0] = new Integer(objectId);

            final String getObjectStmnt = QueryParametrizer.parametrize(c.getGetInstanceStmnt(), param);

            final Connection con = conPool.getConnection();

            Statement stmnt = null;
            ResultSet rs = null;
            try {
                // update meta data
                this.dbMeta = con.getMetaData();

                stmnt = con.createStatement();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getObjectStatement ::" + getObjectStmnt); // NOI18N
                }

                rs = stmnt.executeQuery(getObjectStmnt);

                if (rs.next()) {
                    if ((ohm == null)) {
                        final int newBapassCacheCounter;
                        if (byPassCacheCounter > 0) {
                            newBapassCacheCounter = byPassCacheCounter - 1;
                        } else {
                            newBapassCacheCounter = byPassCacheCounter;
                        }
                        return createObject(objectId, rs, c, allowLightweightObjects, ohm, newBapassCacheCounter);
                    } else {
                        ohm.put(
                            ohmKey,
                            createObject(objectId, rs, c, allowLightweightObjects, ohm, byPassCacheCounter));
                    }
                } else {
                    LOG.error("<LS> ERROR kein match f\u00FCr " + getObjectStmnt); // NOI18N
                    if (ohm == null) {
                        return null;
                    } else {
                        ohm.put(ohmKey, null);
                    }
                }
            } finally {
                DBConnection.closeResultSets(rs);
                DBConnection.closeStatements(stmnt);
            }
        }
        if (ohm != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("use cache for " + ohmKey);
            }
            return ohm.get(ohmKey);
        } else {
            // normally not reachable
            LOG.warn("this block should not be reached !");
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId                 DOCUMENT ME!
     * @param   classId                  DOCUMENT ME!
     * @param   allowLightweightObjects  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getObject(final int objectId,
            final int classId,
            final boolean allowLightweightObjects) throws SQLException {
        return getObject(objectId, classId, allowLightweightObjects, null, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId                 DOCUMENT ME!
     * @param   rs                       DOCUMENT ME!
     * @param   c                        DOCUMENT ME!
     * @param   allowLightweightObjects  DOCUMENT ME!
     * @param   ohm                      DOCUMENT ME!
     * @param   byPassCacheCounter       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object createObject(final int objectId,
            final ResultSet rs,
            final Sirius.server.localserver._class.Class c,
            final boolean allowLightweightObjects,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm,
            final int byPassCacheCounter) throws SQLException {
        return createObject(objectId, rs, c, null, allowLightweightObjects, ohm, byPassCacheCounter);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId                 DOCUMENT ME!
     * @param   rs                       DOCUMENT ME!
     * @param   c                        DOCUMENT ME!
     * @param   allowLightweightObjects  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    Sirius.server.localserver.object.Object createObject(final int objectId,
            final ResultSet rs,
            final Sirius.server.localserver._class.Class c,
            final boolean allowLightweightObjects) throws SQLException {
        return createObject(objectId, rs, c, allowLightweightObjects, null, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId                DOCUMENT ME!
     * @param   rs                      DOCUMENT ME!
     * @param   c                       DOCUMENT ME!
     * @param   backlinkClassToExclude  DOCUMENT ME!
     * @param   byPassCacheCounter      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    Sirius.server.localserver.object.Object createObject(final int objectId,
            final ResultSet rs,
            final Sirius.server.localserver._class.Class c,
            final Sirius.server.localserver._class.Class backlinkClassToExclude,
            final int byPassCacheCounter) throws SQLException {
        return createObject(objectId, rs, c, backlinkClassToExclude, null, byPassCacheCounter);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId                DOCUMENT ME!
     * @param   rs                      DOCUMENT ME!
     * @param   c                       DOCUMENT ME!
     * @param   backlinkClassToExclude  DOCUMENT ME!
     * @param   ohm                     DOCUMENT ME!
     * @param   byPassCacheCounter      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object createObject(final int objectId,
            final ResultSet rs,
            final Sirius.server.localserver._class.Class c,
            final Sirius.server.localserver._class.Class backlinkClassToExclude,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm,
            final int byPassCacheCounter) throws SQLException {
        return createObject(objectId, rs, c, backlinkClassToExclude, false, ohm, byPassCacheCounter);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId                 DOCUMENT ME!
     * @param   rs                       DOCUMENT ME!
     * @param   c                        DOCUMENT ME!
     * @param   backlinkClassToExclude   DOCUMENT ME!
     * @param   allowLightweightObjects  DOCUMENT ME!
     * @param   ohm                      DOCUMENT ME!
     * @param   byPassCacheCounter       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object createObject(final int objectId,
            final ResultSet rs,
            final Sirius.server.localserver._class.Class c,
            final Sirius.server.localserver._class.Class backlinkClassToExclude,
            final boolean allowLightweightObjects,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm,
            final int byPassCacheCounter) throws SQLException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("create Object entered for result" + rs + "object_id:: " + objectId + " class " + c.getID()); // NOI18N
            // construct object rump attributes have to be added yet
        }

        final Sirius.server.localserver.object.Object result = new Sirius.server.localserver.object.DefaultObject(
                objectId,
                c.getID() /*
                           * ,c.getToStringConverter()
                           */);

        // collection containing information about each attribute
        final Collection fields = c.getMemberAttributeInfos().values();

        // iterator zum spaltenweise auslesen der Attribute
        final Iterator iter = fields.iterator();

        // fieldname of the attribute to be added
        String fieldName = null;

        // actual value of the attribute to be added
        java.lang.Object attrValue = null;

        // for all attributes of this object
        while (iter.hasNext()) {
            // retrieve attribute description
            final MemberAttributeInfo mai = (MemberAttributeInfo)iter.next();
            if (!((backlinkClassToExclude != null) && mai.isForeignKey()
                            && (mai.getForeignKeyClassId() == backlinkClassToExclude.getID()))) {
                // retrive name of the column of this attribute
                fieldName = mai.getFieldName();

                // LOG.debug("versuche attr "+fieldName+"hinzuzuf\u00FCgen");

                if (!mai.isVirtual()) {
                    if (!(mai.isForeignKey())) // simple attribute can be directly retrieved from the resultset
                    {
                        attrValue = rs.getObject(fieldName);

                        try {
                            if (attrValue != null) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                        "Class of attribute "
                                                + mai.getName()
                                                + " (within conversion request)" // NOI18N
                                                + attrValue.getClass());
                                }
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Class of attribute " + mai.getName() + " = null"); // NOI18N
                                }
                            }
                            if (attrValue instanceof Clob) {
                                // we convert the clob to a string, otherwise the value is not serialisable out of the
                                // box due to the direct connection to the database
                                // TODO: handle overflows, i.e. clob too big
                                final Clob clob = (Clob)attrValue;
                                if (clob.length() <= Integer.valueOf(Integer.MAX_VALUE).longValue()) {
                                    attrValue = clob.getSubString(1, Long.valueOf(clob.length()).intValue());
                                } else {
                                    throw new IllegalStateException(
                                        "cannot handle clobs larger than Integer.MAX_VALUE: [class="
                                                + c.getName()
                                                + "|field="
                                                + fieldName
                                                + "]");
                                }
                            }
                            if (SQLTools.getGeometryFactory(dialect).isGeometryObject(attrValue)) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                        "Converting in JTS: "
                                                + mai.getName()
                                                + " ("
                                                + attrValue.getClass()
                                                + ")  = " // NOI18N
                                                + attrValue);
                                }
                                attrValue = SQLTools.getGeometryFactory(dialect).createGeometry(attrValue);
                            }
                            if (attrValue != null) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                        "Class of attribute "
                                                + mai.getName()
                                                + " (within conversion request)" // NOI18N
                                                + attrValue.getClass());
                                }
                            } else {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("Class of attribute " + mai.getName() + " = null"); // NOI18N
                                }
                            }
                        } catch (Exception ex) {
                            LOG.error(
                                "Error while converting to serialisable GeoObject. Setting attr to NULL, value was:" // NOI18N
                                        + attrValue,
                                ex);
                            attrValue = null;
                        }
                    } else                                // isForeignKey therfore retrieve DefaultObject (recursion)
                    {
                        if (mai.isArray())                // isForeignKey && isArray
                        {
                            final String referenceKey = rs.getString(fieldName);

                            if (referenceKey != null) {
                                attrValue = getMetaObjectArray(referenceKey, mai, objectId, ohm);
                            } else {
                                attrValue = null;
                            }
                        } else {
                            if ((backlinkClassToExclude != null)
                                        && (backlinkClassToExclude.getID() == mai.getForeignKeyClassId())) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug("skip " + mai.getFieldName()
                                                + " because it's the backlink of a one to many relationship");
                                }
                            } else {
                                // isForeignkey DefaultObject can be retrieved as usual
                                // retrieve foreign key

                                if (rs.getObject(fieldName) == null) // wenn null dann
                                // unterbrechen der rekursion
                                {
                                    attrValue = null;
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug(
                                            "getObject for "
                                                    + fieldName
                                                    + "produced null, setting attrValue to null"); // NOI18N
                                    }
                                } else {
                                    final int o_id = rs.getInt(fieldName);
                                    // LOG.debug("attribute is object");
                                    try {
                                        final Sirius.server.localserver._class.Class maiClass = classCache.getClass(
                                                mai.getForeignKeyClassId());
                                        final boolean cacheHintSet = maiClass.getClassAttribute("CACHEHINT") != null;
                                        if (allowLightweightObjects && cacheHintSet) {
                                            attrValue = new LightweightObject(o_id, mai.getForeignKeyClassId());
                                        } else {
                                            attrValue = getObject(
                                                    o_id,
                                                    mai.getForeignKeyClassId(),
                                                    true,
                                                    ohm,
                                                    byPassCacheCounter);
                                        }
                                    } catch (Exception e) {
                                        LOG.error("getObject recursion interrupted for oid" + o_id + "  MAI " + mai, e); // NOI18N
                                        attrValue = null;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (mai.isExtensionAttribute()) {
                        attrValue = null;
                    } else if (mai.getForeignKeyClassId() < 0) {
                        // 1:n Beziehung

                        attrValue = getMetaObjectArrayForOneToMany(fieldName, mai, objectId);
                    }
                }

                final ObjectAttribute oAttr = new ObjectAttribute(mai, objectId, attrValue, c.getAttributePolicy());
                oAttr.setVisible(mai.isVisible());
                oAttr.setSubstitute(mai.isSubstitute());
                oAttr.setReferencesObject(mai.isForeignKey());
                oAttr.setOptional(mai.isOptional());

                if (attrValue instanceof Sirius.server.localserver.object.Object) {
                    ((Sirius.server.localserver.object.Object)attrValue).setReferencingObjectAttribute(oAttr);
                }

                // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
                oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName()); // NOI18N

                if (!mai.isVirtual()) {
                    oAttr.setJavaType(mai.getJavaclassname());
                } else {
                    oAttr.setJavaType(java.lang.Object.class.getCanonicalName());
                }

                try {
                    final String table = c.getTableName();

                    final String pk = (fieldName + "@" + table).toLowerCase(); // NOI18N

                    if (primaryKeys.contains(pk)) {
                        oAttr.setIsPrimaryKey(true);
                    }
                } catch (Exception e) {
                    LOG.error("could not set primary key property", e); // NOI18N
                }

                // LOG.debug("add attr "+oAttr + " to DefaultObject "+result);

                result.addAttribute(oAttr);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectId                 DOCUMENT ME!
     * @param   rs                       DOCUMENT ME!
     * @param   c                        DOCUMENT ME!
     * @param   backlinkClassToExclude   DOCUMENT ME!
     * @param   allowLightweightObjects  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    Sirius.server.localserver.object.Object createObject(final int objectId,
            final ResultSet rs,
            final Sirius.server.localserver._class.Class c,
            final Sirius.server.localserver._class.Class backlinkClassToExclude,
            final boolean allowLightweightObjects) throws SQLException {
        return createObject(objectId, rs, c, backlinkClassToExclude, allowLightweightObjects, null, 0);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   referenceKey     DOCUMENT ME!
     * @param   mai              DOCUMENT ME!
     * @param   array_predicate  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getMetaObjectArray(final String referenceKey,
            final MemberAttributeInfo mai,
            final int array_predicate) throws SQLException {
        return getMetaObjectArray(referenceKey, mai, array_predicate, null);
    }

    /**
     * Creates a dummay array container object with dummy entries. See
     * https://github.com/cismet/developer-space/wiki/Array-Objekte-Struktur
     *
     * @param   referenceKey     DOCUMENT ME!
     * @param   mai              DOCUMENT ME!
     * @param   array_predicate  DOCUMENT ME!
     * @param   ohm              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object getMetaObjectArray(final String referenceKey,
            final MemberAttributeInfo mai,
            final int array_predicate,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm) throws SQLException {
        // construct artificial metaobject

        final Sirius.server.localserver._class.Class dummyArrayClass = classCache.getClass(mai.getForeignKeyClassId());

        final Sirius.server.localserver.object.Object dummyArrayContainerObject =
            new Sirius.server.localserver.object.DefaultObject(
                array_predicate,
                dummyArrayClass.getID());
        dummyArrayContainerObject.setDummy(true);

        // #169: order by array table id
        final String getObjectStmnt = SQLTools.getStatements(dialect)
                    .getObjectFactoryGetObjectStmt(
                        dummyArrayClass.getTableName(),
                        mai.getArrayKeyFieldName(),
                        referenceKey,
                        dummyArrayClass.getPrimaryKey());

        Statement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = conPool.getConnection().createStatement();
            if (LOG.isDebugEnabled()) {
                LOG.debug(getObjectStmnt);
            }

            rs = stmnt.executeQuery(getObjectStmnt);

            // artificial id
            int i = 0;
            while (rs.next()) {
                final int o_id = rs.getInt(dummyArrayClass.getPrimaryKey());

                // createObject adds the *real* array entry as object attribute to the intermediate object
                final Sirius.server.localserver.object.Object intermediateArrayElementObject = createObject(
                        o_id,
                        rs,
                        dummyArrayClass,
                        true,
                        null,
                        1);

                if (intermediateArrayElementObject != null) {
                    final ObjectAttribute oa = new ObjectAttribute(
                            mai.getId()
                                    + "." // NOI18N
                                    + i++,
                            mai,
                            o_id,
                            intermediateArrayElementObject,
                            dummyArrayClass.getAttributePolicy());
                    oa.setOptional(mai.isOptional());
                    oa.setVisible(mai.isVisible());
                    intermediateArrayElementObject.setReferencingObjectAttribute(oa);
                    // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
                    oa.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName()); // NOI18N
                    dummyArrayContainerObject.addAttribute(oa);
                } else {
                    // TODO: expensive and should probably only be a warning
                    LOG.error(new ObjectAttribute(
                                    mai.getId()
                                    + "."
                                    + i++,
                                    mai,
                                    o_id,
                                    intermediateArrayElementObject,
                                    dummyArrayClass.getAttributePolicy()) // NOI18N
                                + " ommited as element was null");        // NOI18N
                }
            }

            return dummyArrayContainerObject;
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmnt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   referenceKey     DOCUMENT ME!
     * @param   mai              DOCUMENT ME!
     * @param   array_predicate  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getMetaObjectArrayForOneToMany(final String referenceKey,
            final MemberAttributeInfo mai,
            final int array_predicate) throws SQLException {
        return getMetaObjectArrayForOneToMany(referenceKey, mai, array_predicate, null);
    }

    /**
     * Creates a 1-n dummy array object.
     *
     * @param   referenceKey     DOCUMENT ME!
     * @param   mai              DOCUMENT ME!
     * @param   array_predicate  DOCUMENT ME!
     * @param   ohm              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    private Sirius.server.localserver.object.Object getMetaObjectArrayForOneToMany(final String referenceKey,
            final MemberAttributeInfo mai,
            final int array_predicate,
            final HashMap<String, Sirius.server.localserver.object.Object> ohm) throws SQLException {
        // construct artificial metaobject
        // array_predicate is the parent object id

        final int masterClassId = mai.getClassId();

        final Sirius.server.localserver._class.Class masterClass = classCache.getClass(masterClassId);

        /**
         * Class of the actual array entries
         */
        final Sirius.server.localserver._class.Class detailClass = classCache.getClass(-1 * mai.getForeignKeyClassId());

        final Sirius.server.localserver.object.Object dummyArrayContainerObject =
            new Sirius.server.localserver.object.DefaultObject(
                array_predicate,
                detailClass.getID());

        final Collection detailfields = detailClass.getMemberAttributeInfos().values();
        final Iterator detailIter = detailfields.iterator();

        MemberAttributeInfo maiBacklink = null;
        while (detailIter.hasNext()) {
            final MemberAttributeInfo detailMai = (MemberAttributeInfo)detailIter.next();
            if (detailMai.isForeignKey() && !detailMai.isVirtual()
                        && (detailMai.getForeignKeyClassId() == masterClassId)) {
                maiBacklink = detailMai;
                break;
            }
        }
        if (maiBacklink == null) {
            return null;
        }

        dummyArrayContainerObject.setDummy(true);

        final String getObjectStmnt = SQLTools.getStatements(dialect)
                    .getObjectFactoryGetObjectStmt(
                        detailClass.getTableName(),
                        maiBacklink.getFieldName(),
                        String.valueOf(array_predicate),
                        detailClass.getPrimaryKey());

        Statement stmnt = null;
        ResultSet rs = null;
        try {
            stmnt = conPool.getConnection().createStatement();
            if (LOG.isDebugEnabled()) {
                LOG.debug(getObjectStmnt);
            }

            rs = stmnt.executeQuery(getObjectStmnt);

            // artificial id
            int i = 0;
            while (rs.next()) {
                final int o_id = rs.getInt(detailClass.getPrimaryKey());

                /**
                 * The *real* array element object
                 */
                final Sirius.server.localserver.object.Object arrayElementObject = createObject(
                        o_id,
                        rs,
                        detailClass,
                        masterClass,
                        ohm,
                        1);

                if (arrayElementObject != null) {
                    final ObjectAttribute oa = new ObjectAttribute(
                            mai.getId()
                                    + "." // NOI18N
                                    + i++,
                            mai,
                            o_id,
                            arrayElementObject,
                            detailClass.getAttributePolicy());
                    oa.setOptional(mai.isOptional());
                    oa.setVisible(mai.isVisible());
                    arrayElementObject.setReferencingObjectAttribute(oa);
                    // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
                    oa.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName()); // NOI18N
                    dummyArrayContainerObject.addAttribute(oa);
                } else {
                    // TODO: expensive and should probably only be a warning
                    LOG.error(new ObjectAttribute(
                                    mai.getId()
                                    + "."
                                    + i++,
                                    mai,
                                    o_id,
                                    arrayElementObject,
                                    detailClass.getAttributePolicy()) // NOI18N
                                + " ommited as element was null");    // NOI18N
                }
            }

            return dummyArrayContainerObject;
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmnt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getInstance(final int classId) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getInstance(" + classId + ") called"); // NOI18N
        }
        final Sirius.server.localserver._class.Class c = classCache.getClass(classId);

        final Sirius.server.localserver.object.Object o = new Sirius.server.localserver.object.DefaultObject(
                -1,
                classId);

        if ((c.getMemberAttributeInfos() != null) && !c.getMemberAttributeInfos().isEmpty()) {
            final Iterator iter = Collections.synchronizedCollection(c.getMemberAttributeInfos().values()).iterator();

            while (iter.hasNext()) {
                final MemberAttributeInfo mai = (MemberAttributeInfo)iter.next();

                ObjectAttribute oAttr;

                if (!mai.isForeignKey()) {
                    oAttr = new ObjectAttribute(mai, -1, null, c.getAttributePolicy());
                } else if (!mai.isArray()) {
                    oAttr = new ObjectAttribute(
                            mai,
                            -1,
                            getInstance(mai.getForeignKeyClassId()),
                            c.getAttributePolicy());
                } else // isArray
                {
                    // construct artificial metaobject

                    // classId des zwischenobjektes (join tabelle) zuweisen
                    final int jtClassId = mai.getForeignKeyClassId();

                    // Klasse der referenztabellen besorgen
                    final Sirius.server.localserver._class.Class cl = classCache.getClass(jtClassId);

                    // dummy erszeugen
                    final Sirius.server.localserver.object.Object result =
                        new Sirius.server.localserver.object.DefaultObject(-1,
                            cl.getID());

                    // der dummy bekommt jetzt genau ein Attribut vom Typ der Klasse der Referenztabelle, als Muster

                    // zwischenobjekt als arrayelement anlegen

                    result.addAttribute(new ObjectAttribute(mai, -1, getInstance(jtClassId), cl.getAttributePolicy()));

                    result.setDummy(true);

                    // Objektattribut (array dummy) setzten
                    oAttr = new ObjectAttribute(mai, -1, result, cl.getAttributePolicy());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("array oattr :" + oAttr.getName() + " class" + oAttr.getClassKey()); // NOI18N
                    }
                }

                // not covered by the constructor
                oAttr.setVisible(mai.isVisible());
                oAttr.setSubstitute(mai.isSubstitute());
                oAttr.setReferencesObject(mai.isForeignKey());

                oAttr.setIsPrimaryKey(mai.getFieldName().equalsIgnoreCase(c.getPrimaryKey()));

                oAttr.setOptional(mai.isOptional());

                try {
                    final String table = c.getTableName();

                    final String pk = (mai.getFieldName() + "@" + table).toLowerCase(); // NOI18N

                    if (primaryKeys.contains(pk)) {
                        oAttr.setIsPrimaryKey(true); // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt
                        // wird
                    }
                    oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName()); // NOI18N
                } catch (Exception e) {
                    LOG.error("could not set primary key property", e);                                               // NOI18N
                }

                o.addAttribute(oAttr);
            }
        } else {
            LOG.warn("no MemberAttributeInfo found in class '" + c.getTableName() + "' ("
                        + c.getKey() + "): cannot add attrributes to Object!");
        }

        return o;
    }

    /**
     * DOCUMENT ME!
     */
    private void initPrimaryKeys() {
        try {
            final String[] tableType = { "TABLE" }; // NOI18N
            final ResultSet rs = dbMeta.getTables(null, null, null, tableType);

            final List tableNames = new ArrayList(20);

            // get all tablenames
            while (rs.next()) {
                final String[] tablePath = new String[3];

                // catalog
                tablePath[0] = rs.getString(1);
                // schema
                tablePath[1] = rs.getString(2);
                // tableName
                tablePath[2] = rs.getString(3);

                tableNames.add(tablePath);
            }

            for (int i = 0; i < tableNames.size(); i++) {
                final String[] tabl = (String[])tableNames.get(i);
                final ResultSet pks = dbMeta.getPrimaryKeys(tabl[0], tabl[1], tabl[2]);

                // columnname@tablename
                while (pks.next()) {
                    String schema = pks.getString(2);

                    if ((schema == null) || schema.equalsIgnoreCase("public")) {
                        schema = "";
                    } else {
                        schema += ".";
                    }

                    final String pk = (pks.getString(4) + "@" + schema + pks.getString(3)).toLowerCase(); // NOI18N
                    primaryKeys.add(pk);
                    // LOG.debug("pk added :: "+pk);
                }
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String implodedUserGroupIds(final User user) {
        final UserGroup userGroup = user.getUserGroup();
        final Collection<Integer> userGroupIds = new ArrayList<Integer>();
        if (userGroup != null) {
            LOG.info("get top nodes for UserGroup:" + userGroup.getName() + "@" + user.getDomain());               // NOI18N
            userGroupIds.add(userGroup.getId());
        } else {
            LOG.info("get top nodes for UserGroups:");                                                             // NOI18N
            for (final UserGroup potentialUserGroup : user.getPotentialUserGroups()) {
                LOG.info("                            :" + potentialUserGroup.getName() + "@" + user.getDomain()); // NOI18N
                userGroupIds.add(potentialUserGroup.getId());
            }
        }

        final String implodedUserGroupIds;
        if (userGroupIds.isEmpty()) {
            implodedUserGroupIds = "";
        } else {
            final StringBuilder sb = new StringBuilder();
            for (final int userGroupId : userGroupIds) {
                if (sb.length() > 0) { // is the first item ?
                    sb.append(", ");
                }
                sb.append(Integer.toString(userGroupId));
            }
            implodedUserGroupIds = sb.toString();
        }

        return implodedUserGroupIds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o     DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    protected void setAttributePermissions(final Sirius.server.localserver.object.Object o, final User user)
            throws SQLException {
        final String implodedUserGroupIds = implodedUserGroupIds(user);
        Statement stmnt = null;
        ResultSet rs = null;
        try {
            final UserGroup userGroup = user.getUserGroup();
            // check kann es Probleme bei nicht lokalen ugs geben?
            final String attribPerm = SQLTools.getStatements(dialect)
                        .getObjectFactoryAttrPermStmt(o.getClassID(), implodedUserGroupIds);

            stmnt = conPool.getConnection().createStatement();

            rs = stmnt.executeQuery(attribPerm);

            final HashMap attrs = o.getAttributes();

            while (rs.next()) {
                String attrkey = rs.getString("attr_id"); // NOI18N

                if (attrkey != null) {
                    attrkey = attrkey.trim();
                } else {
                    LOG.error(
                        "attrKey in cs_ug_attr_perm does not reference a legal attribute. It is therefor skipped ::" // NOI18N
                                + attrkey);
                    continue;
                }

                final int permId = rs.getInt("pid"); // NOI18N

                String permKey = rs.getString("key"); // NOI18N

                if (permKey != null) {
                    permKey = permKey.trim();
                } else {
                    LOG.error(
                        "permKey in cs_ug_attr_perm does not reference a legal attribute. It is therefor skipped :" // NOI18N
                                + permKey);
                    continue;
                }

                // konstruktion des Keys abhaengig von attr.getKey :-(
                final Attribute a = (Attribute)attrs.get(attrkey + "@" + o.getClassID()); // NOI18N

                if (a == null) {
                    LOG.error("No attribute found for attrKey. It is therefore skipped ::" + attrkey); // NOI18N
                    continue;
                } else {
                    final PermissionHolder p = a.getPermissions();

                    if (p == null) {
                        LOG.error(
                            "Attribute does not contain Permissionholder. PermissionHolder is therefore initialised for attribut::" // NOI18N
                                    + a);

                        a.setPermissions(
                            new PermissionHolder(classCache.getClass(o.getClassID()).getAttributePolicy()));
                    }

                    p.addPermission(userGroup, new Permission(permId, permKey));
                }
            }
        } catch (final SQLException e) {
            LOG.error("cannot create attribute permissions", e); // NOI18N
            throw e;
        } finally {
            DBConnection.closeResultSets(rs);
            DBConnection.closeStatements(stmnt);
        }
    }
}
