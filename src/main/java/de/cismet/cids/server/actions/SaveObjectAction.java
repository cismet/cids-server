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

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.domainserver.MetaService;
import Sirius.server.middleware.interfaces.domainserver.MetaServiceStore;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import Sirius.server.sql.PreparableStatement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.spatialschema.JTSAdapter;

import org.wololo.jts2geojson.GeoJSONReader;
import org.wololo.jts2geojson.GeoJSONWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;

import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.utils.serverresources.GeneralServerResources;
import de.cismet.cids.utils.serverresources.ServerResourcesLoader;

import de.cismet.commons.security.WebDavClient;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ServerAction.class)
public class SaveObjectAction implements ServerAction, MetaServiceStore, UserAwareServerAction {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(SaveObjectAction.class);
    private static final ConnectionContext CC = ConnectionContext.create(
            ConnectionContext.Category.ACTION,
            "SaveObjectAction");
    private static final String EMPTY_FEATURE_COLLECTION_OBJECT = "{\"type\": \"FeatureCollection\", \"features\": []}";

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        className, data, complete, geometry
    }

    //~ Instance fields --------------------------------------------------------

    private final Map<String, String> SOURCE_LAYER_MAPPING = new HashMap<>();
    private final Map<String, String[]> FEATURE_ATTRIBUTES = new HashMap<>();
    private final Map<String, String> GEOMETRY_MAPPING = new HashMap<>();
    private final List<RefreshConfig> REFRESH_CONFIGS = new ArrayList<>();

    private int lastNegativeId = -1;

    private MetaService ms;
    private User user;
    private final ObjectMapper mapper = new ObjectMapper();
    private String webdav;
    private String webdavUser;
    private String webdavPwd;
    private String brandNewFeaturesPath;
    private String brandNewFeaturesDigestPath;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SaveObjectAction object.
     */
    public SaveObjectAction() {
        try {
            final Iterator<Map.Entry<String, JsonNode>> propIt = ServerResourcesLoader.getInstance()
                        .loadJson(GeneralServerResources.NEW_FEATURE_COLLECTION_JSON.getValue());
            GEOMETRY_MAPPING.clear();
            FEATURE_ATTRIBUTES.clear();
            SOURCE_LAYER_MAPPING.clear();
            REFRESH_CONFIGS.clear();

            while (propIt.hasNext()) {
                final Map.Entry<String, JsonNode> content = propIt.next();

                final String key = content.getKey();
                final JsonNode value = content.getValue();

                if (key.equalsIgnoreCase("$webdav")) {
                    brandNewFeaturesPath = value.get("brandNewFeaturesPath").asText();
                    brandNewFeaturesDigestPath = value.get("brandNewFeaturesDigestPath").asText();
                    webdav = value.get("webdav").asText();
                    webdavUser = value.get("webdavUser").asText();
                    webdavPwd = value.get("webdavPwd").asText();
                } else if (key.equalsIgnoreCase("$refreshConfig")) {
                    if (value instanceof ArrayNode) {
                        for (final JsonNode element : (ArrayNode)value) {
                            final String idProperty = element.get("idProperty").asText();
                            final String sourceClassname = element.get("sourceClassname").asText();
                            final String targetClassname = element.get("targetClassname").asText();
                            final String query = element.get("query").asText();

                            final RefreshConfig config = new RefreshConfig(
                                    idProperty,
                                    sourceClassname,
                                    targetClassname,
                                    query);
                            REFRESH_CONFIGS.add(config);
                        }
                    }
                } else {
                    GEOMETRY_MAPPING.put(key, value.get("geometry").asText());

                    final JsonNode props = value.get("properties");
                    final List<String> propsList = new ArrayList<>();

                    if (props instanceof ArrayNode) {
                        final ArrayNode propsArray = (ArrayNode)props;

                        for (final JsonNode element : propsArray) {
                            if (element.isTextual()) {
                                propsList.add(element.asText());
                            }
                        }
                    }

                    if ((propsList != null) && !propsList.isEmpty()) {
                        FEATURE_ATTRIBUTES.put(key, propsList.toArray(new String[propsList.size()]));
                    }

                    SOURCE_LAYER_MAPPING.put(key, value.get("source_layer").asText());
                }
            }
        } catch (Exception e) {
            LOG.warn("Cannot read new feature collection server resource", e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public void setUser(final User user) {
        this.user = user;
    }

    @Override
    public void setMetaService(final MetaService ms) {
        this.ms = ms;
    }

    @Override
    public MetaService getMetaService() {
        return this.ms;
    }

    @Override
    public String getTaskName() {
        return "SaveObject";
    }

    @Override
    public Object execute(final Object body, final ServerActionParameter... params) {
        String className = null;
        String value = null;
        CidsBean beanToSave = null;
        Boolean complete = true;
        JsonNode rootNode = null;
        boolean isNewObject = false;
        String geometry = null;

        for (final ServerActionParameter sap : params) {
            if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.className.toString())) {
                className = (String)sap.getValue();

                if (className.startsWith("vzk_")) {
                    complete = false;
                }
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.data.toString())) {
                value = (String)sap.getValue();
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.complete.toString())) {
                complete = (sap.getValue().toString()).equalsIgnoreCase("true");
            } else if (sap.getKey().equalsIgnoreCase(PARAMETER_TYPE.geometry.toString())) {
                geometry = (String)sap.getValue();
            }
        }

        try {
            if ((className == null) || (value == null)) {
                LOG.error("Error in DataQuisitionAction: No view specified");
                return "{\"Exception\": \"parameters className and data must be set.\"}";
            }

            // Determine table/view to use and check permissions
            final DomainServerImpl domainServer = (DomainServerImpl)ms;
            final MetaClass[] classes = domainServer.getClasses(user, CC);
            final MetaClass clazz = getClassByTableName(classes, className);
            rootNode = mapper.readTree(value);

            if (clazz == null) {
                return "{\"Exception\": \"Class with table name " + className + " not found.\"}";
            }

            beanToSave = createCidsBeanFromJson(classes, rootNode, className, null, complete);
            // cannot use beanToSave.persist(), because the server does not contain an implementation of
            // CidsBeanPersistService
            MetaObject metaObject = beanToSave.getMetaObject();
            System.out.println(beanToSave.getMetaObject().getDebugString());
            if (metaObject.getStatus() == MetaObject.MODIFIED) {
                domainServer.updateMetaObject(user, metaObject, CC);

                metaObject = domainServer.getMetaObject(user, metaObject.getID(), metaObject.getClassID(), CC);
            } else if (metaObject.getStatus() == MetaObject.NEW) {
                metaObject = domainServer.insertMetaObject(user, metaObject, CC);
                isNewObject = true;
            }

            beanToSave = metaObject.getBean();
        } catch (JsonProcessingException e) {
            // todo fehlerbehandlung
            LOG.error("Error while extracting the data sources", e);
            return "{\"Exception\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            LOG.error("Error while saving object", e);
            return "{\"Exception\": \"" + e.getMessage() + "\"}";
        }

        final boolean brandNewFeatureEnabled = isBrandNewFeature(className, rootNode);

        if (brandNewFeatureEnabled && (rootNode instanceof ObjectNode)) {
            if (isNewObject) {
                final SyncBrandNewFeatures thread = new SyncBrandNewFeatures(
                        SOURCE_LAYER_MAPPING,
                        FEATURE_ATTRIBUTES,
                        GEOMETRY_MAPPING,
                        REFRESH_CONFIGS,
                        ms,
                        user,
                        webdav,
                        webdavUser,
                        webdavPwd,
                        brandNewFeaturesPath,
                        brandNewFeaturesDigestPath,
                        className,
                        rootNode,
                        beanToSave,
                        geometry,
                        true);
                thread.start();
            } else {
                final SyncBrandNewFeatures thread = new SyncBrandNewFeatures(
                        SOURCE_LAYER_MAPPING,
                        FEATURE_ATTRIBUTES,
                        GEOMETRY_MAPPING,
                        REFRESH_CONFIGS,
                        ms,
                        user,
                        webdav,
                        webdavUser,
                        webdavPwd,
                        brandNewFeaturesPath,
                        brandNewFeaturesDigestPath,
                        className,
                        rootNode,
                        beanToSave,
                        geometry,
                        false);
                thread.start();
            }
        }

        return "{\"id\": \"" + String.valueOf(beanToSave.getProperty("id")) + "\"}";
    }

    /**
     * DOCUMENT ME!
     *
     * @param   className  DOCUMENT ME!
     * @param   rootNode   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isBrandNewFeature(final String className, final JsonNode rootNode) {
        return SOURCE_LAYER_MAPPING.get(className) != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes              DOCUMENT ME!
     * @param   rootNode             DOCUMENT ME!
     * @param   className            DOCUMENT ME!
     * @param   possibleObjectsList  DOCUMENT ME!
     * @param   complete             DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private CidsBean createCidsBeanFromJson(final MetaClass[] classes,
            JsonNode rootNode,
            String className,
            final List<CidsBean> possibleObjectsList,
            final boolean complete) throws Exception {
        Integer nodeId = ((rootNode.get("id") != null) ? rootNode.get("id").asInt() : null);
        CidsBean bean = null;
        final String uuid = ((rootNode.get("uuid") != null) ? rootNode.get("uuid").asText() : null);

        if ((nodeId == null) && (uuid == null)) {
            if (getClassByTableName(classes, className).isArrayElementLink()) {
                // get reference
                for (final Object fieldName
                            : getClassByTableName(classes, className).getMemberAttributeInfos().keySet()) {
                    final MemberAttributeInfo mai = (MemberAttributeInfo)getClassByTableName(classes, className)
                                .getMemberAttributeInfos().get(fieldName);

                    if (mai.isForeignKey()) {
                        final JsonNode subNode = rootNode.get(getClassById(classes, mai.getForeignKeyClassId())
                                        .getTableName());

                        if (subNode != null) {
                            nodeId = subNode.get("id").asInt();
                        }

                        rootNode = subNode;
                        className = getClassById(classes, mai.getForeignKeyClassId()).getTableName();
                        break;
                    }
                }
            }
        }

        bean = getBeanFromList(possibleObjectsList, nodeId);

        if (bean == null) {
            if (((nodeId == null) || (nodeId < 0)) && (uuid == null)) {
                bean = getClassByTableName(classes, className).getEmptyInstance(CC).getBean();
                bean.setProperty("id", --lastNegativeId);
            } else {
                if ((nodeId != null) && (nodeId >= 0)) {
                    final MetaObject mo = ms.getMetaObject(
                            user,
                            nodeId,
                            getClassByTableName(classes, className).getID(),
                            CC);

                    if (mo != null) {
                        bean = mo.getBean();
                    } else {
                        bean = getClassByTableName(classes, className).getEmptyInstance(CC).getBean();
                        bean.setProperty("id", --lastNegativeId);
                    }
                } else if (uuid != null) {
                    final PreparableStatement ps = new PreparableStatement("select id from " + className
                                    + " where uuid = ?",
                            new int[] { Types.VARCHAR });
                    ps.setObjects(uuid);
                    final ArrayList<ArrayList> list = ms.performCustomSearch(ps, CC);

                    MetaObject mo = null;

                    if ((list != null) && (list.size() > 0) && (list.get(0).size() > 0)
                                && (list.get(0).get(0) != null)) {
                        nodeId = (Integer)list.get(0).get(0);
                        mo = ms.getMetaObject(user, nodeId, getClassByTableName(classes, className).getID(), CC);
                    }

                    if (mo != null) {
                        bean = mo.getBean();
                    } else {
                        bean = getClassByTableName(classes, className).getEmptyInstance(CC).getBean();
                    }
                }
            }
        }

        if (bean == null) {
            return null;
        }

        final HashMap attrMap = bean.getMetaObject().getMetaClass().getMemberAttributeInfos();

        for (final Object key : new ArrayList(attrMap.keySet())) {
            final MemberAttributeInfo attribute = (MemberAttributeInfo)attrMap.get(key);
            String fieldname = attribute.getFieldName();
            boolean array = false;

            if (attribute.isForeignKey()) {
                final JsonNode node = rootNode.get(fieldname);

                if (node == null) {
                    // otherwise, use directly the field name frok the attribute
                    if ((attribute.getArrayKeyFieldName() != null) && !attribute.getArrayKeyFieldName().equals("")) {
                        fieldname = fieldname.toLowerCase() + "Array";
                        array = true;
                    } else if (attribute.getForeignKeyClassId() < 0) {
                        fieldname = getClassById(classes, attribute.getForeignKeyClassId()).getTableName()
                                    + "ArrayRelationShip";
                        array = true;
                    } else if (attribute.isArray()) {
                        fieldname = fieldname.toLowerCase() + "Array";
                        array = true;
                    } else {
                        fieldname = getFieldName(classes, attribute, attrMap);
                    }
                }
            } else if (attribute.isArray()) {
                fieldname = fieldname.toLowerCase() + "Array";
            }

            final JsonNode node = rootNode.get(fieldname);

            if (node != null) {
                if (attribute.isArray() || array) {
                    final Object colObj = bean.getProperty(attribute.getFieldName());

                    if (colObj instanceof Collection) {
                        final List<CidsBean> objList = (List<CidsBean>)colObj;
                        final List<CidsBean> newObjList = new ArrayList<>();

                        if (node.isArray()) {
                            final Iterator<JsonNode> it = node.iterator();

                            while (it.hasNext()) {
                                final JsonNode n = it.next();
                                final CidsBean b = createCidsBeanFromJson(
                                        classes,
                                        n,
                                        getClassById(classes, attribute.getForeignKeyClassId()).getTableName(),
                                        objList,
                                        complete);

                                newObjList.add(b);
                            }
                        }
                        // do not clean the objList list and then add the new beans, because this will lead to an
                        // error. (The old beans will be deleted)
                        for (final CidsBean b : new ArrayList<CidsBean>(objList)) {
                            if ((!complete && (getBeanFromList(newObjList, (Integer)b.getProperty("id")) == null))
                                        || (complete)) {
                                if (attribute.isArray()) {
                                    String referenceKey = null;

                                    if (getClassById(classes, attribute.getForeignKeyClassId()).getTableName()
                                                .equalsIgnoreCase(b.getMetaObject().getMetaClass().getTableName())) {
                                        referenceKey = getForeignAttributeFromArrayCLass(b.getMetaObject()
                                                        .getMetaClass());
                                    }
                                    if (referenceKey != null) {
                                        objList.remove((CidsBean)b.getProperty(referenceKey));
                                    } else {
                                        objList.remove(b);
                                    }
                                } else {
                                    objList.remove(b);
                                }
                            }
                        }
                        for (final CidsBean b : new ArrayList<CidsBean>(newObjList)) {
                            if (getBeanFromList(objList, (Integer)b.getProperty("id")) == null) {
                                if (attribute.isArray()) {
                                    String referenceKey = null;

                                    if (getClassById(classes, attribute.getForeignKeyClassId()).getTableName()
                                                .equalsIgnoreCase(b.getMetaObject().getMetaClass().getTableName())) {
                                        referenceKey = getForeignAttributeFromArrayCLass(b.getMetaObject()
                                                        .getMetaClass());
                                    }

                                    if (referenceKey != null) {
                                        objList.add((CidsBean)b.getProperty(referenceKey));
                                    } else {
                                        objList.add(b);
                                    }
                                } else {
                                    objList.add(b);
                                }
                            }
                        }
                    }
                } else if (attribute.isForeignKey()) {
                    if (node != null) {
                        if (node.isNull()) {
                            bean.setProperty(attribute.getFieldName(), null);
                        } else if (node.isInt()) {
                            final MetaObject mo = ms.getMetaObject(
                                    user,
                                    node.asInt(),
                                    getClassById(classes, attribute.getForeignKeyClassId()).getID(),
                                    CC);

                            if (mo != null) {
                                bean.setProperty(attribute.getFieldName(), mo.getBean());
                            }
                        } else {
                            final Object value = createCidsBeanFromJson(
                                    classes,
                                    node,
                                    getClassById(classes, attribute.getForeignKeyClassId()).getTableName(),
                                    null,
                                    complete);
                            bean.setProperty(attribute.getFieldName(), value);
                        }
                    }
                } else {
                    final String javaClass = attribute.getJavaclassname();

                    if (node.isNull()) {
                        bean.setProperty(attribute.getFieldName(), null);
                    } else if (javaClass.endsWith("String")) {
                        bean.setProperty(attribute.getFieldName(), node.asText());
                    } else if (javaClass.endsWith("Integer")) {
                        if (!attribute.getFieldName().equalsIgnoreCase("id") || (node.asInt() > 0)) {
                            bean.setProperty(attribute.getFieldName(), node.asInt());
                        }
                    } else if (javaClass.endsWith("Long")) {
                        if (node.isNull()) {
                            bean.setProperty(attribute.getFieldName(), null);
                        } else {
                            bean.setProperty(attribute.getFieldName(), node.asLong());
                        }
                    } else if (javaClass.endsWith("Float")) {
                        if (node.isNull()) {
                            bean.setProperty(attribute.getFieldName(), null);
                        } else {
                            bean.setProperty(attribute.getFieldName(), node.floatValue());
                        }
                    } else if (javaClass.endsWith("Double")) {
                        if (node.isNull()) {
                            bean.setProperty(attribute.getFieldName(), null);
                        } else {
                            bean.setProperty(attribute.getFieldName(), node.asDouble());
                        }
                    } else if (javaClass.endsWith("Timestamp")) {
                        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'H:m:s");
                        final java.util.Date d = sdf.parse(node.asText());
                        final Timestamp ts = new Timestamp(d.getTime());
                        bean.setProperty(attribute.getFieldName(), ts);
                    } else if (javaClass.endsWith("Date")) {
                        final Date ts = new Date(node.asLong());
                        bean.setProperty(attribute.getFieldName(), ts);
                    } else if (javaClass.endsWith("BigDecimal")) {
                        final BigDecimal bd = new BigDecimal(node.asText());
                        bean.setProperty(attribute.getFieldName(), bd);
                    } else if (javaClass.endsWith("BigInteger")) {
                        final BigInteger bi = new BigInteger(node.asText());
                        bean.setProperty(attribute.getFieldName(), bi);
                    } else if (javaClass.endsWith("Boolean")) {
                        bean.setProperty(attribute.getFieldName(), node.asBoolean());
                    } else if (javaClass.endsWith("Geometry")) {
                        final GeoJSONReader r = new GeoJSONReader();
                        final String geometryString = "{\"type\":\"" + node.get("type").asText() + "\",\"coordinates\":"
                                    + node.get("coordinates") + "}";
                        final Geometry g = r.read(geometryString);
                        final JsonNode crs = node.get("crs");

                        if (crs != null) {
                            // set the crs
                            final JsonNode props = crs.get("properties");

                            if (props != null) {
                                final JsonNode name = props.get("name");

                                if ((name != null) && (name.asText().indexOf(":") != 0)) {
                                    final String srid = name.asText().substring(name.asText().lastIndexOf(":") + 1);

                                    try {
                                        g.setSRID(Integer.parseInt(srid));
                                    } catch (NumberFormatException e) {
                                        LOG.error("invalid crs found: " + name.toString(), e);
                                    }
                                }
                            }
                        }

                        bean.setProperty(attribute.getFieldName(), g);
                    } else {
                        throw new Exception("unhandled datatype " + javaClass);
                    }
                }
            }
        }

        return bean;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getForeignAttributeFromArrayCLass(final MetaClass c) {
        final HashMap attrMap = c.getMemberAttributeInfos();

        for (final Object key : new ArrayList(attrMap.keySet())) {
            final MemberAttributeInfo attribute = (MemberAttributeInfo)attrMap.get(key);

            if (attribute.isForeignKey()) {
                return attribute.getFieldName();
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   possibleObjectsList  DOCUMENT ME!
     * @param   id                   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private CidsBean getBeanFromList(final List<CidsBean> possibleObjectsList, final Integer id) {
        if ((id != null) && (possibleObjectsList != null)) {
            for (final CidsBean bean : possibleObjectsList) {
                if ((bean.getProperty("id") != null) && bean.getProperty("id").equals(id)) {
                    return bean;
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes    DOCUMENT ME!
     * @param   attribute  DOCUMENT ME!
     * @param   attrMap    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getFieldName(final MetaClass[] classes, final MemberAttributeInfo attribute, final HashMap attrMap) {
        int occs = 0;
        boolean fieldNameDuplicate = false;
        final String attributeTypeName = getClassById(classes, attribute.getForeignKeyClassId()).getTableName();

        for (final Object key : new ArrayList(attrMap.keySet())) {
            final MemberAttributeInfo attr = (MemberAttributeInfo)attrMap.get(key);
            final MetaClass mc = getClassById(classes, attr.getForeignKeyClassId());
            final String attrTypeName = ((mc != null) ? mc.getTableName() : null);

            if (!attr.isExtensionAttribute()) {
                if ((attrTypeName != null)
                            && attrTypeName.equalsIgnoreCase(attributeTypeName)) {
                    occs++;
                }
                if ((attrTypeName != null)
                            && attr.getFieldName().equalsIgnoreCase(attributeTypeName)) {
                    fieldNameDuplicate = true;
                }
            }
        }

        String relName;

        if (occs > 1) {
            relName = attribute.getFieldName().toLowerCase() + '_'
                        + attributeTypeName.toLowerCase();
        } else {
            if (fieldNameDuplicate == true) {
                relName = attributeTypeName.toLowerCase() + "Object";
            } else {
                relName = attributeTypeName.toLowerCase();
            }
        }

        return relName;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes    DOCUMENT ME!
     * @param   tablename  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private MetaClass getClassByTableName(final MetaClass[] classes, final String tablename) {
        for (final MetaClass clazz : classes) {
            if (clazz.getTableName().equalsIgnoreCase(tablename)) {
                return clazz;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     * @param   id       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private MetaClass getClassById(final MetaClass[] classes, final int id) {
        for (final MetaClass clazz : classes) {
            if (clazz.getId() == Math.abs(id)) {
                return clazz;
            }
        }

        return null;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class SyncBrandNewFeatures extends Thread {

        //~ Static fields/initializers -----------------------------------------

        private static final ObjectMapper mapper = new ObjectMapper();
        private static int counter = 100000;

        //~ Instance fields ----------------------------------------------------

        private final Map<String, String> SOURCE_LAYER_MAPPING;
        private final Map<String, String[]> FEATURE_ATTRIBUTES;
        private final Map<String, String> GEOMETRY_MAPPING;
        private final List<RefreshConfig> REFRESH_CONFIGS;
        private String webdav;
        private final User user;
        private final String webdavUser;
        private final String webdavPwd;
        private final String brandNewFeaturesPath;
        private final String brandNewFeaturesDigestPath;
        private final String className;
        private final String geometry;
        private final JsonNode rootNode;
        private final CidsBean beanToSave;
        private final MetaService ms;
        private final boolean isNew;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new SyncBrandNewFeatures object.
         *
         * @param  SOURCE_LAYER_MAPPING        DOCUMENT ME!
         * @param  FEATURE_ATTRIBUTES          DOCUMENT ME!
         * @param  GEOMETRY_MAPPING            DOCUMENT ME!
         * @param  REFRESH_CONFIGS             DOCUMENT ME!
         * @param  ms                          DOCUMENT ME!
         * @param  user                        DOCUMENT ME!
         * @param  webdav                      DOCUMENT ME!
         * @param  webdavUser                  DOCUMENT ME!
         * @param  webdavPwd                   DOCUMENT ME!
         * @param  brandNewFeaturesPath        DOCUMENT ME!
         * @param  brandNewFeaturesDigestPath  DOCUMENT ME!
         * @param  className                   DOCUMENT ME!
         * @param  rootNode                    DOCUMENT ME!
         * @param  beanToSave                  DOCUMENT ME!
         * @param  geometry                    DOCUMENT ME!
         * @param  isNew                       DOCUMENT ME!
         */
        public SyncBrandNewFeatures(final Map<String, String> SOURCE_LAYER_MAPPING,
                final Map<String, String[]> FEATURE_ATTRIBUTES,
                final Map<String, String> GEOMETRY_MAPPING,
                final List<RefreshConfig> REFRESH_CONFIGS,
                final MetaService ms,
                final User user,
                final String webdav,
                final String webdavUser,
                final String webdavPwd,
                final String brandNewFeaturesPath,
                final String brandNewFeaturesDigestPath,
                final String className,
                final JsonNode rootNode,
                final CidsBean beanToSave,
                final String geometry,
                final boolean isNew) {
            this.SOURCE_LAYER_MAPPING = SOURCE_LAYER_MAPPING;
            this.FEATURE_ATTRIBUTES = FEATURE_ATTRIBUTES;
            this.GEOMETRY_MAPPING = GEOMETRY_MAPPING;
            this.REFRESH_CONFIGS = REFRESH_CONFIGS;
            this.ms = ms;
            this.user = user;
            this.webdav = webdav;
            this.webdavUser = webdavUser;
            this.webdavPwd = webdavPwd;
            this.brandNewFeaturesPath = brandNewFeaturesPath;
            this.brandNewFeaturesDigestPath = brandNewFeaturesDigestPath;
            this.rootNode = rootNode;
            this.beanToSave = beanToSave;
            this.className = className;
            this.geometry = geometry;
            this.isNew = isNew;
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public void run() {
            synchronized (SaveObjectAction.class) {
                try {
                    final JsonNode collection = getFeatureCollection();
                    final JsonNode featuresNode = collection.get("features");

                    refreshObject(featuresNode, beanToSave, className, geometry, isNew);

                    for (final RefreshConfig config : REFRESH_CONFIGS) {
                        if ((beanToSave.getProperty(config.getIdProperty()) != null)
                                    && className.equals(config.getSourceClassname())) {
                            final String query = config.getQuery();

                            final MetaObject[] objects = ms.getMetaObject(
                                    user,
                                    query.replace(
                                        "<id>",
                                        String.valueOf(beanToSave.getProperty(config.getIdProperty()))),
                                    CC);

                            if ((objects != null) && (objects.length > 0)) {
                                for (final MetaObject mo : objects) {
                                    if (config.getSourceClassname().equals(config.getTargetClassname())) {
                                        // if source classname and target classname are the same, then check, if the
                                        // current object is not the source object
                                        final CidsBean b = mo.getBean();

                                        if (b.getProperty("id") != beanToSave.getProperty("id")) {
                                            refreshObject(
                                                featuresNode,
                                                mo.getBean(),
                                                config.getTargetClassname(),
                                                geometry,
                                                false);
                                        }
                                    } else {
                                        refreshObject(
                                            featuresNode,
                                            mo.getBean(),
                                            config.getTargetClassname(),
                                            geometry,
                                            false);
                                    }
                                }
                            }
                        }
                    }

                    saveFeatureCollection(collection);
                } catch (Exception e) {
                    LOG.error("Error while creating brand new feature collection", e);
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   featuresNode  DOCUMENT ME!
         * @param   beanToSave    DOCUMENT ME!
         * @param   className     DOCUMENT ME!
         * @param   geometry      DOCUMENT ME!
         * @param   isNew         DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        private void refreshObject(final JsonNode featuresNode,
                final CidsBean beanToSave,
                final String className,
                final String geometry,
                final boolean isNew) throws Exception {
            if (featuresNode instanceof ArrayNode) {
                ObjectNode newFeature = null;

                for (final JsonNode node : featuresNode) {
                    final JsonNode idNode = node.get("id");
                    final JsonNode props = node.get("properties");

                    if ((props != null) && (props.get("_sourceLayer") != null) && props.get("_sourceLayer")
                                .isTextual()
                                && props.get("_sourceLayer").asText().equals(SOURCE_LAYER_MAPPING.get(className))) {
                        if ((props.get("id") != null) && props.get("id").isInt()
                                    && (props.get("id").asInt() == beanToSave.getPrimaryKeyValue())) {
                            newFeature = (ObjectNode)node;
                        }
                    }

                    if (idNode.isInt()) {
                        final int idAsInt = idNode.asInt();

                        if (idAsInt >= counter) {
                            counter = idAsInt + 1;
                        }
                    }
                }

                final Geometry geom = (Geometry)beanToSave.getProperty(GEOMETRY_MAPPING.get(className));

                if ((geom == null) && (geometry == null)) {
                    // features without geometry should not be added to the feature collection
                    return;
                }

                if (newFeature == null) {
                    newFeature = ((ArrayNode)featuresNode).addObject();
                    newFeature.put("type", "Feature");
                }

                newFeature.put("id", ++counter); // what ID is this

                if (geometry == null) {
                    final GeoJSONWriter writer = new GeoJSONWriter();
                    com.vividsolutions.jts.geom.Geometry newGeom = (com.vividsolutions.jts.geom.Geometry)geom.clone();
                    final CoordinateSystem coordSystem = CRSFactory.create("EPSG:25832");
                    org.deegree.model.spatialschema.Geometry deegreeGeom = JTSAdapter.wrap(newGeom);
                    final GeoTransformer transformer = new GeoTransformer("EPSG:4326");
                    deegreeGeom = transformer.transform(deegreeGeom, coordSystem.getCRS());

                    newGeom = JTSAdapter.export(deegreeGeom);

                    final org.wololo.geojson.Geometry geoJson = writer.write(newGeom);

                    newFeature.set("geometry", mapper.readTree(geoJson.toString()));
                } else {
                    newFeature.set("geometry", mapper.readTree(geometry));
                }

                final ObjectNode props = newFeature.putObject("properties");

                for (final String prop : FEATURE_ATTRIBUTES.get(className)) {
                    final String completeKey = (prop.toLowerCase().contains(" as ")
                            ? prop.substring(0, prop.toLowerCase().indexOf(" as ")) : prop);
                    String propName = (completeKey.contains(".")
                            ? completeKey.substring(completeKey.lastIndexOf(".") + 1) : prop);

                    if (prop.contains(" as ")) {
                        propName = prop.substring(prop.indexOf(" as ") + " as ".length()).trim();
                    }

                    if ((completeKey != null) && completeKey.startsWith("count(")) {
                        final String tmpKey = completeKey.substring("count(".length(),
                                completeKey.length()
                                        - 1);
                        final Object o = beanToSave.getProperty(tmpKey);

                        if (o instanceof Collection) {
                            final int size = ((Collection)o).size();

                            props.put(propName, size);
                        } else {
                            props.put(propName, 0);
                        }
                    } else if ((completeKey != null) && completeKey.startsWith("countCalc(")) {
                        final String tmpKey = completeKey.substring("countCalc(".length(),
                                completeKey.length()
                                        - 1);
                        final String query = tmpKey.substring(0, tmpKey.indexOf(";"));
                        final String idKey = tmpKey.substring(tmpKey.indexOf(";") + 1);
                        final ArrayList<ArrayList> count = ms.performCustomSearch(query.replace(
                                    "<id>",
                                    String.valueOf(beanToSave.getProperty(idKey))),
                                CC);

                        if (!count.isEmpty() && !count.get(0).isEmpty()
                                    && (count.get(0).get(0) instanceof Number)) {
                            final int size = ((Number)count.get(0).get(0)).intValue();

                            props.put(propName, size);
                        } else {
                            props.put(propName, 0);
                        }
                    } else {
                        final Object o = beanToSave.getProperty(completeKey);

                        if (o instanceof Integer) {
                            props.put(propName, (Integer)o);
                        } else if (o instanceof Double) {
                            props.put(propName, (Double)o);
                        } else if (o instanceof Short) {
                            props.put(propName, (Short)o);
                        } else if (o instanceof Long) {
                            props.put(propName, (Long)o);
                        } else if (o instanceof BigInteger) {
                            props.put(propName, (BigInteger)o);
                        } else if (o instanceof BigDecimal) {
                            props.put(propName, (BigDecimal)o);
                        } else if (o instanceof Boolean) {
                            props.put(propName, (Boolean)o);
                        } else if (o instanceof Float) {
                            props.put(propName, (Float)o);
                        } else if (o != null) {
                            props.put(propName, String.valueOf(o));
                        }
                    }
                }

                props.put("_sourceLayer", SOURCE_LAYER_MAPPING.get(className));

                if (isNew) {
                    props.put("brandnew", Boolean.TRUE);
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  JsonProcessingException  DOCUMENT ME!
         * @throws  IOException              DOCUMENT ME!
         */
        private JsonNode getFeatureCollection() throws JsonProcessingException, IOException {
            // todo implement
            final WebDavClient webdavclient = new WebDavClient(null, webdavUser, webdavPwd);

            if (!webdav.endsWith("/")) {
                webdav += "/";
            }

            InputStream is = null;

            try {
                is = webdavclient.getInputStream(webdav + brandNewFeaturesPath);

                if (is != null) {
                    return mapper.readTree(is);
                } else {
                    return (ObjectNode)mapper.readTree(EMPTY_FEATURE_COLLECTION_OBJECT);
                }
            } catch (Exception e) {
                return (ObjectNode)mapper.readTree(EMPTY_FEATURE_COLLECTION_OBJECT);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        LOG.error("Error while closing stream", e);
                    }
                }
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @param   rootNode  DOCUMENT ME!
         *
         * @throws  Exception  DOCUMENT ME!
         */
        private void saveFeatureCollection(final JsonNode rootNode) throws Exception {
            final String featureCollection = mapper.writeValueAsString(rootNode);
            final WebDavClient webdavclient = new WebDavClient(null, webdavUser, webdavPwd);

            if (!webdav.endsWith("/")) {
                webdav += "/";
            }

            webdavclient.put(webdav + brandNewFeaturesPath, new ByteArrayInputStream(featureCollection.getBytes()));

            final String md5 = DigestUtils.md5Hex(featureCollection);

            webdavclient.put(webdav + brandNewFeaturesDigestPath, new ByteArrayInputStream(md5.getBytes()));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    private static class RefreshConfig {

        //~ Instance fields ----------------------------------------------------

        private String idProperty;
        private String sourceClassname;
        private String targetClassname;
        private String query;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RefreshConfig object.
         *
         * @param  idProperty       DOCUMENT ME!
         * @param  sourceClassname  DOCUMENT ME!
         * @param  targetClassname  DOCUMENT ME!
         * @param  query            DOCUMENT ME!
         */
        public RefreshConfig(final String idProperty,
                final String sourceClassname,
                final String targetClassname,
                final String query) {
            this.idProperty = idProperty;
            this.sourceClassname = sourceClassname;
            this.targetClassname = targetClassname;
            this.query = query;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return  the idProperty
         */
        public String getIdProperty() {
            return idProperty;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  idProperty  the idProperty to set
         */
        public void setIdProperty(final String idProperty) {
            this.idProperty = idProperty;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the sourceClassname
         */
        public String getSourceClassname() {
            return sourceClassname;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  sourceClassname  the sourceClassname to set
         */
        public void setSourceClassname(final String sourceClassname) {
            this.sourceClassname = sourceClassname;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the targetClassname
         */
        public String getTargetClassname() {
            return targetClassname;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  targetClassname  the targetClassname to set
         */
        public void setTargetClassname(final String targetClassname) {
            this.targetClassname = targetClassname;
        }

        /**
         * DOCUMENT ME!
         *
         * @return  the query
         */
        public String getQuery() {
            return query;
        }

        /**
         * DOCUMENT ME!
         *
         * @param  query  the query to set
         */
        public void setQuery(final String query) {
            this.query = query;
        }
    }
}
