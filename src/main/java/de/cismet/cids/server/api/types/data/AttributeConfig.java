/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.server.api.types.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * DOCUMENT ME!
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

        NAME("Name"),                              // NOI18N
        ARRAY_KEY_FIELD_NAME("ArrayKeyFieldName"), // NOI18N
        DEFAULT_VALUE("DefaultValue"),             // NOI18N
        POSITION("Position"),                      // NOI18N
        JAVACLASS_NAME("Javaclassname"),           // NOI18N
        REFERENCE_TYPE("ReferenceType");           // NOI18N

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

        FOREIGN_KEY("ForeignKey"),                 // NOI18N
        VISIBLE("Visible"),                        // NOI18N
        INDEXED("Indexed"),                        // NOI18N
        ARRAY("Array"),                            // NOI18N
        OPTIONAL("Optional"),                      // NOI18N
        EXTENSION_ATTRIBUTE("ExtensionAttribute"), // NOI18N
        VIRTUAL("Virtual");                        // NOI18N

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

        EDITOR_XP("EditorXP"),                // NOI18N
        TO_STRING_XP("ToStringXP"),           // NOI18N
        FROM_STRING_XP("FromStringXP"),       // NOI18N
        COMPLEX_EDITOR_XP("ComplexEditorXP"), // NOI18N
        RENDERER_XP("RendererXP");            // NOI18N

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }
}
