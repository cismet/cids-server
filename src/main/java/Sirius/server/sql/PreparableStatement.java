/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.localserver.object.PersistenceManager;

import java.io.Serializable;
import java.io.StringReader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * NOT TO BE SHARED AMONG THREADS.
 *
 * @author   martin.scholl@cismet.de
 * @version  0.1
 */
public class PreparableStatement implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private final String statement;
    private final int[] types;

    private Object[] objects;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PreparableStatement object.
     *
     * @param  stmt  DOCUMENT ME!
     */
    public PreparableStatement(final String stmt) {
        this(stmt, null);
    }

    /**
     * Creates a new PreparableStatement object.
     *
     * @param   stmt   DOCUMENT ME!
     * @param   types  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public PreparableStatement(final String stmt, final int... types) {
        if (stmt == null) {
            throw new IllegalArgumentException("stmt must not be null"); // NOI18N
        }

        this.statement = stmt;
        this.types = types;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getStatement() {
        return statement;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int[] getTypes() {
        return types;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object[] getObjects() {
        return objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objects  DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public void setObjects(final Object... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects must not be null"); // NOI18N
        }

        if (objects.length != types.length) {
            throw new IllegalArgumentException("length of types and objects does not match: [types.length=" // NOI18N
                        + types.length + "|" + objects.length + "]");        // NOI18N
        }

        this.objects = objects;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException           DOCUMENT ME!
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public PreparedStatement parameterise(final Connection c) throws SQLException {
        if (objects == null) {
            throw new IllegalStateException("objects not initialised"); // NOI18N
        }

        final PreparedStatement ps = c.prepareStatement(statement);

        for (int i = 0; i < objects.length; ++i) {
            final int type = ((types == null) ? ps.getParameterMetaData().getParameterType(i + 1) : types[i]);
            if ((objects[i] == null) || PersistenceManager.NULL.equals(objects[i])) {
                ps.setNull(i + 1, type);
            } else if (Types.CLOB == type) {
                if (objects[i] instanceof String) {
                    ps.setClob(i + 1, new StringReader((String)objects[i]));
                } else {
                    throw new IllegalStateException(
                        "type mismatch: type shall be CLOB but did not find a string object"); // NOI18N
                }
            } else {
                ps.setObject(i + 1, objects[i], type);
            }
        }

        return ps;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(statement);
        if (objects == null) {
            sb.append("|illegal state: no objects");      // NOI18N
        } else if (types == null) {
            sb.append("|null");                           // NOI18N
        } else {
            for (int i = 0; i < types.length; ++i) {
                sb.append("|types=").append(types[i]);    // NOI18N
                sb.append(",object=").append(objects[i]); // NOI18N
            }
        }

        return sb.toString();
    }

    /**
     * Not intended to do object deserialisation. Builds a PreparableStatement from the given string. String is supposed
     * to start with the statement followed by the parameter type numbers separated by ';'. If no parameter is given it
     * is assumed that the statement does not have any parameters. If any of the parameters is the string 'null'
     * (without quotes) then it is assumed that the types shall be collected from the database.
     *
     * @param   s  the string to parse
     *
     * @return  a <code>PreparableStatement</code> with the appropriate settings
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     *
     * @see     #PreparableStatement(java.lang.String, int...)
     * @see     #toString(Sirius.server.sql.PreparableStatement)
     */
    public static PreparableStatement fromString(final String s) {
        if ((s == null) || s.isEmpty()) {
            return null;
        }

        final String[] split = s.split(";");
        if (split.length == 1) {
            // assume stmt without parameters, thus no type
            return new PreparableStatement(split[0], new int[0]);
        } else {
            final String stmt = split[0];
            int[] types = new int[split.length - 1];

            for (int i = 1; (i < split.length) && (types != null); ++i) {
                final String type = split[i];
                if (type.equalsIgnoreCase("null")) {
                    // this is the "use the db types" sign
                    types = null;
                } else {
                    try {
                        types[i - 1] = Integer.parseInt(type);
                    } catch (final NumberFormatException e) {
                        throw new IllegalStateException("unsupported type format: " + type, e); // NOI18N
                    }
                }
            }

            return new PreparableStatement(stmt, types);
        }
    }

    /**
     * Not intended to do object serialisation as the result will not contain any info of the available objects. Only
     * builds a string that is compatible to {@link #fromString(java.lang.String)}.
     *
     * @param   ps  the object to convert to the string representation
     *
     * @return  a string representation containing the statement and the type information
     */
    public static String toString(final PreparableStatement ps) {
        final StringBuilder sb = new StringBuilder(ps.getStatement());

        if (ps.getTypes() == null) {
            sb.append(";null");              // NOI18N
        } else if (ps.getTypes().length > 0) {
            for (final int type : ps.getTypes()) {
                sb.append(';').append(type); // NOI18N
            }
        }

        return sb.toString();
    }
}
