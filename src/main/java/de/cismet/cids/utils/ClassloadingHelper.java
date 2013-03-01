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
    /**
     * The property which can be used to specify alternative classloading domains, stringValue is expected to be csv.
     */
    public static final String CL_PROP_ALT_DOMAINS = "classloading.alternativeDomains"; // NOI18N
    /**
     * The property which can be used to specify the desired order during candidate class name build. Valid values are:
     * <br/>
     * <br/>
     *
     * <ul>
     *   <li><i>default</i> (classtype only) )</li>
     *   <li><i>classtype</i> (classtype first only, <b>default</b>)</li>
     *   <li><i>domain</i> (domain first only)</li>
     *   <li><i>both-classtype</i> (both, classtype first)</li>
     *   <li><i>both-domain</i> (both, domain first)</li>
     * </ul>
     */
    public static final String CL_PROP_DOM_CTYPE_ORDER = "classloading.domainClassTypeOrder"; // NOI18N

    private static final transient Logger LOG;
    private static final List<String> PACKAGE_PREFIXES;
    private static final transient PurgingCache<String, List<String>> ALT_DOMAIN_CACHE;
    private static final transient PurgingCache<String, DOM_CTYPE_ORDER> DOM_CTYPE_ORDER_CACHE;

    static {
        LOG = Logger.getLogger(ClassloadingHelper.class);

        PACKAGE_PREFIXES = new ArrayList<String>();
        final Collection<? extends ClassLoadingPackagePrefixProvider> c = Lookup.getDefault()
                    .lookupAll(ClassLoadingPackagePrefixProvider.class);

        for (final ClassLoadingPackagePrefixProvider pp : c) {
            PACKAGE_PREFIXES.add(pp.getClassLoadingPackagePrefix());
        }

        // NOTE: we don't cache the properties needed to fill the cache as they are only needed at cache buildup and
        // thus we don't want to waste the memory for the whole application and use some extra IO at startup instead

        ALT_DOMAIN_CACHE = new PurgingCache<String, List<String>>(new Calculator<String, List<String>>() {

                    @Override
                    public List<String> calculate(final String domain) throws Exception {
                        final List<String> altDomains = new ArrayList<String>(0);

                        for (final String prefix : PACKAGE_PREFIXES) {
                            final String altDomainsCSV = getProperty(CL_PROP_ALT_DOMAINS, prefix, domain);

                            if (altDomainsCSV != null) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                        "found alternative domain list: [domain=" // NOI18N
                                                + domain
                                                + "|altDomains="                  // NOI18N
                                                + altDomainsCSV
                                                + "]");                           // NOI18N
                                }

                                final String[] altDomainSplit = altDomainsCSV.split(","); // NOI18N
                                for (final String altDomain : altDomainSplit) {
                                    final String domCandidate = altDomain.trim();
                                    if (!domCandidate.isEmpty()) {
                                        altDomains.add(domCandidate);
                                    }
                                }
                            }
                        }

                        return altDomains;
                    }
                },
                0,
                0);

        DOM_CTYPE_ORDER_CACHE = new PurgingCache<String, DOM_CTYPE_ORDER>(new Calculator<String, DOM_CTYPE_ORDER>() {

                    @Override
                    public DOM_CTYPE_ORDER calculate(final String domain) throws Exception {
                        for (final String prefix : PACKAGE_PREFIXES) {
                            final String orderProp = getProperty(CL_PROP_DOM_CTYPE_ORDER, prefix, domain);

                            if (orderProp != null) {
                                if (LOG.isDebugEnabled()) {
                                    LOG.debug(
                                        "found order directive: [domain=" // NOI18N
                                                + domain
                                                + "|order="               // NOI18N
                                                + orderProp
                                                + "]");                   // NOI18N
                                }

                                try {
                                    // first come, first serve
                                    return DOM_CTYPE_ORDER.getEnumValue(orderProp);
                                } catch (final IllegalArgumentException e) {
                                    if (LOG.isInfoEnabled()) {
                                        LOG.info(
                                            "ignoring illegal value of '"                          // NOI18N
                                                    + CL_PROP_DOM_CTYPE_ORDER
                                                    + "' property for domain and prefix: [domain=" // NOI18N
                                                    + domain
                                                    + "|prefix="                                   // NOI18N
                                                    + prefix
                                                    + "]",                                         // NOI18N
                                            e);
                                    }
                                }
                            }
                        }

                        // no valid hit, use default
                        return DOM_CTYPE_ORDER.DEFAULT;
                    }
                },
                0,
                0);
    }

    //~ Enums ------------------------------------------------------------------

    /**
     * Allowed domain classtype order directives.
     *
     * @version  1.0
     */
    public enum DOM_CTYPE_ORDER {

        //~ Enum constants -----------------------------------------------------

        DEFAULT("default"),               // NOI18N
        CLASSTYPE("classtype"),           // NOI18N
        DOMAIN("domain"),                 // NOI18N
        BOTH_CLASSTYPE("both-classtype"), // NOI18N
        BOTH_DOMAIN("both-domain");       // NOI18N

        //~ Instance fields ----------------------------------------------------

        final String stringValue;

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new DOM_CTYPE_ORDER object.
         *
         * @param  stringValue  DOCUMENT ME!
         */
        private DOM_CTYPE_ORDER(final String stringValue) {
            this.stringValue = stringValue;
        }

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param   stringValue  DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         *
         * @throws  IllegalArgumentException  DOCUMENT ME!
         */
        public static DOM_CTYPE_ORDER getEnumValue(final String stringValue) {
            if (DEFAULT.stringValue.equals(stringValue)) {
                return DEFAULT;
            } else if (CLASSTYPE.stringValue.equals(stringValue)) {
                return CLASSTYPE;
            } else if (DOMAIN.stringValue.equals(stringValue)) {
                return DOMAIN;
            } else if (BOTH_CLASSTYPE.stringValue.equals(stringValue)) {
                return BOTH_CLASSTYPE;
            } else if (BOTH_DOMAIN.stringValue.equals(stringValue)) {
                return BOTH_DOMAIN;
            } else {
                throw new IllegalArgumentException("provided unknown string value, returning null: " + stringValue); // NOI18N
            }
        }

        /**
         * DOCUMENT ME!
         *
         * @return  DOCUMENT ME!
         */
        public String getStringValue() {
            return stringValue;
        }
    }

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
     * Reads a property from a property file. Uses {@link #buildPropertyCandidate(java.lang.String, java.lang.String)}
     * to build the property candidate name and tries to load the properties from there using
     * {@link #loadProperties(java.lang.String) }.
     *
     * @param   propertyName  the name of the property to read
     * @param   prefix        the prefix to use to build the candidate name
     * @param   domain        the name to use to build the candidate name
     *
     * @return  the value of the property or <code>null</code> if there is an error or the properties are non-existent
     */
    private static String getProperty(final String propertyName, final String prefix, final String domain) {
        final String candidate = buildPropertyCandidate(prefix, domain);

        if (LOG.isDebugEnabled()) {
            LOG.debug("trying to read classloading.properties from candidate: " + candidate); // NOI18N
        }

        try {
            final Properties clProperties = loadProperties(candidate);

            if (clProperties != null) {
                return clProperties.getProperty(propertyName);
            }
        } catch (final IOException ex) {
            LOG.warn("cannot load class loading properties from candidate: " + candidate, ex); // NOI18N
        }

        return null;
    }

    /**
     * Loads properties from the specified resource path.
     *
     * @param   propertyResource  the resource path to load the properties from
     *
     * @return  the loaded properties of <code>null</code> if the resource was not present
     *
     * @throws  IOException  if an error occurs while loading the properties
     */
    private static Properties loadProperties(final String propertyResource) throws IOException {
        final Properties clProperties;
        final InputStream is = ClassloadingHelper.class.getResourceAsStream(propertyResource);

        if (is == null) {
            clProperties = null;
        } else {
            clProperties = new Properties();
            clProperties.load(is);
        }

        return clProperties;
    }

    /**
     * Creates a property name candidate for {@link #CL_PROPERTIES} using the given prefix and domain.
     *
     * @param   prefix  package prefix, usually one of {@link #PACKAGE_PREFIXES}
     * @param   domain  the domain name
     *
     * @return  a cl properties candidate name, e.g. <code>
     *          /de/cismet/cids/custom/mydomain/classloading.properties</code>
     */
    private static String buildPropertyCandidate(final String prefix, final String domain) {
        final StringBuilder candBuilder = new StringBuilder("/"); // NOI18N
        candBuilder.append(prefix.replaceAll("\\.", "/"));        // NOI18N
        if ('/' != candBuilder.charAt(candBuilder.length() - 1)) {
            candBuilder.append('/');
        }
        candBuilder.append(domain).append('/').append(CL_PROPERTIES);

        return candBuilder.toString();
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
