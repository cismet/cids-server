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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;
import com.vividsolutions.jump.io.ShapefileWriter;

import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.math.BigDecimal;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class JumpShapeWriter {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(JumpShapeWriter.class);
    private static final String DEFAULT_CPG_CONTENT = "UTF-8";
    private static final boolean DATE_AS_STRING = false;
    public static final String DEFAULT_GEOM_PROPERTY_NAME = "the_geom";

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new JumpShapeWriter object.
     */
    public JumpShapeWriter() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap        DOCUMENT ME!
     * @param   resultRows          DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     * @param   fileToSaveTo        DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void writeShape(final Map<String, Class> fieldTypeMap,
            final List<Map> resultRows,
            final List<String[]> aliasAttributeList,
            final File fileToSaveTo) throws Exception {
        try {
            writeShpFile(fieldTypeMap, resultRows, fileToSaveTo);
        } catch (InterruptedException e) {
            clear(fileToSaveTo);
            return;
        } catch (IllegalParametersException e) {
            clear(fileToSaveTo);
            throw e;
        }

        if (Thread.interrupted()) {
            clear(fileToSaveTo);
            return;
        }
        writePrjFile(fileToSaveTo);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fileToSaveTo  DOCUMENT ME!
     */
    private void clear(final File fileToSaveTo) {
        String fileNameWithoutExt = fileToSaveTo.getAbsolutePath();

        if (fileToSaveTo.getAbsolutePath().contains(".")) {
            fileNameWithoutExt = fileToSaveTo.getAbsolutePath()
                        .substring(0, fileToSaveTo.getAbsolutePath().lastIndexOf("."));
        }

        String fileName = fileNameWithoutExt + ".shp";

        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".shx";
        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".prj";
        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".dbf";
        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".cpg";
        deleteFileIfExists(fileName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap        DOCUMENT ME!
     * @param   resultRows          DOCUMENT ME!
     * @param   aliasAttributeList  DOCUMENT ME!
     * @param   fileToSaveTo        DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void writeDbf(final Map<String, Class> fieldTypeMap,
            final List<Map> resultRows,
            final List<String[]> aliasAttributeList,
            final File fileToSaveTo) throws Exception {
        File tmpFile = fileToSaveTo;
        if (fileToSaveTo.getName().contains(".")
                    && fileToSaveTo.getName().substring(fileToSaveTo.getName().lastIndexOf(".")).equalsIgnoreCase(
                        ".dbf")) {
            tmpFile = new File(fileToSaveTo.getParent(),
                    fileToSaveTo.getName().substring(0, fileToSaveTo.getName().lastIndexOf("."))
                            + ".shp");
        } else if (!fileToSaveTo.getAbsolutePath().contains(".")) {
            tmpFile = new File(fileToSaveTo.getParent(),
                    fileToSaveTo.getName()
                            + ".shp");
        }
        writeShape(fieldTypeMap, resultRows, aliasAttributeList, tmpFile);

        if (Thread.interrupted()) {
            clear(fileToSaveTo);
            return;
        }
        String fileNameWithoutExt = fileToSaveTo.getAbsolutePath();

        if (fileToSaveTo.getAbsolutePath().contains(".")) {
            fileNameWithoutExt = fileToSaveTo.getAbsolutePath()
                        .substring(0, fileToSaveTo.getAbsolutePath().lastIndexOf("."));
        }

        String fileName = fileNameWithoutExt + ".shp";

        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".shx";
        deleteFileIfExists(fileName);
        fileName = fileNameWithoutExt + ".prj";
        deleteFileIfExists(fileName);

        if (Thread.interrupted()) {
            clear(fileToSaveTo);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap  features DOCUMENT ME!
     * @param   resultRows    aliasAttributeList DOCUMENT ME!
     * @param   file          DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void writeShpFile(final Map<String, Class> fieldTypeMap, final List<Map> resultRows,
            final File file) throws Exception {
        writeShpFile(fieldTypeMap, resultRows, file, DEFAULT_CPG_CONTENT);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap  features DOCUMENT ME!
     * @param   resultRows    aliasAttributeList DOCUMENT ME!
     * @param   file          DOCUMENT ME!
     * @param   charset       DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void writeShpFile(final Map<String, Class> fieldTypeMap,
            final List<Map> resultRows,
            final File file,
            final String charset) throws Exception {
        final ShapefileWriter writer = new ShapefileWriter();
        List<Feature> basicFeatures = cidsFeatures2BasicFeature(fieldTypeMap, resultRows);
        final FeatureSchema schema = basicFeatures.get(0).getSchema();
        final FeatureDataset set = new FeatureDataset(basicFeatures, schema);
        basicFeatures = null;
        final DriverProperties properties = new DriverProperties();

        // charset property can also be defined
        properties.set(ShapefileWriter.FILE_PROPERTY_KEY, file.getAbsolutePath());
        properties.set(ShapefileWriter.SHAPE_TYPE_PROPERTY_KEY, "xy");

        if (charset == null) {
            properties.set("charset", DEFAULT_CPG_CONTENT);
        } else {
            properties.set("charset", charset);
        }

        writer.write(set, properties);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  file  DOCUMENT ME!
     */
    private void writePrjFile(final File file) {
        try {
            String fileWithoutExtension = file.getAbsolutePath();

            if (fileWithoutExtension.contains(".")) {
                fileWithoutExtension = fileWithoutExtension.substring(0, fileWithoutExtension.lastIndexOf("."));
            }
            final String crsDefinition =
                "PROJCS[\"ETRS_1989_UTM_Zone_32N\",GEOGCS[\"GCS_ETRS_1989\",DATUM[\"D_ETRS_1989\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",9.0],PARAMETER[\"Scale_Factor\",0.9996],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]";

            final BufferedWriter bw = new BufferedWriter(new FileWriter(fileWithoutExtension + ".prj"));
            bw.write(crsDefinition);
            bw.close();
        } catch (Exception e) {
            LOG.error("Error while writing prj file");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap  features DOCUMENT ME!
     * @param   resultRows    aliasAttributeList DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<Feature> cidsFeatures2BasicFeature(final Map<String, Class> fieldTypeMap, final List<Map> resultRows) {
        final List<Feature> featureList = new ArrayList<>(resultRows.size());
        final FeatureSchema schema = createScheme(fieldTypeMap);

        for (final Map f : resultRows) {
            final Feature bf = new BasicFeature(schema);

            for (final String name : fieldTypeMap.keySet()) {
                Object value = f.get(name);

                if (value instanceof Boolean) {
                    value = String.valueOf(value);
                } else if (DATE_AS_STRING && ((value instanceof Timestamp) || (value instanceof Date))) {
                    value = String.valueOf(value);
                }

                if ((schema.getAttributeType(name) == AttributeType.DOUBLE) && (value instanceof Integer)) {
                    value = ((Integer)value).doubleValue();
                }

                bf.setAttribute(name, value);
            }

            featureList.add(bf);
        }

        return featureList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap  attributes DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private FeatureSchema createScheme(final Map<String, Class> fieldTypeMap) {
        List<String> attributeNames = null;
        final FeatureSchema schema = new FeatureSchema();

        attributeNames = generateAttributeList(fieldTypeMap);

        for (final String name : fieldTypeMap.keySet()) {
            schema.addAttribute(name, getPropertyType(fieldTypeMap.get(name)));
        }

        return schema;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldTypeMap  attributeMap features DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private List<String> generateAttributeList(final Map<String, Class> fieldTypeMap) {
        final List<String> aliasAttrList = new ArrayList<>();

        for (final String key : fieldTypeMap.keySet()) {
            final String aliasAttr = key;
            aliasAttrList.add(aliasAttr);
        }

        return aliasAttrList;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private AttributeType getPropertyType(final Class attr) {
        if ((attr != null)) {
            final Class cl = attr;

            if (Geometry.class.isAssignableFrom(cl)) {
                return AttributeType.GEOMETRY;
            } else if (cl.equals(String.class)) {
                return AttributeType.STRING;
            } else if (cl.equals(Integer.class)) {
                return AttributeType.INTEGER;
            } else if (cl.equals(Long.class)) {
                return AttributeType.INTEGER;
            } else if (cl.equals(Double.class)) {
                return AttributeType.DOUBLE;
            } else if (cl.equals(Date.class)) {
                if (DATE_AS_STRING) {
                    return AttributeType.STRING;
                } else {
                    return AttributeType.DATE;
                }
            } else if (cl.equals(Boolean.class)) {
                return AttributeType.INTEGER;
            } else if (cl.equals(BigDecimal.class)) {
                return AttributeType.OBJECT;
            } else {
                return AttributeType.STRING;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fileName  DOCUMENT ME!
     */
    private void deleteFileIfExists(final String fileName) {
        final File fileToDelete = new File(fileName);

        if (fileToDelete.exists()) {
            fileToDelete.delete();
        }
    }
}
