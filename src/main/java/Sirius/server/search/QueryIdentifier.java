/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * QueryIdentifier.java
 *
 * Created on 25. September 2003, 11:56
 */
package Sirius.server.search;
import Sirius.util.*;
/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class QueryIdentifier implements java.io.Serializable, Mapable {

    //~ Instance fields --------------------------------------------------------

    /** Localserver where this query is hosted and will be executed. */
    protected String domain;

    /** data base identifier of a query. */
    protected int queryId;

    protected String name;

    protected String description;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of QueryIdentifier.
     *
     * @param  domain   DOCUMENT ME!
     * @param  queryId  DOCUMENT ME!
     */
    public QueryIdentifier(final String domain, final int queryId) {
        this.domain = domain;
        this.queryId = queryId;
        name = "";        // NOI18N
        description = ""; // NOI18N
    }

    /**
     * Creates a new instance of QueryIdentifier.
     *
     * @param  domain  DOCUMENT ME!
     * @param  name    DOCUMENT ME!
     */
    public QueryIdentifier(final String domain, final String name) {
        this.domain = domain;
        this.queryId = -1; // unkown
        this.name = name;
        description = "";  // NOI18N
    }

    /**
     * Creates a new instance of QueryIdentifier.
     *
     * @param  domain      DOCUMENT ME!
     * @param  queryId     DOCUMENT ME!
     * @param  name        DOCUMENT ME!
     * @param  desription  DOCUMENT ME!
     */
    public QueryIdentifier(final String domain, final int queryId, final String name, final String desription) {
        this(domain, queryId);
        this.name = name;
        this.description = description;
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
    public void setDomain(final java.lang.String domain) {
        this.domain = domain;
    }

    /**
     * Getter for property queryId.
     *
     * @return  Value of property queryId.
     */
    public int getQueryId() {
        return queryId;
    }

    /**
     * Setter for property queryId.
     *
     * @param  queryId  New value of property queryId.
     */
    public void setQueryId(final int queryId) {
        this.queryId = queryId;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Object hashValue() {
        return getKey();
    }

    @Override
    public String toString() {
        return getKey().toString();
    }

    @Override
    public Object getKey() {
        if (name.equals("")) {             // NOI18N
            return queryId + "@" + domain; // NOI18N
        } else {
            return name + "@" + domain;    // NOI18N
        }
    }

    @Override
    public Object constructKey(final Mapable m) {
        if (m instanceof QueryIdentifier) {
            return m.getKey();
        } else {
            return null;
        }
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
    public void setName(final java.lang.String name) {
        this.name = name;
    }

    /**
     * Getter for property description.
     *
     * @return  Value of property description.
     */
    public java.lang.String getDescription() {
        return description;
    }

    /**
     * Setter for property description.
     *
     * @param  description  New value of property description.
     */
    public void setDescription(final java.lang.String description) {
        this.description = description;
    }
}
