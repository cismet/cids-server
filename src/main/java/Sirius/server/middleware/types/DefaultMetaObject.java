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

import java.util.*;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.tostring.*;

import de.cismet.cids.utils.MetaClassCacheService;

import org.openide.util.Lookup;

/**
 * Return Type of a RMI method.
 *
 * @version  $Revision$, $Date$
 */
public final class DefaultMetaObject extends Sirius.server.localserver.object.DefaultObject implements MetaObject {

    //~ Instance fields --------------------------------------------------------

    /** domain (localserver) of where this object is hosted. */
    protected String domain;
    /** this object was changed (needs to be modified according to status). */
    protected boolean changed;
    /** this objects editor. */
    protected String editor;
    /** this objects renderer. */
    protected String renderer;
    private transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    /** this objects status (NO_STATUS,NEW,MODIFIED,TO_DELETE,TEMPLATE). */
    /** this objects MetaClass (to be set in a clientApplication after retrieval ). */
    private MetaClass metaClass;
    private transient Hashtable classes;
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
    public DefaultMetaObject(Sirius.server.localserver.object.Object o, String domain) throws Exception {
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
        ObjectAttribute[] attr = o.getAttribs();

        for (int i = 0; i < attr.length; i++) {
            if (attr[i].referencesObject()) {
                Sirius.server.localserver.object.Object ob = (Sirius.server.localserver.object.Object)
                    attr[i].getValue();

                if (ob != null) {
                    MetaObject mo = new DefaultMetaObject(ob, domain);
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
    public DefaultMetaObject(Sirius.server.localserver.object.Object object, String domain, UserGroup ug)
        throws Exception {
        this(object.filter(ug), domain);
    }
    // --------------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

    public Hashtable getAllClasses() {
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
    public String getGroup() {
        return domain;
    }

    /**
     * getter for domain.
     *
     * @return  domain
     */
    public String getDomain() {
        return domain;
    }
    // workarround wegen Umstellung

    /**
     * getter for name.
     *
     * @return  name
     */
    public String getName() {
        Collection c = getAttributeByName(new String("name"), 1);   // NOI18N

        Iterator iter = c.iterator();
        Attribute a = null;

        if (iter.hasNext()) {
            a = (Attribute)iter.next();

            Object value = a.getValue();

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
    public String getDescription() {
        Collection c = getAttributeByName(new String("description"), 1);   // NOI18N

        Iterator iter = c.iterator();

        if (iter.hasNext()) {
            Object o = ((Attribute)iter.next()).getValue();

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
    public Object accept(TypeVisitor mov, Object o) {
        return mov.visitMO(this, o);
    }

    public void setArrayKey2PrimaryKey() {
        int primaryKey = getId();
        ObjectAttribute[] allAttribs = getAttribs();

        for (ObjectAttribute oa : allAttribs) {
            if (oa.getMai().isArray()) {
                MetaObject dummyObject = (MetaObject)oa.getValue();
                String backreferenceFieldName = oa.getMai().getArrayKeyFieldName();
                try {
                    ObjectAttribute[] dummyEntries = dummyObject.getAttribs();
                    for (ObjectAttribute dummyEntry : dummyEntries) {
                        MetaObject dummyEntryMO = (MetaObject)dummyEntry.getValue();
                        dummyEntryMO.getAttributeByFieldName(backreferenceFieldName).setValue(primaryKey);
                    }
                } catch (Exception e) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("no dummyobject for " + oa.getMai().getFieldName());   // NOI18N
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
    public String toString(HashMap classes) {
        return metaClass.getToStringConverter().convert(this, classes);
    }

    /**
     * getter for classKey.
     *
     * @return  classKey
     */
    public String getClassKey() {
        return super.classID + "@" + domain;   // NOI18N
    }

    /**
     * Getter for property changed.
     *
     * @return  Value of property changed.
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Setter for property changed.
     *
     * @param  changed  New value of property changed.
     */
    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    /**
     * getter for complex editor.
     *
     * @return  complex editor
     */
    public String getComplexEditor() {
        return editor;
    }

    /**
     * getter for simple editor.
     *
     * @return  siomple editor
     */
    public String getSimpleEditor() {
        return editor;
    }

    /**
     * getter for renderer.
     *
     * @return  renderer
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * Getter for property editor.
     *
     * @return  Value of property editor.
     */
    public java.lang.String getEditor() {
        return editor;
    }

    /**
     * Setter for property editor.
     *
     * @param  editor  New value of property editor.
     */
    public void setEditor(java.lang.String editor) {
        this.editor = editor;
    }

    /**
     * Setter for property renderer.
     *
     * @param  renderer  New value of property renderer.
     */
    public void setRenderer(java.lang.String renderer) {
        this.renderer = renderer;
    }

    /**
     * setter for the primary key sets the value of the attribute being primary key.
     *
     * @param   key  value of the key
     *
     * @return  whether a primary key was found and its value set
     */
    public boolean setPrimaryKey(java.lang.Object key) {
        ObjectAttribute[] as = getAttribs();

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
    public void setAllStatus(int status) {
        this.setStatus(status);

        Iterator attributes = attribHash.values().iterator();

        while (attributes.hasNext()) {
            ObjectAttribute a = (ObjectAttribute)attributes.next();

            // recursion
            if (a.referencesObject()) {
                MetaObject mo = (MetaObject)a.getValue();

                if (mo != null) {
                    mo.setAllStatus(status);
                }
            }
        }
    }

    public Collection getURLs(Collection classKeys) {
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("enter getURLS");   // NOI18N
            }
        }
        ArrayList l = new ArrayList();

        if (classKeys.contains(this.getClassKey())) // class is an URL
        {
            if (log != null) {
                if (log.isDebugEnabled()) {
                    log.debug("getURL meta object is a url");   // NOI18N
                }
            }
            UrlConverter u2s = new UrlConverter();

            String url = u2s.convert(this);

            l.add(url);
        }

        Iterator attributes = attribHash.values().iterator();

        while (attributes.hasNext()) {
            ObjectAttribute a = (ObjectAttribute)attributes.next();

            // recursion
            if (a.referencesObject()) {
                MetaObject mo = (MetaObject)a.getValue();

                if (mo != null) {
                    l.addAll(mo.getURLs(classKeys));
                }
            }
        }
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("end getURLS list contains elementcount = " + l.size());   // NOI18N
            }
        }
        return l;
    }

    public Collection getURLsByName(Collection classKeys, Collection urlNames) {
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("enter getURLS");   // NOI18N
            }
        }
        ArrayList l = new ArrayList();

        if (classKeys.contains(this.getClassKey())) // class is an URL
        {
            if (log != null) {
                if (log.isDebugEnabled()) {
                    log.debug("getURL meta object is a url will not search attributes");   // NOI18N
                }
            }
            UrlConverter u2s = new UrlConverter();

            String url = u2s.convert(this);

            l.add(url);

            return l;
        }

        Collection attrs = getAttributesByName(urlNames);

        Iterator attributes = attrs.iterator();

        while (attributes.hasNext()) {
            ObjectAttribute a = (ObjectAttribute)attributes.next();

            // recursion
            if (a.referencesObject()) {
                MetaObject mo = (MetaObject)a.getValue();

                if (mo != null) {
                    l.addAll(mo.getURLs(classKeys));
                }
            }
        }
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("end getURLS list contains elementcount = " + l.size());   // NOI18N
            }
        }
        return l;
    }

    public MetaClass getMetaClass() {
        if (metaClass == null) {
            setAllClasses();
        }
        return metaClass;
    }

    public void setMetaClass(MetaClass metaClass) {
        this.metaClass = metaClass;
    }

    public void setAllClasses(Hashtable classes) {
        this.classes = classes;
        setAllClasses();
    }

    public void setAllClasses() {
        if (classes == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Classcache not set yet. Setting classcache in Domain:" + domain);   // NOI18N
            }

            try {
                MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
                if (classCacheService == null) {
                    log.warn("MetaClassCacheService not found via lookup");   // NOI18N
                }
                classes = classCacheService.getAllClasses(domain);
            } catch (Exception e) {
                log.error("Error while setting classes.", e);   // NOI18N
            }
        }
        if (classes != null) {
            String classKey = new String(domain + this.classID);
            MetaClass mc = (MetaClass)classes.get(classKey);

            if (mc != null) {
                metaClass = mc;
            }
            ObjectAttribute[] oas = this.getAttribs();

            for (int i = 0; i < oas.length; i++) {
                if (oas[i].referencesObject()) {
                    MetaObject mo = (MetaObject)oas[i].getValue();
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
    public String toString() {
        setLogger();
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("MetaClass set ? " + metaClass);   // NOI18N
            }
        }

        if (getMetaClass() != null) {
            if (getMetaClass().getToStringConverter() != null) {
                return getMetaClass().getToStringConverter().convert(this);
            } else {
                log.warn("no Stringvonverter set");   // NOI18N
                return "";   // NOI18N
            }
        } else {
            if (log != null) {
                log.warn(
                    "no class and therefore no StringConverter set for this MetaObject : " + this.getID());   // NOI18N
            }
            log.error("Metaclass was null classId=" + classID);   // NOI18N
            return "Metaclass was null";   // NOI18N
        }
    }

    public void setLogger() {
        if (log == null) {
            log = org.apache.log4j.Logger.getLogger(this.getClass());
        }
        ObjectAttribute[] attrs = this.getAttribs();

        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i] != null) {
                attrs[i].setLogger();
            }
        }
    }

    public org.apache.log4j.Logger getLogger() {
        if (log == null) {
            setLogger();
        }
        return log;
    }

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
    private String getColorForChangedFlag(boolean changed) {
        if (changed) {
            return "\"#D62408\""; // red   // NOI18N
        } else {
            return "\"#FFFFFF\""; // white   // NOI18N
        }
    }

    public String getDebugString() {
        String ret = "";   // NOI18N
        // System.out.println("class :: "+classID+"object :: " +objectID+"  atrubutes"+ attribHash);
        // border=\"1\"  bgcolor=\"#E0E0E0\"
        ret =
            "<table border=\"1\" rules=\"all\" cellspacing=\"0\" cellpadding=\"2\"> <tr><th colspan=\"2\" align=\"left\">class = "   // NOI18N
            + classID
            + "<br>object id =" + objectID   // NOI18N
            + "<br>status = " + getStatusDebugString()   // NOI18N
            + "<br>dummy = " + isDummy()   // NOI18N
            + "</th></tr>";   // NOI18N

        ObjectAttribute[] as = getAttribs();
        ret += "";   // NOI18N
        for (int i = 0; i < as.length; i++) {
            if (as[i].referencesObject() && (as[i].getValue() != null)) {
                ret += "<tr><td bgcolor=" + getColorForChangedFlag(as[i].isChanged())   // NOI18N
                    + " valign=\"top\" align=\"right\">" + as[i].getName() + "</td><td bgcolor="   // NOI18N
                    + getColorForChangedFlag(as[i].isChanged()) + " valign=\"top\" align=\"right\">["   // NOI18N
                    + as[i].getMai().getFieldName() + "]</td><td>" + ((MetaObject)as[i].getValue()).getDebugString()   // NOI18N
                    + "</td></tr>";   // NOI18N
            } else {
                ret += "<tr><td bgcolor=" + getColorForChangedFlag(as[i].isChanged())   // NOI18N
                    + " valign=\"top\" align=\"right\">" + as[i].getName() + "</td><td bgcolor="   // NOI18N
                    + getColorForChangedFlag(as[i].isChanged()) + " valign=\"top\" align=\"right\">["   // NOI18N
                    + as[i].getMai().getFieldName() + "]</td><td>" + as[i].toString() + "</td></tr>";   // NOI18N
            }
        }
        ret += "";   // NOI18N
        ret += "</table>";   // NOI18N
        return ret;
    }

    public String getPropertyString() {
        String ret = "";   // NOI18N
        ret = "Properties:(" + classID   // NOI18N
            + "," + objectID   // NOI18N
            + "):\n";   // NOI18N

        ObjectAttribute[] as = getAttribs();
        for (int i = 0; i < as.length; i++) {
            if (as[i].referencesObject() && (as[i].getValue() != null)) {
                ret += as[i].getMai().getFieldName() + "-->" + ((MetaObject)as[i].getValue()).getPropertyString();   // NOI18N
                if (((MetaObject)as[i].getValue()).getStatus() == DefaultMetaObject.TO_DELETE) {
                    ret += "**deleteted**";   // NOI18N
                }
                ret += "\n";   // NOI18N
            } else {
                ret += as[i].getMai().getFieldName() + "=" + as[i].toString() + "\n";   // NOI18N
            }
        }
        return ret;
    }

    public boolean propertyEquals(MetaObject tester) {
        try {
            String thisPS = getPropertyString();
            String testerPS = tester.getPropertyString();
            return (thisPS.equals(testerPS));
        } catch (Exception ex) {
            getLogger().error("Error in propertyEquals " + ex);   // NOI18N
        }
        return false;
    }

    public CidsBean getBean() {
        if (bean == null) {
            try {
                bean = BeanFactory.getInstance().createBean(this);
            } catch (Exception e) {
                getLogger().error("Error while creating JavaBean of a MetaObject \n" + getDebugString(), e);   // NOI18N
            }
        }
        return bean;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaObject) {
            final MetaObject tmp = (MetaObject)obj;
            // debug: if ((getClassID() == tmp.getClassID()) && (getID() == tmp.getID()) &&
            // getDomain().equals(tmp.getDomain()) != equals(obj)) { logger.fatal("Different Equals: " + toString() +
            // "\n VS \n" + obj); }
            if (getID() > -1) {
                return (getClassID() == tmp.getClassID()) && (getID() == tmp.getID())
                            && getDomain().equals(tmp.getDomain());
            } else {
                // not persisted MOs are only equal if they have the same reference
                return this == obj;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (11 * hash) + this.getClassID();
        hash = (11 * hash) + this.getID();
        hash = (11 * hash) + this.getDomain().hashCode();
        return hash;
    }
}
