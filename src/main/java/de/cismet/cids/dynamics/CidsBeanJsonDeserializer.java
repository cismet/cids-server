/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class CidsBeanJsonDeserializer extends StdDeserializer<CidsBean> {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            CidsBeanJsonDeserializer.class);

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

        final String wkt;
        final int srid;

        if (skIndex > 0) {
            final String sridKV = ewkt.substring(0, skIndex);
            final int eqIndex = sridKV.indexOf('=');
            wkt = ewkt.substring(skIndex + 1);
            srid = Integer.parseInt(sridKV.substring(eqIndex + 1));
        } else {
            wkt = ewkt;
            srid = -1;
        }

        try {
            final Geometry geom = new WKTReader(new GeometryFactory()).read(wkt);
            if (srid >= 0) {
                geom.setSRID(srid);
            }
            return geom;
        } catch (final ParseException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.error(ex.getMessage(), ex);
            }

            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   _jp  DOCUMENT ME!
     * @param   dc   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IOException              DOCUMENT ME!
     * @throws  JsonProcessingException  DOCUMENT ME!
     * @throws  RuntimeException         DOCUMENT ME!
     */
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

        final HashMap<String, Object> propValueMap = new HashMap<String, Object>();
        try {
            while (jp.nextValue() != JsonToken.END_OBJECT) {
                final String fieldName = jp.getCurrentName();
                if ((!keySet && fieldName.equals(CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER))
                            || fieldName.equals(CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER)) {
                    key = jp.getText();
                    final CidsBeanInfo bInfo = new CidsBeanInfo(key);
                    keySet = true;
                    if (isIntraObjectCacheEnabled() && jp.containsKey(key) && !key.equals("-1")) {
                        cb = jp.get(key);
                        cacheHit = true;
                    } else {
                        cb = CidsBean.createNewCidsBeanFromTableName(bInfo.getDomainKey(), bInfo.getClassKey()); // test
                        cb.quiteSetProperty(cb.getPrimaryKeyFieldname().toLowerCase(),
                            Integer.parseInt(bInfo.getObjectKey()));
                    }
                } else {
                    switch (jp.getCurrentToken()) {
                        case START_ARRAY: {
                            final Collection<CidsBean> array = new ArrayList<CidsBean>();
                            while (jp.nextValue() != JsonToken.END_ARRAY) {
                                final CidsBean arrayObject = jp.readValueAs(CidsBean.class);
                                if (arrayObject != null) {
                                    if (isIntraObjectCacheEnabled() && (arrayObject.getPrimaryKeyValue() != -1)) {
                                        jp.put(arrayObject.getCidsBeanInfo().getJsonObjectKey(), arrayObject);
                                    }
                                    array.add(arrayObject);
                                }
                            }
                            propValueMap.put(fieldName, array);
                            break;
                        }

                        case START_OBJECT: {
                            final CidsBean subObject = jp.readValueAs(CidsBean.class);
                            if (isIntraObjectCacheEnabled() && (subObject.getPrimaryKeyValue() != -1)) {
                                jp.put(subObject.getCidsBeanInfo().getJsonObjectKey(), subObject);
                            }
                            propValueMap.put(fieldName, subObject);
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
                                    propValueMap.put(fieldName, i);
                                } else if (numberClass.equals(Long.class)) {
                                    final long l = jp.getLongValue();
                                    propValueMap.put(fieldName, l);
                                } else if (numberClass.equals(Float.class)) {
                                    final float f = jp.getFloatValue();
                                    propValueMap.put(fieldName, f);
                                } else if (numberClass.equals(Double.class)) {
                                    final double d = jp.getDoubleValue();
                                    propValueMap.put(fieldName, d);
                                } else if (numberClass.equals(java.sql.Timestamp.class)) {
                                    final Timestamp ts = new Timestamp(jp.getLongValue());
                                    propValueMap.put(fieldName, ts);
                                } else if (numberClass.equals(BigDecimal.class)) {
                                    final BigDecimal bd = new BigDecimal(jp.getText());
                                    propValueMap.put(fieldName, bd);
                                } else {
                                    throw new RuntimeException("no handler available for " + numberClass);
                                }
                            } catch (final Exception ex) {
                                throw new RuntimeException("problem during processing of " + fieldName + ". value:"
                                            + jp.getText(),
                                    ex);
                            }
                            break;
                        }

                        case VALUE_NULL: {
                            propValueMap.put(fieldName, null);
                            break;
                        }

                        case VALUE_TRUE: {
                            propValueMap.put(fieldName, true);
                            break;
                        }

                        case VALUE_FALSE: {
                            propValueMap.put(fieldName, false);
                            break;
                        }

                        case VALUE_STRING: {
                            propValueMap.put(fieldName, jp.getText());
                            break;
                        }
                        case VALUE_EMBEDDED_OBJECT: {
                            throw new UnsupportedOperationException("Not supported yet.");
                        }

                        default: {
                            throw new RuntimeException("unhandled case. This is a bad thing (" + fieldName + ")"); // NOI18N
                        }
                    }
                }
            }

            if (!keySet) {
                throw new RuntimeException("Json-Object has to contain a "
                            + CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER + "or a "
                            + CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER); // NOI18N
            }
            if (!cacheHit && (cb != null)) {
                for (final String prop : propValueMap.keySet()) {
                    final Object value = propValueMap.get(prop);

                    if (value instanceof String) {
                        final ObjectAttribute objectAttribute = cb.getMetaObject().getAttributeByFieldName(prop);
                        if (objectAttribute == null) {
                            throw new RuntimeException("unknow property '" + prop + "' in instance of "
                                        + cb.getCidsBeanInfo());
                        }

                        final Class attrClass = BlacklistClassloading.forName(cb.getMetaObject()
                                        .getAttributeByFieldName(
                                            prop).getMai().getJavaclassname());
                        if (attrClass.equals(String.class)) {
                            cb.quiteSetProperty(prop, (String)value);
                        } else if (attrClass.equals(Geometry.class)) {
                            try {
                                cb.quiteSetProperty(prop, fromEwkt((String)value));
                            } catch (Exception e) {
                                throw new RuntimeException("problem during processing of " + prop + "("
                                            + attrClass + "). value:"
                                            + value,
                                    e);
                            }
                        } else {
                            try {
                                cb.quiteSetProperty(prop, value);
                            } catch (Exception e) {
                                throw new RuntimeException("problem bei " + prop + "(" + attrClass + ")",
                                    e);
                            }
                        }
                    } else {
                        if (value instanceof Collection) {
                            cb.getBeanCollectionProperty(prop).addAll((Collection)value);

                            // insert does not work for arrays if statii are not set
                            // -> WHY?!

                            // // Clean up
                            // // No changed flags shall be true.
                            // // All statuses shall be NO_STATUS
                            // final ObjectAttribute oa = cb.getMetaObject().getAttributeByFieldName(prop);
                            // oa.setChanged(false);
                            // final MetaObject dummy = (MetaObject)oa.getValue();
                            // if (dummy != null) {
                            // dummy.setChanged(false);
                            // dummy.forceStatus(MetaObject.NO_STATUS);
                            // dummy.setStatus(MetaObject.NO_STATUS);
                            // final ObjectAttribute[] entries = dummy.getAttribs();
                            // for (final ObjectAttribute entry : entries) {
                            // entry.setChanged(false);
                            // ((MetaObject)entry.getValue()).forceStatus(MetaObject.NO_STATUS);
                            // ((MetaObject)entry.getValue()).setChanged(false);
                            // }
                            // }
                        } else {
                            cb.quiteSetProperty(prop, value);
                        }
                    }
                }
                cb.getMetaObject().setID((cb.getPrimaryKeyValue() != null) ? (int)cb.getPrimaryKeyValue() : -1);
                cb.getMetaObject().forceStatus(MetaObject.NO_STATUS);
                if (isIntraObjectCacheEnabled() && (cb.getPrimaryKeyValue() != -1)) {
                    jp.put(key, cb);
                }
            }
            return cb;
        } catch (Exception ex) {
            throw new RuntimeException("Error during creation of new CidsBean key=" + key, ex); // NOI18N
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
