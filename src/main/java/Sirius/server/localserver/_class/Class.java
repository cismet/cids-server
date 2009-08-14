package Sirius.server.localserver._class;

import Sirius.util.*;
import Sirius.server.localserver.attribute.*;
import Sirius.server.localserver.method.*;
import Sirius.util.image.*;
import de.cismet.tools.collections.*;
import Sirius.server.newuser.permission.*;
import de.cismet.cids.tools.tostring.ToStringConverter;
import java.util.*;

/** Die Klasse Class fungiert zum einen als Mittel zur Klassifkation zum anderen enth\u00E4lt Sie Eigenschaften von
 * referenzierten Tabellen
 */
public class Class implements java.io.Serializable, Mapable {

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /**Fungiert als Klassenreferenz in einem assoziativen Container <BR>
     *@see Sirius.Class.ClassMap
     */
    protected int id;
    /** Name der Klasse wird bei der Visualisierung angzeigt*/
    protected String name;
    /**  Enth\u00E4lt eine URL oder einen Klartext der die Klasse n\u00E4her beschreibt*/
    protected String description;
    /**  Icon wird bei der Visualisierung der Klasse angzeigt*/
    protected Image icon;
    /**  Icon wird bei der Visualisierung eines Objekts der Klasse angzeigt*/
    protected Image objectIcon;
    /**Zugriffsrechte bzgl. Benutzergruppen*/
    protected PermissionHolder permissions;
    /**zur Klasse geh\u00F6render Tabellenname*/
    protected String tableName;
    /**Primaeschluessel der zur Klasse geh\u00F6renden Tabelle*/
    protected String primaryKey;
    /**voll qualifizierter Klassename zur erzeugung einer Stringrepr\u00E4sentation von Objekten dieser Klasse*/
    protected String toString;
    /**
     * indicates whether objects of this class are only links between an array and it's elements
     */
    protected boolean arrayElementLink = false;
//    /**Alle der Klasse zugeordneten "logischen" Methoden durch ihre ids repraesentiert*/
    protected LongVector methodIDs;
    /**  Alle Attribute der Klasse*/
    protected AttributeVector attribs;
    /**
     * enth\u00E4lt information \u00FCber die Attribute der Objekte der Klasse, diesen werden zur Konstruktion von Objekten ben\u00F6tigt
     */
    protected LinkedHashMap memberAttributeInfos;
    /**
     * sql statement welches eine instanz eines meta objektes dieser Klasse erzeugt
     */
    protected String getInstanceStmnt;
    /**
     * statement welches ein template eines meta objektes dieser Klasser erzeugt
     */
    protected String getDefaultInstanceStmnt;
    /**
     * definiert die java Klasse welche als Editor f\u00FCr diese Art von Objekten benutzt werden soll
     */
    protected String editor;
    /**
     * 
     */
    protected String renderer;
    protected Policy attributePolicy;

    //////////////////// constructors///////////////////////////////////////
    /**
     * Erzeug eine unattributierte Klasse <BR>
     * @param id this class key unique on on domain
     * @param name name of this class
     * @param description -
     * @param icon nodes representing this class will be vizualized using this icon
     * @param objectIcon objects of this class will be vizualized using this icon
     * @param tableName name of this class' corresponding table
     * @param primaryKey primary key of the table
     * @param toString class able to create a String representation of this class's objects
     * @param permissions permission container
     */
    public Class(int id, String name, String description, Image icon, Image objectIcon, String tableName, String primaryKey, String toString, PermissionHolder permissions, Policy attributePolicy) {

        this(id, name, description, icon, objectIcon, tableName, primaryKey, toString, (Policy) null, attributePolicy);
        this.permissions = permissions;


    }

    /**
     * Erzeug eine unattributierte Klasse <BR>
     * @param id key
     * @param name name der Klasse
     * @param description Beschreibung der Klasse
     * @param icon nodes representing this class will be vizualized using this icon
     * @param objectIcon objects of this class will be vizualized using this icon
     * @param tableName name of this class' corresponding table
     * @param primaryKey primary key of the table
     * @param toString class able to create a String representation of this class's objects
     */
    public Class(int id, String name, String description, Image icon, Image objectIcon, String tableName, String primaryKey, String toString, Policy policy, Policy attributePolicy) {
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

        this.getInstanceStmnt = "Select * from " + tableName + " where " + primaryKey + " = ?";

        this.getDefaultInstanceStmnt = "Select * from " + tableName + " where " + primaryKey + " = (select min( " + primaryKey + ") from " + tableName + ")";




    }

    //////////////////methods/////////////////////////////////////////////////
    //--------------------------------------------------
    /**
     * get f\u00FCr ObjectIcon
     * @see #objectIcon
     * @return Image
     */
    public final Image getObjectIcon() {
        return objectIcon;
    }

    //----------------------------------------------------------------------------
    /**
     * getter f\u00FCr Icon
     * @see #icon
     * @return Image
     */
    public final Image getIcon() {
        return icon;
    }

    //-------------------------------------------------------------------
    /**
     * getter f\u00FCr ID
     * @see #classID
     * @return id of this class
     */
    public final int getID() {
        return id;
    }

    //--------------------------------------------------------------------
    /**
     * getter f\u00FCr name
     * @see #name
     * @return name of this class
     */
    public final String getName() {
        return name;
    }

    //--------------------------------------------------------------------
    /**
     * getter for description
     * @see #description
     * @return description of this class
     */
    public final String getDescription() {
        return description;
    }

    //-------------------------------------------------------------------------
    /**
     * F\u00FCgt ein Klassenattribut in die davor vorgesehenen AtrributVectoren ein <BR>
     * @param anyAttribute Klassenattribut
     * @throws java.lang.Exception fehler .-)
     */
    protected final void addAttribute(java.lang.Object anyAttribute) throws Exception {
        if (anyAttribute instanceof ClassAttribute) {
            attribs.add((ClassAttribute) anyAttribute);
        }// end if AttributeOfClass
        else {
            throw new java.lang.Exception(" no subtype of ClassAttribute");
        }


    }// end of addAttribute

    //-----------------------------------------------------------------------------------
    /**
     * retrives class attributes
     * @return list of class attributes
     */
    public final ClassAttribute[] getAttribs() {
        return (ClassAttribute[]) attribs.toArray(new ClassAttribute[attribs.size()]);
    }

    public final ClassAttribute getClassAttribute(String key){
        //Todo: irgendwann mal auf ne Hashmap umstellen
         ClassAttribute[] allCA=getAttribs();
         for (ClassAttribute ca:allCA){
             if (ca.getName().toString().equalsIgnoreCase(key)){
                 return ca;
             }
         }
         return null;
    }



    /**
     * retrieves class attributes
     * @return collection of class attributes
     */
    public final Collection getAttributes() {
        return attribs;
    }

    /**
     * retrieves an attribute referenced by its name
     * @param name name of an attribute
     * @return attribute with this name or null if no attribute with this name exists
     */
    public Collection getAttributeByName(String name) {
        Iterator iter = getAttributes().iterator();

        ArrayList attribsByName = new ArrayList();


        while (iter.hasNext()) {
            Attribute a = null;
            a = (Attribute) iter.next();

            if (a.getName().equalsIgnoreCase(name)) {
                attribsByName.add(a);
            }


        }


        return attribsByName;


    }
    //-----------------------------------------------------------------------------------

    /**
     * getter for permissions
     * @return permissionHolder contains all permission entries for this class
     */
    public final PermissionHolder getPermissions() {
        return permissions;
    }

    //-----------------------------------------------------------------------------------
    /**
     * setter for permissions
     * @param permission set permissions or initialize this class' permissionHolder
     */
    public final void setPermissions(PermissionHolder permission) {
        this.permissions = permissions;
    }

    //-----------------------------------------------------------------------------------
    /**
     * retrieves all ids of registered methods in a vector
     * @return mothod ids in a vector
     */
    public final LongVector getMethods() {
        return methodIDs;
    }

    //------------------------------------------------------------------------------------
    /**
     * adds a methd id to the member (container)
     * @param methodID id of a method
     */
    public final void addMethodID(int methodID) {
        if (!methodIDs.contains(methodID)) {
            methodIDs.add(methodID);
        }
    }

    /**
     * setter for methodIDS
     * @param methodIDs method ids in a vector
     * @throws java.lang.Exception error
     */
    public final void setMethodIDs(LongVector methodIDs) throws Exception {
        if (this.methodIDs.size() != 0) {
            throw new Exception("LongVector methodIds of Class allready set use addMethodID instead");
        }

        this.methodIDs = methodIDs;

    }

    /** Getter for property toString.
     * @return Value of property toString.
     *
     */
    public java.lang.String getToString() {
        return toString;
    }

    /** Setter for property toString.
     * @param toString New value of property toString.
     *
     */
    public void setToString(java.lang.String toString) {
        this.toString = toString;
    }

    /** Getter for property tableName.
     * @return Value of property tableName.
     *
     */
    public java.lang.String getTableName() {
        return tableName;
    }

    /** Setter for property tableName.
     * @param tableName New value of property tableName.
     *
     */
    public void setTableName(java.lang.String tableName) {
        this.tableName = tableName;
    }

    /** Getter for property primaryKey.
     * @return Value of property primaryKey.
     *
     */
    public java.lang.String getPrimaryKey() {
        return primaryKey;
    }

    /** Setter for property primaryKey.
     * @param primaryKey New value of property primaryKey.
     *
     */
    public void setPrimaryKey(java.lang.String primaryKey) {
        this.primaryKey = primaryKey;
    }

    //mapable
    /**
     * retrieves the key (Mapable) for this class
     * @return key to register in a Map
     */
    public Object getKey() {

        return new Integer(id);
    }

    /** Getter for property memberAttributeInfos.
     * @return Value of property memberAttributeInfos.
     *
     */
    public java.util.HashMap getMemberAttributeInfos() {
        return memberAttributeInfos;
    }

    /** Setter for property memberAttributeInfos.
     * @param memberAttributeInfos New value of property memberAttributeInfos.
     *
     */
    public void setMemberAttributeInfos(java.util.LinkedHashMap memberAttributeInfos) {
        this.memberAttributeInfos = memberAttributeInfos;
    }

    /**
     * adds an AttributeinfoItem to the class. Used during construction of objects of this class
     * @param mai Info set about an Attribute of this class's objects
     */
    public void addMemberAttributeInfo(MemberAttributeInfo mai) {
        memberAttributeInfos.put(mai.getKey(), mai);

    }

//    
    /**
     * no longer used
     * @deprecated UNUSED
     * @param m object
     * @return key
     */
    public Object constructKey(Mapable m) {
        if (m instanceof Sirius.server.localserver._class.Class) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /**
     * retrieves the names of all attributes of this class'es objects
     * @return attribute names
     */
    public Collection getFieldNames() {
        Iterator iter = memberAttributeInfos.values().iterator();

        ArrayList fields = new ArrayList(memberAttributeInfos.size());


        while (iter.hasNext()) {
            fields.add(((MemberAttributeInfo) iter.next()).getFieldName());

        }

        return fields;

    }

    /** Getter for property getInstanceStmnt.
     * @return Value of property getInstanceStmnt.
     *
     */
    public java.lang.String getGetInstanceStmnt() {
        return getInstanceStmnt;
    }

    /**
     * getter for defaultInstanceStatement
     * @return sql statement capable of creating a default instance
     */
    public java.lang.String getGetDefaultInstanceStmnt() {
        return getDefaultInstanceStmnt;
    }

    /**
     * uses the toStringConverter if applicable
     * @return string representation of this class
     */
    public String toString() {
        return getName();
    }

    /**
     * Getter for property editor.
     * @return Value of property editor.
     */
    public java.lang.String getEditor() {
        return editor;
    }

    /**
     * Setter for property editor.
     * @param editor New value of property editor.
     */
    public void setEditor(java.lang.String editor) {
        this.editor = editor;
    }

    /**
     * Getter for property renderer.
     * @return Value of property renderer.
     */
    public java.lang.String getRenderer() {
        return renderer;
    }

    /**
     * Setter for property renderer.
     * @param renderer New value of property renderer.
     */
    public void setRenderer(java.lang.String renderer) {
        this.renderer = renderer;
    }

    /**
     * retrieves the names of the sql fields of the corresponding table
     * @return list of field name
     */
    public String getSQLFieldNames() {
        Collection c = getFieldNames();
        Iterator iter = c.iterator();

        String res = " (";

        while (iter.hasNext()) {
            res += iter.next();

            if (iter.hasNext()) {
                res += ",";
            }

        }

        res += ") ";
        return res;
    }

    /**
     * setter for arrayElementLink
     * @param arrayElementLink is arrayElemtneLink
     */
    public void setArrayElementLink(boolean arrayElementLink) {
        this.arrayElementLink = arrayElementLink;
    }

    /**
     * whether objects of the class are mere association between array objects and their elements
     * @return is association between arrays and their elements
     */
    public boolean isArrayElementLink() {
        return arrayElementLink;
    }

    public Policy getPolicy() {
        return permissions.getPolicy();
    }

    public Policy getAttributePolicy() {
        return attributePolicy;
    }
}