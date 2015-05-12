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

    private final static transient Logger LOG = Logger.getLogger(CidsClassFactory.class);
    private final static CidsClassFactory factory = new CidsClassFactory();

    private CidsClassFactory() {
    }

    public final static CidsClassFactory getFactory() {
        return factory;
    }

    
//     ClassAttribute attrib = new ClassAttribute(id + "", classID, name, typeID, classes.getClass(classID).getPolicy()); // NOI18N
//                attrib.setValue(value);
//                classes.getClass(attrib.getClassID()).addAttribute(attrib);
    
    
    
    /**
     * Transforms a cids legacy meta class object into a cids rest API class
     * object
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
        LOG.info("converting legacy meta class '"+metaClass.getKey()+"' to JSON serializable cids REST type");
        
        // 1st process the class attributes (configuration) of the cids class
        // map properties of Sirius.server.localserver._class.Class/MetaClass AND
        // class attributes (table cs_class_atr) to configuration attributes!
        final Collection<ClassAttribute> metaClassAttributes = (Collection<ClassAttribute>) metaClass.getAttributes();
        final Map<String, ClassAttribute> caMap = new HashMap<String, ClassAttribute>();
        
        // enumerate class attributes
        for (final ClassAttribute metaClassAttribute : metaClassAttributes) {
            caMap.put(metaClassAttribute.getName(),
                    metaClassAttribute);
        }
        LOG.debug(caMap.size() + " class attributes found in legacy meta class '"+metaClass.getKey()+"'");

        // KEY Attributes ------------------------------------------------------
        // ATTRIBUTE_POLICY: The name is sufficient
        setClassConfig(cidsClass,
                ClassConfig.Key.ATTRIBUTE_POLICY,
                metaClass.getAttributePolicy().getName());
        // LEGACY binary Icon! 
        // TODO: To be repaced by fully qualified icon name (see CLASS_ICON)
        setClassConfig(cidsClass,
                ClassConfig.Key.LEGACY_CLASS_ICON,
                metaClass.getIcon());
//      setClassConfig(cidsClass,
//                ClassConfig.Key.CLASS_ICON,
//                metaClass.getIcon().getName());
        setClassConfig(cidsClass,
                ClassConfig.Key.NAME,
                metaClass.getName());
        // TODO: Check if LEGACY_PK_FIELD is still needed  by pure REST clients
        setClassConfig(cidsClass,
                ClassConfig.Key.LEGACY_PK_FIELD,
                metaClass.getPrimaryKey());
        // POLICY: The name is sufficient!
        setClassConfig(cidsClass,
                ClassConfig.Key.POLICY,
                metaClass.getPolicy().getName());
        // LEGACY binary Icon! 
        // TODO: To be repaced by fully qualified icon name (see OBJECT_ICON)
        setClassConfig(cidsClass,
                ClassConfig.Key.LEGACY_OBJECT_ICON,
                metaClass.getObjectIcon());
        //      setClassConfig(cidsClass,
//                ClassConfig.Key.OBJECT_ICON,
//                metaClass.getObjectIcon().getName());
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.FEATURE_BG,
                caMap.remove(ClassConfig.Key.FEATURE_BG.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.FEATURE_FG,
                caMap.remove(ClassConfig.Key.FEATURE_FG.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.FEATURE_POINT_SYMBOL,
                caMap.remove(ClassConfig.Key.FEATURE_POINT_SYMBOL.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_X,
                caMap.remove(ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_X.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_Y,
                caMap.remove(ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_Y.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.QUERYABLE,
                caMap.remove(ClassConfig.Key.QUERYABLE.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.SORTING_COLUMN,
                caMap.remove(ClassConfig.Key.SORTING_COLUMN.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN,
                caMap.remove(ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN_ATTRIBUTE,
                caMap.remove(ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN_ATTRIBUTE.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.HISTORY_ENABLED,
                caMap.remove(ClassConfig.Key.HISTORY_ENABLED.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.HISTORY_OPTION_ANONYMOUS,
                caMap.remove(ClassConfig.Key.HISTORY_OPTION_ANONYMOUS.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.Key.TO_STRING_CACHE_ENABLED,
                caMap.remove(ClassConfig.Key.TO_STRING_CACHE_ENABLED.getKey()));

        // XPKEY
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.XPKey.AGGREGATION_RENDERER_XP,
                caMap.remove(ClassConfig.XPKey.AGGREGATION_RENDERER_XP.getKey()));
        // well known attribute, string type.
        setClassConfig(cidsClass,
                ClassConfig.XPKey.EDITOR_XP,
                metaClass.getEditor());
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.XPKey.FEATURE_RENDERER_XP,
                caMap.remove(ClassConfig.XPKey.FEATURE_RENDERER_XP.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass, ClassConfig.XPKey.FROM_STRING_XP, 
                caMap.remove(ClassConfig.XPKey.FROM_STRING_XP.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.XPKey.ICON_FACTORY_XP,
                caMap.remove(ClassConfig.XPKey.ICON_FACTORY_XP.getKey()));
        // well known attribute, string type.
        setClassConfig(cidsClass,
                ClassConfig.XPKey.RENDERER_XP,
                metaClass.getRenderer());
        // well attribute, string type.
        setClassConfig(cidsClass,
                ClassConfig.XPKey.TO_STRING_XP,
                metaClass.getToString());
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE,
                caMap.remove(ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_LAYER,
                caMap.remove(ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_LAYER.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_NAME,
                caMap.remove(ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_NAME.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL,
                caMap.remove(ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL.getKey()));
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(cidsClass,
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP,
                caMap.remove(ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP.getKey()));

        // FLAGKEY - binary attributes
        setClassFlag(cidsClass,
                ClassConfig.FlagKey.ARRAY_LINK,
                metaClass.isArrayElementLink());
        setClassFlag(cidsClass, ClassConfig.FlagKey.HIDE_FEATURE, 
                caMap.remove(ClassConfig.FlagKey.HIDE_FEATURE.getKey()) != null);
        setClassFlag(cidsClass,
                ClassConfig.FlagKey.INDEXED,
                metaClass.isIndexed());
        setClassFlag(cidsClass, ClassConfig.FlagKey.REASONABLE_FEW, 
                caMap.remove(ClassConfig.FlagKey.REASONABLE_FEW.getKey()) != null);

        // set other "unkown" class attributes
        for (final String caName : caMap.keySet()) {
            final ClassAttribute otherClassAttribute = caMap.get(caName);
            setClassConfigFromClassAttribute(cidsClass, caName, otherClassAttribute);
        }

        //2nd process the instance attributes of the cids class
        for (final MemberAttributeInfo mai : 
                (Collection<MemberAttributeInfo>) metaClass.getMemberAttributeInfos().values()) {
            try {
                // process legacy cids attributes from meberattributeinfo
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
                LOG.error("could not convert meta object attribute '"
                        +mai.getName()+"' to cids REST attribute: " + ex.getMessage(), ex);
            }
        }
        
        return cidsClass;
    }

    /**
     * Helper method for setting a well known configuration attribute of a cids class.
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
     * Helper method for setting a configuration attribute from a cids class  attribute.
     * Instead of storing the the complete attribute information, only the value
     * of the attribute is stored. 
     * 
     * Note: When reconstructing  the class attribute information from the deserialized JSON
     * Object, default values have to be set!
     *
     * @param cidsClass DOCUMENT ME!
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    private void setClassConfigFromClassAttribute(final CidsClass cidsClass, final CidsClassConfigurationKey key, final ClassAttribute classAttribute) {
        if ((cidsClass != null) && (key != null) && (classAttribute != null) && (classAttribute.getValue() != null)) {
            final Object value = classAttribute.getValue();
            if(LOG.isDebugEnabled() && !(value instanceof String)) {
                LOG.warn("setting non-string config attribute '"+key+"'");
            }
            cidsClass.setConfigAttribute(key, value);
        }
    }
    
    private void setClassConfigFromClassAttribute(final CidsClass cidsClass, final String key, final ClassAttribute classAttribute) {
        if ((cidsClass != null) && (key != null) && (classAttribute != null) && (classAttribute.getValue() != null)) {
            final Object value = classAttribute.getValue();
            if(LOG.isDebugEnabled() && !(value instanceof String)) {
                LOG.warn("setting non-string config attribute '"+key+"'");
            }
            cidsClass.setOtherConfigAttribute(key, value);
        }
    }

    /**
     * Helper method for setting a well known binary configuration attribute (flag) of a cids class.
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
     * Helper method for setting a well known configuration attribute of a cids attribute
     *
     * @param cidsAttribute DOCUMENT ME!
     * @param key DOCUMENT ME!
     * @param value DOCUMENT ME!
     */
    private void setAttributeConfig(final CidsAttribute cidsAttribute, final CidsAttributeConfigurationKey key,
            final Object value) {
        if ((cidsAttribute != null) && (key != null) && (value != null)) {
            if(LOG.isDebugEnabled() && !(value instanceof String)) {
                LOG.warn("setting non-string attribute '"+key+"'");
            }
            
            if(key == AttributeConfig.Key.DEFAULT_VALUE) {
                 LOG.warn("converting attribute default value to string");
                 cidsAttribute.setConfigAttribute(key, value.toString());
            } else {
                cidsAttribute.setConfigAttribute(key, value);
            }
        }
    }

    /**
      * Helper method for setting a well known binary configuration attribute (flag) of a cids attribute.
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
