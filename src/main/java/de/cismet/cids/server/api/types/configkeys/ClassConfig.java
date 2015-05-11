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
 * <strong>Code copied  from package de.cismet.cids.server.data.configkeys (cids-server-rest project)</strong>
 * TODO: Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @version  1.0
 */
public class ClassConfig
{
    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum Key
        implements CidsClassConfigurationKey
    {NAME( "Name" ),
        CLASS_ICON( "ClassIcon" ),
        OBJECT_ICON( "ObjectIcon" ),
        PK_FIELD( "PK_Field" ),
        POLICY( "Policy" ),
        ATTRIBUTE_POLICY( "AttributePolicy" ),
        FEATURE_BG( "FeatureBG" ),
        FEATURE_FG( "FeatureFG" ),
        FEATURE_POINT_SYMBOL( "FeaturePointSymbol" ),
        FEATURE_POINT_SYMBOL_SWEETSPOT_X( "FeaturePointSymbolSweetspotX" ),
        FEATURE_POINT_SYMBOL_SWEETSPOT_Y( "FeaturePointSymbolSweetspotY" );

        private final String key;
    }

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum FlagKey
        implements CidsClassConfigurationFlagKey
    {INDEXED( "Indexed" ),
        ARRAY_LINK( "ArrayLink" ),
        HIDE_FEATURE( "HideFeature" ),
        REASONABLE_FEW( "ReasonableFew" );

        private final String key;
    }

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum XPKey
        implements CidsClassConfigurationKey
    {EDITOR_XP( "EditorXP" ),
        TO_STRING_XP( "ToStringXP" ),
        FROM_STRING_XP( "FromStringXP" ),
        RENDERER_XP( "RendererXP" ),
        AGGREGATION_RENDERER_XP( "AggregationRendererXP" ),
        ICON_FACTORY_XP( "IconFactoryXP" ),
        FEATURE_RENDERER_XP( "FeatureRendererXP" );

        private final String key;
    }

    /**
     * DOCUMENT ME!
     *
     * @version  1.0
     */
    @Getter
    @RequiredArgsConstructor
    public enum FeatureSupportingrasterServiceKey
        implements CidsClassConfigurationKey
    {FEATURE_SUPPORTING_RASTER_SERVICE_SUPPORT_XP( "FeatureSupportingRasterServiceSupportXP" ),
        FEATURE_SUPPORTING_RASTER_SERVICE_ID_ATTRIBUTE( "FeatureSupportingRasterServiceIdAttribute" ),
        FEATURE_SUPPORTING_RASTER_SERVICE_NAME( "FeatureSupportingRasterServiceName" ),
        FEATURE_SUPPORTING_RASTER_SERVICE_LAYER( "FeatureSupportingRasterServiceLayer" ),
        FEATURE_SUPPORTING_RASTER_SERVICE_SIMPLE_URL( "FeatureSupportingRasterServiceSimpleURL" );

        private final String key;
    }
}
