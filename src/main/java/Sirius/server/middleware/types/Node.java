package Sirius.server.middleware.types;

import Sirius.util.*;
import Sirius.server.newuser.permission.*;
import org.apache.commons.lang.builder.HashCodeBuilder;
//import org.apache.commons.lang.builder.ToStringBuilder;

/**
 *Superclass of all NodeTypes used as RMI return value
 *is not to be instaniated and therfore declared abstract
 *if you want to instantiate an organisatory Node use it's extension PureNode
 */
public abstract class Node implements java.io.Serializable, Groupable //Comparable
{

    private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    /**
     * id
     */
    protected int id;
    /**
     * domain
     */
    protected String domain;
    /**
     * indicates wheter this node is a leaf (has no children) by default has children as this will cause the least problems
     */
    protected boolean leaf = false;
    /**
     * name
     */
    protected String name;
    /**
     * description
     */
    protected String description;
    protected boolean isValid = true;
    /**
     * container for permissions
     */
    PermissionHolder permissions;
    protected boolean dynamic;
    protected String dynamicChildrenStatement;    //indicates whether the dynamicChildrenStatement contains sorting so no further sorting should be performed
    protected boolean sqlSort = false;
    protected int classId = -1;
    protected int iconFactory = -1;
    protected boolean derivePermissionsFromClass = false;
    protected String iconString = null;

    /**
     * constructor
     * @param id id
     * @param name name
     * @param domain domain
     * @param description description
     * @param leaf has no children
     */
    public Node(int id, String name, String domain, String description, boolean leaf, Policy policy, int iconFactory, String icon, boolean derivePermissionsFromClass) {

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

    /**
     * getter for group
     * @return grouping information
     */
    public String getGroup() {
        return domain;
    }

    /**
     * getter for id
     * @return id
     */
    public final int getId() {
        return id;
    }

    /**
     * getter for domain
     * @return domain
     */
    public final String getDomain() {
        return domain;
    }

    /**
     * getter for leaf
     * @return has no child nodes
     */
    public boolean isLeaf() {

        return leaf;

    }

    /**
     * getter for name
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * getter for description
     * @return description
     */
    public String getDescription() {
        return description;
    }

    public final boolean isValid() {
        return isValid;
    }

    public final void validate(boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * setter for leaf property
     * @param leaf has no child nodes
     */
    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
    }

    /**
     * retrieve string represntation of this node
     * @return string representation
     */
    public String toString() {
//        System.out.println("toString : "+name);
//        if(log != null) {
//            log.fatal("toString : "+name);
//        }
        return name;
    }

    /**
     * Getter for property permissions.
     * @return Value of property permissions.
     */
    public PermissionHolder getPermissions() {
        return permissions;
    }

    /**
     * Setter for property permissions.
     * @param permissions New value of property permissions.
     */
    public void setPermissions(PermissionHolder permissions) {
        this.permissions = permissions;
    }

    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(String name) {
//        log.fatal("setName(" + name + ")", new Exception());
//        if (log != null) {
//            log.fatal("setName() to " + name);
//        } else {
//            System.out.println("getName() returns " + name);
//        }
        this.name = name;
    }

    /**
     * comparable
     * @param o other object
     * @return ordinal relation
     */
    public int compareTo(Object o) {
        //return ((Node)o).id-this.id;
        return this.name.compareTo(((Node) o).name);
    }

    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder();

        hb.append(id);
        hb.append(domain);

        return hb.toHashCode();

    }

    /**
     * equals
     * @param o other object
     * @return whether this equals o
     */
    public boolean equals(Object other) {

        if (!(other instanceof Node)) {
            return false;
        }
        Node o = (Node) other;

        return id == o.id && domain.equals(o.domain);

    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public String getDynamicChildrenStatement() {
        return dynamicChildrenStatement;
    }

    public void setDynamicChildrenStatement(String dynamicChildrenStatement) {
        this.dynamicChildrenStatement = dynamicChildrenStatement;
    }

    public boolean isSqlSort() {
        return sqlSort;
    }

    public void setSqlSort(boolean sqlSort) {
        this.sqlSort = sqlSort;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public boolean isDerivePermissionsFromClass() {
        return derivePermissionsFromClass;
    }

    public void setDerivePermissionsFromClass(boolean derivePermissionsFromClass) {
        this.derivePermissionsFromClass = derivePermissionsFromClass;
    }

    public int getIconFactory() {
        return iconFactory;
    }

    public void setIconFactory(int iconFactory) {
        this.iconFactory = iconFactory;
    }

    public String getIconString() {
        return iconString;
    }

    public void setIconString(String iconString) {
        this.iconString = iconString;
    }
}
