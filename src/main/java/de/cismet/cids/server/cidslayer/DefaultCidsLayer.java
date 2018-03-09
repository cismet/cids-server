/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.cidslayer;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaClass;
import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class DefaultCidsLayer implements CidsLayerInfo, Serializable, ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(DefaultCidsLayer.class);

    //~ Instance fields --------------------------------------------------------

    protected Map<String, String> catalogueNameMap = null;

    protected final MetaClass mc;
    protected final Map<String, Boolean> primitiveTypes = new HashMap<>();
    protected final Map<String, Integer> catalogueTypes = new HashMap<>();
    protected final Map<String, StationInfo> stationTypes = new HashMap<>();
    protected final Map<String, Integer> referencedClass = new HashMap<>();
    private String sqlGeoField;
    private String geoField;
    private String selectionString;
    private String[] columnNames;
    private String[] sqlColumnNames;
    private String[] columnPropertyNames;
    private String[] primitiveColumnTypes;
    private String additionalJoins = null;
    private final String domain;
    
    private ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultCidsLayer object.
     *
     * @param  mc  DOCUMENT ME!
     */
    public DefaultCidsLayer(final MetaClass mc) {
        this(mc, (String)null);
    }

    /**
     * Creates a new DefaultCidsLayer object.
     *
     * @param  mc                DOCUMENT ME!
     * @param  catalogueNameMap  DOCUMENT ME!
     */
    public DefaultCidsLayer(final MetaClass mc, final Map<String, String> catalogueNameMap) {
        this(mc.getDomain(), mc, catalogueNameMap, null);
    }

    /**
     * Creates a new DefaultCidsLayer object.
     *
     * @param  mc               DOCUMENT ME!
     * @param  additionalJoins  DOCUMENT ME!
     */
    public DefaultCidsLayer(final MetaClass mc, final String additionalJoins) {
        this(mc.getDomain(), mc, null, additionalJoins);
    }

    /**
     * Creates a new DefaultCidsLayer object.
     *
     * @param  domain            DOCUMENT ME!
     * @param  mc                DOCUMENT ME!
     * @param  catalogueNameMap  DOCUMENT ME!
     * @param  additionalJoins   DOCUMENT ME!
     */
    public DefaultCidsLayer(final String domain,
            final MetaClass mc,
            final Map<String, String> catalogueNameMap,
            final String additionalJoins) {
        this.domain = domain;
        this.mc = mc;
        this.catalogueNameMap = catalogueNameMap;
        this.additionalJoins = additionalJoins;

        init(mc);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  mc  DOCUMENT ME!
     */
    private void init(final MetaClass mc) {
        final HashMap attrMap = mc.getMemberAttributeInfos();
        final List<String> sb = new ArrayList<>();
        final StringBuilder joins = new StringBuilder();
        boolean firstAttr = true;
        final HashMap allClasses = mc.getEmptyInstance(getConnectionContext()).getAllClasses();
        final List<String> columnNamesList = new ArrayList<>();
        final List<String> sqlColumnNamesList = new ArrayList<>();
        final List<String> columnPropertyNamesList = new ArrayList<>();
        final List<String> primitiveColumnTypesList = new ArrayList<>();

        for (final Object key : attrMap.keySet()) {
            final MemberAttributeInfo attr = (MemberAttributeInfo)attrMap.get(key);

            if (!attr.getName().equalsIgnoreCase("id") && !attr.isVisible()) {
                continue;
            }

            if (!firstAttr) {
                final String tmp = sb.remove(sb.size() - 1);
                sb.add(tmp + ",");
            } else {
                firstAttr = false;
            }

            if (attr.isForeignKey()
                        && getBeanClassName(allClasses, attr.getForeignKeyClassId()).toLowerCase().endsWith("geom")) {
                sqlGeoField = "geo_field";
                geoField = attr.getName();
                sb.add("st_asBinary(geom.geo_field) as " + geoField);
                columnNamesList.add(attr.getName());
                sqlColumnNamesList.add("geom.geo_field");
                columnPropertyNamesList.add(attr.getName() + ".geo_field");
                joins.append(" join geom on (").append(attr.getFieldName()).append(" = geom.id)");
                primitiveColumnTypesList.add("Geometry");
                referencedClass.put(attr.getName(), attr.getForeignKeyClassId());
            } else if (attr.isForeignKey()) {
                if (!(attr.isForeignKey()
                                && getBeanClassName(allClasses, attr.getForeignKeyClassId()).toLowerCase().endsWith(
                                    "geom"))) {
                    final MetaClass foreignClass = getBeanClass(allClasses, attr.getForeignKeyClassId());
                    try {
                        final String methodName = "handle" + attr.getName().substring(0, 1).toUpperCase()
                                    + attr.getName().substring(1);
                        final Method m = this.getClass()
                                    .getMethod(
                                        methodName,
                                        MemberAttributeInfo.class,
                                        MetaClass.class,
                                        StringBuilder.class,
                                        List.class,
                                        List.class,
                                        List.class,
                                        List.class,
                                        StringBuilder.class);
                        m.invoke(
                            this,
                            attr,
                            foreignClass,
                            sb,
                            columnNamesList,
                            columnPropertyNamesList,
                            sqlColumnNamesList,
                            primitiveColumnTypesList,
                            joins);
                    } catch (final NoSuchMethodException ex) {
                        handleCatalogue(
                            attr,
                            foreignClass,
                            sb,
                            columnNamesList,
                            columnPropertyNamesList,
                            sqlColumnNamesList,
                            primitiveColumnTypesList,
                            joins);
                    } catch (final Exception ex) {
                        LOG.error(ex, ex);
                    }
                } else {
                    final String tmp = sb.remove(sb.size() - 1);
                    sb.add(tmp.substring(0, tmp.length() - 1));
                }
            } else {
                sb.add(mc.getTableName() + "." + attr.getFieldName());
                columnNamesList.add(attr.getName());
                sqlColumnNamesList.add(mc.getTableName() + "." + attr.getFieldName());
                columnPropertyNamesList.add(attr.getName());
                primitiveTypes.put(columnNamesList.get(columnNamesList.size() - 1), Boolean.TRUE);
                primitiveColumnTypesList.add(attr.getJavaclassname());
            }
        }

        sb.add(" from " + mc.getTableName());
        sb.add(joins.toString());
        if (additionalJoins != null) {
            sb.add(additionalJoins);
        }

        selectionString = "Select ";

        for (final String tmp : sb) {
            if (selectionString == null) {
                selectionString = tmp;
            } else {
                selectionString += tmp;
            }
        }

        columnNames = columnNamesList.toArray(new String[columnNamesList.size()]);
        sqlColumnNames = sqlColumnNamesList.toArray(new String[sqlColumnNamesList.size()]);
        columnPropertyNames = columnPropertyNamesList.toArray(new String[columnPropertyNamesList.size()]);
        primitiveColumnTypes = primitiveColumnTypesList.toArray(new String[primitiveColumnTypesList.size()]);
    }

    /**
     * Adds the given catalogue.
     *
     * @param  attr                      DOCUMENT ME!
     * @param  foreignClass              DOCUMENT ME!
     * @param  sb                        DOCUMENT ME!
     * @param  columnNamesList           DOCUMENT ME!
     * @param  columnPropertyNamesList   DOCUMENT ME!
     * @param  sqlColumnNamesList        DOCUMENT ME!
     * @param  primitiveColumnTypesList  DOCUMENT ME!
     * @param  joins                     DOCUMENT ME!
     */
    protected void handleCatalogue(final MemberAttributeInfo attr,
            final MetaClass foreignClass,
            final List<String> sb,
            final List<String> columnNamesList,
            final List<String> columnPropertyNamesList,
            final List<String> sqlColumnNamesList,
            final List<String> primitiveColumnTypesList,
            final StringBuilder joins) {
        String namePropertyName = "name";

        if ((catalogueNameMap != null) && (catalogueNameMap.get(attr.getName()) != null)) {
            namePropertyName = catalogueNameMap.get(attr.getName());
        }
        final ObjectAttribute nameAttr = (ObjectAttribute)foreignClass.getEmptyInstance(getConnectionContext())
                    .getAttribute(namePropertyName);

        if (nameAttr != null) {
            final String alias = addLeftJoin(
                    joins,
                    foreignClass.getTableName(),
                    mc.getTableName()
                            + "."
                            + attr.getFieldName(),
                    "id");
            sb.add(alias + "." + nameAttr.getMai().getFieldName());
            columnNamesList.add(attr.getName());
            sqlColumnNamesList.add(alias + "." + nameAttr.getMai().getFieldName());
            columnPropertyNamesList.add(attr.getName());
            catalogueTypes.put(columnNamesList.get(columnNamesList.size() - 1), attr.getForeignKeyClassId());
            primitiveColumnTypesList.add(nameAttr.getMai().getJavaclassname());
        } else {
            sb.add(mc.getTableName() + "." + attr.getFieldName());
            columnNamesList.add(attr.getName());
            sqlColumnNamesList.add(mc.getTableName() + "." + attr.getFieldName());
            columnPropertyNamesList.add(attr.getName() + ".name");
            catalogueTypes.put(columnNamesList.get(columnNamesList.size() - 1), attr.getForeignKeyClassId());
            primitiveColumnTypesList.add("java.lang.String");
            // primitiveColumnTypesList.add(attr.getJavaclassname());
        }
    }

    /**
     * Adds a left join to the given joins StringBuilder.
     *
     * @param   joins              DOCUMENT ME!
     * @param   table              the table to join
     * @param   onClauseLeftSide   left side of the join expression
     * @param   onClauseRightSide  right side of the join expression
     *
     * @return  DOCUMENT ME!
     */
    protected String addLeftJoin(final StringBuilder joins,
            final String table,
            final String onClauseLeftSide,
            final String onClauseRightSide) {
        String alias = "";
        int counter = 0;

        do {
            alias = table.replace('.', 'P') + (++counter);
        } while (joins.indexOf(alias) != -1);

        joins.append(" left join ")
                .append(table)
                .append(" ")
                .append(alias)
                .append(" on (")
                .append(onClauseLeftSide)
                .append(" = ")
                .append(alias)
                .append(".")
                .append(onClauseRightSide)
                .append(")");

        return alias;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   allClasses  DOCUMENT ME!
     * @param   classId     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected String getBeanClassName(final HashMap<String, MetaClass> allClasses, final int classId) {
        return allClasses.get(domain + classId).toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   allClasses  DOCUMENT ME!
     * @param   classId     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected MetaClass getBeanClass(final HashMap<String, MetaClass> allClasses, final int classId) {
        return allClasses.get(domain + classId);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   allClasses  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected MetaClass getGeomClass(final HashMap<String, MetaClass> allClasses) {
        for (final MetaClass cl : allClasses.values()) {
            if (cl.getName().toLowerCase().equals("geom")) {
                return cl;
            }
        }

        return null;
    }

    @Override
    public String getIdField() {
        return "id";
    }

    @Override
    public String getSqlGeoField() {
        return sqlGeoField;
    }

    @Override
    public String getGeoField() {
        return geoField;
    }

    @Override
    public String getSelectString() {
        return selectionString;
    }

    @Override
    public String[] getColumnNames() {
        return columnNames;
    }

    @Override
    public String[] getPrimitiveColumnTypes() {
        return primitiveColumnTypes;
    }

    @Override
    public boolean isPrimitive(final String column) {
        final Boolean primitive = primitiveTypes.get(column);

        return ((primitive != null) ? primitive.booleanValue() : false);
    }

    @Override
    public boolean isCatalogue(final String column) {
        final Integer catalogue = catalogueTypes.get(column);

        return catalogue != null;
    }

    @Override
    public Integer getCatalogueClass(final String column) {
        final Integer catalogueClass = catalogueTypes.get(column);

        return catalogueClass;
    }

    @Override
    public boolean isStation(final String column) {
        final StationInfo stationInfo = stationTypes.get(column);

        return stationInfo != null;
    }

    @Override
    public StationInfo getStationInfo(final String column) {
        final StationInfo stationInfo = stationTypes.get(column);

        return stationInfo;
    }

    @Override
    public String[] getColumnPropertyNames() {
        return columnPropertyNames;
    }

    @Override
    public int getReferencedCidsClass(final String column) {
        return referencedClass.get(column);
    }

    @Override
    public boolean isReferenceToCidsClass(final String column) {
        final Integer classId = referencedClass.get(column);

        return classId != null;
    }

    @Override
    public String[] getSqlColumnNames() {
        return sqlColumnNames;
    }

    @Override
    public String getRestriction() {
        return null;
    }

    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public void initWithConnectionContext(ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }
        
}
