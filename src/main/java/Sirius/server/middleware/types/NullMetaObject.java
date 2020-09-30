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
 * NullMetaObject.NULL is representing a metaobject that does not exist in the database. The LWMO-cache returns null
 * instead of trying to fetch the object if the cachehint equals the NullMetaObject.NULL.
 *
 * <p>If the LWMO cache would store "null", then there would be no way to know if the cachehit has been cleaned up (high
 * memory usage) and just needs to be refetched, or if the cachehit actually stores a null value.</p>
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class NullMetaObject implements MetaObject {

    //~ Static fields/initializers ---------------------------------------------

    private static final String EXCEPTION_MESSAGE =
        "The methods of NullMetaObject should never be invoked !!! The only purpose of this Class is to allow the LWMO-cache to represent null values.";
    public static final MetaObject NULL = new NullMetaObject();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NullMetaObject object.
     */
    private NullMetaObject() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public HashMap getAllClasses() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public CidsBean getBean() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getClassKey() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getComplexEditor() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getDebugString() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getDomain() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getEditor() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getGroup() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public int getId() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Logger getLogger() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public MetaClass getMetaClass() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getPropertyString() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getRenderer() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getSimpleEditor() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Collection getURLs(final Collection classKeys) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Collection getURLsByName(final Collection classKeys, final Collection urlNames) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean isChanged() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean hasObjectReadPermission(final User user) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean hasObjectWritePermission(final User user) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean propertyEquals(final MetaObject tester) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setAllClasses(final HashMap classes) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setAllClasses() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setAllStatus(final int status) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setArrayKey2PrimaryKey() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setChanged(final boolean changed) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setEditor(final String editor) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setMetaClass(final MetaClass metaClass) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setRenderer(final String renderer) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String toString(final HashMap classes) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void addAllAttributes(final ObjectAttribute[] objectAttributes) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void addAttribute(final ObjectAttribute anyAttribute) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Object constructKey(final Mapable m) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Object filter(final User ug) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public java.lang.Object fromString(final String objectRepresentation, final java.lang.Object mo) throws Exception {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public ObjectAttribute[] getAttribs() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public ObjectAttribute getAttribute(final String name) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public ObjectAttribute getAttributeByFieldName(final String fieldname) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Collection<ObjectAttribute> getAttributeByName(final String name, final int maxResult) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public LinkedHashMap<java.lang.Object, ObjectAttribute> getAttributes() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Collection<ObjectAttribute> getAttributesByName(final Collection names) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Collection<ObjectAttribute> getAttributesByType(final Class c, final int recursionDepth) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Collection<ObjectAttribute> getAttributesByType(final Class c) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public int getClassID() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public int getID() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public java.lang.Object getKey() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public ObjectAttribute getPrimaryKey() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public ObjectAttribute getReferencingObjectAttribute() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public int getStatus() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public String getStatusDebugString() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public Collection<ObjectAttribute> getTraversedAttributesByType(final Class c) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean isDummy() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean isPersistent() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean isStringCreateable() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void removeAttribute(final ObjectAttribute anyAttribute) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setDummy(final boolean dummy) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setID(final int objectID) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setPersistent(final boolean persistent) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setPrimaryKeysNull() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setReferencingObjectAttribute(final ObjectAttribute referencingObjectAttribute) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setStatus(final int status) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void forceStatus(final int status) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public void setValuesNull() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public FromStringCreator getObjectCreator() {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }

    @Override
    public boolean setPrimaryKey(final java.lang.Object key) {
        throw new UnsupportedOperationException(EXCEPTION_MESSAGE); // To change body of generated methods, choose
                                                                    // Tools | Templates.
    }
}
