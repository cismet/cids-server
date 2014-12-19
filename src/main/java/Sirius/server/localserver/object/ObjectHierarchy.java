/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.property.ServerProperties;
import Sirius.server.sql.DBConnectionPool;
import Sirius.server.sql.SQLTools;

import Sirius.util.collections.MultiMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

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

    private final ServerProperties serverProperties;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of ObjectHierarchy.
     *
     * @param   conPool  DOCUMENT ME!
     * @param   props    DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public ObjectHierarchy(final DBConnectionPool conPool, final ServerProperties props) throws Exception {
        this.conPool = conPool;
        this.serverProperties = props;
        final Connection con = conPool.getDBConnection().getConnection();

        final Statement stmnt = con.createStatement();
        // schl\u00FCssel , vatertabellen name , Atrributname

        final String initLookupTable = SQLTools.getStatement(this.getClass(),
                props.getInteralDialect(),
                "initLookupTable");
        ResultSet rs = stmnt.executeQuery(initLookupTable);

        while (rs.next()) {
            final Integer key = new Integer(rs.getInt("child")); // NOI18N

            final String pk = rs.getString("pk"); // NOI18N

            final int father = rs.getInt("father"); // NOI18N
            final boolean isArray = rs.getBoolean("isarray");

            // konstruiere select string f\u00FCr Vaterobjekt mit Auswahlkriterium = Objektid des Attributes

            final String value = SQLTools.getStatements(props.getInteralDialect())
                        .getObjectHierarchyFatherStmt(
                            father,
                            pk,
                            rs.getString("table_name"),
                            rs.getString("field_name"));
            if (logger.isDebugEnabled()) {
                logger.debug(" get Father key :: " + key + " value :: " + value); // NOI18N
            }
            if (!isArray) {
                fatherStmnts.put(key, value);
            }

            classIdHierarchy.put(new Integer(father), key);
        }

        // init array stmns notwendig da array merkmal nicht der primary key ist

        final String initArrayLookupTable = SQLTools.getStatement(this.getClass(),
                props.getInteralDialect(),
                "initArrayLookupTable");

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

            final String value = SQLTools.getStatements(props.getInteralDialect())
                        .getObjectHierarchyFatherArrayStmt(
                            father,
                            father_pk,
                            father_table,
                            attribute,
                            arrayKey,
                            child_table,
                            child_pk);
            if (logger.isDebugEnabled()) {
                logger.debug(" get Array Father key :: " + key + " value :: " + value); // NOI18N
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
            final Collection statements = (LinkedList)fatherStmnts.get(new Integer(classId));

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
            final Collection ids = (LinkedList)classIdHierarchy.get(new Integer(parentClassId));

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
            final Collection statements = (LinkedList)arrayFatherStmnts.get(new Integer(classId));

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
