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
 * Utility class that is mainly used to load classes of the various special cids {@link CLASS_TYPE}s for a
 * {@link MetaClass}. The procedure to load a specific class is the following:<br>
 * <br>
 *
 * <ol>
 *   <li>From a name that was specified by a {@link System#getProperty(java.lang.String)}</li>
 *   <li>From candidate names produced by
 *     {@link #getClassNames(Sirius.server.middleware.types.MetaClass, de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE) )}
 *     for the domain of the <code>MetaClass</code></li>
 *   <li>From candidate names produced by
 *     {@link #getClassNames(Sirius.server.middleware.types.MetaClass, de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE)}
 *     for the alternative domains defined by the {@link #CL_PROP_ALT_DOMAINS} property</li>
 * </ol>
 *
 * @author   stefan
 * @author   mscholl
 * @version  1.2
 */
public class ClassloadingHelper {

    //~ Static fields/initializers ---------------------------------------------

    /**
     * A property file 'classloading.properties' that may contain configurations concerning classloading. It shall be
     * placed in a package with the following name:<br>
     * <br>
     * &nbsp;&lt;packagePrefix provided by a {@link ClassLoadingPackagePrefixProvider}&gt;.&lt;domain of the
     * {@link MetaClass}&gt;<br>
     * <br>
     * Example:<br>
     * &nbsp; <code>DefaultClassLoadingPackagePrefixProvider</code> -&gt; prefix= <code>de.cismet.cids.custom</code><br>
     * &nbsp; <code>MetaClass</code> -&gt; domain= <code>MY_DOMAIN</code><br>
     * <br>
     * &nbsp;property file expected here -&gt; <code>de.cismet.cids.custom.my_domain.classloading.properties</code>
     *
     * @see  ClassLoadingPackagePrefixProvider
     * @see  StringUtils#toPackage(java.lang.String)
     */
    public static final String CL_PROPERTIES = "classloading.properties";                                        // NOI18N
    /**
     * The property 'classloading.alternativeDomains' which can be used to specify alternative classloading domains. Its
     * value is expected to be csv. The property must be defined in a {@link #CL_PROPERTIES} file if it shall be used by
     * the <code>ClassloadingHelper</code> to create candidate names.
     */
    public static final String CL_PROP_ALT_DOMAINS = "classloading.alternativeDomains";     // NOI18N
    /**
     * The property 'classloading.domainClassTypeOrder' which can be used to specify the desired order during candidate
     * class name build. Valid values are:<br>
     * <br>
     *
     * <ul>
     *   <li><i>default</i> (classtype only) )</li>
     *   <li><i>classtype</i> (classtype first only, <b>default</b>)</li>
     *   <li><i>domain</i> (domain first only)</li>
     *   <li><i>both-classtype</i> (both, classtype first)</li>
     *   <li><i>both-domain</i> (both, domain first)</li>
     * </ul>
     * <br>
     * <br>
     * The property must be defined in a {@link #CL_PROPERTIES} file if it shall be used by the <code>
     * ClassloadingHelper</code> to create candidate names.
     */
    public static final String CL_PROP_DOM_CTYPE_ORDER = "classloading.domainClassTypeOrder";   // NOI18N

    private static final transient Logger LOG;
    private static final List<String> PACKAGE_PREFIXES;
    // domain to alternative domains
    private static final transient PurgingCache<String, List<String>> ALT_DOMAIN_CACHE;
    // domain to order directive
    private static final transient PurgingCache<String, DOM_CTYPE_ORDER> DOM_CTYPE_ORDER_CACHE;
    // MC_CT_Struct to class
    private static final transient PurgingCache<MC_CT_Struct, Class<?>> CLASS_CACHE;

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

        CLASS_CACHE = new PurgingCache<MC_CT_Struct, Class<?>>(new Calculator<MC_CT_Struct, Class<?>>() {

                    @Override
                    public Class<?> calculate(final MC_CT_Struct input) throws Exception {
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("calculating class for key: " + input.toString()); // NOI18N
                        }

                        final List<String> classNames = getClassNames(input.metaClass, input.classType);

                        return loadClassFromCandidates(classNames);
                    }
                },
                (60 * 60 * 1000),
                (20 * 60 * 1000),
                true);
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
     * Class types of the extension points supported by the cids system.
     *
     * @version  1.2
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
     * Utility class. Shall never be used.
     *
     * @throws  AssertionError  if used
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
     * @param       metaClass  DOCUMENT ME!
     * @param       mai        DOCUMENT ME!
     * @param       classType  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  support will most likely be dropped
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
     * Capitalises a given string. The result of will be an all lowercase string except for an uppercase first letter,
     * e.g. fOo_BAR -&gt; Foo_bar.
     *
     * @param   toCapitalize  the string to capitalise
     *
     * @return  an all lowercase string except for an uppercase first letter
     */
    public static String capitalize(final String toCapitalize) {
        final StringBuilder result = new StringBuilder(toCapitalize.length());
        result.append(toCapitalize.substring(0, 1).toUpperCase()).append(toCapitalize.substring(1).toLowerCase());

        return result.toString();
    }

    /**
     * Produces candidate class names for a given {@link MetaClass} and {@link CLASS_TYPE}. It first produces candidate
     * class names from the domain of the <code>MetaClass</code> using
     * {@link #getClassNames(Sirius.server.middleware.types.MetaClass, de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE, java.lang.String, de.cismet.cids.utils.ClassloadingHelper.DOM_CTYPE_ORDER) }
     * with the order defined by {@link #CL_PROP_DOM_CTYPE_ORDER}. After that it iterates through the alternative
     * domains defined by {@link #CL_PROP_ALT_DOMAINS} (if any).
     *
     * @param   metaClass  the <code>MetaClass</code> to create candidate class names for
     * @param   classType  the <code>CLASS_TYPE</code> to create candidate class names for
     *
     * @return  a list of candidate class names
     *
     * @see     #getClassNames(Sirius.server.middleware.types.MetaClass,de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE,
     *          java.lang.String, de.cismet.cids.utils.ClassloadingHelper.DOM_CTYPE_ORDER)
     */
    public static List<String> getClassNames(final MetaClass metaClass, final CLASS_TYPE classType) {
        final List<String> result = new ArrayList<String>();

        final String mcDomain = metaClass.getDomain().toLowerCase();
        final String domain = StringUtils.toPackage(mcDomain);

        final DOM_CTYPE_ORDER order = DOM_CTYPE_ORDER_CACHE.get(domain);

        result.addAll(getClassNames(metaClass, classType, domain, order));

        final List<String> altDomains = ALT_DOMAIN_CACHE.get(domain);

        for (final String altDomain : altDomains) {
            result.addAll(getClassNames(metaClass, classType, altDomain, order));
        }

        return result;
    }

    /**
     * Produces candidate names for a given <code>MetaClass</code>, <code>CLASS_TYPE</code>, domain and order. The
     * procedure to create candidate names is the following:<br>
     * <br>
     *
     * <ol>
     *   <li>Use name specified by a {@link System#getProperty(java.lang.String)}</li>
     *   <li>Iterate through the available package prefixes and create candidate names using
     *     {@link #buildCandidateNames(java.lang.String, java.lang.String, java.lang.String, de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE, de.cismet.cids.utils.ClassloadingHelper.DOM_CTYPE_ORDER) }
     *   </li>
     *   <li>Use
     *     {@link #getClassNameByConfiguration(Sirius.server.middleware.types.MetaClass, de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE)}
     *   </li>
     * </ol>
     * <br>
     * The system property that can be used to specify a candidate name is build with the following pattern:<br>
     * <br>
     * &nbsp;&lt;domain&gt;.&lt;tableName&gt;.&lt;{@link CLASS_TYPE#overrideProperty}&gt;<br>
     * <br>
     * NOTE: The domain name is converted to a proper package name first using
     * {@link StringUtils#toPackage(java.lang.String) }.<br>
     * <br>
     * <b>IMPORTANT:</b> Currently the order does not properly follow the 'convention over configuration' rules!
     *
     * @param   metaClass  the <code>MetaClass</code> to create candidate class names for
     * @param   classType  the <code>CLASS_TYPE</code> to create candidate class names for
     * @param   forDomain  the domain to create candidate class names for
     * @param   order      the order that shall be applied
     *
     * @return  a list of candidate class names
     *
     * @see     ClassLoadingPackagePrefixProvider
     * @see     StringUtils#toPackage(java.lang.String)
     */
    @SuppressWarnings("fallthrough")
    public static List<String> getClassNames(final MetaClass metaClass,
            final CLASS_TYPE classType,
            final String forDomain,
            final DOM_CTYPE_ORDER order) {
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
                switch (order) {
                    case DOMAIN: {
                        result.addAll(buildCandidateNames(
                                masterPrefix,
                                domain,
                                tableName,
                                classType,
                                DOM_CTYPE_ORDER.DOMAIN));

                        break;
                    }
                    case BOTH_CLASSTYPE: {
                        result.addAll(buildCandidateNames(
                                masterPrefix,
                                domain,
                                tableName,
                                classType,
                                DOM_CTYPE_ORDER.CLASSTYPE));
                        result.addAll(buildCandidateNames(
                                masterPrefix,
                                domain,
                                tableName,
                                classType,
                                DOM_CTYPE_ORDER.DOMAIN));

                        break;
                    }
                    case BOTH_DOMAIN: {
                        result.addAll(buildCandidateNames(
                                masterPrefix,
                                domain,
                                tableName,
                                classType,
                                DOM_CTYPE_ORDER.DOMAIN));
                        result.addAll(buildCandidateNames(
                                masterPrefix,
                                domain,
                                tableName,
                                classType,
                                DOM_CTYPE_ORDER.CLASSTYPE));

                        break;
                    }
                    case CLASSTYPE: {
                    } // fall-through, classtype is default
                    case DEFAULT: {
                    } // fall-through
                    default: {
                        result.addAll(buildCandidateNames(
                                masterPrefix,
                                domain,
                                tableName,
                                classType,
                                DOM_CTYPE_ORDER.CLASSTYPE));

                        break;
                    }
                }
            }
            // FIXME: mscholl: convention over configuration but this means that convention has precedence
            // adapt javadoc accordingly when changed
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
     * Builds the candidate class names using the given parameters. The candidate class names are build with the
     * following pattern:<br>
     * <br>
     * If order is {@link DOM_CTYPE_ORDER#CLASSTYPE} or {@link DOM_CTYPE_ORDER#DEFAULT}:<br>
     * &nbsp;<i>&lt;masterPrefix&gt;.&lt;{@link CLASS_TYPE#packagePrefix}&gt;.&lt;domain&gt;.&lt;
     * {@link #capitalize(java.lang.String) } tablename{@link CLASS_TYPE#classNameSuffix}&gt;</i><br>
     * &nbsp;<i>&lt;masterPrefix&gt;.&lt;{@link CLASS_TYPE#packagePrefix}&gt;.&lt;domain&gt;.&lt;
     * {@link #camelize(java.lang.String) } tablename{@link CLASS_TYPE#classNameSuffix}&gt;</i><br>
     * <br>
     * If order is {@link DOM_CTYPE_ORDER#DOMAIN}:<br>
     * &nbsp;<i>&lt;masterPrefix&gt;.&lt;domain&gt;.&lt;{@link CLASS_TYPE#packagePrefix}&gt;.&lt;
     * {@link #capitalize(java.lang.String) } tablename{@link CLASS_TYPE#classNameSuffix}&gt;</i><br>
     * &nbsp;<i>&lt;masterPrefix&gt;.&lt;domain&gt;.&lt;{@link CLASS_TYPE#packagePrefix}&gt;.&lt;
     * {@link #camelize(java.lang.String) } tablename{@link CLASS_TYPE#classNameSuffix}&gt;</i><br>
     * <br>
     * <b>NOTE:</b>Only <code>CLASSTYPE</code>, <code>DEFAULT</code> and <code>DOMAIN</code> order directive is
     * supported. Any other value will result in an {@link IllegalArgumentException}<br>
     * <br>
     * Example:<br>
     * &nbsp;<code>buildCandidateNames("my.master.prefix", "myDomain", "myTable", CLASS_TYPE.EDITOR,
     * DOM_CTYPE_ORDER.DOMAIN)</code> will result in<br>
     * <br>
     * &nbsp;<i>"my.master.prefix.myDomain.objecteditors.MytableEditor</i> and<br>
     * &nbsp;<i>"my.master.prefix.myDomain.objecteditors.MyTableEditor</i>
     *
     * @param   masterPrefix  the master prefix of the candidate class names
     * @param   domain        the domain name for which the candidate class names shall be built
     * @param   tableName     the name of the table for which the candidate class names shall be built
     * @param   classType     the classType for which the candidate names shall be built
     * @param   order         the order directive to apply
     *
     * @return  a list containing exactly two candidate class names
     *
     * @throws  IllegalArgumentException  if the given order parameter is not supported
     */
    @SuppressWarnings("fallthrough")
    private static List<String> buildCandidateNames(final String masterPrefix,
            final String domain,
            final String tableName,
            final CLASS_TYPE classType,
            final DOM_CTYPE_ORDER order) {
        final List<String> result = new ArrayList<String>(2);
        final StringBuilder plainClassNameBuilder = new StringBuilder(masterPrefix);
        plainClassNameBuilder.append('.');

        switch (order) {
            case DOMAIN: {
                plainClassNameBuilder.append(domain);
                plainClassNameBuilder.append('.');
                plainClassNameBuilder.append(classType.packagePrefix);

                break;
            }
            case CLASSTYPE: {
            } // fall-through, classtype is default
            case DEFAULT: {
                plainClassNameBuilder.append(classType.packagePrefix);
                plainClassNameBuilder.append('.');
                plainClassNameBuilder.append(domain);

                break;
            }
            default: {
                throw new IllegalArgumentException("domain-classtype order directive not supported: " + order); // NOI18N
            }
        }

        plainClassNameBuilder.append('.');
        final StringBuilder camelCaseClassNameBuilder = new StringBuilder(plainClassNameBuilder);

        plainClassNameBuilder.append(capitalize(tableName)).append(classType.classNameSuffix);
        camelCaseClassNameBuilder.append(camelize(tableName)).append(classType.classNameSuffix);

        result.add(plainClassNameBuilder.toString());
        result.add(camelCaseClassNameBuilder.toString());

        return result;
    }

    /**
     * Loads the candidate class name from configuration according to the cids extension points.
     *
     * @param   metaClass  the <code>MetaClass</code> to get the configuration for
     * @param   classType  the specific <code>CLASS_TYPE</code> to get the configuration for
     *
     * @return  the configured class name (which can be <code>null</code>) or <code>null</code> if the given classType
     *          is not recognised
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
     * Loads the candidate class name from configuration according to the cids extension points.
     *
     * @param   mai        the <code>MemberAttributeInfo</code> to get the configuration for
     * @param   classType  the specific <code>CLASS_TYPE</code> to get the configuration for
     *
     * @return  the configured class name (which can be <code>null</code>) or <code>null</code> if the given classType
     *          is not recognised
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
     * @param       metaClass  DOCUMENT ME!
     * @param       mai        DOCUMENT ME!
     * @param       classType  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  support will most likely be dropped
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
     * Returns the value of a specific class attribute.
     *
     * @param   name  the class attribute whose value shall be fetched
     * @param   mc    the <code>MetaClass</code> where the class attribute value shall be looked for
     *
     * @return  the value of the class attribute or <code>null</code> if no such attribute was defined
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
     * Camelises a given string. It ignores all non-letters and non-digits. However, the very first character and every
     * character following an ignored one will be uppercase. The rest will be lowercase, e.g. fOo_bAr -&gt; FooBar
     *
     * @param   toCamelize  the string to camelise
     *
     * @return  the camelised string
     */
    public static String camelize(final String toCamelize) {
        boolean upperCase = true;
        final char[] result = new char[toCamelize.length()];
        int resultPosition = 0;
        for (int i = 0; i < toCamelize.length(); ++i) {
            char current = toCamelize.charAt(i);
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
     * Loads a <code>Class</code> from candidate names. The result of the first successful try will be returned.
     *
     * @param   candidateClassNames  a list of class names that shall be tried
     *
     * @return  a fully initialised <code>Class</code> object of the first successful try or <code>null</code> if none
     *          of the candidate names could be used to load the class
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
     * @param       metaClass  DOCUMENT ME!
     * @param       mai        DOCUMENT ME!
     * @param       classType  DOCUMENT ME!
     *
     * @return      DOCUMENT ME!
     *
     * @deprecated  support will most likely be dropped
     */
    public static Class<?> getDynamicClass(final MetaClass metaClass,
            final MemberAttributeInfo mai,
            final CLASS_TYPE classType) {
        final List<String> classNames = getClassNames(metaClass, mai, classType);

        return loadClassFromCandidates(classNames);
    }

    /**
     * Loads a <code>Class</code> object for the given <code>MetaClass</code> of the given <code>CLASS_TYPE</code> by
     * first receiving a list of candidate names from
     * {@link #getClassNames(Sirius.server.middleware.types.MetaClass, de.cismet.cids.utils.ClassloadingHelper.CLASS_TYPE)}
     * and then using {@link #loadClassFromCandidates(java.util.List)}.
     *
     * @param   metaClass  the <code>MetaClass</code> to load a <code>Class</code> object of the given <code>
     *                     CLASS_TYPE</code> for
     * @param   classType  the requested <code>CLASS_TYPE</code> to load
     *
     * @return  a fully initialsed <code>Class</code> of requested type or <code>null</code> if no class of the given
     *          type for the given <code>MetaClass</code> could be found
     *
     * @throws  IllegalArgumentException  if metaClass or classType is <code>null</code>
     */
    public static Class<?> getDynamicClass(final MetaClass metaClass, final CLASS_TYPE classType) {
        if ((metaClass == null) || (classType == null)) {
            throw new IllegalArgumentException("neither metaClass nor classType may be null: [metaClass=" // NOI18N
                        + metaClass
                        + "|classType=" + classType + "]");                // NOI18N
        }

        final MC_CT_Struct key = new MC_CT_Struct();
        key.metaClass = metaClass;
        key.classType = classType;

        return CLASS_CACHE.get(key);
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * For use with {@link #CLASS_CACHE} only!
     *
     * @version  1.0
     */
    private static final class MC_CT_Struct {

        //~ Instance fields ----------------------------------------------------

        private MetaClass metaClass;
        private CLASS_TYPE classType;

        //~ Methods ------------------------------------------------------------

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof MC_CT_Struct) {
                final MC_CT_Struct mcct = (MC_CT_Struct)obj;

                // this class is for strict internal use only, omitting null checks

                return this.metaClass.getKey().equals(mcct.metaClass.getKey()) || classType.equals(mcct.classType);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            // this class is for strict internal use only, omitting null checks
            hash = (47 * hash) + this.metaClass.getKey().hashCode();
            hash = (47 * hash) + this.classType.hashCode();

            return hash;
        }

        @Override
        public String toString() {
            return metaClass.getKey() + "@" + classType; // NOI18N
        }
    }
}
