/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import Sirius.server.middleware.types.*;
import Sirius.server.newuser.permission.*;

import Sirius.util.*;

import de.cismet.cids.tools.tostring.StringConvertable;
import de.cismet.cids.tools.tostring.ToStringConverter;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public abstract class Attribute implements Mapable, java.io.Serializable, StringConvertable {

    //~ Instance fields --------------------------------------------------------

    // warum string?? totaler unsinn bei Gelgenheit nochmal int (vorher checken was mit class_attribs ist)
    // id des Attributs (des Datensatzes in cs_attr)
    protected String id; // Hell: String wird glaube ich bei den Arrays benoetigt: id+"."+counter

    protected String name;

    protected String description;

    protected boolean visible;

    protected PermissionHolder permissions;

    // attribute is foreign key
    protected boolean referencesObject = false;

    protected boolean substitute;

    /** This attributes value. */
    protected java.lang.Object value;

    /** The Classkey of the value if the value is a metaobject. */
    protected String classKey = null;

    // sql type
    protected int typeId;

    protected boolean changed;

    protected boolean isPrimaryKey = false;

    protected String javaType;

    protected boolean isArray = false;

    protected transient ToStringConverter toStringConverter;

    // protected String defaultValue="";

    protected boolean optional = false;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    /////////////////members////////////////////////////////

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Attribute object.
     *
     * @param  a  DOCUMENT ME!
     */
    public Attribute(final Attribute a) {
        this.id = a.id;
        this.name = new String(a.name).trim();
        this.description = description;
        this.permissions = a.getPermissions();
        this.visible = a.visible;
        this.referencesObject = a.referencesObject;
    }

    /**
     * //////////////constructors/////////////////////////////
     *
     * @param  id           DOCUMENT ME!
     * @param  name         DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     * @param  policy       DOCUMENT ME!
     */
    public Attribute(final String id, final String name, final String description, final Policy policy) {
        this.id = id;

        this.name = name;
        this.description = description;
        this.permissions = new PermissionHolder(policy);
        this.visible = true;
        this.referencesObject = false;
    }

    /**
     * Creates a new Attribute object.
     *
     * @param  id           DOCUMENT ME!
     * @param  name         DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     * @param  permissions  DOCUMENT ME!
     */
    public Attribute(final String id, final String name, final String description, final PermissionHolder permissions) {
        this(id, name, description, (Policy)null);
        this.permissions = permissions;
    }

    /**
     * Creates a new Attribute object.
     *
     * @param  id           DOCUMENT ME!
     * @param  name         DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     * @param  visible      DOCUMENT ME!
     * @param  policy       DOCUMENT ME!
     */
    public Attribute(final String id,
            final String name,
            final String description,
            final boolean visible,
            final Policy policy) {
        this(id, name, description, policy);
        this.visible = visible;
    }

    /**
     * Creates a new Attribute object.
     *
     * @param  id           DOCUMENT ME!
     * @param  name         DOCUMENT ME!
     * @param  description  DOCUMENT ME!
     * @param  permissions  DOCUMENT ME!
     * @param  visible      DOCUMENT ME!
     */
    public Attribute(final String id,
            final String name,
            final String description,
            final PermissionHolder permissions,
            final boolean visible) {
        this(id, name, description, permissions);
        this.visible = visible;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * //////////////methods//////////////////////////////////////////
     *
     * @return  DOCUMENT ME!
     */
    public final String getID() {
        return id;
    }

    // Mapable schl\u00FCssel \u00E4ndern xxx
    @Override
    public Object getKey() {
        return id + "";
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final java.lang.Object getValue() {
        return value;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getName() {
        return name;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final PermissionHolder getPermissions() {
        return permissions;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean referencesObject() {
        return referencesObject;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  b  DOCUMENT ME!
     */
    public final void setReferencesObject(final boolean b) {
        referencesObject = b;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  permissions  DOCUMENT ME!
     */
    public final void setPermissions(final PermissionHolder permissions) {
        this.permissions = permissions;
    }

//    final public void addPermission(Mapable userGroup)
//    {
//        permissions.addPermission(userGroup);
//    }
//

    /**
     * Setter for property value.
     *
     * @param  value  New value of property value.
     */
    public void setValue(final java.lang.Object value) {
        this.value = value;
    }

    @Override
    public Object constructKey(final Mapable m) {
        if (m instanceof Attribute) {
            return m.getKey();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        if (value != null) {
            if (toStringConverter != null) {
                return toStringConverter.convert(this);
            } else if (referencesObject /*&& value!=null*/ && (value instanceof MetaObject)) {
                return ((MetaObject)value).toString();
            } else {
                return value.toString();
            }
        } else {
            return "";
        }
    }

    /**
     * Setter for property visible.
     *
     * @param  visible  New value of property visible.
     */
    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * Getter for property substitute.
     *
     * @return  Value of property substitute.
     */
    public boolean isSubstitute() {
        return substitute;
    }

    /**
     * Setter for property substitute.
     *
     * @param  substitute  New value of property substitute.
     */
    public void setSubstitute(final boolean substitute) {
        this.substitute = substitute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getClassKey() {
        if (classKey != null) {
            return classKey;
        }
//         else if(referencesObject && value!= null && value instanceof MetaObject)
//            return ((MetaObject)value).getClassKey();
        else {
            if (logger != null) {
                logger.error("Attribute Value kein Type f\u00FCr getCLassKey ::" + value.getClass());
            }
            return null;
        }
    }

    /**
     * Setter for property classKey.
     *
     * @param  classKey  New value of property classKey.
     */
    public void setClassKey(final java.lang.String classKey) {
        this.classKey = classKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  typeId  DOCUMENT ME!
     */
    public void setTypeId(final int typeId) {
        this.typeId = typeId;
    }

    /**
     * Getter for property typeId.
     *
     * @return  Value of property typeId.
     */
    public int getTypeId() {
        return typeId;
    }

    /**
     * Getter for property changed.
     *
     * @return  Value of property changed.
     */
    public boolean isChanged() {
        return changed;
    }

    /**
     * Setter for property changed.
     *
     * @param  changed  New value of property changed.
     */
    public void setChanged(final boolean changed) {
        this.changed = changed;
    }

    /**
     * DOCUMENT ME!
     */
    public void setValuesNull() {
        if (!referencesObject) {
            value = null;
            if (logger.isDebugEnabled()) {
                logger.debug("would set " + value + " to null");
            }
        } else {
            ((Sirius.server.localserver.object.Object)value).setValuesNull();
        }
    }

    /**
     * Getter for property isPrimaryKey.
     *
     * @return  Value of property isPrimaryKey.
     */
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * Setter for property isPrimaryKey.
     *
     * @param  isPrimaryKey  New value of property isPrimaryKey.
     */
    public void setIsPrimaryKey(final boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    /**
     * Getter for property javaType.
     *
     * @return  Value of property javaType.
     */
    public java.lang.String getJavaType() {
        return javaType;
    }

    /**
     * Setter for property javaType.
     *
     * @param  javaType  New value of property javaType.
     */
    public void setJavaType(final java.lang.String javaType) {
        this.javaType = javaType;
    }

    /**
     * Getter for property isArray.
     *
     * @return  Value of property isArray.
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * Setter for property isArray.
     *
     * @param  isArray  New value of property isArray.
     */
    public void setIsArray(final boolean isArray) {
        this.isArray = isArray;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     */
    public void printMe() {
        System.out.println(name + " : " + value);
    }

    /**
     * Getter for property toString.
     *
     * @return  Value of property toString.
     */
    public de.cismet.cids.tools.tostring.ToStringConverter getToStringConverter() {
        return toStringConverter;
    }

    /**
     * Setter for property toString.
     *
     * @param  toString  New value of property toString.
     */
    public void setToString(final de.cismet.cids.tools.tostring.ToStringConverter toString) {
        this.toStringConverter = toStringConverter;
    }
    /**
     * public void setDefaultValue(String val){this.defaultValue=val;}.
     *
     * @param  optional  DOCUMENT ME!
     */
    public void setOptional(final boolean optional) {
        this.optional = optional;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isOptional() {
        return optional;
    }
}
