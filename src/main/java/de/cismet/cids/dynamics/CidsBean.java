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
package de.cismet.cids.dynamics;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import org.apache.commons.beanutils.PropertyUtils;

import org.jdesktop.observablecollections.ObservableList;

import org.openide.util.Lookup;

import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;

import java.util.HashMap;
import java.util.List;

import de.cismet.cids.utils.CidsBeanPersistService;
import de.cismet.cids.utils.MetaClassCacheService;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
public class CidsBean implements PropertyChangeListener {

    //~ Instance fields --------------------------------------------------------
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected MetaObject metaObject = null;
    protected String backlinkFieldname;
    protected CidsBean backlinkObject;
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Methods ----------------------------------------------------------------
    /**
     * DOCUMENT ME!
     *
     * @param   meta       DOCUMENT ME!
     * @param   u          DOCUMENT ME!
     * @param   domain     DOCUMENT ME!
     * @param   tableName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static CidsBean constructNew(final MetaService meta,
            final User u,
            final String domain,
            final String tableName) throws Exception {
        final MetaClass mc = meta.getClassByTableName(u, tableName, domain);
        final MetaObject mo = mc.getEmptyInstance();
        return mo.getBean();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public MetaObject getMetaObject() {
        return metaObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean getHasWritePermission(final User user) {
        return metaObject.getMetaClass().getPermissions().hasWritePermission(user.getUserGroup());
    }

    /**
     * DOCUMENT ME!
     *
     * @param  metaObject  DOCUMENT ME!
     */
    public void setMetaObject(final MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getMOString() {
        return metaObject.getDebugString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String toString() {
        final String ret = metaObject.toString();
        // log.fatal("ToString von CIDSBEAN: "+ret);
        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   obj  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CidsBean other = (CidsBean) obj;
        return metaObject.equals(other.metaObject);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int hashCode() {
        final String s = metaObject.getID() + "." + metaObject.getMetaClass().getID() + "." + metaObject.getDomain();//NOI18N
        return s.hashCode();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toObjectString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());//NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaService  DOCUMENT ME!
     * @param   user         DOCUMENT ME!
     * @param   domain       DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsBean persist(final MetaService metaService, final User user, final String domain) throws Exception {
        if (metaObject.getStatus() == MetaObject.MODIFIED) {
            metaService.updateMetaObject(user, metaObject, domain);
            return metaService.getMetaObject(user, metaObject.getID(), metaObject.getClassID(), domain).getBean();
        } else if (metaObject.getStatus() == MetaObject.TO_DELETE) {
            metaService.deleteMetaObject(user, metaObject, domain);
            return null;
        } else if (metaObject.getStatus() == MetaObject.NEW) {
            final MetaObject mo = metaService.insertMetaObject(user, metaObject, domain);
            if (mo != null) {
                return mo.getBean();
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsBean persist() throws Exception {
        final CidsBeanPersistService persistService = Lookup.getDefault().lookup(CidsBeanPersistService.class);
        if (persistService != null) {
            return persistService.persistCidsBean(this);
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   property  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public CidsBean addNewArrayElement(final String property) {
        throw new UnsupportedOperationException("Not yet implemented.");//NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   property      DOCUMENT ME!
     * @param   arrayElement  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  UnsupportedOperationException  DOCUMENT ME!
     */
    public CidsBean addArrayElement(final String property, final CidsBean arrayElement) {
        throw new UnsupportedOperationException("Not yet implemented.");//NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fieldname     DOCUMENT ME!
     * @param  parentObject  DOCUMENT ME!
     */
    public void setBacklinkInformation(final String fieldname, final CidsBean parentObject) {
        backlinkFieldname = fieldname;
        backlinkObject = parentObject;
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param  listener  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param  listener  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return propertyChangeSupport.getPropertyChangeListeners();
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param  evt  A PropertyChangeEvent object describing the event source and the property that has changed.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        final String field = evt.getPropertyName();

        final ObjectAttribute oa = metaObject.getAttributeByFieldName(field);
        final Object oldValue = oa.getValue();
        final Object value = evt.getNewValue();
        if (oa.referencesObject() && (value instanceof CidsBean) && (value != null)) {
//            if (value == null) {
//                MetaObject oldMO = (MetaObject) oa.getValue();
//
//                if (oldMO != null) {
//                    try {
//                        oldMO.getBean().delete();
//                    } catch (Exception ex) {
//                        throw new IllegalArgumentException("Value for " + field + " cannot be deleted", ex);
//                    }
//                }
//            } else if (value instanceof CidsBean) {
            final CidsBean cbv = (CidsBean) value;
            oa.setValue(cbv.getMetaObject());
            cbv.setBacklinkInformation(field, this);
            if (cbv.getMetaObject().getStatus() == MetaObject.TO_DELETE) {
                cbv.getMetaObject().setStatus(MetaObject.MODIFIED);
            }
//            }
//        else {
//                throw new IllegalArgumentException("Value for " + field + " must be a CidsBean");
//            }
        } else {
            oa.setValue(value);
        }
        if (log.isDebugEnabled()) {
            log.debug("a property changed:" + metaObject.getDebugString());//NOI18N
        }
        if (((oldValue == null) && (value != null)) || ((oldValue != null) && !oldValue.equals(value))) {
            oa.setChanged(true);
            metaObject.setStatus(MetaObject.MODIFIED);

            final ObjectAttribute referencingOA = metaObject.getReferencingObjectAttribute();
            walkUpAndSetChangedAndModified(referencingOA);
        } else {
            log.info("set with the same value. no status change required (" + field + ":" + value + ")");//NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  referencingOA  DOCUMENT ME!
     */
    private void walkUpAndSetChangedAndModified(ObjectAttribute referencingOA) {
        while (referencingOA != null) {
            referencingOA.setChanged(true);
            final Sirius.server.localserver.object.Object parent = referencingOA.getParentObject();
            parent.setStatus(MetaObject.MODIFIED); // funzt jetzt weil beim Erzeugen der Bean nochmals gesetzt (funzt
            // nicht weil �ber den MetaObject Konstruktor eine neue Adresse
            // genutzt wird. Der andere Kram funktioniert aber, da die
            // gleichen ObjectAttributes genutzt werden.)
            referencingOA = parent.getReferencingObjectAttribute();
        }
    }

    /**
     * call this method to delete the subobject and remove all the references it will not delete subobjects of the
     * object itself.
     *
     * @throws  Exception  java.lang.Exception
     */
    public void delete() throws Exception {
        metaObject.setStatus(MetaObject.TO_DELETE);
        metaObject.setChanged(true);
        if (backlinkObject != null) {
            final ObjectAttribute backlinkOA = backlinkObject.getMetaObject().getAttributeByFieldName(backlinkFieldname);
            walkUpAndSetChangedAndModified(backlinkOA);

            final Object o = PropertyUtils.getProperty(backlinkObject, backlinkFieldname);
            if (o instanceof CidsBean) {
                PropertyUtils.setProperty(backlinkObject, backlinkFieldname, null);
            } else if (o instanceof ObservableList) {
                ((ObservableList) o).remove(this);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  DOCUMENT ME!
     *
     * @throws  Exception         DOCUMENT ME!
     * @throws  RuntimeException  DOCUMENT ME!
     */
    public void fillEmptyFieldWithEmptySubInstance(final String name) throws Exception {
        final ObjectAttribute oa = getMetaObject().getAttributeByFieldName(name);
        final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
        if (classCacheService != null) {
            final MetaClass mc = classCacheService.getMetaClass(
                    getMetaObject().getDomain(),
                    oa.getMai().getForeignKeyClassId());
            final CidsBean newOne = mc.getEmptyInstance().getBean();
            setProperty(name, newOne);
        } else {
            throw new RuntimeException("Could not lookup MetaClassCacheService");//NOI18N
        }
    }

    /**
     * Convenience Method. Wraps <code>PropertyUtils.setProperty(this, name, value);</code>
     *
     * @param   name   DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception  java.lang.Exception
     */
    public void setProperty(final String name, final Object value) throws Exception {
        try {
            // TODO seems to call nonexisting properties on array classes?
            PropertyUtils.setProperty(this, name, value);
        } catch (Exception e) {
            log.error("Fehler in setProperty:" + name + "\n", e);
        }
    }

    /**
     * Convenience Method. Wraps <code>PropertyUtils.getProperty(this, name);</code>
     *
     * @param   name  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getProperty(final String name) {
        try {
            return PropertyUtils.getProperty(this, name);
        } catch (Exception e) {
            log.error("Fehler in getproperty:" + name, e);//NOI18N
        }
        return null;
    }

    // Es folgen Methoden des Interfaces ObservableListListener. Das Objekt kann
    // diesen Listener allerdings nicht implementieren, da es dann Probleme mit
    // RMI gibt (UnmarshallException mit einer EOFException - ???)
    //
    // In der BeanFactory wird deshalb ein Listener beim Anlegen der Liste
    // lokal, anonym implementiert und registriert. Dieser Listener macht nichts
    // anderes, als untenstehende Methoden aufzurufen.
    //
    // Tsssssssssssssss
    /**
     * Notification that elements have been added to the list.
     *
     * @param  arrayfield  DOCUMENT ME!
     * @param  list        the {@code ObservableList} that has changed
     * @param  index       the index the elements were added to
     * @param  length      the number of elements that were added
     */
    public void listElementsAdded(final String arrayfield,
            final ObservableList list,
            final int index,
            final int length) {
        for (int i = index; i < (index + length); ++i) {
            try {
                final Object o = list.get(i);
                if (arrayfield != null) {
                    if (o instanceof CidsBean) {
                        final CidsBean cb = (CidsBean) o;
                        cb.setBacklinkInformation(arrayfield, this);
                        final ObjectAttribute oa = this.getMetaObject().getAttributeByFieldName(arrayfield);

                        walkUpAndSetChangedAndModified(oa);
                        // ArrayElement anlegen
                        final MetaClass zwischenTabellenKlasse = (MetaClass) (getMetaObject().getAllClasses()).get(
                                getMetaObject().getDomain()
                                + oa.getMai().getForeignKeyClassId());
                        final MetaObject arrayElement = zwischenTabellenKlasse.getEmptyInstance();

                        final ObjectAttribute[] arrayElementAttrs = arrayElement.getAttribs();
                        for (final ObjectAttribute arrayElementAttribute : arrayElementAttrs) {
                            arrayElementAttribute.setParentObject(arrayElement);
                            if (arrayElementAttribute.isPrimaryKey()) {
                                arrayElementAttribute.setValue(-1);
                            } else if (arrayElementAttribute.referencesObject()) {
                                arrayElementAttribute.setValue(cb.getMetaObject());
                                arrayElementAttribute.setChanged(true);
                                cb.getMetaObject().setReferencingObjectAttribute(arrayElementAttribute);
                            } else {
                                arrayElementAttribute.setValue(getMetaObject().getID());
                            }
                        }

                        // Wen noch kein Dummy-Objekt existiert (Wert ist noch null)
                        // Anlegen eines Dummy-Objektes
                        if (oa.getValue() == null) {
                            final Sirius.server.localserver.object.Object dummyO =
                                    new Sirius.server.localserver.object.DefaultObject(
                                    getMetaObject().getID(),
                                    oa.getMai().getForeignKeyClassId());
                            final MetaObject dummyMO = new DefaultMetaObject(dummyO, getMetaObject().getDomain());
                            dummyMO.setReferencingObjectAttribute(oa);
                            dummyMO.setDummy(true);
                            dummyMO.setStatus(MetaObject.NEW);
                            oa.setValue(dummyMO);
                            oa.setChanged(true);
                        }

                        // hinzufuegen eines Attributes, das auf das angelegte Arrayelement zeigt
                        final MetaObject dummy = (MetaObject) oa.getValue();
                        dummy.setStatus(MetaObject.MODIFIED);
                        int counter = dummy.getAttribs().length;
                        // MAI des ArrayFeldes des Hauptobjektes
                        final MemberAttributeInfo mai = oa.getMai();
                        final ObjectAttribute dummyOA = new ObjectAttribute(
                                mai.getId()
                                + "."
                                + ++counter,
                                mai,
                                -1,
                                arrayElement,
                                zwischenTabellenKlasse.getAttributePolicy());
                        dummyOA.setParentObject(dummy);
                        dummyOA.setChanged(true);
                        dummy.addAttribute(dummyOA);
                        arrayElement.setReferencingObjectAttribute(dummyOA);
                    } else {
                        throw new IllegalArgumentException("Every element of an array must be a CidsBean");//NOI18N
                    }
                } else {
                    throw new IllegalArgumentException("ObservableList is not registered as Array");//NOI18N
                }
            } catch (Exception e) {
                log.error("Fehler in listElementsAdded", e);//NOI18N
            }
        }
    }

    /**
     * Notification that elements have been removed from the list.
     *
     * @param  arrayfield   DOCUMENT ME!
     * @param  list         the {@code ObservableList} that has changed
     * @param  index        the starting index the elements were removed from
     * @param  oldElements  a list containing the elements that were removed.
     */
    public void listElementsRemoved(final String arrayfield,
            final ObservableList list,
            final int index,
            final List oldElements) {
        for (final Object element : oldElements) {
            final CidsBean cidsBean = (CidsBean) element;
            final ObjectAttribute deepestReferencingAttribute = cidsBean.getMetaObject().getReferencingObjectAttribute();
            if ((cidsBean.getMetaObject().getStatus() == MetaObject.TO_DELETE)
                    || (cidsBean.getMetaObject().getStatus() == MetaObject.MODIFIED)) {
                deepestReferencingAttribute.setChanged(true);
            } else if (cidsBean.getMetaObject().getStatus() == MetaObject.NEW) {
                // wurde gerade erst angelegt, braucht nur entfernt zu werden
                deepestReferencingAttribute.setValue(null);
            }
            final Sirius.server.localserver.object.Object arrayEntry = deepestReferencingAttribute.getParentObject();
            if (arrayEntry.getStatus() == MetaObject.NEW) {
                // wurde gerade erst angelegt, braucht nur entfernt zu werden
                final ObjectAttribute toDelete = arrayEntry.getReferencingObjectAttribute();
                toDelete.getParentObject().removeAttribute(toDelete);
                // toDelete.getKey();
            } else if ((arrayEntry.getStatus() != MetaObject.TEMPLATE)
                    || (arrayEntry.getStatus() != MetaObject.TEMPLATE)) {
                arrayEntry.setStatus(MetaObject.TO_DELETE);
                final ObjectAttribute referencingOA = arrayEntry.getReferencingObjectAttribute();
                walkUpAndSetChangedAndModified(referencingOA);
            }
            // log.fatal(this.getMOString());
        }
        getMetaObject().setStatus(MetaObject.MODIFIED);
    }

    /**
     * Notification that an element has been replaced by another in the list.
     *
     * @param  arrayfield  DOCUMENT ME!
     * @param  list        the {@code ObservableList} that has changed
     * @param  index       the index of the element that was replaced
     * @param  oldElement  the element at the index before the change
     */
    public void listElementReplaced(final String arrayfield,
            final ObservableList list,
            final int index,
            final Object oldElement) {
    }

    /**
     * Notification than a property of an element in this list has changed. Not all {@code ObservableLists} support this
     * notification. Only observable lists that return {@code true} from {@code supportsElementPropertyChanged} send
     * this notification.
     *
     * @param  arrayfield  DOCUMENT ME!
     * @param  list        the {@code ObservableList} that has changed
     * @param  index       the index of the element that changed
     */
    public void listElementPropertyChanged(final String arrayfield, final ObservableList list, final int index) {
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getPropertyNames() {
        // to be overridden by the dynamic class
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Error  DOCUMENT ME!
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            final PropertyDescriptor pd = new PropertyDescriptor("MOString", CidsBean.class);//NOI18N
            return new PropertyDescriptor[]{pd};
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
