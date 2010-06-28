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

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 3625829713268727487L;

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
    public MetaNode(final int id,
            final String domain,
            final String name,
            final String description,
            final boolean isLeaf,
            final Policy policy,
            final int iconFactory,
            final String icon,
            final boolean derivePermissionsFromClass,
            final int classId) {
        super(id, name, domain, description, isLeaf, policy, iconFactory, icon, derivePermissionsFromClass);
        super.classId = classId;
    }

//      public MetaNode(MetaNode node)
//      {super(node);}

//      public MetaNode(Sirius.server.localserver.tree.node.Node node,String domain)
//      {super(node,domain);}

}
