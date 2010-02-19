/*
 * PersistenceManager.java, encoding: UTF-8
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
 * Created on 3. Juni 2006, 12:48
 *
 */
package Sirius.server.localserver.object;


import Sirius.server.localserver.DBServer;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;
import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;
import de.cismet.tools.CurrentStackTrace;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author schlob
 */
public class PersistenceManager {

    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    public static final String DEL_ATTR_STRING =
            "DELETE FROM cs_attr_string "
            + "WHERE class_id = ? AND object_id = ?";
    public static final String DEL_ATTR_MAPPING =
            "DELETE FROM cs_all_attr_mapping "
            + "WHERE class_id = ? AND object_id = ?";

    public static final String INS_ATTR_STRING =
            "INSERT INTO cs_attr_string "
            + "(class_id, object_id, attr_id, string_val) VALUES (?, ?, ?, ?)";
    public static final String INS_ATTR_MAPPING =
            "INSERT INTO cs_all_attr_mapping "
            + "(class_id, object_id, attr_class_id, attr_object_id) VALUES "
            + "(?, ?, ?, ?)";

    public static final String UP_ATTR_STRING =
            "UPDATE cs_attr_string "
            + "SET string_val = ? "
            + "WHERE class_id = ? AND object_id = ? AND attr_id = ?";
    public static final String UP_ATTR_MAPPING =
            "UPDATE cs_all_attr_mapping "
            + "SET attr_object_id = ? "
            + "WHERE class_id = ? AND object_id = ? AND attr_class_id = ?";


    /** Creates a new instance of PersistenceManager */
    protected DBServer dbServer;
    protected TransactionHelper transactionHelper;
    protected PersistenceHelper persistenceHelper;

    public PersistenceManager(DBServer dbServer) throws Throwable {
        this.dbServer = dbServer;

        transactionHelper = new TransactionHelper(dbServer.getActiveDBConnection(), dbServer.getSystemProperties());

        persistenceHelper = new PersistenceHelper(dbServer);

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /** loescht mo und alle Objekte die mo als Attribute hat */
    public int deleteMetaObject(User user, MetaObject mo) throws Throwable {
        logger.debug("deleteMetaObject entered " + mo + "status :" + mo.getStatus() + " der klasse:" + mo.getClassID() + " isDummy(ArrayContainer) :" + mo.isDummy());

        if (dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(user.getUserGroup())) {


            // start transaction
            transactionHelper.beginWork();





            // intitialize sql-string
            String deleteMetaObjectSQLStatement = "delete from ";

            try {

                // Mo was created artificially (array holder) so there is no object to delete
                // directly proceed to subObjects

                if (mo == null) {
                    logger.error("cannot delete MetaObject == null");
                    return 0;
                }

                if (mo.isDummy()) {
                    return deleteSubObjects(user, mo);
                }


                ObjectAttribute[] allAttributes = mo.getAttribs();
                boolean deeper = false;
                for (ObjectAttribute oa : allAttributes) {
                    if (oa.isChanged()) {
                        deeper = true;
                        break;
                    }
                }

                if (deeper) {
                    updateMetaObject(user, mo);
                }

                // intitialize UserGroup
                UserGroup ug = null;

                // retrieve userGroup is user is not null
                if (user != null) {
                    ug = user.getUserGroup();
                }

                // retrieve the metaObject's class
                Sirius.server.localserver._class.Class c = dbServer.getClass(ug, mo.getClassID());

                // get Tablename from class
                String tableName = c.getTableName();

                // get primary Key from class
                String pk = c.getPrimaryKey();


                // add tablename and whereclause to the delete statement
                deleteMetaObjectSQLStatement += tableName + " where " + pk + " = " + mo.getPrimaryKey().getValue();

                logger.info("sql: " + deleteMetaObjectSQLStatement);

                //transactionHelper.getConnection().prepareStatement(deleteMetaObjectSQLStatement).executeUpdate();
                // execute deletion and retrieve number of affected objects
                int result = transactionHelper.getConnection().createStatement().executeUpdate(deleteMetaObjectSQLStatement);

                // now delete all subObjects
                result += deleteSubObjects(user, mo);

                /*
                 * since the meta-jdbc driver is obsolete the index must be
                 * refreshed by the server explicitly
                 */
                deleteIndex(mo);

                transactionHelper.commit(); // stimmt das ??


                return result;
            } catch (Exception e) {
                transactionHelper.rollback();
                logger.error("Fehler in deleteMetaObject daher rollback on::" + deleteMetaObjectSQLStatement, e);
                throw e;
            }
        } else {
            logger.debug("User " + user + "is not allowed to delete MetaObject " + mo.getID() + "." + mo.getClassKey(), new CurrentStackTrace());
            return 0;
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    /** loescht alle Objekte die mo als Attribute hat */
    private int deleteSubObjects(User user, MetaObject mo) throws Throwable {

        logger.debug("deleteMetaObject dummy entered discard object insert elements" + mo);

        // initialize number of affected objects
        int count = 0;

        // retrieve number of array elements
        ObjectAttribute[] oas = mo.getAttribs();

        for (int i = 0; i < oas.length; i++) {
            // delete all referenced Object / array elements
            if (oas[i].referencesObject()) {
                MetaObject metaObject = (MetaObject) oas[i].getValue();

                logger.debug("try to delete :" + metaObject);

                if (metaObject != null && metaObject.getStatus() == MetaObject.TEMPLATE) {
                    count += deleteMetaObject(user, metaObject);
                }

            }
        }

        logger.debug("array elements deleted :: " + count);


        return count;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Aktualisiert rekursiv MetaObjekte im MetaSystem
     *
     * @return anzahl der aktualisierter Objekte.
     */
    public void updateMetaObject(User user, MetaObject mo) throws Throwable {
        logger.debug("updateMetaObject entered " + mo + "status :" + mo.getStatus() + " der klasse:" + mo.getClassID() + " isDummy(ArrayContainer) :" + mo.isDummy());
        if (dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(user.getUserGroup())) {




            // wenn Array
            if (mo.isDummy()) {
                updateArrayObjects(user, mo);
                return;
            }


            // variablen f\u00FCr sql statement
            String sql = "UPDATE ";
            String komma = "";



            // Klassenobjekt besorgen
            MetaClass metaClass = dbServer.getClass(mo.getClassID());

            // Tabellenamen der Klasse anf\u00FCgen + set klausel
            sql += metaClass.getTableName() + " SET ";

            // info objekte f\u00FCr attribute
            // HashMap maiMap = metaClass.getMemberAttributeInfos();

            // alle attribute des objekts besorgen
            ObjectAttribute[] mAttr = mo.getAttribs();




            MemberAttributeInfo mai;

            // z\u00E4hlt die zu updatenden Felder wenn 0 dann keine Aus\u00FChrung des stmnts
            int updateCounter = 0;

            //  iteriere \u00FCber alle attribute
            FORALLATTRIBUTES:
            for (int i = 0; i < mAttr.length; i++) {



                // wenn nicht ver\u00E4ndert gehe zum n\u00E4chsten attribut
                if (!mAttr[i].isChanged()) {
                    continue FORALLATTRIBUTES;
                }

                // besorge info objekt f\u00FCr dieses attribut
                //mai = (MemberAttributeInfo)maiMap.get(persistenceHelper.getKeyForMAI(mAttr[i]));
                mai = mAttr[i].getMai();

                if (mai == null) {
                    String message = "Info f\u00FCr Metaattribut " + mAttr[i].getName() + " wurde nicht gefunden.";
                    throw new Exception(message);
                }



                // feldname ist jetzt gesetzt jetzt value setzen

                java.lang.Object value = mAttr[i].getValue();

                String valueString = "";

                // value == null checken dann auf null setzen

                if (value == null) {
                    // delete MetaObject???
                    valueString = " NULL ";
                    logger.debug("valueSTring set to null as value of attribute was null");
                } else if (value instanceof MetaObject) {
                    MetaObject subObject = (MetaObject) value;

                    int status = subObject.getStatus();

                    // entscheide bei MO ob update/delete/insert
                    switch (status) {
                        case MetaObject.NEW:
                            // neuer schl\u00FCssel wird gesetzt
                            int key = insertMetaObject(user, subObject);
                            if (!subObject.isDummy()) {
                                valueString += key;
                            } else {
                                valueString += mo.getID();// setze value auf primarschluesselwert
                                insertMetaObjectArray(user, subObject);
                            }


                            break;

                        case MetaObject.TO_DELETE:
                            deleteMetaObject(user, subObject);
                            valueString = " NULL ";
                            break;

                        case MetaObject.NO_STATUS:
                        case MetaObject.MODIFIED:
                            updateMetaObject(user, subObject);

                            valueString += subObject.getID();
                            break;
                        //kommentar unten ungueltig:-)))
                        // schluessel bleibt wie er ist deshalb attribut ueberspringen d.h. kommt nicht ins updatestatement des uebergeordentetn objekts
                        // continue  FORALLATTRIBUTES;// gehe wieder zum Schleifenanfang



                        default:
                            logger.error("error update f\u00FCr attribut das auf subObjekt zeigt gerufen aber " + subObject + " hat ung\u00FCltigen status ::" + status);


                    }// end switch


                } else {
                    // einfaches nicht null attribut d.h. kein MetaObjekt wird referenziert
                    if (persistenceHelper.GEOMETRY.isAssignableFrom(value.getClass())) {
                        valueString += PostGisGeometryFactory.getPostGisCompliantDbString((Geometry) value);
                    } else {
                        valueString += value.toString();
                    }



                }

                // quotierung einf\u00FCgen wenn n\u00F6tig
                if (persistenceHelper.toBeQuoted(value)) {
                    valueString = "'" + valueString + "'";
                }

                // update feldname hinzuf\u00FCgen
                sql += komma + mai.getFieldName() + " = " + valueString;

                updateCounter++;


                // komma zwischen fieldname = value,* zum ersten mal im 2ten durchlauf gesetzt
                komma = ",";


            } // ender der for schleife \u00FCber alle attribute

            // nur wenn mind 1 attribut sqlm\u00E4ssig upgedatet werden muss ausf\u00FChren
            //z.B. reference_tabellen werden nicht upgedated wenn array_elemente ver\u00E4ndert werden obwohl
            // sie mit update gekennzeichnet sind



            if (updateCounter > 0) {

                transactionHelper.beginWork();

                // statemtent fertig jetzt noch where clause (id des Objekts) dazu
                sql += " WHERE " + metaClass.getPrimaryKey() + " = " + mo.getID();

                logger.info("sql " + sql);

                transactionHelper.getConnection().createStatement().executeUpdate(sql);

                /*
                 * since the meta-jdbc driver is obsolete the index must be
                 * refreshed by the server explicitly
                 */
                updateIndex(mo);

                transactionHelper.commit();
            }
        } else {
            logger.debug("User " + user + "is not allowed to update MetaObject " + mo.getID() + "." + mo.getClassKey(), new CurrentStackTrace());
        }


    }

    /** ruft update f\u00FCr alle arrayElemente auf   */
    public void updateArrayObjects(User user, MetaObject mo) throws Throwable {
        logger.debug("updateArrayObjects gerufen f\u00FCr " + mo);

        ObjectAttribute[] oas = mo.getAttribs();

        for (int i = 0; i < oas.length; i++) {
            if (oas[i].referencesObject()) {
                MetaObject metaObject = (MetaObject) oas[i].getValue();
                int stat = metaObject.getStatus();

                switch (stat) {
                    case MetaObject.NEW:

                        //da in update muss der arraykey nicht angefasst werden!
                        insertMetaObject(user, metaObject);

                        break;

                    case MetaObject.TO_DELETE:
                        deleteMetaObject(user, metaObject);
                        break;

                    case MetaObject.NO_STATUS:
                    case MetaObject.MODIFIED:
                        updateMetaObject(user, metaObject);
                        break;

                    default:
                        logger.error("error f\u00FCr array element " + metaObject + " hat ung\u00FCltigen status ::" + stat);


                }// end switch
            } else {
                logger.error("ArrayElement kein MetaObject und wird daher nicht eingef\u00FCgt");
            }
        }

        // schl\u00FCsselbeziehungen f\u00FCr arrays werden im client bereits gesetzt
        return;

    }

    void insertMetaObjectArray(User user, MetaObject dummy) throws Throwable {

//     if(mo.isDummy())
//        {
        //logger.debug("insertMO dummy entered discard object insert elements"+mo);


        ObjectAttribute[] oas = dummy.getAttribs();

        for (int i = 0; i < oas.length; i++) {
            logger.debug("insertMO arrayelement " + i);


            MetaObject arrayElement = (MetaObject) oas[i].getValue();

            int status = arrayElement.getStatus();


            // entscheide bei MO ob update/delete/insert


            switch (status) {
                case MetaObject.NEW:
                    // neuer schluessel wird gesetzt
                    insertMetaObject(user, arrayElement);

                    break; // war auskommentiert HELL

                case MetaObject.TO_DELETE:
                    deleteMetaObject(user, arrayElement);

                    break;

                case MetaObject.NO_STATUS:
                    break;
                case MetaObject.MODIFIED:
                    updateMetaObject(user, arrayElement);


            }// end switch


//            }


            // this causes no problem as it is never on the top level (-1 != object_id:-)
            // die notwendigen schl\u00FCsselbeziehungen werden im client gesetzt???

            return;

        }

    }

    public int insertMetaObject(User user, MetaObject mo) throws Throwable {

        logger.debug("insertMetaObject entered " + mo + "status :" + mo.getStatus() + " der klasse:" + mo.getClassID() + " isDummy(ArrayContainer) :" + mo.isDummy());

        // wenn array dummy schmeisse das array object weg und rufe insertMO rekursiv f\u00FCr alle attribute auf

        if (dbServer.getClassCache().getClass(mo.getClassID()).getPermissions().hasWritePermission(user.getUserGroup())) {


            // variablen aus denen das insert statement f\u00FCr das MetaObject zusammengebaut wird (Bausteine)
            String attrNameList = "", valueList = "", komma = "";

            // Klasse des einzuf\u00FCgenden Objektes
            MetaClass metaClass = dbServer.getClass(mo.getClassID());

            // intialisiere insert string
            String sql = "INSERT INTO " + metaClass.getTableName() + " (";

            // MAI der Attribute des MetaObjekts
            // HashMap map = metaClass.getMemberAttributeInfos();

            /////////////////// begin setze Schl\u00FCsseslattribut

            // initialisiere Schluessel mit neuer ID (Schl\u00FCssel des MetaObjekts)
            int rootPk = persistenceHelper.getNextID(metaClass.getTableName(), metaClass.getPrimaryKey());

            // setzt den schluessel im Attribut das den primary key halten soll bis jetzt -1 oder null
            // weiter unten wird kann so das pk attribut wie jedes andere behandelt werden


            ObjectAttribute[] allAttribs = mo.getAttribs();
            for (ObjectAttribute maybePK : allAttribs) {
                if (maybePK.isPrimaryKey()) {
                    maybePK.setValue(rootPk);
                }
            }

            //objectId muss manuell gesetzt werden: tsssss
            mo.setID(rootPk);


            //initialisiert alle array attribute mit dem wert des primary keys
            mo.setArrayKey2PrimaryKey();

            //////////////// ende setze Schl\u00FCssel Attribut des MetaObjekts

            // deklariere AttributInfovariable
            // MemberAttributeInfo mai;

            ObjectAttribute[] mAttr = mo.getAttribs();

            // iteriere \u00FCber alle attribute um die Bausteine des insert stmnts zu setzen
            for (int i = 0; i < mAttr.length; i++) {
                // Wert des Attributs
                java.lang.Object value = mAttr[i].getValue();
                logger.debug("mAttr[" + i + "].getName() von " + mo.getClassKey() + ": " + mAttr[i].getName());

                // besorge info Objekt f\u00FCr diese Attribut
                //   mai = (MemberAttributeInfo)map.get(persistenceHelper.getKeyForMAI(mAttr[i]));
                MemberAttributeInfo mai = mAttr[i].getMai();

                // wenn kein Infoobjekt vorhanden insert nicht m\u00F6glich
                if (mai == null) {
                    String message = ("Info f\u00FCr Metaattribut " + mAttr[i].getName() + " wurde nicht gefunden.");
                    throw new Exception(message);

                }

                // f\u00FCge feldinfo f\u00FCr diese attribut dem insert stmnt hinzu
                attrNameList += komma + mai.getFieldName();

                // initialisieren defaultValue
                String defaultVal = persistenceHelper.getDefaultValue(mai, value);

                if (!mAttr[i].referencesObject()) // zeigt auf kein Objekt also auch eigener schl\u00FCssel
                {
                    // hier werden alle einfache felder abgehandelt
                    // (keine Objektreferenzen)
                    if (value == null) {
                        // use defaultvalue
                        valueList += komma + defaultVal;
                    } else {
                        try {
                            // contains fieldvalue and komma
                            String val = "";

                            if (!persistenceHelper.toBeQuoted(mai, value)) {
                                // no quotation
                                val += komma + value.toString();
                            } else {
                                // if not isGeometry simply add quotes
                                if (!persistenceHelper.GEOMETRY.isAssignableFrom(value.getClass())) {
                                    val += komma + ("'" + value.toString() + "'");
                                } else {
                                    val += komma + ("'" + PostGisGeometryFactory.getPostGisCompliantDbString((Geometry) value) + "'");
                                }
                            }


                            valueList += val;

                        } catch (java.util.MissingResourceException e) {
                            logger.error("Exception when trying to retrieve list of quoted types insert unsafe therefore rollback", e);
                            transactionHelper.rollback();
                        }
                    }



                } else if (!mAttr[i].isPrimaryKey()) // hier zeigt auf MetaObjekt
                {

                    // null was dann???




                    // besorge Schl\u00FCssel
                    Sirius.server.localserver._class.Class c = dbServer.getClass(mai.getForeignKeyClassId());

                    String attrTab = c.getTableName();

                    String pk = c.getPrimaryKey();


                    MetaObject moAttr = (MetaObject) value;
                    try {
                        // rekursion
                        // wenn value null wird das feld null gesetzt und die rekursion nicht aufgerufen
                        if (value != null) {
                            int status = moAttr.getStatus();

                            Integer o_id = moAttr.getID();

                            if (status == MetaObject.NEW) {
                                if (!moAttr.isDummy()) {
                                    o_id = insertMetaObject(user, moAttr);
                                } else {

                                    o_id = mo.getID();
                                    //setzen der id in den jt-objekten noch zu machen
                                    insertMetaObjectArray(user, moAttr);

                                }
                            }// noch zu testen
                            else if (status == MetaObject.TO_DELETE) {
                                o_id = null;
                                deleteMetaObject(user, moAttr);
                            }
                            //else bei update NOP

                            // foreignkey wird hier gesetzt
                            if (status != MetaObject.TEMPLATE) { //Hell <--
                                valueList += komma + o_id; //Orig
                            } else {
                                valueList += komma + "NULL";
                            }   //-->Hell
                        }else if (mAttr[i].isArray()){
                            valueList += komma + rootPk;
                        }
                        else {//value == null
                            valueList += komma + "NULL";
                        }

                    } catch (Exception e) {
                        String error = "rekursion in insert mo unterbrochen moAttr::" + moAttr + " MAI" + mai;
                        System.err.println(error);
                        e.printStackTrace();
                        logger.error(error, e);
                        throw e;
                    }


                }

                // wird erst im 2ten durchlauf gesetzt damit nach der klammer nicht direkt ein komma kommt
                komma = ",";

            } // ende der iteration \u00FCber alle attribute

            // die Variablen attrNameList u. valueList enthalten jetzt die notwendigen werte f\u00FCr ein insert

            // attributnamen und values zum statement hinzuf\u00FCgen
            sql += attrNameList + ") VALUES (" + valueList + ")";



            transactionHelper.beginWork();

            Statement s = transactionHelper.getConnection().createStatement();

            logger.info("sql: " + sql);
            s.executeUpdate(sql);

            /*
             * since the meta-jdbc driver is obsolete the index must be
             * refreshed by the server explicitly
             */
            insertIndex(mo);

            transactionHelper.commit();


            return rootPk;
        } else {
            logger.debug("User " + user + "is not insert to update MetaObject " + mo.getID() + "." + mo.getClassKey(), new CurrentStackTrace());
            return -1;
        }
    }

    /**
     * mscholl:
     * Deletes the index from cs_attr_string and cs_all_attr_mapping for a given
     * metaobject. If the metaobject does not contain a metaclass it is skipped.
     *
     * @param mo the metaobject which will be deleted
     * @throws java.sql.SQLException if an error occurs during index deletion
     */
    private void deleteIndex(final MetaObject mo) throws SQLException
    {
        if(mo == null)
        {
            throw new NullPointerException("MetaObject must not be null");
        }else if(mo.isDummy())
        {
            // don't do anything with a dummy object
            if(logger.isDebugEnabled())
            {
                logger.debug("delete index for dummy won't be done");
            }
            return;
        }else if(logger.isInfoEnabled())
        {
            logger.info("delete index for MetaObject: " + mo);
        }
        PreparedStatement psAttrString = null;
        PreparedStatement psAttrMap = null;
        try
        {
            // prepare the update statements
            psAttrString = transactionHelper.getConnection()
                    .prepareStatement(DEL_ATTR_STRING);
            psAttrMap = transactionHelper.getConnection()
                    .prepareStatement(DEL_ATTR_MAPPING);
            
            // set the appropriate param values
            psAttrString.setInt(1, mo.getClassID());
            psAttrString.setInt(2, mo.getID());
            psAttrMap.setInt(1, mo.getClassID());
            psAttrMap.setInt(2, mo.getID());
            
            // execute the deletion
            final int strRows = psAttrString.executeUpdate();
            final int mapRows = psAttrMap.executeUpdate();
            if(logger.isDebugEnabled())
            {
                logger.debug("cs_attr_string: deleted " + strRows + " rows");
                logger.debug("cs_all_attr_mapping: deleted " + mapRows 
                        + " rows");
            }
        }catch(final SQLException e)
        {
            logger.error("could not delete index for object '" + mo.getID() 
                    + "' of class '" + mo.getClass() + "'", e);
            throw e;
        }finally
        {
            closeStatements(psAttrString, psAttrMap);
        }
    }

    /**
     * mscholl:
     * Updates the index of cs_attr_string and cs_all_attr_mapping for the given
     * metaobject. Update for a certain attribute will only be done if the
     * attribute is changed.
     *
     * @param mo the metaobject which will be updated
     * @throws java.sql.SQLException if an error occurs during index update
     */
    private void updateIndex(final MetaObject mo) throws SQLException
    {
        if(mo == null)
        {
            throw new NullPointerException("MetaObject must not be null");
        }else if(mo.isDummy())
        {
            // don't do anything with a dummy object
            if(logger.isDebugEnabled())
            {
                logger.debug("update index for dummy won't be done");
            }
            return;
        }else if(logger.isInfoEnabled())
        {
            logger.info("update index for MetaObject: " + mo);
        }
        PreparedStatement psAttrString = null;
        PreparedStatement psAttrMap = null;
        try
        {
            for(final ObjectAttribute attr : mo.getAttribs())
            {
                final MemberAttributeInfo mai = attr.getMai();
                if(mai.isIndexed() && attr.isChanged())
                {
                    // set the appropriate param values according to the field
                    // value
                    if(mai.isForeignKey())
                    {
                        // lazily prepare the statement
                        if(psAttrMap == null)
                        {
                            psAttrMap = transactionHelper.getConnection()
                                    .prepareStatement(UP_ATTR_MAPPING);
                        }
                        // if field represents a foreign key the attribute value
                        // is assumed to be a MetaObject
                        final MetaObject value = (MetaObject)attr.getValue();
                        psAttrMap.setInt(1, value.getID());
                        psAttrMap.setInt(2, mo.getClassID());
                        psAttrMap.setInt(3, mo.getID());
                        psAttrMap.setInt(4, value.getClassID());
                        psAttrMap.addBatch();
                        if(logger.isDebugEnabled())
                        {
                            // create debug statement
                            final String debugStmt = UP_ATTR_MAPPING
                                    .replaceFirst("\\?", "" + value.getID())
                                    .replaceFirst("\\?", "" + mo.getClassID())
                                    .replaceFirst("\\?", "" + mo.getID())
                                    .replaceFirst("\\?", ""
                                    + value.getClassID());
                            logger.debug("added to batch: " + debugStmt);
                        }
                    }else
                    {
                        // lazily prepare the statement
                        if(psAttrString == null)
                        {
                            psAttrString = transactionHelper.getConnection()
                                    .prepareStatement(UP_ATTR_STRING);
                        }
                        // interpret the fields value as a string
                        psAttrString.setString(1, attr.getValue().toString());
                        psAttrString.setInt(2, mo.getClassID());
                        psAttrString.setInt(3, mo.getID());
                        psAttrString.setInt(4, mai.getId());
                        psAttrString.addBatch();
                        if(logger.isDebugEnabled())
                        {
                            // create debug statement
                            final String debugStmt = UP_ATTR_MAPPING
                                    .replaceFirst("\\?", "" + attr.getValue())
                                    .replaceFirst("\\?", "" + mo.getClassID())
                                    .replaceFirst("\\?", "" + mo.getID())
                                    .replaceFirst("\\?", "" + mai.getId());
                            logger.debug("added to batch: " + debugStmt);
                        }
                    }
                }
            }

            // execute the batches if there are indexed fields
            if(psAttrString != null)
            {
                final int[] strRows = psAttrString.executeBatch();
                if(logger.isDebugEnabled())
                {
                    int updateCount = 0;
                    for(final int row : strRows)
                    {
                        updateCount += row;
                    }
                    logger.debug("cs_attr_string: updated " + updateCount
                            + " rows");
                }
            }
            if(psAttrMap != null)
            {
                final int[] mapRows = psAttrMap.executeBatch();
                if(logger.isDebugEnabled())
                {
                    int updateCount = 0;
                    for(final int row : mapRows)
                    {
                        updateCount += row;
                    }
                    logger.debug("cs_all_attr_mapping: updated " + updateCount
                            + " rows");
                }
            }
        }catch(final SQLException e)
        {
            logger.error("could not insert index for object '" + mo.getID()
                    + "' of class '" + mo.getClass() + "'", e);
            throw e;
        }finally
        {
            closeStatements(psAttrString, psAttrMap);
        }
    }

    /**
     * mscholl:
     * Inserts the index in cs_attr_string and cs_all_attr_mapping for the given
     * metaobject. If the metaobject does not contain a metaclass it is skipped.
     *
     * @param mo the metaobject which will be newly created
     * @throws java.sql.SQLException if an error occurs during index insertion
     */
    private void insertIndex(final MetaObject mo) throws SQLException
    {
        if(mo == null)
        {
            throw new NullPointerException("MetaObject must not be null");
        }else if(mo.isDummy())
        {
            // don't do anything with a dummy object
            if(logger.isDebugEnabled())
            {
                logger.debug("insert index for dummy won't be done");
            }
            return;
        }else if(logger.isInfoEnabled())
        {
            logger.info("insert index for MetaObject: " + mo);
        }
        try
        {
            // we just want to make sure that there is no index present for the
            // given object
            deleteIndex(mo);
        }catch(final SQLException e)
        {
            logger.error("could not delete index before insert index", e);
            throw e;
        }
        PreparedStatement psAttrString = null;
        PreparedStatement psAttrMap = null;
        try
        {
            for(final ObjectAttribute attr : mo.getAttribs())
            {
                final MemberAttributeInfo mai = attr.getMai();
                if(mai.isIndexed())
                {
                    // set the appropriate param values according to the field
                    // value
                    if(mai.isForeignKey())
                    {
                        // lazily prepare the statement
                        if(psAttrMap == null)
                        {
                            psAttrMap = transactionHelper.getConnection()
                                    .prepareStatement(INS_ATTR_MAPPING);
                        }
                        psAttrMap.setInt(1, mo.getClassID());
                        psAttrMap.setInt(2, mo.getID());
                        // if field represents a foreign key the attribute value
                        // is assumed to be a MetaObject
                        final MetaObject value = (MetaObject)attr.getValue();
                        psAttrMap.setInt(3, value.getClassID());
                        psAttrMap.setInt(4, value.getID());
                        psAttrMap.addBatch();
                    }else
                    {
                        // lazily prepare the statement
                        if(psAttrString == null)
                        {
                            psAttrString = transactionHelper.getConnection()
                                    .prepareStatement(INS_ATTR_STRING);
                        }
                        psAttrString.setInt(1, mo.getClassID());
                        psAttrString.setInt(2, mo.getID());
                        psAttrString.setInt(3, mai.getId());
                        // interpret the fields value as a string
                        psAttrString.setString(4, attr.getValue().toString());
                        psAttrString.addBatch();
                    }
                }
            }

            // execute the batches if there are indexed fields
            if(psAttrString != null)
            {
                final int[] strRows = psAttrString.executeBatch();
                if(logger.isDebugEnabled())
                {
                    int insertCount = 0;
                    for(final int row : strRows)
                    {
                        insertCount += row;
                    }
                    logger.debug("cs_attr_string: inserted " + insertCount 
                            + " rows");
                }
            }
            if(psAttrMap != null)
            {
                final int[] mapRows = psAttrMap.executeBatch();
                if(logger.isDebugEnabled())
                {
                    int insertCount = 0;
                    for(final int row : mapRows)
                    {
                        insertCount += row;
                    }
                    logger.debug("cs_all_attr_mapping: inserted " + insertCount
                            + " rows");
                }
            }
        }catch(final SQLException e)
        {
            logger.error("could not insert index for object '" + mo.getID()
                    + "' of class '" + mo.getClass() + "'", e);
            throw e;
        }finally
        {
            closeStatements(psAttrString, psAttrMap);
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
                logger.warn("could not close statement", e);
            }
        }
    }

    private void closeStatements(final Statement ... s)
    {
        for(final Statement stmt : s)
        {
            closeStatement(stmt);
        }
    }
}

