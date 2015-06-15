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
package de.cismet.cids.server.api.types;

import Sirius.util.image.Image;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.cismet.cids.base.types.Key;

import de.cismet.cids.server.api.types.configkeys.CidsClassConfigurationFlagKey;
import de.cismet.cids.server.api.types.configkeys.CidsClassConfigurationKey;
import de.cismet.cids.server.api.types.configkeys.ClassConfig;

/**
 * cids class REST API Type and JSON Serializer / Deserializer. <strong>Code copied from
 * de.cismet.cids.server.data.legacy.CidsClass (cids-server-rest-legacy project) for feature branch #100</strong> TODO:
 * Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@JsonSerialize(using = CidsClassSerializer.class)
@JsonDeserialize(using = CidsClassDeserializer.class)
public class CidsClass implements Key {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsClass.class);

    //~ Instance fields --------------------------------------------------------

    private String name;
    private String domain;
    private final LinkedHashMap<String, CidsAttribute> attributes = new LinkedHashMap<String, CidsAttribute>();

    private final LinkedHashMap<String, Object> configurationAttributes = new LinkedHashMap<String, Object>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsClass object.
     */
    public CidsClass() {
    }

    /**
     * Creates a new CidsClass object.
     *
     * @param  name    DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public CidsClass(final String name, final String domain) {
        this.name = name;
        this.domain = domain;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Returns the name part of a class key ('/DOMAIN.CLASSNAME') or null if the class key is invalid.
     *
     * @param   classKey  the class key e.g. '/DOMAIN.CLASSNAME'
     *
     * @return  name part of the key or null if the class key is invalid
     */
    public static String nameFromKey(final String classKey) {
        final int domainSeparator = classKey.lastIndexOf('.');
        final String name;
        if ((domainSeparator > 3) && (classKey.length() > (domainSeparator + 1))) {
            // ignore trailing /
            // final String domain = classKey.substring(1, domainSeparator);
            name = classKey.substring(domainSeparator + 1);
        } else {
            LOG.error("invalid class key provided: '" + classKey
                        + "', expected $self reference: '/DOMAIN.CLASSNAME'");
            name = null;
        }

        return name;
    }

    /**
     * Returns the domain part of a class key ('/DOMAIN.CLASSNAME') or null if the class key is invalid.
     *
     * @param   classKey  the class key e.g. '/DOMAIN.CLASSNAME'
     *
     * @return  domain part of the key or null if the class key is invalid
     */
    public static String domainFromKey(final String classKey) {
        final int domainSeparator = classKey.lastIndexOf('.');
        final String domain;
        if ((domainSeparator > 3) && (classKey.length() > (domainSeparator + 1))) {
            // ignore trailing /
            domain = classKey.substring(1, domainSeparator);
        } else {
            LOG.error("invalid class key provided: '" + classKey
                        + "', expected $self reference: '/DOMAIN.CLASSNAME'");
            domain = null;
        }

        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  attr  DOCUMENT ME!
     */
    public void putAttribute(final CidsAttribute attr) {
        attributes.put(attr.getAttributeKey(),
            attr);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributeKey  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsAttribute getAttribute(final String attributeKey) {
        return attributes.get(attributeKey);
    }

    /**
     * DOCUMENT ME!
     *
     * @param       attr  DOCUMENT ME!
     *
     * @deprecated  operation is not used
     */
    public void removeAttribute(final CidsAttribute attr) {
        attributes.remove(attr.getAttributeKey());
    }

    /**
     * Sets a binary configuration Flag. The flag will be serialized as $name = true. Note that flags that are actually
     * not set (false) will not appears in the JSON representation of cids class.
     *
     * @param  key  DOCUMENT ME!
     */
    public void setConfigFlag(final CidsClassConfigurationFlagKey key) {
        configurationAttributes.put(key.toString(),
            true);
    }

    /**
     * Removes a binary configuration flag.
     *
     * @param       key  DOCUMENT ME!
     *
     * @deprecated  operation is not used
     */
    public void removeConfigFlag(final CidsClassConfigurationFlagKey key) {
        configurationAttributes.remove(key.toString());
    }

    /**
     * Sets a well-known configuration attribute, usually a string. The CidsClassDeserializer will take care about the
     * property serialisation of the attribute
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public void setConfigAttribute(final CidsClassConfigurationKey key, final Object value) {
        // ATTENTION: name.toString() returns the NAME of the ENUM, not the value of the
        // name, e.g. NAME( "Name" ) -> JSON property names are uppercase.
        // This allows for reconstructing the ENUM from the JSON property name.
        configurationAttributes.put(key.toString(),
            value);
    }

    /**
     * Returns a configuration attribute. This operation id used to return well-known configuration attributes
     * (identified by a CidsClassConfigurationKey) as well as other "unknown" configuration attributes.
     *
     * @param   key  Either a CidsClassConfigurationKey or a String
     *
     * @return  The value of a configuration attribute
     */
    public Object getConfigAttribute(final String key) {
        return configurationAttributes.get(key);
    }

    /**
     * Sets an "unknown" configuration attribute. The CidsClassDeserializer has to proper deserialization of the object.
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public void setOtherConfigAttribute(final String key, final Object value) {
        if (LOG.isDebugEnabled()) {
            LOG.warn("setting unknown configuration attribute '" + key + "' to '" + value + "'");
        }
        configurationAttributes.put(key, value);
    }

    /**
     * DOCUMENT ME!
     *
     * @param       key  DOCUMENT ME!
     *
     * @deprecated  operation is not used
     */
    public void removeConfigAttribute(final CidsClassConfigurationKey key) {
        configurationAttributes.remove(key.toString());
    }

    /**
     * Returns the name of the class.
     *
     * @return  name of the class
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the domain of the class.
     *
     * @return  the domain of the class
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Return the key resp. the $self reference of the cids class instance.
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getKey() {
        return new StringBuffer("/").append(domain).append('.').append(name).toString();
    }

    /**
     * Sets the key resp. the $self reference of the cids class instance and derives and sets additionally the domain
     * property.
     *
     * @param  classKey  the $self reference of the cids class
     */
    public void setKey(final String classKey) {
        this.name = CidsClass.nameFromKey(classKey);
        this.domain = CidsClass.domainFromKey(classKey);
    }

    /**
     * Returns all attributes (~ member attribute info) of the cids class.
     *
     * @return  map with all attributes
     */
    public LinkedHashMap<String, CidsAttribute> getAttributes() {
        return attributes;
    }

    /**
     * Returns all configuration attributes (~class attributes) attributes of the cids class.
     *
     * @return  map with all configuration attributes
     */
    public LinkedHashMap<String, Object> getConfigurationAttributes() {
        return configurationAttributes;
    }
}

/**
 * Custom serializer for cids class REST types. Uses default object serialization for the simplified cids class object
 * structure. Conversion from complex legacy meta class and attribute structure to cids class is performed in
 * CidsClassFactory.
 *
 * @version  $Revision$, $Date$
 */
class CidsClassSerializer extends StdSerializer<CidsClass> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsClassSerializer object.
     */
    public CidsClassSerializer() {
        super(CidsClass.class);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void serialize(final CidsClass cidsClass, final JsonGenerator jg, final SerializerProvider provider)
            throws IOException, JsonGenerationException {
        jg.writeStartObject();
        jg.writeStringField("$self", cidsClass.getKey());
        // ------ Config
        jg.writeFieldName("configuration");
        jg.writeStartObject();

        final Set<Map.Entry<String, Object>> configAttributesSet = cidsClass.getConfigurationAttributes().entrySet();

        for (final Map.Entry<String, Object> entry : configAttributesSet) {
            jg.writeObjectField(entry.getKey(),
                entry.getValue());
        }

        jg.writeEndObject();

        // ------- Attributes
        final Set<Map.Entry<String, CidsAttribute>> attributesSet = cidsClass.getAttributes().entrySet();

        if (attributesSet.size() > 0) {
            jg.writeFieldName("attributes");
            jg.writeStartObject();

            for (final Map.Entry<String, CidsAttribute> attr : attributesSet) {
                jg.writeObjectField(attr.getValue().name,
                    attr.getValue());
            }

            jg.writeEndObject();
        }

        jg.writeEndObject();
    }
}

/**
 * Custom deserializer for cids class REST type.
 *
 * <p>Uses mainly the default object deserialization for the simplified cids class object structure, apart form binary
 * legacy icons (Sirius Image). Conversion from cids class to complex legacy meta class and attribute structure is
 * performed in CidsClassFactory.</p>
 *
 * @version  $Revision$, $Date$
 */
class CidsClassDeserializer extends StdDeserializer<CidsClass> {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsClassDeserializer.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsClassDeserializer object.
     */
    public CidsClassDeserializer() {
        super(CidsClass.class);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsClass deserialize(final JsonParser jp, final DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
        final CidsClass cidsClass = new CidsClass();
        final boolean keySet = false;

        final ObjectNode rootNode = jp.readValueAsTree();
        cidsClass.setKey(rootNode.get("$self").asText());
        final ObjectMapper mapper = new ObjectMapper();

        // 1st process the configuration attributes (class atributes)
        final ObjectNode configurationNodes = (ObjectNode)rootNode.get("configuration");
        final Iterator<Map.Entry<String, JsonNode>> configurationElements = configurationNodes.fields();
        while (configurationElements.hasNext()) {
            final Map.Entry<String, JsonNode> configEntry = configurationElements.next();
            final String configKey = configEntry.getKey();
            final JsonNode configNode = configEntry.getValue();

            // deserialize the known configuration attributes
            if (ClassConfig.Key.LEGACY_CLASS_ICON.toString().equals(configKey)) {
                // TODO: get rid of the legacy binary icons!
                final Image image = this.deserializeSiriusImage(configKey, configNode);
                cidsClass.getConfigurationAttributes().put(configKey, image);
            } else if (ClassConfig.Key.LEGACY_OBJECT_ICON.toString().equals(configKey)) {
                // TODO: get rid of the legacy binary icons!
                final Image image = this.deserializeSiriusImage(configKey, configNode);
                cidsClass.getConfigurationAttributes().put(configKey, image);
            } else if (configNode.isArray()) {
                LOG.warn("unexpected JSON configuration Attribute array. expecting string value for node '" + configKey
                            + "', ignoring node!");
                final Object value = mapper.treeToValue(configNode, Object.class);
                cidsClass.getConfigurationAttributes().put(configKey, value);
            } else if (configNode.isObject()) {
                LOG.warn("unexpected JSON configuration Attribute object node. Expecting string value for node '"
                            + configKey
                            + "' but actual value is: \n" + configNode.toString());
                final Object value = mapper.treeToValue(configNode, Object.class);
                cidsClass.getConfigurationAttributes().put(configKey, value);
            } else if (configNode.isTextual()) {
                cidsClass.getConfigurationAttributes().put(configKey, configNode.textValue());
            } else if (configNode.isBinary()) {
                cidsClass.getConfigurationAttributes().put(configKey, configNode.binaryValue());
            } else if (configNode.isInt()) {
                cidsClass.getConfigurationAttributes().put(configKey, configNode.intValue());
            } else if (configNode.isBigInteger()) {
                cidsClass.getConfigurationAttributes().put(configKey, configNode.bigIntegerValue());
            } else if (configNode.isLong()) {
                cidsClass.getConfigurationAttributes().put(configKey, configNode.longValue());
            } else if (configNode.isBoolean()) {
                cidsClass.getConfigurationAttributes().put(configKey, configNode.booleanValue());
            } else {
                LOG.warn("unknown type of JSON configuration Attribute '" + configKey
                            + "': \n" + configNode.toString());
                final Object value = mapper.treeToValue(configNode, Object.class);
                cidsClass.getConfigurationAttributes().put(configKey, value);
            }
        }

        // 2nd process the instance attributes
        final JsonNode attributeNodes = (ObjectNode)rootNode.get("attributes");
        final Iterator<JsonNode> attributesElements = attributeNodes.elements();
        while (attributesElements.hasNext()) {
            final JsonNode attributeNode = attributesElements.next();
            final CidsAttribute cidsAttribute = mapper.treeToValue(attributeNode, CidsAttribute.class);
            cidsClass.putAttribute(cidsAttribute);
        }

        return cidsClass;
    }

    /**
     * Legacy helper operation for deserializing binary icons distributed by legacy cids server.
     *
     * @param       configKey   DOCUMENT ME!
     * @param       configNode  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  DOCUMENT ME!
     */
    private Image deserializeSiriusImage(final String configKey, final JsonNode configNode) {
        final Image image = new Image();
        if (configNode.isObject()) {
            final ObjectNode objectNode = (ObjectNode)configNode;
            if (objectNode.has("name")) {
                image.setName(objectNode.get("name").asText());
            }

            if (objectNode.has("description")) {
                image.setDescription(objectNode.get("description").asText());
            }

            if (objectNode.has("imageData")) {
                try {
                    image.setImageData(objectNode.get("imageData").binaryValue());
                } catch (IOException ex) {
                    LOG.error("cannot deserialize binary image data for '" + configKey + "':"
                                + ex.getMessage(), ex);
                }
            } else {
                LOG.warn("no binary image data available for '" + configKey + "': "
                            + image.getName());
            }
        } else {
            LOG.warn("cannot deserialize '" + ClassConfig.Key.LEGACY_CLASS_ICON
                        + "', not an object: " + configNode.toString());
        }

        return image;
    }
}
