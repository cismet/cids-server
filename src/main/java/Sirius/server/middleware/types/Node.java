/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.*;

import Sirius.util.*;

import org.apache.commons.lang.builder.HashCodeBuilder;
//import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Superclass of all NodeTypes used as RMI return value is not to be instaniated and therfore declared abstract if you
 * want to instantiate an organisatory Node use it's extension PureNode.
 *
 * @version  $Revision$, $Date$
 */
public abstract class Node implements java.io.Serializable, Groupable // Comparable
{

    //~ Instance fields --------------------------------------------------------

    /** id. */
    protected int id;
    /** domain. */
    protected String domain;
    /**
     * indicates wheter this node is a leaf (has no children) by default has children as this will cause the least
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
    /** container for permissions. */
    PermissionHolder permissions;

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

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
        this.id = id;
        this.domain = domain;
        this.name = name;
        this.description = description;
        this.leaf = leaf;
        this.permissions = new PermissionHolder(policy);
        this.iconFactory = iconFactory;
        this.derivePermissionsFromClass = derivePermissionsFromClass;
        this.iconString = icon;
    }
//------------------------------------------------------------------------------------------

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
//        System.out.println("toString : "+name);
//        if(log != null) {
//            log.fatal("toString : "+name);
//        }
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
//        log.fatal("setName(" + name + ")", new Exception());
//        if (log != null) {
//            log.fatal("setName() to " + name);
//        } else {
//            System.out.println("getName() returns " + name);
//        }
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
        // return ((Node)o).id-this.id;
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
}
