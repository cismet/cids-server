/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser;

import Sirius.util.*;

import java.io.*;

/**
 * Eine Klasse, die Informationen enthaelt, welche Benutzergruppen welchem Benutzer zugeordnet werden.*
 *
 * @version  $Revision$, $Date$
 */
public class Membership implements Serializable, Mapable {

    //~ Instance fields --------------------------------------------------------

    protected String login;
    protected String userDomain;
    protected String ug;
    protected String ugDomain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Membership object.
     *
     * @param  login       DOCUMENT ME!
     * @param  userDomain  DOCUMENT ME!
     * @param  ug          DOCUMENT ME!
     * @param  ugDomain    DOCUMENT ME!
     */
    public Membership(final String login, final String userDomain, final String ug, final String ugDomain) {
        this.login = login.trim();
        this.userDomain = userDomain.trim();
        this.ug = ug.trim();
        this.ugDomain = ugDomain.trim();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String toString() {
        return login + "/" + userDomain + "/" + ug + "/" + ugDomain; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mem  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public boolean equals(final java.lang.Object mem) {
        final Membership m = (Membership)mem;
        return m.login.equals(this.login) && m.userDomain.equals(this.userDomain) && m.ug.equals(this.ug)
                    && m.ugDomain.equals(this.ugDomain);
    }

    /**
     * Getter for property login.
     *
     * @return  Value of property login.
     */
    public java.lang.String getLogin() {
        return login;
    }

    /**
     * Setter for property login.
     *
     * @param  login  New value of property login.
     */
    public void setLogin(final java.lang.String login) {
        this.login = login;
    }

    /**
     * Getter for property ug.
     *
     * @return  Value of property ug.
     */
    public java.lang.String getUg() {
        return ug;
    }

    /**
     * Setter for property ug.
     *
     * @param  ug  New value of property ug.
     */
    public void setUg(final java.lang.String ug) {
        this.ug = ug;
    }

    /**
     * Getter for property ugDomain.
     *
     * @return  Value of property ugDomain.
     */
    public java.lang.String getUgDomain() {
        return ugDomain;
    }

    /**
     * Setter for property ugDomain.
     *
     * @param  ugDomain  New value of property ugDomain.
     */
    public void setUgDomain(final java.lang.String ugDomain) {
        this.ugDomain = ugDomain;
    }

    /**
     * Getter for property userDomain.
     *
     * @return  Value of property userDomain.
     */
    public java.lang.String getUserDomain() {
        return userDomain;
    }

    /**
     * Setter for property userDomain.
     *
     * @param  userDomain  New value of property userDomain.
     */
    public void setUserDomain(final java.lang.String userDomain) {
        this.userDomain = userDomain;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   m  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object constructKey(final Mapable m) {
        return getKey();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public Object getKey() {
        return login + "@" + userDomain + "€" + ug + "@" + ugDomain; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getUserKey() {
        return login + "@" + userDomain; // NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object getUserGroupkey() {
        return ug + "@" + ugDomain; // NOI18N
    }

// end equals
}
