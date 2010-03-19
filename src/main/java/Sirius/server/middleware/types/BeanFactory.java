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

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.tools.CurrentStackTrace;

import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;

import java.security.ProtectionDomain;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

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
        "de.cismet.cids.dynamics.CidsBean";
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
    public static String createObservableListHash(ObservableList ol) {
        long l = 0;
        for (Object o : ol) {
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
        MetaObject metaObject = cidsbean.getMetaObject();
        MetaClass metaClass = metaObject.getMetaClass();
        String domain = metaObject.getDomain();
        ObjectAttribute[] attribs = metaObject.getAttribs();
        for (ObjectAttribute oa : attribs) {
            if (oa.isArray()) {
            } else if (oa.referencesObject()) {
                Object value = oa.getValue();
                if (value == null) {
                    MetaClass foreignClass = (MetaClass)classCacheService.getAllClasses(domain)
                                .get(domain + oa.getMai().getForeignKeyClassId());
                    MetaObject emptyInstance = foreignClass.getEmptyInstance();
                    emptyInstance.setStatus(Sirius.server.localserver.object.Object.TEMPLATE);
                } else {
                    MetaObject subObject = (MetaObject)value;
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
        Class javaClass = null;
        try {
            // TODO getmetaClass kann null liefern wenn keine Rechte vorhanden sind
            javaClass = metaObject.getMetaClass().getJavaClass();

            final CidsBean bean = (CidsBean)javaClass.newInstance();

            HashMap values = new HashMap();
            ObjectAttribute[] attribs = metaObject.getAttribs();
            for (ObjectAttribute a : attribs) {
                final String field = a.getMai().getFieldName().toLowerCase();
                Object value = a.getValue();
                a.setParentObject(metaObject);
                if ((value != null) && (value instanceof MetaObject)) {
                    MetaObject tmpMO = ((MetaObject)value);
                    if (tmpMO.isDummy()) {
                        // 1-n Beziehung (Array)
                        Vector arrayElements = new Vector();
                        ObservableList observableArrayElements = ObservableCollections.observableList(arrayElements);
                        ObjectAttribute[] arrayOAs = tmpMO.getAttribs();
                        for (ObjectAttribute arrayElementOA : arrayOAs) {
                            arrayElementOA.setParentObject(tmpMO);
                            MetaObject arrayElementMO = (MetaObject)arrayElementOA.getValue();
                            // In diesem MetaObject gibt es nun genau ein Attribut das als Value ein MetaObject hat
                            ObjectAttribute[] arrayElementAttribs = arrayElementMO.getAttribs();
                            for (ObjectAttribute targetArrayElement : arrayElementAttribs) {
                                targetArrayElement.setParentObject(arrayElementMO);

                                if (targetArrayElement.getValue() instanceof MetaObject) {
                                    MetaObject targetMO = (MetaObject)targetArrayElement.getValue();
                                    CidsBean cdBean = targetMO.getBean();
                                    cdBean.setBacklinkInformation(field, bean);
                                    observableArrayElements.add(cdBean);
                                    break;
                                }
                            }
                        }
                        value = observableArrayElements;

                        observableArrayElements.addObservableListListener(
                            new ObservableListListener() {

                                public void listElementsAdded(ObservableList list, int index, int length) {
                                    bean.listElementsAdded(field, list, index, length);
                                }

                                public void listElementsRemoved(ObservableList list, int index, List oldElements) {
                                    bean.listElementsRemoved(field, list, index, oldElements);
                                }

                                public void listElementReplaced(ObservableList list, int index, Object oldElement) {
                                    bean.listElementReplaced(field, list, index, oldElement);
                                }

                                public void listElementPropertyChanged(ObservableList list, int index) {
                                    bean.listElementPropertyChanged(field, list, index);
                                }
                            });
                    } else {
                        // 1-1 Beziehung
                        value = tmpMO.getBean();
                        ((CidsBean)value).setBacklinkInformation(field, bean);
                    }
                } else if ((value == null) && a.isArray()) {
                    // lege leeren Vector an, sonst wirds sp?ter zu kompliziert
                    Vector arrayElements = new Vector();
                    ObservableList observableArrayElements = ObservableCollections.observableList(arrayElements);
                    value = observableArrayElements;
                    observableArrayElements.addObservableListListener(
                        new ObservableListListener() {

                            public void listElementsAdded(ObservableList list, int index, int length) {
                                bean.listElementsAdded(field, list, index, length);
                            }

                            public void listElementsRemoved(ObservableList list, int index, List oldElements) {
                                bean.listElementsRemoved(field, list, index, oldElements);
                            }

                            public void listElementReplaced(ObservableList list, int index, Object oldElement) {
                                bean.listElementReplaced(field, list, index, oldElement);
                            }

                            public void listElementPropertyChanged(ObservableList list, int index) {
                                bean.listElementPropertyChanged(field, list, index);
                            }
                        });
                }

                values.put(field, value);
                bean.setProperty(field, value);
            }
            // bean.addPropertyChangeListener(metaObject);
            bean.setMetaObject(metaObject);
            bean.addPropertyChangeListener(bean);
            return bean;
        } catch (Exception e) {
            log.fatal("Error in createBean", e);
            throw new Exception(
                "Error in getBean() (instanceof " + javaClass + ") of MetaObject:" + metaObject.getDebugString(),
                e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String createJavaClassnameOutOfTableName(String tableName) {
        String lowerTableName = tableName.toLowerCase();
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
        String classname = createJavaClassnameOutOfTableName(metaClass.getTableName());
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
        String classname = "de.cismet.cids.dynamics." + createJavaClassnameOutOfTableName(metaClass.getTableName());
        // String beaninfoClassname=classname+"BeanInfo";

        ClassPool pool = ClassPool.getDefault();
        ClassLoader cl = this.getClass().getClassLoader();
        LoaderClassPath lcp = new LoaderClassPath(cl);
        pool.appendClassPath(lcp);

        CtClass ctClass = pool.makeClass(classname);
        // CtClass ctClassBeanInfo = pool.makeClass(beaninfoClassname);

        CtClass superClass = pool.getCtClass(CIDS_DYNAMICS_SUPERCLASS);
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

        Vector<MemberAttributeInfo> mais = new Vector<MemberAttributeInfo>(
                metaClass.getMemberAttributeInfos().values());

        for (MemberAttributeInfo mai : mais) {
            String fieldname = mai.getFieldName().toLowerCase();
            String attributeJavaClassName = mai.getJavaclassname();

            if (mai.isArray()) {
                attributeJavaClassName = "org.jdesktop.observablecollections.ObservableList"; // zu erstellen mit:
                                                                                              // ObservableCollections.observableList(list)
            } else if (mai.isForeignKey()) {
                if (attributeJavaClassName.equals("org.postgis.PGgeometry")) {
                    attributeJavaClassName = "com.vividsolutions.jts.geom.Geometry";
                } else {
                    attributeJavaClassName = CIDS_DYNAMICS_SUPERCLASS;
                }
            }
            try {
                addPropertyToCtClass(pool, ctClass, Class.forName(attributeJavaClassName), fieldname);
            } catch (Exception e) {
                log.warn("Could not add " + fieldname, e);
            }
        }

        ProtectionDomain pd = this.getClass().getProtectionDomain();
        Class ret = ctClass.toClass(getClass().getClassLoader(), pd);
        log.info("Klasse " + ret + " wurde erfolgreich erzeugt", new CurrentStackTrace());
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
    private static void addPropertyToCtClass(ClassPool pool, CtClass ctClass, Class propertyType, String propertyName)
        throws Exception {
        CtField f = new CtField(pool.get(propertyType.getCanonicalName()), propertyName, ctClass);
        ctClass.addField(f);

        String fieldname = f.getName();
        String getterPrefix = null;
        String postfix = fieldname.toUpperCase().substring(0, 1) + fieldname.substring(1);
        if ((propertyType != boolean.class) && (propertyType != Boolean.class)) {
            getterPrefix = "get";
        } else {
            // Hier wird ein zusaetzlicher "getter" angelegt
            getterPrefix = "is";
            CtMethod additionalGetter = CtNewMethod.getter(getterPrefix + postfix, f);
            ctClass.addMethod(additionalGetter);

            // leider reicht dieser "getter" nicht. beans binding braucht auch bei einem Boolean ein "getter" der mit
            // get anfaengt
            getterPrefix = "get";
        }

        String getterName = getterPrefix + postfix;
        String setterName = "set" + postfix;

        CtMethod getter = CtNewMethod.getter(getterName, f);
        CtMethod setter = CtNewMethod.setter(setterName, f);

        setter.insertAfter(
            "propertyChangeSupport.firePropertyChange(\"" + f.getName() + "\", null, " + f.getName() + ");");

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
    public static void main(String[] args) throws Throwable {
        Log4JQuickConfig.configure4LumbermillOnLocalhost();
        String domain = "WUNDA_DEMO";

        int AAPERSON_CLASSID = 374;

        // rmi registry lokaliseren
        java.rmi.registry.Registry rmiRegistry = LocateRegistry.getRegistry(1099);

        // lookup des callservers
        Remote r = (Remote)Naming.lookup("rmi://localhost/callServer");

        // ich weiss, dass die server von callserver implementiert werden
        SearchService ss = (SearchService)r;
        CatalogueService cat = (CatalogueService)r;
        MetaService meta = (MetaService)r;
        UserService us = (UserService)r;

        User u = us.getUser(domain, "Demo", domain, "demo", "demo");

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

        CidsBean stefan = CidsBean.constructNew(meta, u, domain, "aaperson");
        stefan.setProperty("name", "Richter");
        stefan.setProperty("vorname", "Stefan");

        CidsBean newBild = CidsBean.constructNew(meta, u, domain, "aabild");

        newBild.setProperty("url", "http://www.stefan-richter.info/Unterseiten/Fotos/2005/picture-0006.jpg");
        stefan.setProperty("bild", newBild);

        CidsBean newSRAuto = CidsBean.constructNew(meta, u, domain, "aaauto");
        newSRAuto.setProperty("marke", "VW Golf");
        newSRAuto.setProperty("kennz", "MZG-SR-1");
        ((List)stefan.getProperty("autos")).add(newSRAuto);
        if (log.isDebugEnabled()) {
            log.debug("Autos:" + stefan.getProperty("autos"));
        }
        if (log.isDebugEnabled()) {
            log.debug("vor persist:" + stefan.getMOString());
        }
        CidsBean check2 = stefan.persist(meta, u, domain);
        log.info("Check:" + check2.getMOString());

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
