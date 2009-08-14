/*
 * SearchService.java
 *
 * Created on 12. November 2003, 18:31
 */

package Sirius.server.middleware.interfaces.domainserver;
import java.rmi.*;
import java.util.*;
import Sirius.server.search.*;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;
/**
 *
 * @author  schlob
 */
public interface SearchService extends Remote

{
    
    //add single query root and leaf returns a query_id
    public int addQuery(String name,String description,String statement,int resultType,char isUpdate, char isBatch,char isRoot,char isUnion) throws RemoteException;
     
    public int addQuery(String name,String description,String statement) throws RemoteException;
    
    public boolean addQueryParameter(int queryId,int typeId,String paramkey,String description,char isQueryResult,int queryPosition) throws RemoteException;
    
    //position set in order of the addition
    public boolean addQueryParameter(int queryId,String paramkey,String description) throws RemoteException;
     
    public HashMap getSearchOptions(User user) throws RemoteException;
           
    public SearchResult search(User user,int[] classIds,Query query) throws RemoteException;
    
    
}
