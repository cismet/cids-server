/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.Policy;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MetaObjectNode extends Node implements Comparable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            MetaObjectNode.class);

    //~ Instance fields --------------------------------------------------------

    protected int objectId;

    protected volatile MetaObject theObject;

    //~ Constructors -----------------------------------------------------------

    /**
     * -----------------------------------------------
     *
     * @param  id                          DOCUMENT ME!
     * @param  localServerName             DOCUMENT ME!
     * @param  theObject                   DOCUMENT ME!
     * @param  name                        DOCUMENT ME!
     * @param  description                 DOCUMENT ME!
     * @param  isLeaf                      DOCUMENT ME!
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     */
    public MetaObjectNode(final int id,
            final String localServerName,
            final MetaObject theObject,
            final String name,
            final String description,
            final boolean isLeaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass) {
        super(id, name, localServerName, description, isLeaf, policy, iconFactory, icon, derivePermissionsFromClass);
        this.theObject = theObject;
        if (theObject != null) {
            objectId = theObject.getID();
            classId = theObject.getClassID();
        } else {
            objectId = -1;
            classId = -1;
        }
    }

    /**
     * Creates a new MetaObjectNode object.
     *
     * @param  id                          DOCUMENT ME!
     * @param  name                        DOCUMENT ME!
     * @param  description                 DOCUMENT ME!
     * @param  domain                      DOCUMENT ME!
     * @param  objectId                    DOCUMENT ME!
     * @param  classId                     DOCUMENT ME!
     * @param  isLeaf                      DOCUMENT ME!
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     */
    public MetaObjectNode(final int id,
            final String name,
            final String description,
            final String domain,
            final int objectId,
            final int classId,
            final boolean isLeaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass) {
        super(id, name, domain, description, isLeaf, policy, iconFactory, icon, derivePermissionsFromClass);

        this.objectId = objectId;
        this.classId = classId;
    }

//------------------------------------------------

    //~ Methods ----------------------------------------------------------------

// public MetaObjectNode(int id,String localServerName,String name,String description,boolean isLeaf)
// {
// super(id,name,localServerName,description,isLeaf);
//
// }

// --------------------------------------------------

// public MetaObjectNode(int id,String localServerName,String name,String description)
// {
// super(id,name,localServerName,description,false);
//
// }

// -----------------------------------------------

// public MetaObjectNode(MetaObjectNode node)
// {
// super(node);
// //this.theObject = node.getObject();
// this.classId=node.getClassId();
// this.objectId=node.getObjectId();
// }

    @Override
    public String getDescription() {
        return super.getDescription();
    }

    /**
     * ---------------------------------------------------------- public
     * MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode node,Sirius.server.localserver.object.Object
     * object,String domain) throws Exception { super(node,domain); this.theObject = new MetaObject(object,domain);
     * this.objectId=object.getID(); this.classId=object.getClassID(); } //Bugfix public
     * MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode node,Sirius.server.localserver.object.Object
     * object,String domain, UserGroup ug) throws Exception { super(node,domain); this.theObject = new
     * MetaObject(object.filter(ug),domain); this.objectId=object.getID(); this.classId=object.getClassID(); } public
     * MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode node,MetaObject object,String localServerName)
     * throws Exception { super(node,localServerName); this.theObject = object; this.objectId=object.getID();
     * this.classId=object.getClassID(); } public MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode
     * node,String localServerName) throws Exception { super(node,localServerName); this.objectId=node.getObjectID();
     * this.classId=node.getClassID(); } ------------------------------------------------
     *
     * @return  DOCUMENT ME!
     */
    public MetaObject getObject() {
        return theObject;
    }

    /**
     * ---------------------------------------------------
     *
     * @param  theObject  DOCUMENT ME!
     */
    public void setObject(final MetaObject theObject) {
        this.theObject = theObject;
        if (theObject != null) {
            this.classId = theObject.getClassID();
            this.objectId = theObject.getID();
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean objectSet() {
        return theObject != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getObjectId() {
        return objectId;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hb = new HashCodeBuilder();

        hb.append(id);
        hb.append(classId);
        hb.append(objectId);
        hb.append(domain);

        return hb.toHashCode();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof MetaObjectNode)) {
            return false;
        }

        final MetaObjectNode o = (MetaObjectNode)other;

        return (id == o.id) && domain.equals(o.domain) && (objectId == o.objectId) && (classId == o.classId);
    }
}
