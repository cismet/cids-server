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
 *
 * @author  schlob
 */
public class ObjectFactory {

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());    // reference to this ls classes
    protected ClassCache classCache;    //reference to this ls data base connections
    protected DBConnectionPool conPool;
    protected DatabaseMetaData dbMeta = null;
    protected HashSet primaryKeys;

    /** Creates a new instance of Factory */
    public ObjectFactory(DBConnectionPool conPool, ClassCache classCache) {

        this.classCache = classCache;
        this.conPool = conPool;
        try {
            this.dbMeta = conPool.getConnection().getConnection().getMetaData();
            this.primaryKeys = new HashSet(50, 20);
            initPrimaryKeys();
        } catch (Exception e) {
            logger.error("failed to retrieve db meta data", e);
        }


    }

    public Sirius.server.localserver.object.Object getObject(int objectId, int classId, UserGroup ug) throws Exception {
        Sirius.server.localserver.object.Object o = getObject(objectId, classId);
        if (o != null) {
            setAttributePermissions(o, ug);
        }
        return o;

    }

    //---!!!
    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classID, User user, String[] representationFields, String representationPattern) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classID);
        final String findAllStmnt = createFindAllQueryForClassID(c, representationFields);
        return getLightweightMetaObjectsByQuery(c, user, findAllStmnt.toString(), representationFields, new StringPatternFormater(representationPattern, representationFields));
    }

    public LightweightMetaObject[] getAllLightweightMetaObjectsForClass(int classID, User user, String[] representationFields) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classID);
        final String findAllStmnt = createFindAllQueryForClassID(c, representationFields);
        return getLightweightMetaObjectsByQuery(c, user, findAllStmnt.toString(), representationFields, null);
    }

    private final String createFindAllQueryForClassID(Sirius.server.localserver._class.Class c, String[] representationFields) {
        final String primaryKeyField = c.getPrimaryKey();
        final ClassAttribute sortingColumnAttribute = c.getClassAttribute("sortingColumn");
        final StringBuilder findAllStmnt = new StringBuilder("select " + primaryKeyField);
        if (representationFields.length > 0) {
            findAllStmnt.append(",");
        }
        String field;
        for (int i = 0; i < representationFields.length; ++i) {
            field = representationFields[i];
            findAllStmnt.append(field);
            findAllStmnt.append(",");
        }
        findAllStmnt.deleteCharAt(findAllStmnt.length() - 1);
        findAllStmnt.append(" from " + c.getTableName());
        if (sortingColumnAttribute != null) {
            findAllStmnt.append(" order by ").append(sortingColumnAttribute.getValue());
        }
        return findAllStmnt.toString();
    }

    //---!!!
    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields, String representationPattern) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classId);
        return getLightweightMetaObjectsByQuery(c, user, query, representationFields, new StringPatternFormater(representationPattern, representationFields));
    }

    public LightweightMetaObject[] getLightweightMetaObjectsByQuery(int classId, User user, String query, String[] representationFields) throws Exception {
        final Sirius.server.localserver._class.Class c = classCache.getClass(classId);
        return getLightweightMetaObjectsByQuery(c, user, query, representationFields, null);
    }

    //---!!!
    private final LightweightMetaObject[] getLightweightMetaObjectsByQuery(final Sirius.server.localserver._class.Class c, User user, String query, String[] representationFields, AbstractAttributeRepresentationFormater formater) throws Exception {
        final String primaryKeyField = c.getPrimaryKey();
        final ResultSet rs = conPool.getConnection().getConnection().createStatement().executeQuery(query);
        final Set<LightweightMetaObject> lwMoSet = new LinkedHashSet<LightweightMetaObject>();
        while (rs.next()) {
            final Map<String, java.lang.Object> attributeMap = new HashMap<String, java.lang.Object>();
            //primary key must be returned by the query!
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

    private final java.lang.Object checkSerializabilityAndMakeSerializable(java.lang.Object o) {
        if (o == null || o instanceof Serializable) {
            return o;
        } else {
            return o.toString();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    public Sirius.server.localserver.object.Object getObject(int objectId, int classId) throws Exception {
        if (logger != null) {
            logger.debug("Sirius.server.localserver.object.Object getObject(objectId=" + objectId + ",classId=" + classId + ")", new CurrentStackTrace());
        }
        Sirius.server.localserver._class.Class c = classCache.getClass(classId);

        if (c == null) {
            return null;
            //logger.debug("Klasse f\u00FCr getObject"+c);
            //logger.debug("Objectid f\u00FCr getObject"+objectId);
            //object id as singular parameter for the getInstanceStmnt of a class
        }
        java.lang.Object[] param = new java.lang.Object[1];
        param[0] = new Integer(objectId);

        String getObjectStmnt = QueryParametrizer.parametrize(c.getGetInstanceStmnt(), param);

        Connection con = conPool.getConnection().getConnection();

        // update meta data
        this.dbMeta = con.getMetaData();

        Statement stmnt = con.createStatement();

        logger.debug("getObjectStatement ::" + getObjectStmnt);

        ResultSet rs = stmnt.executeQuery(getObjectStmnt);

        if (rs.next()) {
            return createObject(objectId, rs, c);
        } else {
            logger.error("<LS> ERROR kein match f\u00FCr " + getObjectStmnt);
            return null;
        }


    }
    // creates an DefaultObject from  apositioned Resultset

    Sirius.server.localserver.object.Object createObject(int objectId, ResultSet rs, Sirius.server.localserver._class.Class c) throws Exception {
        logger.debug("create Object entered for result" + rs + "object_id:: " + objectId + " class " + c.getID());
        //construct object rump attributes have to be added yet

        Sirius.server.localserver.object.Object result = new Sirius.server.localserver.object.DefaultObject(objectId, c.getID()/*,c.getToStringConverter()*/);





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
            MemberAttributeInfo mai = (MemberAttributeInfo) iter.next();

            // retrive name of the column of this attribute
            fieldName = mai.getFieldName();



            // logger.debug("versuche attr "+fieldName+"hinzuzuf\u00FCgen");

            if (!mai.isExtensionAttribute()) {

                if (!(mai.isForeignKey())) // simple attribute can be directly retrieved from the resultset
                {
                    // SQLObject fetched as is; from the field with fieldname
                    //logger.debug("simple attribute");
                    // !!!!! umstellung auf PostgisGeometry
//                if(mai.getTypeId()==236|| mai.getTypeId()==268) // Polygon
//                {
//                    attrValue=rs.getString(fieldName);
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

                    //  if(attrValue!=null)logger.debug("Klasse des Attributs "+attrValue.getClass());

                    try {
                        if (attrValue != null) {
                            logger.debug("Klasse des Attributs " + mai.getName() + " (innerhalb der Konvertierungsabfrage)" + attrValue.getClass());
                        } else {
                            logger.debug("Klasse des Attributs " + mai.getName() + " = null");
                        }
                        if (attrValue instanceof org.postgis.PGgeometry) //TODO assignable from machen
                        // attrValue = de.cismet.tools.postgis.FeatureConverter.convert( ( (org.postgis.PGgeometry)attrValue).getGeometry());
                        {
                            logger.debug("Konvertiere in JTS: " + mai.getName() + " (" + attrValue.getClass() + ")  = " + attrValue);
                            attrValue = PostGisGeometryFactory.createJtsGeometry(((org.postgis.PGgeometry) attrValue).getGeometry());
                        }
                        if (attrValue != null) {
                            logger.debug("Klasse des Attributs " + mai.getName() + " (innerhalb der Konvertierungsabfrage)" + attrValue.getClass());
                        } else {
                            logger.debug("Klasse des Attributs " + mai.getName() + " = null");
                        }
                    } catch (Exception ex) {
                        logger.error("Fehler beim konvertieren in ein serialisierbares Geoobject setzt attr auf NULL wert war:" + attrValue, ex);
                        attrValue = null;
                    }
                } else// isForeignKey therfore retrieve DefaultObject (recursion)
                {


                    if (mai.isArray()) // isForeignKey && isArray
                    {
                        // create Array of Objects
                        //logger.debug("isArray");

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
                            logger.debug("getObject f\u00FCr " + fieldName + "ergab null, setzte attrValue auf null");

                        } else {

                            int o_id = rs.getInt(fieldName);
                            //logger.debug("attribute is object");
                            try {
                                attrValue = getObject(o_id, mai.getForeignKeyClassId());
                            } catch (Exception e) {
                                logger.error("getObject Rekursion unterbrochen fuer oid" + o_id + "  MAI " + mai, e);
                                attrValue = null;
                            }

                        }


                    }

                }
            }
            else {
                attrValue=null;
            }


            ObjectAttribute oAttr = new ObjectAttribute(mai, objectId, attrValue, c.getAttributePolicy());
            oAttr.setVisible(mai.isVisible());
            oAttr.setSubstitute(mai.isSubstitute());
            oAttr.setReferencesObject(mai.isForeignKey());
            oAttr.setOptional(mai.isOptional());

            oAttr.setParentObject(result); //Achtung die Adresse des Objektes ist nicht die Adresse des tats�chlichen MetaObjects. Dieses wird neu erzeugt. da aber die gleichen objectattributes benutzt werden funktioniert ein zugriff �ber parent auf diese oa's trotzdem

            if (attrValue instanceof Sirius.server.localserver.object.Object) {
                ((Sirius.server.localserver.object.Object) attrValue).setReferencingObjectAttribute(oAttr);
            }


            // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
            oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName());

            if (!mai.isExtensionAttribute()) {
                // spaltenindex f\u00FCr sql metadaten abfragen
                int colNo = rs.findColumn(fieldName);

                // java type retrieved by getObject
                String javaType = rs.getMetaData().getColumnClassName(colNo);
                oAttr.setJavaType(javaType);
            }
            else {
                oAttr.setJavaType(java.lang.Object.class.getCanonicalName());
            }

            try {


                String table = c.getTableName();

                String pk = (fieldName + "@" + table).toLowerCase();

                if (primaryKeys.contains(pk)) {
                    oAttr.setIsPrimaryKey(true);
                }
            } catch (Exception e) {
                logger.error("could not set primary key property", e);
            }




            //  logger.debug("add attr "+oAttr + " to DefaultObject "+result);

            result.addAttribute(oAttr);

        }

        return result;

    }

    public Sirius.server.localserver.object.Object getMetaObjectArray(String referenceKey, MemberAttributeInfo mai, int array_predicate) throws Exception {

        // construct artificial metaobject

        Sirius.server.localserver._class.Class c = classCache.getClass(mai.getForeignKeyClassId());

        Sirius.server.localserver.object.Object result = new Sirius.server.localserver.object.DefaultObject(array_predicate, c.getID());
        result.setDummy(true);

        String getObjectStmnt = "Select * from " + c.getTableName() + " where " + mai.getArrayKeyFieldName() + " = " + referenceKey;

        Connection con = conPool.getConnection().getConnection();

        Statement stmnt = con.createStatement();

        logger.debug(getObjectStmnt);

        ResultSet rs = stmnt.executeQuery(getObjectStmnt);

        //artificial id
        int i = 0;
        while (rs.next()) {
            int o_id = rs.getInt(c.getPrimaryKey());

            Sirius.server.localserver.object.Object element = createObject(o_id, rs, c);

            if (element != null) {
                ObjectAttribute oa = new ObjectAttribute(mai.getId() + "." + i++, mai, o_id, element, c.getAttributePolicy());
                oa.setOptional(mai.isOptional());
                oa.setVisible(mai.isVisible());
                element.setReferencingObjectAttribute(oa);
                oa.setParentObject(result);
                // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
                oa.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName());
                result.addAttribute(oa);

            } else {
                logger.error(new ObjectAttribute(mai.getId() + "." + i++, mai, o_id, element, c.getAttributePolicy()) + " ommited as element was null");
            }
        }

        return result;
    }

    public Sirius.server.localserver.object.Object getInstance(int classId) throws Exception {
        logger.debug("getInstance(" + classId + ") aufgerufen", new CurrentStackTrace());
        Sirius.server.localserver._class.Class c = classCache.getClass(classId);

        Sirius.server.localserver.object.Object o = new Sirius.server.localserver.object.DefaultObject(-1, classId);

        // nur ein Versuch:-)
        Iterator iter = Collections.synchronizedCollection(c.getMemberAttributeInfos().values()).iterator();


        while (iter.hasNext()) {

            MemberAttributeInfo mai = (MemberAttributeInfo) iter.next();

            ObjectAttribute oAttr;

            if (!mai.isForeignKey()) {
                oAttr = new ObjectAttribute(mai, -1, null, c.getAttributePolicy());
            } else if (!mai.isArray()) {
                oAttr = new ObjectAttribute(mai, -1, getInstance(mai.getForeignKeyClassId()), c.getAttributePolicy());
            } else // isArray
            {

                // construct artificial metaobject

                //classId des zwischenobjektes (join tabelle) zuweisen
                int jtClassId = mai.getForeignKeyClassId();

                // Klasse der referenztabellen besorgen
                Sirius.server.localserver._class.Class cl = classCache.getClass(jtClassId);

                // dummy erszeugen
                Sirius.server.localserver.object.Object result = new Sirius.server.localserver.object.DefaultObject(-1, cl.getID());

                // der dummy bekommt jetzt genau ein Attribut vom Typ der Klasse der Referenztabelle, als Muster

                // zwischenobjekt als arrayelement anlegen

                result.addAttribute(new ObjectAttribute(mai, -1, getInstance(jtClassId), cl.getAttributePolicy()));

                result.setDummy(true);

                // Objektattribut (array dummy) setzten
                oAttr = new ObjectAttribute(mai, -1, result, cl.getAttributePolicy());
                logger.debug("array oattr :" + oAttr.getName() + " class" + oAttr.getClassKey());

            }


            // not covered by the constructor
            oAttr.setVisible(mai.isVisible());
            oAttr.setSubstitute(mai.isSubstitute());
            oAttr.setReferencesObject(mai.isForeignKey());

            oAttr.setIsPrimaryKey(mai.getFieldName().equalsIgnoreCase(c.getPrimaryKey()));

            oAttr.setOptional(mai.isOptional());




            try {


                String table = c.getTableName();

                String pk = (mai.getFieldName() + "@" + table).toLowerCase();

                if (primaryKeys.contains(pk)) {
                    oAttr.setIsPrimaryKey(true);                // bei gelegenheit raus da es im Konstruktor von MetaObject gesetzt wird
                }
                oAttr.setClassKey(mai.getForeignKeyClassId() + "@" + classCache.getProperties().getServerName());


            } catch (Exception e) {
                logger.error("could not set primary key property", e);
            }

            o.addAttribute(oAttr);


        }

        logger.debug("ergebniss von getINstance()" + new DefaultMetaObject(o, "LOCAL"));
        return o;




    }

    private void initPrimaryKeys() {
        try {
            String[] tableType = {"TABLE"};
            ResultSet rs = dbMeta.getTables(null, null, null, tableType);

            Vector tableNames = new Vector(20, 20);

            // get all tablenames
            while (rs.next()) {
                String[] tablePath = new String[3];

                //catalog
                tablePath[0] = rs.getString(1);
                //schema
                tablePath[1] = rs.getString(2);
                //tableName
                tablePath[2] = rs.getString(3);


                tableNames.add(tablePath);
            }

            for (int i = 0; i < tableNames.size(); i++) {
                String[] tabl = (String[]) tableNames.get(i);
                ResultSet pks = dbMeta.getPrimaryKeys(tabl[0], tabl[1], tabl[2]);

                // columnname@tablename
                while (pks.next()) {
                    String pk = (pks.getString(4) + "@" + pks.getString(3)).toLowerCase();
                    primaryKeys.add(pk);
                    //logger.debug("pk added :: "+pk);
                }



            }



        } catch (Exception e) {
            logger.error(e);
        }
    }

    protected void setAttributePermissions(Sirius.server.localserver.object.Object o, UserGroup ug) throws java.sql.SQLException {
        try {

            // check kann es Probleme bei nicht lokalen ugs geben?
            String attribPerm = "select p.id as pid,p.key as key, u.ug_id as ug_id, u.attr_id as attr_id from cs_ug_attr_perm as u, cs_permission as p  where attr_id in (select id  from cs_attr where class_id =" + o.getClassID() + ") and u.permission = p.id and ug_id = " + ug.getId();

            Connection con = conPool.getConnection().getConnection();

            Statement stmnt = con.createStatement();

            ResultSet rs = stmnt.executeQuery(attribPerm);

            HashMap attrs = o.getAttributes();

            while (rs.next()) {
                String attrkey = rs.getString("attr_id");

                if (attrkey != null) {
                    attrkey = attrkey.trim();
                } else {
                    logger.error("attrKey in cs_ug_attr_perm referenziert kein g\u00FCltiges attribut und wird daher \u00FCbersprungen ::" + attrkey);
                    continue;
                }

                int permId = rs.getInt("pid");


                String permKey = rs.getString("key");

                if (permKey != null) {
                    permKey = permKey.trim();
                } else {
                    logger.error("permKey in cs_ug_attr_perm refernziert kein g\u00FCltiges attribut. Attribut wird daher \u00FCbersprungen :" + permKey);
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
                Attribute a = (Attribute) attrs.get(attrkey + "@" + o.getClassID());

                if (a == null) {
                    logger.error("kein Attribut zu attrKey gefunden und wird daher \u00FCbersprungen ::" + attrkey);
                    continue;
                } else {
                    PermissionHolder p = a.getPermissions();

                    if (p == null) {
                        logger.error("Attribut enth\u00E4lt keinen Permissionholder. PermissionHolder wird daher initialisiert fuer attribut::" + a);

                        a.setPermissions(new PermissionHolder(classCache.getClass(o.getClassID()).getAttributePolicy()));
                    }

                    p.addPermission(ug, new Permission(permId, permKey));
                }




            }





        } catch (java.sql.SQLException e) {
            logger.error("Fehler in setAttributePermissons", e);
            throw e;
        } catch (Exception e) {
            logger.error("Fehler in setAttributePermissons", e);
        }
    }
}

