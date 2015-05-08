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

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.cismet.cids.server.api.types.data.CidsClassConfigurationFlagKey;
import de.cismet.cids.server.api.types.data.CidsClassConfigurationKey;

import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@JsonSerialize(using = CidsClassSerializer.class)
@JsonDeserialize(using = CidsClassDeserializer.class)
public class CidsClass {

    //~ Instance fields --------------------------------------------------------

    String key;
    String domain;
    LinkedHashMap<String, CidsAttribute> attributes = new LinkedHashMap<String, CidsAttribute>();
    LinkedHashMap<String, Object> configurationAttributes = new LinkedHashMap<String, Object>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsClass object.
     *
     * @param  key     DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public CidsClass(final String key, final String domain) {
        this.key = key;
        this.domain = domain;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  attr  DOCUMENT ME!
     */
    public void putAttribute(final CidsAttribute attr) {
        attributes.put(attr.getAttributeKey(), attr);
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
     * @param  attr  DOCUMENT ME!
     */
    public void removeAttribute(final CidsAttribute attr) {
        attributes.remove(attr.getAttributeKey());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    public void setConfigFlag(final CidsClassConfigurationFlagKey key) {
        configurationAttributes.put(key.toString(), true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    public void removeConfigFlag(final CidsClassConfigurationFlagKey key) {
        configurationAttributes.remove(key.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public void setConfigAttribute(final CidsClassConfigurationKey key, final Object value) {
        configurationAttributes.put(key.toString(), value);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public void setOtherConfigAttribute(final String key, final Object value) {
        configurationAttributes.put(key, value);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    public void removeConfigAttribute(final CidsClassConfigurationKey key) {
        configurationAttributes.remove(key.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getKey() {
        return new StringBuffer("/").append(domain).append('.').append(key).toString();
    }
}
/**
 * DOCUMENT ME!
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
    public void serialize(final CidsClass c, final JsonGenerator jg, final SerializerProvider provider)
            throws IOException, JsonGenerationException {
        jg.writeStartObject();
        jg.writeStringField("$self", c.getKey());
        // ------ Config
        jg.writeFieldName("configuration");
        jg.writeStartObject();
        final Set<Map.Entry<String, Object>> configAttributesSet = c.configurationAttributes.entrySet();
        for (final Map.Entry<String, Object> entry : configAttributesSet) {
            jg.writeObjectField(entry.getKey(), entry.getValue());
        }
        jg.writeEndObject();
        // ------- Attributes
        final Set<Map.Entry<String, CidsAttribute>> attributesSet = c.attributes.entrySet();
        if (attributesSet.size() > 0) {
            jg.writeFieldName("attributes");
            jg.writeStartObject();
            for (final Map.Entry<String, CidsAttribute> attr : attributesSet) {
                jg.writeObjectField(attr.getValue().key, attr.getValue());
            }
            jg.writeEndObject();
        }
        jg.writeEndObject();
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class CidsClassDeserializer extends StdDeserializer<CidsClass> {

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
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
        // Tools | Templates.
    }
}
