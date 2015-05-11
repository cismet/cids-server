/**
 * *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 * 
* ... and it just works.
 * 
***************************************************
 */
package de.cismet.cids.server.api.types.configkeys;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <strong>Code copied from package de.cismet.cids.server.data.configkeys
 * (cids-server-rest project)</strong>
 * TODO: Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @author thorsten
 * @version 1.0
 */
public class AttributeConfig {

    /**
     * DOCUMENT ME!
     *
     * @version 1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum Key
            implements CidsAttributeConfigurationKey {

        NAME("Name"),
        ARRAY_KEY_FIELD_NAME("ArrayKeyFieldName"),
        DEFAULT_VALUE("DefaultValue"),
        POSITION("Position"),
        JAVACLASS_NAME("Javaclassname"),
        REFERENCE_TYPE("ReferenceType");

        private final String key;
    }

    /**
     * DOCUMENT ME!
     *
     * @version 1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum FlagKey
            implements CidsAttributeConfigurationFlagKey {

        FOREIGN_KEY("ForeignKey"),
        VISIBLE("Visible"),
        INDEXED("Indexed"),
        ARRAY("Array"),
        OPTIONAL("Optional"),
        EXTENSION_ATTRIBUTE("ExtensionAttribute"),
        VIRTUAL("Virtual");

        private final String key;
    }

    /**
     * DOCUMENT ME!
     *
     * @version 1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum XPKey
            implements CidsAttributeConfigurationKey {

        EDITOR_XP("EditorXP"),
        TO_STRING_XP("ToStringXP"),
        FROM_STRING_XP("FromStringXP"),
        COMPLEX_EDITOR_XP("ComplexEditorXP"),
        RENDERER_XP("RendererXP");

        private final String key;
    }
}
