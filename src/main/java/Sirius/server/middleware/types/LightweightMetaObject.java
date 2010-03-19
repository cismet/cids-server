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

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.fromstring.FromStringCreator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class LightweightMetaObject implements MetaObject, Comparable<LightweightMetaObject> {

    //~ Methods ----------------------------------------------------------------

    // <editor-fold defaultstate="collapsed" desc="delegation-only methods">
    public Object accept(TypeVisitor mov, Object o) {
        return getRealMetaObject().accept(mov, o);
    }

    public Hashtable getAllClasses() {
        return getRealMetaObject().getAllClasses();
    }

    public CidsBean getBean() {
        return getRealMetaObject().getBean();
    }

    public String getComplexEditor() {
        return getRealMetaObject().getComplexEditor();
    }

    public String getDebugString() {
        return getRealMetaObject().getDebugString();
    }

    public String getDescription() {
        return getRealMetaObject().getDescription();
    }

    public String getEditor() {
        return getRealMetaObject().getEditor();
    }

    public String getGroup() {
        return getRealMetaObject().getGroup();
    }

    public Logger getLogger() {
        return getRealMetaObject().getLogger();
    }

    public MetaClass getMetaClass() {
        return getRealMetaObject().getMetaClass();
    }

    public String getName() {
        return getRealMetaObject().getName();
    }

    public String getPropertyString() {
        return getRealMetaObject().getPropertyString();
    }

    public String getRenderer() {
        return getRealMetaObject().getRenderer();
    }

    public String getSimpleEditor() {
        return getRealMetaObject().getSimpleEditor();
    }

    public Collection getURLs(Collection classKeys) {
        return getRealMetaObject().getURLs(classKeys);
    }

    public Collection getURLsByName(Collection classKeys, Collection urlNames) {
        return getRealMetaObject().getURLsByName(classKeys, urlNames);
    }

    public boolean isChanged() {
        return getRealMetaObject().isChanged();
    }

    public boolean propertyEquals(MetaObject tester) {
        return getRealMetaObject().propertyEquals(tester);
    }

    public void setAllClasses(Hashtable classes) {
        getRealMetaObject().setAllClasses(classes);
    }

    public void setAllClasses() {
        getRealMetaObject().setAllClasses();
    }

    public void setAllStatus(int status) {
        getRealMetaObject().setAllStatus(status);
    }

    public void setArrayKey2PrimaryKey() {
        getRealMetaObject().setArrayKey2PrimaryKey();
    }

    public void setChanged(boolean changed) {
        getRealMetaObject().setChanged(changed);
    }

    public void setEditor(String editor) {
        getRealMetaObject().setEditor(editor);
    }

    public void setLogger() {
        getRealMetaObject().setLogger();
    }

    public void setMetaClass(MetaClass metaClass) {
        getRealMetaObject().setMetaClass(metaClass);
    }

    public boolean setPrimaryKey(Object key) {
        return getRealMetaObject().setPrimaryKey(key);
    }

    public void setRenderer(String renderer) {
        getRealMetaObject().setRenderer(renderer);
    }

    public String toString(HashMap classes) {
        return getRealMetaObject().toString(classes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectAttributes  DOCUMENT ME!
     */
    public void addAllAttributes(ObjectAttribute[] objectAttributes) {
        getRealMetaObject().addAllAttributes(objectAttributes);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   anyAttribute  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void addAttribute(ObjectAttribute anyAttribute) throws Exception {
        getRealMetaObject().addAttribute(anyAttribute);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   m  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object constructKey(Mapable m) {
        return getRealMetaObject().constructKey(m);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Sirius.server.localserver.object.Object filter(UserGroup ug) throws Exception {
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
    public Object fromString(String objectRepresentation, Object mo) throws Exception {
        return getRealMetaObject().fromString(objectRepresentation, mo);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
    public Object getAttribute(Object key) {
        return getRealMetaObject().getAttribute(key);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ObjectAttribute getAttributeByFieldName(String fieldname) {
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
    public Collection<Attribute> getAttributeByName(String name, int maxResult) {
        return getRealMetaObject().getAttributeByName(name, maxResult);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
    public Collection getAttributesByName(Collection names) {
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
    public Collection getAttributesByType(Class c, int recursionDepth) {
        return getRealMetaObject().getAttributesByType(c, recursionDepth);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Collection getAttributesByType(Class c) {
        return getRealMetaObject().getAttributesByType(c);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getKey() {
        return getRealMetaObject().getKey();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Attribute getPrimaryKey() {
        return getRealMetaObject().getPrimaryKey();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public ObjectAttribute getReferencingObjectAttribute() {
        return getRealMetaObject().getReferencingObjectAttribute();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getStatus() {
        return getRealMetaObject().getStatus();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
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
    public Collection getTraversedAttributesByType(Class c) {
        return getRealMetaObject().getTraversedAttributesByType(c);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isDummy() {
        return getRealMetaObject().isDummy();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isPersistent() {
        return getRealMetaObject().isPersistent();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isStringCreateable() {
        return getRealMetaObject().isStringCreateable();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  anyAttribute  DOCUMENT ME!
     */
    public void removeAttribute(ObjectAttribute anyAttribute) {
        getRealMetaObject().removeAttribute(anyAttribute);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dummy  DOCUMENT ME!
     */
    public void setDummy(boolean dummy) {
        getRealMetaObject().setDummy(dummy);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  persistent  DOCUMENT ME!
     */
    public void setPersistent(boolean persistent) {
        getRealMetaObject().setPersistent(persistent);
    }

    /**
     * DOCUMENT ME!
     */
    public void setPrimaryKeysNull() {
        getRealMetaObject().setPrimaryKeysNull();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  referencingObjectAttribute  DOCUMENT ME!
     */
    public void setReferencingObjectAttribute(ObjectAttribute referencingObjectAttribute) {
        getRealMetaObject().setReferencingObjectAttribute(referencingObjectAttribute);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  status  DOCUMENT ME!
     */
    public void setStatus(int status) {
        getRealMetaObject().setStatus(status);
    }

    /**
     * DOCUMENT ME!
     */
    public void setValuesNull() {
        getRealMetaObject().setValuesNull();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public FromStringCreator getObjectCreator() {
        return getRealMetaObject().getObjectCreator();
    }
// </editor-fold>

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
    public LightweightMetaObject(
            int classID,
            int objectID,
            User user,
            Map<String, Object> attributesMap,
            AbstractAttributeRepresentationFormater formater) {
        this.classID = classID;
        this.objectID = objectID;
        this.user = user;
        this.metaService = null;
        this.attributesMap = Collections.unmodifiableMap(attributesMap);
        setFormater(formater);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  formater  DOCUMENT ME!
     */
    public void setFormater(AbstractAttributeRepresentationFormater formater) {
        if (formater != null) {
            formater.setAttributes(attributesMap);
            representation = formater.getRepresentation();
        } else {
            representation = "FORMATER IS NULL! (cID=" + classID + ", oID=" + objectID + ")";
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
                "Can not retrieve MetaObject, as Metaservice for LightweightMetaObject \"" + toString()
                + "\" is null!");
        }
        return metaService.getMetaObject(getUser(), getObjectID(), getClassID(), getUser().getDomain());
    }

    @Override
    public String toString() {
        return representation;
    }

    @Override
    public boolean equals(Object obj) {
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
    public int compareTo(LightweightMetaObject o) {
        return representation.compareTo(o + "");
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
    public void setMetaService(MetaService metaService) {
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
    public void setID(int objectID) {
        this.objectID = objectID;
        getRealMetaObject().setID(objectID);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getID() {
        return getObjectID();
    }

    public int getId() {
        return getObjectID();
    }

    public String getDomain() {
        return getUser().getDomain();
    }

    public String getClassKey() {
        return classID + "@" + getUser().getDomain();
    }
}
