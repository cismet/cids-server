/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.attribute;

import Sirius.server.middleware.types.MetaObject;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.newuser.permission.Policy;

import Sirius.util.Mapable;

import org.apache.log4j.Logger;

import java.io.Serializable;

import de.cismet.cids.tools.tostring.StringConvertable;
import de.cismet.cids.tools.tostring.ToStringConverter;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public abstract class Attribute implements Mapable, Serializable, StringConvertable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(Attribute.class);

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

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Attribute object.
     *
     * @param  a  DOCUMENT ME!
     */
    public Attribute(final Attribute a) {
        this.id = a.id;
        this.name = a.name.trim();
        // TODO: error
        this.description = a.description;
        this.permissions = a.getPermissions();
        this.visible = a.visible;
        this.referencesObject = a.referencesObject;
    }

    /**
     * Creates a new Attribute object.
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
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getID() {
        return id;
    }

    // Mapable schl\u00FCssel \u00E4ndern xxx
    @Override
    public Object getKey() {
        return id + ""; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final Object getValue() {
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

    /**
     * Setter for property value.
     *
     * @param  value  New value of property value.
     */
    public void setValue(final Object value) {
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
            } else if (referencesObject && (value instanceof MetaObject)) {
                return ((MetaObject)value).toString();
            } else {
                return value.toString();
            }
        } else {
            return ""; // NOI18N
        }
    }

    /**
     * Setter for property visible.
     *
     * @param  visible  New value of property visible.
     */
    public final void setVisible(final boolean visible) {
        this.visible = visible;
    }

    /**
     * Getter for property substitute.
     *
     * @return  Value of property substitute.
     */
    public final boolean isSubstitute() {
        return substitute;
    }

    /**
     * Setter for property substitute.
     *
     * @param  substitute  New value of property substitute.
     */
    public final void setSubstitute(final boolean substitute) {
        this.substitute = substitute;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final String getClassKey() {
        return classKey;
    }

    /**
     * Setter for property classKey.
     *
     * @param  classKey  New value of property classKey.
     */
    public final void setClassKey(final java.lang.String classKey) {
        this.classKey = classKey;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  typeId  DOCUMENT ME!
     */
    public final void setTypeId(final int typeId) {
        this.typeId = typeId;
    }

    /**
     * Getter for property typeId.
     *
     * @return  Value of property typeId.
     */
    public final int getTypeId() {
        return typeId;
    }

    /**
     * Getter for property changed.
     *
     * @return  Value of property changed.
     */
    public final boolean isChanged() {
        return changed;
    }

    /**
     * Setter for property changed.
     *
     * @param  changed  New value of property changed.
     */
    public final void setChanged(final boolean changed) {
        this.changed = changed;
    }

    /**
     * DOCUMENT ME!
     */
    public final void setValuesNull() {
        if (!referencesObject) {
            value = null;
            if (LOG.isDebugEnabled()) {
                LOG.debug("would set " + value + " to null"); // NOI18N
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
    public final boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    /**
     * Setter for property isPrimaryKey.
     *
     * @param       isPrimaryKey  New value of property isPrimaryKey.
     *
     * @deprecated  does not change value in MemberAttributeInfo!
     */
    @Deprecated
    public final void setIsPrimaryKey(final boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    /**
     * Getter for property javaType.
     *
     * @return      Value of property javaType.
     *
     * @deprecated  does not change value in MemberAttributeInfo!
     */
    @Deprecated
    public final String getJavaType() {
        return javaType;
    }

    /**
     * Setter for property javaType.
     *
     * @param  javaType  New value of property javaType.
     */
    public final void setJavaType(final String javaType) {
        this.javaType = javaType;
    }

    /**
     * Getter for property isArray.
     *
     * @return  Value of property isArray.
     */
    public final boolean isArray() {
        return isArray;
    }

    /**
     * Setter for property isArray.
     *
     * @param       isArray  New value of property isArray.
     *
     * @deprecated  does not change value in MemberAttributeInfo!
     */
    @Deprecated
    public final void setIsArray(final boolean isArray) {
        this.isArray = isArray;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  id  DOCUMENT ME!
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * DOCUMENT ME!
     */
    @Deprecated
    public void printMe() {
        System.out.println(name + " : " + value); // NOI18N
    }

    /**
     * Getter for property toString.
     *
     * @return  Value of property toString.
     */
    public ToStringConverter getToStringConverter() {
        return toStringConverter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param       optional  DOCUMENT ME!
     *
     * @deprecated  does not change value in MemberAttributeInfo!
     */
    @Deprecated
    public final void setOptional(final boolean optional) {
        this.optional = optional;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isOptional() {
        return optional;
    }
}
