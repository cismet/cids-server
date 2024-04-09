/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.sql.DialectProvider;
import Sirius.server.sql.SQLTools;

import com.vividsolutions.jts.geom.Geometry;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.This;

import org.apache.log4j.Logger;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;

import org.openide.util.Lookup;

import java.lang.reflect.Method;

import java.math.BigDecimal;

import java.security.ProtectionDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class BeanFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(BeanFactory.class);
    public static final String CIDS_DYNAMICS_SUPERCLASS = "de.cismet.cids.dynamics.CidsBean"; // NOI18N
    private static final BeanFactory instance = new BeanFactory();
    public static String[] PROP_NAMES = new String[0];

    //~ Instance fields --------------------------------------------------------

    private final Map<String, Class> javaclassCache;
    private MetaClassCacheService classCacheService;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BeanFactory object.
     */
    private BeanFactory() {
        javaclassCache = new HashMap<String, Class>();
        classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static BeanFactory getInstance() {
        return instance;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ol  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String createObservableListHash(final ObservableList ol) {
        long l = 0;
        for (final Object o : ol) {
            l += o.hashCode();
        }

        return Long.toHexString(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsbean  DOCUMENT ME!
     */
    @Deprecated
    public void changeNullSubObjectsToTemplates(final CidsBean cidsbean) {
        changeNullSubObjectsToTemplates(cidsbean, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsbean           DOCUMENT ME!
     * @param  connectionContext  DOCUMENT ME!
     */
    public void changeNullSubObjectsToTemplates(final CidsBean cidsbean, final ConnectionContext connectionContext) {
        final MetaObject metaObject = cidsbean.getMetaObject();
        final MetaClass metaClass = metaObject.getMetaClass();
        final String domain = metaObject.getDomain();
        final ObjectAttribute[] attribs = metaObject.getAttribs();
        for (final ObjectAttribute oa : attribs) {
            if (oa.isArray()) {
            } else if (oa.referencesObject()) {
                final Object value = oa.getValue();
                if (value == null) {
                    final MetaClass foreignClass = (MetaClass)classCacheService.getAllClasses(domain, connectionContext)
                                .get(domain + oa.getMai().getForeignKeyClassId());
                    final MetaObject emptyInstance = foreignClass.getEmptyInstance(connectionContext);
                    emptyInstance.setStatus(Sirius.server.localserver.object.Object.TEMPLATE);
                } else {
                    final MetaObject subObject = (MetaObject)value;
                    changeNullSubObjectsToTemplates(subObject.getBean(), connectionContext);
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsBean createBean(final MetaObject metaObject) throws Exception {
        // TODO getmetaClass kann null liefern wenn keine Rechte vorhanden sind
        final MetaClass mc = metaObject.getMetaClass();
        if (mc != null) {
            final Class<?> javaClass = mc.getJavaClass();
            try {
                final CidsBean bean = (CidsBean)javaClass.newInstance();
                final ObjectAttribute[] attribs = metaObject.getAttribs();
                for (final ObjectAttribute a : attribs) {
                    if (!a.getMai().isExtensionAttribute()) {
                        final String field = a.getMai().getFieldName().toLowerCase();
                        Object value = a.getValue();
                        // a.setParentObject(metaObject); // disabled -> fixed in #172
                        if (value instanceof MetaObject) {
                            final MetaObject tmpMO = (MetaObject)value;
                            if (tmpMO.isDummy()) {
                                final List<CidsBean> arrayElements = new ArrayList();
                                final ObservableList<CidsBean> observableArrayElements = ObservableCollections
                                            .observableList(
                                                arrayElements);
                                final ObjectAttribute[] arrayOAs = tmpMO.getAttribs();

                                // 1-n Beziehung (Array)
                                if (a.getMai().isVirtual() && a.getMai().isForeignKey()
                                            && (a.getMai().getForeignKeyClassId() < 0)) {
                                    for (final ObjectAttribute arrayElementOA : arrayOAs) {
                                        // arrayElementOA.setParentObject(tmpMO);  // disabled -> fixed in #172
                                        final MetaObject arrayElementMO = (MetaObject)arrayElementOA.getValue();
                                        final CidsBean cdBean = arrayElementMO.getBean();
                                        if (cdBean != null) {
                                            cdBean.setBacklinkInformation(field, bean);
                                            observableArrayElements.add(cdBean);
                                        } else {
                                            LOG.warn(String.format(
                                                    "getBean() delivered null -> could be a possible problem with rights/policy? ClassId: %d, ObjectId: %d",
                                                    arrayElementMO.getClassID(),
                                                    arrayElementMO.getId()));
                                        }
                                    }
                                    value = observableArrayElements;
                                    addObservableListListener(observableArrayElements, bean, field);
                                } else {
                                    // n-m Beziehung (Array)
                                    for (final ObjectAttribute arrayElementOA : arrayOAs) {
                                        // arrayElementOA.setParentObject(tmpMO);  // disabled -> fixed in #172
                                        final MetaObject arrayElementMO = (MetaObject)arrayElementOA.getValue();
                                        // In diesem MetaObject gibt es nun genau ein Attribut das als Value ein
                                        // MetaObject hat
                                        final ObjectAttribute[] arrayElementAttribs = arrayElementMO.getAttribs();
                                        for (final ObjectAttribute targetArrayElement : arrayElementAttribs) {
                                            // targetArrayElement.setParentObject(arrayElementMO);  // disabled ->
                                            // fixed in #172
                                            final Object targetArrayElementValObj = targetArrayElement.getValue();
                                            if (targetArrayElementValObj instanceof MetaObject) {
                                                final MetaObject targetMO = (MetaObject)targetArrayElementValObj;
                                                final CidsBean cdBean = targetMO.getBean();
                                                if (cdBean != null) {
                                                    cdBean.setBacklinkInformation(field, bean);
                                                    observableArrayElements.add(cdBean);
                                                } else {
                                                    LOG.warn(String.format(
                                                            "getBean() delivered null -> could be a possible problem with rights/policy? ClassId: %d, ObjectId: %d",
                                                            targetMO.getClassID(),
                                                            targetMO.getId()));
                                                }
                                                break;
                                            }
                                        }
                                    }
                                    value = observableArrayElements;
                                    addObservableListListener(observableArrayElements, bean, field);
                                }
                            } else {
                                // 1-1 Beziehung
                                final CidsBean tmpMOBean = tmpMO.getBean();
                                value = tmpMOBean;
                                tmpMOBean.setBacklinkInformation(field, bean);
                            }
                        } else if ((value == null) && (a.isArray() || a.isVirtualOneToManyAttribute())) {
                            // lege leeren Vector an, sonst wirds sp?ter zu kompliziert
                            final List<?> arrayElements = new ArrayList();
                            final ObservableList observableArrayElements = ObservableCollections.observableList(
                                    arrayElements);
                            value = observableArrayElements;
                            addObservableListListener(observableArrayElements, bean, field);
                        }

                        if (mc.getPrimaryKey().equalsIgnoreCase(field) && (value instanceof BigDecimal)) {
                            // FIXME: this is probably not what we want
                            value = ((BigDecimal)value).intValue();
                        }
                        bean.setProperty(field, value);
                    }
                }
                // bean.addPropertyChangeListener(metaObject);
                bean.setMetaObject(metaObject);
                bean.addPropertyChangeListener(bean);
                return bean;
            } catch (Exception e) {
                LOG.fatal("Error in createBean metaclass:" + mc, e); // NOI18N
                throw new Exception(
                    "Error in getBean() (instanceof "                // NOI18N
                            + javaClass
                            + ") of MetaObject:"                     // NOI18N
                            + metaObject.getDebugString(),
                    e);
            }
        } else {
            LOG.warn(String.format(
                    "getMetaClass() delivered null -> please check policy/permissions of the class with the id %d !",
                    metaObject.getClassID()));                       // NOI18N
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  toObserve  DOCUMENT ME!
     * @param  bean       DOCUMENT ME!
     * @param  field      DOCUMENT ME!
     */
    private static void addObservableListListener(final ObservableList<?> toObserve,
            final CidsBean bean,
            final String field) {
        toObserve.addObservableListListener(
            new ObservableListListener() {

                @Override
                public void listElementsAdded(final ObservableList list,
                        final int index,
                        final int length) {
                    bean.listElementsAdded(field, list, index, length);
                }

                @Override
                public void listElementsRemoved(final ObservableList list,
                        final int index,
                        final List oldElements) {
                    bean.listElementsRemoved(field, list, index, oldElements);
                }

                @Override
                public void listElementReplaced(final ObservableList list,
                        final int index,
                        final Object oldElement) {
                    bean.listElementReplaced(field, list, index, oldElement);
                }

                @Override
                public void listElementPropertyChanged(final ObservableList list, final int index) {
                    bean.listElementPropertyChanged(field, list, index);
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String createJavaClassnameOutOfTableName(final String tableName) {
        final String lowerTableName = tableName.toLowerCase();
        return tableName.substring(0, 1) + lowerTableName.substring(1);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public synchronized Class getJavaClass(final MetaClass metaClass) throws Exception {
        final String classname = createJavaClassnameOutOfTableName(metaClass.getTableName());
        Class ret = javaclassCache.get(classname);
        if (ret == null) {
            try {
                ret = createJavaClass(metaClass);
            } catch (final Exception exception) {
                LOG.error("fatal error in creating javaclass", exception);
            }
            javaclassCache.put(classname, ret); // K?nnte null sein
        }
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaClass  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private Class createJavaClass(final MetaClass metaClass) throws Exception {
        final String classname = "de.cismet.cids.dynamics." // NOI18N
                    + createJavaClassnameOutOfTableName(metaClass.getTableName());
        final ClassLoader cl = this.getClass().getClassLoader();
        DynamicType.Builder builder = new ByteBuddy().subclass(CidsBean.class).name(classname);

        final List<MemberAttributeInfo> mais = new ArrayList<MemberAttributeInfo>(
                metaClass.getMemberAttributeInfos().values());
        final StringBuilder propertyNames = new StringBuilder();
        for (final MemberAttributeInfo mai : mais) {
            final String fieldname = mai.getFieldName().toLowerCase();
            String attributeJavaClassName = mai.getJavaclassname();

            // FIXME: some drivers (such as oracle) map any number to BigDecimal
            // at least for the primary key field we assume integer
            if (mai.getFieldName().equalsIgnoreCase(metaClass.getPrimaryKey())) {
                attributeJavaClassName = Integer.class.getName();
            } else if (mai.isArray() || (mai.isVirtual() && (mai.getForeignKeyClassId() < 0))) {
                attributeJavaClassName = "org.jdesktop.observablecollections.ObservableList"; // NOI18N
            } else if (mai.isForeignKey()) {
                if (SQLTools.getGeometryFactory(Lookup.getDefault().lookup(DialectProvider.class).getDialect())
                            .isGeometryColumn(attributeJavaClassName)) {                      // NOI18N
                    attributeJavaClassName = Geometry.class.getName();
                } else {
                    attributeJavaClassName = CIDS_DYNAMICS_SUPERCLASS;
                }
            }

            try {
                builder = addPropertyToClass(builder, Class.forName(attributeJavaClassName), fieldname);
                if (propertyNames.length() > 0) {
                    propertyNames.append(", ");                            // NOI18N
                }
                propertyNames.append("\"").append(fieldname).append("\""); // NOI18N
            } catch (final Exception e) {
                LOG.warn("Could not add " + fieldname, e);                 // NOI18N
            }
        }
        // FIXME: immutable collection instead of possible mutable (-> corrputable) array?
        // empty array initialiser causes compile error (new Object[] {})

        if (propertyNames.length() == 0) {
            builder = builder.defineField("PROPERTY_NAMES", String[].class, Visibility.PRIVATE);
        } else {
            builder = builder.defineField("PROPERTY_NAMES", String[].class, Visibility.PRIVATE);
        }
        builder = builder.defineMethod("getPropertyNames", String[].class, Visibility.PUBLIC)
                    .intercept(FixedValue.value(StringArrayFromStringBuilder(propertyNames)));
        final ProtectionDomain pd = this.getClass().getProtectionDomain();

        final Class ret = builder.make().load(cl).getLoaded();
        if (LOG.isInfoEnabled()) {
            LOG.info("Class " + ret + " was successfully created"); // NOI18N
        }

        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sb  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String[] StringArrayFromStringBuilder(final StringBuilder sb) {
        final List<String> l = new ArrayList<>();
        final StringTokenizer st = new StringTokenizer(sb.toString(), ",");

        while (st.hasMoreTokens()) {
            String tmp = st.nextToken();

            tmp = tmp.trim();

            l.add(tmp.substring(1, tmp.length() - 1));
        }

        return l.toArray(new String[l.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   builder       pool DOCUMENT ME!
     * @param   propertyType  DOCUMENT ME!
     * @param   propertyName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static DynamicType.Builder addPropertyToClass(DynamicType.Builder builder,
            final Class propertyType,
            final String propertyName) throws Exception {
        builder = builder.defineField(propertyName, propertyType, Visibility.PRIVATE);

        final String fieldname = propertyName;
        String getterPrefix = null;
        final String postfix = fieldname.toUpperCase().substring(0, 1) + fieldname.substring(1);
        if ((propertyType != boolean.class) && (propertyType != Boolean.class)) {
            getterPrefix = "get"; // NOI18N
        } else {
            // Hier wird ein zusaetzlicher "getter" angelegt
            getterPrefix = "is"; // NOI18N

            builder = builder.defineMethod(getterPrefix + postfix, propertyType, Visibility.PUBLIC)
                        .intercept(FieldAccessor.ofField(propertyName));

            // leider reicht dieser "getter" nicht. beans binding braucht auch bei einem Boolean ein "getter" der mit
            // get anfaengt
            getterPrefix = "get"; // NOI18N
        }

        final String getterName = getterPrefix + postfix;
        final String setterName = "set" + postfix; // NOI18N

        builder = builder.defineMethod(getterName, propertyType, Visibility.PUBLIC)
                    .intercept(FieldAccessor.ofField(propertyName));
        // the method with the 2 underscores is a workaround, bacause it was not possible for me to set the variable
        // and invoke the firePropertyChanged method
        builder = builder.defineMethod("__" + setterName, Void.TYPE, Visibility.PUBLIC).withParameter(propertyType)
                    .intercept(FieldAccessor.ofField(propertyName));
        builder = builder.defineMethod(setterName, Void.TYPE, Visibility.PUBLIC).withParameter(propertyType)
                    .intercept(MethodDelegation.to(Interceptor.class));

        // Idee falls man oldValue benoetigt: erzeuge den setter wie oben jedoch mit einem anderen Namen (z.b::
        // stealthySetVorname) und setze den modifier auf private oder protected in dieser methode wird NICHT der
        // propertyChangesupport aufgerufen in einer zus?tzlichen Methoden setVorname die komplett impl. wird kann man
        // dann auf den noch nicht ver?nderten Wert zugreifen und oldvalue setzen diese Methode ruft dann die Metjode
        // stealthy... auf

        return builder;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public static class Interceptor {

        //~ Methods ------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @param  value     DOCUMENT ME!
         * @param  method    DOCUMENT ME!
         * @param  original  DOCUMENT ME!
         */
        @Advice.OnMethodEnter
        public static void intercept(final Object value, @Origin final Method method, @This final CidsBean original) {
            try {
                original.getClass()
                        .getMethod("__" + method.getName(),
                                new Class[] { Class.forName(method.getGenericParameterTypes()[0].getTypeName()) })
                        .invoke(original, new Object[] { value });

                final String fieldName = method.getName().substring(3, 4).toLowerCase() + method.getName()
                            .substring(4);
                original.firePropertyChanged(fieldName, null, value);
            } catch (Exception e) {
                LOG.error("Error before fire property change", e);
            }
        }
    }
}
