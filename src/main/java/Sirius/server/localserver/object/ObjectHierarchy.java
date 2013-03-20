/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.sql.DBConnectionPool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Generiert die Objekthierarchie f\u00FCr alle !!!!indizierten!!!! Strukturen
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class ObjectHierarchy {

    //~ Instance fields --------------------------------------------------------

    protected DBConnectionPool conPool;

    // getFatherstmnt hierarchy
    private final Map<Integer, Set<String>> fatherStmnts = new HashMap<Integer, Set<String>>();
    private final Map<Integer, Set<String>> arrayFatherStmnts = new HashMap<Integer, Set<String>>();
    // liefert classId referenziert von classIds
    private final Map<Integer, Set<Integer>> classIdHierarchy = new HashMap<Integer, Set<Integer>>();
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
        final Connection con = conPool.getDBConnection().getConnection();

        final Statement stmnt = con.createStatement();
        // schl\u00FCssel , vatertabellen name , Atrributname
        final String initLookupTable =
            "select  a.foreign_key_references_to as child,a.class_id as father,c.primary_key_field as pk,c.table_name,a.field_name, isarray  from cs_attr a,cs_class  c where a.foreign_key ='T' and  a.class_id = c.id   and a.indexed=true";
        ResultSet rs = stmnt.executeQuery(initLookupTable);

        while (rs.next()) {
            final Integer key = new Integer(rs.getInt("child")); // NOI18N

            final String pk = rs.getString("pk"); // NOI18N

            final int father = rs.getInt("father"); // NOI18N
            final boolean isArray = rs.getBoolean("isarray");

            // konstruiere select string f\u00FCr Vaterobjekt mit Auswahlkriterium = Objektid des Attributes

            final String value = "Select " + father + " as class_id ," + pk + " as object_id" + " from "
                        + rs.getString("table_name") + " where " + rs.getString("field_name") + " = ";
            if (logger.isDebugEnabled()) {
                logger.debug(" get Father key :: " + key + " value :: " + value); // NOI18N
            }
            if (!isArray) {
                if (!fatherStmnts.containsKey(key)) {
                    fatherStmnts.put(key, Collections.synchronizedSet(new LinkedHashSet<String>()));
                }
                fatherStmnts.get(key).add(value);
            }

            final Integer cKey = new Integer(father);
            if (!classIdHierarchy.containsKey(cKey)) {
                classIdHierarchy.put(cKey, Collections.synchronizedSet(new LinkedHashSet<Integer>()));
            }
            classIdHierarchy.get(cKey).add(key);
        }

        // init array stmns notwendig da array merkmal nicht der primary key ist

        final String initArrayLookupTable =
            "select cf.primary_key_field as father_pk,cc.primary_key_field as child_pk,a.array_key, a.foreign_key_references_to as child,a.class_id as father,cf.table_name as father_table, cc.table_name as child_table,a.field_name as attribute  from cs_attr a,cs_class  cf, cs_class cc where a.foreign_key ='T' and  a.class_id = cf.id and isarray ='T' and a.foreign_key_references_to =cc.id";

        rs = stmnt.executeQuery(initArrayLookupTable);

        while (rs.next()) {
            final String arrayKey = rs.getString("array_key");        // NOI18N
            final Integer key = new Integer(rs.getInt("child"));      // NOI18N
            final String father_pk = rs.getString("father_pk");       // NOI18N
            final int father = rs.getInt("father");                   // NOI18N
            final String attribute = rs.getString("attribute");       // NOI18N
            final String child_table = rs.getString("child_table");   // NOI18N
            final String father_table = rs.getString("father_table"); // NOI18N
            final String child_pk = rs.getString("child_pk");         // NOI18N

            final String value = "Select " + father + " as class_id ," + father_pk + " as object_id"
                        + " from "                                                                         // NOI18N
                        + father_table
                        + " where " + attribute + " in "
                        + " (select " + arrayKey + " from " + child_table + " where  " + child_pk + " = "; // ? )
            if (logger.isDebugEnabled()) {
                logger.debug(" get Array Father key :: " + key + " value :: " + value);                    // NOI18N
            }

            if (!arrayFatherStmnts.containsKey(key)) {
                arrayFatherStmnts.put(key, Collections.synchronizedSet(new LinkedHashSet<String>()));
            }
            arrayFatherStmnts.get(key).add(value);
            final Integer cKey = new Integer(father);
            if (!classIdHierarchy.containsKey(cKey)) {
                classIdHierarchy.put(cKey, Collections.synchronizedSet(new LinkedHashSet<Integer>()));
            }
            classIdHierarchy.get(cKey).add(key);
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
            final Collection statements = fatherStmnts.get(new Integer(classId));

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
            final Collection ids = classIdHierarchy.get(new Integer(parentClassId));

            final Iterator iter = ids.iterator();

            result = new ArrayList(ids.size());

            while (iter.hasNext()) {
                final Integer child = (Integer)iter.next();
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

        return (Integer[])result.toArray(new Integer[result.size()]);
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
            final Collection statements = arrayFatherStmnts.get(new Integer(classId));

            if (statements == null) {
                return result;
            }

            final Iterator iter = statements.iterator();

            result = new ArrayList(statements.size());
            while (iter.hasNext()) {
                result.add(iter.next().toString() + objectId + ")"); // NOI18N
            }
        }

        return result;
    }
}
