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

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserGroup;

import Sirius.util.Mapable;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.fromstring.FromStringCreator;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class LightweightMetaObject implements MetaObject, Comparable<LightweightMetaObject> {

    //~ Instance fields --------------------------------------------------------

    private transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    // use volantile variable to fix "double checked locking" problem!
    private transient volatile MetaObject lazyMetaObject;
    private transient MetaService metaService;
    private final Map<String, Object> attributesMap;
    private final int classID;
    private final User user;
    private int objectID;
    private String representation;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LightweightMetaObject object.
     *
     * @param  classID        DOCUMENT ME!
     * @param  objectID       DOCUMENT ME!
     * @param  user           DOCUMENT ME!
     * @param  attributesMap  DOCUMENT ME!
     * @param  formater       DOCUMENT ME!
     */
    public LightweightMetaObject(final int classID,
            final int objectID,
            final User user,
            final Map<String, Object> attributesMap,
            final AbstractAttributeRepresentationFormater formater) {
        this.classID = classID;
        this.objectID = objectID;
        this.user = user;
        this.metaService = null;
        this.attributesMap = Collections.unmodifiableMap(attributesMap);
        setFormater(formater);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object accept(final TypeVisitor mov, final Object o) {
        return getRealMetaObject().accept(mov, o);
    }

    @Override
    public HashMap getAllClasses() {
        return getRealMetaObject().getAllClasses();
    }

    @Override
    public CidsBean getBean() {
        return getRealMetaObject().getBean();
    }

    @Override
    public String getComplexEditor() {
        return getRealMetaObject().getComplexEditor();
    }

    @Override
    public String getDebugString() {
        return getRealMetaObject().getDebugString();
    }

    @Override
    public String getDescription() {
        return getRealMetaObject().getDescription();
    }

    @Override
    public String getEditor() {
        return getRealMetaObject().getEditor();
    }

    @Override
    public String getGroup() {
        return getRealMetaObject().getGroup();
    }

    @Override
    public Logger getLogger() {
        return getRealMetaObject().getLogger();
    }

    @Override
    public MetaClass getMetaClass() {
        return getRealMetaObject().getMetaClass();
    }

    @Override
    public String getName() {
        return getRealMetaObject().getName();
    }

    @Override
    public String getPropertyString() {
        return getRealMetaObject().getPropertyString();
    }

    @Override
    public String getRenderer() {
        return getRealMetaObject().getRenderer();
    }

    @Override
    public String getSimpleEditor() {
        return getRealMetaObject().getSimpleEditor();
    }

    @Override
    public Collection getURLs(final Collection classKeys) {
        return getRealMetaObject().getURLs(classKeys);
    }

    @Override
    public Collection getURLsByName(final Collection classKeys, final Collection urlNames) {
        return getRealMetaObject().getURLsByName(classKeys, urlNames);
    }

    @Override
    public boolean isChanged() {
        return getRealMetaObject().isChanged();
    }

    @Override
    public boolean propertyEquals(final MetaObject tester) {
        return getRealMetaObject().propertyEquals(tester);
    }

    @Override
    public void setAllClasses(final HashMap classes) {
        getRealMetaObject().setAllClasses(classes);
    }

    @Override
    public void setAllClasses() {
        getRealMetaObject().setAllClasses();
    }

    @Override
    public void setAllStatus(final int status) {
        getRealMetaObject().setAllStatus(status);
    }

    @Override
    public void setArrayKey2PrimaryKey() {
        getRealMetaObject().setArrayKey2PrimaryKey();
    }

    @Override
    public void setChanged(final boolean changed) {
        getRealMetaObject().setChanged(changed);
    }

    @Override
    public void setEditor(final String editor) {
        getRealMetaObject().setEditor(editor);
    }

    @Override
    public void setMetaClass(final MetaClass metaClass) {
        getRealMetaObject().setMetaClass(metaClass);
    }

    @Override
    public boolean setPrimaryKey(final Object key) {
        return getRealMetaObject().setPrimaryKey(key);
    }

    @Override
    public void setRenderer(final String renderer) {
        getRealMetaObject().setRenderer(renderer);
    }

    @Override
    public String toString(final HashMap classes) {
        return getRealMetaObject().toString(classes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectAttributes  DOCUMENT ME!
     */
    @Override
    public void addAllAttributes(final ObjectAttribute[] objectAttributes) {
        getRealMetaObject().addAllAttributes(objectAttributes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  anyAttribute  DOCUMENT ME!
     */
    @Override
    public void addAttribute(final ObjectAttribute anyAttribute) {
        getRealMetaObject().addAttribute(anyAttribute);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   m  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object constructKey(final Mapable m) {
        return getRealMetaObject().constructKey(m);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Sirius.server.localserver.object.Object filter(final UserGroup ug) {
        return getRealMetaObject().filter(ug);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectRepresentation  DOCUMENT ME!
     * @param   mo                    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Override
    public Object fromString(final String objectRepresentation, final Object mo) throws Exception {
        return getRealMetaObject().fromString(objectRepresentation, mo);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute[] getAttribs() {
        return getRealMetaObject().getAttribs();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getAttribute(final Object key) {
        return getRealMetaObject().getAttribute(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute getAttributeByFieldName(final String fieldname) {
        return getRealMetaObject().getAttributeByFieldName(fieldname);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name       DOCUMENT ME!
     * @param   maxResult  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection<Attribute> getAttributeByName(final String name, final int maxResult) {
        return getRealMetaObject().getAttributeByName(name, maxResult);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public HashMap getAttributes() {
        return getRealMetaObject().getAttributes();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   names  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection getAttributesByName(final Collection names) {
        return getRealMetaObject().getAttributesByName(names);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c               DOCUMENT ME!
     * @param   recursionDepth  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection getAttributesByType(final Class c, final int recursionDepth) {
        return getRealMetaObject().getAttributesByType(c, recursionDepth);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection getAttributesByType(final Class c) {
        return getRealMetaObject().getAttributesByType(c);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getKey() {
        return getRealMetaObject().getKey();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Attribute getPrimaryKey() {
        return getRealMetaObject().getPrimaryKey();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute getReferencingObjectAttribute() {
        return getRealMetaObject().getReferencingObjectAttribute();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getStatus() {
        return getRealMetaObject().getStatus();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getStatusDebugString() {
        return getRealMetaObject().getStatusDebugString();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection getTraversedAttributesByType(final Class c) {
        return getRealMetaObject().getTraversedAttributesByType(c);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isDummy() {
        return getRealMetaObject().isDummy();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isPersistent() {
        return getRealMetaObject().isPersistent();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isStringCreateable() {
        return getRealMetaObject().isStringCreateable();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  anyAttribute  DOCUMENT ME!
     */
    @Override
    public void removeAttribute(final ObjectAttribute anyAttribute) {
        getRealMetaObject().removeAttribute(anyAttribute);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dummy  DOCUMENT ME!
     */
    @Override
    public void setDummy(final boolean dummy) {
        getRealMetaObject().setDummy(dummy);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  persistent  DOCUMENT ME!
     */
    @Override
    public void setPersistent(final boolean persistent) {
        getRealMetaObject().setPersistent(persistent);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void setPrimaryKeysNull() {
        getRealMetaObject().setPrimaryKeysNull();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  referencingObjectAttribute  DOCUMENT ME!
     */
    @Override
    public void setReferencingObjectAttribute(final ObjectAttribute referencingObjectAttribute) {
        getRealMetaObject().setReferencingObjectAttribute(referencingObjectAttribute);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  status  DOCUMENT ME!
     */
    @Override
    public void setStatus(final int status) {
        getRealMetaObject().setStatus(status);
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void setValuesNull() {
        getRealMetaObject().setValuesNull();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public FromStringCreator getObjectCreator() {
        return getRealMetaObject().getObjectCreator();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  formater  DOCUMENT ME!
     */
    public void setFormater(final AbstractAttributeRepresentationFormater formater) {
        if (formater != null) {
            formater.setAttributes(attributesMap);
            representation = formater.getRepresentation();
        } else {
            representation = "FORMATER IS NULL! (cID=" + classID + ", oID=" + objectID + ")"; // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   aName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getLWAttribute(final String aName) {
        return attributesMap.get(aName);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Set<String> getKnownAttributeNames() {
        return attributesMap.keySet();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception              DOCUMENT ME!
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private MetaObject fetchRealMetaObject() throws Exception {
        if (metaService == null) {
            throw new IllegalStateException(
                "Can not retrieve MetaObject, as Metaservice for LightweightMetaObject \""
                        + toString() // NOI18N
                        + "\" is null!"); // NOI18N
        }
        return metaService.getMetaObject(getUser(), getObjectID(), getClassID(), getUser().getDomain());
    }

    @Override
    public String toString() {
        return representation;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof MetaObject) {
            final MetaObject tmp = (MetaObject)obj;
            // debug: if ((getClassID() == tmp.getClassID()) && (getID() == tmp.getID()) &&
            // getDomain().equals(tmp.getDomain()) != equals(obj)) { log.fatal("Different Equals: " + toString() + "\n
            // VS \n" + obj); }
            return (getClassID() == tmp.getClassID()) && (getObjectID() == tmp.getID())
                        && getDomain().equals(tmp.getDomain());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (11 * hash) + this.getClassID();
        hash = (11 * hash) + this.getObjectID();
        hash = (11 * hash) + this.getDomain().hashCode();
        return hash;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the classID
     */
    @Override
    public int getClassID() {
        return classID;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the objectID
     */
    public int getObjectID() {
        return objectID;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the user
     */
    public User getUser() {
        return user;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final LightweightMetaObject o) {
        return representation.compareTo(o + ""); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the metaService
     */
    public MetaService getMetaService() {
        return metaService;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metaService  the metaService to set
     */
    public void setMetaService(final MetaService metaService) {
        this.metaService = metaService;
    }

    /**
     * Lazy loads for the real MetaObject if needed, the returns it.
     *
     * @return  the real MetaObject which the LWMetaObject is a proxy for.
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    public MetaObject getRealMetaObject() {
        if (lazyMetaObject == null) {
            synchronized (this) {
                try {
                    lazyMetaObject = fetchRealMetaObject();
                } catch (Exception ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }
        return lazyMetaObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectID  DOCUMENT ME!
     */
    @Override
    public void setID(final int objectID) {
        this.objectID = objectID;
        getRealMetaObject().setID(objectID);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getID() {
        return getObjectID();
    }

    @Override
    public int getId() {
        return getObjectID();
    }

    @Override
    public String getDomain() {
        return getUser().getDomain();
    }

    @Override
    public String getClassKey() {
        return classID + "@" + getUser().getDomain(); // NOI18N
    }
}