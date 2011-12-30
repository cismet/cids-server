/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.middleware.types.MetaClass;

import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.cismet.tools.BlacklistClassloading;

/**
 * DOCUMENT ME!
 *
 * @author   stefan
 * @version  $Revision$, $Date$
 */
public class ClassloadingHelper {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClassloadingHelper.class);
    private static final ArrayList<String> packagePrefixes = new ArrayList<String>();

    static {
        final Collection<? extends ClassLoadingPackagePrefixProvider> c = Lookup.getDefault()
                    .lookupAll(ClassLoadingPackagePrefixProvider.class);
        for (final ClassLoadingPackagePrefixProvider pp : c) {
            packagePrefixes.add(pp.getClassLoadingPackagePrefix());
        }
    }

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum CLASS_TYPE {

        //~ Enum constants -----------------------------------------------------

        ICON_FACTORY("treeicons", "IconFactory", "iconfactory"),
        EXTENSION_FACTORY("extensionfactories", "ExtensionFactory", "extensionfactory"),
        RENDERER("objectrenderer", "Renderer", "renderer"),
        AGGREGATION_RENDERER("objectrenderer", "AggregationRenderer", "aggregationrenderer"),
        TO_STRING_CONVERTER("tostringconverter", "ToStringConverter", "tostringconverter"),
        EDITOR("objecteditors", "Editor", "editor"),
        ATTRIBUTE_EDITOR("objecteditors", "AttributeEditor", "attributeeditor"),
        FEATURE_RENDERER("featurerenderer", "FeatureRenderer", "featurerenderer"),
        ACTION_PROVIDER("objectactions", "ActionsProvider", "actionsprovider"),
        PERMISSION_PROVIDER("permissions", "PermissionProvider", "permissionprovider");

        //~ Instance fields ----------------------------------------------------

        final String packagePrefix;
        final String classNameSuffix;
        final String overrideProperty;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new CLASS_TYPE object.
         *
         * @param  packagePrefix     DOCUMENT ME!
         * @param  classNameSuffix   DOCUMENT ME!
         * @param  overrideProperty  DOCUMENT ME!
         */
        private CLASS_TYPE(final String packagePrefix, final String classNameSuffix, final String overrideProperty) {
            this.packagePrefix = packagePrefix;
            this.classNameSuffix = classNameSuffix;
            this.overrideProperty = overrideProperty;
        }
    }

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new ClassloadingHelper object.
     *
     * @throws  AssertionError  DOCUMENT ME!
     */
    private ClassloadingHelper() {
        throw new AssertionError();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   mai        DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<String> getClassNames(final MetaClass metaClass,
            final MemberAttributeInfo mai,
            final CLASS_TYPE classType) {
        final List<String> result = new ArrayList<String>();
        final String domain = metaClass.getDomain().toLowerCase();
        final String tableName = metaClass.getTableName().toLowerCase();
        final String fieldName = mai.getFieldName().toLowerCase();
        final String overrideClassName = System.getProperty(domain + "." + tableName + "." + fieldName + "."
                        + classType.overrideProperty);
        if (overrideClassName != null) {
            result.add(overrideClassName);
        }

        for (final String masterPrefix : packagePrefixes) {
            final StringBuilder plainClassNameBuilder = new StringBuilder(masterPrefix + "." + classType.packagePrefix);
            plainClassNameBuilder.append(".").append(domain).append(".").append(tableName).append(".");
            final StringBuilder camelCaseClassNameBuilder = new StringBuilder(plainClassNameBuilder);
            plainClassNameBuilder.append(capitalize(fieldName)).append(classType.classNameSuffix);
            camelCaseClassNameBuilder.append(camelize(fieldName)).append(classType.classNameSuffix);
            //
            result.add(plainClassNameBuilder.toString());
            result.add(camelCaseClassNameBuilder.toString());
        }
        //
        final String configurationClassName = ((mai == null) ? getClassNameByConfiguration(metaClass, classType)
                                                             : getClassNameByConfiguration(mai, classType));
        if (configurationClassName != null) {
            result.add(configurationClassName);
        }
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   toCapitalize  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String capitalize(final String toCapitalize) {
        final StringBuilder result = new StringBuilder(toCapitalize.length());
        result.append(toCapitalize.substring(0, 1).toUpperCase()).append(toCapitalize.substring(1).toLowerCase());
        return result.toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<String> getClassNames(final MetaClass metaClass, final CLASS_TYPE classType) {
        final List<String> result = new ArrayList<String>();
        final String tableName = metaClass.getTableName().toLowerCase();
        final String domain = metaClass.getDomain().toLowerCase();
        final String overrideClassName = System.getProperty(domain + "." + tableName + "."
                        + classType.overrideProperty);
        if (overrideClassName != null) {
            result.add(overrideClassName);
        }
        if (tableName.length() > 2) {
            for (final String masterPrefix : packagePrefixes) {
                final StringBuilder plainClassNameBuilder = new StringBuilder(masterPrefix + "."
                                + classType.packagePrefix);
                plainClassNameBuilder.append(".").append(domain).append(".");
                final StringBuilder camelCaseClassNameBuilder = new StringBuilder(plainClassNameBuilder);
                //
                plainClassNameBuilder.append(capitalize(tableName)).append(classType.classNameSuffix);
                camelCaseClassNameBuilder.append(camelize(tableName)).append(classType.classNameSuffix);
                //
                result.add(plainClassNameBuilder.toString());
                result.add(camelCaseClassNameBuilder.toString());
            }
            //
            final String configurationClassName = getClassNameByConfiguration(metaClass, classType);
            if (configurationClassName != null) {
                result.add(configurationClassName);
            }
        } else {
            log.error("Invalid table name: " + tableName);
        }
        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getClassNameByConfiguration(final MetaClass metaClass, final CLASS_TYPE classType) {
        switch (classType) {
//            case ICON_FACTORY:
//                break;
            case TO_STRING_CONVERTER: {
                return metaClass.getToString();
            }
            case RENDERER: {
                return metaClass.getRenderer();
            }
            case EDITOR: {
                return metaClass.getEditor();
            }
            case AGGREGATION_RENDERER: {
                return metaClass.getRenderer();
            }
            case FEATURE_RENDERER: {
                return getClassAttributeValue("FEATURE_RENDERER", metaClass);
            }
//            case EXTENSION_FACTORY:
//                break;
            default: {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mai        DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getClassNameByConfiguration(final MemberAttributeInfo mai, final CLASS_TYPE classType) {
        switch (classType) {
            case TO_STRING_CONVERTER: {
                return mai.getToString();
            }
            case RENDERER: {
                return mai.getRenderer();
            }
            case EDITOR: {
                return mai.getEditor();
            }
            case AGGREGATION_RENDERER: {
                return mai.getRenderer();
            }
            default: {
                return null;
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   mai        DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getClassNameByConfiguration(final MetaClass metaClass,
            final MemberAttributeInfo mai,
            final CLASS_TYPE classType) {
        switch (classType) {
            case ATTRIBUTE_EDITOR: {
                return mai.getEditor();
            }
            default: {
                return getClassNameByConfiguration(metaClass, classType);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     * @param   mc    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String getClassAttributeValue(final String name, final MetaClass mc) {
        final Collection cca = mc.getAttributeByName(name);
        if (cca.size() > 0) {
            final ClassAttribute ca = (ClassAttribute)(cca.toArray()[0]);
            final Object valueObj = ca.getValue();
            if (valueObj != null) {
                return valueObj.toString();
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String camelize(final String tableName) {
        boolean upperCase = true;
        final char[] result = new char[tableName.length()];
        int resultPosition = 0;
        for (int i = 0; i < tableName.length(); ++i) {
            char current = tableName.charAt(i);
            if (Character.isLetterOrDigit(current)) {
                if (upperCase) {
                    current = Character.toUpperCase(current);
                    upperCase = false;
                } else {
                    current = Character.toLowerCase(current);
                }
                result[resultPosition++] = current;
            } else {
                upperCase = true;
            }
        }
        return String.valueOf(result, 0, resultPosition);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   candidateClassNames  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Class<?> loadClassFromCandidates(final List<String> candidateClassNames) {
        for (final String candidateClassName : candidateClassNames) {
            final Class<?> result = BlacklistClassloading.forName(candidateClassName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   mai        DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Class<?> getDynamicClass(final MetaClass metaClass,
            final MemberAttributeInfo mai,
            final CLASS_TYPE classType) {
        final List<String> classNames = getClassNames(metaClass, mai, classType);
        return loadClassFromCandidates(classNames);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Class<?> getDynamicClass(final MetaClass metaClass, final CLASS_TYPE classType) {
        final List<String> classNames = getClassNames(metaClass, classType);
        return loadClassFromCandidates(classNames);
    }
}
