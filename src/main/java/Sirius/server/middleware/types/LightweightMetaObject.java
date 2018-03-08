/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.impls.domainserver.DomainServerImpl;
import Sirius.server.middleware.interfaces.proxy.MetaService;
import Sirius.server.newuser.User;

import Sirius.util.Mapable;

import org.apache.log4j.Logger;

import org.openide.util.Lookup;

import java.lang.ref.SoftReference;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cids.server.CallServerServiceProvider;

import de.cismet.cids.tools.fromstring.FromStringCreator;

import de.cismet.connectioncontext.ConnectionContext;
import de.cismet.connectioncontext.ConnectionContextStore;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public final class LightweightMetaObject implements MetaObject,
    Comparable<LightweightMetaObject>,
    ConnectionContextStore {

    //~ Static fields/initializers ---------------------------------------------

    private static transient org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(
            LightweightMetaObject.class);

    private static Map<String, SoftReference<MetaObject>> CACHE = new HashMap<String, SoftReference<MetaObject>>();
    public static final String CACHE_INVALIDATION_MESSAGE = "lwmoCacheInvalidationRequest*";

    //~ Instance fields --------------------------------------------------------

    // use volantile variable to fix "double checked locking" problem!
    private transient volatile MetaObject fetchedMetaObject;
    private transient MetaService metaService;
    private final Map<String, Object> attributesMap;
    private final int classID;
    private User user;
    private int objectID;
    private String representation;
    private String domain;
    private ObjectAttribute referencingObjectAttribute;
    private transient ConnectionContext connectionContext = ConnectionContext.createDummy();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new LightweightMetaObject object.
     *
     * @param  classID   DOCUMENT ME!
     * @param  objectID  DOCUMENT ME!
     * @param  domain    DOCUMENT ME!
     * @param  user      DOCUMENT ME!
     */
    public LightweightMetaObject(final int classID,
            final int objectID,
            final String domain,
            final User user) {
        this.classID = classID;
        this.objectID = objectID;
        this.domain = domain;
        this.user = user;
        this.attributesMap = new HashMap<>();
        setFormater(new AbstractAttributeRepresentationFormater() {

                @Override
                public String getRepresentation() {
                    final StringBuilder result = new StringBuilder();
                    result.append("LWO:").append(classID).append("@").append(objectID).append("@").append(domain);
                    return result.toString();
                }
            });
    }

    /**
     * Creates a new LightweightMetaObject object.
     *
     * @param  classID        DOCUMENT ME!
     * @param  objectID       DOCUMENT ME!
     * @param  domain         DOCUMENT ME!
     * @param  user           DOCUMENT ME!
     * @param  attributesMap  DOCUMENT ME!
     */
    public LightweightMetaObject(final int classID,
            final int objectID,
            final String domain,
            final User user,
            final Map<String, Object> attributesMap) {
        this(
            classID,
            objectID,
            domain,
            user,
            attributesMap,
            new AbstractAttributeRepresentationFormater() {

                @Override
                public String getRepresentation() {
                    final StringBuilder result = new StringBuilder();
                    result.append("LWO:").append(classID).append("@").append(objectID).append("@").append(domain);
                    return result.toString();
                }
            });
    }

    /**
     * Creates a new LightweightMetaObject object.
     *
     * @param  classID        DOCUMENT ME!
     * @param  objectID       DOCUMENT ME!
     * @param  domain         DOCUMENT ME!
     * @param  user           DOCUMENT ME!
     * @param  attributesMap  DOCUMENT ME!
     * @param  formater       DOCUMENT ME!
     */
    public LightweightMetaObject(final int classID,
            final int objectID,
            final String domain,
            final User user,
            final Map<String, Object> attributesMap,
            final AbstractAttributeRepresentationFormater formater) {
        this.classID = classID;
        this.objectID = objectID;
        this.domain = domain;
        this.user = user;
        this.metaService = null;
        this.attributesMap = Collections.unmodifiableMap(attributesMap);
        setFormater(formater);
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public HashMap getAllClasses() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAllClasses();
        } else {
            return null;
        }
    }

    @Override
    public CidsBean getBean() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getBean();
        } else {
            return null;
        }
    }

    @Override
    public String getComplexEditor() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getComplexEditor();
        } else {
            return null;
        }
    }

    @Override
    public String getDebugString() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getDebugString();
        } else {
            return null;
        }
    }

    @Override
    public String getDescription() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getDescription();
        } else {
            return null;
        }
    }

    @Override
    public String getEditor() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getEditor();
        } else {
            return null;
        }
    }

    @Override
    public String getGroup() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getGroup();
        } else {
            return null;
        }
    }

    @Override
    public Logger getLogger() {
        if (LOG == null) {
            LOG = org.apache.log4j.Logger.getLogger(this.getClass());
        }

        return LOG;
    }

    @Override
    public MetaClass getMetaClass() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getMetaClass();
        } else {
            return null;
        }
    }

    @Override
    public String getName() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getName();
        } else {
            return null;
        }
    }

    @Override
    public String getPropertyString() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getPropertyString();
        } else {
            return null;
        }
    }

    @Override
    public String getRenderer() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getRenderer();
        } else {
            return null;
        }
    }

    @Override
    public String getSimpleEditor() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getSimpleEditor();
        } else {
            return null;
        }
    }

    @Override
    public Collection<String> getURLs(final Collection classKeys) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getURLs(classKeys);
        } else {
            return null;
        }
    }

    @Override
    public Collection<String> getURLsByName(final Collection classKeys, final Collection urlNames) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getURLsByName(classKeys, urlNames);
        } else {
            return null;
        }
    }

    @Override
    public boolean isChanged() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.isChanged();
        } else {
            return false;
        }
    }

    @Override
    public boolean propertyEquals(final MetaObject tester) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.propertyEquals(tester);
        } else {
            return false;
        }
    }

    @Override
    public void setAllClasses(final HashMap classes) {
        final MetaObject mo = getRealMetaObject(false);
        if (mo != null) {
            mo.setAllClasses(classes);
        }
    }

    @Override
    public void setAllClasses() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setAllClasses();
        }
    }

    @Override
    public void setAllStatus(final int status) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setAllStatus(status);
        }
    }

    @Override
    public void setArrayKey2PrimaryKey() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setArrayKey2PrimaryKey();
        }
    }

    @Override
    public void setChanged(final boolean changed) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setChanged(changed);
        }
    }

    @Override
    public void setEditor(final String editor) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setEditor(editor);
        }
    }

    @Override
    public void setMetaClass(final MetaClass metaClass) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setMetaClass(metaClass);
        }
    }

    @Override
    public boolean setPrimaryKey(final Object key) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.setPrimaryKey(key);
        } else {
            return false;
        }
    }

    @Override
    public void setRenderer(final String renderer) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setRenderer(renderer);
        }
    }

    @Override
    public String toString(final HashMap classes) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.toString(classes);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectAttributes  DOCUMENT ME!
     */
    @Override
    public void addAllAttributes(final ObjectAttribute[] objectAttributes) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.addAllAttributes(objectAttributes);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  anyAttribute  DOCUMENT ME!
     */
    @Override
    public void addAttribute(final ObjectAttribute anyAttribute) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.addAttribute(anyAttribute);
        }
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
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.constructKey(m);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   u  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Sirius.server.localserver.object.Object filter(final User u) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.filter(u);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   objectRepresentation  DOCUMENT ME!
     * @param   o                     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    @Deprecated
    @Override
    public Object fromString(final String objectRepresentation, final Object o) throws Exception {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.fromString(objectRepresentation, o);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute[] getAttribs() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttribs();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   name  key DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute getAttribute(final String name) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttribute(name);
        } else {
            return null;
        }
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
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttributeByFieldName(fieldname);
        } else {
            return null;
        }
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
    public Collection<ObjectAttribute> getAttributeByName(final String name, final int maxResult) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttributeByName(name, maxResult);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public LinkedHashMap<java.lang.Object, ObjectAttribute> getAttributes() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttributes();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   names  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection<ObjectAttribute> getAttributesByName(final Collection names) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttributesByName(names);
        } else {
            return null;
        }
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
    public Collection<ObjectAttribute> getAttributesByType(final Class c, final int recursionDepth) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttributesByType(c, recursionDepth);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection<ObjectAttribute> getAttributesByType(final Class c) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getAttributesByType(c);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getKey() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getKey();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute getPrimaryKey() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getPrimaryKey();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute getReferencingObjectAttribute() {
        return referencingObjectAttribute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int getStatus() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getStatus();
        } else {
            return -1;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String getStatusDebugString() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getStatusDebugString();
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Collection<ObjectAttribute> getTraversedAttributesByType(final Class c) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getTraversedAttributesByType(c);
        } else {
            return null;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  false, because a LightweightMetaObject cannot be a dummy
     */
    @Override
    public boolean isDummy() {
        // the isDummy method is invoked by the updateMetaObject method of the PersistenceManager
        // and a check, if the object is a dummy or not, would be decrease performance of the update method
        return false;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isPersistent() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.isPersistent();
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean isStringCreateable() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.isStringCreateable();
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  anyAttribute  DOCUMENT ME!
     */
    @Override
    public void removeAttribute(final ObjectAttribute anyAttribute) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.removeAttribute(anyAttribute);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dummy  true will be ignored, because a LightweightMetaObject cannot be a dummy
     */
    @Override
    public void setDummy(final boolean dummy) {
        if (dummy) {
            getLogger().error("A LightweightMetaObject is set to dummy, but this is not allowed and will be ignored.");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  persistent  DOCUMENT ME!
     */
    @Override
    public void setPersistent(final boolean persistent) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setPersistent(persistent);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void setPrimaryKeysNull() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setPrimaryKeysNull();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  referencingObjectAttribute  DOCUMENT ME!
     */
    @Override
    public void setReferencingObjectAttribute(final ObjectAttribute referencingObjectAttribute) {
        this.referencingObjectAttribute = referencingObjectAttribute;

        final MetaObject mo = getRealMetaObject(false);
        if (mo != null) {
            mo.setReferencingObjectAttribute(referencingObjectAttribute);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  status  DOCUMENT ME!
     */
    @Override
    public void setStatus(final int status) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setStatus(status);
        }
    }

    @Override
    public void forceStatus(final int status) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.forceStatus(status);
        }
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void setValuesNull() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setValuesNull();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public FromStringCreator getObjectCreator() {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.getObjectCreator();
        } else {
            return null;
        }
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
     * @param   domain    DOCUMENT ME!
     * @param   classID   DOCUMENT ME!
     * @param   objectID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String getKeyForCache(final String domain, final int classID, final int objectID) {
        return new StringBuilder().append(classID).append('@').append(domain).append(',').append(objectID).toString();
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
        // try the cache

        final String keyForCache = getKeyForCache(domain, classID, objectID);
        final SoftReference<MetaObject> refCacheHit = CACHE.get(keyForCache);
        final MetaObject cacheHit;
        if (refCacheHit != null) {
            cacheHit = refCacheHit.get();
        } else {
            cacheHit = null;
        }

        if (cacheHit != null) {
            if (NullMetaObject.NULL.equals(cacheHit)) {
                // checking for the null-representing MetaObject.
                //
                // explanation: if we would store "null" as the metaobject,
                // then the cache would try to fetch the metaobject each time
                // thinking that the softreference has been cleaned up.
                return null;
            } else {
                return new BackreferencingCachehitMetaObject(cacheHit, referencingObjectAttribute);
            }
        } else {
            if (metaService == null) {
                // try to get the metaservice over the lookup
                final CallServerServiceProvider csProvider = Lookup.getDefault()
                            .lookup(CallServerServiceProvider.class);
                if (csProvider != null) {
                    metaService = csProvider.getCallServerService();

                    if (metaService == null) {
                        throw new IllegalStateException(
                            "Can not retrieve MetaObject, as Metaservice for LightweightMetaObject \""
                                    + toString() // NOI18N
                                    + "\" is null!"); // NOI18N
                    }
                }
            }

            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Fetch real Object for " + this);
            }
            final MetaObject mo;
            if (metaService != null) { // this code should only be executed on the client side
                mo = metaService.getMetaObject(
                        getUser(),
                        getObjectID(),
                        getClassID(),
                        getDomain(),
                        getConnectionContext());
            } else {                   // this code should only be executed on the server side
                mo = DomainServerImpl.getServerInstance()
                            .getMetaObject(
                                    getUser(),
                                    getObjectID(),
                                    getClassID(),
                                    getConnectionContext());
            }

            final SoftReference<MetaObject> sr;
            if (mo == null) {
                sr = new SoftReference<>(NullMetaObject.NULL);
            } else {
                sr = new SoftReference<>(mo);
            }
            CACHE.put(getKeyForCache(domain, classID, objectID), sr);

            return mo;
        }
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
     */
    public MetaObject getRealMetaObject() {
        return getRealMetaObject(true);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fetchIfNull  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  IllegalStateException  DOCUMENT ME!
     */
    private MetaObject getRealMetaObject(final boolean fetchIfNull) {
        if (fetchedMetaObject == null) {
            if (fetchIfNull) {
                synchronized (this) {
                    try {
                        fetchedMetaObject = fetchRealMetaObject();
                    } catch (Exception ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }
        }
        return fetchedMetaObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectID  DOCUMENT ME!
     */
    @Override
    public void setID(final int objectID) {
        this.objectID = objectID;

        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            mo.setID(objectID);
        }
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
        return domain;
    }

    @Override
    public String getClassKey() {
        return classID + "@" + getDomain(); // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean alreadyFetched() {
        return fetchedMetaObject != null;
    }

    @Override
    public boolean hasObjectWritePermission(final User user) {
        final MetaObject mo = getRealMetaObject();
        if (mo != null) {
            return mo.hasObjectWritePermission(user);
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   domain    mo DOCUMENT ME!
     * @param   classId   DOCUMENT ME!
     * @param   objectId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static boolean invalidateCacheFor(final String domain, final int classId, final int objectId) {
        final String key = getKeyForCache(domain, classId, objectId);
        if (CACHE.containsKey(key)) {
            CACHE.remove(key);
            return true;
        }
        return false;
    }

    @Override
    public ConnectionContext getConnectionContext() {
        return connectionContext;
    }

    @Override
    public void initWithConnectionContext(final ConnectionContext connectionContext) {
        this.connectionContext = connectionContext;
    }
}
