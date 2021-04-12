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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        CHARSET
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

        if (params != null) {
            for (final ServerActionParameter sap : params) {
                if (sap.getKey().equals(ParameterType.DISTINCT_ON.toString())) {
                    distinctOn = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.WHERE.toString())) {
                    whereCause = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.BOOLEAN_YES.toString())) {
                    booleanYes = (String)sap.getValue();
                } else if (sap.getKey().equals(ParameterType.BOOLEAN_YES.toString())) {
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

            final List<String> rows = new ArrayList<>();
            if (columnNames != null) {
                rows.add(String.join(columnSeparator, columnNames));
            }
            rows.addAll(createRows(
                    metaClass,
                    fields,
                    whereCause,
                    distinctOn,
                    booleanYes,
                    booleanNo,
                    dateFormat,
                    columnSeparator));
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
     * @param   whereCause       DOCUMENT ME!
     * @param   distinctOn       DOCUMENT ME!
     * @param   booleanYes       DOCUMENT ME!
     * @param   booleanNo        DOCUMENT ME!
     * @param   dateFormat       DOCUMENT ME!
     * @param   columnSeparator  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private List<String> createRows(final MetaClass metaClass,
            final List<String> fields,
            final String whereCause,
            final String distinctOn,
            final String booleanYes,
            final String booleanNo,
            final String dateFormat,
            final String columnSeparator) throws Exception {
        final MetaObject moDummy = metaClass.getEmptyInstance(getConnectionContext());

        final List<String> formattedFields = new ArrayList<>(fields.size());
        for (final String field : fields) {
            final String formattedField;
            if (moDummy.getAttributeByFieldName(field) != null) {
                final String javaClassName = moDummy.getAttributeByFieldName(field).getMai().getJavaclassname();
                final Class javaClass = Class.forName(javaClassName);

                if (Date.class.isAssignableFrom(javaClass)) {
                    formattedField = String.format("to_char(%s, '%s')", field, dateFormat);
                } else if (Boolean.class.isAssignableFrom(javaClass)) {
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
                (whereCause != null) ? whereCause : "TRUE");

        final ArrayList<ArrayList> results = getMetaService().performCustomSearch(sql, getConnectionContext());

        final ArrayList<String> rows = new ArrayList<>();
        for (final ArrayList result : results) {
            final List<String> columns = new ArrayList<>(result.size());
            for (final Object object : result) {
                columns.add((object == null) ? "" : String.format("\"%s\"", object.toString()));
            }
            rows.add(String.join(columnSeparator, columns));
        }

        return rows;
    }
}
