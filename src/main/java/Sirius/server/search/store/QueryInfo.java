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
    public QueryInfo(int id, String name, String domain, String fileName) {
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
    public QueryInfo(int id, String name, String domain, String fileName, HashSet userGroups) {
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
    public java.lang.String getDomain() {
        return domain;
    }

    /**
     * Setter for property domain.
     *
     * @param  domain  New value of property domain.
     */
    public void setDomain(java.lang.String domain) {
        this.domain = domain;
    }

    /**
     * Getter for property id.
     *
     * @return  Value of property id.
     */
    public int getID() {
        return id;
    }

    /**
     * Setter for property id.
     *
     * @param  id  New value of property id.
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Getter for property name.
     *
     * @return  Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Setter for property name.
     *
     * @param  name  New value of property name.
     */
    public void setName(java.lang.String name) {
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
    public void setUserGroups(java.util.HashSet userGroups) {
        this.userGroups = userGroups;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  userGroupKey  DOCUMENT ME!
     */
    public void addUserGroup(String userGroupKey) {
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
    public void setFileName(java.lang.String fileName) {
        this.fileName = fileName;
    }

    public String toString() {
        return this.getName();
    }
}
