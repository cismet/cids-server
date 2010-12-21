/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser;
import Sirius.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class User implements java.io.Serializable, Mapable {

    //~ Instance fields --------------------------------------------------------

    // /** die BenutzerID **/
    protected int id; // identifier im ls

    /** der LoginName des Benutzers.* */
    protected String name;           // loginname

    /** der Heimat-LocalServer des Benutzers.* */
    protected String domain;                  // heimatserver

    /** Die Benutzergruppe, der der Benutzer zugeordnet ist. Sie wird explizit gesetzt * */
    protected UserGroup userGroup;

    /** Variable, die anzeigt, ob eine Benutzergruppe gesetzt wurde.* */
    protected boolean valid = false;

    protected boolean isAdmin = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new User object.
     *
     * @param  id      Heimat-LocalServer des Benutzers
     * @param  name    Benutzername
     * @param  domain  BenutzerID *
     */
    public User(final int id, final String name, final String domain) {
        this.domain = domain;
        this.id = id;
        this.name = name;
    }

    /**
     * Creates a new User object.
     *
     * @param  id       DOCUMENT ME!
     * @param  name     DOCUMENT ME!
     * @param  domain   DOCUMENT ME!
     * @param  isAdmin  DOCUMENT ME!
     */
    public User(final int id, final String name, final String domain, final boolean isAdmin) {
        this(id, name, domain);
        this.isAdmin = isAdmin;
    }

    /**
     * legt Benutzer an und weist ihm direkt eine Benutzergruppe zu.
     *
     * @param  id         BenutzerID
     * @param  name       der Benutzername
     * @param  domain     localServerName Heimat-LocalServer des Benutzers
     * @param  userGroup  die Benutzergruppe, der der Benutzer zugeordnet werden soll*
     */
    public User(final int id, final String name, final String domain, final UserGroup userGroup) {
        this(id, name, domain);
        this.userGroup = userGroup;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String toString() {
        return getKey().toString();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  Login-Name des Benutzers *
     */
    public String getName() {
        return name;
    }

    /**
     * weist dem User eine UserGroup zu und setzt valid = true. Mit isValid() kann abgefragt werden, ob schon eine
     * UserGroup gesetzt wurde *
     *
     * @param  userGroup  DOCUMENT ME!
     */
    public void setUserGroup(final UserGroup userGroup) {
        this.userGroup = userGroup;
        valid = true;
    }

    /**
     * liefert UserGroup.*
     *
     * @return  DOCUMENT ME!
     */
    public UserGroup getUserGroup() {
        return userGroup;
    }

    /**
     * DOCUMENT ME!
     */
    public void setValid() {
        valid = true;
    }

    /**
     * hiermit kann abgefragt werden, ob dem User schon eine Usergroup zugewiesen wurde.*
     *
     * @return  DOCUMENT ME!
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * hiermit kann abgefragt werden, ob es sich um einen Admin handelt.
     *
     * @return  DOCUMENT ME!
     */
    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public boolean equals(final java.lang.Object obj) {
        final User user = (User)obj;
        return (this.name.equals(user.name) && this.domain.equals(user.domain));
    }

    @Override
    public Object getKey() {
        if (userGroup != null) {
            return name + "@" + userGroup.getKey(); // NOI18N
        } else {
            return name + "";
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getRegistryKey() {
        return name + "@" + domain; // NOI18N
    }

    @Override
    public Object constructKey(final Mapable m) {
        if (m instanceof User) {
            return m.getKey();
        } else {
            return null;
        }
    }

    /**
     * Getter for property id.
     *
     * @return  Value of property id.
     */
    public int getId() {
        return id;
    }
}
