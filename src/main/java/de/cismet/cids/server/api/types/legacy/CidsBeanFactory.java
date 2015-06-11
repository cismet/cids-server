/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.api.types.legacy;

import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
import Sirius.server.middleware.types.LightweightMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;
import de.cismet.cids.dynamics.CidsBean;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 * Factory with help method for converting between LightweightMetaObjects and
 * cids Beans.
 *
 * @author Pascal Dihé
 */
public class CidsBeanFactory {

    //~ Static fields/initializers ---------------------------------------------
    /**
     * toString Property of CidsBean representing a serialized
     * LightweightMetaObject.
     */
    public final static String LEGACY_DISPLAY_NAME = "$legacyDisplayName";

    private static final transient Logger LOG = Logger.getLogger(CidsBeanFactory.class);
    private static final CidsBeanFactory factory = new CidsBeanFactory();

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
     * @return DOCUMENT ME!
     */
    public static final CidsBeanFactory getFactory() {
        return factory;
    }

    /**
     * Tries to derive LightweightMetaObject representation fields from a
     * cidsBean instance
     *
     * @param cidsBean
     * @return String array of representationFields (may be empty)
     */
    public String[] representationFieldsFromCidsBean(final CidsBean cidsBean) {
        final LinkedList<String> representationFields = new LinkedList<String>();

        for (final String propertyName : cidsBean.getPropertyNames()) {
            if (propertyName.indexOf('$') != 0) {
                representationFields.add(propertyName);
            }
        }

        return representationFields.toArray(new String[representationFields.size()]);
    }

    /**
     * Helper Method for creating sub .
     *
     * @param cidsBean DOCUMENT ME!
     * @param domain DOCUMENT ME!
     * @param user DOCUMENT ME!
     * @param classNameCache
     *
     * @return DOCUMENT ME!
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
     * Convenience Method that automatically derives the LWMO representation
     * fields from the CidsBean and assumes that a
     * {@link DummyRepresentationFormater} can be created based on the
     * {@link #LEGACY_DISPLAY_NAME} property.
     *
     * @param cidsBean
     * @param classId
     * @param domain
     * @param user
     * @param classNameCache
     * @return
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
     * Transforms a CidsBean into a LightweightMetaObject.
     *
     * @param cidsBean DOCUMENT ME!
     * @param classId DOCUMENT ME!
     * @param domain DOCUMENT ME!
     * @param user DOCUMENT ME!
     * @param representationFields DOCUMENT ME!
     * @param representationFormater DOCUMENT ME!
     * @param classNameCache
     *
     * @return DOCUMENT ME!
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
                            ((Collection) property).size());
                    final Iterator cidsBeanIerator = ((Collection) property).iterator();

                    while (cidsBeanIerator.hasNext()) {
                        final Object object = cidsBeanIerator.next();
                        if ((object != null) && CidsBean.class.isAssignableFrom(object.getClass())) {
                            final CidsBean subCidsBean = (CidsBean) object;
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
                    final CidsBean subCidsBean = (CidsBean) property;
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

        if (representationFormater != null) {
            lightweightMetaObject.setFormater(representationFormater);
        } else if (this.isLightweightMetaObject(cidsBean)) {
            LOG.debug(LEGACY_DISPLAY_NAME + " property set in cids bean, creating DummyRepresentationFormater");
            lightweightMetaObject.setFormater(new DummyRepresentationFormater(cidsBean.getProperty(LEGACY_DISPLAY_NAME).toString()));
        }

        return lightweightMetaObject;
    }

    /**
     * Creates a LightweightMetaObject from a CidsBean
     * 
     * @param lightweightMetaObject
     * @param metaClass
     * @return 
     */
    public CidsBean cidsBeanFromLightweightMetaObject(
            LightweightMetaObject lightweightMetaObject,
            MetaClass metaClass) {

        final MetaObject metaObject = metaClass.getEmptyInstance();
        metaObject.setID(lightweightMetaObject.getObjectID());
        final CidsBean cidsBean = metaObject.getBean();

        for (String attributeName : lightweightMetaObject.getKnownAttributeNames()) {
            final Object value = lightweightMetaObject.getLWAttribute(attributeName);
            try {
                cidsBean.setProperty(attributeName, value);
            } catch (Exception ex) {
                LOG.warn("could not set attribute '" + attributeName + "' of LightweightMetaObject '"
                        + lightweightMetaObject + "' to CidsBean: " + ex.getMessage(), ex);
            }
        }

        try {
            cidsBean.setProperty(LEGACY_DISPLAY_NAME, lightweightMetaObject.toString());
        } catch (Exception ex) {
            LOG.warn("could not toStringRepresentation of LightweightMetaObject '"
                    + lightweightMetaObject + "' to CidsBean: " + ex.getMessage(), ex);
        }
        
        return cidsBean;
    }

    /**
     * Helper Method for checking if a cids bean is a potential
     * LightweightMetaObject. Note: This method relies on meta information
     * ($properties) of the cids bean!
     *
     * @param cidsBean
     * @return
     */
    public boolean isLightweightMetaObject(final CidsBean cidsBean) {
        return cidsBean.getProperty(LEGACY_DISPLAY_NAME) != null
                && !cidsBean.getProperty(LEGACY_DISPLAY_NAME).toString().isEmpty();
    }
}

/**
 * DummyRepresentationFormater that does not perform any formatting by itself
 * bur returns an already formated string for the toString Method.
 *
 * @author Pascal Dihé
 */
class DummyRepresentationFormater extends AbstractAttributeRepresentationFormater {

    final String formattedToString;

    DummyRepresentationFormater(final String formattedToString) {
        this.formattedToString = formattedToString;
    }

    @Override
    public String getRepresentation() {
        return this.formattedToString;
    }
}
