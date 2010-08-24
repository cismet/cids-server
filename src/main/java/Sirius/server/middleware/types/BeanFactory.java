/***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.interfaces.proxy.CatalogueService;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.middleware.interfaces.proxy.SearchService;
import Sirius.server.middleware.interfaces.proxy.UserService;
import Sirius.server.newuser.User;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;

import org.openide.util.Lookup;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;

import java.security.ProtectionDomain;

import java.util.HashMap;
import java.util.List;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
import java.util.ArrayList;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class BeanFactory {

    //~ Static fields/initializers ---------------------------------------------
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(BeanFactory.class);
    public static final String CIDS_DYNAMICS_SUPERCLASS = /*CidsBean.class.toString();*/
        "de.cismet.cids.dynamics.CidsBean";   // NOI18N
    private static BeanFactory instance = null;
    //~ Instance fields --------------------------------------------------------
    private HashMap<String, Class> javaclassCache = new HashMap<String, Class>();
    private MetaClassCacheService classCacheService;

    //~ Constructors -----------------------------------------------------------
    /**
     * Creates a new BeanFactory object.
     */
    private BeanFactory() {
        classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
    }

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static BeanFactory getInstance() {
        if (instance == null) {
            instance = new BeanFactory();
        }
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
    public void changeNullSubObjectsToTemplates(final CidsBean cidsbean) {
        final MetaObject metaObject = cidsbean.getMetaObject();
        final MetaClass metaClass = metaObject.getMetaClass();
        final String domain = metaObject.getDomain();
        final ObjectAttribute[] attribs = metaObject.getAttribs();
        for (final ObjectAttribute oa : attribs) {
            if (oa.isArray()) {
            } else if (oa.referencesObject()) {
                final Object value = oa.getValue();
                if (value == null) {
                    final MetaClass foreignClass = (MetaClass) classCacheService.getAllClasses(domain).get(domain + oa.getMai().getForeignKeyClassId());
                    final MetaObject emptyInstance = foreignClass.getEmptyInstance();
                    emptyInstance.setStatus(Sirius.server.localserver.object.Object.TEMPLATE);
                } else {
                    final MetaObject subObject = (MetaObject) value;
                    changeNullSubObjectsToTemplates(subObject.getBean());
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
                final CidsBean bean = (CidsBean) javaClass.newInstance();
                final ObjectAttribute[] attribs = metaObject.getAttribs();
                for (final ObjectAttribute a : attribs) {
                    final String field = a.getMai().getFieldName().toLowerCase();
                    Object value = a.getValue();
                    a.setParentObject(metaObject);
                    if (value instanceof MetaObject) {
                        final MetaObject tmpMO = (MetaObject) value;
                        if (tmpMO.isDummy()) {
                            // 1-n Beziehung (Array)
                            final List<CidsBean> arrayElements = new ArrayList();
                            final ObservableList<CidsBean> observableArrayElements = ObservableCollections.observableList(
                                    arrayElements);
                            final ObjectAttribute[] arrayOAs = tmpMO.getAttribs();
                            for (final ObjectAttribute arrayElementOA : arrayOAs) {
                                arrayElementOA.setParentObject(tmpMO);
                                final MetaObject arrayElementMO = (MetaObject) arrayElementOA.getValue();
                                // In diesem MetaObject gibt es nun genau ein Attribut das als Value ein MetaObject hat
                                final ObjectAttribute[] arrayElementAttribs = arrayElementMO.getAttribs();
                                for (final ObjectAttribute targetArrayElement : arrayElementAttribs) {
                                    targetArrayElement.setParentObject(arrayElementMO);
                                    final Object targetArrayElementValObj = targetArrayElement.getValue();
                                    if (targetArrayElementValObj instanceof MetaObject) {
                                        final MetaObject targetMO = (MetaObject) targetArrayElementValObj;
                                        final CidsBean cdBean = targetMO.getBean();
                                        if (cdBean != null) {
                                            cdBean.setBacklinkInformation(field, bean);
                                            observableArrayElements.add(cdBean);
                                        } else {
                                            log.warn(
                                                    "getBean() delivered null -> could be a possible problem with rights/policy?");//NOI18N
                                        }
                                        break;
                                    }
                                }
                            }
                            value = observableArrayElements;
                            addObservableListListener(observableArrayElements, bean, field);
                        } else {
                            // 1-1 Beziehung
                            final CidsBean tmpMOBean = tmpMO.getBean();
                            value = tmpMOBean;
                            tmpMOBean.setBacklinkInformation(field, bean);
                        }
                    } else if ((value == null) && a.isArray()) {
                        // lege leeren Vector an, sonst wirds sp?ter zu kompliziert
                        final List<?> arrayElements = new ArrayList();
                        final ObservableList observableArrayElements = ObservableCollections.observableList(arrayElements);
                        value = observableArrayElements;
                        addObservableListListener(observableArrayElements, bean, field);
                    }
                    bean.setProperty(field, value);
                }
                // bean.addPropertyChangeListener(metaObject);
                bean.setMetaObject(metaObject);
                bean.addPropertyChangeListener(bean);
                return bean;
            } catch (Exception e) {
                log.fatal("Error in createBean", e);//NOI18N
                throw new Exception(
                        "Error in getBean() (instanceof "//NOI18N
                        + javaClass
                        + ") of MetaObject:"//NOI18N
                        + metaObject.getDebugString(),
                        e);
            }
        } else {
            log.warn("getMetaClass() delivered null -> please check policy/permissions!");//NOI18N
            return null;
        }
    }

    private static void addObservableListListener(final ObservableList<?> toObserve, final CidsBean bean, final String field) {
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
            ret = createJavaClass(metaClass);
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
        final String classname = "de.cismet.cids.dynamics."//NOI18N
                + createJavaClassnameOutOfTableName(metaClass.getTableName());
        // String beaninfoClassname=classname+"BeanInfo";

        final ClassPool pool = ClassPool.getDefault();
        final ClassLoader cl = this.getClass().getClassLoader();
        final LoaderClassPath lcp = new LoaderClassPath(cl);
        pool.appendClassPath(lcp);

        final CtClass ctClass = pool.makeClass(classname);
        // CtClass ctClassBeanInfo = pool.makeClass(beaninfoClassname);

        final CtClass superClass = pool.getCtClass(CIDS_DYNAMICS_SUPERCLASS);
//        CtClass superClassBeanInfo = pool.getCtClass("java.beans.SimpleBeanInfo");

        ctClass.setSuperclass(superClass);
        // ctClassBeanInfo.setSuperclass(superClassBeanInfo);

        // Beaninfotest
// String code="public PropertyDescriptor[] getPropertyDescriptors() {"+
// "try {"+
// "PropertyDescriptor textPD = " +
// "   new PropertyDescriptor(\"text\", beanClass); "+
// "PropertyDescriptor rv[] = {textPD}; "+
// "return rv; "+
// "} catch (IntrospectionException e) { "+
// "      throw new Error(e.toString()); "+
// "   } ";
//
// ctClassBeanInfo.addMethod(CtNewMethod.make(code, ctClassBeanInfo));

        final List<MemberAttributeInfo> mais = new ArrayList<MemberAttributeInfo>(
                metaClass.getMemberAttributeInfos().values());
        final StringBuilder propertyNames = new StringBuilder();
        for (final MemberAttributeInfo mai : mais) {
            final String fieldname = mai.getFieldName().toLowerCase();
            String attributeJavaClassName = mai.getJavaclassname();

            if (mai.isArray()) {
                attributeJavaClassName = "org.jdesktop.observablecollections.ObservableList"; // zu erstellen mit:   // NOI18N
                // ObservableCollections.observableList(list)
            } else if (mai.isForeignKey()) {
                if (attributeJavaClassName.equals("org.postgis.PGgeometry")) {   // NOI18N
                    attributeJavaClassName = "com.vividsolutions.jts.geom.Geometry";   // NOI18N
                } else {
                    attributeJavaClassName = CIDS_DYNAMICS_SUPERCLASS;
                }
            }

            try {
                addPropertyToCtClass(pool, ctClass, Class.forName(attributeJavaClassName), fieldname);
                if (propertyNames.length() > 0) {
                    propertyNames.append(", ");//NOI18N
                }
                propertyNames.append("\"").append(fieldname).append("\"");//NOI18N
            } catch (Exception e) {
                log.warn("Could not add " + fieldname, e);   // NOI18N
            }
        }
        // FIXME: immutable collection instead of possible mutable (-> corrputable) array?
        final CtField propertyNamesStaticField = CtField.make("private String[] PROPERTY_NAMES = new String[]{"//NOI18N
                + propertyNames + "};",//NOI18N
                ctClass);
        final CtMethod propertyNamesGetter = CtNewMethod.getter("getPropertyNames", propertyNamesStaticField);//NOI18N
//        CtMethod propertyNamesGetter = CtNewMethod.make(
//                "public String[] getPropertyNames() { return PROPERTY_NAMES.clone(); }",
//                ctClass);
        ctClass.addField(propertyNamesStaticField);
        ctClass.addMethod(propertyNamesGetter);
        final ProtectionDomain pd = this.getClass().getProtectionDomain();
        final Class ret = ctClass.toClass(getClass().getClassLoader(), pd);
        if(log.isInfoEnabled())
            log.info("Class " + ret + " was successfully created", new CurrentStackTrace());   // NOI18N
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pool          DOCUMENT ME!
     * @param   ctClass       DOCUMENT ME!
     * @param   propertyType  DOCUMENT ME!
     * @param   propertyName  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private static void addPropertyToCtClass(final ClassPool pool,
            final CtClass ctClass,
            final Class propertyType,
            final String propertyName) throws Exception {
        final CtField f = new CtField(pool.get(propertyType.getCanonicalName()), propertyName, ctClass);
        ctClass.addField(f);

        final String fieldname = f.getName();
        String getterPrefix = null;
        final String postfix = fieldname.toUpperCase().substring(0, 1) + fieldname.substring(1);
        if ((propertyType != boolean.class) && (propertyType != Boolean.class)) {
            getterPrefix = "get";   // NOI18N
        } else {
            // Hier wird ein zusaetzlicher "getter" angelegt
            getterPrefix = "is";   // NOI18N
            final CtMethod additionalGetter = CtNewMethod.getter(getterPrefix + postfix, f);
            ctClass.addMethod(additionalGetter);

            // leider reicht dieser "getter" nicht. beans binding braucht auch bei einem Boolean ein "getter" der mit
            // get anfaengt
            getterPrefix = "get";   // NOI18N
        }

        final String getterName = getterPrefix + postfix;
        final String setterName = "set" + postfix;//NOI18N

        final CtMethod getter = CtNewMethod.getter(getterName, f);
        final CtMethod setter = CtNewMethod.setter(setterName, f);

        setter.insertAfter(
                "propertyChangeSupport.firePropertyChange(\""//NOI18N
                + f.getName()
                + "\", null, "//NOI18N
                + f.getName()
                + ");");//NOI18N

        ctClass.addMethod(getter);
        ctClass.addMethod(setter);

        // Idee falls man oldValue benoetigt: erzeuge den setter wie oben jedoch mit einem anderen Namen (z.b::
        // stealthySetVorname) und setze den modifier auf private oder protected in dieser methode wird NICHT der
        // propertyChangesupport aufgerufen in einer zus?tzlichen Methoden setVorname die komplett impl. wird kann man
        // dann auf den noch nicht ver?nderten Wert zugreifen und oldvalue setzen diese Methode ruft dann die Metjode
        // stealthy... auf

    }

    /**
     * DOCUMENT ME!
     *
     * @param   args  DOCUMENT ME!
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public static void main(final String[] args) throws Throwable {
        Log4JQuickConfig.configure4LumbermillOnLocalhost();
        final String domain = "WUNDA_DEMO";//NOI18N

        final int AAPERSON_CLASSID = 374;

        // rmi registry lokaliseren
        final java.rmi.registry.Registry rmiRegistry = LocateRegistry.getRegistry(1099);

        // lookup des callservers
        final Remote r = (Remote) Naming.lookup("rmi://localhost/callServer");//NOI18N

        // ich weiss, dass die server von callserver implementiert werden
        final SearchService ss = (SearchService) r;
        final CatalogueService cat = (CatalogueService) r;
        final MetaService meta = (MetaService) r;
        final UserService us = (UserService) r;

        final User u = us.getUser(domain, "Demo", domain, "demo", "demo");//NOI18N

//        ClassCacheMultiple.addInstance(domain);//, meta, u); //musste auskommentiert werden wegen umstellung auf lookup. main() funzt nicht mehr

//
//        MetaObject thorsten = meta.getMetaObject(u, 1, AAPERSON_CLASSID, domain);
//        log.debug("Thorsten:" + thorsten.getDebugString());
//
////        MetaObject mo= meta.getInstance(u, thorsten.getMetaClass());
////
//
//        CidsBean bean = thorsten.getBean();
//
////        MetaObject paula=meta.getMetaObject(u, 26, 88, domain);
////        log.debug("Paula:"+paula.getDebugString());
////
////
////        DefaultObject bean=paula.getBean();
////
////        log.info(BeanUtils.describe(bean));
//
//        log.info("id=" + bean.getProperty("id"));
//        log.info("name=" + bean.getProperty("name"));
//        log.info("vorname=" + bean.getProperty("vorname"));
//
//        log.info("bild=" + bean.getProperty("bild.url"));
//        List l = (List) bean.getProperty("autos");
//
//        for (int i = 0; i < l.size(); ++i) {
//            log.info("autos[" + i + "]=" +
//                    bean.getProperty("autos[" + i + "].marke") + " (" +
//                    bean.getProperty("autos[" + i + "].kennz") + "," +
//                    bean.getProperty("autos[" + i + "].farbe.name") + ")");
//        }
//
//        //?ndern des einfachen Attributs
//        bean.setProperty("name", "Test");
//
//        //?ndern eines Attributes eines Unterobjektes
//        bean.setProperty("bild.url", "Testurl");
//        log.debug("name=" + bean.getProperty("name"));
//
//
////        //L?schen der URL
////        CidsBean urlO=(CidsBean)bean.getProprty("bild");
////        urlO.delete();
//
//        //?ndern eines Attributes eine ArrayElementes
//        //bean.setProperty("autos[0].kennz", "NK-XX-1");
//
//
//        //L?schen eines Arrayelementes
//        //((CidsBean)l.get(0)).delete();
//
//        //Hinzuf?gen eines Arrayelementes
//        CidsBean newAuto = CidsBean.constructNew(meta, u, domain, "aaauto");
//        newAuto.setProperty("kennz", "SB-CI-99");
//        newAuto.setProperty("marke", "Aston Martin V8 Vantage");
//        ((List) bean.getProperty("autos")).add(newAuto);
//
//        log.debug("vor persist:" + thorsten.getDebugString());
//
//        bean.persist(meta, u, domain);
//
//        MetaObject check = meta.getMetaObject(u, thorsten.getID(), thorsten.getClassID(), domain);
////
////
//        log.info("Check:" + check.getDebugString());

        final CidsBean stefan = CidsBean.constructNew(meta, u, domain, "aaperson");//NOI18N
        stefan.setProperty("name", "Richter");//NOI18N
        stefan.setProperty("vorname", "Stefan");//NOI18N

        final CidsBean newBild = CidsBean.constructNew(meta, u, domain, "aabild");//NOI18N

        newBild.setProperty("url", "http://www.stefan-richter.info/Unterseiten/Fotos/2005/picture-0006.jpg");   // NOI18N
        stefan.setProperty("bild", newBild);   // NOI18N

        final CidsBean newSRAuto = CidsBean.constructNew(meta, u, domain, "aaauto");//NOI18N
        newSRAuto.setProperty("marke", "VW Golf");//NOI18N
        newSRAuto.setProperty("kennz", "MZG-SR-1");//NOI18N
        ((List) stefan.getProperty("autos")).add(newSRAuto);
        if (log.isDebugEnabled()) {
            log.debug("Autos:" + stefan.getProperty("autos"));   // NOI18N
        }
        if (log.isDebugEnabled()) {
            log.debug("vor persist:" + stefan.getMOString());   // NOI18N
        }
        final CidsBean check2 = stefan.persist(meta, u, domain);
        log.info("Check:" + check2.getMOString());   // NOI18N

////
//        //check2.setAllClasses(classHash);
//        CidsBean check2Bean=check2.getBean();
//        check2Bean.delete();
//        check2Bean.persist(meta, u, domain);

        // Wunschcode

        // Neue Person anlegen

        // getInstance("aaperson");

        // Neues Bild hinzuf?gen, wenn vorher keins gesetzt war

        // Neues Auto hinzuf?gen

    }
}
