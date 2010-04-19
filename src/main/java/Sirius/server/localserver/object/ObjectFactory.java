/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Factory.java
 *
 * Created on 26. November 2003, 16:09
 */
package Sirius.server.localserver.object;

import Sirius.server.sql.*;
import Sirius.server.localserver._class.*;

import java.util.*;

import java.sql.*;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.StringPatternFormater;
import Sirius.server.newuser.*;
import Sirius.server.newuser.permission.*;

import de.cismet.cismap.commons.jtsgeometryfactories.*;

import de.cismet.tools.CurrentStackTrace;

import java.io.Serializable;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ObjectFactory {

    //~ Instance fields --------------------------------------------------------

    protected ClassCache classCache; // reference to this ls data base connections
    protected DBConnectionPool conPool;
    protected DatabaseMetaData dbMeta = null;
    protected HashSet primaryKeys;

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass()); // reference to this ls classes

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Factory.
     *
     * @param  conPool     DOCUMENT ME!
     * @param  classCache  DOCUMENT ME!
     */
    public ObjectFactory(DBConnectionPool conPool, ClassCache classCache) {
        this.classCache = classCache;
        this.conPool = conPool;
        try {
            this.dbMeta = conPool.getConnection().getConnection().getMetaData();
            this.primaryKeys = new HashSet(50, 20);
            initPrimaryKeys();
        } catch (Exception e) {
            logger.error("failed to retrieve db meta data", e);   // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   objectId  DOCUMENT ME!
     * @param   classId   DOCUMENT ME!
     * @param   ug        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getObject(int objectId, int classId, UserGroup ug) throws Exception {
        Sirius.server.localserver.object.Object o = getObject(objectId, classId);
        if (o != null) {
            setAttributePermissions(o, ug);
        }
        return o;
    }
    /**
     * ---!!!
     *
     * @param   classID                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classID,
            User user,
            String[] representationFields,
            String representationPattern) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classID);
        final String findAllStmnt = createFindAllQueryForClassID(c, representationFields);
        return getLightweightMetaObjectsByQuery(
                c,
                user,
                findAllStmnt.toString(),
                representationFields,
                new StringPatternFormater(representationPattern, representationFields));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classID               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(
            int classID,
            User user,
            String[] representationFields) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classID);
        final String findAllStmnt = createFindAllQueryForClassID(c, representationFields);
        return getLightweightMetaObjectsByQuery(c, user, findAllStmnt.toString(), representationFields, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c                     DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String createFindAllQueryForClassID(
            Sirius.server.localserver._class.Class c,
            String[] representationFields) {
        final String primaryKeyField = c.getPrimaryKey();
        final ClassAttribute sortingColumnAttribute = c.getClassAttribute("sortingColumn");   // NOI18N
        final StringBuilder findAllStmnt = new StringBuilder("select " + primaryKeyField);   // NOI18N
        if (representationFields.length > 0) {
            findAllStmnt.append(",");   // NOI18N
        }
        String field;
        for (int i = 0; i < representationFields.length; ++i) {
            field = representationFields[i];
            findAllStmnt.append(field);
            findAllStmnt.append(",");   // NOI18N
        }
        findAllStmnt.deleteCharAt(findAllStmnt.length() - 1);
        findAllStmnt.append(" from " + c.getTableName());   // NOI18N
        if (sortingColumnAttribute != null) {
            findAllStmnt.append(" order by ").append(sortingColumnAttribute.getValue());   // NOI18N
        }
        return findAllStmnt.toString();
    }
    /**
     * ---!!!
     *
     * @param   classId                DOCUMENT ME!
     * @param   user                   DOCUMENT ME!
     * @param   query                  DOCUMENT ME!
     * @param   representationFields   DOCUMENT ME!
     * @param   representationPattern  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields,
            String representationPattern) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classId);
        return getLightweightMetaObjectsByQuery(
                c,
                user,
                query,
                representationFields,
                new StringPatternFormater(representationPattern, representationFields));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId               DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            int classId,
            User user,
            String query,
            String[] representationFields) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classId);
        return getLightweightMetaObjectsByQuery(c, user, query, representationFields, null);
    }
    /**
     * ---!!!
     *
     * @param   c                     DOCUMENT ME!
     * @param   user                  DOCUMENT ME!
     * @param   query                 DOCUMENT ME!
     * @param   representationFields  DOCUMENT ME!
     * @param   formater              DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private LightweightMetaObject[] getLightweightMetaObjectsByQuery(
            final Sirius.server.localserver._class.Class c,
            User user,
            String query,
            String[] representationFields,
            AbstractAttributeRepresentationFormater formater) throws Exception {
        final String primaryKeyField = c.getPrimaryKey();
        final ResultSet rs = conPool.getConnection().getConnection().createStatement().executeQuery(query);
        final Set<LightweightMetaObject> lwMoSet = new LinkedHashSet<LightweightMetaObject>();
        while (rs.next()) {
            final Map<String, java.lang.Object> attributeMap = new HashMap<String, java.lang.Object>();
            // primary key must be returned by the query!
            final int oID = rs.getInt(primaryKeyField);
            attributeMap.put(primaryKeyField, oID);
            final java.lang.Object[] repObjs = new java.lang.Object[representationFields.length];
            for (int i = 0; i < repObjs.length; ++i) {
                final String fld = representationFields[i];
                final java.lang.Object retAttrVal = checkSerializabilityAndMakeSerializable(rs.getObject(fld));
                attributeMap.put(fld.toLowerCase(), retAttrVal);
                repObjs[i] = retAttrVal;
            }
            lwMoSet.add(new LightweightMetaObject(c.getID(), oID, user, attributeMap, formater));
        }
        return lwMoSet.toArray(new LightweightMetaObject[0]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private java.lang.Object checkSerializabilityAndMakeSerializable(java.lang.Object o) {
        if ((o == null) || (o instanceof Serializable)) {
            return o;
        } else {
            return o.toString();
        }
    }
    /**
     * ////////////////////////////////////////////////////////////////////////////////
     *
     * @param   objectId  DOCUMENT ME!
     * @param   classId   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getObject(int objectId, int classId) throws Exception {
        if (logger != null) {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Sirius.server.localserver.object.Object getObject(objectId=" + objectId + ",classId=" + classId   // NOI18N
                    + ")",   // NOI18N
                    new CurrentStackTrace());
            }
        }
        Sirius.server.localserver._class.Class c = classCache.getClass(classId);

        if (c == null) {
            return null;
                // logger.debug("Klasse f\u00FCr getObject"+c);
                // logger.debug("Objectid f\u00FCr getObject"+objectId);
                // object id as singular parameter for the getInstanceStmnt of a class
        }
        java.lang.Object[] param = new java.lang.Object[1];
        param[0] = new Integer(objectId);

        String getObjectStmnt = QueryParametrizer.parametrize(c.getGetInstanceStmnt(), param);

        Connection con = conPool.getConnection().getConnection();

        // update meta data
        this.dbMeta = con.getMetaData();

        Statement stmnt = con.createStatement();
        if (logger.isDebugEnabled()) {
            logger.debug("getObjectStatement ::" + getObjectStmnt);   // NOI18N
        }

        ResultSet rs = stmnt.executeQuery(getObjectStmnt);

        if (rs.next()) {
            return createObject(objectId, rs, c);
        } else {
            logger.error("<LS> ERROR no match for " + getObjectStmnt);   // NOI18N
            return null;
        }
    }
    // creates an DefaultObject from  apositioned Resultset

    /**
     * DOCUMENT ME!
     *
     * @param   objectId  DOCUMENT ME!
     * @param   rs        DOCUMENT ME!
     * @param   c         DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    Sirius.server.localserver.object.Object createObject(
            int objectId,
            ResultSet rs,
            Sirius.server.localserver._class.Class c) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("create Object entered for result" + rs + "object_id:: " + objectId + " class " + c.getID());   // NOI18N
            // construct object rump attributes have to be added yet
        }

        Sirius.server.localserver.object.Object result = new Sirius.server.localserver.object.DefaultObject(
                objectId,
                c.getID() /*,c.getToStringConverter()*/);

        // collection containing information about each attribute
        Collection fields = c.getMemberAttributeInfos().values();

        // iterator zum spaltenweise auslesen der Attribute
        Iterator iter = fields.iterator();

        // fieldname of the attribute to be added
        String fieldName = null;

        // actual value of the attribute to be added
        java.lang.Object attrValue = null;

        // for all attributes of this object
        while (iter.hasNext()) {
            // retrieve attribute description
            MemberAttributeInfo mai = (MemberAttributeInfo)iter.next();

            // retrive name of the column of this attribute
            fieldName = mai.getFieldName();

            // logger.debug("versuche attr "+fieldName+"hinzuzuf\u00FCgen");

            if (!mai.isExtensionAttribute()) {
                if (!(mai.isForeignKey())) // simple attribute can be directly retrieved from the resultset
                {
                    // SQLObject fetched as is; from the field with fieldname
                    // logger.debug("simple attribute");
                    // !!!!! umstellung auf PostgisGeometry
// if(mai.getTypeId()==236|| mai.getTypeId()==268) // Polygon
// {
// attrValue=rs.getString(fieldName);
//
////                    if(logger.isDebugEnabled())
////                    {
////                        java.lang.Class cc = null;
////                        java.lang.DefaultObject o =rs.getObject(fieldName);
////
////                        if(o!=null)
////                            cc = o.getClass();
////
////                       // logger.debug("GEOTYPE :: "+cc);
////                    }
//
//                }
//                else
                    attrValue = rs.getObject(fieldName);

                    // if(attrValue!=null)logger.debug("Klasse des Attributs "+attrValue.getClass());

                    try {
                        if (attrValue != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "Class of attribute " + mai.getName() + " (within conversion request)"   // NOI18N
                                    + attrValue.getClass());
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Class of attribute " + mai.getName() + " = null");   // NOI18N
                            }
                        }
                        if (attrValue instanceof org.postgis.PGgeometry) // TODO assignable from machen
                        // attrValue = de.cismet.tools.postgis.FeatureConverter.convert( (
                        // (org.postgis.PGgeometry)attrValue).getGeometry());
                        {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "Converting in JTS: " + mai.getName() + " (" + attrValue.getClass() + ")  = "   // NOI18N
                                    + attrValue);
                            }
                            attrValue = PostGisGeometryFactory.createJtsGeometry(
                                    ((org.postgis.PGgeometry)attrValue).getGeometry());
                        }
                        if (attrValue != null) {
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "Class of attribute " + mai.getName() + " (within conversion request)"   // NOI18N
                                    + attrValue.getClass());
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Class of attribute " + mai.getName() + " = null");   // NOI18N
                            }
                        }
                    } catch (Exception ex) {
                        logger.error(
                            "Error while converting to serialisable GeoObject. Setting attr to NULL, value was:"   // NOI18N
                            + attrValue,
                            ex);
                        attrValue = null;
                    }
                } else                 // isForeignKey therfore retrieve DefaultObject (recursion)
                {
                    if (mai.isArray()) // isForeignKey && isArray
                    {
                        // create Array of Objects
                        // logger.debug("isArray");

                        String referenceKey = rs.getString(fieldName);

                        if (referenceKey != null) {
                            attrValue = getMetaObjectArray(referenceKey, mai, objectId);
                        } else {
                            attrValue = null;
                        }
                    } else // isForeignkey DefaultObject can be retrieved as usual
                    {
                        // retrieve foreign key

                        if (rs.getObject(fieldName) == null) // wenn null dann unterbrechen der rekursion
                        {
                            attrValue = null;
                            if (logger.isDebugEnabled()) {
                                logger.debug(
                                    "getObject for " + fieldName + "produced null, setting attrValue to null");   // NOI18N
                            }
                        } else {
                            int o_id = rs.getInt(fieldName);
                            // logger.debug("attribute is object");
                            try {
                                attrValue = getObject(o_id, mai.getForeignKeyClassId());
                            } catch (Exception e) {
                                logger.error("getObject recursion interrupted for oid" + o_id + "  MAI " + mai, e);   // NOI18N
                                attrValue = null;
                            }
                        }
                    }
                }
            } else {
                attrValue = null;
            }

            ObjectAttribute oAttr = new ObjectAttribute(mai, objectId, attrValue, c.getAttributePolicy());
            oAttr.setVisible(mai.isVisible());
            oAttr.setSubstitute(mai.isSubstitute());
            oAttr.setReferencesObject(mai.isForeignKey());
            oAttr.setOptional(mai.isOptional());

            oAttr.setParentObject(result); // Achtung die Adresse des Objektes ist nicht die Adresse des
                                           // tats�chlichen MetaObjects. Dieses wird neu erzeugt. da aber die
                                           // gleichen objectattributes benutzt werden funktioniert ein zugriff �ber
                                           // parent auf diese oa's trotzdem

            if (attrValue instanceof Sirius.server.localserver.object.Object) {
                ((Sirius.server.localserver.object.Object)attrValue).setReferencingObjectAttribute(oAttr);
            }

            // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
            oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName());   // NOI18N

            if (!mai.isExtensionAttribute()) {
                // spaltenindex f\u00FCr sql metadaten abfragen
                int colNo = rs.findColumn(fieldName);

                // java type retrieved by getObject
                String javaType = rs.getMetaData().getColumnClassName(colNo);
                oAttr.setJavaType(javaType);
            } else {
                oAttr.setJavaType(java.lang.Object.class.getCanonicalName());
            }

            try {
                String table = c.getTableName();

                String pk = (fieldName + "@" + table).toLowerCase();   // NOI18N

                if (primaryKeys.contains(pk)) {
                    oAttr.setIsPrimaryKey(true);
                }
            } catch (Exception e) {
                logger.error("could not set primary key property", e);   // NOI18N
            }

            // logger.debug("add attr "+oAttr + " to DefaultObject "+result);

            result.addAttribute(oAttr);
        }

        return result;
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
     * @throws  Exception  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object getMetaObjectArray(
            String referenceKey,
            MemberAttributeInfo mai,
            int array_predicate) throws Exception {
        // construct artificial metaobject

        Sirius.server.localserver._class.Class c = classCache.getClass(mai.getForeignKeyClassId());

        Sirius.server.localserver.object.Object result = new Sirius.server.localserver.object.DefaultObject(
                array_predicate,
                c.getID());
        result.setDummy(true);

        String getObjectStmnt = "Select * from " + c.getTableName() + " where " + mai.getArrayKeyFieldName() + " = "   // NOI18N
            + referenceKey;

        Connection con = conPool.getConnection().getConnection();

        Statement stmnt = con.createStatement();
        if (logger.isDebugEnabled()) {
            logger.debug(getObjectStmnt);
        }

        ResultSet rs = stmnt.executeQuery(getObjectStmnt);

        // artificial id
        int i = 0;
        while (rs.next()) {
            int o_id = rs.getInt(c.getPrimaryKey());

            Sirius.server.localserver.object.Object element = createObject(o_id, rs, c);

            if (element != null) {
                ObjectAttribute oa = new ObjectAttribute(
                        mai.getId() + "." + i++,   // NOI18N
                        mai,
                        o_id,
                        element,
                        c.getAttributePolicy());
                oa.setOptional(mai.isOptional());
                oa.setVisible(mai.isVisible());
                element.setReferencingObjectAttribute(oa);
                oa.setParentObject(result);
                // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
                oa.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName());   // NOI18N
                result.addAttribute(oa);
            } else {
                logger.error(
                    new ObjectAttribute(mai.getId() + "." + i++, mai, o_id, element, c.getAttributePolicy())   // NOI18N
                    + " ommited as element was null");   // NOI18N
            }
        }

        return result;
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
    public Sirius.server.localserver.object.Object getInstance(int classId) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("getInstance(" + classId + ") called", new CurrentStackTrace());   // NOI18N
        }
        Sirius.server.localserver._class.Class c = classCache.getClass(classId);

        Sirius.server.localserver.object.Object o = new Sirius.server.localserver.object.DefaultObject(-1, classId);

        // nur ein Versuch:-)
        Iterator iter = Collections.synchronizedCollection(c.getMemberAttributeInfos().values()).iterator();

        while (iter.hasNext()) {
            MemberAttributeInfo mai = (MemberAttributeInfo)iter.next();

            ObjectAttribute oAttr;

            if (!mai.isForeignKey()) {
                oAttr = new ObjectAttribute(mai, -1, null, c.getAttributePolicy());
            } else if (!mai.isArray()) {
                oAttr = new ObjectAttribute(mai, -1, getInstance(mai.getForeignKeyClassId()), c.getAttributePolicy());
            } else // isArray
            {
                // construct artificial metaobject

                // classId des zwischenobjektes (join tabelle) zuweisen
                int jtClassId = mai.getForeignKeyClassId();

                // Klasse der referenztabellen besorgen
                Sirius.server.localserver._class.Class cl = classCache.getClass(jtClassId);

                // dummy erszeugen
                Sirius.server.localserver.object.Object result = new Sirius.server.localserver.object.DefaultObject(
                        -1,
                        cl.getID());

                // der dummy bekommt jetzt genau ein Attribut vom Typ der Klasse der Referenztabelle, als Muster

                // zwischenobjekt als arrayelement anlegen

                result.addAttribute(new ObjectAttribute(mai, -1, getInstance(jtClassId), cl.getAttributePolicy()));

                result.setDummy(true);

                // Objektattribut (array dummy) setzten
                oAttr = new ObjectAttribute(mai, -1, result, cl.getAttributePolicy());
                if (logger.isDebugEnabled()) {
                    logger.debug("array oattr :" + oAttr.getName() + " class" + oAttr.getClassKey());   // NOI18N
                }
            }

            // not covered by the constructor
            oAttr.setVisible(mai.isVisible());
            oAttr.setSubstitute(mai.isSubstitute());
            oAttr.setReferencesObject(mai.isForeignKey());

            oAttr.setIsPrimaryKey(mai.getFieldName().equalsIgnoreCase(c.getPrimaryKey()));

            oAttr.setOptional(mai.isOptional());

            try {
                String table = c.getTableName();

                String pk = (mai.getFieldName() + "@" + table).toLowerCase();   // NOI18N

                if (primaryKeys.contains(pk)) {
                    oAttr.setIsPrimaryKey(true); // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt
                                                 // wird
                }
                oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName());   // NOI18N
            } catch (Exception e) {
                logger.error("could not set primary key property", e);   // NOI18N
            }

            o.addAttribute(oAttr);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("result of getInstance()" + new DefaultMetaObject(o, "LOCAL"));   // NOI18N
        }
        return o;
    }

    /**
     * DOCUMENT ME!
     */
    private void initPrimaryKeys() {
        try {
            String[] tableType = { "TABLE" };   // NOI18N
            ResultSet rs = dbMeta.getTables(null, null, null, tableType);

            Vector tableNames = new Vector(20, 20);

            // get all tablenames
            while (rs.next()) {
                String[] tablePath = new String[3];

                // catalog
                tablePath[0] = rs.getString(1);
                // schema
                tablePath[1] = rs.getString(2);
                // tableName
                tablePath[2] = rs.getString(3);

                tableNames.add(tablePath);
            }

            for (int i = 0; i < tableNames.size(); i++) {
                String[] tabl = (String[])tableNames.get(i);
                ResultSet pks = dbMeta.getPrimaryKeys(tabl[0], tabl[1], tabl[2]);

                // columnname@tablename
                while (pks.next()) {
                    String pk = (pks.getString(4) + "@" + pks.getString(3)).toLowerCase();   // NOI18N
                    primaryKeys.add(pk);
                    // logger.debug("pk added :: "+pk);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o   DOCUMENT ME!
     * @param   ug  DOCUMENT ME!
     *
     * @throws  java.sql.SQLException  DOCUMENT ME!
     */
    protected void setAttributePermissions(Sirius.server.localserver.object.Object o, UserGroup ug)
        throws java.sql.SQLException {
        try {
            // check kann es Probleme bei nicht lokalen ugs geben?
            String attribPerm =
                "select p.id as pid,p.key as key, u.ug_id as ug_id, u.attr_id as attr_id from cs_ug_attr_perm as u, cs_permission as p  where attr_id in (select id  from cs_attr where class_id ="   // NOI18N
                + o.getClassID() + ") and u.permission = p.id and ug_id = " + ug.getId();   // NOI18N

            Connection con = conPool.getConnection().getConnection();

            Statement stmnt = con.createStatement();

            ResultSet rs = stmnt.executeQuery(attribPerm);

            HashMap attrs = o.getAttributes();

            while (rs.next()) {
                String attrkey = rs.getString("attr_id");

                if (attrkey != null) {
                    attrkey = attrkey.trim();
                } else {
                    logger.error(
                        "attrKey in cs_ug_attr_perm does not reference a legal attribute. It is therefor skipped ::"   // NOI18N
                        + attrkey);
                    continue;
                }

                int permId = rs.getInt("pid");   // NOI18N

                String permKey = rs.getString("key");   // NOI18N

                if (permKey != null) {
                    permKey = permKey.trim();
                } else {
                    logger.error(
                        "permKey in cs_ug_attr_perm does not reference a legal attribute. It is therefor skipped :"   // NOI18N
                        + permKey);
                    continue;
                }

//                String policy = rs.getString("policy");
//
//                if (policy != null) {
//                    policy = policy.trim();
//                } else {
//                    logger.error(" keine policy   Attribut wird daher \u00FCbersprungen ::" + policy);
//                    continue;
//                }

                // konstruktion des Keys abhaengig von attr.getKey :-(
                Attribute a = (Attribute)attrs.get(attrkey + "@" + o.getClassID());   // NOI18N

                if (a == null) {
                    logger.error("No attribute found for attrKey. It is therefore skipped ::" + attrkey);   // NOI18N
                    continue;
                } else {
                    PermissionHolder p = a.getPermissions();

                    if (p == null) {
                        logger.error(
                            "Attribute does not contain Permissionholder. PermissionHolder is therefore initialised for attribut::"   // NOI18N
                            + a);

                        a.setPermissions(
                            new PermissionHolder(classCache.getClass(o.getClassID()).getAttributePolicy()));
                    }

                    p.addPermission(ug, new Permission(permId, permKey));
                }
            }
        } catch (java.sql.SQLException e) {
            logger.error("Error in setAttributePermissons", e);   // NOI18N
            throw e;
        } catch (Exception e) {
            logger.error("Error in setAttributePermissons", e);   // NOI18N
        }
    }
}
