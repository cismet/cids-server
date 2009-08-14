/*
 * QueryPlaner.java
 *
 * Created on 31. Oktober 2006, 14:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ordnet die SuchOptionen nach Ls und deren classIds
 * @author schlob
 */
public class QueryPlaner
{
    
    private static transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(QueryPlaner.class);
    
    HashMap<String, ArrayList<QueryConfiguration>> queryPlansPerDomain= new  HashMap<String, ArrayList<QueryConfiguration>>();
    
    /** Creates a new instance of QueryPlaner */
    public QueryPlaner(String[] classIds,SearchOption[] searchOptions)
    {
        HashSet<String> domainList = extractAllDomains(convertSearchOptions(searchOptions));
        
        setSubqueryParameters(convertSearchOptions(searchOptions));
        
        logger.debug("List der Query domains aufgestellt"+domainList);
        
        String[] domains = (String[])domainList.toArray(new String[domainList.size()]);
        
        // query
        for(int i=0;i<domains.length;i++)
        {
            logger.debug("construct queryplan for domain "+domains[i] +"of # of domains :"+domains.length);
            
            String[] cIds = filterClassIdsForDomain(domains[i],classIds);
            
            logger.debug("classids f\u00FCr domain"+domains[i]+" ids:"+cIds);
            
            ArrayList<Query> qs = extractQueriesForDomain(domains[i],convertSearchOptions(searchOptions));
            
            Iterator<Query> iter = qs.iterator();
            
            ArrayList<QueryConfiguration> qcList = new  ArrayList<QueryConfiguration>(qs.size());
            
            while(iter.hasNext())
            {
                qcList.add( new QueryConfiguration(iter.next(),cIds));
            }
            
            queryPlansPerDomain.put(domains[i],qcList);
        }
        
        
    }
    
    
    
    private static String extractDomainFromClassId(String classId)
    {
        if(!checkClassId(classId))
        {
            logger.error("improper classid has to be of the form: digit@domain");
            return null;
            
        }
        
        
        return classId.split("@")[1];
        
    }
    
    private static String extractDomainFromQueryId(String qId)
    {
        if(!checkQueryId(qId))
        {
            logger.error("improper classid has to be of the form: query@domain");
            return null;
            
        }
        
        
        return qId.split("@")[1];
        
    }
    
    private static int extractClassId(String classId)
    {
        if(!checkClassId(classId))
        {
            logger.error("improper classid has to be of the form: digit@domain");
            return -1;
            
        }
        
        return new Integer(classId.split("@")[0]).intValue();
        
    }
    private static int extractQueryId(String qId)
    {
        if(!checkQueryId(qId))
        {
            logger.error("improper queryid has to be of the form: quid@domain");
            return -1;
            
        }
        
        return new Integer(qId.split("@")[0]).intValue();
        
    }
    
    
    
    private static boolean checkClassId(String classId)
    {
        // classid@domain
        
        String regex = "[0-9]+[@][^@]+"; // digit of arbitray length + @ +arbitrary not @
        
        
        Pattern p = Pattern.compile(regex);
        
        Matcher m = p.matcher(classId);
        
        
        return m.matches();
    }
    
    private static boolean checkQueryId(String qId)
    {
        // classid@domain
        
        String regex = "[^@]+[@][^@]+"; // non at of arbitray length + @ +arbitrary not @
        
        
        Pattern p = Pattern.compile(regex);
        
        Matcher m = p.matcher(qId);
        
        
        return m.matches();
    }
    
    
    
    String[] filterClassIdsForDomain( String domain,String[] classIds)
    {
        
        ArrayList v = new ArrayList(classIds.length);
        
        for(int i=0;i<classIds.length;i++)
        {
            String cdomain = extractDomainFromClassId(classIds[i]);
            
            logger.debug("domain aus class id "+classIds[i]+" extrahiert" +cdomain);
            
            if(domain.equals(cdomain))
            {
                v.add(classIds[i]);
                logger.debug(classIds[i]+" zu den klassids der domain" + domain +"hinzugef\u00FCgt");
            }
            else
                logger.debug(cdomain+ "of classId:: "+classIds[i]+" ::does not match domain "+ domain);
            
        }
        
        logger.debug(" classids for domain"+domain+ " are" +v);
        return (String[])v.toArray(new String[v.size()]);
        
        
    }
    
    HashSet<String> extractAllDomains(Query[] qs)
    {
        
        HashSet<String> domains = new HashSet<String>(qs.length);
        
        for(int i=0;i<qs.length;i++)
        {
            domains.add(qs[i].getQueryIdentifier().getDomain());
            
            Query[] subs = qs[i].getSubQueries();
            
            if(subs!=null)
                domains.addAll(extractAllDomains(subs));
            
        }
        
        return domains;
    }
    
    
    
    ArrayList<Query> extractQueriesForDomain(String domain,Query[] queries)
    {
        ArrayList<Query> queryList = new ArrayList<Query>(queries.length+5);
        
        
        for(int i=0;i<queries.length;i++)
        {
            if(  queries[i].getQueryIdentifier().getDomain().equals(domain) )
            {
                Query q = queries[i];
                
                queryList.add(q);
  
            }
            
            if(queries[i].isUnionQuery())
                queryList.addAll( extractQueriesForDomain(domain,queries[i].getSubQueries()));
        }
        
        return queryList;
        
    }
    
    
    
    private Query searchOption2Query(SearchOption so)
    {
        return so.getQuery();
        
        
    }
    
    private Query[] convertSearchOptions(SearchOption[] sos)
    {
        ArrayList<Query> qs = new ArrayList<Query>(sos.length+5);
        
        for(int i =0;i<sos.length;i++)
        {
            qs.add(sos[i].getQuery());
            
        }
        
        return (Query[])qs.toArray(new Query[qs.size()]);
        
    }
    
    
    public Collection< ArrayList<QueryConfiguration> > getQueryPlans()
    {
        return queryPlansPerDomain.values();
        
    }
    
    public void setSubqueryParameters(Query[] qs)
    {
   
        
        for(int i=0;i<qs.length;i++)
        {
            Query[] subs = qs[i].getSubQueries();
            
            if(subs!=null)
            {
                for(int j=0;j<subs.length;j++)
                    subs[j].setParameters(qs[i].getParameters());
                
                    setSubqueryParameters(subs);
            
            }
        }
            
    
    }
    
    
    
    
    public static void main(String[] args)
    {
        String t = "444444444444444444@oppp";
        
        System.out.println(extractDomainFromClassId(t));
        
        
    }
    
    
    
}
