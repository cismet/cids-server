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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import org.deegree.model.spatialschema.JTSAdapter;

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
public abstract class AbstractJumpPostgresToShapefileServerAction implements ConnectionContextStore,
    UserAwareServerAction,
    MetaServiceStore {

    //~ Static fields/initializers ---------------------------------------------

    protected static final Logger LOG = Logger.getLogger(AbstractJumpPostgresToShapefileServerAction.class);

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
     * @param   fieldTypeMap  DOCUMENT ME!
     * @param   resultRows    DOCUMENT ME!
     * @param   fileToSaveTo  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private void writeToShp(final Map<String, Class> fieldTypeMap, final List<Map> resultRows, final File fileToSaveTo)
            throws Exception {
        final JumpShapeWriter jsw = new JumpShapeWriter();
        jsw.writeShpFile(fieldTypeMap, resultRows, fileToSaveTo, getCharset());
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
    public String getWrtProjection() {
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getCharset() {
        return null;
    }

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
                    rowMap.put(metaData.getColumnLabel(colIndex), convertValue(resultSet.getObject(colIndex)));
                }
                rowsMap.add(rowMap);
            }

            final String name = (body instanceof String) ? (String)body : String.valueOf(System.currentTimeMillis());
            final File shpFile = new File(getTmpDir(), String.format("%s.shp", name));
            final File shxFile = new File(getTmpDir(), String.format("%s.shx", name));
            final File dbfFile = new File(getTmpDir(), String.format("%s.dbf", name));
            final File prjFile = new File(getTmpDir(), String.format("%s.prj", name));
            final File cpgFile = new File(getTmpDir(), String.format("%s.cpg", name));
            final File zipFile = new File(getTmpDir(), String.format("%s.zip", name));
            final String charset = getCharset();

            writeToShp(fieldTypeMap, rowsMap, shpFile);

            try(final ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
                writeToZip(shpFile.getName(), new FileInputStream(shpFile), zipOut);
                writeToZip(shxFile.getName(), new FileInputStream(shxFile), zipOut);
                writeToZip(dbfFile.getName(), new FileInputStream(dbfFile), zipOut);
                final String wrtProjection = getWrtProjection();
                if (wrtProjection != null) {
                    writeToZip(prjFile.getName(), IOUtils.toInputStream(wrtProjection, "UTF-8"), zipOut);
                }
                if (charset != null) {
                    writeToZip(cpgFile.getName(), IOUtils.toInputStream(charset, "UTF-8"), zipOut);
                }
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
            return ServerActionHelper.asyncByteArrayHelper(Files.readAllBytes(zipFile.toPath()), "shape.zip");
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
     * @param   value  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Object convertValue(final Object value) {
        try {
            final PostGisGeometryFactory factory = new PostGisGeometryFactory();
            return factory.createGeometry(value);
        } catch (final Exception ex) {
            if (value instanceof Boolean) {
                return (Boolean)value ? 1 : 0;
            }

            return value;
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
}
