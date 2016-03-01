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
package de.cismet.cids.dynamics;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.UserContextProvider;

import Sirius.util.collections.MultiMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.apache.commons.lang.StringUtils;

import org.openide.util.Lookup;

import java.io.IOException;

import java.math.BigDecimal;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.cismet.cids.json.IntraObjectCacheJsonParser;

import de.cismet.cids.server.CallServerService;
import de.cismet.cids.server.CallServerServiceProvider;

import de.cismet.commons.classloading.BlacklistClassloading;

import static de.cismet.cids.dynamics.CidsBean.mapper;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class CidsBeanJsonUpdateDeserializer extends StdDeserializer<CidsBean> {

    //~ Instance fields --------------------------------------------------------

    private final boolean patchEnabled;
    private CallServerService metaService;
    private UserContextProvider userContext;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsBeanJsonDeserializer object.
     */
    public CidsBeanJsonUpdateDeserializer() {
        this(false);
    }

    /**
     * Creates a new CidsBeanJsonUpdateDeserializer object.
     *
     * @param  patchEnabled  DOCUMENT ME!
     */
    public CidsBeanJsonUpdateDeserializer(final boolean patchEnabled) {
        super(CidsBean.class);
        this.patchEnabled = patchEnabled;
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

        final String sridKV = ewkt.substring(0, skIndex);
        if (skIndex > 0) {
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
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected CallServerService getMetaService() {
        if (metaService == null) {
            final CallServerServiceProvider csProvider = Lookup.getDefault().lookup(CallServerServiceProvider.class);
            if (csProvider != null) {
                metaService = csProvider.getCallServerService();
            }
        }
        return metaService;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected UserContextProvider getUserContext() {
        if (userContext == null) {
            userContext = Lookup.getDefault().lookup(UserContextProvider.class);
        }
        return userContext;
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
            final Collection<String> fullListColl = new LinkedList<String>();

            final MultiMap fullListMap = new MultiMap();
            final MultiMap addListMap = new MultiMap();
            final MultiMap updateListMap = new MultiMap();
            final MultiMap removeListMap = new MultiMap();

            final HashMap<String, Object> propValueMap = new HashMap<String, Object>();
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
                        final int classId = getMetaService().getClassByTableName(
                                    getUserContext().getUser(),
                                    bInfo.getClassKey().toLowerCase(),
                                    bInfo.getDomainKey())
                                    .getId();
                        final int objectId = Integer.parseInt(bInfo.getObjectKey());
                        cb = getMetaService().getMetaObject(getUserContext().getUser(),
                                    objectId,
                                    classId,
                                    bInfo.getDomainKey()).getBean();
                    }
                } else {
                    switch (jp.getCurrentToken()) {
                        case START_ARRAY: {
                            final String refersToList;
                            final MultiMap listMap;
                            if (fieldName.endsWith(CidsBeanInfo.JSON_CIDS_OBJECT_PATCH_ADD_SUFFIX)) {
                                refersToList = StringUtils.substringBeforeLast(
                                        fieldName,
                                        CidsBeanInfo.JSON_CIDS_OBJECT_PATCH_ADD_SUFFIX);
                                listMap = addListMap;
                            } else if (fieldName.endsWith(CidsBeanInfo.JSON_CIDS_OBJECT_PATCH_UPDATE_SUFFIX)) {
                                refersToList = StringUtils.substringBeforeLast(
                                        fieldName,
                                        CidsBeanInfo.JSON_CIDS_OBJECT_PATCH_UPDATE_SUFFIX);
                                listMap = updateListMap;
                            } else if (fieldName.endsWith(CidsBeanInfo.JSON_CIDS_OBJECT_PATCH_REMOVE_SUFFIX)) {
                                refersToList = StringUtils.substringBeforeLast(
                                        fieldName,
                                        CidsBeanInfo.JSON_CIDS_OBJECT_PATCH_REMOVE_SUFFIX);
                                listMap = removeListMap;
                            } else {
                                fullListColl.add(fieldName);
                                refersToList = fieldName;
                                listMap = fullListMap;
                            }
                            while (jp.nextValue() != JsonToken.END_ARRAY) {
                                final CidsBean arrayObject = jp.readValueAs(CidsBean.class);
                                if (isIntraObjectCacheEnabled() && (arrayObject.getPrimaryKeyValue() != -1)) {
                                    jp.put(arrayObject.getCidsBeanInfo().getJsonObjectKey(), arrayObject);
                                }
                                listMap.put(refersToList, arrayObject);
                            }

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
                            } catch (Exception ex) {
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
                            throw new RuntimeException("unhandled case. This is a bad thing"); // NOI18N
                        }
                    }
                }
            }

            if (!keySet) {
                throw new RuntimeException("Json-Object has to contain a "
                            + CidsBeanInfo.JSON_CIDS_OBJECT_KEY_IDENTIFIER + "or a "
                            + CidsBeanInfo.JSON_CIDS_OBJECT_KEY_REFERENCE_IDENTIFIER); // NOI18N
            }

            for (final String prop : propValueMap.keySet()) {
                final Object value = propValueMap.get(prop);

                if (value instanceof String) {
                    final Class attrClass = BlacklistClassloading.forName(cb.getMetaObject().getAttributeByFieldName(
                                prop).getMai().getJavaclassname());
                    if (attrClass.equals(String.class)) {
                        cb.setPropertyForceChanged(prop, (String)value);
                    } else if (attrClass.equals(Geometry.class)) {
                        try {
                            cb.setPropertyForceChanged(prop, fromEwkt((String)value));
                        } catch (Exception e) {
                            throw new RuntimeException("problem during processing of " + prop + "("
                                        + attrClass + "). value:"
                                        + value,
                                e);
                        }
                    } else {
                        try {
                            cb.setPropertyForceChanged(prop, mapper.readValue(jp, attrClass));
                        } catch (Exception e) {
                            throw new RuntimeException("problem bei " + prop + "(" + attrClass + ")",
                                e);
                        }
                    }
                } else {
                    cb.setPropertyForceChanged(prop, value);
                }
            }

            for (final String listName : fullListColl) {
                final List<CidsBean> origColl = cb.getBeanCollectionProperty(listName);

                // each object from the json list collection that not exists
                // in the original collection have to be added.
                final List<CidsBean> toAddColl = new ArrayList<CidsBean>();

                // contains first all objects of the original collection.
                // each object from the json list collection that already exists
                // in this collection is removed from it, so that after processing all
                // objects from the json list, the remaing objects are all the objects
                // that have to be removed from the original collection. (because they dont
                // have be found in the json collection).
                if (fullListMap.get(listName) == null) {
                    origColl.clear();
                } else {
                    final List<CidsBean> toRemoveColl = new ArrayList<CidsBean>(origColl);

                    final HashMap<CidsBean, Integer> toUpdateMap = new HashMap<CidsBean, Integer>();

                    // processing the objects of the json list
                    for (final CidsBean bean : (Collection<CidsBean>)fullListMap.get(listName)) {
                        final int indexOf = toRemoveColl.indexOf(bean);
                        if (indexOf < 0) { // not found in the original collection
                            toAddColl.add(bean);
                        } else {           // found => update & remove from toRemove (yes, thats right
                                           // !)
                            toUpdateMap.put(bean, indexOf);
                            toRemoveColl.remove(bean);
                        }
                    }

                    // here happens all the changes to the original list collection

                    // - add
                    origColl.addAll(toAddColl);

                    // - update
                    for (final CidsBean toUpdateBean : toUpdateMap.keySet()) {
                        final Integer indexOf = toUpdateMap.get(toUpdateBean);
                        origColl.remove(indexOf.intValue());
                        origColl.add(toUpdateBean);
//                        origColl.set(indexOf, toUpdateBean);
                    }

                    // - remove
                    origColl.removeAll(toRemoveColl);
                }
            }

            // ignore the patch tags if patch is not enabled
            if (isPatchEnabled()) {
                // add
                for (final String listName : (Set<String>)addListMap.keySet()) {
                    final List<CidsBean> origColl = cb.getBeanCollectionProperty(listName);
                    for (final CidsBean toAddBean : (Collection<CidsBean>)addListMap.get(listName)) {
                        origColl.add(toAddBean);
                    }
                }

                // update
                for (final String listName : (Set<String>)updateListMap.keySet()) {
                    final HashMap<CidsBean, Integer> toUpdateMap = new HashMap<CidsBean, Integer>();

                    // first filling HashMap for more performance later.
                    final List<CidsBean> toUpdateColl = (List<CidsBean>)updateListMap.get(listName);
                    for (int indexOfUpdate = 0; indexOfUpdate < toUpdateColl.size(); indexOfUpdate++) {
                        final CidsBean toUpdateBean = toUpdateColl.get(indexOfUpdate);
                        toUpdateMap.put(toUpdateBean, indexOfUpdate);
                    }

                    // now processing update
                    final List<CidsBean> origColl = cb.getBeanCollectionProperty(listName);
                    for (int indexOfOrig = 0; indexOfOrig < origColl.size(); indexOfOrig++) {
                        final CidsBean origBean = origColl.get(indexOfOrig);
                        if (toUpdateMap.containsKey(origBean)) {
                            final Integer indexOfUpdate = toUpdateMap.get(origBean);
                            final CidsBean toUpdateBean = toUpdateColl.get(indexOfUpdate);
                            origColl.set(indexOfOrig, toUpdateBean);
                            toUpdateBean.getMetaObject()
                                    .getAttributeByFieldName(listName)
                                    .getParentObject()
                                    .setStatus(MetaObject.MODIFIED);
                        }
                    }
                }

                // remove
                for (final String listName : (Set<String>)removeListMap.keySet()) {
                    final List<CidsBean> origColl = cb.getBeanCollectionProperty(listName);
                    for (final CidsBean toRemoveBean
                                : (Collection<CidsBean>)removeListMap.get(listName)) {
                        origColl.remove(toRemoveBean);
                    }
                }
            }

            if (isIntraObjectCacheEnabled() && (cb.getPrimaryKeyValue() != -1)) {
                jp.put(key, cb);
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
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected boolean isPatchEnabled() {
        return patchEnabled;
    }
}
