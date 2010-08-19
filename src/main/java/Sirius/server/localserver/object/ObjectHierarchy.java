/***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
/*
 * ObjectHierarchy.java
 *
 * Created on 25. Mai 2004, 11:48
 */
package Sirius.server.localserver.object;

import Sirius.server.sql.*;

import java.sql.*;

import java.util.*;

import de.cismet.tools.collections.*;

/**
 * Generiert die Objekthierarchie f\u00FCr alle !!!!indizierten!!!! Strukturen
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ObjectHierarchy {

    //~ Instance fields --------------------------------------------------------
    // getFatherstmnt hierarchy
    protected MultiMap fatherStmnts = new MultiMap();
    protected MultiMap arrayFatherStmnts = new MultiMap();
    // liefert classId referenziert von classIds
    protected MultiMap classIdHierarchy = new MultiMap();
    protected DBConnectionPool conPool;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------
    /**
     * Creates a new instance of ObjectHierarchy.
     *
     * @param   conPool  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ObjectHierarchy(final DBConnectionPool conPool) throws Exception {
        this.conPool = conPool;
        final Connection con = conPool.getConnection().getConnection();

        final Statement stmnt = con.createStatement();
        // schl\u00FCssel , vatertabellen name , Atrributname
        final String initLookupTable =
                "select  a.foreign_key_references_to as child,a.class_id as father,c.primary_key_field as pk,c.table_name,a.field_name, isarray  from cs_attr a,cs_class  c where a.foreign_key ='T' and  a.class_id = c.id   and a.indexed=true";
//            "select  a.foreign_key_references_to as child,a.class_id as father,c.primary_key_field as pk,c.table_name,a.field_name  from cs_attr a,cs_class  c where a.foreign_key ='T' and  a.class_id = c.id   and a.indexed=true and isarray ='F'";
        // and a.indexed=true
        // and ( a.isarray is null or a.isarray = 'F') rausgenommen
        ResultSet rs = stmnt.executeQuery(initLookupTable);

        while (rs.next()) {
            final Integer key = new Integer(rs.getInt("child"));

            final String pk = rs.getString("pk");

            final int father = rs.getInt("father");
            final boolean isArray = rs.getBoolean("isarray");

            // konstruiere select string f\u00FCr Vaterobjekt mit Auswahlkriterium = Objektid des Attributes

            final String value = "Select " + father + " as class_id ," + pk + " as object_id" + " from "
                    + rs.getString("table_name") + " where " + rs.getString("field_name") + " = ";
            if (logger.isDebugEnabled()) {
                logger.debug(" get Father key :: " + key + " value :: " + value);
            }
            if (!isArray) {
                fatherStmnts.put(key, value);
            }

            classIdHierarchy.put(new Integer(father), key);
        }

        // init array stmns notwendig da array merkmal nicht der primary key ist

        final String initArrayLookupTable =
                "select cf.primary_key_field as father_pk,cc.primary_key_field as child_pk,a.array_key, a.foreign_key_references_to as child,a.class_id as father,cf.table_name as father_table, cc.table_name as child_table,a.field_name as attribute  from cs_attr a,cs_class  cf, cs_class cc where a.foreign_key ='T' and  a.class_id = cf.id and isarray ='T' and a.foreign_key_references_to =cc.id";

        rs = stmnt.executeQuery(initArrayLookupTable);

        while (rs.next()) {
            final String arrayKey = rs.getString("array_key");
            final Integer key = new Integer(rs.getInt("child"));
            final String father_pk = rs.getString("father_pk");
            final int father = rs.getInt("father");
            final String attribute = rs.getString("attribute");
            final String child_table = rs.getString("child_table");
            final String father_table = rs.getString("father_table");
            final String child_pk = rs.getString("child_pk");

            final String value = "Select " + father + " as class_id ," + father_pk + " as object_id" + " from "
                    + father_table
                    + " where " + attribute + " in "
                    + " (select " + arrayKey + " from " + child_table + " where  " + child_pk + " = "; // ? )
            if (logger.isDebugEnabled()) {
                logger.debug(" get Array Father key :: " + key + " value :: " + value);
            }

            arrayFatherStmnts.put(key, value);
            classIdHierarchy.put(new Integer(father), key);
        }
    }

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean classIsReferenced(final int classId) {
        return fatherStmnts.containsKey(new Integer(classId)) || arrayFatherStmnts.containsKey(new Integer(classId));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean classIsArrayType(final int classId) {
        return arrayFatherStmnts.containsKey(new Integer(classId));
    }

    /**
     * delivers a collection of statements delivering class_id,object_id of a fatherobject.
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection getFatherStatements(final int classId, final int objectId) {
        ArrayList result = new ArrayList();

        if (fatherStmnts.containsKey(new Integer(classId))) {
            // Liste
            final Collection statements = (LinkedList) fatherStmnts.get(new Integer(classId));

            if (statements == null) {
                return result;
            }

            final Iterator iter = statements.iterator();

            result = new ArrayList(statements.size());
            while (iter.hasNext()) {
                result.add(iter.next().toString() + objectId);
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parentClassId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection getChildClassIds(final int parentClassId) {
        // als Arraylist zur\u00FCckgeben
        ArrayList result = null;

        if (classIdHierarchy.containsKey(new Integer(parentClassId))) {
            // Liste
            final Collection ids = (LinkedList) classIdHierarchy.get(new Integer(parentClassId));

            final Iterator iter = ids.iterator();

            result = new ArrayList(ids.size());

            while (iter.hasNext()) {
                final Integer child = (Integer) iter.next();
                result.add(child);

                // recursion
                final Collection childs = getChildClassIds(child.intValue());

                if (childs != null) {
                    result.addAll(childs);
                }
            }
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classIds  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Integer[] getExtendedClassList(final int[] classIds) {
        // gesch\u00E4tzte kapazit\u00E4t
        final java.util.ArrayList result = new java.util.ArrayList(classIds.length * 2);

        for (int i = 0; i < classIds.length; i++) {
            // add classid itself

            result.add(new Integer(classIds[i]));

            final Collection c = this.getChildClassIds(classIds[i]);

            if (c != null) {
                final Iterator iter = c.iterator();

                while (iter.hasNext()) {
                    result.add(iter.next());
                }
            }
        }

        return (Integer[]) result.toArray(new Integer[result.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection getArrayFatherStatements(final int classId, final int objectId) {
        ArrayList result = new ArrayList();

        if (arrayFatherStmnts.containsKey(new Integer(classId))) {
            // Liste
            final Collection statements = (LinkedList) arrayFatherStmnts.get(new Integer(classId));

            if (statements == null) {
                return result;
            }

            final Iterator iter = statements.iterator();

            result = new ArrayList(statements.size());
            while (iter.hasNext()) {
                result.add(iter.next().toString() + objectId + ")");
            }
        }

        return result;
    }
}
