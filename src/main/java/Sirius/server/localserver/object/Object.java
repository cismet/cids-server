/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Sirius.server.localserver.object;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.newuser.UserGroup;
import Sirius.util.Mapable;
import de.cismet.cids.tools.fromstring.FromStringCreator;
import de.cismet.cids.tools.fromstring.StringCreateable;
import de.cismet.cids.tools.tostring.StringConvertable;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

/**
 *
 * @author srichter
 */
public interface Object extends Mapable, StringConvertable, StringCreateable, Serializable {

    /**
     * modidifed meta object (to be updated)
     */
    int MODIFIED = 2;
    /**
     * new meta object (to be inserted)
     */
    int NEW = 1;
    /**
     * status not set
     */
    int NO_STATUS = 0;
    /**
     * object template
     */
    int TEMPLATE = 4;
    /**
     * deleted meta object (to be deleted)
     */
    int TO_DELETE = 3;

    /**
     * adds all attributes to the Object
     * @param objectAttributes attributes to be added to the Object
     */
    void addAllAttributes(ObjectAttribute[] objectAttributes);

    /**
     * Fügt ein Attribut in die davor vorgesehenen AtrributVectoren ein <BR>
     * @see #longs
     * @see #dates
     * @see #doubles
     * @see #longs
     * @param anyAttribute Objektattribute
     * @throws java.lang.Exception fehler beim hinzufügen
     */
    void addAttribute(ObjectAttribute anyAttribute) throws Exception;

    /**
     * UNUSED
     * @deprecated UNUSED
     * @param m UNUSED
     * @return UNUSED
     */
     java.lang.Object constructKey(Mapable m);

    /**
     * liefert eine fuer ug sichtbare Attributierung
     * @param ug Benutzergruppe nach der gefiltert werden soll
     * @throws java.lang.Exception Fehler
     * @return eine fuer UG massgeschneiderte Version des Objekts
     */
    Object filter(UserGroup ug) throws Exception;

    /**
     * creates an Instance of this Object from a string representation
     *
     * fromString(o.toSting())=o <B>should</B> be valid however this cannot be true in all thinkable situations
     * @param objectRepresentation string represntation of this Object
     * @param mo templet of an Object
     * @throws java.lang.Exception error during consturction of an Object
     * @return an instance of Object constucted by the objectCreator using the Stringreprsentation as input
     */
    java.lang.Object fromString(String objectRepresentation, java.lang.Object mo) throws Exception;

    /**
     * beschafft eine Arrayrprenstation aller Attribute des Object<BR>
     * @see #longs
     * @return Alle Attribute des Objekts
     */
    ObjectAttribute[] getAttribs();

    /**
     * retrieves an Attributed referenced by its key (name)
     * Please note that this method retrieves the first attribute that matchtes
     * if one needs all attributes matching he should use getAttributeByname()
     * @param key Schlüssel (key) des gewünschten Attributs
     * @return das Attribut zu dem der Schlüssel passt
     */
    java.lang.Object getAttribute(java.lang.Object key);

    /**
     * Method from Hell
     * liefert ein Attribut �ber den Fieldname
     * Es wird davon ausgegangen, dass nur ObjectAttributes im getAttributes() sind
     */
    ObjectAttribute getAttributeByFieldName(String fieldname);

    /**
     * beschafft eine Collection welche alle Attribute enthält deren Schlüssel dem parameter name entsprechen
     * @param name Name/Schlüssel des Attributes
     * @return Collection mit allen attributen gleichen schlüssels == name
     */
    Collection<Attribute> getAttributeByName(String name, int maxResult);

    /**
     * getter for attribHash
     * @return Hashtabel containing this objects attributes
     */
    HashMap getAttributes();

    /**
     * beschafft eine Collection welche alle Attribute enthält deren Schlüssel dem parameter name entsprechen
     * @param name Name/Schlüssel des Attributes
     * @return Collection mit allen attributen gleichen schlüssels == name
     */
    Collection getAttributesByName(Collection names);

    Collection getAttributesByType(Class c, int recursionDepth);

    Collection getAttributesByType(Class c);

    /**
     * getter for classID
     * @see #classID
     * @return id der Klasse dieses Objekts
     */
    int getClassID();

    /**
     * getter for ID
     * @see #objectID
     * @return eineutiger Schlüssel (innerhlab einer Klasse)
     */
    int getID();

    /**
     * key of the form classId.objectID
     * @return eindeutiger Schlüssel innerhalb einer domain (i.d.R. objectID@classID)
     */
    java.lang.Object getKey();

    /**
     * geter for primaryKey
     * @return this objects class tables primary key
     */
    Attribute getPrimaryKey();

    ObjectAttribute getReferencingObjectAttribute();

    /**
     * geter for status
     * @return staturs
     */
    int getStatus();

    String getStatusDebugString();

    Collection getTraversedAttributesByType(Class c);

    /**
     * getter for dummy
     * @return determines whether it is an artificial object
     */
    boolean isDummy();

    /**
     * Getter for property persistent.
     * @return Value of property persistent.
     */
    boolean isPersistent();

    /**
     * indicates whether fromString() can be called
     * @return can be created from a string reprenstation of this object
     */
    boolean isStringCreateable();

    void removeAttribute(ObjectAttribute anyAttribute);

    /**
     * setter for dummy
     * @param dummy whether it is a dummy
     */
    void setDummy(boolean dummy);

    void setID(int objectID);

    /**
     * Setter for property persistent.
     * @param persistent New value of property persistent.
     */
    void setPersistent(boolean persistent);

    void setPrimaryKeysNull();

    void setReferencingObjectAttribute(ObjectAttribute referencingObjectAttribute);

    /**
     * setter for status
     * @param status status
     */
    void setStatus(int status);

    /**
     * initializes all attributes with NULL
     */
    void setValuesNull();

    /**
     * @return the objectCreator
     */
    FromStringCreator getObjectCreator();
}
