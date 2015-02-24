/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.search.builtin;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import de.cismet.cids.server.search.AbstractCidsServerSearch;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class CsvExportSearchStatement extends AbstractCidsServerSearch {

    //~ Static fields/initializers ---------------------------------------------

    /** LOGGER. */
    private static final transient Logger LOG = Logger.getLogger(CsvExportSearchStatement.class);

    public static final Collection<String> METACLASSES_ALLOWED = Arrays.asList("kassenzeichen_view_all");
    public static final String CSV_SEPARATOR = ";";

    //~ Instance fields --------------------------------------------------------

    private final String metaClassName;
    private final String domainName;
    private final List<String> fields;
    private final String whereCause;
    private String dateFormat = "dd.MM.yyyy";
    private String[] booleanFormat = new String[] { "no", "yes" };

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new KassenzeichenSearchStatement object.
     *
     * @param  metaClassName  DOCUMENT ME!
     * @param  domainName     DOCUMENT ME!
     * @param  fields         DOCUMENT ME!
     * @param  whereCause     DOCUMENT ME!
     */
    public CsvExportSearchStatement(final String metaClassName,
            final String domainName,
            final List<String> fields,
            final String whereCause) {
        this.metaClassName = metaClassName;
        this.domainName = domainName;
        this.fields = fields;
        this.whereCause = whereCause;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Collection<String> performServerSearch() {
        try {
            final MetaService ms = (MetaService)(getActiveLocalServers().get(domainName));
            final MetaClass metaClass = ms.getClassByTableName(getUser(), metaClassName.toLowerCase());
            if ((metaClass != null) && !metaClass.getAttributeByName("Queryable").isEmpty()) {
                final MetaObject moDummy = metaClass.getEmptyInstance();

                final List<String> formattedFields = new ArrayList<String>(fields.size());
                for (final String field : fields) {
                    final String javaClassName = moDummy.getAttributeByFieldName(field).getMai().getJavaclassname();
                    final Class javaClass = Class.forName(javaClassName);

                    if (javaClass.isAssignableFrom(Date.class)) {
                        formattedFields.add("to_char(" + field + ", " + dateFormat + ")");
                    } else if (javaClass.isAssignableFrom(Boolean.class)) {
                        formattedFields.add("CASE WHEN " + field + " IS TRUE THEN '" + booleanFormat[1] + "' ELSE '"
                                    + booleanFormat[0] + "' END");
                    } else if (javaClass.isAssignableFrom(Geometry.class)) {
                        formattedFields.add("st_astext(" + field + ")");
                    } else {
                        formattedFields.add(field);
                    }
                }
                final String sql = "SELECT " + implode(formattedFields.toArray(new String[0]), ", ") + " "
                            + "FROM " + metaClass.getTableName() + " "
                            + "WHERE " + whereCause;

                final ArrayList<ArrayList> results = ms.performCustomSearch(sql);

                final ArrayList<String> rows = new ArrayList<String>();
                for (final ArrayList result : results) {
                    final List<String> columns = new ArrayList<String>(result.size());
                    for (final Object object : result) {
                        columns.add((object == null) ? "" : ("\"" + object.toString() + "\""));
                    }
                    rows.add(implode(columns.toArray(new String[0]), CSV_SEPARATOR));
                }

                return rows;
            } else {
                LOG.error(metaClassName + " not allowed");           // NOI18N
                return null;
            }
        } catch (final Exception e) {
            LOG.error("problem during CsvExportSearchStatement", e); // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dateFormat  DOCUMENT ME!
     */
    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getBooleanFormat() {
        return booleanFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  booleanFormat  DOCUMENT ME!
     */
    public void setBooleanFormat(final String[] booleanFormat) {
        this.booleanFormat = booleanFormat;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   stringArray  DOCUMENT ME!
     * @param   delimiter    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String implode(final Object[] stringArray, final String delimiter) {
        if (stringArray.length == 0) {
            return "";
        } else {
            final StringBuilder sb = new StringBuilder();
            sb.append(stringArray[0]);
            for (int index = 1; index < stringArray.length; index++) {
                sb.append(delimiter);
                sb.append(stringArray[index]);
            }
            return sb.toString();
        }
    }
}
