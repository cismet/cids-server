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
package de.cismet.cidsx.server.api.types.legacy;

import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;

/**
 * Factory with help method for converting between LightweightMetaObjects and cids Beans.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class CidsBeanFactory implements ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    /** toString Property of CidsBean representing a serialized LightweightMetaObject. */
    public static final String LEGACY_DISPLAY_NAME = "$legacyDisplayName";
    private static final Pattern CLASSKEY_PATTERN = Pattern.compile("^/([^/]*)/");
    private static final Pattern OBJECTID_PATTERN = Pattern.compile("([^/?]+)(?=/?(?:$|\\?))");

    private static final transient Logger LOG = Logger.getLogger(CidsBeanFactory.class);
    private static final CidsBeanFactory FACTORY = new CidsBeanFactory();

    //~ Instance fields --------------------------------------------------------

    private final ObjectMapper mapper = new ObjectMapper(new JsonFactory());

    private final transient ConnectionContext connectionContext = ConnectionContext.create(
            ConnectionContext.Category.LEGACY,
            getClass().getSimpleName());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsClassFactory object.
     */
    private CidsBeanFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final CidsBeanFactory getFactory() {
        return FACTORY;
    }

    /**
     * Tries to derive LightweightMetaObject representation fields from a cidsBean instance.
     *
     * @param       cidsBean  DOCUMENT ME!
     *
     * @return      String array of representationFields (may be empty)
     *
     * @deprecated  not suitable to reliably determine representation fields
     */
    private String[] representationFieldsFromCidsBean(final CidsBean cidsBean) {
        final LinkedList<String> representationFields = new LinkedList<String>();

        for (final String propertyName : cidsBean.getPropertyNames()) {
            if ((propertyName.indexOf('$') != 0) && (cidsBean.getProperty(propertyName) != null)) {
                representationFields.add(propertyName);
            }
        }

        return representationFields.toArray(new String[representationFields.size()]);
    }

    /**
     * Helper Method for creating sub LightweightMetaObject.
     *
     * @param   cidsBean        DOCUMENT ME!
     * @param   domain          DOCUMENT ME!
     * @param   user            DOCUMENT ME!
     * @param   classNameCache  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected LightweightMetaObject childLightweightMetaObjectFromCidsBean(
            final CidsBean cidsBean,
            final String domain,
            final User user,
            final ClassNameCache classNameCache) {
        LightweightMetaObject lwo;
        final int subClassId = classNameCache.getClassIdForClassName(
                domain,
                cidsBean.getCidsBeanInfo().getClassKey());

        if (subClassId != -1) {
            lwo = this.lightweightMetaObjectFromCidsBean(
                    cidsBean,
                    subClassId,
                    domain,
                    user,
                    null,
                    null,
                    classNameCache);
        } else {
            LOG.warn("cannot create LightweightMetaObject for class '"
                        + cidsBean.getCidsBeanInfo().getClassKey() + "', class key not found. "
                        + "Returning null!");
            lwo = null;
        }

        return lwo;
    }

    /**
     * Convenience Method that automatically tries to derive the LWMO representation fields from the CidsBean and
     * assumes that a {@link DummyRepresentationFormater} can be created based on the {@link #LEGACY_DISPLAY_NAME}
     * property.
     *
     * @param       cidsBean        DOCUMENT ME!
     * @param       classId         DOCUMENT ME!
     * @param       domain          DOCUMENT ME!
     * @param       user            DOCUMENT ME!
     * @param       classNameCache  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  not suitable to reliably determine representation fields!
     */
    public LightweightMetaObject lightweightMetaObjectFromCidsBean(
            final CidsBean cidsBean,
            final int classId,
            final String domain,
            final User user,
            final ClassNameCache classNameCache) {
        final String[] representationFields = this.representationFieldsFromCidsBean(cidsBean);
        return this.lightweightMetaObjectFromCidsBean(
                cidsBean,
                classId,
                domain,
                user,
                representationFields,
                null,
                classNameCache);
    }

    /**
     * Tries to derive LightweightMetaObject representation fields from a ObjectNode instance.
     *
     * @param   objectNode  DOCUMENT ME!
     * @param   classId     DOCUMENT ME!
     * @param   domain      DOCUMENT ME!
     * @param   user        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LightweightMetaObject lightweightMetaObjectFromJsonNode(
            final JsonNode objectNode,
            final int classId,
            final String domain,
            final User user) {
        final LinkedHashMap<String, Object> lmoAttributes = new LinkedHashMap<>();
        final int objectId = Integer.parseInt(CidsBeanFactory.getFactory().getObjectId(objectNode));
        // lmoAttributes.put(metaClass.getPrimaryKey(), objectId);
        // FIXME: assuming that primary key is always ID!
        lmoAttributes.put("ID", objectId);

        final Iterator<Map.Entry<String, JsonNode>> propertiesIterator = objectNode.fields();
        while (propertiesIterator.hasNext()) {
            final Map.Entry<String, JsonNode> configEntry = propertiesIterator.next();
            final String propertyName = configEntry.getKey();
            final JsonNode propertyNode = configEntry.getValue();

            if (propertyName.indexOf('$') != 0) {
                try {
                    if (propertyNode.isArray()) {
                        LOG.warn("unexpected JSON Attribute array. expecting string value for node '" + propertyName
                                    + "', ignoring node!");
                        final Object value = mapper.treeToValue(propertyNode, Object.class);
                        lmoAttributes.put(propertyName, value);
                    } else if (propertyNode.isObject()) {
                        LOG.warn("unexpected JSON Attribute object node. Expecting string value for node '"
                                    + propertyName
                                    + "' but actual value is: \n" + propertyNode.toString());
                        final Object value = mapper.treeToValue(propertyNode, Object.class);
                        lmoAttributes.put(propertyName, value);
                    } else if (propertyNode.isTextual()) {
                        lmoAttributes.put(propertyName, propertyNode.textValue());
                    } else if (propertyNode.isBinary()) {
                        lmoAttributes.put(propertyName, propertyNode.binaryValue());
                    } else if (propertyNode.isInt()) {
                        lmoAttributes.put(propertyName, propertyNode.intValue());
                    } else if (propertyNode.isBigInteger()) {
                        lmoAttributes.put(propertyName, propertyNode.bigIntegerValue());
                    } else if (propertyNode.isLong()) {
                        lmoAttributes.put(propertyName, propertyNode.longValue());
                    } else if (propertyNode.isBoolean()) {
                        lmoAttributes.put(propertyName, propertyNode.booleanValue());
                    } else {
                        LOG.warn("unknown type of JSON configuration Attribute '" + propertyName
                                    + "': \n" + propertyNode.toString());
                        final Object value = mapper.treeToValue(propertyNode, Object.class);
                        lmoAttributes.put(propertyName, value);
                    }
                } catch (Throwable t) {
                    final String message = "could not set property '" + propertyName
                                + "' of lightweight Meta Object: '" + t.getMessage()
                                + "', setting value to null!";
                    LOG.error(message, t);
                    lmoAttributes.put(propertyName, null);
                }
            }
        }

        final LightweightMetaObject lightweightMetaObject = new LightweightMetaObject(
                classId,
                objectId,
                domain,
                user,
                lmoAttributes);
        lightweightMetaObject.initWithConnectionContext(getConnectionContext());

        if (objectNode.has(LEGACY_DISPLAY_NAME)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(LEGACY_DISPLAY_NAME + " property set in cids bean, creating DummyRepresentationFormater");
            }

            lightweightMetaObject.setFormater(new DummyRepresentationFormater(
                    objectNode.get(LEGACY_DISPLAY_NAME).asText()));
        }

        return lightweightMetaObject;
    }

    /**
     * Transforms a CidsBean into a LightweightMetaObject.
     *
     * @param   cidsBean                DOCUMENT ME!
     * @param   classId                 DOCUMENT ME!
     * @param   domain                  DOCUMENT ME!
     * @param   user                    DOCUMENT ME!
     * @param   representationFields    DOCUMENT ME!
     * @param   representationFormater  DOCUMENT ME!
     * @param   classNameCache          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LightweightMetaObject lightweightMetaObjectFromCidsBean(
            final CidsBean cidsBean,
            final int classId,
            final String domain,
            final User user,
            final String[] representationFields,
            final AbstractAttributeRepresentationFormater representationFormater,
            final ClassNameCache classNameCache) {
        final int objectId = cidsBean.getPrimaryKeyValue();
        final LinkedHashMap<String, Object> lmoAttributes = new LinkedHashMap<String, Object>();
        lmoAttributes.put(cidsBean.getPrimaryKeyFieldname(), cidsBean.getPrimaryKeyValue());

        if ((representationFields != null) && (representationFields.length > 0)) {
            for (final String propertyName : representationFields) {
                final Object property = cidsBean.getProperty(propertyName);
                if ((property != null) && Collection.class.isAssignableFrom(property.getClass())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("filling LightweightMetaObject property array '" + propertyName + "'");
                    }
                    final ArrayList<LightweightMetaObject> subLwos = new ArrayList<LightweightMetaObject>(
                            ((Collection)property).size());
                    final Iterator cidsBeanIerator = ((Collection)property).iterator();

                    while (cidsBeanIerator.hasNext()) {
                        final Object object = cidsBeanIerator.next();
                        if ((object != null) && CidsBean.class.isAssignableFrom(object.getClass())) {
                            final CidsBean subCidsBean = (CidsBean)object;
                            final LightweightMetaObject subLwo = this.childLightweightMetaObjectFromCidsBean(
                                    subCidsBean,
                                    domain,
                                    user,
                                    classNameCache);
                            subLwos.add(subLwo);
                        } else {
                            LOG.warn("entry '" + object + "' of array attribute '" + propertyName
                                        + "' is not a cids bean, entry is ignored in LightweightMetaObject!");
                        }
                    }

                    lmoAttributes.put(propertyName, subLwos);
                } else if ((property != null) && CidsBean.class.isAssignableFrom(property.getClass())) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("filling LightweightMetaObject object property '" + propertyName + "'");
                    }
                    final CidsBean subCidsBean = (CidsBean)property;
                    final LightweightMetaObject subLwo = this.childLightweightMetaObjectFromCidsBean(
                            subCidsBean,
                            domain,
                            user,
                            classNameCache);
                    lmoAttributes.put(propertyName, subLwo);
                } else {
                    lmoAttributes.put(propertyName, property);
                }
            }
        }

        final LightweightMetaObject lightweightMetaObject = new LightweightMetaObject(
                classId,
                objectId,
                domain,
                user,
                lmoAttributes);
        lightweightMetaObject.initWithConnectionContext(getConnectionContext());

        if (representationFormater != null) {
            lightweightMetaObject.setFormater(representationFormater);
        } else if (this.isLightweightMetaObject(cidsBean)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(LEGACY_DISPLAY_NAME + " property set in cids bean, creating DummyRepresentationFormater");
            }
            lightweightMetaObject.setFormater(new DummyRepresentationFormater(
                    cidsBean.getProperty(LEGACY_DISPLAY_NAME).toString()));
        }

        return lightweightMetaObject;
    }

    /**
     * Creates a LightweightMetaObject from a CidsBean.
     *
     * @param       lightweightMetaObject  DOCUMENT ME!
     * @param       metaClass              DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  unable to reliably determine the representation fields
     */
    public CidsBean cidsBeanFromLightweightMetaObject(final LightweightMetaObject lightweightMetaObject,
            final MetaClass metaClass) {
        final MetaObject metaObject = metaClass.getEmptyInstance(getConnectionContext());
        metaObject.setID(lightweightMetaObject.getObjectID());
        final CidsBean cidsBean = metaObject.getBean();

        for (final String attributeName : lightweightMetaObject.getKnownAttributeNames()) {
            final Object value = lightweightMetaObject.getLWAttribute(attributeName);
            try {
                cidsBean.setProperty(attributeName, value);
            } catch (Exception ex) {
                LOG.warn("could not set attribute '" + attributeName + "' of LightweightMetaObject '"
                            + lightweightMetaObject + "' to CidsBean: " + ex.getMessage(),
                    ex);
            }
        }

        try {
            cidsBean.setProperty(LEGACY_DISPLAY_NAME, lightweightMetaObject.toString());
        } catch (Exception ex) {
            LOG.warn("could not toStringRepresentation of LightweightMetaObject '"
                        + lightweightMetaObject + "' to CidsBean: " + ex.getMessage(),
                ex);
        }

        return cidsBean;
    }

    /**
     * Derives an ObjectNode (~CidsBean) from a LightweightMetaObject instance.
     *
     * @param   lightweightMetaObject  DOCUMENT ME!
     * @param   className              DOCUMENT ME!
     * @param   domain                 DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public JsonNode jsonNodeFromLightweightMetaObject(final LightweightMetaObject lightweightMetaObject,
            final String className,
            final String domain) {
        final String selfReference = "/" + domain + "." + className + "/" + lightweightMetaObject.getId();
        final ObjectNode objectNode = this.mapper.createObjectNode();

        objectNode.put("$self", selfReference);
        objectNode.put(LEGACY_DISPLAY_NAME, lightweightMetaObject.toString());

        for (final String attributeName : lightweightMetaObject.getKnownAttributeNames()) {
            final Object value = lightweightMetaObject.getLWAttribute(attributeName);
            final JsonNode jsonNode = mapper.valueToTree(value);
            objectNode.put(attributeName, jsonNode);
        }

        return objectNode;
    }

    /**
     * Helper Method for checking if a cids bean is a potential LightweightMetaObject. Note: This method relies on meta
     * information ($properties) of the cids bean!
     *
     * @param       cidsBean  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    public boolean isLightweightMetaObject(final CidsBean cidsBean) {
        return (cidsBean.getProperty(LEGACY_DISPLAY_NAME) != null)
                    && !cidsBean.getProperty(LEGACY_DISPLAY_NAME).toString().isEmpty();
    }

    /**
     * Returns the parsed class name from the $self or $ref properties of the object or throws an error, if the
     * properties are not found or invalid.<br>
     * <strong>Copied from LegacyEntityCore in cids-server-rest-legacy</strong>
     *
     * @param   jsonObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Error  DOCUMENT ME!
     */
    public String getClassKey(final JsonNode jsonObject) {
        if (jsonObject.get("$self") != null) {
            final Matcher matcher = CLASSKEY_PATTERN.matcher(jsonObject.get("$self").asText());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new Error("Object with malformed self reference: " + jsonObject.get("$self"));
            }
        } else if (jsonObject.get("$ref") != null) {
            final Matcher matcher = CLASSKEY_PATTERN.matcher(jsonObject.get("$ref").asText());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new Error("Object with malformed reference: " + jsonObject.get("$ref"));
            }
        } else {
            throw new Error("Object without (self) reference is invalid!");
        }
    }

    /**
     * Returns the value of the object property 'id' or tries to extract the id from the $self or $ref properties.
     * Returns -1 if no id is found.<br>
     * <strong>Copied from LegacyEntityCore in cids-server-rest-legacy</strong>
     *
     * @param   jsonObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Error  DOCUMENT ME!
     */
    public String getObjectId(final JsonNode jsonObject) {
        if (jsonObject.get("id") != null) {
            return jsonObject.get("id").asText();
        } else if (jsonObject.get("$self") != null) {
            final Matcher matcher = OBJECTID_PATTERN.matcher(jsonObject.get("$self").asText());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new Error("Object with malformed self reference: " + jsonObject.get("$ref"));
            }
        } else if (jsonObject.get("$ref") != null) {
            final Matcher matcher = OBJECTID_PATTERN.matcher(jsonObject.get("$ref").asText());
            if (matcher.find()) {
                return matcher.group(1);
            } else {
                throw new Error("Object with malformed reference: " + jsonObject.get("$ref"));
            }
        }
        {
            return "-1";
        }
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }
}

/**
 * DummyRepresentationFormater that does not perform any formatting by itself bur returns an already formated string for
 * the toString Method.
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
class DummyRepresentationFormater extends AbstractAttributeRepresentationFormater {

    //~ Instance fields --------------------------------------------------------

    final String formattedToString;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DummyRepresentationFormater object.
     *
     * @param  formattedToString  DOCUMENT ME!
     */
    DummyRepresentationFormater(final String formattedToString) {
        this.formattedToString = formattedToString;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getRepresentation() {
        return this.formattedToString;
    }
}
