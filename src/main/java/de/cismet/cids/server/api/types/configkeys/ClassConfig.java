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

import org.openide.util.Exceptions;

/**
 * <strong>Code copied from package de.cismet.cids.server.data.configkeys (cids-server-rest project)</strong> TODO:
 * Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @version  1.0
 */
public class ClassConfig {

    //~ Enums ------------------------------------------------------------------

    /**
     * Standard configuration attributes of the cids class.
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum Key implements CidsClassConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        LEGACY_ID("Id"), NAME("Name"), DESCRIPTION("Description"),
        // CLASS_ICON( "ClassIcon" ),
        // OBJECT_ICON( "ObjectIcon" ),
        LEGACY_CLASS_ICON("ClassIcon"), LEGACY_OBJECT_ICON("ObjectIcon"), LEGACY_PK_FIELD("PK_Field"), POLICY("Policy"),
        ATTRIBUTE_POLICY("AttributePolicy"), FEATURE_BG("FeatureBG"), FEATURE_FG("FeatureFG"),
        FEATURE_POINT_SYMBOL("FeaturePointSymbol"), FEATURE_POINT_SYMBOL_SWEETSPOT_X("FeaturePointSymbolSweetspotX"),
        FEATURE_POINT_SYMBOL_SWEETSPOT_Y("FeaturePointSymbolSweetspotY"), QUERYABLE("Queryable"),
        SORTING_COLUMN("sortingColumn"), SEARCH_HIT_DYNAMIC_CHILDREN("searchHit_dynamicChildren"),
        SEARCH_HIT_DYNAMIC_CHILDREN_ATTRIBUTE("searchHit_dynamicChildrenAttribute"), HISTORY_ENABLED("history_enabled"), // NOI18N
        HISTORY_OPTION_ANONYMOUS("anonymous"),                                                                           // NOI18N
        TO_STRING_CACHE_ENABLED("tostringcache");                                                                        // NOI18N

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }

    /**
     * Binary configuration attributes (flags).If unset, then false.
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum FlagKey implements CidsClassConfigurationFlagKey {

        //~ Enum constants -----------------------------------------------------

        INDEXED("Indexed"), ARRAY_LINK("ArrayLink"), HIDE_FEATURE("HideFeature"), REASONABLE_FEW("ReasonableFew");

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }

    /**
     * XP (??) configuration attributes.
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum XPKey implements CidsClassConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        EDITOR_XP("EditorXP"), TO_STRING_XP("ToStringXP"), FROM_STRING_XP("FromStringXP"), RENDERER_XP("RendererXP"),
        AGGREGATION_RENDERER_XP("AggregationRendererXP"), ICON_FACTORY_XP("IconFactoryXP"),
        FEATURE_RENDERER_XP("FeatureRendererXP");

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }

    /**
     * FeatureSupportingrasterService configuration attributes.
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum FeatureSupportingrasterServiceKey implements CidsClassConfigurationKey {

        //~ Enum constants -----------------------------------------------------

        FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP("FeatureSupportingRasterServiceSupportXP"),
        FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE("FeatureSupportingRasterServiceIdAttribute"),
        FEATURE_SUPPORTING_RASTER_SERVICE_NAME("FeatureSupportingRasterServiceName"),
        FEATURE_SUPPORTING_RASTER_SERVICE_LAYER("FeatureSupportingRasterServiceLayer"),
        FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL("FeatureSupportingRasterServiceSimpleURL");

        //~ Instance fields ----------------------------------------------------

        private final String key;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        try {
            final ClassConfig.Key classConfigKey = ClassConfig.Key.valueOf(ClassConfig.Key.class, "DESCRIPTION");
            System.out.println("classConfigKey.getKey(): " + classConfigKey.getKey());
            System.out.println("classConfigKey.name(): " + classConfigKey.name());
            System.out.println("classConfigKey.toString(): " + classConfigKey.toString());
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
