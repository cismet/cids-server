/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.object;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.permission.PermissionHolder;

import Sirius.util.Mapable;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import de.cismet.cids.tools.fromstring.FromStringCreator;

/**
 * DefaultObject ist die ist die Eiheitliche Darstellung eines Tabelleneintrages in Sirius.
 *
 * @version  $Revision$, $Date$
 */
public class DefaultObject implements Object {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(DefaultObject.class);

    //~ Instance fields --------------------------------------------------------

    /** Id der Klasse des Objekts, hierueber kann die zugeh\u00F6rige Klasse referenziert werden. */
    protected int classID;
    /** Fungiert als Objectreferenz in einem assoziativen Container (ObjectMap). */
    protected int objectID;
    /** indicates whether this object was constucted artificially eg array object. */
    protected boolean dummy = false;
    /** container for this objects attributes. */
    protected LinkedHashMap attribHash;
    /** indicates wheter this object was loaded from a domainservers database. */
    protected boolean persistent = true;
    protected ObjectAttribute referencingObjectAttribute;
    /** helper for generating an DefaultObject instance from a string. */
    private FromStringCreator objectCreator;
    private int status = NO_STATUS;

    //~ Constructors -----------------------------------------------------------

    /**
     * Kopierkonstruktor.
     *
     * @param  o  original
     */
    public DefaultObject(final Sirius.server.localserver.object.Object o) {
        this((o != null) ? o.getID() : -1, (o != null) ? o.getClassID() : -1);
        if (o != null) {
            if (o.getAttributes() != null) {
                attribHash = new LinkedHashMap(o.getAttributes());
            } else {
                attribHash = new LinkedHashMap(10, 0.75f, false);
            }
            this.objectCreator = o.getObjectCreator();
            this.referencingObjectAttribute = o.getReferencingObjectAttribute();
            this.status = o.getStatus();
        } else {
            LOG.error("object null default object created"); // NOI18N
        }
    }

    /**
     * Erzeug ein unattributiertes Objekt.<BR>
     *
     * @param  objectID  id des Objekts
     * @param  classID   id der Klasse des Objekts
     */
    public DefaultObject(final int objectID, final int classID) {
        this.classID = classID;
        this.objectID = objectID;

        // insertion order
        attribHash = new LinkedHashMap(10, 0.75f, false);
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * getter for classID.
     *
     * @return  id der Klasse dieses Objekts
     *
     * @see     #classID
     */
    @Override
    public final int getClassID() {
        return classID;
    }

    /**
     * getter for ID.
     *
     * @return  eineutiger Schl\u00FCssel (innerhlab einer Klasse)
     *
     * @see     #objectID
     */
    @Override
    public final int getID() {
        return objectID;
    }

    @Override
    public void setID(final int objectID) {
        this.objectID = objectID;
    }

    /**
     * key of the form classId.objectID.
     *
     * @return  eindeutiger Schl\u00FCssel innerhalb einer domain (i.d.R. objectID@classID)
     */
    @Override
    public java.lang.Object getKey() {
        return objectID + "@" + classID; // NOI18N
    }

    /**
     * F\u00FCgt ein Attribut in die davor vorgesehenen AtrributVectoren ein.<BR>
     *
     * @param  anyAttribute  Objektattribute
     *
     * @see    #longs
     * @see    #dates
     * @see    #doubles
     * @see    #longs
     */
    @Override
    public void addAttribute(final ObjectAttribute anyAttribute) {
        /*
         * if (anyAttribute instanceof ObjectAttribute ) { attribs.add((Attribute)anyAttribute); }// end if
         * AttributeOfClass else throw new java.lang.Exception(" no subtype of Attribute");
         */
        if (dummy)                                                           // in einem arrayLink Objekt m\u00FCssen
                                                                             // alle Felder ausgef\u00FCllt sein egal
                                                                             // was gesetzt wurde
                                                                             // (Unsinnsbeschr\u00E4nkung)
        {
            anyAttribute.setOptional(false);
            if ((LOG != null) && LOG.isInfoEnabled()) {
                LOG.info(
                    "optional set to false for attribute : "                 // NOI18N
                            + anyAttribute
                            + " because it belongs to a arrayLink (dummy)"); // NOI18N
            }
        }

        attribHash.put(anyAttribute.getKey(), anyAttribute);
    } // end of addAttribute

    @Override
    public void removeAttribute(final ObjectAttribute anyAttribute) {
        attribHash.remove(anyAttribute.getKey());
    }

    /**
     * beschafft eine Arrayrprenstation aller Attribute des DefaultObject.<BR>
     *
     * @return  Alle Attribute des Objekts
     *
     * @see     #longs
     */
    @Override
    public ObjectAttribute[] getAttribs() {
        return (ObjectAttribute[])attribHash.values().toArray(new ObjectAttribute[attribHash.size()]);
    }

    /**
     * getter for attribHash.
     *
     * @return  Hashtabel containing this objects attributes
     */
    @Override
    public HashMap getAttributes() {
        return attribHash;
    }

    /**
     * retrieves an Attributed referenced by its key (name) Please note that this method retrieves the first attribute
     * that matchtes if one needs all attributes matching he should use getAttributeByname().
     *
     * @param   key  Schl\u00FCssel (key) des gew\u00FCnschten Attributs
     *
     * @return  das Attribut zu dem der Schl\u00FCssel passt
     */
    @Override
    public java.lang.Object getAttribute(final java.lang.Object key) {
        // return  attribHash.get(key);
        final ObjectAttribute[] as = getAttribs();
        for (int i = 0; i < as.length; i++) {
            if (as[i].getName().equalsIgnoreCase(key.toString())) {
                return as[i];
            }
        }
        return null;
    }

    /**
     * beschafft eine Collection welche alle Attribute enth\u00E4lt deren Schl\u00FCssel dem parameter name entsprechen.
     *
     * @param   name       Name/Schl\u00FCssel des Attributes
     * @param   maxResult  DOCUMENT ME!
     *
     * @return  Collection mit allen attributen gleichen schl\u00FCssels == name
     */
    @Override
    public Collection<Attribute> getAttributeByName(final String name, int maxResult) {
        final Iterator<Attribute> iter = getAttributes().values().iterator();
        final ArrayList<Attribute> attribsByName = new ArrayList();
        while ((maxResult > 0) && iter.hasNext()) {
            Attribute a = null;
            a = iter.next();
            if (a.getName().equalsIgnoreCase(name)) {
                attribsByName.add(a);
                maxResult--;
            }
        }
        return attribsByName;
    }

    /**
     * Method from Hell liefert ein Attribut ueber den Fieldname Es wird davon ausgegangen, dass nur ObjectAttributes im
     * getAttributes() sind.
     *
     * @param   fieldname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public ObjectAttribute getAttributeByFieldName(final String fieldname) {
        final Iterator<Attribute> iter = getAttributes().values().iterator();
        while (iter.hasNext()) {
            ObjectAttribute a = null; // TODO intanceof check ???
            a = (ObjectAttribute)iter.next();
            if (a.getMai().getFieldName().equalsIgnoreCase(fieldname)) {
                return a;
            }
        }
        return null;
    }

    /**
     * beschafft eine Collection welche alle Attribute enth\u00E4lt deren Schl\u00FCssel dem parameter name entsprechen.
     *
     * @param   names  Name/Schl\u00FCssel des Attributes
     *
     * @return  Collection mit allen attributen gleichen schl\u00FCssels == name
     */
    @Override
    public Collection getAttributesByName(final Collection names) {
        final Iterator iter = getAttributes().values().iterator();

        final ArrayList attribsByName = new ArrayList();
        while (iter.hasNext()) {
            Attribute a = null;
            a = (Attribute)iter.next();

            // if(logger!=null)logger.debug(a.toString()+" attribute gefunden");

            if (names.contains(a.getName())) {
                attribsByName.add(a);
            }
        }
        return attribsByName;
    }

    @Override
    public Collection getAttributesByType(final java.lang.Class c, int recursionDepth) {
        final Iterator iter = getAttributes().values().iterator();

        final ArrayList attribsByType = new ArrayList();

        if (recursionDepth < 0) {
            return attribsByType;
        }
        recursionDepth--;
        while (iter.hasNext()) {
            Attribute a = null;
            a = (Attribute)iter.next();
            // if(logger!=null)logger.debug(a.toString()+" attribute gefunden");
            final java.lang.Object val = a.getValue();
            // if(val!=null && val.getClass().equals(c))
            if ((val != null) && c.isAssignableFrom(val.getClass())) {
                attribsByType.add(a);
            } else if ((val != null) && (val instanceof Sirius.server.localserver.object.Object)) {
                attribsByType.addAll(
                    ((Sirius.server.localserver.object.Object)val).getAttributesByType(c, recursionDepth));
            }
        }
        return attribsByType;
    }
    // --------------------------------------------------------------------------

    @Override
    public Collection getAttributesByType(final java.lang.Class c) {
        return getAttributesByType(c, 0);
    }

    @Override
    public Collection getTraversedAttributesByType(final java.lang.Class c) {
        return getAttributesByType(c, Integer.MAX_VALUE);
    }

    /**
     * liefert eine fuer ug sichtbare Attributierung.
     *
     * @param   ug  Benutzergruppe nach der gefiltert werden soll
     *
     * @return  eine fuer UG massgeschneiderte Version des Objekts
     */
    @Override
    public Object filter(final UserGroup ug) {
        final DefaultObject tmp = new DefaultObject(this);

        final LinkedHashMap view = new LinkedHashMap();

        final Collection col = this.attribHash.values();

        final Iterator iter = col.iterator();

        while (iter.hasNext()) {
            final Attribute a = (Attribute)iter.next();
            if (a.getPermissions().hasPermission(ug.getKey(), PermissionHolder.READPERMISSION)) {
                view.put(a.getKey(), a);
            }
        }

        tmp.attribHash = view;

        return tmp;
    }

    /**
     * UNUSED.
     *
     * @param       m  UNUSED
     *
     * @return      UNUSED
     *
     * @deprecated  UNUSED
     */
    @Override
    public java.lang.Object constructKey(final Mapable m) {
        if (m instanceof Sirius.server.localserver.object.Object) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /**
     * adds all attributes to the DefaultObject.
     *
     * @param  objectAttributes  attributes to be added to the DefaultObject
     */
    @Override
    public void addAllAttributes(final ObjectAttribute[] objectAttributes) {
        for (int i = 0; i < objectAttributes.length; i++) {
            try {
                addAttribute(objectAttributes[i]);
            } catch (Exception e) {
                LOG.error("add attribute", e); // NOI18N
            }
        }
    }

    /**
     * Getter for property persistent.
     *
     * @return  Value of property persistent.
     */
    @Override
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Setter for property persistent.
     *
     * @param  persistent  New value of property persistent.
     */
    @Override
    public void setPersistent(final boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * creates an Instance of this DefaultObject from a string representation.
     *
     * <p>fromString(o.toSting())=o <B>should</B> be valid however this cannot be true in all thinkable situations</p>
     *
     * @param   objectRepresentation  string represntation of this DefaultObject
     * @param   mo                    templet of an DefaultObject
     *
     * @return  an instance of DefaultObject constucted by the objectCreator using the Stringreprsentation as input
     *
     * @throws  Exception  java.lang.Exception error during consturction of an DefaultObject
     */
    @Override
    public java.lang.Object fromString(final String objectRepresentation, final java.lang.Object mo) throws Exception {
        // if(objectCreator != null)
        // return objectCreator.create(objectRepresentation);
        // else
        return null;
    }

    /**
     * indicates whether fromString() can be called.
     *
     * @return  can be created from a string reprenstation of this object
     */
    @Override
    public boolean isStringCreateable() {
        return (getObjectCreator() != null);
    }

    /**
     * initializes all attributes with NULL.
     */
    @Override
    public void setValuesNull() {
        final Attribute[] as = getAttribs();

        for (int i = 0; i < as.length; i++) {
            as[i].setValuesNull();
            // as[i].setValue(null);
        }
    }

    @Override
    public void setPrimaryKeysNull() {
        final Iterator iter = getAttributes().values().iterator();

        while (iter.hasNext()) {
            Attribute a = null;
            a = (Attribute)iter.next();

            if (a.isPrimaryKey()) {
                a.setValue(null);
            } else if (a.referencesObject()) // rekursion
            {
                final Sirius.server.localserver.object.Object o = (Sirius.server.localserver.object.Object)a.getValue();
                if (o != null) {
                    o.setPrimaryKeysNull();
                }
            }
        }
    }

    /**
     * geter for primaryKey.
     *
     * @return  this objects class tables primary key
     */
    @Override
    public Attribute getPrimaryKey() {
        final Iterator iter = getAttributes().values().iterator();

        final ArrayList attribsByName = new ArrayList();

        while (iter.hasNext()) {
            Attribute a = null;
            a = (Attribute)iter.next();

            if (a.isPrimaryKey()) {
                return a;
            }
        }

        return null;
    }

    /**
     * getter for dummy.
     *
     * @return  determines whether it is an artificial object
     */
    @Override
    public boolean isDummy() {
        return dummy;
    }

    /**
     * setter for dummy.
     *
     * @param  dummy  whether it is a dummy
     */
    @Override
    public void setDummy(final boolean dummy) {
        this.dummy = dummy;
    }

    @Override
    public ObjectAttribute getReferencingObjectAttribute() {
        return referencingObjectAttribute;
    }

    @Override
    public void setReferencingObjectAttribute(final ObjectAttribute referencingObjectAttribute) {
        this.referencingObjectAttribute = referencingObjectAttribute;
    }

    /**
     * geter for status.
     *
     * @return  staturs
     */
    @Override
    public int getStatus() {
        return status;
    }

    /**
     * setter for status.
     *
     * @param  status  status
     */
    @Override
    public void setStatus(final int status) {
        if (this.status == NEW) {
        } else if ((status > 0) && (status < 5)) {
            this.status = status;
        } else {
            this.status = NO_STATUS;
        }
    }

    @Override
    public String getStatusDebugString() {
        return getStatusDebugString(getStatus());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   status  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    protected static String getStatusDebugString(final int status) {
        String statusString = "unknown"; // NOI18N

        switch (status) {
            case 0: {
                statusString = "No Status"; // NOI18N
                break;
            }
            case 1: {
                statusString = "New";       // NOI18N
                break;
            }
            case 2: {
                statusString = "Modified";  // NOI18N
                break;
            }
            case 3: {
                statusString = "To Delete"; // NOI18N
                break;
            }
            case 4: {
                statusString = "Template";  // NOI18N
                break;
            }
        }
        return statusString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the objectCreator
     */
    @Override
    public FromStringCreator getObjectCreator() {
        return objectCreator;
    }
}