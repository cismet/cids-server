/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cidsx.server.api.types.legacy;

import Sirius.server.newuser.UserGroup;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A factory class for converting between legacy cids types and REST/JSON types. TODO: Integrate into <strong>
 * cids-server-rest-types project</strong>!
 *
 * @author   Pascal Dihé
 * @version  $Revision$, $Date$
 */
public class UserFactory {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(UserFactory.class);
    private static final UserFactory factory = new UserFactory();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserFactory object.
     */
    private UserFactory() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static final UserFactory getFactory() {
        return factory;
    }

    /**
     * Transforms a cids legacy user object into a cids rest API user object.
     *
     * @param   cidsUser  the cids legacy user object to be converted
     *
     * @return  the converted cids rest API user object
     */
    public de.cismet.cidsx.server.api.types.User restUserFromLegacyUser(final Sirius.server.newuser.User cidsUser) {
        final de.cismet.cidsx.server.api.types.User restUser = new de.cismet.cidsx.server.api.types.User();

        final Collection<String> userGroupNames = new ArrayList<String>();

        for (final UserGroup cidsUserGroup : cidsUser.getPotentialUserGroups()) {
            userGroupNames.add(cidsUserGroup.getName());
        }

        restUser.setUser(cidsUser.getName());
        restUser.setDomain(cidsUser.getDomain());
        restUser.setUserGroups(userGroupNames);

        return restUser;
    }

    /**
     * Transforms a cids rest API user object into a cids legacy user object.
     *
     * @param   restUser  the cids rest API user object to be converted
     *
     * @return  the converted cids legacy user object
     */
    public Sirius.server.newuser.User cidsUserFromRestUser(final de.cismet.cidsx.server.api.types.User restUser) {
        final Sirius.server.newuser.User cidsUser = new Sirius.server.newuser.User(
                -1,
                restUser.getUser(),
                restUser.getDomain());

        final Collection<UserGroup> cidsUserGroups = new ArrayList<UserGroup>();

        for (final String restUserGroup : restUser.getUserGroups()) {
            final UserGroup cidsUserGroup = new UserGroup(-1,
                    restUserGroup,
                    restUser.getDomain());
            cidsUserGroups.add(cidsUserGroup);
        }

        cidsUser.setPotentialUserGroups(cidsUserGroups);

        return cidsUser;
    }
}
