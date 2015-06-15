/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.api.types.configkeys;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * <strong>Code copied from package de.cismet.cids.server.data.configkeys (cids-server-rest project)</strong> TODO:
 * Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @author   thorsten
 * @version  1.0
 */
public class AttributeConfig {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum Key implements CidsAttributeConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        LEGACY_ID("Id"), NAME("Name"), LEGACY_ARRAY_KEY_FIELD_NAME("ArrayKeyFieldName"), DEFAULT_VALUE("DefaultValue"),
        POSITION("Position"), LEGACY_JAVACLASS_NAME("JavaClassName"), LEGACY_REFERENCE_TYPE("ReferenceType"),
        LEGACY_FOREIGN_KEY_CLASS_ID("ForeignKeyClassId");

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum FlagKey implements CidsAttributeConfigurationFlagKey {

        //~ Enum constants -----------------------------------------------------

        FOREIGN_KEY("ForeignKey"), VISIBLE("Visible"), INDEXED("Indexed"), ARRAY("Array"), OPTIONAL("Optional"),
        EXTENSION_ATTRIBUTE("ExtensionAttribute"), VIRTUAL("Virtual"), SUBSTITUTE("Subsitute");

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum XPKey implements CidsAttributeConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        EDITOR_XP("EditorXP"), TO_STRING_XP("ToStringXP"), FROM_STRING_XP("FromStringXP"),
        COMPLEX_EDITOR_XP("ComplexEditorXP"), RENDERER_XP("RendererXP");

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }
}
