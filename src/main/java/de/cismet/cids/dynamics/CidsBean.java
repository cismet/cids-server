/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.dynamics;

import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.middleware.types.DefaultMetaObject;
import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.User;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.log4j.Logger;

import org.jdesktop.observablecollections.ObservableList;

import org.openide.util.Lookup;

import java.beans.IntrospectionException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.cismet.cids.json.IntraObjectCacheJsonParams;

import de.cismet.cids.utils.CidsBeanPersistService;
import de.cismet.cids.utils.ClassloadingHelper;
import de.cismet.cids.utils.MetaClassCacheService;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextProvider;
import de.cismet.connectioncontext.ConnectionContextStore;

import static de.cismet.cids.dynamics.CidsBean.mapper;

/**
 * DOCUMENT ME!
 *
 * @author   hell
 * @version  $Revision$, $Date$
 */
//@JsonSerialize(using = CidsBeanJsonSerializer.class)
//@JsonDeserialize(using = CidsBeanJsonDeserializer.class)
public class CidsBean implements PropertyChangeListener, ConnectionContextProvider {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(CidsBean.class);
    static final ObjectMapper mapper = new ObjectMapper();
    static final ObjectMapper intraObjectCacheMapper = new ObjectMapper();

    /**
     * DOCUMENT ME!
     *
     * @param   bean   DOCUMENT ME!
     * @param   field  DOCUMENT ME!
     * @param   n      DOCUMENT ME!
     *
     * @throws  Exception         DOCUMENT ME!
     * @throws  RuntimeException  DOCUMENT ME!
     */
    static JsonFactory fac = new JsonFactory();

    static {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        final SimpleModule regularModule = new SimpleModule("NOIOC", new Version(1, 0, 0, null, null, null));
        regularModule.addSerializer(new CidsBeanJsonSerializer());
        regularModule.addDeserializer(CidsBean.class, new CidsBeanJsonDeserializer());
        mapper.registerModule(regularModule);

        intraObjectCacheMapper.enable(SerializationFeature.INDENT_OUTPUT);
        final SimpleModule intraObjectCacheModule = new SimpleModule("IOC", new Version(1, 0, 0, null, null, null));
        intraObjectCacheModule.addSerializer(new IntraObjectCacheEnabledCidsBeanJsonSerializer());
        intraObjectCacheModule.addDeserializer(CidsBean.class, new IntraObjectCacheEnabledCidsBeanJsonDeserializer());
        intraObjectCacheMapper.registerModule(intraObjectCacheModule);
    }

    //~ Instance fields --------------------------------------------------------

    protected PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    protected MetaObject metaObject = null;
    protected String backlinkFieldname;
    protected CidsBean backlinkObject;
    protected boolean artificialChange;
    protected transient IntraObjectCacheJsonParams jsonSerializerParams;
    String pkFieldName = null;
    HashMap<String, CidsBean> intraObjectCache = new HashMap<String, CidsBean>();

    private CustomBeanPermissionProvider customPermissionProvider;
    private final ConnectionContext dummyConnectionContext = ConnectionContext.createDummy();

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ObjectMapper getCidsBeanObjectMapper() {
        return mapper;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ObjectMapper getCidsBeanIntraObjectCacheMapper() {
        return intraObjectCacheMapper;
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
        return metaObject.getMetaClass().getPermissions().hasWritePermission(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasObjectWritePermission(final User user) {
        if (metaObject != null) {
            return metaObject.hasObjectWritePermission(user);
        } else {
            LOG.error("meta object is null. The write permission cannot be determined.");
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasObjectReadPermission(final User user) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasObjectReadPermission for user: " + user); // NOI18N
        }

        if (customPermissionProvider == null) {
            try {
                final Class cpp = ClassloadingHelper.getDynamicClass(getMetaObject().getMetaClass(),
                        ClassloadingHelper.CLASS_TYPE.PERMISSION_PROVIDER);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("custom read permission provider retrieval result: " + cpp); // NOI18N
                }

                if (cpp == null) {
                    return true;
                }

                customPermissionProvider = (CustomBeanPermissionProvider)cpp.getConstructor().newInstance();
                customPermissionProvider.setCidsBean(this);
            } catch (Exception ex) {
                // FIXME: probably this behaviour is error prone since we allow write permission if there is a problem
                // with the loading of the custom permission provider, which probably would say "NO" if it was loaded
                // correctly
                LOG.warn("error during creation of custom permission provider", ex); // NOI18N
            }
        }

        if (customPermissionProvider != null) {
            return customPermissionProvider.getCustomReadPermissionDecisionforUser(user);
        } else {
            return true;
        }
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
        final CidsBean other = (CidsBean)obj;

        return metaObject.equals(other.metaObject);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int hashCode() {
        final String s = metaObject.getID() + "." + metaObject.getMetaClass().getID() + "." + metaObject.getDomain(); // NOI18N

        return s.hashCode();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toObjectString() {
        return getClass().getName() + "@" + Integer.toHexString(hashCode()); // NOI18N
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
    @Deprecated
    public CidsBean persist(final MetaService metaService, final User user, final String domain) throws Exception {
        return persist(metaService, user, domain, null);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaService        DOCUMENT ME!
     * @param   user               DOCUMENT ME!
     * @param   domain             DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsBean persist(final MetaService metaService,
            final User user,
            final String domain,
            final ConnectionContext connectionContext) throws Exception {
        if (metaObject.getStatus() == MetaObject.MODIFIED) {
            metaService.updateMetaObject(
                user,
                metaObject,
                domain,
                connectionContext);

            return metaService.getMetaObject(
                        user,
                        metaObject.getID(),
                        metaObject.getClassID(),
                        domain,
                        connectionContext)
                        .getBean();
        } else if (metaObject.getStatus() == MetaObject.TO_DELETE) {
            metaService.deleteMetaObject(
                user,
                metaObject,
                domain,
                connectionContext);

            return null;
        } else if (metaObject.getStatus() == MetaObject.NEW) {
            final MetaObject mo = metaService.insertMetaObject(
                    user,
                    metaObject,
                    domain,
                    connectionContext);
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
    @Deprecated
    public CidsBean persist() throws Exception {
        return persist(ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public CidsBean persist(final ConnectionContext connectionContext) throws Exception {
        final CidsBeanPersistService persistService = Lookup.getDefault().lookup(CidsBeanPersistService.class);
        if (persistService != null) {
            if (persistService instanceof ConnectionContextStore) {
                ((ConnectionContextStore)persistService).initWithConnectionContext(connectionContext);
            }
            return persistService.persistCidsBean(this);
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  property      DOCUMENT ME!
     * @param  arrayElement  DOCUMENT ME!
     */
    public void addCollectionElement(final String property, final CidsBean arrayElement) {
        final List<CidsBean> list = getBeanCollectionProperty(property);
        if ((list != null) && (arrayElement != null)) {
            list.add(arrayElement);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  property       DOCUMENT ME!
     * @param  arrayElements  DOCUMENT ME!
     */
    public void addCollectionElements(final String property, final Collection<CidsBean> arrayElements) {
        final List<CidsBean> list = getBeanCollectionProperty(property);
        if ((list != null) && (arrayElements != null)) {
            list.addAll(arrayElements);
        }
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

        // if oa is array we won't have to do anything because the listElement* operations take care of array elements
        if (!oa.isArray() && !oa.isVirtualOneToManyAttribute()) {
            final Object oldValue = oa.getValue();
            final Object value = evt.getNewValue();
            boolean realChanges = false;
            if (oa.referencesObject() && (value instanceof CidsBean) && (value != null)) {
                final CidsBean cbv = (CidsBean)value;
                realChanges = ((oldValue == null)
                                || ((oldValue instanceof MetaObject)
                                    && !((Sirius.server.middleware.types.MetaObject)oldValue).getBean().toJSONString(
                                        true).equals(
                                        cbv.toJSONString(true))));
                oa.setValue(cbv.getMetaObject());
                cbv.setBacklinkInformation(field, this);
                if (cbv.getMetaObject().getStatus() == MetaObject.TO_DELETE) {
                    cbv.getMetaObject().setStatus(MetaObject.MODIFIED);
                }
            } else if (((oldValue == null) && (value != null))
                        || ((oldValue instanceof Geometry)
                            && (System.identityHashCode(oldValue) != System.identityHashCode(value)))
                        || ((oldValue != null) && !oldValue.equals(value))) {
                oa.setValue(value);
                realChanges = true;
            }

            if (LOG.isDebugEnabled()) {
                if (realChanges) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("a property changed:" + metaObject.getDebugString()); // NOI18N
                    }
                } else if (LOG.isDebugEnabled()) {
                    LOG.debug(
                        "a property changed, but the content of the object was not changed. seams to be a caching or normalization move.:"
                                + metaObject.getDebugString());                         // NOI18N
                }
            }

            if (((oldValue == null) && (value != null))
                        || ((oldValue != null) && realChanges)) {
                oa.setChanged(true);
                metaObject.setStatus(MetaObject.MODIFIED);

                final ObjectAttribute referencingOA = metaObject.getReferencingObjectAttribute();
                walkUpAndSetChangedAndModified(referencingOA);
            } else {
                LOG.info("set with the same value. no status change required (" + field + ":" + value + ")"); // NOI18N
            }
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
            // nicht weil über den MetaObject Konstruktor eine neue Adresse
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
            final ObjectAttribute backlinkOA = backlinkObject.getMetaObject()
                        .getAttributeByFieldName(backlinkFieldname);
            walkUpAndSetChangedAndModified(backlinkOA);

            final Object o = PropertyUtils.getProperty(backlinkObject, backlinkFieldname);
            if (o instanceof CidsBean) {
                PropertyUtils.setProperty(backlinkObject, backlinkFieldname, null);
            } else if (o instanceof ObservableList) {
                ((ObservableList)o).remove(this);
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
                    oa.getMai().getForeignKeyClassId(),
                    getConnectionContext());
            final CidsBean newOne = mc.getEmptyInstance(getConnectionContext()).getBean();
            setProperty(name, newOne);
        } else {
            throw new RuntimeException("Could not lookup MetaClassCacheService"); // NOI18N
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fieldname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    public CidsBean getEmptyBeanFromArrayAttribute(final String fieldname) {
        final MetaClassCacheService classCacheService = Lookup.getDefault().lookup(MetaClassCacheService.class);
        if (classCacheService != null) {
            final ObjectAttribute oa = getMetaObject().getAttributeByFieldName(fieldname);
            // abs weil im 1:n Fall eine negative class-id im foreign-key-field steht
            final MetaClass firstMC = classCacheService.getMetaClass(
                    getMetaObject().getDomain(),
                    Math.abs(oa.getMai().getForeignKeyClassId()),
                    getConnectionContext());
            if (oa.isVirtualOneToManyAttribute()) {
                final CidsBean newOne = firstMC.getEmptyInstance(getConnectionContext()).getBean();
                return newOne;
            } else if (oa.isArray()) {
                final HashMap hm = firstMC.getMemberAttributeInfos();
                final Iterator it = hm.values().iterator();
                while (it.hasNext()) {
                    final Object tmp = it.next();
                    if (tmp instanceof MemberAttributeInfo) {
                        if (((MemberAttributeInfo)tmp).isForeignKey()) {
                            final int classId = ((MemberAttributeInfo)tmp).getForeignKeyClassId();
                            final MetaClass targetClass = classCacheService.getMetaClass(firstMC.getDomain(),
                                    classId,
                                    getConnectionContext());
                            final CidsBean newOne = targetClass.getEmptyInstance(getConnectionContext()).getBean();
                            return newOne;
                        }
                    }
                }
                throw new RuntimeException("Missconfigured Array-Class"); // NOI18N
            } else {
                throw new RuntimeException("Must be an Array-Attribute"); // NOI18N
            }
        } else {
            throw new RuntimeException("Could not lookup MetaClassCacheService"); // NOI18N
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
            LOG.warn("Error in setProperty:" + name + ". Result will be null. No exception is being thrown.", e); // NOI18N
        }
    }

    /**
     * Sets a property but does not set the change status flag.
     *
     * @param   name   DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void quiteSetProperty(final String name, final Object value) throws Exception {
        setProperty(name, value);
        if (metaObject.getAttributeByFieldName(name) != null) {
            metaObject.getAttributeByFieldName(name).setChanged(false);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name   DOCUMENT ME!
     * @param   value  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void setPropertyForceChanged(final String name, final Object value) throws Exception {
        setProperty(name, value);
        if (metaObject.getAttributeByFieldName(name) != null) {
            metaObject.getAttributeByFieldName(name).setChanged(true);
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
            LOG.warn("Error in getproperty:" + name + ". Result will be null. No exception is being thrown.", e); // NOI18N
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
     * @param   arrayfield  DOCUMENT ME!
     * @param   list        the {@code ObservableList} that has changed
     * @param   index       the index the elements were added to
     * @param   length      the number of elements that were added
     *
     * @throws  RuntimeException  DOCUMENT ME!
     */
    public void listElementsAdded(final String arrayfield,
            final ObservableList list,
            final int index,
            final int length) {
        final List<CidsBean> old = new ArrayList<CidsBean>(list);

        // FIXME: #171
        for (int i = index; i < (index + length); ++i) {
            try {
                old.remove(i - (list.size() - old.size()));
                final Object o = list.get(i);
                if (arrayfield != null) {
                    if (o instanceof CidsBean) {
                        final CidsBean cb = (CidsBean)o;
                        cb.setBacklinkInformation(arrayfield, this);
                        final ObjectAttribute oa = this.getMetaObject().getAttributeByFieldName(arrayfield);
                        final MemberAttributeInfo mai = oa.getMai();
                        walkUpAndSetChangedAndModified(oa);

                        // Wenn noch kein Dummy-Objekt existiert (Wert ist noch null)
                        // Anlegen eines Dummy-Objektes
                        MetaObject dummy = (MetaObject)oa.getValue();
                        if (dummy == null) {
                            final int classId = oa.isVirtualOneToManyAttribute() ? (-1 * mai.getForeignKeyClassId())
                                                                                 : mai.getForeignKeyClassId();
                            final Sirius.server.localserver.object.Object dummyO =
                                new Sirius.server.localserver.object.DefaultObject(
                                    getMetaObject().getID(),
                                    classId);
                            final DefaultMetaObject mo = new DefaultMetaObject(dummyO, getMetaObject().getDomain());
                            mo.initWithConnectionContext(getConnectionContext());
                            mo.setReferencingObjectAttribute(oa);
                            mo.setDummy(true);
                            mo.setStatus(MetaObject.NEW);
                            oa.setValue(mo);
                            oa.setChanged(true);
                            dummy = mo;
                        }

                        // 1:n Beziehung??
                        if (oa.isVirtualOneToManyAttribute()) {
                            final ObjectAttribute[] arrayElementAttrs = dummy.getAttribs();

                            dummy.setStatus(MetaObject.MODIFIED);
                            final ObjectAttribute entryToAddOA = new ObjectAttribute(
                                    null,
                                    mai,
                                    -1,
                                    cb.getMetaObject(),
                                    cb.getMetaObject().getMetaClass().getAttributePolicy());
                            entryToAddOA.setParentObject(dummy);
                            entryToAddOA.setClassKey(mai.getForeignKeyClassId() + "@" + dummy.getDomain());
                            entryToAddOA.setChanged(true);
                            dummy.addAttribute(entryToAddOA);
                            cb.getMetaObject().setReferencingObjectAttribute(entryToAddOA);

//                            entryToAddOA.setValue(getMetaObject());
                        } else { // n-m Beziehung
                            // ArrayElement anlegen
                            final MetaClass zwischenTabellenKlasse = (MetaClass)(getMetaObject().getAllClasses()).get(
                                    getMetaObject().getDomain()
                                            + oa.getMai().getForeignKeyClassId());
                            final MetaObject arrayElement = zwischenTabellenKlasse.getEmptyInstance(
                                    getConnectionContext());

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

                            // hinzufuegen eines Attributes, das auf das angelegte Arrayelement zeigt
                            dummy.setStatus(MetaObject.MODIFIED);
                            final ObjectAttribute[] attribs = dummy.getAttribs();

                            final ObjectAttribute dummyOA = new ObjectAttribute(
                                    null,
                                    mai,
                                    -1,
                                    arrayElement,
                                    zwischenTabellenKlasse.getAttributePolicy());
                            dummyOA.setParentObject(dummy);
                            dummyOA.setClassKey(mai.getForeignKeyClassId() + "@" + zwischenTabellenKlasse.getDomain());
                            dummyOA.setChanged(true);
                            dummy.addAttribute(dummyOA);
                            arrayElement.setReferencingObjectAttribute(dummyOA);
                        }
                    } else {
                        throw new IllegalArgumentException("Every element of an array must be a CidsBean"); // NOI18N
                    }
                } else {
                    throw new IllegalArgumentException("ObservableList is not registered as Array");        // NOI18N
                }
            } catch (final Exception e) {
                final String msg = "Fehler in listElementsAdded";
                LOG.error(msg, e);                                                                          // NOI18N
                throw new RuntimeException(msg, e);
            }
        }

        propertyChangeSupport.firePropertyChange(arrayfield, old, getBeanCollectionProperty(arrayfield));
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
            final CidsBean cidsBean = (CidsBean)element;
            final ObjectAttribute deepestReferencingAttribute = cidsBean.getMetaObject()
                        .getReferencingObjectAttribute();
            final ObjectAttribute oa = this.getMetaObject().getAttributeByFieldName(arrayfield);
            final MetaObject dummy = (MetaObject)oa.getValue();
            final boolean virtualOneToMany = oa.isVirtualOneToManyAttribute();
            if ((cidsBean.getMetaObject().getStatus() == MetaObject.TO_DELETE)
                        || ((cidsBean.getMetaObject().getStatus() == MetaObject.MODIFIED)
                            || (cidsBean.getMetaObject().getStatus() == MetaObject.NO_STATUS))) {
                if (virtualOneToMany) {
                    oa.setChanged(true);
                    cidsBean.getMetaObject().setStatus(MetaObject.TO_DELETE);
                } else {
                    deepestReferencingAttribute.setChanged(true);
                }
            } else if (cidsBean.getMetaObject().getStatus() == MetaObject.NEW) {
                // wurde gerade erst angelegt, braucht nur entfernt zu werden
                if (virtualOneToMany) {
                    dummy.removeAttribute(deepestReferencingAttribute);
                } else {
                    deepestReferencingAttribute.setValue(null);
                }
            }
            if (!virtualOneToMany) {
                final Sirius.server.localserver.object.Object arrayEntry =
                    deepestReferencingAttribute.getParentObject();
                if (arrayEntry.getStatus() == MetaObject.NEW) {
                    // wurde gerade erst angelegt, braucht nur entfernt zu werden
                    final ObjectAttribute toDelete = arrayEntry.getReferencingObjectAttribute();
                    toDelete.getParentObject().removeAttribute(toDelete);
                } else if ((arrayEntry.getStatus() != MetaObject.TEMPLATE)
                            || (arrayEntry.getStatus() != MetaObject.TEMPLATE)) {
                    arrayEntry.setStatus(MetaObject.TO_DELETE);
                    final ObjectAttribute referencingOA = arrayEntry.getReferencingObjectAttribute();
                    walkUpAndSetChangedAndModified(referencingOA);
                }
            }
        }
        getMetaObject().setStatus(MetaObject.MODIFIED);

        final ArrayList<CidsBean> old = new ArrayList(list);
        old.addAll(index, oldElements);
        propertyChangeSupport.firePropertyChange(arrayfield, old, getBeanCollectionProperty(arrayfield));
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
        if (LOG.isDebugEnabled()) {
            LOG.warn("listElementReplaced: " + this, new Exception());
        }

        // FIXME: implement or throw unsupported operation exception!
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
        if (LOG.isDebugEnabled()) {
            LOG.debug("listElementPropertyChanged: " + this, new Exception());
        }
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
     */
    public String getPrimaryKeyFieldname() {
        if (pkFieldName == null) {
            pkFieldName = getMetaObject().getMetaClass().getPrimaryKey();
        }
        return pkFieldName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Integer getPrimaryKeyValue() {
        return (Integer)getProperty(getPrimaryKeyFieldname().toLowerCase());
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
            final PropertyDescriptor pd = new PropertyDescriptor("MOString", CidsBean.class); // NOI18N

            return new PropertyDescriptor[] { pd };
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   intraObjectCacheEnabled  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toJSONString(final boolean intraObjectCacheEnabled) {
        final IntraObjectCacheJsonParams params = new IntraObjectCacheJsonParams();
        params.setCacheDuplicates(intraObjectCacheEnabled);

        return toJSONString(params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   intraObjectCacheEnabled  DOCUMENT ME!
     * @param   jsonOmitNull             DOCUMENT ME!
     * @param   jsonLevel                DOCUMENT ME!
     * @param   jsonPropFields           DOCUMENT ME!
     * @param   jsonPropExpand           DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toJSONString(final boolean intraObjectCacheEnabled,
            final boolean jsonOmitNull,
            final int jsonLevel,
            final Collection<String> jsonPropFields,
            final Collection<String> jsonPropExpand) {
        final IntraObjectCacheJsonParams params = new IntraObjectCacheJsonParams();
        params.setCacheDuplicates(intraObjectCacheEnabled);
        params.setOmitNull(jsonOmitNull);
        params.setMaxLevel(jsonLevel);
        params.setFieldsPropNames(jsonPropFields);
        params.setExpandPropNames(jsonPropExpand);

        return toJSONString(params);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String toJSONString(final IntraObjectCacheJsonParams params) {
        try {
            setJsonSerializerParams(params);
            final boolean intraObjectCacheEnabled = (params != null) && params.isCacheDuplicates();

            this.intraObjectCache.clear();
            if (intraObjectCacheEnabled) {
                return intraObjectCacheMapper.writeValueAsString(this);
            } else {
                return mapper.writeValueAsString(this);
            }
        } catch (Exception ex) {
            LOG.error("Error in Json Output", ex);
            return "{\"error\":\"Error during Json Production\",\"exception\":\"" + ex
                        + "\",\"details\":\"see the log\"}";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   intraObjectCacheEnabled  DOCUMENT ME!
     * @param   beans                    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toJSONString(final boolean intraObjectCacheEnabled, final Collection<CidsBean> beans) {
        final IntraObjectCacheJsonParams params = new IntraObjectCacheJsonParams();
        params.setCacheDuplicates(intraObjectCacheEnabled);

        return toJSONString(params, beans);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   params  DOCUMENT ME!
     * @param   beans   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String toJSONString(final IntraObjectCacheJsonParams params, final Collection<CidsBean> beans) {
        try {
            for (final CidsBean bean : beans) {
                bean.setJsonSerializerParams(params);
            }
            return intraObjectCacheMapper.writeValueAsString(beans);
        } catch (Exception ex) {
            LOG.error("Error in serialization of Cidsbeans Array");
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBeanInfo getCidsBeanInfo() {
        return new CidsBeanInfo(getMetaObject().getMetaClass().getDomain(),
                getMetaObject().getMetaClass().getTableName(),
                getPrimaryKeyValue());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean hasArtificialChangeFlag() {
        return artificialChange;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  artificialChange  DOCUMENT ME!
     */
    public void setArtificialChangeFlag(final boolean artificialChange) {
        this.artificialChange = artificialChange;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domainName         DOCUMENT ME!
     * @param   tableName          DOCUMENT ME!
     * @param   initialProperties  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Deprecated
    public static CidsBean createNewCidsBeanFromTableName(final String domainName,
            final String tableName,
            final Map<String, Object> initialProperties) throws Exception {
        return createNewCidsBeanFromTableName(
                domainName,
                tableName,
                initialProperties,
                ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domainName         DOCUMENT ME!
     * @param   tableName          DOCUMENT ME!
     * @param   initialProperties  DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static CidsBean createNewCidsBeanFromTableName(final String domainName,
            final String tableName,
            final Map<String, Object> initialProperties,
            final ConnectionContext connectionContext) throws Exception {
        final CidsBean newBean = createNewCidsBeanFromTableName(domainName, tableName, connectionContext);
        for (final Entry<String, Object> property : initialProperties.entrySet()) {
            final Object valuObject = property.getValue();
            if (valuObject instanceof Collection) {
                final List<CidsBean> arrayRelation = newBean.getBeanCollectionProperty(property.getKey());
                if (arrayRelation != null) {
                    arrayRelation.addAll((Collection<CidsBean>)valuObject);
                }
            } else {
                newBean.setProperty(property.getKey(), property.getValue());
            }
        }

        return newBean;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   intraObjectCacheEnabled  DOCUMENT ME!
     * @param   json                     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static CidsBean createNewCidsBeanFromJSON(final boolean intraObjectCacheEnabled, final String json)
            throws Exception {
        // #174 boolean flag intraObjectCacheEnabled in CidsBean.createNewCidsBeanFromJSON() is not evaluated
        if (intraObjectCacheEnabled) {
            return intraObjectCacheMapper.readValue(json, CidsBean.class);
        } else {
            LOG.warn("ignoring intraObjectCache disabled flag! https://github.com/cismet/cids-server/issues/175");
            // return mapper.readValue(json, CidsBean.class);
            return intraObjectCacheMapper.readValue(json, CidsBean.class);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   intraObjectCacheEnabled  DOCUMENT ME!
     * @param   json                     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static Collection<CidsBean> createNewCidsBeansFromJSONCollection(final boolean intraObjectCacheEnabled,
            final String json) throws Exception {
        final TypeFactory t = TypeFactory.defaultInstance();

        final Collection<CidsBean> jsonBeans;
        // #174 boolean flag intraObjectCacheEnabled in CidsBean.createNewCidsBeanFromJSON() is not evaluated
        if (intraObjectCacheEnabled) {
            jsonBeans = intraObjectCacheMapper.readValue(
                    json,
                    t.constructCollectionType(Collection.class, CidsBean.class));
        } else {
            LOG.warn("ignoring intraObjectCache disabled flag! https://github.com/cismet/cids-server/issues/175");
//            jsonBeans = mapper..readValue(
//                json,
//                t.constructCollectionType(Collection.class, CidsBean.class));
            jsonBeans = intraObjectCacheMapper.readValue(
                    json,
                    t.constructCollectionType(Collection.class, CidsBean.class));
        }

        return jsonBeans;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domainName  DOCUMENT ME!
     * @param   tableName   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Deprecated
    public static CidsBean createNewCidsBeanFromTableName(final String domainName, final String tableName)
            throws Exception {
        return createNewCidsBeanFromTableName(domainName, tableName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domainName         DOCUMENT ME!
     * @param   tableName          DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static CidsBean createNewCidsBeanFromTableName(final String domainName,
            final String tableName,
            final ConnectionContext connectionContext) throws Exception {
        final MetaClassCacheService classcache = Lookup.getDefault().lookup(MetaClassCacheService.class);
        if (tableName != null) {
            final MetaClass metaClass = classcache.getMetaClass(domainName, tableName, connectionContext);
            if (metaClass != null) {
                return metaClass.getEmptyInstance(connectionContext).getBean();
            }
        }
        throw new Exception("Could not find MetaClass for table " + tableName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domainName  DOCUMENT ME!
     * @param   tableName   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Deprecated
    public static MetaClass getMetaClassFromTableName(final String domainName, final String tableName)
            throws Exception {
        return getMetaClassFromTableName(domainName, tableName, ConnectionContext.createDeprecated());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domainName         DOCUMENT ME!
     * @param   tableName          DOCUMENT ME!
     * @param   connectionContext  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public static MetaClass getMetaClassFromTableName(final String domainName,
            final String tableName,
            final ConnectionContext connectionContext) throws Exception {
        final MetaClassCacheService classcache = Lookup.getDefault().lookup(MetaClassCacheService.class);
        if (tableName != null) {
            final MetaClass mc = classcache.getMetaClass(domainName, tableName, connectionContext);
            if (mc != null) {
                return mc;
            }
        }
        throw new Exception("Could not find MetaClass for table " + tableName);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   collectionProperty  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public List<CidsBean> getBeanCollectionProperty(final String collectionProperty) {
        if (collectionProperty != null) {
            final Object colObj = getProperty(collectionProperty);
            if (colObj instanceof Collection) {
                return (List<CidsBean>)colObj;
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     * @param   bean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean checkWritePermission(final User user, final CidsBean bean) {
        return bean.getHasWritePermission(user) && bean.hasObjectWritePermission(user);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public IntraObjectCacheJsonParams getJsonSerializerParams() {
        return jsonSerializerParams;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  jsonSerializerParams  DOCUMENT ME!
     */
    protected void setJsonSerializerParams(final IntraObjectCacheJsonParams jsonSerializerParams) {
        this.jsonSerializerParams = jsonSerializerParams;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        final MetaObject mo = getMetaObject();
        if ((mo != null) && (mo instanceof ConnectionContextProvider)) {
            return ((ConnectionContextProvider)mo).getConnectionContext();
        } else {
            return dummyConnectionContext;
        }
    }
}
