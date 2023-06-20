/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.actions;

import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.middleware.types.MetaObjectNode;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class CsvExportServerAction extends DefaultServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CsvExportServerAction.class);
    public static final String TASKNAME = "CsvExport";
    public static final String DEFAULT_COLUMN_SEPARATOR = ";";
    public static final String DEFAULT_ROW_SEPARATOR = "\n";
    public static final String DEFAULT_CHARSET = "UTF-8";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum ParameterType {

        //~ Enum constants -----------------------------------------------------

        COLUMN_NAMES, FIELDS, WHERE, DISTINCT_ON, BOOLEAN_YES, BOOLEAN_NO, DATE_FORMAT, ROW_SEPARATOR, COLUMN_SEPARATOR,
        CHARSET, MONS, ESCAPE_STRINGS
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getTaskName() {
        return TASKNAME;
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        final String metaClassName = (String)body;

        List<String> columnNames = null;
        List<String> fields = null;
        String whereCause = null;
        String distinctOn = null;
        String booleanYes = null;
        String booleanNo = null;
        String dateFormat = null;
        String charset = DEFAULT_CHARSET;
        String columnSeparator = DEFAULT_COLUMN_SEPARATOR;
        String rowSeparator = DEFAULT_ROW_SEPARATOR;
        List<MetaObjectNode> mons = null;
        boolean escapeStrings = true;

        if (params != null) {
            for (final ServerActionParameter sap : params) {
                if (sap.getKey().equals(ParameterType.DISTINCT_ON.toString())) {
                    distinctOn = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.WHERE.toString())) {
                    whereCause = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.BOOLEAN_YES.toString())) {
                    booleanYes = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.BOOLEAN_NO.toString())) {
                    booleanNo = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.DATE_FORMAT.toString())) {
                    dateFormat = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.COLUMN_SEPARATOR.toString())) {
                    columnSeparator = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.ROW_SEPARATOR.toString())) {
                    rowSeparator = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.CHARSET.toString())) {
                    charset = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.COLUMN_NAMES.toString())) {
                    columnNames = (List<String>)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.FIELDS.toString())) {
                    fields = (List<String>)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.MONS.toString())) {
                    mons = (List<MetaObjectNode>)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.ESCAPE_STRINGS.toString())) {
                    escapeStrings = (Boolean)sap.getValue();
                }
            }
        }

        try {
            if (metaClassName == null) {
                throw new Exception("METACLASS_NAME is empty");
            }

            final MetaService ms = getMetaService();
            final MetaClass metaClass = ms.getClassByTableName(
                    getUser(),
                    metaClassName.toLowerCase(),
                    getConnectionContext());
            if ((metaClass == null) || metaClass.getAttributeByName("Queryable").isEmpty()) {
                throw new Exception(String.format("%s not allowed", metaClassName)); // NOI18N
            }

            final List<String> wheres = new ArrayList<>();
            if (whereCause != null) {
                wheres.add(whereCause);
            }
            final List<String> wheresMon = new ArrayList<>();
            if (mons != null) {
                for (final MetaObjectNode mon : mons) {
                    if ((mon != null) && (mon.getClassId() == metaClass.getId())) {
                        wheresMon.add(String.format("%s = %d", metaClass.getPrimaryKey(), mon.getObjectId()));
                    }
                }
            }
            if (!wheresMon.isEmpty()) {
                wheres.add(String.format("(%s)", String.join(" OR ", wheresMon)));
            }

            final List<String> rows = new ArrayList<>();
            if (columnNames != null) {
                rows.add(String.join(columnSeparator, columnNames.stream().map(str -> String.format("\"%s\"", str))
                                .collect(Collectors.toList())));
            }
            rows.addAll(createRows(
                    metaClass,
                    fields,
                    wheres,
                    distinctOn,
                    booleanYes,
                    booleanNo,
                    dateFormat,
                    columnSeparator,
                    escapeStrings));
            return String.join(rowSeparator, rows).getBytes(charset);
        } catch (final Exception e) {
            LOG.error("problem during CsvExportServerAction", e); // NOI18N
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass        DOCUMENT ME!
     * @param   fields           DOCUMENT ME!
     * @param   wheres           DOCUMENT ME!
     * @param   distinctOn       DOCUMENT ME!
     * @param   booleanYes       DOCUMENT ME!
     * @param   booleanNo        DOCUMENT ME!
     * @param   dateFormat       DOCUMENT ME!
     * @param   columnSeparator  DOCUMENT ME!
     * @param   escapeStrings    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<String> createRows(final MetaClass metaClass,
            final List<String> fields,
            final List<String> wheres,
            final String distinctOn,
            final String booleanYes,
            final String booleanNo,
            final String dateFormat,
            final String columnSeparator,
            final boolean escapeStrings) throws Exception {
        final MetaObject moDummy = metaClass.getEmptyInstance(getConnectionContext());

        final List<String> formattedFields = new ArrayList<>(fields.size());
        for (final String field : fields) {
            final String formattedField;
            if (moDummy.getAttributeByFieldName(field) != null) {
                final String javaClassName = moDummy.getAttributeByFieldName(field).getMai().getJavaclassname();
                final Class javaClass = Class.forName(javaClassName);

                if ((dateFormat != null) && Date.class.isAssignableFrom(javaClass)) {
                    formattedField = String.format("to_char(%s, '%s')", field, dateFormat);
                } else if ((booleanYes != null) && (booleanNo != null) && Boolean.class.isAssignableFrom(javaClass)) {
                    formattedField = String.format(
                            "CASE WHEN %s IS TRUE THEN '%s' ELSE '%s' END",
                            field,
                            booleanYes,
                            booleanNo);
                } else if (Geometry.class.isAssignableFrom(javaClass)) {
                    formattedField = String.format("st_astext(%s)", field);
                } else {
                    formattedField = field;
                }
            } else {
                formattedField = field;
            }
            formattedFields.add(formattedField);
        }
        final String queryFields = ((distinctOn != null) ? (String.format("DISTINCT ON (%s) ", distinctOn)) : "")
                    + String.join(", ", formattedFields);
        final String sql = String.format(
                "SELECT %s FROM %s WHERE %s",
                queryFields,
                metaClass.getTableName(),
                ((wheres != null) && !wheres.isEmpty()) ? String.join(" AND ", wheres) : "TRUE");

        final ArrayList<ArrayList> results = getMetaService().performCustomSearch(sql, getConnectionContext());

        final ArrayList<String> rows = new ArrayList<>();
        for (final ArrayList result : results) {
            final List<String> columns = new ArrayList<>(result.size());
            for (final Object object : result) {
                final String formattedResult;
                if (object == null) {
                    formattedResult = "";
                } else if ((object.getClass() == Double.class)
                            || (object.getClass() == Float.class)
                            || (object.getClass() == Long.class)
                            || (object.getClass() == Integer.class)
                            || (object.getClass() == Character.class)
                            || (object.getClass() == Boolean.class)) { // dont escape primitives
                    formattedResult = String.valueOf(object);
                } else if (escapeStrings) {                            // escape strings (and all complex
                                                                       // strings-representations)
                    final String potentiallyMissingQuotes = StringEscapeUtils.escapeCsv(object.toString());
                    formattedResult =
                        (!potentiallyMissingQuotes.startsWith("\"") && !object.toString().startsWith("\""))
                        ? String.format("\"%s\"", potentiallyMissingQuotes) : potentiallyMissingQuotes;
                } else {
                    formattedResult = object.toString();
                }
                columns.add(formattedResult);
            }
            rows.add(String.join(columnSeparator, columns));
        }

        return rows;
    }
}
