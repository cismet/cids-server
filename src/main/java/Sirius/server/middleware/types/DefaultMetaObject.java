/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.*;
import Sirius.server.newuser.*;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.util.*;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.tostring.*;

import de.cismet.cids.utils.MetaClassCacheService;

/**
 * Return Type of a RMI method.
 *
 * @version  $Revision$, $Date$
 */
public final class DefaultMetaObject extends Sirius.server.localserver.object.DefaultObject implements MetaObject {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DefaultMetaObject.class);

    //~ Instance fields --------------------------------------------------------

    /** domain (localserver) of where this object is hosted. */
    protected String domain;
    /** this object was changed (needs to be modified according to status). */
    protected boolean changed;
    /** this objects editor. */
    protected String editor;
    /** this objects renderer. */
    protected String renderer;
    /** this objects status (NO_STATUS,NEW,MODIFIED,TO_DELETE,TEMPLATE). */
    /** this objects MetaClass (to be set in a clientApplication after retrieval ). */
    private MetaClass metaClass;
    private transient HashMap classes;
    private transient CidsBean bean = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * constructs a metaObject out of a (server) object. mainly adds the domain infromation
     *
     * @param   o       "server" object
     * @param   domain  domain
     *
     * @throws  Exception  java.lang.Exception error
     */
    public DefaultMetaObject(final Sirius.server.localserver.object.Object o, final String domain) throws Exception {
        // zum Testen einfach rekursives ersetzen
        super(o);
        this.domain = domain;
        setStatus(o.getStatus());
        if (o instanceof DefaultMetaObject) {
            // this.status = ((MetaObject) o).status;
            this.classes = ((DefaultMetaObject)o).classes;
        } else {
            // this.status = NO_STATUS;
        }
        final ObjectAttribute[] attr = o.getAttribs();

        for (int i = 0; i < attr.length; i++) {
            if (attr[i].referencesObject()) {
                final Sirius.server.localserver.object.Object ob = (Sirius.server.localserver.object.Object)
                    attr[i].getValue();

                if (ob != null) {
                    final MetaObject mo = new DefaultMetaObject(ob, domain);
                    attr[i].setValue(mo);
                    // attr[i].setClassKey(ob.getClassID()+"@"+domain);
                }
            }
            // \u00FCbrschreibe classkey der attribute
        }

        this.setDummy(o.isDummy());

        if (Lookup.getDefault().lookup(MetaClassCacheService.class) != null) {
            this.setAllClasses();
        }
    }
    // bugfix

    /**
     * constructs a metaObject out of a (server) object. mainly adds the domain infromation and filters attribute not
     * allowed for a usergroup (ug)
     *
     * @param   object  "server" object
     * @param   domain  domain
     * @param   ug      user group
     *
     * @throws  Exception  java.lang.Exception error
     */
    public DefaultMetaObject(final Sirius.server.localserver.object.Object object,
            final String domain,
            final UserGroup ug) throws Exception {
        this(object.filter(ug), domain);
    }
    // --------------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

    @Override
    public HashMap getAllClasses() {
        if (classes == null) {
            setAllClasses();
        }
        return classes;
    }

    /**
     * getter for grouping criterion in this case the domain (in the sense of the group by clause in SQL).
     *
     * @return  grouping criterion
     */
    @Override
    public String getGroup() {
        return domain;
    }

    /**
     * getter for domain.
     *
     * @return  domain
     */
    @Override
    public String getDomain() {
        return domain;
    }
    // workarround wegen Umstellung

    /**
     * getter for name.
     *
     * @return  name
     */
    @Override
    public String getName() {
        final Collection c = getAttributeByName("name", 1); // NOI18N

        final Iterator iter = c.iterator();
        Attribute a = null;

        if (iter.hasNext()) {
            a = (Attribute)iter.next();

            final Object value = a.getValue();

            if (value != null) {
                return value.toString();
            }
        }

        return null;
    }
    // workarround wegen Umstellung

    /**
     * getter for description.
     *
     * @return  description
     */
    @Override
    public String getDescription() {
        final Collection c = getAttributeByName("description", 1); // NOI18N

        final Iterator iter = c.iterator();

        if (iter.hasNext()) {
            final Object o = ((Attribute)iter.next()).getValue();

            if (o != null) {
                return o.toString();
            }
        }

        return null;
    }

    /**
     * method fo rthe visitor pattern (resolves recursion).
     *
     * @param   mov  DOCUMENT ME!
     * @param   o    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object accept(final TypeVisitor mov, final Object o) {
        return mov.visitMO(this, o);
    }

    @Override
    public void setArrayKey2PrimaryKey() {
        final int primaryKey = getId();
        final ObjectAttribute[] allAttribs = getAttribs();

        for (final ObjectAttribute oa : allAttribs) {
            if (oa.getMai().isArray()) {
                final MetaObject dummyObject = (MetaObject)oa.getValue();
                final String backreferenceFieldName = oa.getMai().getArrayKeyFieldName();
                try {
                    final ObjectAttribute[] dummyEntries = dummyObject.getAttribs();
                    for (final ObjectAttribute dummyEntry : dummyEntries) {
                        final MetaObject dummyEntryMO = (MetaObject)dummyEntry.getValue();
                        dummyEntryMO.getAttributeByFieldName(backreferenceFieldName).setValue(primaryKey);
                    }
                } catch (Exception e) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("no dummyobject for " + oa.getMai().getFieldName()); // NOI18N
                    }
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   classes  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String toString(final HashMap classes) {
        return metaClass.getToStringConverter().convert(this, classes);
    }

    /**
     * getter for classKey.
     *
     * @return  classKey
     */
    @Override
    public String getClassKey() {
        return super.classID + "@" + domain; // NOI18N
    }

    /**
     * Getter for property changed.
     *
     * @return  Value of property changed.
     */
    @Override
    public boolean isChanged() {
        return changed;
    }

    /**
     * Setter for property changed.
     *
     * @param  changed  New value of property changed.
     */
    @Override
    public void setChanged(final boolean changed) {
        this.changed = changed;
    }

    /**
     * getter for complex editor.
     *
     * @return  complex editor
     */
    @Override
    public String getComplexEditor() {
        return editor;
    }

    /**
     * getter for simple editor.
     *
     * @return  siomple editor
     */
    @Override
    public String getSimpleEditor() {
        return editor;
    }

    /**
     * getter for renderer.
     *
     * @return  renderer
     */
    @Override
    public String getRenderer() {
        return renderer;
    }

    /**
     * Getter for property editor.
     *
     * @return  Value of property editor.
     */
    @Override
    public java.lang.String getEditor() {
        return editor;
    }

    /**
     * Setter for property editor.
     *
     * @param  editor  New value of property editor.
     */
    @Override
    public void setEditor(final java.lang.String editor) {
        this.editor = editor;
    }

    /**
     * Setter for property renderer.
     *
     * @param  renderer  New value of property renderer.
     */
    @Override
    public void setRenderer(final java.lang.String renderer) {
        this.renderer = renderer;
    }

    /**
     * setter for the primary key sets the value of the attribute being primary key.
     *
     * @param   key  value of the key
     *
     * @return  whether a primary key was found and its value set
     */
    @Override
    public boolean setPrimaryKey(final java.lang.Object key) {
        final ObjectAttribute[] as = getAttribs();

        for (int i = 0; i < as.length; i++) {
            if (as[i].isPrimaryKey()) {
                as[i].setValue(key);
                return true;
            }
        }
        return false;
    }

    /**
     * setter for modified.
     *
     * @param  status  DOCUMENT ME!
     */
// public void setModified() {
// this.status = MODIFIED;}
// /**
// * setter for deleted
// */
// public void setDeleted() {
// this.status = TO_DELETE;}
// /**
// * setter for new
// */
// public void setNew() {
// this.status =NEW;}
//
//
// /**
// * setter for new
// */
// public void setTemplate() {
// this.status =TEMPLATE;}
//
    /**
     * sets the same status for all Objects in the hirarchy recursively.
     *
     * @param  status  DOCUMENT ME!
     */
    @Override
    public void setAllStatus(final int status) {
        this.setStatus(status);

        final Iterator attributes = attribHash.values().iterator();

        while (attributes.hasNext()) {
            final ObjectAttribute a = (ObjectAttribute)attributes.next();

            // recursion
            if (a.referencesObject()) {
                final MetaObject mo = (MetaObject)a.getValue();

                if (mo != null) {
                    mo.setAllStatus(status);
                }
            }
        }
    }

    @Override
    public Collection getURLs(final Collection classKeys) {
        if (LOG != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enter getURLS"); // NOI18N
            }
        }
        final ArrayList l = new ArrayList();

        if (classKeys.contains(this.getClassKey()))           // class is an URL
        {
            if (LOG != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getURL meta object is a url"); // NOI18N
                }
            }
            final UrlConverter u2s = new UrlConverter();

            final String url = u2s.convert(this);

            l.add(url);
        }

        final Iterator attributes = attribHash.values().iterator();

        while (attributes.hasNext()) {
            final ObjectAttribute a = (ObjectAttribute)attributes.next();

            // recursion
            if (a.referencesObject()) {
                final MetaObject mo = (MetaObject)a.getValue();

                if (mo != null) {
                    l.addAll(mo.getURLs(classKeys));
                }
            }
        }
        if (LOG != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("end getURLS list contains elementcount = " + l.size()); // NOI18N
            }
        }
        return l;
    }

    @Override
    public Collection getURLsByName(final Collection classKeys, final Collection urlNames) {
        if (LOG != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enter getURLS"); // NOI18N
            }
        }
        final ArrayList l = new ArrayList();

        if (classKeys.contains(this.getClassKey()))                                      // class is an URL
        {
            if (LOG != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getURL meta object is a url will not search attributes"); // NOI18N
                }
            }
            final UrlConverter u2s = new UrlConverter();

            final String url = u2s.convert(this);

            l.add(url);

            return l;
        }

        final Collection attrs = getAttributesByName(urlNames);

        final Iterator attributes = attrs.iterator();

        while (attributes.hasNext()) {
            final ObjectAttribute a = (ObjectAttribute)attributes.next();

            // recursion
            if (a.referencesObject()) {
                final MetaObject mo = (MetaObject)a.getValue();

                if (mo != null) {
                    l.addAll(mo.getURLs(classKeys));
                }
            }
        }
        if (LOG != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("end getURLS list contains elementcount = " + l.size()); // NOI18N
            }
        }
        return l;
    }

    @Override
    public MetaClass getMetaClass() {
        if (metaClass == null) {
            setAllClasses();
        }
        return metaClass;
    }

    @Override
    public void setMetaClass(final MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    @Override
    public void setAllClasses(final HashMap classes) {
        this.classes = classes;
        setAllClasses();
    }

    @Override
    public void setAllClasses() {
        if (classes == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Classcache not set yet. Setting classcache in Domain:" + domain); // NOI18N
            }

            try {
                final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
                if (classCacheService == null) {
                    LOG.warn("MetaClassCacheService not found via lookup"); // NOI18N
                }
                classes = classCacheService.getAllClasses(domain);
            } catch (Exception e) {
                LOG.error("Error while setting classes.", e);               // NOI18N
            }
        }
        if (classes != null) {
            final String classKey = domain + this.classID;
            final MetaClass mc = (MetaClass)classes.get(classKey);

            if (mc != null) {
                metaClass = mc;
            }
            final ObjectAttribute[] oas = this.getAttribs();

            for (int i = 0; i < oas.length; i++) {
                if (oas[i].referencesObject()) {
                    final MetaObject mo = (MetaObject)oas[i].getValue();
                    // recursion
                    if (mo != null) {
                        mo.setAllClasses(classes);
                    }
                }
            }
        } else {
            // logger.warn("Classcache konnte nicht gesetzt werden.");
        }
    }

    /**
     * String representation of this DefaultObject.
     *
     * @return  DefaultObject as a String
     */
    @Override
    public String toString() {
//        setLogger();
//        if (log != null) {
//            if (log.isDebugEnabled()) {
//                log.debug("MetaClass gesetzt ? " + metaClass);
//            }
//        }
//
//        if (getMetaClass() != null) {
//            if (getMetaClass().getToStringConverter() != null) {
//                return getMetaClass().getToStringConverter().convert(this);
//            } else {
//                log.warn("kein Stringvonverter gesetzt");
//                return "";
//            }
//        } else {
//            if (log != null) {
//                log.warn(
//                        "keine Klasse und daher kein StringConverter f\u00FCr dieses MetaObject gesetzt : "
//                        + this.getID());
//            }
//            log.error("Metaclass was null classId=" + classID);
//            return "Metaclass was null";
//        }
        final MetaClass mc = getMetaClass();
        if (mc != null) {
            final ToStringConverter converter = metaClass.getToStringConverter();
            if (converter != null) {
                return converter.convert(this);
            } else {
                return "";               // NOI18N
            }
        } else {
            return "Metaclass was null"; // NOI18N
        }
    }

    @Override
    public org.apache.log4j.Logger getLogger() {
        return LOG;
    }

    @Override
    public int getId() {
        return super.getID();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   changed  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private String getColorForChangedFlag(final boolean changed) {
        if (changed) {
            return "\"#D62408\""; // red   // NOI18N
        } else {
            return "\"#FFFFFF\""; // white   // NOI18N
        }
    }

    @Override
    public String getDebugString() {
        String ret = ""; // NOI18N
        // System.out.println("class :: "+classID+"object :: " +objectID+"  atrubutes"+ attribHash);
        // border=\"1\"  bgcolor=\"#E0E0E0\"
        ret =
            "<table border=\"1\" rules=\"all\" cellspacing=\"0\" cellpadding=\"2\"> <tr><th colspan=\"2\" align=\"left\">class = " // NOI18N
                    + classID
                    + "<br>object id ="                                                                                            // NOI18N
                    + objectID
                    + "<br>status = "                                                                                              // NOI18N
                    + getStatusDebugString()
                    + "<br>dummy = "                                                                                               // NOI18N
                    + isDummy()
                    + "</th></tr>";                                                                                                // NOI18N

        final ObjectAttribute[] as = getAttribs();
        for (int i = 0; i < as.length; i++) {
            if (as[i].referencesObject() && (as[i].getValue() != null)) {
                ret += "<tr><td bgcolor="                         // NOI18N
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">"  // NOI18N
                            + as[i].getName()
                            + "</td><td bgcolor="                 // NOI18N
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">[" // NOI18N
                            + as[i].getMai().getFieldName()
                            + "]</td><td>"                        // NOI18N
                            + ((MetaObject)as[i].getValue()).getDebugString()
                            + "</td></tr>";                       // NOI18N
            } else {
                ret += "<tr><td bgcolor="                         // NOI18N
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">"  // NOI18N
                            + as[i].getName()
                            + "</td><td bgcolor="                 // NOI18N
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">[" // NOI18N
                            + as[i].getMai().getFieldName()
                            + "]</td><td>"                        // NOI18N
                            + as[i].toString()
                            + "</td></tr>";                       // NOI18N
            }
        }
        ret += "</table>";                                        // NOI18N
        return ret;
    }

    @Override
    public String getPropertyString() {
        String ret = "";      // NOI18N
        ret = "Properties:("  // NOI18N
                    + classID
                    + ","     // NOI18N
                    + objectID
                    + "):\n"; // NOI18N

        final ObjectAttribute[] as = getAttribs();
        for (int i = 0; i < as.length; i++) {
            if (as[i].referencesObject() && (as[i].getValue() != null)) {
                ret += as[i].getMai().getFieldName()
                            + "-->"         // NOI18N
                            + ((MetaObject)as[i].getValue()).getPropertyString();
                if (((MetaObject)as[i].getValue()).getStatus() == DefaultMetaObject.TO_DELETE) {
                    ret += "**deleteted**"; // NOI18N
                }
                ret += "\n";                // NOI18N
            } else {
                ret += as[i].getMai().getFieldName()
                            + "="           // NOI18N
                            + as[i].toString()
                            + "\n";         // NOI18N
            }
        }
        return ret;
    }

    @Override
    public boolean propertyEquals(final MetaObject tester) {
        try {
            final String thisPS = getPropertyString();
            final String testerPS = tester.getPropertyString();
            return (thisPS.equals(testerPS));
        } catch (Exception ex) {
            getLogger().error("Error in propertyEquals " + ex); // NOI18N
        }
        return false;
    }

    @Override
    public CidsBean getBean() {
        if (bean == null) {
            try {
                bean = BeanFactory.getInstance().createBean(this);
            } catch (Exception e) {
                getLogger().error("Error while creating JavaBean of a MetaObject \n" + getDebugString(), e); // NOI18N
            }
        }
        return bean;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MetaObject) {
            final MetaObject tmp = (MetaObject)obj;
            // debug: if ((getClassID() == tmp.getClassID()) && (getID() == tmp.getID()) &&
            // getDomain().equals(tmp.getDomain()) != equals(obj)) { logger.fatal("Different Equals: " + toString() +
            // "\n VS \n" + obj); }
            if (getID() > -1) {
                return (getClassID() == tmp.getClassID())
                            && (getID() == tmp.getID())
                            && getDomain().equals(tmp.getDomain());
            } else {
                // not persisted MOs are only equal if they have the same reference
                return this
                            == obj;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (11 * hash)
                    + this.getClassID();
        hash = (11 * hash)
                    + this.getID();
        hash = (11 * hash)
                    + this.getDomain().hashCode();
        return hash;
    }
}
