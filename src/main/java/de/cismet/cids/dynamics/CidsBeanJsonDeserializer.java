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
package de.cismet.cids.dynamics;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import java.io.IOException;

import java.math.BigDecimal;

import java.sql.Timestamp;

import de.cismet.cids.json.IntraObjectCacheJsonParser;

import de.cismet.commons.classloading.BlacklistClassloading;

import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_EMBEDDED_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_FALSE;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_FLOAT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static com.fasterxml.jackson.core.JsonToken.VALUE_TRUE;

import static de.cismet.cids.dynamics.CidsBean.mapper;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class CidsBeanJsonDeserializer extends StdDeserializer<CidsBean> {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsBeanJsonDeserializer object.
     */
    public CidsBeanJsonDeserializer() {
        super(CidsBean.class);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   ewkt  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static Geometry fromEwkt(final String ewkt) {
        final int skIndex = ewkt.indexOf(';');
        if (skIndex > 0) {
            final String sridKV = ewkt.substring(0, skIndex);
            final int eqIndex = sridKV.indexOf('=');

            if (eqIndex > 0) {
                final int srid = Integer.parseInt(sridKV.substring(eqIndex + 1));
                final String wkt = ewkt.substring(skIndex + 1);
                try {
                    final Geometry geom = new WKTReader(new GeometryFactory()).read(wkt);
                    geom.setSRID(srid);
                    return geom;
                } catch (final ParseException ex) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public CidsBean deserialize(final JsonParser _jp, final DeserializationContext dc) throws IOException,
        JsonProcessingException {
        boolean cacheHit = false;
        boolean keySet = false;
        CidsBean cb = null;
        String key = "???";
        IntraObjectCacheJsonParser jp = null;
        if (_jp instanceof IntraObjectCacheJsonParser) {
            jp = (IntraObjectCacheJsonParser)_jp;
        } else {
            jp = new IntraObjectCacheJsonParser(_jp);
        }

        try {
            while (jp.nextValue() != JsonToken.END_OBJECT) {
                final String fieldName = jp.getCurrentName();
                if (!cacheHit) {
                    if ((!keySet && fieldName.equals(CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER))
                                || fieldName.equals(CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER)) {
                        key = jp.getText();
                        final CidsBeanInfo bInfo = new CidsBeanInfo(key);
                        keySet = true;
                        if (isIntraObjectCacheEnabled() && jp.containsKey(key)) {
                            cb = jp.get(key);
                            cacheHit = true;
                        } else {
                            cb = CidsBean.createNewCidsBeanFromTableName(bInfo.getDomainKey(), bInfo.getClassKey()); // test
                        }
                    } else {
                        if (cb == null) {
                            throw new RuntimeException("Json-Object has to start with a "
                                        + CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER + "or with a "
                                        + CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER);                   // NOI18N
                        }
                        switch (jp.getCurrentToken()) {
                            case START_ARRAY: {
                                while (jp.nextValue() != JsonToken.END_ARRAY) {
                                    final CidsBean arrayObject = jp.readValueAs(CidsBean.class);
                                    if (isIntraObjectCacheEnabled()) {
                                        jp.put(arrayObject.getCidsBeanInfo().getJsonObjectKey(), arrayObject);
                                    }
                                    cb.addCollectionElement(fieldName, arrayObject);
                                }
                                // Clean up
                                // No changed flags shall be true.
                                // All statuses shall be NO_STATUS
                                final ObjectAttribute oa = cb.getMetaObject().getAttributeByFieldName(fieldName);
                                oa.setChanged(false);
                                final MetaObject dummy = (MetaObject)oa.getValue();
                                if (dummy != null) {
                                    dummy.setChanged(false);
                                    dummy.forceStatus(MetaObject.NO_STATUS);
                                    dummy.setStatus(MetaObject.NO_STATUS);
                                    final ObjectAttribute[] entries = dummy.getAttribs();
                                    for (final ObjectAttribute entry : entries) {
                                        entry.setChanged(false);
                                        ((MetaObject)entry.getValue()).forceStatus(MetaObject.NO_STATUS);
                                        ((MetaObject)entry.getValue()).setChanged(false);
                                    }
                                }
                                break;
                            }

                            case START_OBJECT: {
                                final CidsBean subObject = jp.readValueAs(CidsBean.class);
                                if (isIntraObjectCacheEnabled()) {
                                    jp.put(subObject.getCidsBeanInfo().getJsonObjectKey(), subObject);
                                }
                                cb.quiteSetProperty(fieldName, subObject);
                                break;
                            }

                            case VALUE_NUMBER_FLOAT:
                            case VALUE_NUMBER_INT: {
                                try {
                                    final Class numberClass = BlacklistClassloading.forName(cb.getMetaObject()
                                                    .getAttributeByFieldName(
                                                        fieldName).getMai().getJavaclassname());
                                    if (numberClass.equals(Integer.class)) {
                                        final int i = jp.getIntValue();
                                        cb.quiteSetProperty(fieldName, i);
                                    } else if (numberClass.equals(Long.class)) {
                                        final long l = jp.getLongValue();
                                        cb.quiteSetProperty(fieldName, l);
                                    } else if (numberClass.equals(Float.class)) {
                                        final float f = jp.getFloatValue();
                                        cb.quiteSetProperty(fieldName, f);
                                    } else if (numberClass.equals(Double.class)) {
                                        final double d = jp.getDoubleValue();
                                        cb.quiteSetProperty(fieldName, d);
                                    } else if (numberClass.equals(java.sql.Timestamp.class)) {
                                        final Timestamp ts = new Timestamp(jp.getLongValue());
                                        cb.quiteSetProperty(fieldName, ts);
                                    } else if (numberClass.equals(BigDecimal.class)) {
                                        final BigDecimal bd = new BigDecimal(jp.getText());
                                        cb.quiteSetProperty(fieldName, bd);
                                    } else {
                                        throw new RuntimeException("no handler available for " + numberClass);
                                    }
                                } catch (Exception ex) {
                                    throw new RuntimeException("problem during processing of " + fieldName + ". value:"
                                                + jp.getText(),
                                        ex);
                                }
                                break;
                            }

                            case VALUE_NULL: {
                                cb.quiteSetProperty(fieldName, null);
                                break;
                            }

                            case VALUE_TRUE: {
                                cb.quiteSetProperty(fieldName, true);
                                break;
                            }

                            case VALUE_FALSE: {
                                cb.quiteSetProperty(fieldName, false);
                                break;
                            }

                            case VALUE_STRING: {
                                final Class attrClass = BlacklistClassloading.forName(cb.getMetaObject()
                                                .getAttributeByFieldName(
                                                    fieldName).getMai().getJavaclassname());
                                if (attrClass.equals(String.class)) {
                                    final String s = jp.getText();
                                    cb.quiteSetProperty(fieldName, s);
                                } else if (attrClass.equals(Geometry.class)) {
                                    try {
                                        final String s = jp.getText();
                                        cb.quiteSetProperty(fieldName, fromEwkt(s));
                                    } catch (Exception e) {
                                        throw new RuntimeException("problem during processing of " + fieldName + "("
                                                    + attrClass + "). value:"
                                                    + jp.getText(),
                                            e);
                                    }
                                } else {
                                    try {
                                        cb.quiteSetProperty(fieldName, mapper.readValue(jp, attrClass));
                                    } catch (Exception e) {
                                        throw new RuntimeException("problem bei " + fieldName + "(" + attrClass + ")",
                                            e);
                                    }
                                }

                                break;
                            }
                            case VALUE_EMBEDDED_OBJECT: {
                                throw new UnsupportedOperationException("Not supported yet.");
                            }

                            default: {
                                throw new RuntimeException("unhandled case. This is a bad thing"); // NOI18N
                            }
                        }
                    }
                }
            }
            cb.getMetaObject().setID((cb.getPrimaryKeyValue() != null) ? (int)cb.getPrimaryKeyValue() : -1);
            cb.getMetaObject().forceStatus(MetaObject.NO_STATUS);
            if (isIntraObjectCacheEnabled()) {
                jp.put(key, cb);
            }
            return cb;
        } catch (Exception ex) {
            throw new RuntimeException("Error during creation of new CidsBean key=" + key, ex);    // NOI18N
        }
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
