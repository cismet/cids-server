/**
 * *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 * 
* ... and it just works.
 * 
***************************************************
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.api.types;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import de.cismet.cids.server.api.types.configkeys.CidsAttributeConfigurationFlagKey;
import de.cismet.cids.server.api.types.configkeys.CidsAttributeConfigurationKey;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * cids attribute REST API Type and JSON Serializer / Deserializer
 * Represents attribute meta data, not an actual attribute!
 *
 * <strong>Code copied from de.cismet.cids.server.data.legacy.CidsAttribute
 * (cids-server-rest-legacy project) for feature branch #100</strong>
 * TODO: Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @author thorsten
 * @version $Revision$, $Date$
 */
@JsonSerialize(using = CidsAttributeSerializer.class)
@JsonDeserialize(using = CidsAttributeDeserializer.class)
public class CidsAttribute {

    String name;
    String className;
    LinkedHashMap<String, Object> configurationAttributes = new LinkedHashMap<String, Object>();

    /**
     * Creates a new CidsAttribute object.
     */
    public CidsAttribute() {
    }

    /**
     * Creates a new CidsAttribute object.
     *
     */
    public CidsAttribute(final String name, final String className) {
        this.name = name;
        this.className = className;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getAttributeKey() {
        return new StringBuffer(className).append('/').append(name).toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param attrKey DOCUMENT ME!
     */
    public void setAttributeKey(final String attrKey) {
        final int firstAt = attrKey.lastIndexOf('/');
        className = attrKey.substring(0, firstAt);
        name = attrKey.substring(firstAt + 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     */
    public void setConfigFlag(final CidsAttributeConfigurationFlagKey key) {
        configurationAttributes.put(key.toString(),
                true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     * @deprecated operation is not used
     */
    public void removeConfigFlag(final CidsAttributeConfigurationFlagKey key) {
        configurationAttributes.remove(key.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    public void setConfigAttribute(final CidsAttributeConfigurationKey key, final Object value) {
        configurationAttributes.put(key.toString(),
                value);
    }

    /**
     * DOCUMENT ME!
     *
     * @param key DOCUMENT ME!
     * @deprecated operation is not used
     */
    public void removeConfigAttribute(final CidsAttributeConfigurationKey key) {
        configurationAttributes.remove(key.toString());
    }
    
    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    /**
     * Returns all configuration attributes of the cids attribute
     * 
     * @return 
     */
    public LinkedHashMap<String, Object> getConfigurationAttributes() {
        return configurationAttributes;
    }
}

/**
/**
 * Custom serializer for cids attribute REST types.
 * Uses default object serialization for the simplified cids attribute object structure.
 * Conversion from complex legacy MemberAttributeInfo to cids attribute 
 * is performed in CidsClassFactory.
 *
 * @version $Revision$, $Date$
 */
class CidsAttributeSerializer
        extends StdSerializer<CidsAttribute> {

    private final static transient Logger LOG = Logger.getLogger(CidsAttributeSerializer.class);
    
    /**
     * Creates a new CidsAttributeJsonSerializer object.
     */
    public CidsAttributeSerializer() {
        super(CidsAttribute.class);
    }

    @Override
    public void serialize(final CidsAttribute cidsAttribute, final JsonGenerator jg, final SerializerProvider sp)
            throws IOException, JsonGenerationException {
        jg.writeStartObject();
        jg.writeStringField("$self",
                cidsAttribute.getAttributeKey());

        final Set<Map.Entry<String, Object>> entrySet = cidsAttribute.configurationAttributes.entrySet();

        for (final Map.Entry<String, Object> entry : entrySet) {
            final Object value = entry.getValue();
            if(LOG.isDebugEnabled() && !(value instanceof String)) {
                LOG.warn("setting non-string attribute '"+entry.getKey()+"'");
            }
            
            jg.writeObjectField(entry.getKey(), entry.getValue());
        }

        jg.writeEndObject();
    }
}

/**
 * Custom deserializer for cids attribute REST type.
 * 
 * Uses mainly the default object deserialization for the simplified cids attribute object structure.
 * Conversion from cids attribute to complex legacy MemberAttributeInfo is performed in CidsClassFactory.
 *
 * @version $Revision$, $Date$
 */
class CidsAttributeDeserializer
        extends StdDeserializer<CidsAttribute> {

     private final static transient Logger LOG = Logger.getLogger(CidsAttributeDeserializer.class);
    
    /**
     * Creates a new CidsAttributeJsonDeserializer object.
     */
    public CidsAttributeDeserializer() {
        super(CidsAttribute.class);
    }

    @Override
    public CidsAttribute deserialize(final JsonParser jp, final DeserializationContext dc)
            throws IOException, JsonProcessingException {
        final CidsAttribute cidsAttribute = new CidsAttribute();
        boolean keySet = false;

        while (jp.nextValue() != JsonToken.END_OBJECT) {
            final String fieldName = jp.getCurrentName();

            if (!keySet && fieldName.equals("$self")) {
                cidsAttribute.setAttributeKey(jp.getText());
                keySet = true;
            } else {
                switch (jp.getCurrentToken()) {
                    case VALUE_NUMBER_FLOAT: {
                        final double d = jp.getDoubleValue();
                        cidsAttribute.configurationAttributes.put(fieldName, d);
                        break;
                    }

                    case VALUE_NUMBER_INT: {
                        final int i = jp.getIntValue();
                        cidsAttribute.configurationAttributes.put(fieldName, i);
                        break;
                    }

                    case VALUE_NULL:
                    case VALUE_TRUE: {
                        cidsAttribute.configurationAttributes.put(fieldName, true);
                        break;
                    }
                    
                     case VALUE_STRING: {
                        final String s = jp.getValueAsString();
                        cidsAttribute.configurationAttributes.put(fieldName, s);
                        break;
                     }

                    case VALUE_FALSE:
                        cidsAttribute.configurationAttributes.put(fieldName, false);
                        break;

                    default: {
                        LOG.warn("deserializing non-string attribute '"+fieldName+"'");
                        final String s = jp.getText();
                        cidsAttribute.configurationAttributes.put(fieldName, s);
                    }
                }
            }
        }

        return cidsAttribute;
    }
}
