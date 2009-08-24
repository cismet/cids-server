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
import de.cismet.cids.utils.CidsBeanPersistService;
import de.cismet.cids.utils.MetaClassCacheService;
import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import org.apache.commons.beanutils.PropertyUtils;
import org.jdesktop.observablecollections.ObservableList;
import org.openide.util.Lookup;

/**
 *
 * @author hell
 */
public class CidsBean implements PropertyChangeListener {

    public static CidsBean constructNew(MetaService meta, User u, String domain, String tableName) throws Exception {
        MetaClass mc = meta.getClassByTableName(u, tableName, domain);
        MetaObject mo = mc.getEmptyInstance();
        return mo.getBean();
    }
    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected MetaObject metaObject = null;
    protected String backlinkFieldname;
    protected CidsBean backlinkObject;

    public MetaObject getMetaObject() {
        return metaObject;
    }

    public boolean getHasWritePermission(User user) {
        return metaObject.getMetaClass().getPermissions().hasWritePermission(user.getUserGroup());
    }

    public void setMetaObject(MetaObject metaObject) {
        this.metaObject = metaObject;
    }

    public String getMOString() {
        return metaObject.getDebugString();
    }

    @Override
    public String toString() {
        String ret = metaObject.toString();
        //log.fatal("ToString von CIDSBEAN: "+ret);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CidsBean other = (CidsBean) obj;

        return other.metaObject.getID() == metaObject.getID() && other.metaObject.getMetaClass().getID() == metaObject.getMetaClass().getID();
    }

    @Override
    public int hashCode() {
        String s = metaObject.getID() + "." + metaObject.getMetaClass().getID() + "." + metaObject.getDomain();
        return s.hashCode();
    }

    public String toObjectString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    public CidsBean persist(MetaService metaService, User user, String domain) throws Exception {
        if (metaObject.getStatus() == MetaObject.MODIFIED) {
            metaService.updateMetaObject(user, metaObject, domain);
            return metaService.getMetaObject(user, metaObject.getID(), metaObject.getClassID(), domain).getBean();
        } else if (metaObject.getStatus() == MetaObject.TO_DELETE) {
            metaService.deleteMetaObject(user, metaObject, domain);
            return null;
        } else if (metaObject.getStatus() == MetaObject.NEW) {
            MetaObject mo = metaService.insertMetaObject(user, metaObject, domain);
            if (mo != null) {
                return mo.getBean();
            }
        }
        return null;
    }

    public CidsBean persist() throws Exception {
        CidsBeanPersistService persistService = Lookup.getDefault().lookup(CidsBeanPersistService.class);
        if (persistService != null) {
            return persistService.persistCidsBean(this);
        }
        return null;
    }

    public CidsBean addNewArrayElement(String property) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public CidsBean addArrayElement(String property, CidsBean arrayElement) {
        throw new UnsupportedOperationException("Not yet implemented.");
    }

    public void setBacklinkInformation(String fieldname, CidsBean parentObject) {
        backlinkFieldname = fieldname;
        backlinkObject = parentObject;
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *   	and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String field = evt.getPropertyName();



        ObjectAttribute oa = metaObject.getAttributeByFieldName(field);
        Object oldValue = oa.getValue();
        Object value = evt.getNewValue();
        if (oa.referencesObject() && value instanceof CidsBean && value != null) {
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
            CidsBean cbv = (CidsBean) value;
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

        log.debug("a property changed:" + metaObject.getDebugString());
        if ((oldValue == null && value != null) || oldValue != null && !oldValue.equals(value)) {
            oa.setChanged(true);
            metaObject.setStatus(MetaObject.MODIFIED);

            ObjectAttribute referencingOA = metaObject.getReferencingObjectAttribute();
            walkUpAndSetChangedAndModified(referencingOA);
        } else {
            log.info("set with the same value. no status change required (" + field + ":" + value + ")");
        }
    }

    private void walkUpAndSetChangedAndModified(ObjectAttribute referencingOA) {
        while (referencingOA != null) {
            referencingOA.setChanged(true);
            Sirius.server.localserver.object.Object parent = referencingOA.getParentObject();
            parent.setStatus(MetaObject.MODIFIED); //funzt jetzt weil beim Erzeugen der Bean nochmals gesetzt (funzt nicht weil �ber den MetaObject Konstruktor eine neue Adresse genutzt wird. Der andere Kram funktioniert aber, da die gleichen ObjectAttributes genutzt werden.)
            referencingOA = parent.getReferencingObjectAttribute();
        }
    }

    /**
     * call this method to delete the subobject and remove all the references
     * it will not delete subobjects
     * @throws java.lang.Exception
     */
    public void delete() throws Exception {
        metaObject.setStatus(MetaObject.TO_DELETE);
        if (backlinkObject != null) {

            ObjectAttribute backlinkOA = backlinkObject.getMetaObject().getAttributeByFieldName(backlinkFieldname);
            walkUpAndSetChangedAndModified(backlinkOA);

            Object o = PropertyUtils.getProperty(backlinkObject, backlinkFieldname);
            if (o instanceof CidsBean) {
                PropertyUtils.setProperty(backlinkObject, backlinkFieldname, null);
            } else if (o instanceof ObservableList) {
                ((ObservableList) o).remove(this);
            }
        }

    }

    public void fillEmptyFieldWithEmptySubInstance(final String name) throws Exception {
        ObjectAttribute oa = getMetaObject().getAttributeByFieldName(name);
        MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
        if (classCacheService != null) {
            MetaClass mc = classCacheService.getMetaClass(getMetaObject().getDomain(), oa.getMai().getForeignKeyClassId());
            CidsBean newOne = mc.getEmptyInstance().getBean();
            setProperty(name, newOne);
        } else {
            throw new RuntimeException("Could not lookup MetaClassCacheService");
        }
    }

    /**
     * Convenience Method. Wraps <code>PropertyUtils.setProperty(this, name, value);</code>
     * @param name
     * @param value
     * @throws java.lang.Exception
     */
    public void setProperty(final String name, final Object value) throws Exception {
        try {
            //TODO seems to call nonexisting properties on array classes?
            PropertyUtils.setProperty(this, name, value);
        } catch (Exception e) {
            log.error("Fehler in setProperty:" + name + "\n", e);
        }
    }

    /**
     * Convenience Method. Wraps <code>PropertyUtils.getProperty(this, name);</code>
     * @param name
     * @return
     * @throws java.lang.Exception
     */
    public Object getProperty(final String name) {
        try {
            return PropertyUtils.getProperty(this, name);
        } catch (Exception e) {
            log.error("Fehler in getproperty:" + name, e);
        }
        return null;
    }

    //Es folgen Methoden des Interfaces ObservableListListener. Das Objekt kann
    //diesen Listener allerdings nicht implementieren, da es dann Probleme mit
    //RMI gibt (UnmarshallException mit einer EOFException - ???)
    //
    //In der BeanFactory wird deshalb ein Listener beim Anlegen der Liste
    //lokal, anonym implementiert und registriert. Dieser Listener macht nichts
    //anderes, als untenstehende Methoden aufzurufen.
    //
    //Tsssssssssssssss
    /**
     * Notification that elements have been added to the list.
     *
     * @param arrayfield
     * @param list the {@code ObservableList} that has changed
     * @param index the index the elements were added to
     * @param length the number of elements that were added
     */
    public void listElementsAdded(String arrayfield, ObservableList list, int index, int length) {
        for (int i = index; i < index + length; ++i) {
            try {
                Object o = list.get(i);
                if (arrayfield != null) {
                    if (o instanceof CidsBean) {
                        CidsBean cb = (CidsBean) o;
                        cb.setBacklinkInformation(arrayfield, this);
                        ObjectAttribute oa = this.getMetaObject().getAttributeByFieldName(arrayfield);

                        walkUpAndSetChangedAndModified(oa);
                        //ArrayElement anlegen
                        MetaClass zwischenTabellenKlasse = (MetaClass) ((Hashtable) getMetaObject().getAllClasses()).get(getMetaObject().getDomain() + oa.getMai().getForeignKeyClassId());
                        MetaObject arrayElement = zwischenTabellenKlasse.getEmptyInstance();

                        ObjectAttribute[] arrayElementAttrs = arrayElement.getAttribs();
                        for (ObjectAttribute arrayElementAttribute : arrayElementAttrs) {
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

                        //Wen noch kein Dummy-Objekt existiert (Wert ist noch null)
                        //Anlegen eines Dummy-Objektes
                        if (oa.getValue() == null) {
                            Sirius.server.localserver.object.Object dummyO = new Sirius.server.localserver.object.DefaultObject(getMetaObject().getID(), oa.getMai().getForeignKeyClassId());
                            MetaObject dummyMO = new DefaultMetaObject(dummyO, getMetaObject().getDomain());
                            dummyMO.setReferencingObjectAttribute(oa);
                            dummyMO.setDummy(true);
                            dummyMO.setStatus(MetaObject.NEW);
                            oa.setValue(dummyMO);
                            oa.setChanged(true);
                        }

                        //hinzufuegen eines Attributes, das auf das angelegte Arrayelement zeigt
                        MetaObject dummy = (MetaObject) oa.getValue();
                        dummy.setStatus(MetaObject.MODIFIED);
                        int counter = dummy.getAttribs().length;
                        //MAI des ArrayFeldes des Hauptobjektes
                        MemberAttributeInfo mai = oa.getMai();
                        ObjectAttribute dummyOA = new ObjectAttribute(mai.getId() + "." + ++counter, mai, -1, arrayElement, zwischenTabellenKlasse.getAttributePolicy());
                        dummyOA.setParentObject(dummy);
                        dummyOA.setChanged(true);
                        dummy.addAttribute(dummyOA);
                        arrayElement.setReferencingObjectAttribute(dummyOA);

                    } else {
                        throw new IllegalArgumentException("Every element of an array must be a CidsBean");
                    }
                } else {
                    throw new IllegalArgumentException("ObservableList is not registered as Array");
                }
            } catch (Exception e) {
                log.error("Fehler in listElementsAdded", e);
            }
        }
    }

    /**
     * Notification that elements have been removed from the list.
     *
     * @param list the {@code ObservableList} that has changed
     * @param index the starting index the elements were removed from
     * @param oldElements a list containing the elements that were removed.
     */
    public void listElementsRemoved(String arrayfield, ObservableList list, int index,
            List oldElements) {
        for (Object element : oldElements) {
            CidsBean cidsBean = (CidsBean) element;
            ObjectAttribute deepestReferencingAttribute = cidsBean.getMetaObject().getReferencingObjectAttribute();
            if (cidsBean.getMetaObject().getStatus() == MetaObject.TO_DELETE || cidsBean.getMetaObject().getStatus() == MetaObject.MODIFIED) {
                deepestReferencingAttribute.setChanged(true);
            } else if (cidsBean.getMetaObject().getStatus() == MetaObject.NEW) {
                //wurde gerade erst angelegt, braucht nur entfernt zu werden
                deepestReferencingAttribute.setValue(null);
            }
            Sirius.server.localserver.object.Object arrayEntry = deepestReferencingAttribute.getParentObject();
            if (arrayEntry.getStatus() == MetaObject.NEW) {
                //wurde gerade erst angelegt, braucht nur entfernt zu werden
                ObjectAttribute toDelete = arrayEntry.getReferencingObjectAttribute();
                toDelete.getParentObject().removeAttribute(toDelete);
                //toDelete.getKey();
            } else if (arrayEntry.getStatus() != MetaObject.TEMPLATE || arrayEntry.getStatus() != MetaObject.TEMPLATE) {
                arrayEntry.setStatus(MetaObject.TO_DELETE);
                ObjectAttribute referencingOA = arrayEntry.getReferencingObjectAttribute();
                walkUpAndSetChangedAndModified(referencingOA);
            }
            //log.fatal(this.getMOString());

        }
        getMetaObject().setStatus(MetaObject.MODIFIED);
    }

    /**
     * Notification that an element has been replaced by another in the list.
     *
     * @param list the {@code ObservableList} that has changed
     * @param index the index of the element that was replaced
     * @param oldElement the element at the index before the change
     */
    public void listElementReplaced(String arrayfield, ObservableList list, int index,
            Object oldElement) {
    }

    /**
     * Notification than a property of an element in this list has changed.
     * Not all {@code ObservableLists} support this notification. Only
     * observable lists that return {@code true} from
     * {@code supportsElementPropertyChanged} send this notification.
     *
     * @param list the {@code ObservableList} that has changed
     * @param index the index of the element that changed
     */
    public void listElementPropertyChanged(String arrayfield, ObservableList list, int index) {
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor pd = new PropertyDescriptor("MOString", CidsBean.class);
            return new PropertyDescriptor[]{pd};
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }
}
