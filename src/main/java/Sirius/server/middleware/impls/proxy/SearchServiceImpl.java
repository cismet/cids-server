/*
 * SearchServiceImpl.java
 *
 * Created on 23. November 2003, 14:39
 */

package Sirius.server.middleware.impls.proxy;
import Sirius.server.*;
import Sirius.server.naming.NameServer;
import java.util.*;
import Sirius.server.search.*;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;
import java.rmi.*;
/**
 *
 * @author  schlob
 */
public class SearchServiceImpl
{
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    // resolves Query tree
    private QueryExecuter qex;
    
    private java.util.Hashtable activeLocalServers;
    
    private NameServer nameServer;
    
    
    
    
    
    /** Creates a new instance of QueryStoreImpl */
    public SearchServiceImpl(java.util.Hashtable activeLocalServers,
            NameServer nameServer) throws RemoteException
    {
        this.activeLocalServers = activeLocalServers;
        this.nameServer = nameServer;
        
        qex= new QueryExecuter(activeLocalServers);
        
    }
    
    //    public java.util.Collection getAllSearchOptions(User user) throws RemoteException {
    //
    //        return getSearchOptions(user).values();
    //
    //    }
    //
    //    public java.util.Collection getAllSearchOptions(User user, String domain) throws RemoteException {
    //        return getSearchOptions(user,domain).values();
    //    }
    //
    
    
    public java.util.HashMap getSearchOptions(User user) throws RemoteException
    {
        logger.debug("getSearchOptions searchService gerufen User:: "+user);
        //umstelen auf activelocalservrs sp\u00E4ter
        Server[] localServers =  nameServer.getServers(ServerType.LOCALSERVER);
        HashMap result=new HashMap();
        
        String serverName = null;
        for(int i =0;i<localServers.length;i++)
        {
            
            serverName= localServers[i].getName();
            
            logger.debug("localserver Suchoptionen "+serverName+ " searchoptions ::");
            
            
            HashMap options =getSearchOptions(user,serverName);
            
            logger.debug("localserver Suchoptionen "+serverName+ " searchoptions ::"+options);
            
            
            result.putAll( options );
            
        }
        
        logger.debug("Search Options ::"+result);
        return result;
        
    }
    
    public java.util.HashMap getSearchOptions(User user, String domain) throws RemoteException
    {
        logger.debug("getSearchOptions searchService gerufen User:: "+user +" domain::"+domain);
        return ((Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain)).getSearchOptions(user);
    }
    
    //    public Sirius.server.search.SearchResult search(User user, String[] classIds, Sirius.server.search.SearchOption[] searchOptions) throws RemoteException
    //    {
    //
    //        SearchResult v = new SearchResult(new Node[0]);
    //
    //
    //        //
    //        //fehlt \u00FCberpr\u00FCfung ob Typ tats\u00E4chlich passt
    //        //alle Queries m\u00FCssen einen Typ liefern
    //        //sp\u00E4ter
    //        //
    //
    //        Query q = null;
    //
    //        try
    //        {
    //            for(int i =0;i<searchOptions.length;i++)
    //            {
    //                q = searchOptions[i].getQuery();
    //
    //                v.addAll((SearchResult) qex.executeQuery(user,classIds,q));
    //
    //            }
    //        }catch(Exception e)
    //        {System.err.println(e);throw new RemoteException(e.getMessage(),e);}
    //
    //
    //
    //        return v;
    //    }
    
    // permissions muessen hier gepr\u00FCft werden nicht nur die Domain abfragen
    
    public int addQuery(User user,String name,String description,String statement,int resultType,char isUpdate,char isBatch,char isRoot,char isUnion) throws RemoteException
    {
        
        logger.debug("addQuery searchService gerufen User:: "+user + " queryName ::"+name);
        String domain = user.getDomain();
        Sirius.server.middleware.interfaces.domainserver.SearchService s =  (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);
        
        return s.addQuery(name,description,statement,resultType,isUpdate,isBatch,isRoot,isUnion);
        
        
        
    }
    
    public int addQuery(User user,String name,String description,String statement) throws RemoteException
    {
        
        logger.debug("addQuery searchService gerufen User:: "+user + " queryName ::"+name);
        
        String domain = user.getDomain();
        Sirius.server.middleware.interfaces.domainserver.SearchService s =  (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);
        
        return s.addQuery(name,description,statement);
        
    }
    
    public boolean addQueryParameter(User user,int queryId,int typeId,String paramkey,String description,char isQueryResult,int queryPosition) throws RemoteException
    {
        
        logger.debug("addQueryParameter searchService gerufen User:: "+user + " queryId ::"+queryId +" paramKey::"+paramkey);
        String domain = user.getDomain();
        Sirius.server.middleware.interfaces.domainserver.SearchService s =  (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);
        return s.addQueryParameter(queryId,typeId, paramkey, description,isQueryResult, queryPosition);
        
    }
    
    //position set in order of the addition
    public boolean addQueryParameter(User user,int queryId,String paramkey,String description) throws RemoteException
    {
        
        logger.debug("addQueryParameter searchService gerufen User:: "+user + " queryId ::"+queryId +" paramKey::"+paramkey);
        String domain = user.getDomain();
        Sirius.server.middleware.interfaces.domainserver.SearchService s =  (Sirius.server.middleware.interfaces.domainserver.SearchService)activeLocalServers.get(domain);
        
        return s.addQueryParameter(queryId, paramkey, description);
        
    }
    
    public SearchResult search(User user, String[] classIds, Sirius.server.search.SearchOption[] searchOptions) throws RemoteException
    {
        logger.debug("search in searchService gerufen User:: "+user );
        
        SearchResult v = new SearchResult(new MetaObjectNode[0]);
        
        logger.debug("Queryplaner instnziiert");
        
        QueryPlaner qp = new QueryPlaner(classIds,searchOptions);
        
        logger.debug("Querypl\u00E4ne abgerufen");
        
        Collection<ArrayList<QueryConfiguration>> qps = qp.getQueryPlans();
        
        logger.debug("Querypl\u00E4ne abgerufen"+qps);
        
        Iterator<ArrayList<QueryConfiguration>> iter = qps.iterator();
        
        while(iter.hasNext())
        {
            try
            {
                v.addAll(searchX(user,iter.next()));
            }
            catch (RemoteException ex)
            {
                logger.error(ex);
            }
            catch (Exception ex)
            {
                logger.error(ex);
            }
        }
        
        
        
        return v;
        
    }
    
    // fromer searchAdapted
    public SearchResult searchX(User user, ArrayList<QueryConfiguration> qList) throws RemoteException
    {
        
        logger.debug("search in searchService gerufen User:: "+user );
        
        SearchResult v = new SearchResult(new MetaObjectNode[0]);
        
        QueryConfiguration[] qcs = (QueryConfiguration[])qList.toArray(new QueryConfiguration[qList.size()]);
        
        Query q = null;
        
        try
        {
            for(int i =0;i<qcs.length;i++)
            {
                q = qcs[i].getQuery();
                
                String[] classIds = qcs[i].getClassIds();
                
                
                // debugtext
                String deb ="";
                for(int it =0;it<classIds.length;it++)
                    deb+=classIds[it]+"\n";
                
                logger.debug("classids fro query"+q +" are"+ deb);
                
                
                HashMap params =q.getParameters();
                
                logger.debug("Parameter ::"+q +" isconjunction??"+q.isConjunction());
                
                SearchResult result =(SearchResult) qex.executeQuery(user,classIds,q);
                
                if(q.isConjunction())
                {
                    
                    if(!v.retainerSet())
                    {
                        if(v.getResult()!=null&&((HashSet)v.getResult()).size()>0)
                            v.setRetainer(v.intersect((HashSet)result.getResult(),(HashSet)v.getResult()));// setze retainer mit suchergebnis
                        else
                            v.setRetainer((HashSet)result.getResult());
                    }
                    else
                        v.setRetainer( v.intersect(v.getRetainer(),(HashSet)result.getResult() )); // schnittmenge 2er retainer
                }
                
                
                v.addAll(result);
                
                // wenn union hole alle subqueries und f\u00FCge diese zu den ausgef\u00FChrten queries hinzu
//                if(q.isUnionQuery())
//                {
//                    Query[] sqs = q.getSubQueries();
//                    
//                    
//                    for(int j =0;j<sqs.length;j++)
//                    {
//                        sqs[j].setParameters(params);
//                        logger.debug("Subquery"+sqs[j]);
//                        SearchResult s = null;
//                        try
//                        {
//                            if(qex.serviceAvailable(sqs[j].getQueryIdentifier().getDomain() ))
//                                s = (SearchResult) qex.executeQuery(user,qcs[i].getClassIds(),sqs[j]);
//                            
//                            if(s==null||(s.isEmpty()&&q.isConjunction()))// wenn server nicht da \u00FCberspringen
//                                continue;
//                            
//                            if(sqs[j].isConjunction())
//                            {
//                                
//                                if(!v.retainerSet())
//                                {
//                                    if(v.getResult()!=null&&((HashSet)v.getResult()).size()>0)
//                                        v.setRetainer(v.intersect((HashSet)s.getResult(),(HashSet)v.getResult()));// setze retainer mit suchergebnis
//                                    else
//                                        v.setRetainer((HashSet)s.getResult());
//                                }
//                                else
//                                {
//                                    if (!q.isConjunction())
//                                        v.setRetainer( v.intersect(v.getRetainer(),(HashSet)s.getResult() )); // schnittmenge 2er retainer
//                                    else
//                                    {
//                                        HashSet h = (HashSet)s.getResult();
//                                        h.addAll(v.getRetainer());
//                                        v.setRetainer(h);
//                                        
//                                    }
//                                }
//                            }
//                            
//                            v.addAll(s);
//                        }
//                        catch(Exception e)
//                        {
//                            logger.error(e,e);
//                        }
//                        
//                        
//                    }
//                }
                
            }
        }
        catch(Exception e)
        {
            logger.error(e,e);}
        
        
        
        
        return v;
        
    }
    
    
}
