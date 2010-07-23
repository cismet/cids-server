package de.cismet.cids.utils;

import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.middleware.types.MetaClass;
import de.cismet.tools.BlacklistClassloading;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author stefan
 */
public class ClassloadingHelper {

    private ClassloadingHelper() {
        throw new AssertionError();
    }

    public enum CLASS_TYPE {

        ICON_FACTORY("de.cismet.cids.custom.treeicons", "IconFactory", "iconfactory"),
        EXTENSION_FACTORY("de.cismet.cids.custom.extensionfactories", "ExtensionFactory", "extensionfactory"),
        RENDERER("de.cismet.cids.custom.objectrenderer", "Renderer", "renderer"),
        AGGREGATION_RENDERER("de.cismet.cids.custom.objectrenderer", "AggregationRenderer", "aggregationrenderer"),
        TO_STRING_CONVERTER("de.cismet.cids.custom.tostringconverter", "ToStringConverter", "tostringconverter"),
        EDITOR("de.cismet.cids.custom.objecteditors", "Editor", "editor"),
        ATTRIBUTE_EDITOR("de.cismet.cids.custom.objecteditors", "AttributeEditor", "attributeeditor"),
        FEATURE_RENDERER("de.cismet.cids.custom.featurerenderer", "FeatureRenderer", "featurerenderer");

        private CLASS_TYPE(String packagePrefix, String classNameSuffix, String overrideProperty) {
            this.packagePrefix = packagePrefix;
            this.classNameSuffix = classNameSuffix;
            this.overrideProperty = overrideProperty;
        }
        final String packagePrefix;
        final String classNameSuffix;
        final String overrideProperty;
    }
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ClassloadingHelper.class);

    public static List<String> getClassNames(MetaClass metaClass, MemberAttributeInfo mai, CLASS_TYPE classType) {
        String domain = metaClass.getDomain().toLowerCase();
        String tableName = metaClass.getTableName().toLowerCase();
        String fieldName = mai.getFieldName().toLowerCase();
        List<String> result = new ArrayList<String>();
        String overrideClassName = System.getProperty(domain + "." + tableName + "." + fieldName + "." + classType.overrideProperty);
        if (overrideClassName != null) {
            result.add(overrideClassName);
        }
        StringBuilder plainClassNameBuilder = new StringBuilder(classType.packagePrefix);
        plainClassNameBuilder.append(".").append(domain).append(".").append(tableName).append(".");
        StringBuilder camelCaseClassNameBuilder = new StringBuilder(plainClassNameBuilder);
        plainClassNameBuilder.append(capitalize(fieldName)).append(classType.classNameSuffix);
        camelCaseClassNameBuilder.append(camelize(fieldName)).append(classType.classNameSuffix);
        //
        result.add(plainClassNameBuilder.toString());
        result.add(camelCaseClassNameBuilder.toString());
        //

        String configurationClassName = mai == null ? getClassNameByConfiguration(metaClass, classType) : getClassNameByConfiguration(mai, classType);
        if (configurationClassName != null) {
            result.add(configurationClassName);
        }
        return result;
    }

    public static String capitalize(String toCapitalize) {
        StringBuilder result = new StringBuilder(toCapitalize.length());
        result.append(toCapitalize.substring(0, 1).toUpperCase()).append(toCapitalize.substring(1).toLowerCase());
        return result.toString();
    }

    public static List<String> getClassNames(MetaClass metaClass, CLASS_TYPE classType) {
        String tableName = metaClass.getTableName().toLowerCase();
        String domain = metaClass.getDomain().toLowerCase();
        List<String> result = new ArrayList<String>();
        String overrideClassName = System.getProperty(domain + "." + tableName + "." + classType.overrideProperty);
        if (overrideClassName != null) {
            result.add(overrideClassName);
        }
        if (tableName.length() > 2) {
            StringBuilder plainClassNameBuilder = new StringBuilder(classType.packagePrefix);
            plainClassNameBuilder.append(".").append(domain).append(".");
            StringBuilder camelCaseClassNameBuilder = new StringBuilder(plainClassNameBuilder);
            //
            plainClassNameBuilder.append(capitalize(tableName)).append(classType.classNameSuffix);
            //
            camelCaseClassNameBuilder.append(camelize(tableName)).append(classType.classNameSuffix);
            //
            result.add(plainClassNameBuilder.toString());
            result.add(camelCaseClassNameBuilder.toString());
            //
            String configurationClassName = getClassNameByConfiguration(metaClass, classType);
            if (configurationClassName != null) {
                result.add(configurationClassName);
            }
        } else {
            log.error("Invalid table name: " + tableName);
        }
        return result;
    }

    public static String getClassNameByConfiguration(MetaClass metaClass, CLASS_TYPE classType) {
        switch (classType) {
//            case ICON_FACTORY:
//                break;
            case TO_STRING_CONVERTER:
                return metaClass.getToString();
            case RENDERER:
                return metaClass.getRenderer();
            case EDITOR:
                return metaClass.getEditor();
            case AGGREGATION_RENDERER:
                return metaClass.getRenderer();
            case FEATURE_RENDERER:
                return getClassAttributeValue("FEATURE_RENDERER", metaClass);
//            case EXTENSION_FACTORY:
//                break;
            default:
                return null;
        }
    }

    public static String getClassNameByConfiguration(MemberAttributeInfo mai, CLASS_TYPE classType) {
        switch (classType) {
            case TO_STRING_CONVERTER:
                return mai.getToString();
            case RENDERER:
                return mai.getRenderer();
            case EDITOR:
                return mai.getEditor();
            case AGGREGATION_RENDERER:
                return mai.getRenderer();
            default:
                return null;
        }
    }

    public static String getClassNameByConfiguration(MetaClass metaClass, MemberAttributeInfo mai, CLASS_TYPE classType) {
        switch (classType) {
            case ATTRIBUTE_EDITOR:
                return mai.getEditor();
            default:
                return getClassNameByConfiguration(metaClass, classType);
        }
    }

    private static String getClassAttributeValue(String name, final MetaClass mc) {
        final Collection cca = mc.getAttributeByName(name);
        if (cca.size() > 0) {
            final ClassAttribute ca = (ClassAttribute) (cca.toArray()[0]);
            Object valueObj = ca.getValue();
            if (valueObj != null) {
                return valueObj.toString();
            }
        }
        return null;
    }

    public static String camelize(String tableName) {
        boolean upperCase = true;
        char[] result = new char[tableName.length()];
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

    public static Class<?> loadClassFromCandidates(List<String> candidateClassNames) {
        for (String candidateClassName : candidateClassNames) {
            Class<?> result = BlacklistClassloading.forName(candidateClassName);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static Class<?> getDynamicClass(MetaClass metaClass, MemberAttributeInfo mai, CLASS_TYPE classType) {
        final List<String> classNames = getClassNames(metaClass, mai, classType);
        return loadClassFromCandidates(classNames);
    }

    public static Class<?> getDynamicClass(MetaClass metaClass, CLASS_TYPE classType) {
        final List<String> classNames = getClassNames(metaClass, classType);
        return loadClassFromCandidates(classNames);
    }
}
