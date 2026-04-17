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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import org.wololo.jts2geojson.GeoJSONReader;

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

import de.cismet.cids.dynamics.CidsBean;

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

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum PARAMETER_TYPE {

        //~ Enum constants -----------------------------------------------------

        className, data, complete
    }

    //~ Instance fields --------------------------------------------------------

    private int lastNegativeId = -1;

    private MetaService ms;
    private User user;

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
            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode rootNode = mapper.readTree(value);

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

        return "{\"id\": \"" + String.valueOf(beanToSave.getProperty("id")) + "\"}";
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
}
