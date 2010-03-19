/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * QueryPlan.java
 *
 * Created on 31. Oktober 2006, 16:16
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.search;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class QueryConfiguration {

    //~ Instance fields --------------------------------------------------------

    private Query query;
    private String[] classIds;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Querycofniguration.
     *
     * @param  query     DOCUMENT ME!
     * @param  classIds  DOCUMENT ME!
     */
    public QueryConfiguration(Query query, String[] classIds) {
        this.query = query;
        this.classIds = classIds;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Query getQuery() {
        return query;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String[] getClassIds() {
        return classIds;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classIds  DOCUMENT ME!
     */
    public void setClassIds(String[] classIds) {
        this.classIds = classIds;
    }
}
