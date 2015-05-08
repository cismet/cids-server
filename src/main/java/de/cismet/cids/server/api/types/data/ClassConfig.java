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
 * @version  1.0
 */
public class ClassConfig {

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum Key implements CidsClassConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        NAME("Name"),                                                     // NOI18N
        CLASS_ICON("ClassIcon"),                                          // NOI18N
        OBJECT_ICON("ObjectIcon"),                                        // NOI18N
        PK_FIELD("PK_Field"),                                             // NOI18N
        POLICY("Policy"),                                                 // NOI18N
        ATTRIBUTE_POLICY("AttributePolicy"),                              // NOI18N
        FEATURE_BG("FeatureBG"),                                          // NOI18N
        FEATURE_FG("FeatureFG"),                                          // NOI18N
        FEATURE_POINT_SYMBOL("FeaturePointSymbol"),                       // NOI18N
        FEATURE_POINT_SYMBOL_SWEETSPOT_X("FeaturePointSymbolSweetspotX"), // NOI18N
        FEATURE_POINT_SYMBOL_SWEETSPOT_Y("FeaturePointSymbolSweetspotY"); // NOI18N

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
    public enum FlagKey implements CidsClassConfigurationFlagKey {

        //~ Enum constants -----------------------------------------------------

        INDEXED("Indexed"),          // NOI18N
        ARRAY_LINK("ArrayLink"),     // NOI18N
        HIDE_FEATURE("HideFeature"), // NOI18N
        REASONABLE_FEW("ReasonableFew"); // NOI18N

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
    public enum XPKey implements CidsClassConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        EDITOR_XP("EditorXP"),                            // NOI18N
        TO_STRING_XP("ToStringXP"),                       // NOI18N
        FROM_STRING_XP("FromStringXP"),                   // NOI18N
        RENDERER_XP("RendererXP"),                        // NOI18N
        AGGREGATION_RENDERER_XP("AggregationRendererXP"), // NOI18N
        ICON_FACTORY_XP("IconFactoryXP"),                 // NOI18N
        FEATURE_RENDERER_XP("FeatureRendererXP");         // NOI18N

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
    public enum FeatureSupportingrasterServiceKey implements CidsClassConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP("FeatureSupportingRasterServiceSupportXP"),     // NOI18N
        FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE("FeatureSupportingRasterServiceIdAttribute"), // NOI18N
        FEATURE_SUPPORTING_RASTER_SERVICE_NAME("FeatureSupportingRasterServiceName"),                // NOI18N
        FEATURE_SUPPORTING_RASTER_SERVICE_LAYER("FeatureSupportingRasterServiceLayer"),              // NOI18N
        FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL("FeatureSupportingRasterServiceSimpleURL");     // NOI18N

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }
}
