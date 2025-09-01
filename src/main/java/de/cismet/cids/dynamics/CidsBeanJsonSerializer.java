/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.dynamics;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;

import java.math.BigDecimal;

import java.util.List;

import de.cismet.cids.json.IntraObjectCacheJsonGenerator;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class CidsBeanJsonSerializer extends StdSerializer<CidsBean> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsAttributeJsonSerializer object.
     */
    public CidsBeanJsonSerializer() {
        super(CidsBean.class);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   geom  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toEwkt(final Geometry geom) {
        if (geom == null) {
            return null;
        }
        return "SRID=" + geom.getSRID() + ";" + new WKTWriter().write(geom);
    }

    @Override
    public void serialize(final CidsBean cb, final JsonGenerator _jg, final SerializerProvider sp) throws IOException,
        JsonGenerationException {
        IntraObjectCacheJsonGenerator jg;
        if (_jg instanceof IntraObjectCacheJsonGenerator) {
            jg = (IntraObjectCacheJsonGenerator)_jg;
        } else {
            jg = new IntraObjectCacheJsonGenerator(_jg);
            jg.setParams(cb.getJsonSerializerParams());
        }

        jg.writeStartObject();

        final boolean selfOnly = !jg.checkLevelExpand()
                    || (jg.getParams().isCacheDuplicates()
                        && jg.containsKey(cb.getCidsBeanInfo().getJsonObjectKey()));
        if (!selfOnly) {
            jg.writeStringField(
                CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER,
                cb.getCidsBeanInfo().getJsonObjectKey());
            final String[] propNames = cb.getPropertyNames();
            for (int i = 0; i < propNames.length; ++i) {
                final String attribute = propNames[i];
                jg.appendPropertyName(attribute);
                if (jg.checkFields()) {
                    final Object object = cb.getProperty(attribute);
                    if (object instanceof CidsBean) {
                        jg.writeObjectField(attribute, object);
                    } else if (object instanceof List) {
                        final List<CidsBean> collection = (List<CidsBean>)object;
                        jg.writeArrayFieldStart(attribute);
                        for (int j = 0; j < collection.size(); ++j) {
                            final CidsBean colBean = collection.get(j);
                            jg.writeObject(colBean);
                        }
                        jg.writeEndArray();
                    } else {
                        if (object == null) {
                            if (!jg.getParams().isOmitNull()) {
                                jg.writeNullField(attribute);
                            }
                        } else if (object instanceof Geometry) {
                            jg.writeStringField(attribute, StringEscapeUtils.escapeJava(toEwkt((Geometry)object)));
                        } else if (object instanceof BigDecimal) {
                            jg.writeNumberField(attribute, (BigDecimal)object);
                        } else if (object instanceof Double) {
                            jg.writeNumberField(attribute, (Double)object);
                        } else if (object instanceof Float) {
                            jg.writeNumberField(attribute, (Float)object);
                        } else if (object instanceof Integer) {
                            jg.writeNumberField(attribute, (Integer)object);
                        } else if (object instanceof Long) {
                            jg.writeNumberField(attribute, (Long)object);
                        } else if (object instanceof Boolean) {
                            jg.writeBooleanField(attribute, (Boolean)object);
                        } else if (object instanceof String) {
                            jg.writeStringField(attribute, String.valueOf(object));
                        } else {
                            jg.writeObjectField(attribute, object);
                        }
                    }
                }
            }
            if (isIntraObjectCacheEnabled()) {
                jg.put(cb.getCidsBeanInfo().getJsonObjectKey(), cb);
            }
        } else {
            jg.writeStringField(
                CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER,
                cb.getCidsBeanInfo().getJsonObjectKey());
        }
        jg.writeEndObject();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected boolean isIntraObjectCacheEnabled() {
        return false;
    }
}
