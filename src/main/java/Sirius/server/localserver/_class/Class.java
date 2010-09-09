/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver._class;

import Sirius.server.localserver.attribute.Attribute;
import Sirius.server.localserver.attribute.AttributeVector;
import Sirius.server.localserver.attribute.ClassAttribute;
import Sirius.server.localserver.attribute.MemberAttributeInfo;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.newuser.permission.Policy;

import Sirius.util.Mapable;
import Sirius.util.image.Image;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import de.cismet.tools.collections.LongVector;

/**
 * Die Klasse Class fungiert zum einen als Mittel zur Klassifkation zum anderen enth\u00E4lt Sie Eigenschaften von
 * referenzierten Tabellen.
 *
 * @version  $Revision$, $Date$
 */
public class Class implements java.io.Serializable, Mapable {

    //~ Static fields/initializers ---------------------------------------------


    //~ Instance fields --------------------------------------------------------

    /**
     * Fungiert als Klassenreferenz in einem assoziativen Container.<BR>
     *
     * @see  Sirius.Class.ClassMap
     */
    protected int id;
    /** Name der Klasse wird bei der Visualisierung angzeigt. */
    protected String name;
    /** Enth\u00E4lt eine URL oder einen Klartext der die Klasse n\u00E4her beschreibt. */
    protected String description;
    /** Icon wird bei der Visualisierung der Klasse angzeigt. */
    protected Image icon;
    /** Icon wird bei der Visualisierung eines Objekts der Klasse angzeigt. */
    protected Image objectIcon;
    /** Zugriffsrechte bzgl. Benutzergruppen */
    protected PermissionHolder permissions;
    /** zur Klasse geh\u00F6render Tabellenname. */
    protected String tableName;
    /** Primaeschluessel der zur Klasse geh\u00F6renden Tabelle. */
    protected String primaryKey;
    /** voll qualifizierter Klassename zur erzeugung einer Stringrepr\u00E4sentation von Objekten dieser Klasse. */
    protected String toString;
    /** indicates whether objects of this class are only links between an array and it's elements. */
    protected boolean arrayElementLink = false;
//    /**Alle der Klasse zugeordneten "logischen" Methoden durch ihre ids repraesentiert*/
    protected LongVector methodIDs;
    /** Alle Attribute der Klasse. */
    protected AttributeVector attribs;
    /**
     * enth\u00E4lt information \u00FCber die Attribute der Objekte der Klasse, diesen werden zur Konstruktion von
     * Objekten ben\u00F6tigt.
     */
    protected LinkedHashMap memberAttributeInfos;
    /** sql statement welches eine instanz eines meta objektes dieser Klasse erzeugt. */
    protected String getInstanceStmnt;
    /** statement welches ein template eines meta objektes dieser Klasser erzeugt. */
    protected String getDefaultInstanceStmnt;
    /** definiert die java Klasse welche als Editor f\u00FCr diese Art von Objekten benutzt werden soll. */
    protected String editor;
    /** DOCUMENT ME! */
    protected String renderer;
    protected Policy attributePolicy;

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeug eine unattributierte Klasse.<BR>
     *
     * @param  id               this class key unique on on domain
     * @param  name             name of this class
     * @param  description      -
     * @param  icon             nodes representing this class will be vizualized using this icon
     * @param  objectIcon       objects of this class will be vizualized using this icon
     * @param  tableName        name of this class' corresponding table
     * @param  primaryKey       primary key of the table
     * @param  toString         class able to create a String representation of this class's objects
     * @param  permissions      permission container
     * @param  attributePolicy  DOCUMENT ME!
     */
    public Class(final int id,
            final String name,
            final String description,
            final Image icon,
            final Image objectIcon,
            final String tableName,
            final String primaryKey,
            final String toString,
            final PermissionHolder permissions,
            final Policy attributePolicy) {
        this(id, name, description, icon, objectIcon, tableName, primaryKey, toString, (Policy)null, attributePolicy);
        this.permissions = permissions;
    }

    /**
     * Erzeug eine unattributierte Klasse.<BR>
     *
     * @param  id               key
     * @param  name             name der Klasse
     * @param  description      Beschreibung der Klasse
     * @param  icon             nodes representing this class will be vizualized using this icon
     * @param  objectIcon       objects of this class will be vizualized using this icon
     * @param  tableName        name of this class' corresponding table
     * @param  primaryKey       primary key of the table
     * @param  toString         class able to create a String representation of this class's objects
     * @param  policy           DOCUMENT ME!
     * @param  attributePolicy  DOCUMENT ME!
     */
    public Class(final int id,
            final String name,
            final String description,
            final Image icon,
            final Image objectIcon,
            final String tableName,
            final String primaryKey,
            final String toString,
            final Policy policy,
            final Policy attributePolicy) {
        this.id = id;

        this.name = name;

        this.description = description;

        this.icon = icon;

        this.objectIcon = objectIcon;

        attribs = new AttributeVector(5, 5);

        memberAttributeInfos = new LinkedHashMap();

        methodIDs = new LongVector(5, 5);

        permissions = new PermissionHolder(policy);

        this.attributePolicy = attributePolicy;

        this.tableName = tableName;

        this.primaryKey = primaryKey;

        this.toString = toString;

        this.getInstanceStmnt = "Select * from " + tableName + " where " + primaryKey + " = ?"; // NOI18N

        this.getDefaultInstanceStmnt = "Select * from " + tableName + " where " + primaryKey // NOI18N
                    + " = (select min( "                                                     // NOI18N
                    + primaryKey + ") from " + tableName + ")";                              // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * get f\u00FCr ObjectIcon.
     *
     * @return  Image
     *
     * @see     #objectIcon
     */
    public final Image getObjectIcon() {
        return objectIcon;
    }

    /**
     * getter f\u00FCr Icon.
     *
     * @return  Image
     *
     * @see     #icon
     */
    public final Image getIcon() {
        return icon;
    }

    /**
     * getter f\u00FCr ID.
     *
     * @return  id of this class
     *
     * @see     #classID
     */
    public final int getID() {
        return id;
    }

    /**
     * getter f\u00FCr name.
     *
     * @return  name of this class
     *
     * @see     #name
     */
    public final String getName() {
        return name;
    }

    /**
     * getter for description.
     *
     * @return  description of this class
     *
     * @see     #description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * F\u00FCgt ein Klassenattribut in die davor vorgesehenen AtrributVectoren ein.<BR>
     *
     * @param   anyAttribute  Klassenattribut
     *
     * @throws  Exception  DOCUMENT ME!
     */
    protected final void addAttribute(final java.lang.Object anyAttribute) throws Exception {
        if (anyAttribute instanceof ClassAttribute) {
            attribs.add((ClassAttribute)anyAttribute);
        } else {
            throw new Exception(" no subtype of ClassAttribute"); // NOI18N
        }
    }

    /**
     * retrives class attributes.
     *
     * @return  list of class attributes
     */
    public final ClassAttribute[] getAttribs() {
        return (ClassAttribute[])attribs.toArray(new ClassAttribute[attribs.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   key  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final ClassAttribute getClassAttribute(final String key) {
        // Todo: irgendwann mal auf ne Hashmap umstellen
        final ClassAttribute[] allCA = getAttribs();

        for (final ClassAttribute ca : allCA) {
            if (ca.getName().toString().equalsIgnoreCase(key)) {
                return ca;
            }
        }

        return null;
    }

    /**
     * retrieves class attributes.
     *
     * @return  collection of class attributes
     */
    public final Collection getAttributes() {
        return attribs;
    }

    /**
     * retrieves an attribute referenced by its name.
     *
     * @param   name  name of an attribute
     *
     * @return  attribute with this name or null if no attribute with this name exists
     */
    public Collection getAttributeByName(final String name) {
        final Iterator iter = getAttributes().iterator();

        final ArrayList attribsByName = new ArrayList();

        while (iter.hasNext()) {
            Attribute a = null;
            a = (Attribute)iter.next();

            if (a.getName().equalsIgnoreCase(name)) {
                attribsByName.add(a);
            }
        }

        return attribsByName;
    }

    /**
     * getter for permissions.
     *
     * @return  permissionHolder contains all permission entries for this class
     */
    public final PermissionHolder getPermissions() {
        return permissions;
    }

    /**
     * retrieves all ids of registered methods in a vector.
     *
     * @return  mothod ids in a vector
     */
    public final LongVector getMethods() {
        return methodIDs;
    }

    /**
     * adds a methd id to the member (container).
     *
     * @param  methodID  id of a method
     */
    public final void addMethodID(final int methodID) {
        if (!methodIDs.contains(methodID)) {
            methodIDs.add(methodID);
        }
    }

    /**
     * setter for methodIDS.
     *
     * @param   methodIDs  method ids in a vector
     *
     * @throws  Exception  java.lang.Exception error
     */
    public final void setMethodIDs(final LongVector methodIDs) throws Exception {
        if (this.methodIDs.size() != 0) {
            throw new Exception("LongVector methodIds of Class allready set use addMethodID instead"); // NOI18N
        }

        this.methodIDs = methodIDs;
    }

    /**
     * Getter for property toString.
     *
     * @return  Value of property toString.
     */
    public String getToString() {
        return toString;
    }

    /**
     * Setter for property toString.
     *
     * @param  toString  New value of property toString.
     */
    public void setToString(final String toString) {
        this.toString = toString;
    }

    /**
     * Getter for property tableName.
     *
     * @return  Value of property tableName.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Setter for property tableName.
     *
     * @param  tableName  New value of property tableName.
     */
    public void setTableName(final String tableName) {
        this.tableName = tableName;
    }

    /**
     * Getter for property primaryKey.
     *
     * @return  Value of property primaryKey.
     */
    public String getPrimaryKey() {
        return primaryKey;
    }

    /**
     * Setter for property primaryKey.
     *
     * @param  primaryKey  New value of property primaryKey.
     */
    public void setPrimaryKey(final String primaryKey) {
        this.primaryKey = primaryKey;
    }

    // mapable
    /**
     * retrieves the key (Mapable) for this class.
     *
     * @return  key to register in a Map
     */
    @Override
    public Object getKey() {
        return new Integer(id);
    }

    /**
     * Getter for property memberAttributeInfos.
     *
     * @return  Value of property memberAttributeInfos.
     */
    public HashMap getMemberAttributeInfos() {
        return memberAttributeInfos;
    }

    /**
     * Setter for property memberAttributeInfos.
     *
     * @param  memberAttributeInfos  New value of property memberAttributeInfos.
     */
    public void setMemberAttributeInfos(final LinkedHashMap memberAttributeInfos) {
        this.memberAttributeInfos = memberAttributeInfos;
    }

    /**
     * adds an AttributeinfoItem to the class. Used during construction of objects of this class
     *
     * @param  mai  Info set about an Attribute of this class's objects
     */
    public void addMemberAttributeInfo(final MemberAttributeInfo mai) {
        memberAttributeInfos.put(mai.getKey(), mai);
    }

//
    /**
     * no longer used.
     *
     * @param       m  object
     *
     * @return      key
     *
     * @deprecated  UNUSED
     */
    @Override
    public Object constructKey(final Mapable m) {
        if (m instanceof Sirius.server.localserver._class.Class) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /**
     * retrieves the names of all attributes of this class'es objects.
     *
     * @return  attribute names
     */
    public Collection getFieldNames() {
        final Iterator iter = memberAttributeInfos.values().iterator();

        final ArrayList fields = new ArrayList(memberAttributeInfos.size());

        while (iter.hasNext()) {
            fields.add(((MemberAttributeInfo)iter.next()).getFieldName());
        }

        return fields;
    }

    /**
     * Getter for property getInstanceStmnt.
     *
     * @return  Value of property getInstanceStmnt.
     */
    public String getGetInstanceStmnt() {
        return getInstanceStmnt;
    }

    /**
     * getter for defaultInstanceStatement.
     *
     * @return  sql statement capable of creating a default instance
     */
    public String getGetDefaultInstanceStmnt() {
        return getDefaultInstanceStmnt;
    }

    /**
     * uses the toStringConverter if applicable.
     *
     * @return  string representation of this class
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Getter for property editor.
     *
     * @return  Value of property editor.
     */
    public String getEditor() {
        return editor;
    }

    /**
     * Setter for property editor.
     *
     * @param  editor  New value of property editor.
     */
    public void setEditor(final String editor) {
        this.editor = editor;
    }

    /**
     * Getter for property renderer.
     *
     * @return  Value of property renderer.
     */
    public String getRenderer() {
        return renderer;
    }

    /**
     * Setter for property renderer.
     *
     * @param  renderer  New value of property renderer.
     */
    public void setRenderer(final String renderer) {
        this.renderer = renderer;
    }

    /**
     * retrieves the names of the sql fields of the corresponding table.
     *
     * @return  list of field name
     */
    public String getSQLFieldNames() {
        final Collection c = getFieldNames();
        final Iterator iter = c.iterator();

        String res = " ("; // NOI18N

        while (iter.hasNext()) {
            res += iter.next();

            if (iter.hasNext()) {
                res += ","; // NOI18N
            }
        }

        res += ") "; // NOI18N

        return res;
    }

    /**
     * setter for arrayElementLink.
     *
     * @param  arrayElementLink  is arrayElemtneLink
     */
    public void setArrayElementLink(final boolean arrayElementLink) {
        this.arrayElementLink = arrayElementLink;
    }

    /**
     * whether objects of the class are mere association between array objects and their elements.
     *
     * @return  is association between arrays and their elements
     */
    public boolean isArrayElementLink() {
        return arrayElementLink;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Policy getPolicy() {
        return permissions.getPolicy();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Policy getAttributePolicy() {
        return attributePolicy;
    }
}
