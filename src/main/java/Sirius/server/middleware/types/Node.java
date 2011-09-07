/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.newuser.permission.Policy;

import Sirius.util.Groupable;

import org.apache.commons.lang.builder.HashCodeBuilder;

import static de.cismet.tools.Equals.nullEqual;

/**
 * Superclass of all NodeTypes used as RMI return value is not to be instaniated and therfore declared abstract if you
 * want to instantiate an organisatory Node use it's extension PureNode.
 *
 * @version  $Revision$, $Date$
 */
// FIXME: why is this not comparable although the compareTo operation is implemented and subtypes such as MetaObjectNode
// implement the comparable interface but don't implement the operation
public abstract class Node implements java.io.Serializable, Groupable // Comparable
{

    //~ Instance fields --------------------------------------------------------

    /** id. */
    protected int id;
    /** domain. */
    protected final String domain;
    /**
     * indicates whether this node is a leaf (has no children) by default has children as this will cause the least
     * problems.
     */
    protected boolean leaf = false;
    /** name. */
    protected String name;
    /** description. */
    protected String description;
    protected boolean isValid = true;
    protected boolean dynamic;
    protected String dynamicChildrenStatement; // indicates whether the dynamicChildrenStatement contains sorting so no
                                               // further sorting should be performed
    protected boolean sqlSort = false;
    protected int classId = -1;
    protected int iconFactory = -1;
    protected boolean derivePermissionsFromClass = false;
    protected String iconString = null;
    protected String artificialId = null;
    /** container for permissions. */
    PermissionHolder permissions;

    //~ Constructors -----------------------------------------------------------

    /**
     * constructor.
     *
     * @param  id                          id
     * @param  name                        name
     * @param  domain                      domain
     * @param  description                 description
     * @param  leaf                        has no children
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     */
    public Node(final int id,
            final String name,
            final String domain,
            final String description,
            final boolean leaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass) {
        this(id, name, domain, description, leaf, policy, iconFactory, icon, derivePermissionsFromClass, null);
    }

    /**
     * Creates a new Node object.
     *
     * @param   id                          DOCUMENT ME!
     * @param   name                        DOCUMENT ME!
     * @param   domain                      DOCUMENT ME!
     * @param   description                 DOCUMENT ME!
     * @param   leaf                        DOCUMENT ME!
     * @param   policy                      DOCUMENT ME!
     * @param   iconFactory                 DOCUMENT ME!
     * @param   icon                        DOCUMENT ME!
     * @param   derivePermissionsFromClass  DOCUMENT ME!
     * @param   artificalId                 DOCUMENT ME!
     *
     * @throws  IllegalArgumentException  DOCUMENT ME!
     */
    public Node(final int id,
            final String name,
            final String domain,
            final String description,
            final boolean leaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass,
            final String artificalId) {
        if (domain == null) {
            throw new IllegalArgumentException("domain must not be null"); // NOI18N
        }

        this.id = id;
        this.domain = domain;
        this.name = name;
        this.description = description;
        this.leaf = leaf;
        this.permissions = new PermissionHolder(policy);
        this.iconFactory = iconFactory;
        this.derivePermissionsFromClass = derivePermissionsFromClass;
        this.iconString = icon;
        this.artificialId = artificalId;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * getter for group.
     *
     * @return  grouping information
     */
    @Override
    public String getGroup() {
        return domain;
    }

    /**
     * getter for id.
     *
     * @return  id
     */
    @Override
    public final int getId() {
        return id;
    }

    /**
     * getter for domain.
     *
     * @return  domain
     */
    public final String getDomain() {
        return domain;
    }

    /**
     * getter for leaf.
     *
     * @return  has no child nodes
     */
    public boolean isLeaf() {
        return leaf;
    }

    /**
     * getter for name.
     *
     * @return  name
     */
    public String getName() {
        return name;
    }

    /**
     * getter for description.
     *
     * @return  description
     */
    public String getDescription() {
        return description;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public final boolean isValid() {
        return isValid;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  isValid  DOCUMENT ME!
     */
    public final void validate(final boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * setter for leaf property.
     *
     * @param  leaf  has no child nodes
     */
    public void setLeaf(final boolean leaf) {
        this.leaf = leaf;
    }

    /**
     * retrieve string represntation of this node.
     *
     * @return  string representation
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Getter for property permissions.
     *
     * @return  Value of property permissions.
     */
    public PermissionHolder getPermissions() {
        return permissions;
    }

    /**
     * Setter for property permissions.
     *
     * @param  permissions  New value of property permissions.
     */
    public void setPermissions(final PermissionHolder permissions) {
        this.permissions = permissions;
    }

    /**
     * Setter for property name.
     *
     * @param  name  New value of property name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * comparable.
     *
     * @param   o  other object
     *
     * @return  ordinal relation
     */
    public int compareTo(final Object o) {
        return this.name.compareTo(((Node)o).name);
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hb = new HashCodeBuilder();

        hb.append(id);
        hb.append(domain);

        return hb.toHashCode();
    }

    /**
     * equals.
     *
     * @param   other  o other object
     *
     * @return  whether this equals o
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof Node)) {
            return false;
        }
        final Node o = (Node)other;

        return (id == o.id) && domain.equals(o.domain);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   other  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean deepEquals(final Object other) {
        if (!(other instanceof Node)) {
            return false;
        }

        if (this == other) {
            return true;
        }

        final Node n = (Node)other;

        return nullEqual(id, n.id)
                    && nullEqual(name, n.name)
                    && nullEqual(description, n.description)
                    && nullEqual(classId, n.classId)
                    && nullEqual(dynamicChildrenStatement, n.dynamicChildrenStatement)
                    && nullEqual(sqlSort, n.sqlSort)
                    && nullEqual(derivePermissionsFromClass, n.derivePermissionsFromClass)
                    && nullEqual(iconFactory, n.iconFactory)
                    && nullEqual(iconString, n.iconString)
                    && nullEqual(domain, n.domain)
                    && nullEqual(artificialId, n.artificialId);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isDynamic() {
        return dynamic;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dynamic  DOCUMENT ME!
     */
    // FIXME: a node is dynamic if the getDynamicChildrenStatement operation does not return null
    public void setDynamic(final boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDynamicChildrenStatement() {
        return dynamicChildrenStatement;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  dynamicChildrenStatement  DOCUMENT ME!
     */
    public void setDynamicChildrenStatement(final String dynamicChildrenStatement) {
        this.dynamicChildrenStatement = dynamicChildrenStatement;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSqlSort() {
        return sqlSort;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sqlSort  DOCUMENT ME!
     */
    public void setSqlSort(final boolean sqlSort) {
        this.sqlSort = sqlSort;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getClassId() {
        return classId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classId  DOCUMENT ME!
     */
    public void setClassId(final int classId) {
        this.classId = classId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isDerivePermissionsFromClass() {
        return derivePermissionsFromClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     */
    public void setDerivePermissionsFromClass(final boolean derivePermissionsFromClass) {
        this.derivePermissionsFromClass = derivePermissionsFromClass;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getIconFactory() {
        return iconFactory;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  iconFactory  DOCUMENT ME!
     */
    public void setIconFactory(final int iconFactory) {
        this.iconFactory = iconFactory;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getIconString() {
        return iconString;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  iconString  DOCUMENT ME!
     */
    public void setIconString(final String iconString) {
        this.iconString = iconString;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getArtificialId() {
        return artificialId;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  artificialId  DOCUMENT ME!
     */
    public void setArtificialId(final String artificialId) {
        this.artificialId = artificialId;
    }
}
