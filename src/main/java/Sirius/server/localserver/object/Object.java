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

import Sirius.util.Mapable;

import java.io.Serializable;

import java.util.Collection;
import java.util.HashMap;

import de.cismet.cids.tools.fromstring.FromStringCreator;
import de.cismet.cids.tools.fromstring.StringCreateable;
import de.cismet.cids.tools.tostring.StringConvertable;

/**
 * DOCUMENT ME!
 *
 * @author   srichter
 * @version  $Revision$, $Date$
 */
public interface Object extends Mapable, StringConvertable, StringCreateable, Serializable {

    //~ Instance fields --------------------------------------------------------

    /** modidifed meta object (to be updated). */
    int MODIFIED = 2;
    /** new meta object (to be inserted). */
    int NEW = 1;
    /** status not set. */
    int NO_STATUS = 0;
    /** object template. */
    int TEMPLATE = 4;
    /** deleted meta object (to be deleted). */
    int TO_DELETE = 3;

    //~ Methods ----------------------------------------------------------------

    /**
     * adds all attributes to the Object.
     *
     * @param  objectAttributes  attributes to be added to the Object
     */
    void addAllAttributes(ObjectAttribute[] objectAttributes);

    /**
     * Fügt ein Attribut in die davor vorgesehenen AtrributVectoren ein.<BR>
     *
     * @param   anyAttribute  Objektattribute
     *
     * @throws  Exception  java.lang.Exception fehler beim hinzufügen
     *
     * @see     #longs
     * @see     #dates
     * @see     #doubles
     * @see     #longs
     */
    void addAttribute(ObjectAttribute anyAttribute) throws Exception;

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
    java.lang.Object constructKey(Mapable m);

    /**
     * liefert eine fuer ug sichtbare Attributierung.
     *
     * @param   ug  Benutzergruppe nach der gefiltert werden soll
     *
     * @return  eine fuer UG massgeschneiderte Version des Objekts
     *
     * @throws  Exception  java.lang.Exception Fehler
     */
    Object filter(UserGroup ug) throws Exception;

    /**
     * creates an Instance of this Object from a string representation.
     *
     * <p>fromString(o.toSting())=o <B>should</B> be valid however this cannot be true in all thinkable situations</p>
     *
     * @param   objectRepresentation  string represntation of this Object
     * @param   mo                    templet of an Object
     *
     * @return  an instance of Object constucted by the objectCreator using the Stringreprsentation as input
     *
     * @throws  Exception  java.lang.Exception error during consturction of an Object
     */
    @Override
    java.lang.Object fromString(String objectRepresentation, java.lang.Object mo) throws Exception;

    /**
     * beschafft eine Arrayrprenstation aller Attribute des Object.<BR>
     *
     * @return  Alle Attribute des Objekts
     *
     * @see     #longs
     */
    ObjectAttribute[] getAttribs();

    /**
     * retrieves an Attributed referenced by its key (name) Please note that this method retrieves the first attribute
     * that matchtes if one needs all attributes matching he should use getAttributeByname().
     *
     * @param   key  Schlüssel (key) des gewünschten Attributs
     *
     * @return  das Attribut zu dem der Schlüssel passt
     */
    java.lang.Object getAttribute(java.lang.Object key);

    /**
     * Method from Hell liefert ein Attribut �ber den Fieldname Es wird davon ausgegangen, dass nur ObjectAttributes
     * im getAttributes() sind.
     *
     * @param   fieldname  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ObjectAttribute getAttributeByFieldName(String fieldname);

    /**
     * beschafft eine Collection welche alle Attribute enthält deren Schlüssel dem parameter name entsprechen.
     *
     * @param   name       Name/Schlüssel des Attributes
     * @param   maxResult  DOCUMENT ME!
     *
     * @return  Collection mit allen attributen gleichen schlüssels == name
     */
    Collection<Attribute> getAttributeByName(String name, int maxResult);

    /**
     * getter for attribHash.
     *
     * @return  Hashtabel containing this objects attributes
     */
    HashMap getAttributes();

    /**
     * beschafft eine Collection welche alle Attribute enthält deren Schlüssel dem parameter name entsprechen.
     *
     * @param   names  Name/Schlüssel des Attributes
     *
     * @return  Collection mit allen attributen gleichen schlüssels == name
     */
    Collection getAttributesByName(Collection names);

    /**
     * DOCUMENT ME!
     *
     * @param   c               DOCUMENT ME!
     * @param   recursionDepth  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection getAttributesByType(Class c, int recursionDepth);

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection getAttributesByType(Class c);

    /**
     * getter for classID.
     *
     * @return  id der Klasse dieses Objekts
     *
     * @see     #classID
     */
    int getClassID();

    /**
     * getter for ID.
     *
     * @return  eineutiger Schlüssel (innerhlab einer Klasse)
     *
     * @see     #objectID
     */
    int getID();

    /**
     * key of the form classId.objectID.
     *
     * @return  eindeutiger Schlüssel innerhalb einer domain (i.d.R. objectID@classID)
     */
    @Override
    java.lang.Object getKey();

    /**
     * geter for primaryKey.
     *
     * @return  this objects class tables primary key
     */
    Attribute getPrimaryKey();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    ObjectAttribute getReferencingObjectAttribute();

    /**
     * geter for status.
     *
     * @return  staturs
     */
    int getStatus();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getStatusDebugString();

    /**
     * DOCUMENT ME!
     *
     * @param   c  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Collection getTraversedAttributesByType(Class c);

    /**
     * getter for dummy.
     *
     * @return  determines whether it is an artificial object
     */
    boolean isDummy();

    /**
     * Getter for property persistent.
     *
     * @return  Value of property persistent.
     */
    boolean isPersistent();

    /**
     * indicates whether fromString() can be called.
     *
     * @return  can be created from a string reprenstation of this object
     */
    @Override
    boolean isStringCreateable();

    /**
     * DOCUMENT ME!
     *
     * @param  anyAttribute  DOCUMENT ME!
     */
    void removeAttribute(ObjectAttribute anyAttribute);

    /**
     * setter for dummy.
     *
     * @param  dummy  whether it is a dummy
     */
    void setDummy(boolean dummy);

    /**
     * DOCUMENT ME!
     *
     * @param  objectID  DOCUMENT ME!
     */
    void setID(int objectID);

    /**
     * Setter for property persistent.
     *
     * @param  persistent  New value of property persistent.
     */
    void setPersistent(boolean persistent);

    /**
     * DOCUMENT ME!
     */
    void setPrimaryKeysNull();

    /**
     * DOCUMENT ME!
     *
     * @param  referencingObjectAttribute  DOCUMENT ME!
     */
    void setReferencingObjectAttribute(ObjectAttribute referencingObjectAttribute);

    /**
     * setter for status.
     *
     * @param  status  status
     */
    void setStatus(int status);

    /**
     * initializes all attributes with NULL.
     */
    void setValuesNull();

    /**
     * DOCUMENT ME!
     *
     * @return  the objectCreator
     */
    FromStringCreator getObjectCreator();
}
