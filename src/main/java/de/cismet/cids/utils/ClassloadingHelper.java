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

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import de.cismet.commons.classloading.BlacklistClassloading;

import de.cismet.commons.ref.PurgingCache;

import de.cismet.commons.utils.StringUtils;

import de.cismet.tools.Calculator;

/**
 * DOCUMENT ME!
 *
 * @author   stefan
 * @version  $Revision$, $Date$
 */
public class ClassloadingHelper {

    //~ Static fields/initializers ---------------------------------------------

    /** A property file that may contain configurations concerning classloading. */
    public static final String CL_PROPERTIES = "classloading.properties";       // NOI18N
    /** The property which can be used to specify alternative classloading domains, value is expected to be csv. */
    public static final String CL_PROP_ALT_DOMAINS = "classloading.alternativeDomains";                         // NOI18N

    private static final transient Logger LOG;
    private static final List<String> PACKAGE_PREFIXES;
    private static final transient PurgingCache<String, List<String>> ALT_DOMAIN_CACHE;

    static {
        LOG = Logger.getLogger(ClassloadingHelper.class);

        PACKAGE_PREFIXES = new ArrayList<String>();
        final Collection<? extends ClassLoadingPackagePrefixProvider> c = Lookup.getDefault()
                    .lookupAll(ClassLoadingPackagePrefixProvider.class);

        for (final ClassLoadingPackagePrefixProvider pp : c) {
            PACKAGE_PREFIXES.add(pp.getClassLoadingPackagePrefix());
        }

        ALT_DOMAIN_CACHE = new PurgingCache<String, List<String>>(new Calculator<String, List<String>>() {

                    @Override
                    public List<String> calculate(final String domain) throws Exception {
                        final List<String> altDomains = new ArrayList<String>(0);

                        for (final String prefix : PACKAGE_PREFIXES) {
                            final StringBuilder candBuilder = new StringBuilder("/"); // NOI18N
                            candBuilder.append(prefix.replaceAll("\\.", "/"));        // NOI18N
                            if ('/' != candBuilder.charAt(candBuilder.length() - 1)) {
                                candBuilder.append('/');
                            }
                            candBuilder.append(domain).append('/').append(CL_PROPERTIES);

                            final String candidate = candBuilder.toString();

                            if (LOG.isDebugEnabled()) {
                                LOG.debug("trying to read classloading.properties from candidate: " + candidate); // NOI18N
                            }

                            final InputStream is = ClassloadingHelper.class.getResourceAsStream(candidate);
                            if (is != null) {
                                final Properties clProperties = new Properties();
                                try {
                                    clProperties.load(is);

                                    final String altDomainsCSV = clProperties.getProperty(CL_PROP_ALT_DOMAINS);

                                    if (altDomainsCSV != null) {
                                        if (LOG.isDebugEnabled()) {
                                            LOG.debug(
                                                "found alternative domain list: [domain=" // NOI18N
                                                        + domain
                                                        + "|altDomains="                  // NOI18N
                                                        + altDomainsCSV
                                                        + "]");                           // NOI18N
                                        }

                                        final String[] altDomainSplit = altDomainsCSV.split(",");                      // NOI18N
                                        for (final String altDomain : altDomainSplit) {
                                            final String domCandidate = altDomain.trim();
                                            if (!domCandidate.isEmpty()) {
                                                altDomains.add(domCandidate);
                                            }
                                        }
                                    }
                                } catch (final IOException ex) {
                                    LOG.warn("cannot load class loading properties from candidate: " + candidate, ex); // NOI18N
                                }
                            }
                        }

                        return altDomains;
                    }
                }, 0, 0);
    }

    //~ Enums ------------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public enum CLASS_TYPE {

        //~ Enum constants -----------------------------------------------------

        ICON_FACTORY("treeicons", "IconFactory", "iconfactory"),                              // NOI18N
        EXTENSION_FACTORY("extensionfactories", "ExtensionFactory", "extensionfactory"),      // NOI18N
        RENDERER("objectrenderer", "Renderer", "renderer"),                                   // NOI18N
        AGGREGATION_RENDERER("objectrenderer", "AggregationRenderer", "aggregationrenderer"), // NOI18N
        TO_STRING_CONVERTER("tostringconverter", "ToStringConverter", "tostringconverter"),   // NOI18N
        EDITOR("objecteditors", "Editor", "editor"),                                          // NOI18N
        ATTRIBUTE_EDITOR("objecteditors", "AttributeEditor", "attributeeditor"),              // NOI18N
        FEATURE_RENDERER("featurerenderer", "FeatureRenderer", "featurerenderer"),            // NOI18N
        ACTION_PROVIDER("objectactions", "ActionsProvider", "actionsprovider"),               // NOI18N
        PERMISSION_PROVIDER("permissions", "PermissionProvider", "permissionprovider"),       // NOI18N
        CUSTOM_BEAN("beans", "CustomBean", "custombean");                                     // NOI18N

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
        final String overrideClassName = System.getProperty(domain + "." + tableName + "." + fieldName + "." // NOI18N
                        + classType.overrideProperty);
        if (overrideClassName != null) {
            result.add(overrideClassName);
        }

        for (final String masterPrefix : PACKAGE_PREFIXES) {
            final StringBuilder plainClassNameBuilder = new StringBuilder(masterPrefix + "." + classType.packagePrefix); // NOI18N
            plainClassNameBuilder.append(".").append(domain).append(".").append(tableName).append(".");                  // NOI18N
            final StringBuilder camelCaseClassNameBuilder = new StringBuilder(plainClassNameBuilder);
            plainClassNameBuilder.append(capitalize(fieldName)).append(classType.classNameSuffix);
            camelCaseClassNameBuilder.append(camelize(fieldName)).append(classType.classNameSuffix);

            result.add(plainClassNameBuilder.toString());
            result.add(camelCaseClassNameBuilder.toString());
        }

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

        final String domain = metaClass.getDomain().toLowerCase();

        result.addAll(getClassNames(metaClass, classType, domain));

        final List<String> altDomains = ALT_DOMAIN_CACHE.get(domain);

        for (final String altDomain : altDomains) {
            result.addAll(getClassNames(metaClass, classType, altDomain));
        }

        return result;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     * @param   classType  DOCUMENT ME!
     * @param   forDomain  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static List<String> getClassNames(final MetaClass metaClass,
            final CLASS_TYPE classType,
            final String forDomain) {
        final List<String> result = new ArrayList<String>();
        final String tableName = metaClass.getTableName().toLowerCase();

        final String domain = StringUtils.toPackage(forDomain);

        final String overrideClassName = System.getProperty(domain + "." + tableName + "." // NOI18N
                        + classType.overrideProperty);

        if (overrideClassName != null) {
            result.add(overrideClassName);
        }

        if (tableName.length() > 2) {
            for (final String masterPrefix : PACKAGE_PREFIXES) {
                final StringBuilder plainClassNameBuilder = new StringBuilder(masterPrefix + "." // NOI18N
                                + classType.packagePrefix);
                plainClassNameBuilder.append(".").append(domain).append("."); // NOI18N
                final StringBuilder camelCaseClassNameBuilder = new StringBuilder(plainClassNameBuilder);
                //
                plainClassNameBuilder.append(capitalize(tableName)).append(classType.classNameSuffix);
                camelCaseClassNameBuilder.append(camelize(tableName)).append(classType.classNameSuffix);
                //
                result.add(plainClassNameBuilder.toString());
                result.add(camelCaseClassNameBuilder.toString());
            }
            // FIXME: mscholl: convention over configuration but this means that convention has precedence
            final String configurationClassName = getClassNameByConfiguration(metaClass, classType);
            if (configurationClassName != null) {
                result.add(configurationClassName);
            }
        } else {
            LOG.error("Invalid table name: " + tableName); // NOI18N
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
                final String caCfg = getClassAttributeValue("AGGREGATION_RENDERER", metaClass); // NOI18N
                if (caCfg == null) {
                    // for backwards-compatibility
                    return metaClass.getRenderer();
                } else {
                    return caCfg;
                }
            }
            case FEATURE_RENDERER: {
                return getClassAttributeValue("FEATURE_RENDERER", metaClass); // NOI18N
            }
            case CUSTOM_BEAN: {
                return getClassAttributeValue("CUSTOM_BEAN", metaClass);      // NOI18N
            }
            case ACTION_PROVIDER: {
                return getClassAttributeValue("ACTION_PROVIDER", metaClass);  // NOI18N
            }
            case ICON_FACTORY: {
                return getClassAttributeValue("ICON_FACTORY", metaClass);     // NOI18N
            }
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
