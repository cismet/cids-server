/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.api.types.legacy;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.newuser.permission.Permission;
import Sirius.server.newuser.permission.Policy;

import Sirius.util.image.Image;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.cismet.cids.server.api.types.CidsAttribute;
import de.cismet.cids.server.api.types.CidsClass;
import de.cismet.cids.server.api.types.configkeys.AttributeConfig;
import de.cismet.cids.server.api.types.configkeys.CidsAttributeConfigurationFlagKey;
import de.cismet.cids.server.api.types.configkeys.CidsAttributeConfigurationKey;
import de.cismet.cids.server.api.types.configkeys.CidsClassConfigurationFlagKey;
import de.cismet.cids.server.api.types.configkeys.CidsClassConfigurationKey;
import de.cismet.cids.server.api.types.configkeys.ClassConfig;

/**
 * A factory class for converting between legacy cids types and REST/JSON types. TODO: Integrate into <strong>
 * cids-server-rest-types project</strong>!
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class CidsClassFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsClassFactory.class);
    private static final CidsClassFactory factory = new CidsClassFactory();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsClassFactory object.
     */
    private CidsClassFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final CidsClassFactory getFactory() {
        return factory;
    }

    /**
     * Transforms a cids rest API class into a cids legacy meta class object (Sirius) object.
     *
     * @param   cidsClass  the cids class to be converted
     *
     * @return  the reconstructed metaClass
     *
     * @throws  Exception             java.lang.Exception if any error occurs
     * @throws  NoSuchFieldException  DOCUMENT ME!
     * @throws  ClassCastException    DOCUMENT ME!
     */
    public MetaClass legacyCidsClassFromRestCidsClass(final CidsClass cidsClass) throws Exception {
        // create a copy of the cids class' configuration attributes
        final HashMap<String, Object> configurationAttributes = new HashMap<String, Object>();
        configurationAttributes.putAll(cidsClass.getConfigurationAttributes());

        String configurationKey;
        Object configurationAttribute;

        // MetaClass.id --------------------------------------------------------
        final int id;
        configurationKey = ClassConfig.Key.LEGACY_ID.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        id = this.getIntValue("MetaClass.id", configurationKey, configurationAttribute);
        if (id == -1) {
            // throw new NoSuchFieldException(message);
            // FIXME: adjust cids rest legacy cids bean serialization and include LEGACY_ID!
            LOG.warn("NoSuchFieldException Exception for MetaClass.id '" + configurationKey
                        + "' supressed to ensure compatibility with cids rest legacy cids bean serialization.");
        }

        // MetaClass.tableName ------------------------------------------------------
        // the table name of the meta class is part of the class key of the cids class (tablename@domain) .
        final String tableName = cidsClass.getName();
        if ((tableName == null) || tableName.isEmpty()) {
            final String message =
                "cannot set MetaClass.tableName to CidsClass.name: property is not available or null! Name is a required attribute and part of the class key (name@domain) of the cids class";
            LOG.error(message);
            throw new NoSuchFieldException(message);
        }

        // MetaClass.name ------------------------------------------------------
        configurationKey = ClassConfig.Key.NAME.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        final String name;
        if (configurationAttribute != null) {
            name = configurationAttribute.toString();
        } else {
            final String message = "cannot set MetaClass.name to configuration attribute '"
                        + configurationKey
                        + "': configuration attribute is not available or null. Setting to MetaClass.name to MetaClass.tableName: '"
                        + tableName + "'.";
            LOG.warn(message);
            name = tableName;
        }

        // MetaClass.description -----------------------------------------------
        final String description;
        configurationKey = ClassConfig.Key.DESCRIPTION.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            description = configurationAttribute.toString();
        } else {
            final String message = "cannot set MetaClass.description to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            // LOG.debug(message);
            description = null;
        }

        // MetaClass.icon ------------------------------------------------------
        // FIXME: build icon object from CLASS_ICON (string) instead from
        // LEGACY_CLASS_ICON (byte[]).
        final Image icon;
        configurationKey = ClassConfig.Key.LEGACY_CLASS_ICON.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            if (Sirius.util.image.Image.class.isAssignableFrom(configurationAttribute.getClass())) {
                icon = (Sirius.util.image.Image) configurationAttribute;
            } else {
                final String message = "cannot restore MetaClass.icon from configuration attribute '"
                            + configurationKey + "' = '" + configurationAttribute
                            + "': unexpected configuration attribute class '"
                            + configurationAttribute.getClass() + "'";
                LOG.error(message);
                throw new ClassCastException(message);
            }
        } else {
            final String message = "cannot set MetaClass.icon to to configuration attribute '"
                        + configurationKey + "': property is not available or null";
            LOG.error(message);
            // FIXME: adjust cids rest legacy cids bean serialization and include LEGACY_CLASS_ICON!
            // throw new NoSuchFieldException(message);
            LOG.warn("NoSuchFieldException Exception for MetaClass.icon '" + configurationKey
                        + "' supressed to ensure compatibility with cids rest legacy cids bean serialization.");
            icon = null;
        }

        // MetaClass.objectIcon -------------------------------------------------
        // FIXME: build icon object from CLASS_ICON (string) instead from
        // LEGACY_CLASS_ICON (byte[]).
        final Image objectIcon;
        configurationKey = ClassConfig.Key.LEGACY_OBJECT_ICON.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            if (Sirius.util.image.Image.class.isAssignableFrom(configurationAttribute.getClass())) {
                objectIcon = (Sirius.util.image.Image) configurationAttribute;
            } else {
                final String message = "cannot restore MetaClass.objectIcon from configuration attribute '"
                            + configurationKey + "' = '" + configurationAttribute
                            + "': unexpected configuration attribute class '"
                            + configurationAttribute.getClass() + "'";
                LOG.error(message);
                throw new ClassCastException(message);
            }
        } else {
            final String message = "cannot set MetaClass.objectIcon to to configuration attribute '"
                        + configurationKey + "': property is not available or null";
            LOG.error(message);
            // FIXME: adjust cids rest legacy cids bean serialization and include LEGACY_OBJECT_ICON!
            // throw new NoSuchFieldException(message);
            LOG.warn("NoSuchFieldException Exception for MetaClass.objectIcon '" + configurationKey
                        + "' supressed to ensure compatibility with cids rest legacy cids bean serialization.");
            objectIcon = null;
        }

        // MetaClass.primaryKey ------------------------------------------------
        final String primaryKey;
        configurationKey = ClassConfig.Key.LEGACY_PK_FIELD.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            primaryKey = configurationAttribute.toString();
        } else {
            final String message = "cannot set MetaClass.primaryKey to configuration attribute '"
                        + configurationKey
                        + "': configuration attribute is not available or null. Setting to default: 'id'.";
            LOG.warn(message);
            primaryKey = "id";
        }

        // MetaClass.toString -------------------------------------------------
        final String toString;
        configurationKey = ClassConfig.XPKey.TO_STRING_XP.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            toString = configurationAttribute.toString();
        } else {
            final String message = "cannot set MetaClass.toString to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            toString = null;
        }

        // MetaClass.Policy ----------------------------------------------------
        final Policy policy;
        configurationKey = ClassConfig.Key.POLICY.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            final String policyName = configurationAttribute.toString().toUpperCase();
            policy = this.createPolicy(policyName);
        } else {
            final String message = "cannot set MetaClass.policy to configuration attribute '"
                        + configurationKey
                        + "': configuration attribute is not available or null. Setting to default: 'SECURE'";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            policy = this.createPolicy("SECURE");
        }

        // MetaClass.attributePolicy -------------------------------------------
        final Policy attributePolicy;
        configurationKey = ClassConfig.Key.ATTRIBUTE_POLICY.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            final String policyName = configurationAttribute.toString().toUpperCase();
            attributePolicy = this.createPolicy(policyName);
        } else {
            final String message = "cannot set MetaClass.attributePolicy to configuration attribute '"
                        + configurationKey
                        + "': configuration attribute is not available or null. Setting to default: 'SECURE'";
            LOG.warn(message);
            attributePolicy = this.createPolicy("SECURE");
        }

        // MetaClass.indexed ---------------------------------------------------
        final boolean indexed;
        configurationKey = ClassConfig.FlagKey.INDEXED.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        indexed = this.getBooleanValue("indexed", configurationKey, configurationAttribute);

        // MetaClass.domain ----------------------------------------------------
        final String domain = cidsClass.getDomain();
        if ((domain == null) || domain.isEmpty()) {
            final String message = "cannot set MetaClass.domain to CidsClass.domain: property is not available or null";
            LOG.error(message);
            throw new NoSuchFieldException(message);
        }

        // MetaClass.editor ----------------------------------------------------
        final String editor;
        configurationKey = ClassConfig.XPKey.EDITOR_XP.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            editor = configurationAttribute.toString();
        } else {
            final String message = "cannot set MetaClass.editor to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            editor = null;
        }

        // MetaClass.renderer --------------------------------------------------
        final String renderer;
        configurationKey = ClassConfig.XPKey.EDITOR_XP.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            renderer = configurationAttribute.toString();
        } else {
            final String message = "cannot set MetaClass.renderer to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            renderer = null;
        }

        // MetaClass.arrayLink -------------------------------------------------
        final boolean arrayLink;
        configurationKey = ClassConfig.FlagKey.ARRAY_LINK.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        arrayLink = this.getBooleanValue("arrayLink", configurationKey, configurationAttribute);

        // create the class with basic attributes
        final Sirius.server.localserver._class.Class siriusClass = new Sirius.server.localserver._class.Class(
                id,
                name,
                description,
                icon,
                objectIcon,
                tableName,
                primaryKey,
                toString,
                policy,
                attributePolicy,
                indexed);
        final MetaClass metaClass = new MetaClass(siriusClass, domain);
        metaClass.setEditor(editor);
        metaClass.setRenderer(renderer);
        metaClass.setArrayElementLink(arrayLink);

        // process configuration attributes and create the respective class attributes
        for (final String configurationKeyName : configurationAttributes.keySet()) {
            Object value = configurationAttributes.get(configurationKeyName);
            String classAttributeName = null;

            // ckeck ClassConfig.Key -------------------------------------------
            try {
                // JSON property names of configurationAttributes are names of the Key Enums
                // key.getName(), not keys of Key Enums key.getKey()!
                // - >Enums can  be reconstructed from property names!
                final ClassConfig.Key classConfigKey = ClassConfig.Key.valueOf(
                        ClassConfig.Key.class,
                        configurationKeyName);
                classAttributeName = classConfigKey.getKey();
            } catch (IllegalArgumentException ex) {
                final String message = "configuration attribute '"
                            + configurationKeyName + "' is not a known ClassConfig Key.";
                if (LOG.isDebugEnabled()) {
                    LOG.debug(message);
                }
            }

            // check ClassConfig.FeatureSupportingrasterServiceKey -------------
            if (classAttributeName == null) {
                try {
                    final ClassConfig.FeatureSupportingrasterServiceKey classConfigKey =
                        ClassConfig.FeatureSupportingrasterServiceKey.valueOf(
                            ClassConfig.FeatureSupportingrasterServiceKey.class,
                            configurationKeyName);
                    classAttributeName = classConfigKey.getKey();
                } catch (IllegalArgumentException ex) {
                    final String message = "configuration attribute '"
                                + configurationKeyName
                                + "' is not a known ClassConfig FeatureSupportingrasterServiceKey.";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message);
                    }
                }
            }

            // check ClassConfig.XPKey -----------------------------------------
            if (classAttributeName == null) {
                try {
                    final ClassConfig.XPKey classConfigKey = ClassConfig.XPKey.valueOf(
                            ClassConfig.XPKey.class,
                            configurationKeyName);
                    classAttributeName = classConfigKey.getKey();
                } catch (IllegalArgumentException ex) {
                    final String message = "configuration attribute '"
                                + configurationKeyName + "' is not a known ClassConfig XPKey.";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message);
                    }
                }
            }

            // check ClassConfig.FlagKey ---------------------------------------
            if (classAttributeName == null) {
                try {
                    final ClassConfig.FlagKey classConfigKey = ClassConfig.FlagKey.valueOf(
                            ClassConfig.FlagKey.class,
                            configurationKeyName);
                    classAttributeName = classConfigKey.getKey();
                    // try to convert to boolean
                    value = this.getBooleanValue("classAttribute." + classAttributeName,
                            classAttributeName, value);
                } catch (IllegalArgumentException ex) {
                    final String message = "configuration attribute '"
                                + configurationKeyName + "' is not a known ClassConfig FlagKey.";
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(message);
                    }
                }
            }

            // "unknown configuration attribute!"
            if (classAttributeName == null) {
                final String message = "configuration attribute '"
                            + configurationKeyName + "' is not a known ClassConfig attribute";
                classAttributeName = configurationKeyName;
                LOG.warn(message);
            }

            final ClassAttribute classAttribute = this.createClassAttribute(classAttributeName, value, metaClass);
            metaClass.getAttributes().add(classAttribute);
        }

        // process attributes and create the respective member attribute info
        for (final CidsAttribute cidsAttribute : cidsClass.getAttributes().values()) {
            final MemberAttributeInfo mai = this.createMemberAttributeInfo(metaClass, cidsAttribute);
            metaClass.addMemberAttributeInfo(mai);
        }

        return metaClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass      DOCUMENT ME!
     * @param   cidsAttribute  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private MemberAttributeInfo createMemberAttributeInfo(final MetaClass metaClass,
            final CidsAttribute cidsAttribute) {
        // create a copy of the cids class' configuration attributes
        final HashMap<String, Object> configurationAttributes = new HashMap<String, Object>();
        configurationAttributes.putAll(cidsAttribute.getConfigurationAttributes());

        String configurationKey;
        Object configurationAttribute;

        // MemberAttributeInfo.id ----------------------------------------------
        final int id;
        configurationKey = AttributeConfig.Key.LEGACY_ID.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        id = this.getIntValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".id",
                configurationKey,
                configurationAttribute);
        if (id == -1) {
            // final String message = "Missing or invalid integer Attribute
            // MemberAttributeInfo."+cidsAttribute.getAttributeKey()+".id in cids class '"+metaClass.getKey()+"'";
            // LOG.error(message); throw new NoSuchFieldException(message); FIXME: adjust cids rest legacy cids bean
            // serialization and inlcude LEGACY_ID!
            LOG.warn("NoSuchFieldException Exception for MemberAttributeInfo" + cidsAttribute.getAttributeKey()
                        + ".id (" + configurationKey
                        + ") supressed to ensure compatibility with cids rest legacy cids bean serialization.");
        }

        // MemberAttributeInfo.classId -----------------------------------------
        final int classId = metaClass.getId();

        // MemberAttributeInfo.typeId ------------------------------------------
        final int typeId;
        configurationKey = AttributeConfig.Key.LEGACY_REFERENCE_TYPE.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        typeId = this.getIntValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".typeId",
                configurationKey,
                configurationAttribute);
        if (typeId == -1) {
            final String message = "Missing or invalid integer Attribute MemberAttributeInfo."
                        + cidsAttribute.getAttributeKey() + ".typeId in cids class '" + metaClass.getKey()
                        + "', setting to default value: " + typeId;
            LOG.warn(message);
        }

        // MemberAttributeInfo.fieldName ---------------------------------------
        final String fieldName = cidsAttribute.getName();

        // MemberAttributeInfo.name --------------------------------------------
        configurationKey = AttributeConfig.Key.NAME.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        final String name;
        if (configurationAttribute != null) {
            name = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".name to configuration attribute '"
                        + configurationKey
                        + "': configuration attribute is not available or null. Setting to name to fieldName: '"
                        + fieldName + "'.";
            LOG.warn(message);
            name = fieldName;
        }

        // MemberAttributeInfo.foreignKey --------------------------------------
        final boolean foreignKey;
        configurationKey = AttributeConfig.FlagKey.FOREIGN_KEY.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        foreignKey = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".foreignKey",
                configurationKey,
                configurationAttribute);

        // MemberAttributeInfo.substitute --------------------------------------
        final boolean substitute;
        configurationKey = AttributeConfig.FlagKey.SUBSTITUTE.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        substitute = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".foreignKey",
                configurationKey,
                configurationAttribute);

        // MemberAttributeInfo.foreignKeyClassId -------------------------------
        final int foreignKeyClassId;
        configurationKey = AttributeConfig.Key.LEGACY_FOREIGN_KEY_CLASS_ID.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        foreignKeyClassId = this.getIntValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".foreignKeyClassId",
                configurationKey,
                configurationAttribute);
        if (foreignKeyClassId == -1) {
            final String message = "Missing or invalid integer Attribute MemberAttributeInfo."
                        + cidsAttribute.getAttributeKey() + ".foreignKeyClassId in cids class '" + metaClass.getKey()
                        + "', setting to default value: " + foreignKeyClassId;
            LOG.warn(message);
        }

        // MemberAttributeInfo.visible -----------------------------------------
        final boolean visible;
        configurationKey = AttributeConfig.FlagKey.VISIBLE.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        visible = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".visible",
                configurationKey,
                configurationAttribute);

        // MemberAttributeInfo.indexed -----------------------------------------
        final boolean indexed;
        configurationKey = AttributeConfig.FlagKey.INDEXED.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        indexed = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".indexed",
                configurationKey,
                configurationAttribute);

        // MemberAttributeInfo.isArray -----------------------------------------
        final boolean isArray;
        configurationKey = AttributeConfig.FlagKey.ARRAY.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        isArray = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".isArray",
                configurationKey,
                configurationAttribute);

        // MemberAttributeInfo.arrayKeyFieldName -------------------------------
        final String arrayKeyFieldName;
        configurationKey = AttributeConfig.Key.LEGACY_ARRAY_KEY_FIELD_NAME.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            arrayKeyFieldName = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".indexed to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            arrayKeyFieldName = null;
        }

        // MemberAttributeInfo.fromString --------------------------------------
        final String fromString;
        configurationKey = AttributeConfig.XPKey.FROM_STRING_XP.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            fromString = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".fromString to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            fromString = null;
        }

        // MemberAttributeInfo.toString ----------------------------------------
        final String toString;
        configurationKey = AttributeConfig.XPKey.TO_STRING_XP.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            toString = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".toString to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            toString = null;
        }

        // MemberAttributeInfo.position ----------------------------------------
        final int position;
        configurationKey = AttributeConfig.Key.POSITION.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        position = this.getIntValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".position",
                configurationKey,
                configurationAttribute);
        if (position == -1) {
            final String message = "Missing or invalid integer Attribute MemberAttributeInfo."
                        + cidsAttribute.getAttributeKey() + ".position in cids class '" + metaClass.getKey()
                        + "', setting to default value: " + foreignKeyClassId;
            LOG.warn(message);
        }

        // MemberAttributeInfo.render ------------------------------------------
        final String render;
        configurationKey = AttributeConfig.XPKey.RENDERER_XP.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            render = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".render to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            render = null;
        }

        // MemberAttributeInfo.editor ------------------------------------------
        final String editor;
        configurationKey = AttributeConfig.XPKey.EDITOR_XP.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            editor = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".editor to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            editor = null;
        }

        // MemberAttributeInfo.Javaclassname ------------------------------------------
        final String javaclassname;
        configurationKey = AttributeConfig.Key.LEGACY_JAVACLASS_NAME.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            javaclassname = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".javaclassname to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            javaclassname = null;
        }

        // MemberAttributeInfo.defaultValue ------------------------------------
        final String defaultValue;
        configurationKey = AttributeConfig.Key.DEFAULT_VALUE.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        if (configurationAttribute != null) {
            defaultValue = configurationAttribute.toString();
        } else {
            final String message = "cannot set MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".defaultValue to configuration attribute '"
                        + configurationKey + "': configuration attribute is not available or null";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
            defaultValue = null;
        }

        // MemberAttributeInfo.optional ----------------------------------------
        final boolean optional;
        configurationKey = AttributeConfig.FlagKey.OPTIONAL.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        optional = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".optional",
                configurationKey,
                configurationAttribute);

        // MemberAttributeInfo.virtual -----------------------------------------
        final boolean virtual;
        configurationKey = AttributeConfig.FlagKey.VIRTUAL.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        virtual = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey() + ".virtual",
                configurationKey,
                configurationAttribute);

        // MemberAttributeInfo.virtual -----------------------------------------
        final boolean extensionAttribute;
        configurationKey = AttributeConfig.FlagKey.EXTENSION_ATTRIBUTE.name();
        configurationAttribute = configurationAttributes.remove(configurationKey);
        extensionAttribute = this.getBooleanValue("MemberAttributeInfo." + cidsAttribute.getAttributeKey()
                        + ".extensionAttribute",
                configurationKey,
                configurationAttribute);

        if (!configurationAttributes.isEmpty()) {
            LOG.warn(configurationAttributes.size()
                        + " unsupported configuration attributes in attribute '"
                        + cidsAttribute.getAttributeKey() + "'!");
            for (final String configurationKeyKey : configurationAttributes.keySet()) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("unsupported configuration attribute '" + configurationKeyKey
                                + "' = ' " + configurationAttributes.get(configurationKeyKey)
                                + "' in attribute '" + cidsAttribute.getAttributeKey() + " '!");
                }
            }
        }

        final MemberAttributeInfo mai = new MemberAttributeInfo(
                id,
                classId,
                typeId,
                name,
                fieldName,
                foreignKey,
                substitute,
                foreignKeyClassId,
                visible,
                indexed,
                isArray,
                arrayKeyFieldName,
                fromString,
                toString,
                position);

        mai.setRenderer(render);
        mai.setEditor(editor);
        mai.setJavaclassname(javaclassname);
        mai.setDefaultValue(defaultValue);
        mai.setOptional(optional);
        mai.setVirtual(virtual);
        mai.setExtensionAttribute(extensionAttribute);

        return mai;
    }

    /**
     * Create a new legacy Class Attribute from a Configuration Attribute. TODO: check if typeID is really required
     *
     * @param   attributeName  DOCUMENT ME!
     * @param   value          DOCUMENT ME!
     * @param   metaClass      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private ClassAttribute createClassAttribute(final String attributeName,
            final Object value,
            final MetaClass metaClass) {
        // ClassAttribute.id ----------------------------------------------------
        final String id = attributeName;
        // ClassAttribute.classID -----------------------------------------------
        final int classID = metaClass.getID();
        // ClassAttribute.name --------------------------------------------------
        final String name = attributeName;
        // ClassAttribute.typeID ------------------------------------------------
        // set to default (7 = char)
        final int typeID = 7;
        // ClassAttribute.policy ------------------------------------------------
        final Policy policy = metaClass.getPolicy();
        // ClassAttribute.value -------------------------------------------------
        final ClassAttribute classAttribute = new ClassAttribute(id, classID, name, typeID, policy);
        classAttribute.setValue(value);
        if (value != null) {
            if (!String.class.isAssignableFrom(value.getClass())) {
                final String message = "value of class attribute '"
                            + attributeName
                            + "' (created from existing configuration attribute) is not of type String but '"
                            + value.getClass() + "'";
                LOG.warn(message);
            }
        } else {
            final String message = "value of class attribute '"
                        + attributeName + "' (created from existing configuration attribute) is null!";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
        }

        return classAttribute;
    }

    /**
     * Get a boolean value from a configuration attribute. Return false by default
     *
     * @param   attributeName           DOCUMENT ME!
     * @param   configurationKey        DOCUMENT ME!
     * @param   configurationAttribute  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean getBooleanValue(final String attributeName,
            final String configurationKey,
            final Object configurationAttribute) {
        boolean booleanValue = false;
        if (configurationAttribute != null) {
            if (Boolean.class.isAssignableFrom(configurationAttribute.getClass())) {
                booleanValue = ((Boolean) configurationAttribute);
            } else {
                LOG.warn("cannot restore boolean MetaClass." + attributeName + " from configuration attribute '"
                            + configurationKey + "': unexpected configuration attribute class '"
                            + configurationAttribute.getClass() + "', forcing string-deserialization");
                try {
                    booleanValue = Boolean.valueOf(configurationAttribute.toString());
                } catch (Exception ex) {
                    final String message = "cannot restore boolean MetaClass." + attributeName
                                + " from configuration attribute '"
                                + configurationKey + "' = '" + configurationAttribute
                                + "': '" + ex.getMessage() + "' -> Setting to default: false";
                    LOG.warn(message, ex);
                }
            }
        } else {
            final String message = "cannot set boolean MetaClass." + attributeName + " to configuration attribute '"
                        + configurationKey
                        + "': configuration attribute is not available or null. -> Setting to default: false";
            if (LOG.isDebugEnabled()) {
                LOG.debug(message);
            }
        }

        return booleanValue;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attributeName           DOCUMENT ME!
     * @param   configurationKey        DOCUMENT ME!
     * @param   configurationAttribute  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int getIntValue(final String attributeName,
            final String configurationKey,
            final Object configurationAttribute) {
        int intValue = -1;

        if (configurationAttribute != null) {
            if (Integer.class.isAssignableFrom(configurationAttribute.getClass())) {
                intValue = ((Integer) configurationAttribute);
            } else {
                LOG.warn("cannot restore '" + attributeName + "' from configuration attribute '"
                            + configurationKey + "': unexpected configuration attribute class '"
                            + configurationAttribute.getClass() + "', forcing string-deserialization");
                try {
                    intValue = Integer.parseInt(configurationAttribute.toString());
                } catch (Exception ex) {
                    final String message = "cannot restore '" + attributeName + "' from configuration attribute '"
                                + configurationKey + "' = '" + configurationAttribute
                                + "' : '" + ex.getMessage() + "', returning default value: -1";
                    LOG.warn(message, ex);
                    // throw new NumberFormatException(message);
                }
            }
        } else {
            final String message = "cannot set '" + attributeName + "' to configuration attribute '"
                        + configurationKey
                        + "': configuration attribute is not available or null. Returning default: -1";
            LOG.warn(message);
        }

        return intValue;
    }

    /**
     * Helper method for creating Policies.
     *
     * @param   policyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected Policy createPolicy(String policyName) {
        if (policyName == null || policyName.isEmpty() || 
                (!policyName.equalsIgnoreCase("DEFAULT")
                    && !policyName.equalsIgnoreCase("STANDARD")
                    && !policyName.equalsIgnoreCase("SECURE")
                    && !policyName.equalsIgnoreCase("WIKI"))) {
            LOG.warn("policy '" + policyName + "' is currently not supported, setting to default 'SECURE'");
            policyName = "SECURE";
        }

        final Permission readPermission = new Permission(0, "read");
        final Permission writePermission = new Permission(1, "write");
        final Map<Permission, Boolean> policyMap = new HashMap<Permission, Boolean>();

        if (policyName.equalsIgnoreCase("STANDARD")
                    || policyName.equalsIgnoreCase("DEFAULT")) {
            policyMap.put(readPermission, true);
            policyMap.put(writePermission, false);
            return new Policy(policyMap, 0, "STANDARD");
        } else if (policyName.equalsIgnoreCase("WIKI")) {
            policyMap.put(readPermission, true);
            policyMap.put(writePermission, true);
            return new Policy(policyMap, 1, "WIKI");
        } else {
            policyMap.put(readPermission, false);
            policyMap.put(writePermission, false);
            return new Policy(policyMap, 2, "SECURE");
        }

// not support in 1.6
//        switch (policyName) {
//            case "STANDARD": {
//                policyMap.put(readPermission, false);
//                policyMap.put(writePermission, false);
//                return new Policy(policyMap, 0, policyName);
//            }
//            case "WIKI": {
//                policyMap.put(readPermission, true);
//                policyMap.put(writePermission, true);
//                return new Policy(policyMap, 1, policyName);
//            }
//            case "SECURE":
//            default: {
//                policyMap.put(readPermission, false);
//                policyMap.put(writePermission, false);
//                return new Policy(policyMap, 2, policyName);
//            }
//        }
    }

    /**
     * Transforms a cids legacy meta class object (Sirius) into a cids rest API class object. <strong>Code copied from
     * de.cismet.cids.server.backend.legacy.LegacyCoreBackend.createCidsClass() (cids-server-rest-legacy
     * project)</strong>
     *
     * @param   metaClass  the sirius meta class to be converted
     *
     * @return  the converted cids rest API class object
     */
    public CidsClass restCidsClassFromLegacyCidsClass(final MetaClass metaClass) {
        final CidsClass cidsClass = new CidsClass((String)metaClass.getTableName(),
                metaClass.getDomain());
        LOG.info("converting legacy meta class '" + metaClass.getKey() + "' to JSON serializable cids REST type");

        // 1st process the class attributes (configuration) of the cids class
        // map properties of Sirius.server.localserver._class.Class/MetaClass AND
        // class attributes (table cs_class_atr) to configuration attributes!
        final Collection<ClassAttribute> metaClassAttributes = (Collection<ClassAttribute>)metaClass.getAttributes();
        final Map<String, ClassAttribute> classAttributesMap = new HashMap<String, ClassAttribute>();

        // enumerate class attributes
        for (final ClassAttribute metaClassAttribute : metaClassAttributes) {
            classAttributesMap.put(metaClassAttribute.getName(),
                metaClassAttribute);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(classAttributesMap.size() + " class attributes found in legacy meta class '" + metaClass.getKey()
                        + "'");
        }

        // KEY Attributes ======================================================
        // LEGACY CLASS ID -------------------------------------------------< OK
        // required to reconstruct the legacy SIRIUS MetaClass from REST cidsClass
        setClassConfig(cidsClass,
            ClassConfig.Key.LEGACY_ID,
            metaClass.getID());
        // ATTRIBUTE_POLICY ------------------------------------------------< OK
        // The name is sufficient
        setClassConfig(cidsClass,
            ClassConfig.Key.ATTRIBUTE_POLICY,
            metaClass.getAttributePolicy().getName());
        // LEGACY_CLASS_ICON -----------------------------------------------< OK
        // required to reconstruct the legacy class icon (binary data!) from REST cidsClass
        // TODO: To be repaced by fully qualified icon name (see CLASS_ICON)
        setClassConfig(cidsClass,
            ClassConfig.Key.LEGACY_CLASS_ICON,
            metaClass.getIcon());
//      setClassConfig(cidsClass,
//                ClassConfig.Key.CLASS_ICON,
//                metaClass.getIcon().getName());
        // NAME -------------------------------------------------------------< OK
        setClassConfig(cidsClass,
            ClassConfig.Key.NAME,
            metaClass.getName());
        // DESCRIPTION -----------------------------------------------------< OK
        setClassConfig(cidsClass,
            ClassConfig.Key.DESCRIPTION,
            metaClass.getDescription());
        // LEGACY_PK_FIELD -------------------------------------------------< OK
        // TODO: Check if LEGACY_PK_FIELD is still needed by pure REST clients
        setClassConfig(cidsClass,
            ClassConfig.Key.LEGACY_PK_FIELD,
            metaClass.getPrimaryKey());
        // POLICY ----------------------------------------------------------< OK
        // The name is sufficient!
        setClassConfig(cidsClass,
            ClassConfig.Key.POLICY,
            metaClass.getPolicy().getName());
        // LEGACY_OBJECT_ICON ----------------------------------------------< OK
        // required to reconstruct the legacy object icon (binary data!) from REST cidsClass
        // TODO: To be repaced by fully qualified icon name (see OBJECT_ICON)
        setClassConfig(cidsClass,
            ClassConfig.Key.LEGACY_OBJECT_ICON,
            metaClass.getObjectIcon());
        // setClassConfig(cidsClass,
// ClassConfig.Key.OBJECT_ICON,
// metaClass.getObjectIcon().getName());
        // well known class attribute. assuming string type.
        // FEATURE_BG ------------------------------------------------------< OK
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.FEATURE_BG,
            classAttributesMap.remove(ClassConfig.Key.FEATURE_BG.getKey()));
        // FEATURE_FG ------------------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.FEATURE_FG,
            classAttributesMap.remove(ClassConfig.Key.FEATURE_FG.getKey()));
        // FEATURE_POINT_SYMBOL --------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.FEATURE_POINT_SYMBOL,
            classAttributesMap.remove(ClassConfig.Key.FEATURE_POINT_SYMBOL.getKey()));
        // FEATURE_POINT_SYMBOL_SWEETSPOT_X --------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_X,
            classAttributesMap.remove(ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_X.getKey()));
        // FEATURE_POINT_SYMBOL_SWEETSPOT_Y --------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_Y,
            classAttributesMap.remove(ClassConfig.Key.FEATURE_POINT_SYMBOL_SWEETSPOT_Y.getKey()));
        // QUERYABLE -------------------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.QUERYABLE,
            classAttributesMap.remove(ClassConfig.Key.QUERYABLE.getKey()));
        // SORTING_COLUMN --------------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.SORTING_COLUMN,
            classAttributesMap.remove(ClassConfig.Key.SORTING_COLUMN.getKey()));
        // SEARCH_HIT_DYNAMIC_CHILDREN -------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN,
            classAttributesMap.remove(ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN.getKey()));
        // SEARCH_HIT_DYNAMIC_CHILDREN_ATTRIBUTE ---------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN_ATTRIBUTE,
            classAttributesMap.remove(ClassConfig.Key.SEARCH_HIT_DYNAMIC_CHILDREN_ATTRIBUTE.getKey()));
        // HISTORY_ENABLED --------------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.HISTORY_ENABLED,
            classAttributesMap.remove(ClassConfig.Key.HISTORY_ENABLED.getKey()));
        // HISTORY_OPTION_ANONYMOUS ----------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.HISTORY_OPTION_ANONYMOUS,
            classAttributesMap.remove(ClassConfig.Key.HISTORY_OPTION_ANONYMOUS.getKey()));
        // TO_STRING_CACHE_ENABLED -----------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.Key.TO_STRING_CACHE_ENABLED,
            classAttributesMap.remove(ClassConfig.Key.TO_STRING_CACHE_ENABLED.getKey()));

        // XPKEYs ==============================================================
        // well known class attribute. assuming string type.
        // AGGREGATION_RENDERER_XP -----------------------------------------< OK
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.XPKey.AGGREGATION_RENDERER_XP,
            classAttributesMap.remove(ClassConfig.XPKey.AGGREGATION_RENDERER_XP.getKey()));
        // well known attribute, string type.
        // EDITOR_XP -------------------------------------------------------< OK
        setClassConfig(cidsClass,
            ClassConfig.XPKey.EDITOR_XP,
            metaClass.getEditor());
        // FEATURE_RENDERER_XP ---------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.XPKey.FEATURE_RENDERER_XP,
            classAttributesMap.remove(ClassConfig.XPKey.FEATURE_RENDERER_XP.getKey()));
        // FROM_STRING_XP --------------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.XPKey.FROM_STRING_XP,
            classAttributesMap.remove(ClassConfig.XPKey.FROM_STRING_XP.getKey()));
        // ICON_FACTORY_XP -------------------------------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.XPKey.ICON_FACTORY_XP,
            classAttributesMap.remove(ClassConfig.XPKey.ICON_FACTORY_XP.getKey()));
        // RENDERER_XP -----------------------------------------------------< OK
        // well known attribute, string type.
        setClassConfig(cidsClass,
            ClassConfig.XPKey.RENDERER_XP,
            metaClass.getRenderer());
        // TO_STRING_XP ----------------------------------------------------< OK
        // well attribute, string type.
        setClassConfig(cidsClass,
            ClassConfig.XPKey.TO_STRING_XP,
            metaClass.getToString());
        // FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE ------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE,
            classAttributesMap.remove(
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE.getKey()));
        // FEATURE_SUPPORTING_RASTER_SERVICE_LAYER -------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_LAYER,
            classAttributesMap.remove(
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_LAYER.getKey()));
        // FEATURE_SUPPORTING_RASTER_SERVICE_NAME --------------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_NAME,
            classAttributesMap.remove(
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_NAME.getKey()));
        // FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL --------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL,
            classAttributesMap.remove(
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL.getKey()));
        // FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP --------------------< OK
        // well known class attribute. assuming string type.
        setClassConfigFromClassAttribute(
            cidsClass,
            ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP,
            classAttributesMap.remove(
                ClassConfig.FeatureSupportingrasterServiceKey.FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP.getKey()));

        // FLAGKEY - boolean attributes ========================================
        // ARRAY_LINK -----------------------------------------------------< OK
        setClassFlag(cidsClass,
            ClassConfig.FlagKey.ARRAY_LINK,
            metaClass.isArrayElementLink());
        // HIDE_FEATURE --------------------------------------------------< OK
        setClassFlag(
            cidsClass,
            ClassConfig.FlagKey.HIDE_FEATURE,
            classAttributesMap.remove(ClassConfig.FlagKey.HIDE_FEATURE.getKey())
                    != null);
        // INDEXED  --------------------------------------------------------< OK
        setClassFlag(cidsClass,
            ClassConfig.FlagKey.INDEXED,
            metaClass.isIndexed());
        // REASONABLE_FEW -------------------------------------------------< OK
        setClassFlag(
            cidsClass,
            ClassConfig.FlagKey.REASONABLE_FEW,
            classAttributesMap.remove(ClassConfig.FlagKey.REASONABLE_FEW.getKey())
                    != null);

        // set other "unkown" class attributes =================================
        for (final String caName : classAttributesMap.keySet()) {
            final ClassAttribute otherClassAttribute = classAttributesMap.get(caName);
            setClassConfigFromClassAttribute(cidsClass, caName, otherClassAttribute);
        }

        // 2nd process the instance attributes of the cids class ////////////////
        for (final MemberAttributeInfo mai
                    : (Collection<MemberAttributeInfo>)metaClass.getMemberAttributeInfos().values()) {
            try {
                // process legacy cids attributes from meberattributeinfo
                final CidsAttribute cidsAttribute = new CidsAttribute((String)mai.getFieldName(),
                        (String)metaClass.getTableName());
                // KEY /////////////////////////////////////////////////////////
                // LEGACY_ID -----------------------------------------------< OK
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.Key.LEGACY_ID,
                    mai.getId());
                // NAME ----------------------------------------------------< OK
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.Key.NAME,
                    mai.getName());
                // DEFAULT_VALUE -------------------------------------------<
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.Key.DEFAULT_VALUE,
                    mai.getDefaultValue());
                setAttributeConfig(
                    cidsAttribute,
                    // LEGACY_ARRAY_KEY_FIELD_NAME ------------------------------------< OK
                    AttributeConfig.Key.LEGACY_ARRAY_KEY_FIELD_NAME,
                    mai.getArrayKeyFieldName());
                // LEGACY_JAVACLASS_NAME ------------------------------------------<
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.Key.LEGACY_JAVACLASS_NAME,
                    mai.getJavaclassname());
                // POSITION ------------------------------------------------<
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.Key.POSITION,
                    mai.getPosition());
                // LEGACY_REFERENCE_TYPE ------------------------------------------< OK
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.Key.LEGACY_REFERENCE_TYPE,
                    mai.getTypeId());
                // LEGACY_FOREIGN_KEY_CLASS_ID -----------------------------< OK
                setAttributeConfig(
                    cidsAttribute,
                    AttributeConfig.Key.LEGACY_FOREIGN_KEY_CLASS_ID,
                    mai.getForeignKeyClassId());

                // XPKey ///////////////////////////////////////////////////////
                // COMPLEX_EDITOR_XP ---------------------------------------<
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.XPKey.COMPLEX_EDITOR_XP,
                    mai.getComplexEditor());
                // EDITOR_XP -----------------------------------------------<
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.XPKey.EDITOR_XP,
                    mai.getEditor());
                // FROM_STRING_XP ------------------------------------------< OK
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.XPKey.FROM_STRING_XP,
                    mai.getFromString());
                // RENDERER_XP ---------------------------------------------<
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.XPKey.RENDERER_XP,
                    mai.getRenderer());
                // TO_STRING_XP --------------------------------------------< OK
                setAttributeConfig(cidsAttribute,
                    AttributeConfig.XPKey.TO_STRING_XP,
                    mai.getToString());

                // FLAGKEY /////////////////////////////////////////////////////
                // ARRAY ---------------------------------------------------< OK
                setAttributeFlag(cidsAttribute,
                    AttributeConfig.FlagKey.ARRAY,
                    mai.isArray());
                // EXTENSION_ATTRIBUTE -------------------------------------<
                setAttributeFlag(
                    cidsAttribute,
                    AttributeConfig.FlagKey.EXTENSION_ATTRIBUTE,
                    mai.isExtensionAttribute());
                // FOREIGN_KEY ---------------------------------------------< OK
                setAttributeFlag(cidsAttribute,
                    AttributeConfig.FlagKey.FOREIGN_KEY,
                    mai.isForeignKey());
                // INDEXED -------------------------------------------------< OK
                setAttributeFlag(cidsAttribute,
                    AttributeConfig.FlagKey.INDEXED,
                    mai.isIndexed());
                // OPTIONAL ------------------------------------------------<
                setAttributeFlag(cidsAttribute,
                    AttributeConfig.FlagKey.OPTIONAL,
                    mai.isOptional());
                // VIRTUAL -------------------------------------------------<
                setAttributeFlag(cidsAttribute,
                    AttributeConfig.FlagKey.VIRTUAL,
                    mai.isVirtual());
                // VISIBLE -------------------------------------------------< OK
                setAttributeFlag(cidsAttribute,
                    AttributeConfig.FlagKey.VISIBLE,
                    mai.isVisible());
                // SUBSTITUTE ----------------------------------------------< OK
                setAttributeFlag(cidsAttribute,
                    AttributeConfig.FlagKey.SUBSTITUTE,
                    mai.isSubstitute());

                cidsClass.putAttribute(cidsAttribute);
            } catch (final Exception ex) {
                LOG.error("could not convert meta object attribute '"
                            + mai.getName() + "' to cids REST attribute: " + ex.getMessage(),
                    ex);
            }
        }

        return cidsClass;
    }

    /**
     * Helper method for setting a well known configuration attribute of a cids class.
     *
     * @param  cidsClass  DOCUMENT ME!
     * @param  key        DOCUMENT ME!
     * @param  value      DOCUMENT ME!
     */
    private void setClassConfig(final CidsClass cidsClass, final CidsClassConfigurationKey key, final Object value) {
        if ((cidsClass != null) && (key != null) && (value != null)) {
            cidsClass.setConfigAttribute(key, value);
        }
    }

    /**
     * Helper method for setting a configuration attribute from a cids class attribute. Instead of storing the complete
     * attribute information, only the value of the attribute is stored.
     *
     * <p>Note: When reconstructing the class attribute information from the deserialized JSON Object, default values
     * have to be set!</p>
     *
     * @param  cidsClass       DOCUMENT ME!
     * @param  key             DOCUMENT ME!
     * @param  classAttribute  value DOCUMENT ME!
     */
    private void setClassConfigFromClassAttribute(final CidsClass cidsClass,
            final CidsClassConfigurationKey key,
            final ClassAttribute classAttribute) {
        if ((cidsClass != null) && (key != null) && (classAttribute != null) && (classAttribute.getValue() != null)) {
            final Object value = classAttribute.getValue();
            if (LOG.isDebugEnabled() && !(value instanceof String)) {
                LOG.warn("setting non-string config attribute '" + key + "'");
            }
            cidsClass.setConfigAttribute(key, value);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsClass       DOCUMENT ME!
     * @param  key             DOCUMENT ME!
     * @param  classAttribute  DOCUMENT ME!
     */
    private void setClassConfigFromClassAttribute(final CidsClass cidsClass,
            final String key,
            final ClassAttribute classAttribute) {
        if ((cidsClass != null) && (key != null) && (classAttribute != null) && (classAttribute.getValue() != null)) {
            final Object value = classAttribute.getValue();
            if (LOG.isDebugEnabled() && !(value instanceof String)) {
                LOG.warn("setting non-string config attribute '" + key + "'");
            }
            cidsClass.setOtherConfigAttribute(key, value);
        }
    }

    /**
     * Helper method for setting a well known boolean configuration attribute (flag) of a cids class.
     *
     * @param  cidsClass  DOCUMENT ME!
     * @param  flagKey    DOCUMENT ME!
     * @param  isSet      DOCUMENT ME!
     */
    private void setClassFlag(final CidsClass cidsClass,
            final CidsClassConfigurationFlagKey flagKey,
            final boolean isSet) {
        if ((cidsClass != null) && (flagKey != null) && isSet) {
            cidsClass.setConfigFlag(flagKey);
        }
    }

    /**
     * Helper method for setting a well known configuration attribute of a cids attribute.
     *
     * @param  cidsAttribute  DOCUMENT ME!
     * @param  key            DOCUMENT ME!
     * @param  value          DOCUMENT ME!
     */
    private void setAttributeConfig(final CidsAttribute cidsAttribute,
            final CidsAttributeConfigurationKey key,
            final Object value) {
        if ((cidsAttribute != null) && (key != null) && (value != null)) {
//            if (LOG.isDebugEnabled() && !(value instanceof String)) {
//                LOG.warn("setting non-string attribute '" + key + "'");
//            }

            if (key == AttributeConfig.Key.DEFAULT_VALUE) {
                LOG.warn("converting attribute default value to string");
                cidsAttribute.setConfigAttribute(key, value.toString());
            } else {
                cidsAttribute.setConfigAttribute(key, value);
            }
        }
    }

    /**
     * Helper method for setting a well known boolean configuration attribute (flag) of a cids attribute.
     *
     * @param  cidsAttribute  DOCUMENT ME!
     * @param  flagKey        DOCUMENT ME!
     * @param  isSet          DOCUMENT ME!
     */
    private void setAttributeFlag(final CidsAttribute cidsAttribute,
            final CidsAttributeConfigurationFlagKey flagKey,
            final boolean isSet) {
        if ((cidsAttribute != null) && (flagKey != null) && isSet) {
            cidsAttribute.setConfigFlag(flagKey);
        }
    }
}
