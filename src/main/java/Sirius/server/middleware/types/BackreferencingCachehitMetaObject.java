/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.localserver.object.Object;
import Sirius.server.newuser.User;

import Sirius.util.Mapable;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.tools.fromstring.FromStringCreator;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class BackreferencingCachehitMetaObject implements MetaObject {

    //~ Instance fields --------------------------------------------------------

    private transient CidsBean bean;
    private ObjectAttribute referencingObjectAttribute;
    private final MetaObject metaObject;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new BackreferencingCachehitMetaObject object.
     *
     * @param  metaObject                  DOCUMENT ME!
     * @param  referencingObjectAttribute  DOCUMENT ME!
     */
    public BackreferencingCachehitMetaObject(final MetaObject metaObject,
            final ObjectAttribute referencingObjectAttribute) {
        this.metaObject = metaObject;
        this.referencingObjectAttribute = referencingObjectAttribute;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public HashMap getAllClasses() {
        return metaObject.getAllClasses();
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
    public String getClassKey() {
        return metaObject.getClassKey();
    }

    @Override
    public String getComplexEditor() {
        return metaObject.getComplexEditor();
    }

    @Override
    public String getDebugString() {
        return metaObject.getDebugString();
    }

    @Override
    public String getDescription() {
        return metaObject.getDescription();
    }

    @Override
    public String getDomain() {
        return metaObject.getDomain();
    }

    @Override
    public String getEditor() {
        return metaObject.getEditor();
    }

    @Override
    public String getGroup() {
        return metaObject.getGroup();
    }

    @Override
    public int getId() {
        return metaObject.getId();
    }

    @Override
    public Logger getLogger() {
        return metaObject.getLogger();
    }

    @Override
    public MetaClass getMetaClass() {
        return metaObject.getMetaClass();
    }

    @Override
    public String getName() {
        return metaObject.getName();
    }

    @Override
    public String getPropertyString() {
        return metaObject.getPropertyString();
    }

    @Override
    public String getRenderer() {
        return metaObject.getRenderer();
    }

    @Override
    public String getSimpleEditor() {
        return metaObject.getSimpleEditor();
    }

    @Override
    public Collection getURLs(final Collection classKeys) {
        return metaObject.getURLs(classKeys);
    }

    @Override
    public Collection getURLsByName(final Collection classKeys, final Collection urlNames) {
        return metaObject.getURLsByName(classKeys, urlNames);
    }

    @Override
    public boolean isChanged() {
        return metaObject.isChanged();
    }

    @Override
    public boolean hasObjectReadPermission(final User user) {
        return metaObject.hasObjectReadPermission(user);
    }

    @Override
    public boolean hasObjectWritePermission(final User user) {
        return metaObject.hasObjectWritePermission(user);
    }

    @Override
    public boolean propertyEquals(final MetaObject tester) {
        return metaObject.propertyEquals(tester);
    }

    @Override
    public void setAllClasses(final HashMap classes) {
        metaObject.setAllClasses(classes);
    }

    @Override
    public void setAllClasses() {
        metaObject.setAllClasses();
    }

    @Override
    public void setAllStatus(final int status) {
        metaObject.setAllStatus(status);
    }

    @Override
    public void setArrayKey2PrimaryKey() {
        metaObject.setArrayKey2PrimaryKey();
    }

    @Override
    public void setChanged(final boolean changed) {
        metaObject.setChanged(changed);
    }

    @Override
    public void setEditor(final String editor) {
        metaObject.setEditor(editor);
    }

    @Override
    public void setMetaClass(final MetaClass metaClass) {
        metaObject.setMetaClass(metaClass);
    }

    @Override
    public boolean setPrimaryKey(final java.lang.Object key) {
        return metaObject.setPrimaryKey(key);
    }

    @Override
    public void setRenderer(final String renderer) {
        metaObject.setRenderer(renderer);
    }

    @Override
    public String toString(final HashMap classes) {
        return metaObject.toString(classes);
    }

    @Override
    public String toString() {
        return metaObject.toString();
    }

    @Override
    public int hashCode() {
        return metaObject.hashCode();
    }

    @Override
    public boolean equals(final java.lang.Object obj) {
        return metaObject.equals(obj);
    }

    @Override
    public void addAllAttributes(final ObjectAttribute[] objectAttributes) {
        metaObject.addAllAttributes(objectAttributes);
    }

    @Override
    public void addAttribute(final ObjectAttribute anyAttribute) {
        metaObject.addAttribute(anyAttribute);
    }

    @Override
    public java.lang.Object constructKey(final Mapable m) {
        return metaObject.constructKey(m);
    }

    @Override
    public Object filter(final User ug) {
        return metaObject.filter(ug);
    }

    @Override
    public java.lang.Object fromString(final String objectRepresentation, final java.lang.Object mo) throws Exception {
        return metaObject.fromString(objectRepresentation, mo);
    }

    @Override
    public ObjectAttribute[] getAttribs() {
        return metaObject.getAttribs();
    }

    @Override
    public ObjectAttribute getAttribute(final String name) {
        return metaObject.getAttribute(name);
    }

    @Override
    public ObjectAttribute getAttributeByFieldName(final String fieldname) {
        return metaObject.getAttributeByFieldName(fieldname);
    }

    @Override
    public Collection<ObjectAttribute> getAttributeByName(final String name, final int maxResult) {
        return metaObject.getAttributeByName(name, maxResult);
    }

    @Override
    public LinkedHashMap<java.lang.Object, ObjectAttribute> getAttributes() {
        return metaObject.getAttributes();
    }

    @Override
    public Collection<ObjectAttribute> getAttributesByName(final Collection names) {
        return metaObject.getAttributesByName(names);
    }

    @Override
    public Collection<ObjectAttribute> getAttributesByType(final Class c, final int recursionDepth) {
        return metaObject.getAttributesByType(c, recursionDepth);
    }

    @Override
    public Collection<ObjectAttribute> getAttributesByType(final Class c) {
        return metaObject.getAttributesByType(c);
    }

    @Override
    public int getClassID() {
        return metaObject.getClassID();
    }

    @Override
    public int getID() {
        return metaObject.getID();
    }

    @Override
    public java.lang.Object getKey() {
        return metaObject.getKey();
    }

    @Override
    public ObjectAttribute getPrimaryKey() {
        return metaObject.getPrimaryKey();
    }

    @Override
    public ObjectAttribute getReferencingObjectAttribute() {
        return referencingObjectAttribute;
    }

    @Override
    public int getStatus() {
        return metaObject.getStatus();
    }

    @Override
    public String getStatusDebugString() {
        return metaObject.getStatusDebugString();
    }

    @Override
    public Collection<ObjectAttribute> getTraversedAttributesByType(final Class c) {
        return metaObject.getTraversedAttributesByType(c);
    }

    @Override
    public boolean isDummy() {
        return metaObject.isDummy();
    }

    @Override
    public boolean isPersistent() {
        return metaObject.isPersistent();
    }

    @Override
    public boolean isStringCreateable() {
        return metaObject.isStringCreateable();
    }

    @Override
    public void removeAttribute(final ObjectAttribute anyAttribute) {
        metaObject.removeAttribute(anyAttribute);
    }

    @Override
    public void setDummy(final boolean dummy) {
        metaObject.setDummy(dummy);
    }

    @Override
    public void setID(final int objectID) {
        metaObject.setID(objectID);
    }

    @Override
    public void setPersistent(final boolean persistent) {
        metaObject.setPersistent(persistent);
    }

    @Override
    public void setPrimaryKeysNull() {
        metaObject.setPrimaryKeysNull();
    }

    @Override
    public void setReferencingObjectAttribute(final ObjectAttribute referencingObjectAttribute) {
        this.referencingObjectAttribute = referencingObjectAttribute;
    }

    @Override
    public void setStatus(final int status) {
        metaObject.setStatus(status);
    }

    @Override
    public void forceStatus(final int status) {
        metaObject.forceStatus(status);
    }

    @Override
    public void setValuesNull() {
        metaObject.setValuesNull();
    }

    @Override
    public FromStringCreator getObjectCreator() {
        return metaObject.getObjectCreator();
    }
}
