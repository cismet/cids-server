/**
 * *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 * 
* ... and it just works.
 * 
***************************************************
 */
package de.cismet.cids.server.api.types.legacy;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;

import Sirius.server.middleware.types.MetaClass;

import de.cismet.cids.server.api.types.CidsAttribute;
import de.cismet.cids.server.api.types.CidsClass;
import de.cismet.cids.server.api.types.configkeys.AttributeConfig;
import de.cismet.cids.server.api.types.configkeys.CidsAttributeConfigurationFlagKey;
import de.cismet.cids.server.api.types.configkeys.CidsAttributeConfigurationKey;
import de.cismet.cids.server.api.types.configkeys.CidsClassConfigurationFlagKey;
import de.cismet.cids.server.api.types.configkeys.CidsClassConfigurationKey;
import de.cismet.cids.server.api.types.configkeys.ClassConfig;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory class for converting between legacy cids types and REST/JSON types.
 * TODO: Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @author Pascal Dihé
 */
public class CidsClassFactory {

    private final static transient Logger LOG = Logger.getLogger(UserFactory.class);
    private final static CidsClassFactory factory = new CidsClassFactory();

    private CidsClassFactory() {
    }

    public final static CidsClassFactory getFactory() {
        return factory;
    }

    /**
     *  Transforms a cids legacy meta class object into a cids rest API class object
     * 
     * <strong>Code copied from
     * de.cismet.cids.server.backend.legacy.LegacyCoreBackend.createCidsClass()
     * (cids-server-rest-legacy project)</strong>
     *
     *
     * @param metaClass
     * @return
     */
    public CidsClass restCidsClassFromLegacyCidsClass(final MetaClass metaClass) {
        final CidsClass cidsClass = new CidsClass((String) metaClass.getTableName(),
                metaClass.getDomain());

        for (final MemberAttributeInfo mai : (Collection<MemberAttributeInfo>) metaClass.getMemberAttributeInfos()
                .values()) {
            try {
                final CidsAttribute cidsAttribute
                        = new CidsAttribute((String) mai.getFieldName(), (String) metaClass.getTableName());

                // KEY
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.Key.NAME,
                        mai.getName());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.Key.DEFAULT_VALUE,
                        mai.getDefaultValue());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.Key.ARRAY_KEY_FIELD_NAME,
                        mai.getArrayKeyFieldName());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.Key.JAVACLASS_NAME,
                        mai.getJavaclassname());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.Key.POSITION,
                        mai.getPosition());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.Key.REFERENCE_TYPE,
                        mai.getTypeId());

                // XPKey
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.XPKey.COMPLEX_EDITOR_XP,
                        mai.getComplexEditor());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.XPKey.EDITOR_XP,
                        mai.getEditor());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.XPKey.FROM_STRING_XP,
                        mai.getFromString());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.XPKey.RENDERER_XP,
                        mai.getRenderer());
                setAttributeConfig(cidsAttribute,
                        AttributeConfig.XPKey.TO_STRING_XP,
                        mai.getToString());

                // FLAGKEY
                setAttributeFlag(cidsAttribute,
                        AttributeConfig.FlagKey.ARRAY,
                        mai.isArray());
                setAttributeFlag(cidsAttribute,
                        AttributeConfig.FlagKey.EXTENSION_ATTRIBUTE,
                        mai.isExtensionAttribute());
                setAttributeFlag(cidsAttribute,
                        AttributeConfig.FlagKey.FOREIGN_KEY,
                        mai.isForeignKey());
                setAttributeFlag(cidsAttribute,
                        AttributeConfig.FlagKey.INDEXED,
                        mai.isIndexed());
                setAttributeFlag(cidsAttribute,
                        AttributeConfig.FlagKey.OPTIONAL,
                        mai.isOptional());
                setAttributeFlag(cidsAttribute,
                        AttributeConfig.FlagKey.VIRTUAL,
                        mai.isVirtual());
                setAttributeFlag(cidsAttribute,
                        AttributeConfig.FlagKey.VISIBLE,
                        mai.isVisible());

                cidsClass.putAttribute(cidsAttribute);
            } catch (final Exception ex) {
                LOG.error(ex, ex);
            }
        }

        final Collection<ClassAttribute> metaClassAttributes = (Collection<ClassAttribute>) metaClass.getAttributes();
        final Map<String, ClassAttribute> caMap = new HashMap<String, ClassAttribute>();

        for (final ClassAttribute metaClassAttribute : metaClassAttributes) {
            caMap.put(metaClassAttribute.getName(),
                    metaClassAttribute);
        }

        // KEY
        setClassConfig(cidsClass,
                ClassConfig.Key.ATTRIBUTE_POLICY,
                metaClass.getAttributePolicy());
        setClassConfig(cidsClass,
                ClassConfig.Key.CLASS_ICON,
                metaClass.getIcon());
        setClassConfig(cidsClass,
                ClassConfig.Key.NAME,
                metaClass.getName());
        setClassConfig(cidsClass,
                ClassConfig.Key.PK_FIELD,
                metaClass.getPrimaryKey());
        setClassConfig(cidsClass,
                ClassConfig.Key.POLICY,
                metaClass.getPolicy());
        setClassConfig(cidsClass,
                ClassConfig.Key.OBJECT_ICON,
                metaClass.getObjectIcon());
        setClassConfig(cidsClass,
                ClassConfig.Key.FEATURE_BG,
                caMap.remove("FEATURE_BG"));
        setClassConfig(cidsClass,
                ClassConfig.Key.FEATURE_FG,
                caMap.remove("FEATURE_FG"));
        setClassConfig(cidsClass,
                ClassConfig.Key.FEATURE_POINT_SYMBOL,
                caMap.remove("FEATURE_POINT_SYMBOL"));
        setClassConfig(cidsClass,
                ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_X,
                caMap.remove("FEATURE_POINT_SYMBOL_SWEETSPOT_X"));
        setClassConfig(cidsClass,
                ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_Y,
                caMap.remove("FEATURE_POINT_SYMBOL_SWEETSPOT_Y"));

        // XPKEY
        setClassConfig(cidsClass,
                ClassConfig.XPKey.AGGREGATION_RENDERER_XP,
                caMap.remove("AGGREGATION_RENDERER"));
        setClassConfig(cidsClass,
                ClassConfig.XPKey.EDITOR_XP,
                metaClass.getEditor());
        setClassConfig(cidsClass,
                ClassConfig.XPKey.FEATURE_RENDERER_XP,
                caMap.remove("FEATURE_RENDERER"));
//        setClassConfig(cidsClass, ClassConfig.XPKey.FROM_STRING_XP, );
        setClassConfig(cidsClass,
                ClassConfig.XPKey.ICON_FACTORY_XP,
                caMap.remove("ICON_FACTORY"));
        setClassConfig(cidsClass,
                ClassConfig.XPKey.RENDERER_XP,
                metaClass.getRenderer());
        setClassConfig(cidsClass,
                ClassConfig.XPKey.TO_STRING_XP,
                metaClass.getToString());
        setClassConfig(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE,
                caMap.remove("FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE"));
        setClassConfig(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_LAYER,
                caMap.remove("FEATURE_SUPPORTING_RASTER_SERVICE_LAYER"));
        setClassConfig(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_NAME,
                caMap.remove("FEATURE_SUPPORTING_RASTER_SERVICE_NAME"));
        setClassConfig(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL,
                caMap.remove("FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL"));
        setClassConfig(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP,
                caMap.remove("FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP"));

        // FLAGKEY
        setClassFlag(cidsClass,
                ClassConfig.FlagKey.ARRAY_LINK,
                metaClass.isArrayElementLink());
        setClassFlag(cidsClass, ClassConfig.FlagKey.HIDE_FEATURE, caMap.remove("HIDE_FEATURE") != null);
        setClassFlag(cidsClass,
                ClassConfig.FlagKey.INDEXED,
                metaClass.isIndexed());
        setClassFlag(cidsClass, ClassConfig.FlagKey.REASONABLE_FEW, caMap.remove("REASONABLE_FEW") != null);

        for (final String caName : caMap.keySet()) {
            final ClassAttribute otherAttribute = caMap.get(caName);
            cidsClass.setOtherConfigAttribute(caName, otherAttribute);
        }

        return cidsClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param cidsClass DOCUMENT ME!
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    private void setClassConfig(final CidsClass cidsClass, final CidsClassConfigurationKey key, final Object value) {
        if ((cidsClass != null) && (key != null) && (value != null)) {
            cidsClass.setConfigAttribute(key, value);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param cidsClass DOCUMENT ME!
     * @param flagKey DOCUMENT ME!
     * @param isSet DOCUMENT ME!
     */
    private void setClassFlag(final CidsClass cidsClass, final CidsClassConfigurationFlagKey flagKey,
            final boolean isSet) {
        if ((cidsClass != null) && (flagKey != null) && isSet) {
            cidsClass.setConfigFlag(flagKey);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param cidsAttribute DOCUMENT ME!
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    private void setAttributeConfig(final CidsAttribute cidsAttribute, final CidsAttributeConfigurationKey key,
            final Object value) {
        if ((cidsAttribute != null) && (key != null) && (value != null)) {
            cidsAttribute.setConfigAttribute(key, value);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param cidsAttribute DOCUMENT ME!
     * @param flagKey DOCUMENT ME!
     * @param isSet DOCUMENT ME!
     */
    private void setAttributeFlag(final CidsAttribute cidsAttribute, final CidsAttributeConfigurationFlagKey flagKey,
            final boolean isSet) {
        if ((cidsAttribute != null) && (flagKey != null) && isSet) {
            cidsAttribute.setConfigFlag(flagKey);
        }
    }
}
