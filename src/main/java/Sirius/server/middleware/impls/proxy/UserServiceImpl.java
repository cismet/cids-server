/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * UserServiceImpl.java
 *
 * Created on 25. September 2003, 12:53
 */
package Sirius.server.middleware.impls.proxy;
//import Sirius.middleware.interfaces.domainserver.*;

import Sirius.server.localserver.user.LoginRestrictionHelper;
import Sirius.server.middleware.interfaces.domainserver.UserService;
import Sirius.server.newuser.User;
import Sirius.server.newuser.UserException;
import Sirius.server.newuser.UserGroup;
import Sirius.server.newuser.UserServer;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import org.apache.log4j.Logger;

import java.rmi.RemoteException;

import java.security.Key;

import java.util.Base64;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.cismet.connectioncontext.ConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class UserServiceImpl {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(UserServiceImpl.class);
    private static final String DOMAINSPLITTER = "@";

    //~ Instance fields --------------------------------------------------------

    private UserServer userServer;
    private Hashtable activeLocalServers;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of UserServiceImpl.
     *
     * @param   activeLocalServers  DOCUMENT ME!
     * @param   userServer          DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public UserServiceImpl(final Hashtable activeLocalServers, final UserServer userServer) throws RemoteException {
        this.activeLocalServers = activeLocalServers;
        this.userServer = userServer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
        System.out.println("Will produce 20 random Secret Keys");
        for (int i = 0; i < 20; ++i) {
            final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            final String secretString = Encoders.BASE64.encode(key.getEncoded());
            System.out.println(secretString);
        }
        System.out.println("\nDecoding Test");
        final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        final byte[] decodedKey = Base64.getDecoder().decode("J0j+LcPz1I3ATqoi/QENz0dZD+C4pL6B9waw4zVw4e4=");
        final Key serverKey = new SecretKeySpec(decodedKey, signatureAlgorithm.getJcaName());

        final String secretString = Encoders.BASE64.encode(serverKey.getEncoded());
        System.out.println(secretString);
        // .claim("usergroup", userGroupName+"@"+userGroupLsName)
        final String jws = Jwts.builder().setId("1").setSubject("admin@s").signWith(serverKey).compact();
        System.out.println("\nJWS Test");
        System.out.println(jws);
    }

    /**
     * Wie konnte das jemals gehen Falsche Reihenfolge in Signatur public User getUser( String userLsName, String
     * userName, String userGroupLsName, String userGroupName, String password) throws RemoteException, UserException {.
     *
     * @param   userGroupLsName  DOCUMENT ME!
     * @param   userGroupName    DOCUMENT ME!
     * @param   userLsName       DOCUMENT ME!
     * @param   userName         DOCUMENT ME!
     * @param   password         DOCUMENT ME!
     * @param   context          DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    public User getUser(final String userGroupLsName,
            final String userGroupName,
            final String userLsName,
            final String userName,
            final String password,
            final ConnectionContext context) throws RemoteException, UserException {
        final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        final byte[] decodedKey = Base64.getDecoder().decode("J0j+LcPz1I3ATqoi/QENz0dZD+C4pL6B9waw4zVw4e4=");
        final Key serverKey = new SecretKeySpec(decodedKey, signatureAlgorithm.getJcaName());

        if (LOG.isDebugEnabled()) {
            LOG.debug("getUser calles for user::" + userName); // NOI18N

            LOG.debug("userLsName:" + userLsName);           // NOI18N
            LOG.debug("userName:" + userName);               // NOI18N
            LOG.debug("userGroupLsName:" + userGroupLsName); // NOI18N
//            LOG.debug("userGroupName:" + userGroupName);                      // NOI18N
            LOG.debug((("password:" + password) == null) ? "null" : "*****"); // NOI18N
        }

        final User u;
        if (userName.equals("jwt")) {
            final String jwt = password;
            final Claims claims = Jwts.parserBuilder().setSigningKey(serverKey).build().parseClaimsJws(jwt).getBody();
            final int uid = new Integer(claims.getId()).intValue();
            final String jwtUsername = claims.getSubject();
            final String jwtDomain = claims.get("domain", String.class);
            final String jwtUsergroup = claims.get("usergroup", String.class);
            final String jwtUsergroupDomain = claims.get("usergroupDomain", String.class);
            u = userServer.getUser(jwtUsergroupDomain, jwtUsergroup, jwtDomain, jwtUsername, "jwtCreatedUser");
            u.setValid();
            return u;
        } else {
//

            u = userServer.getUser(userGroupLsName, userGroupName, userLsName, userName, password);

            boolean validated = false;

            if (u != null) {
                final Sirius.server.middleware.interfaces.domainserver.UserService us =
                    (Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(userLsName);

                if (us != null) {
                    validated = us.validateUser(u, password, context);
                } else {
                    throw new UserException(
                        "Login failed, home server of the user is not reachable :: "
                                + userName, // NOI18N
                        false,
                        false,
                        false,
                        true);
                }
            }

            if (validated) {
                final String loginRestrictionValue = getConfigAttr(u, "login.restriction", context);
                if (loginRestrictionValue != null) {
                    LoginRestrictionHelper.getInstance().checkLoginRestriction(loginRestrictionValue);
                }

                final JwtBuilder builder = Jwts.builder().setId(u.getId() + "").setSubject(userName);
                builder.claim("domain", userLsName);
                if ((userGroupName != null) && (userGroupLsName != null)) {
                    builder.claim("usergroup", userGroupName);
                    builder.claim("usergroupDomain", userGroupLsName);
                }

                final String jws = builder.signWith(serverKey).compact();
                u.setJwsToken(jws);
                return u;
            }
        }
        throw new UserException("Login failed :: " + userName, false, true, false, false); // NOI18N
    }

    /**
     * result contains strings.
     *
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUserGroupNames(final ConnectionContext context) throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserGroupName called"); // NOI18N
        }

        final Vector names = new Vector(20, 20);

        final Collection c = userServer.getUserGroups();

        final Iterator i = c.iterator();

        while (i.hasNext()) {
            final UserGroup tmpUserGroup;

            final String[] s = new String[2];
            tmpUserGroup = (UserGroup)i.next();

            s[0] = tmpUserGroup.getName();
            s[1] = tmpUserGroup.getDomain();

            names.add(s);
        }

        return names;
    }

    /**
     * result contains string[2] subset of all ugs.
     *
     * @param   userName  DOCUMENT ME!
     * @param   lsHome    DOCUMENT ME!
     * @param   context   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public Vector getUserGroupNames(final String userName, final String lsHome, final ConnectionContext context)
            throws RemoteException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getUserGroupNames called for :username:" + userName); // NOI18N
        }
        return userServer.getUserGroupNames(userName.trim(), lsHome.trim());
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user         DOCUMENT ME!
     * @param   oldPassword  DOCUMENT ME!
     * @param   newPassword  DOCUMENT ME!
     * @param   context      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     * @throws  UserException    DOCUMENT ME!
     */
    public boolean changePassword(final User user,
            final String oldPassword,
            final String newPassword,
            final ConnectionContext context) throws RemoteException, UserException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("changePassword called for :user:" + user); // NOI18N
        }
        return ((Sirius.server.middleware.interfaces.domainserver.UserService)activeLocalServers.get(user.getDomain()))
                    .changePassword(user, oldPassword, newPassword, context);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   user     DOCUMENT ME!
     * @param   key      DOCUMENT ME!
     * @param   context  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    public String getConfigAttr(final User user, final String key, final ConnectionContext context)
            throws RemoteException {
        final String domain;
        final String realKey;
        if (key.contains(DOMAINSPLITTER)) {
            final String[] split = key.split(DOMAINSPLITTER);
            domain = split[1];
            realKey = split[0];
        } else {
            domain = user.getDomain();
            realKey = key;
        }
        final UserService userService = (UserService)activeLocalServers.get(domain);
        if (userService != null) {
            return userService.getConfigAttr(user, realKey, context);
        } else {
            return null;
        }
    }
}
