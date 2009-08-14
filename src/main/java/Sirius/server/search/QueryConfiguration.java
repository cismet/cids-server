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
 *
 * @author schlob
 */
public class QueryConfiguration
{
    private Query query;
    private String[] classIds;
   
    
    /** Creates a new instance of Querycofniguration */
    public QueryConfiguration(Query query, String[] classIds)
    {
        this.query=query;
        this.classIds=classIds;
    }

    public Query getQuery()
    {
        return query;
    }

    public String[] getClassIds()
    {
        return classIds;
    }

    public void setClassIds(String[] classIds)
    {
        this.classIds = classIds;
    }
    
    
    
}
