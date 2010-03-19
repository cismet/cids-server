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
 * Organisatory Node (represents no Class or Object).
 *
 * @version  $Revision$, $Date$
 */

public class MetaNode extends Sirius.server.middleware.types.Node {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MetaNode object.
     *
     * @param  id                          DOCUMENT ME!
     * @param  domain                      DOCUMENT ME!
     * @param  name                        DOCUMENT ME!
     * @param  description                 DOCUMENT ME!
     * @param  isLeaf                      DOCUMENT ME!
     * @param  policy                      DOCUMENT ME!
     * @param  iconFactory                 DOCUMENT ME!
     * @param  icon                        DOCUMENT ME!
     * @param  derivePermissionsFromClass  DOCUMENT ME!
     * @param  classId                     DOCUMENT ME!
     */
    public MetaNode(
            int id,
            String domain,
            String name,
            String description,
            boolean isLeaf,
            Policy policy,
            int iconFactory,
            String icon,
            boolean derivePermissionsFromClass,
            int classId) {
        super(id, name, domain, description, isLeaf, policy, iconFactory, icon, derivePermissionsFromClass);
        super.classId = classId;
    }

//      public MetaNode(MetaNode node)
//      {super(node);}

//      public MetaNode(Sirius.server.localserver.tree.node.Node node,String domain)
//      {super(node,domain);}

}
