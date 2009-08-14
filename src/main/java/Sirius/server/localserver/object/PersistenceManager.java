/*
 * PersistenceManager.java
 *
 * Created on 3. Juni 2006, 12:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.localserver.object;

import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;
import java.sql.*;
import Sirius.server.search.*;
import Sirius.server.localserver.attribute.*;
import Sirius.server.localserver.*;
import java.util.*;
import Sirius.server.property.*;
import com.vividsolutions.jts.geom.Geometry;
import de.cismet.cismap.commons.jtsgeometryfactories.*;
import de.cismet.tools.CurrentStackTrace;

/**
 *
 * @author schlob
 */
public class PersistenceManager {

    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
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
                        } else {//value == null
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

            transactionHelper.commit();


            return rootPk;
        } else {
            logger.debug("User " + user + "is not insert to update MetaObject " + mo.getID() + "." + mo.getClassKey(), new CurrentStackTrace());
            return -1;
        }
    }
}

