/**
 * *************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 * 
* ... and it just works.
 * 
***************************************************
 */
package de.cismet.cids.server.api.types.legacy;

import Sirius.server.newuser.UserGroup;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A factory class for converting between legacy cids types and REST/JSON types.
 * TODO: Integrate into <strong>cids-server-rest-types project</strong>!
 *
 * @author Pascal Dihé
 */
public class UserFactory {

    private final static transient Logger LOG = Logger.getLogger(UserFactory.class);
    private final static UserFactory factory = new UserFactory();

    private UserFactory() {
    }

    public final static UserFactory getFactory() {
        return factory;
    }

    /**
     * Transforms a cids legacy user object into a cids rest API user object
     *
     * @param cidsUser
     * @return
     */
    public de.cismet.cids.server.api.types.User restUserFromLegacyUser(final Sirius.server.newuser.User cidsUser) {
        final de.cismet.cids.server.api.types.User restUser = new de.cismet.cids.server.api.types.User();

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
     * Transforms a cids rest API user object into a cids legacy user object
     *
     * @param restUser
     * @return
     */
    public Sirius.server.newuser.User cidsUserFromRestUser(final de.cismet.cids.server.api.types.User restUser) {
        final Sirius.server.newuser.User cidsUser
                = new Sirius.server.newuser.User(-1,
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
