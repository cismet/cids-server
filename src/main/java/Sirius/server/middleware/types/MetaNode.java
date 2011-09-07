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

public class MetaNode extends Node {

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
        this(
            id,
            domain,
            name,
            description,
            isLeaf,
            policy,
            iconFactory,
            icon,
            derivePermissionsFromClass,
            classId,
            null);
    }

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
     * @param  artificialId                DOCUMENT ME!
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
            final int classId,
            final String artificialId) {
        super(
            id,
            name,
            domain,
            description,
            isLeaf,
            policy,
            iconFactory,
            icon,
            derivePermissionsFromClass,
            artificialId);

        super.classId = classId;
    }
}
