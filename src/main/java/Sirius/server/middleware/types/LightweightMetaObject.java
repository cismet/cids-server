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
 *
 * @author srichter
 */
public final class LightweightMetaObject implements MetaObject, Comparable<LightweightMetaObject> {

    private transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    public LightweightMetaObject(int classID, int objectID, User user, Map<String, Object> attributesMap, AbstractAttributeRepresentationFormater formater) {
        this.classID = classID;
        this.objectID = objectID;
        this.user = user;
        this.metaService = null;
        this.attributesMap = Collections.unmodifiableMap(attributesMap);
        setFormater(formater);
    }
    //use volantile variable to fix "double checked locking" problem!
    private volatile transient MetaObject lazyMetaObject;
    private transient MetaService metaService;
    private final Map<String, Object> attributesMap;
    private final int classID;
    private final User user;
    private int objectID;
    private String representation;

    public void setFormater(AbstractAttributeRepresentationFormater formater) {
        if (formater != null) {
            formater.setAttributes(attributesMap);
            representation = formater.getRepresentation();
        } else {
            representation = "FORMATER IS NULL! (cID=" + classID + ", oID=" + objectID + ")";
        }
    }

    public final Object getAttribute(final String aName) {
        return attributesMap.get(aName);
    }

    public Set<String> getKnownAttributeNames() {
        return attributesMap.keySet();
    }

    private MetaObject fetchRealMetaObject() throws Exception {
        if (metaService == null) {
            throw new IllegalStateException("Can not retrieve MetaObject, as Metaservice for LightweightMetaObject \"" + toString() + "\" is null!");
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
            final MetaObject tmp = (MetaObject) obj;
            //debug:
//            if ((getClassID() == tmp.getClassID()) && (getID() == tmp.getID()) && getDomain().equals(tmp.getDomain()) != equals(obj)) {
//                log.fatal("Different Equals: " + toString() + "\n VS \n" + obj);
//            }
            return (getClassID() == tmp.getClassID()) && (getObjectID() == tmp.getID()) && getDomain().equals(tmp.getDomain());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + this.getClassID();
        hash = 11 * hash + this.getObjectID();
        hash = 11 * hash + this.getDomain().hashCode();
        return hash;
    }

    /**
     * @return the classID
     */
    public int getClassID() {
        return classID;
    }

    /**
     * @return the objectID
     */
    public int getObjectID() {
        return objectID;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    /**
     * 
     * @param o
     * @return
     */
    public int compareTo(LightweightMetaObject o) {
        return representation.compareTo(o + "");
    }

    /**
     * @return the metaService
     */
    public MetaService getMetaService() {
        return metaService;
    }

    /**
     * @param metaService the metaService to set
     */
    public void setMetaService(MetaService metaService) {
        this.metaService = metaService;
    }

    /**
     * Lazy loads for the real MetaObject if needed, the returns it.
     * 
     * @return the real MetaObject which the LWMetaObject is a proxy for.
     */
    public final MetaObject getRealMetaObject() {
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

    public void setID(int objectID) {
        this.objectID = objectID;
        getRealMetaObject().setID(objectID);
    }

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

    public void addAllAttributes(ObjectAttribute[] objectAttributes) {
        getRealMetaObject().addAllAttributes(objectAttributes);
    }

    public void addAttribute(ObjectAttribute anyAttribute) throws Exception {
        getRealMetaObject().addAttribute(anyAttribute);
    }

    public Object constructKey(Mapable m) {
        return getRealMetaObject().constructKey(m);
    }

    public Sirius.server.localserver.object.Object filter(UserGroup ug) throws Exception {
        return getRealMetaObject().filter(ug);
    }

    public Object fromString(String objectRepresentation, Object mo) throws Exception {
        return getRealMetaObject().fromString(objectRepresentation, mo);
    }

    public ObjectAttribute[] getAttribs() {
        return getRealMetaObject().getAttribs();
    }

    public Object getAttribute(Object key) {
        return getRealMetaObject().getAttribute(key);
    }

    public ObjectAttribute getAttributeByFieldName(String fieldname) {
        return getRealMetaObject().getAttributeByFieldName(fieldname);
    }

    public Collection<Attribute> getAttributeByName(String name, int maxResult) {
        return getRealMetaObject().getAttributeByName(name, maxResult);
    }

    public HashMap getAttributes() {
        return getRealMetaObject().getAttributes();
    }

    public Collection getAttributesByName(Collection names) {
        return getRealMetaObject().getAttributesByName(names);
    }

    public Collection getAttributesByType(Class c, int recursionDepth) {
        return getRealMetaObject().getAttributesByType(c, recursionDepth);
    }

    public Collection getAttributesByType(Class c) {
        return getRealMetaObject().getAttributesByType(c);
    }

    public Object getKey() {
        return getRealMetaObject().getKey();
    }

    public Attribute getPrimaryKey() {
        return getRealMetaObject().getPrimaryKey();
    }

    public ObjectAttribute getReferencingObjectAttribute() {
        return getRealMetaObject().getReferencingObjectAttribute();
    }

    public int getStatus() {
        return getRealMetaObject().getStatus();
    }

    public String getStatusDebugString() {
        return getRealMetaObject().getStatusDebugString();
    }

    public Collection getTraversedAttributesByType(Class c) {
        return getRealMetaObject().getTraversedAttributesByType(c);
    }

    public boolean isDummy() {
        return getRealMetaObject().isDummy();
    }

    public boolean isPersistent() {
        return getRealMetaObject().isPersistent();
    }

    public boolean isStringCreateable() {
        return getRealMetaObject().isStringCreateable();
    }

    public void removeAttribute(ObjectAttribute anyAttribute) {
        getRealMetaObject().removeAttribute(anyAttribute);
    }

    public void setDummy(boolean dummy) {
        getRealMetaObject().setDummy(dummy);
    }

    public void setPersistent(boolean persistent) {
        getRealMetaObject().setPersistent(persistent);
    }

    public void setPrimaryKeysNull() {
        getRealMetaObject().setPrimaryKeysNull();
    }

    public void setReferencingObjectAttribute(ObjectAttribute referencingObjectAttribute) {
        getRealMetaObject().setReferencingObjectAttribute(referencingObjectAttribute);
    }

    public void setStatus(int status) {
        getRealMetaObject().setStatus(status);
    }

    public void setValuesNull() {
        getRealMetaObject().setValuesNull();
    }

    public FromStringCreator getObjectCreator() {
        return getRealMetaObject().getObjectCreator();
    }
// </editor-fold>
}
