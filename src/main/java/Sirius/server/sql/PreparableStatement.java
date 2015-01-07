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
     * Creates a new PreparableStatement object. Similar to {@link #PreparableStatement(java.lang.String, int...)} with
     * <code>null</code> for types array.
     *
     * @param  stmt  the parameterised statement, not <code>null</code>
     */
    public PreparableStatement(final String stmt) {
        this(stmt, null);
    }

    /**
     * Creates a new PreparableStatement object. If <code>null</code> for the types array is provided then the parameterise
     * operation will assume that the types are properly inferred by the jdbc driver.
     *
     * @param   stmt   the parameterised statement, not <code>null</code>
     * @param   types  an int array with the types of the single parameters, sorted by occurrence in statement, or <code>null</code>
     * if the types shall be inferred by the jdbc driver.
     *
     * @throws  IllegalArgumentException  if the stmt is <code>null</code>
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
     * The statement
     *
     * @return  the statement
     */
    public String getStatement() {
        return statement;
    }

    /**
     * The types of the parameters of the statement, <code>null</code> if the types shall be inferred by the jdbc driver.
     *
     * @return  the types of the parameters of the statement
     */
    public int[] getTypes() {
        return types;
    }

    /**
     * The actual values for the parameters.
     *
     * @return  the actual values for the parameters
     */
    public Object[] getObjects() {
        return objects;
    }

    /**
     * Sets the actual objects that shall be used for parameterisation. The number of objects must match the number of
     * types if the types are actually provided.
     *
     * @param   objects  the objects to use during parameterisation
     *
     * @throws  IllegalArgumentException  if objects is <code>null</code> or if the number of object does not match the number of types if the types are not <code>null</code>
     */
    public void setObjects(final Object... objects) {
        if (objects == null) {
            throw new IllegalArgumentException("objects must not be null"); // NOI18N
        }

        if (types != null && objects.length != types.length) {
            throw new IllegalArgumentException("length of types and objects does not match: [types.length=" // NOI18N
                        + types.length + "|" + objects.length + "]");        // NOI18N
        }

        this.objects = objects;
    }

    /**
     * Creates a <code>PreparedStatement</code> using the parameterised statement, the types of this instance or the
     * types of the jdbc driver if the types of this instance is <code>null</code> and the actual objects.
     *
     * @param   c  a jdbc connection to create the statement
     *
     * @return  the parameterised <code>PreparedStatement</code> ready for execution
     *
     * @throws  SQLException           if there is an error during parameterisation
     * @throws  IllegalStateException  if the objects have not been set (objects are <code>null</code>)
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

    /**
     * This operation is only for debugging purposes. It does not provide proper serialisation or anything similar.
     * 
     * @return a debug string representation of the instance
     */
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
