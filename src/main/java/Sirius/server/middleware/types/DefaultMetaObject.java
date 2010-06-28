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
        final Collection c = getAttributeByName(new String("name"), 1);

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
        final Collection c = getAttributeByName(new String("description"), 1);

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
                        getLogger().debug("no dummyobject for " + oa.getMai().getFieldName());
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
        return super.classID + "@" + domain;
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
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("enter getURLS");
            }
        }
        final ArrayList l = new ArrayList();

        if (classKeys.contains(this.getClassKey())) // class is an URL
        {
            if (log != null) {
                if (log.isDebugEnabled()) {
                    log.debug("getURL meta object is a url");
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
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("end getURLS list contains elementcount = " + l.size());
            }
        }
        return l;
    }

    @Override
    public Collection getURLsByName(final Collection classKeys, final Collection urlNames) {
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("enter getURLS");
            }
        }
        final ArrayList l = new ArrayList();

        if (classKeys.contains(this.getClassKey())) // class is an URL
        {
            if (log != null) {
                if (log.isDebugEnabled()) {
                    log.debug("getURL meta object is a url will not search attributes");
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
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("end getURLS list contains elementcount = " + l.size());
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
    public void setAllClasses(final Hashtable classes) {
        this.classes = classes;
        setAllClasses();
    }

    @Override
    public void setAllClasses() {
        if (classes == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Classcache noch nicht gesetzt. Setze Classcache der Domain:" + domain);
            }

            try {
                final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
                if (classCacheService == null) {
                    log.warn("MetaClassCacheService ueber Lookup nicht gefunden");
                }
                classes = classCacheService.getAllClasses(domain);
            } catch (Exception e) {
                log.error("Fehler beim Setzen der Klassen.", e);
            }
        }
        if (classes != null) {
            final String classKey = new String(domain + this.classID);
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
        setLogger();
        if (log != null) {
            if (log.isDebugEnabled()) {
                log.debug("MetaClass gesetzt ? " + metaClass);
            }
        }

        if (getMetaClass() != null) {
            if (getMetaClass().getToStringConverter() != null) {
                return getMetaClass().getToStringConverter().convert(this);
            } else {
                log.warn("kein Stringvonverter gesetzt");
                return "";
            }
        } else {
            if (log != null) {
                log.warn(
                    "keine Klasse und daher kein StringConverter f\u00FCr dieses MetaObject gesetzt : "
                            + this.getID());
            }
            log.error("Metaclass was null classId=" + classID);
            return "Metaclass was null";
        }
    }

    @Override
    public void setLogger() {
        if (log == null) {
            log = org.apache.log4j.Logger.getLogger(this.getClass());
        }
        final ObjectAttribute[] attrs = this.getAttribs();

        for (int i = 0; i < attrs.length; i++) {
            if (attrs[i] != null) {
                attrs[i].setLogger();
            }
        }
    }

    @Override
    public org.apache.log4j.Logger getLogger() {
        if (log == null) {
            setLogger();
        }
        return log;
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
            return "\"#D62408\""; // red
        } else {
            return "\"#FFFFFF\""; // white
        }
    }

    @Override
    public String getDebugString() {
        String ret = "";
        // System.out.println("class :: "+classID+"object :: " +objectID+"  atrubutes"+ attribHash);
        // border=\"1\"  bgcolor=\"#E0E0E0\"
        ret =
            "<table border=\"1\" rules=\"all\" cellspacing=\"0\" cellpadding=\"2\"> <tr><th colspan=\"2\" align=\"left\">class = "
                    + classID
                    + "<br>object id ="
                    + objectID
                    + "<br>status = "
                    + getStatusDebugString()
                    + "<br>dummy = "
                    + isDummy()
                    + "</th></tr>";

        final ObjectAttribute[] as = getAttribs();
        ret += "";
        for (int i = 0; i < as.length; i++) {
            if (as[i].referencesObject() && (as[i].getValue() != null)) {
                ret += "<tr><td bgcolor="
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">"
                            + as[i].getName()
                            + "</td><td bgcolor="
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">["
                            + as[i].getMai().getFieldName()
                            + "]</td><td>"
                            + ((MetaObject)as[i].getValue()).getDebugString()
                            + "</td></tr>";
            } else {
                ret += "<tr><td bgcolor="
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">"
                            + as[i].getName()
                            + "</td><td bgcolor="
                            + getColorForChangedFlag(as[i].isChanged())
                            + " valign=\"top\" align=\"right\">["
                            + as[i].getMai().getFieldName()
                            + "]</td><td>"
                            + as[i].toString()
                            + "</td></tr>";
            }
        }
        ret += "";
        ret += "</table>";
        return ret;
    }

    @Override
    public String getPropertyString() {
        String ret = "";
        ret = "Properties:("
                    + classID
                    + ","
                    + objectID
                    + "):\n";

        final ObjectAttribute[] as = getAttribs();
        for (int i = 0; i < as.length; i++) {
            if (as[i].referencesObject() && (as[i].getValue() != null)) {
                ret += as[i].getMai().getFieldName()
                            + "-->"
                            + ((MetaObject)as[i].getValue()).getPropertyString();
                if (((MetaObject)as[i].getValue()).getStatus() == DefaultMetaObject.TO_DELETE) {
                    ret += "**deleteted**";
                }
                ret += "\n";
            } else {
                ret += as[i].getMai().getFieldName()
                            + "="
                            + as[i].toString()
                            + "\n";
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
            getLogger().error("Error in propertyEquals " + ex);
        }
        return false;
    }

    @Override
    public CidsBean getBean() {
        if (bean == null) {
            try {
                bean = BeanFactory.getInstance().createBean(this);
            } catch (Exception e) {
                getLogger().error("Fehler beim Erzeugen der JavaBean eines MetaObjects \n" + getDebugString(), e);
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
