/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.sql.Clob;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cismet.cismap.commons.jtsgeometryfactories.IGeometryFactory;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  0.1
 */
public final class SQLTools {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(SQLTools.class);
    private static final String DIALECT = Lookup.getDefault().lookup(DialectProvider.class).getDialect();

    //~ Instance fields --------------------------------------------------------

    private final Map<String, ServerSQLStatements> stmts;
    private final Map<String, IGeometryFactory> geometryFactories;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SQLTools object.
     */
    private SQLTools() {
        final Collection<? extends ServerSQLStatements> c = Lookup.getDefault().lookupAll(ServerSQLStatements.class);
        final Map<String, ServerSQLStatements> map = new HashMap<String, ServerSQLStatements>();
        for (final ServerSQLStatements s : c) {
            map.put(s.getDialect(), s);
        }

        final Collection<? extends IGeometryFactory> c3 = Lookup.getDefault().lookupAll(IGeometryFactory.class);
        final Map<String, IGeometryFactory> m3 = new HashMap<String, IGeometryFactory>();
        for (final IGeometryFactory gf : c3) {
            m3.put(gf.getDialect(), gf);
        }

        stmts = Collections.unmodifiableMap(map);
        geometryFactories = Collections.unmodifiableMap(m3);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   c           DOCUMENT ME!
     * @param   dialect     DOCUMENT ME!
     * @param   descriptor  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getStatement(final Class c, final String dialect, final String descriptor) {
        final String statement;
        if ((dialect == null) || dialect.isEmpty()) {
            statement = NbBundle.getMessage(c, descriptor);
        } else {
            final String bundleName;
            if ("postgres_9".equals(dialect)) {
                // default dialect
                bundleName = "Bundle";
            } else {
                bundleName = "Bundle_" + dialect;
            }
            final String bundle = c.getPackage().getName().replaceAll("\\.", "/") + "/" + bundleName;
            statement = ResourceBundle.getBundle(bundle).getString(descriptor);
        }

        return statement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dialect  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public static ServerSQLStatements getStatements(final String dialect) {
        final String d = ((dialect == null) || dialect.isEmpty()) ? "postgres_9" : dialect; // NOI18N

        final ServerSQLStatements s = LazyInitializer.INSTANCE.stmts.get(d);

        if (s == null) {
            throw new IllegalStateException("dialect not found: " + dialect); // NOI18N
        }

        return s;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   dialect  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public static IGeometryFactory getGeometryFactory(final String dialect) {
        final String d = ((dialect == null) || dialect.isEmpty()) ? "postgres_9" : dialect; // NOI18N

        final IGeometryFactory gf = LazyInitializer.INSTANCE.geometryFactories.get(d);

        if (gf == null) {
            throw new IllegalStateException("dialect not found: " + dialect); // NOI18N
        }

        return gf;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectFromResultSet  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Geometry getGeometryFromResultSetObject(Object objectFromResultSet) {
        try {
            if (objectFromResultSet != null) {
                if (objectFromResultSet instanceof Clob) {
                    // we convert the clob to a string, otherwise the value is not serialisable out of the
                    // box due to the direct connection to the database
                    // TODO: handle overflows, i.e. clob too big
                    final Clob clob = (Clob)objectFromResultSet;
                    if (clob.length() <= Integer.valueOf(Integer.MAX_VALUE).longValue()) {
                        objectFromResultSet = clob.getSubString(1, Long.valueOf(clob.length()).intValue());
                    } else {
                        throw new IllegalStateException(
                            "cannot handle clobs larger than Integer.MAX_VALUE)");
                    }
                }
                if (SQLTools.getGeometryFactory(DIALECT).isGeometryObject(objectFromResultSet)) {
                    return SQLTools.getGeometryFactory(DIALECT).createGeometry(objectFromResultSet);
                }
            }
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("cashedGeometry was not in the resultset. But this is normal for the most parts", e); // NOI18N
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pgInsertDump      DOCUMENT ME!
     * @param  oracleInsertDump  oracleConnection DOCUMENT ME!
     * @param  srid              DOCUMENT ME!
     */
    public static void insertIntoGeom(final File pgInsertDump, final File oracleInsertDump, final int srid) {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new FileReader(pgInsertDump));
            bw = new BufferedWriter(new FileWriter(oracleInsertDump));

            final Pattern p = Pattern.compile("^INSERT INTO geom.+VALUES \\((\\d+), '([A-Z\\d]+)'\\);$");
            final WKBReader r = new WKBReader(new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), srid));

            String line = br.readLine();
            while (line != null) {
                final Matcher m = p.matcher(line);
                if (!m.matches()) {
                    throw new IllegalStateException("dump does not contain proper insert statements: " + p.toString());
                }

                final String hex = m.group(2);
                final byte[] bytes = new byte[hex.length() / 2];
                for (int i = 0; i < bytes.length; ++i) {
                    bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, (2 * i) + 2), 16);
                }

                final Geometry geom = r.read(bytes);

                bw.write("INSERT INTO geom VALUES (" + m.group(1) + ", SDO_GEOMETRY('" + geom.toText() + "', " + srid
                            + "));");
                bw.newLine();

                line = br.readLine();
            }

            bw.flush();
        } catch (final Exception e) {
            LOG.error("cannot insert geometries from file: " + pgInsertDump, e);
        } finally {
            IOUtils.closeQuietly(br);
            IOUtils.closeQuietly(bw);
        }
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static final class LazyInitializer {

        //~ Static fields/initializers -----------------------------------------

        private static final SQLTools INSTANCE = new SQLTools();
    }
}
