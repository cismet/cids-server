/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * QueryInfo.java
 *
 * Created on 13. November 2003, 20:45
 */
package Sirius.server.search.store;
import java.util.*;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class QueryInfo implements java.io.Serializable, Info {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 8322629885911804823L;

    //~ Instance fields --------------------------------------------------------

    /** QueryID.* */
    protected int id;

    /** Heimat-LocalServer.* */
    protected String domain;

    /** Name des Suchergebnisses.* */
    protected String name;

    protected String fileName;

    protected HashSet userGroups;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of QueryInfo.
     *
     * @param  id        DOCUMENT ME!
     * @param  name      DOCUMENT ME!
     * @param  domain    DOCUMENT ME!
     * @param  fileName  DOCUMENT ME!
     */
    public QueryInfo(final int id, final String name, final String domain, final String fileName) {
        this(id, name, domain, fileName, new HashSet());
    }

    /**
     * Creates a new instance of QueryInfo.
     *
     * @param  id          DOCUMENT ME!
     * @param  name        DOCUMENT ME!
     * @param  domain      DOCUMENT ME!
     * @param  fileName    DOCUMENT ME!
     * @param  userGroups  DOCUMENT ME!
     */
    public QueryInfo(final int id,
            final String name,
            final String domain,
            final String fileName,
            final HashSet userGroups) {
        this.name = name;
        this.domain = domain;
        this.id = id;
        this.userGroups = userGroups;
        this.fileName = fileName;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Getter for property domain.
     *
     * @return  Value of property domain.
     */
    @Override
    public java.lang.String getDomain() {
        return domain;
    }

    /**
     * Setter for property domain.
     *
     * @param  domain  New value of property domain.
     */
    public void setDomain(final java.lang.String domain) {
        this.domain = domain;
    }

    /**
     * Getter for property id.
     *
     * @return  Value of property id.
     */
    @Override
    public int getID() {
        return id;
    }

    /**
     * Setter for property id.
     *
     * @param  id  New value of property id.
     */
    public void setID(final int id) {
        this.id = id;
    }

    /**
     * Getter for property name.
     *
     * @return  Value of property name.
     */
    @Override
    public java.lang.String getName() {
        return name;
    }

    /**
     * Setter for property name.
     *
     * @param  name  New value of property name.
     */
    public void setName(final java.lang.String name) {
        this.name = name;
    }

    /**
     * Getter for property userGroups.
     *
     * @return  Value of property userGroups.
     */
    public java.util.HashSet getUserGroups() {
        return userGroups;
    }

    /**
     * Setter for property userGroups.
     *
     * @param  userGroups  New value of property userGroups.
     */
    public void setUserGroups(final java.util.HashSet userGroups) {
        this.userGroups = userGroups;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  userGroupKey  DOCUMENT ME!
     */
    public void addUserGroup(final String userGroupKey) {
        userGroups.add(userGroupKey);
    }

    /**
     * Getter for property fileName.
     *
     * @return  Value of property fileName.
     */
    public java.lang.String getFileName() {
        return fileName;
    }

    /**
     * Setter for property fileName.
     *
     * @param  fileName  New value of property fileName.
     */
    public void setFileName(final java.lang.String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
