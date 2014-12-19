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
import com.vividsolutions.jts.io.oracle.OraReader;

import oracle.sql.STRUCT;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.sql.Connection;
import java.sql.ResultSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cismet.cismap.commons.jtsgeometryfactories.IGeometryFactory;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class SQLTools {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(SQLTools.class);

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

    /**
     * DOCUMENT ME!
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static void main(final String[] args) throws Exception {
        final Properties p = new Properties();
        p.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender"); // NOI18N
        p.put("log4j.appender.Remote.remoteHost", "localhost");                // NOI18N
        p.put("log4j.appender.Remote.port", "4445");                           // NOI18N
        p.put("log4j.appender.Remote.locationInfo", "true");                   // NOI18N
        p.put("log4j.rootLogger", "DEBUG,Remote");                             // NOI18N
        org.apache.log4j.PropertyConfigurator.configure(p);

        final DBClassifier dbc = new DBClassifier(
                "jdbc:oracle:thin:@enciva-de2.com:1521:humboldt",
                "CISMET",                   // NOI18N
                "rTid9dEke5",               // NOI18N
                "oracle.jdbc.OracleDriver", // NOI18N
                5);
        final DBConnection con = new DBConnection(dbc);
        final Connection c = con.getConnection();
        final ResultSet s = c.createStatement().executeQuery("select * from geom where rownum <= 1");
        System.out.println(s.next());
        System.out.println(s.getInt(1));
        System.out.println(s.getObject(2));
        final OraReader or = new OraReader(new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326));
        final Geometry g = or.read((STRUCT)s.getObject(2));
        System.out.println(g.toText());

//        insertIntoGeom(new File("/Users/mscholl/Desktop/switchon_geometries.plaindump"),
//            new File("/Users/mscholl/Desktop/switchon_geometries_oracle.plaindump"),
//            4326);
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
