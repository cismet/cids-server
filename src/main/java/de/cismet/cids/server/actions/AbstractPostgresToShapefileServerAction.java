/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.actions;

import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.newuser.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.apache.log4j.Logger;

import org.deegree.datatypes.QualifiedName;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.shpapi.shape_new.ShapeFile;
import org.deegree.io.shpapi.shape_new.ShapeFileWriter;
import org.deegree.model.feature.AbstractFeatureCollection;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.feature.schema.SimplePropertyType;
import org.deegree.model.spatialschema.JTSAdapter;
import org.deegree.ogcbase.PropertyPath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Files;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Types;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.cismet.cismap.commons.jtsgeometryfactories.PostGisGeometryFactory;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public abstract class AbstractPostgresToShapefileServerAction implements ConnectionContextStore,
    UserAwareServerAction,
    MetaServiceStore {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOG = Logger.getLogger(AbstractPostgresToShapefileServerAction.class);

    //~ Instance fields --------------------------------------------------------

    private ConnectionContext connectionContext = ConnectionContext.createDummy();
    private User user;
    private MetaService metaService;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name           DOCUMENT ME!
     * @param   propertyClass  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected SimplePropertyType identifyPropertyType(final QualifiedName name, final Class propertyClass) {
        if (propertyClass != null) {
            if (propertyClass.isAssignableFrom(String.class)) {
                return new SimplePropertyType(name, org.deegree.datatypes.Types.VARCHAR, 0, 1);
            } else if (propertyClass.isAssignableFrom(Integer.class)) {
                return new SimplePropertyType(name, org.deegree.datatypes.Types.INTEGER, 0, 1);
            } else if (propertyClass.isAssignableFrom(Double.class)) {
                return new SimplePropertyType(name, org.deegree.datatypes.Types.DOUBLE, 0, 1);
            } else if (propertyClass.isAssignableFrom(Polygon.class)) {
                return new SimplePropertyType(name, org.deegree.datatypes.Types.SURFACE, 0, 1);
            } else if (propertyClass.isAssignableFrom(Point.class)) {
                return new SimplePropertyType(name, org.deegree.datatypes.Types.POINT, 0, 1);
            } else if (propertyClass.isAssignableFrom(Date.class)) {
                return new SimplePropertyType(name, org.deegree.datatypes.Types.VARCHAR, 0, 1);
            } else if (propertyClass.isAssignableFrom(Boolean.class)) {
                return new SimplePropertyType(name, org.deegree.datatypes.Types.INTEGER, 0, 1);
            } else {
                LOG.error("unknown type: " + propertyClass.getClass().getName());
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap  DOCUMENT ME!
     * @param   rows          DOCUMENT ME!
     * @param   rowId         propTypeArray DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Feature toDeegreeFeature(final Map<String, Class> fieldTypeMap,
            final Map<String, Object> rows,
            final String rowId) {
        final Map<String, PostgresFeatureProperty> propertyMap = new LinkedHashMap<>();

        for (final String rowKey : rows.keySet()) {
            propertyMap.put(
                rowKey,
                new PostgresFeatureProperty(rowKey, fieldTypeMap.get(rowKey), rows.get(rowKey), this));
        }

        final List<PropertyType> propertyTypes = new ArrayList<>();
        for (final PostgresFeatureProperty property : propertyMap.values()) {
            propertyTypes.add(property.getPropertyType());
        }

        final FeatureType ft = FeatureFactory.createFeatureType("", false, propertyTypes.toArray(new PropertyType[0]));
        return FeatureFactory.createFeature(String.valueOf(propertyMap.get(rowId)),
                ft,
                propertyMap.values().toArray(new PostgresFeatureProperty[0]));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap  DOCUMENT ME!
     * @param   resultRows    DOCUMENT ME!
     * @param   fileToSaveTo  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void writeToShp(final Map<String, Class> fieldTypeMap, final List<Map> resultRows, final File fileToSaveTo)
            throws Exception {
        final List<Feature> features = (resultRows != null) ? new ArrayList<Feature>(resultRows.size()) : null;
        if (features != null) {
            for (final Map resultRow : resultRows) {
                features.add(toDeegreeFeature(fieldTypeMap, resultRow, getRowId()));
            }
        }

        final FeatureCollection featureCollection = new PostgresFeatureCollection(fileToSaveTo.getName(), features);
        final ShapeFile shape = new ShapeFile(
                featureCollection,
                fileToSaveTo.getAbsolutePath().substring(0, fileToSaveTo.getAbsolutePath().lastIndexOf(".")));
        new ShapeFileWriter(shape).write();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getRowId();

    /**
     * DOCUMENT ME!
     *
     * @param   columnName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract Class<? extends Geometry> getGeometryClass(final String columnName);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract String getQuery();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public abstract File getTmpDir();

    /**
     * DOCUMENT ME!
     *
     * @param   colIndex  DOCUMENT ME!
     * @param   colName   DOCUMENT ME!
     * @param   type      DOCUMENT ME!
     * @param   typeName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Class identifyClass(final int colIndex, final String colName, final int type, final String typeName) {
        switch (type) {
            case Types.INTEGER: {
                return Integer.class;
            }
            case Types.FLOAT: {
                return Float.class;
            }
            case Types.DOUBLE: {
                return Double.class;
            }
            case Types.BOOLEAN: {
                return Boolean.class;
            }
            case Types.BIT: {
                return Boolean.class;
            }
            case Types.VARCHAR: {
                return String.class;
            }
            case Types.NVARCHAR: {
                return String.class;
            }
            case Types.DATE: {
                return Date.class;
            }
            default: {
                if ("geometry".equalsIgnoreCase(typeName)) {
                    return getGeometryClass(colName);
                } else {
                    return Object.class;
                }
            }
        }
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... saps) {
        try {
            final Map<String, Class> fieldTypeMap = new LinkedHashMap<>();
            final List<Map> rowsMap = new ArrayList<>();

            final Connection connection = ((DomainServerImpl)getMetaService()).getConnectionPool().getConnection();
            final PreparedStatement preparedStatement = connection.prepareStatement(getQuery());
            final ResultSet resultSet = preparedStatement.executeQuery();

            final ResultSetMetaData metaData = resultSet.getMetaData();
            for (int colIndex = 1; colIndex <= metaData.getColumnCount(); colIndex++) {
                final int colType = metaData.getColumnType(colIndex);
                final String colName = metaData.getColumnLabel(colIndex);
                final String colTypeName = metaData.getColumnTypeName(colIndex);

                fieldTypeMap.put(colName, identifyClass(colIndex, colName, colType, colTypeName));
            }

            while (resultSet.next()) {
                final Map<String, Object> rowMap = new LinkedHashMap<>();
                for (int colIndex = 1; colIndex <= metaData.getColumnCount(); colIndex++) {
                    rowMap.put(metaData.getColumnLabel(colIndex), resultSet.getObject(colIndex));
                }
                rowsMap.add(rowMap);
            }

            final String name = (body instanceof String) ? (String)body : String.valueOf(System.currentTimeMillis());
            final File shpFile = new File(getTmpDir(), String.format("%s.shp", name));
            final File shxFile = new File(getTmpDir(), String.format("%s.shx", name));
            final File dbfFile = new File(getTmpDir(), String.format("%s.dbf", name));
            final File zipFile = new File(getTmpDir(), String.format("%s.zip", name));
            writeToShp(fieldTypeMap, rowsMap, shpFile);
            try(final ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
                writeToZip(shpFile.getName(), new FileInputStream(shpFile), zipOut);
                writeToZip(shxFile.getName(), new FileInputStream(shxFile), zipOut);
                writeToZip(dbfFile.getName(), new FileInputStream(dbfFile), zipOut);
                zipOut.close();
            }
            try {
                shpFile.delete();
            } catch (final Exception ex) {
                LOG.warn(String.format("could not delete %s", shpFile.getName()), ex);
            }
            try {
                shxFile.delete();
            } catch (final Exception ex) {
                LOG.warn(String.format("could not delete %s", shxFile.getName()), ex);
            }
            try {
                dbfFile.delete();
            } catch (final Exception ex) {
                LOG.warn(String.format("could not delete %s", dbfFile.getName()), ex);
            }
            return Files.readAllBytes(zipFile.toPath());
        } catch (final Exception ex) {
            LOG.error(ex, ex);
            return ex;
        } finally {
            // todo zip delete
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fileName  DOCUMENT ME!
     * @param   in        DOCUMENT ME!
     * @param   zipOut    DOCUMENT ME!
     *
     * @throws  IOException  DOCUMENT ME!
     */
    private void writeToZip(final String fileName, final InputStream in, final ZipOutputStream zipOut)
            throws IOException {
        final byte[] buf = new byte[1024];
        int len;
        zipOut.putNextEntry(new ZipEntry(fileName));
        while ((len = in.read(buf)) > 0) {
            zipOut.write(buf, 0, len);
        }
        zipOut.flush();
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    @Override
    public MetaService getMetaService() {
        return metaService;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class PostgresFeatureCollection extends AbstractFeatureCollection {

        //~ Instance fields ----------------------------------------------------

        private final List<Feature> features;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PostgresFeatureCollection object.
         *
         * @param  id        DOCUMENT ME!
         * @param  features  DOCUMENT ME!
         */
        public PostgresFeatureCollection(final String id, final List<Feature> features) {
            super(id);
            this.features = features;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void clear() {
            features.clear();
        }

        @Override
        public Feature getFeature(final int i) {
            return features.get(i);
        }

        @Override
        public Feature getFeature(final String id) {
            for (final Feature feature : features) {
                if (feature.getId().equals(id)) {
                    return feature;
                }
            }
            return null;
        }

        @Override
        public Feature[] toArray() {
            return features.toArray(new Feature[0]);
        }

        @Override
        public Iterator<Feature> iterator() {
            return features.iterator();
        }

        @Override
        public int size() {
            return features.size();
        }

        @Override
        public FeatureProperty getDefaultProperty(final PropertyPath pp) throws PropertyPathResolvingException {
            LOG.info("method call ignored", new Exception());
            return null;
        }

        @Override
        public void setProperty(final FeatureProperty fp, final int i) {
            LOG.info("method call ignored", new Exception());
        }

        @Override
        public void addProperty(final FeatureProperty fp) {
            LOG.info("method call ignored", new Exception());
        }

        @Override
        public void removeProperty(final QualifiedName qn) {
            LOG.info("method call ignored", new Exception());
        }

        @Override
        public void replaceProperty(final FeatureProperty fp, final FeatureProperty fp1) {
            LOG.info("method call ignored", new Exception());
        }

        @Override
        public Feature cloneDeep() throws CloneNotSupportedException {
            LOG.info("method call ignored", new Exception());
            return null;
        }

        @Override
        public void add(final Feature ftr) {
            LOG.info("method call ignored", new Exception());
        }

        @Override
        public Feature remove(final Feature ftr) {
            LOG.info("method call ignored", new Exception());
            return null;
        }

        @Override
        public Feature remove(final int i) {
            LOG.info("method call ignored", new Exception());
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private class PostgresFeatureProperty implements FeatureProperty {

        //~ Instance fields ----------------------------------------------------

        private final QualifiedName name;
        private final PropertyType propertyType;
        private final Object value;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new PostgresFeatureProperty object.
         *
         * @param  name           DOCUMENT ME!
         * @param  propertyClass  DOCUMENT ME!
         * @param  value          DOCUMENT ME!
         * @param  action         DOCUMENT ME!
         */
        private PostgresFeatureProperty(final String name,
                final Class propertyClass,
                final Object value,
                final AbstractPostgresToShapefileServerAction action) {
            this.name = new QualifiedName(name);
            this.value = convertValue(value);
            this.propertyType = action.identifyPropertyType(this.name, propertyClass);
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   value  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        private Object convertValue(final Object value) {
            try {
                final PostGisGeometryFactory factory = new PostGisGeometryFactory();
                return JTSAdapter.wrap(factory.createGeometry(value));
            } catch (final Exception ex) {
                if (value instanceof Boolean) {
                    return (Boolean)value ? 1 : 0;
                } else if (value instanceof Date) {
                    return "";
                }
                return value;
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public PropertyType getPropertyType() {
            return propertyType;
        }

        @Override
        public QualifiedName getName() {
            return name;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Object getValue(final Object defaultValue) {
            if (value == null) {
                return defaultValue;
            }
            return value;
        }

        @Override
        public final void setValue(final Object value) {
        }

        @Override
        public String toString() {
            return String.format("name: %s\nvalue: %s\n", name, value);
        }

        @Override
        public Feature getOwner() {
            return null;
        }
    }
}
