/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.Policy;

/**
 * ClassNode in this package reprents a class (in the sirius context) wrapped with navigational metainfo (Node) e.g. to
 * insert a class in the catalogue use an ClassNode This Type is to be used as return value of an RMI Method
 *
 * @version  $Revision$, $Date$
 */

public class MetaClassNode extends Node implements Comparable {

    //~ Static fields/initializers ---------------------------------------------


    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaClassNode object.
     *
     * @param  id                          DOCUMENT ME!
     * @param  domain                      DOCUMENT ME!
     * @param  classID                     DOCUMENT ME!
     * @param  name                        DOCUMENT ME!
     * @param  description                 DOCUMENT ME!
     * @param  isLeaf                      DOCUMENT ME!
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     * @param  classId                     DOCUMENT ME!
     */
    public MetaClassNode(final int id,
            final String domain,
            final int classID,
            final String name,
            final String description,
            final boolean isLeaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass,
            final int classId) {
        super(id, name, domain, description, isLeaf, policy, iconFactory, icon, derivePermissionsFromClass);
        this.classId = classID;
    }

//      public MetaClassNode(MetaClassNode node)
//      {
//              super(node);
//              this.classId=node.getClassID();
//
//      }

//      public MetaClassNode(Sirius.server.localserver.tree.node.ClassNode node,String domain) throws Exception
//              {
//                      super(node,domain);
//                      this.classId=node.getClassID();
//
//      }

//--------------------------------------

}
