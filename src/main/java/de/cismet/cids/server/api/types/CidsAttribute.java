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
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.cismet.cids.server.api.types.data.CidsAttributeConfigurationFlagKey;
import de.cismet.cids.server.api.types.data.CidsAttributeConfigurationKey;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@JsonSerialize(using = CidsAttributeJsonSerializer.class)
@JsonDeserialize(using = CidsAttributeJsonDeserializer.class)
public class CidsAttribute {

    //~ Instance fields --------------------------------------------------------

    String key;
    String classKey;
    LinkedHashMap<String, Object> configurationAttributes = new LinkedHashMap<String, Object>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsAttribute object.
     */
    public CidsAttribute() {
    }

    /**
     * Creates a new CidsAttribute object.
     *
     * @param  key       DOCUMENT ME!
     * @param  classKey  DOCUMENT ME!
     */
    public CidsAttribute(final String key, final String classKey) {
        this.key = key;
        this.classKey = classKey;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getAttributeKey() {
        return new StringBuffer(classKey).append('/').append(key).toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  attrKey  DOCUMENT ME!
     */
    public void setAttributeKey(final String attrKey) {
        final int firstAt = attrKey.lastIndexOf('/');
        classKey = attrKey.substring(0, firstAt);
        key = attrKey.substring(firstAt + 1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    public void setConfigFlag(final CidsAttributeConfigurationFlagKey key) {
        configurationAttributes.put(key.toString(), true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    public void removeConfigFlag(final CidsAttributeConfigurationFlagKey key) {
        configurationAttributes.remove(key.toString());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key    DOCUMENT ME!
     * @param  value  DOCUMENT ME!
     */
    public void setConfigAttribute(final CidsAttributeConfigurationKey key, final Object value) {
        configurationAttributes.put(key.toString(), value);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  key  DOCUMENT ME!
     */
    public void removeConfigAttribute(final CidsAttributeConfigurationKey key) {
        configurationAttributes.remove(key.toString());
    }
}
/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class CidsAttributeJsonSerializer extends StdSerializer<CidsAttribute> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsAttributeJsonSerializer object.
     */
    public CidsAttributeJsonSerializer() {
        super(CidsAttribute.class);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void serialize(final CidsAttribute t, final JsonGenerator jg, final SerializerProvider sp)
            throws IOException, JsonGenerationException {
        jg.writeStartObject();
        jg.writeStringField("$self", t.getAttributeKey());
        final Set<Map.Entry<String, Object>> entrySet = t.configurationAttributes.entrySet();
        for (final Map.Entry<String, Object> entry : entrySet) {
            jg.writeObjectField(entry.getKey(), entry.getValue());
        }

        jg.writeEndObject();
    }
}

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
class CidsAttributeJsonDeserializer extends StdDeserializer<CidsAttribute> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsAttributeJsonDeserializer object.
     */
    public CidsAttributeJsonDeserializer() {
        super(CidsAttribute.class);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public CidsAttribute deserialize(final JsonParser jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        final CidsAttribute ca = new CidsAttribute();
        boolean keySet = false;
        while (jp.nextValue() != JsonToken.END_OBJECT) {
            final String fieldName = jp.getCurrentName();
            if (!keySet && fieldName.equals("key")) {
                ca.setAttributeKey(jp.getText());
                keySet = true;
            } else {
                switch (jp.getCurrentToken()) {
                    case VALUE_NUMBER_FLOAT: {
                        final double d = jp.getDoubleValue();
                        ca.configurationAttributes.put(fieldName, d);
                        break;
                    }
                    case VALUE_NUMBER_INT: {
                        final int i = jp.getIntValue();
                        ca.configurationAttributes.put(fieldName, i);
                        break;
                    }
                    case VALUE_NULL:
                    case VALUE_TRUE: {
                        ca.configurationAttributes.put(fieldName, true);
                        break;
                    }
                    case VALUE_FALSE: {
                        break;
                    }
                    default: {
                        final String s = jp.getText();
                        ca.configurationAttributes.put(fieldName, s);
                    }
                }
            }
        }

        return ca;
    }
}
