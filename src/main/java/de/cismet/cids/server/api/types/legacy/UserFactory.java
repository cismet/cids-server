/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.api.types.legacy;

import Sirius.server.newuser.UserGroup;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;

/**
 * A factory class for converting between legacy cids types and REST/JSON types.
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

    public de.cismet.cids.server.api.types.User restUserFromCidsUser(final Sirius.server.newuser.User cidsUser) {
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

    public Sirius.server.newuser.User cidsUserFromRestUser(final de.cismet.cids.server.api.types.User restUser) {
        final Sirius.server.newuser.User cidsUser
                = new Sirius.server.newuser.User(-1, restUser.getUser(), restUser.getDomain());

        final Collection<UserGroup> cidsUserGroups = new ArrayList<UserGroup>();
        for (final String restUserGroup : restUser.getUserGroups()) {
            final UserGroup cidsUserGroup = new UserGroup(-1, restUserGroup, restUser.getDomain());
            cidsUserGroups.add(cidsUserGroup);
        }
        cidsUser.setPotentialUserGroups(cidsUserGroups);

        return cidsUser;
    }
}
