/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.*;
import Sirius.server.localserver.object.LightweightObject;
import Sirius.server.newuser.*;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.util.*;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.CustomBeanPermissionProvider;

import de.cismet.cids.server.connectioncontext.ConnectionContext;
import de.cismet.cids.server.connectioncontext.ConnectionContextStore;

import de.cismet.cids.tools.tostring.*;

import de.cismet.cids.utils.ClassloadingHelper;
import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.tools.CurrentStackTrace;
import de.cismet.tools.StaticDebuggingTools;

/**
 * Return Type of a RMI method.
 *
 * @version  $Revision$, $Date$
 */
public class DefaultMetaObject extends Sirius.server.localserver.object.DefaultObject implements MetaObject,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DefaultMetaObject.class);
    private static MetaClassCacheService classCacheService = null;
    private static Boolean DEBUG_STRING_ENABLED = null;

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
    private transient ConnectionContext connectionContext;

    //~ Constructors -----------------------------------------------------------

    /**
     * constructs a metaObject out of a (server) object. mainly adds the domain infromation If the user object is known,
     * the constructor
     * {@link #DefaultMetaObject(Sirius.server.localserver.object.Object, java.lang.String, Sirius.server.newuser.User)}
     * should be used in order to build possibly contained LightweightMetaObjects properly.
     *
     * @param  o       "server" object
     * @param  domain  domain
     */
    public DefaultMetaObject(final Sirius.server.localserver.object.Object o, final String domain) {
        this(o, domain, (User)null);
    }
    // bugfix

    /**
     * constructs a metaObject out of a (server) object. mainly adds the domain infromation and filters attribute not
     * allowed for a user
     *
     * @param  o       "server" object
     * @param  domain  domain
     * @param  user    DOCUMENT ME!
     */
    public DefaultMetaObject(final Sirius.server.localserver.object.Object o, final String domain, final User user) {
        // zum Testen einfach rekursives ersetzen
        super(o);
        if (user != null) {
            o.filter(user);
        }
        this.domain = domain;
        super.setStatus(o.getStatus());
        if (o instanceof DefaultMetaObject) {
            // this.status = ((MetaObject) o).status;
            this.classes = ((DefaultMetaObject)o).classes;
        } else {
            // this.status = NO_STATUS;
        }

        super.setDummy(o.isDummy());
        this.initAttributes(domain, user);
    }

    // --------------------------------------------------------------

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private boolean isDebugStringEnabled() {
        if (DEBUG_STRING_ENABLED == null) {
            DEBUG_STRING_ENABLED = StaticDebuggingTools.checkHomeForFile("MoDebugStringEnabled");
        }
        return DEBUG_STRING_ENABLED;
    }

    /**
     * Since no middleware.MetaAttribute class exists, we have to update the "old" localserver.Attributes to make them
     * compatible to thier new parent middleware.MetaObject instance.<br>
     * Warning: This operation must not be called more than once per MetaObject instance!
     *
     * @param  domain  DOCUMENT ME!
     * @param  user    DOCUMENT ME!
     */
    private void initAttributes(final String domain, final User user) {
        for (final ObjectAttribute objectAttribute : this.attribHash.values()) {
            // important: change parent from deprecated localserver.Object to new middleware.MetaObject instance!
            // FIX for #172
            objectAttribute.setParentObject(this);

            if (!this.isDummy() && (objectAttribute.getObjectID() != this.getID())) {
                LOG.warn("object attribute '" + objectAttribute.getName() + "' of MetaObject "
                            + this.getName() + "' (" + this.getID() + "@" + this.getClassKey()
                            + ") object id does not match: " + objectAttribute.getObjectID());
                objectAttribute.setObjectID(this.getID());
            }

            if (objectAttribute.referencesObject() && (objectAttribute.getValue() != null)) {
                final Sirius.server.localserver.object.Object theObject = (Sirius.server.localserver.object.Object)
                    objectAttribute.getValue();

                if (MetaObject.class.isAssignableFrom(theObject.getClass())
                            || LightweightMetaObject.class.isAssignableFrom(theObject.getClass())) {
                    LOG.error("object attribute '" + objectAttribute.getName() + "' of MetaObject '"
                                + this.getName() + "' (" + this.getID() + "@" + this.getClassKey()
                                + ") already converted to MetaObject!",
                        new CurrentStackTrace());
                }

                if (theObject instanceof LightweightObject) {
                    final LightweightMetaObject lwmo = new LightweightMetaObject(
                            theObject.getClassID(),
                            theObject.getID(),
                            domain,
                            user);
                    lwmo.setConnectionContext(getConnectionContext());
                    objectAttribute.setValue(lwmo);
                } else {
                    final DefaultMetaObject mo = new DefaultMetaObject(theObject, domain, user);
                    mo.setConnectionContext(getConnectionContext());
                    objectAttribute.setValue(mo);
                }
                // disabled! why?
                // attr[i].setClassKey(ob.getClassID()+"@"+domain);
            }
        }
        // \u00FCbrschreibe classkey der attribute
    }

    @Override
    public void addAttribute(final ObjectAttribute objectAttribute) {
        super.addAttribute(objectAttribute);

        if (!this.isDummy() && (objectAttribute.getObjectID() != this.getID())) {
            LOG.warn("object attribute '" + objectAttribute.getName() + "' of MetaObject "
                        + this.getName() + "' (" + this.getID() + "@" + this.getClassKey()
                        + ") object id does not match: " + objectAttribute.getObjectID());
            objectAttribute.setObjectID(this.getID());
        }

        if (objectAttribute.referencesObject() && (objectAttribute.getValue() != null)) {
            final Sirius.server.localserver.object.Object theObject = (Sirius.server.localserver.object.Object)
                objectAttribute.getValue();

            if (!MetaObject.class.isAssignableFrom(theObject.getClass())
                        || LightweightMetaObject.class.isAssignableFrom(theObject.getClass())) {
                LOG.error("object attribute '" + objectAttribute.getName() + "' of MetaObject "
                            + this.getName() + "' (" + this.getID() + "@" + this.getClassKey()
                            + ")  does not contain a MetaObject!",
                    new CurrentStackTrace());
            }
        }
    }

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

    /**
     * getter for name.
     *
     * @return  name
     */
    @Override
    public String getName() {
        final Collection<ObjectAttribute> c = getAttributeByName("name", 1); // NOI18N
        String name = null;
        if (c.size() > 0) {
            final Iterator<ObjectAttribute> iter = c.iterator();
            if (iter.hasNext()) {
                final ObjectAttribute a = iter.next();

                final Object value = a.getValue();

                if (value != null) {
                    name = value.toString();
                }
            }
        } else {
            final ObjectAttribute oa = getAttributeByFieldName("name");
            if (oa != null) {
                name = String.valueOf(oa.getValue());
            }
        }
        return name;
    }

    /**
     * getter for description.
     *
     * @return  description
     */
    @Override
    public String getDescription() {
        final Collection<ObjectAttribute> c = getAttributeByName("description", 1); // NOI18N

        final Iterator<ObjectAttribute> iter = c.iterator();

        if (iter.hasNext()) {
            final Object o = iter.next().getValue();

            if (o != null) {
                return o.toString();
            }
        }

        return null;
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
    @Deprecated
    public boolean isChanged() {
        return changed || (getStatus() == MODIFIED) || (getStatus() == NEW);
    }

    /**
     * Setter for property changed.
     *
     * @param  changed  New value of property changed.
     */
    @Override
    @Deprecated
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

        final Iterator<ObjectAttribute> attributes = attribHash.values().iterator();

        while (attributes.hasNext()) {
            final ObjectAttribute a = attributes.next();

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
    public Collection<String> getURLs(final Collection classKeys) {
        if (LOG != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enter getURLS"); // NOI18N
            }
        }
        final ArrayList<String> l = new ArrayList<String>();

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

        final Iterator<ObjectAttribute> attributes = attribHash.values().iterator();

        while (attributes.hasNext()) {
            final ObjectAttribute a = attributes.next();

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
    public Collection<String> getURLsByName(final Collection classKeys, final Collection urlNames) {
        if (LOG != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("enter getURLS"); // NOI18N
            }
        }
        final ArrayList<String> l = new ArrayList<String>();

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

        final Collection<ObjectAttribute> attrs = getAttributesByName(urlNames);

        final Iterator<ObjectAttribute> attributes = attrs.iterator();

        while (attributes.hasNext()) {
            final ObjectAttribute a = attributes.next();

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
                if (classCacheService == null) {
                    classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
                }
                if (classCacheService == null) {
                    LOG.warn("MetaClassCacheService not found via lookup"); // NOI18N
                } else {
                    classes = classCacheService.getAllClasses(domain);
                }
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

    @Override
    public boolean hasObjectWritePermission(final User user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasObjectWritePermission for user: " + user); // NOI18N
        }
        CustomBeanPermissionProvider customPermissionProvider = null;

        try {
            final Class cpp = ClassloadingHelper.getDynamicClass(this.getMetaClass(),
                    ClassloadingHelper.CLASS_TYPE.PERMISSION_PROVIDER);

            if (LOG.isDebugEnabled()) {
                LOG.debug("custom write permission provider retrieval result: " + cpp); // NOI18N
            }

            if (cpp == null) {
                return true;
            }

            customPermissionProvider = (CustomBeanPermissionProvider)cpp.getConstructor().newInstance();
            customPermissionProvider.setCidsBean(this.getBean());
        } catch (final Exception ex) {
            // FIXME: probably this behaviour is error prone since we allow write permission if there is a problem
            // with the loading of the custom permission provider, which probably would say "NO" if it was loaded
            // correctly
            LOG.warn("error during creation of custom permission provider", ex); // NOI18N
        }

        if (customPermissionProvider != null) {
            return customPermissionProvider.getCustomWritePermissionDecisionforUser(user);
        } else {
            return true;
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
        if (isDebugStringEnabled()) {
            String ret =
                "<table border=\"1\" rules=\"all\" cellspacing=\"0\" cellpadding=\"2\"> <tr><th colspan=\"3\" align=\"left\">class = " // NOI18N
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
                    final int maxLength = 255;
                    final String suffix = "...";
                    String string = as[i].toString();
                    if (string.length() >= maxLength) {
                        string = string.substring(0, maxLength - suffix.length())
                                    + suffix;
                    }

                    ret += "<tr><td bgcolor="                         // NOI18N
                                + getColorForChangedFlag(as[i].isChanged())
                                + " valign=\"top\" align=\"right\">"  // NOI18N
                                + as[i].getName()
                                + "</td><td bgcolor="                 // NOI18N
                                + getColorForChangedFlag(as[i].isChanged())
                                + " valign=\"top\" align=\"right\">[" // NOI18N
                                + as[i].getMai().getFieldName()
                                + "]</td><td>"                        // NOI18N
                                + string
                                + "</td></tr>";                       // NOI18N
                }
            }
            ret += "</table>";                                        // NOI18N
            return ret;
        } else {
            return
                "DebugString is not enabled. Add a file named \"MoDebugStringEnabled\" in your home directory to enabled it (application restart needed).";
        }
    }

    @Override
    public String getPropertyString() {
        final StringBuilder ret = new StringBuilder("");                                        // NOI18N
        ret.append("Properties:(").append(classID).append(",").append(objectID).append("):\n"); // NOI18N

        final ObjectAttribute[] as = getAttribs();
        for (int i = 0; i < as.length; i++) {
            if (as[i].referencesObject() && (as[i].getValue() != null)) {
                ret.append(as[i].getMai().getFieldName()).append("-->") // NOI18N
                .append(((MetaObject)as[i].getValue()).getPropertyString());
                if (((MetaObject)as[i].getValue()).getStatus() == DefaultMetaObject.TO_DELETE) {
                    ret.append("**deleteted**");                        // NOI18N
                }
                ret.append("\n");                                       // NOI18N
            } else {
                ret.append(as[i].getMai().getFieldName()).append("=").append(as[i].toString()).append("\n");
            }
        }
        return ret.toString();
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
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (MetaObject.class.isAssignableFrom(obj.getClass())) {
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
                // if the MO to be compared is proxied, compare not the proxy reference
                // but the actual MO
                if (obj instanceof java.lang.reflect.Proxy) {
                    return obj.equals(this);
                }

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

    @Override
    public void setID(final int objectID) {
        super.setID(objectID);
        if (!this.isDummy() && !this.attribHash.isEmpty()) {
            for (final ObjectAttribute objectAttribute : this.attribHash.values()) {
                objectAttribute.setObjectID(objectID);
            }
        }
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public void setConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }
}
