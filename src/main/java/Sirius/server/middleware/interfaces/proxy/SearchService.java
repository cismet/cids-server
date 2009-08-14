/*
 * SearchService.java
 *
 * Created on 10. November 2003, 17:39
 */



package Sirius.server.middleware.interfaces.proxy;


import java.rmi.*;
import java.util.*;
import Sirius.server.search.*;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;
/**
 * encapsulates query, search mechanisms
 * @author schlob
 */

public interface SearchService extends Remote{
    
    //add single query root and leaf returns a query_id
    /**
     * enables user to register queries with a domain server
     * @param user user token
     * @param name name of the query (key)
     * @param description description of the query
     * @param statement sql statement to be executed when the query with the key name is called
     * @param resultType result type of the query when executed (eg node)
     * @param isUpdate indicates whether the query affects changes
     * @param isBatch indicates whether the query consists of several queries (batch)
     * @param isRoot indicates whether the query is a subquery which itself is not executable
     * @param isUnion indicates whether this query is part of a union statement
     * @throws java.rmi.RemoteException server error (eg query name already exists)
     * @return query addes succesfully 
     */
    public int addQuery(User user,String name,String description,String statement,int resultType,char isUpdate,char isBatch,char isRoot,char isUnion) throws RemoteException;
     
    /**
     * enables user to register queries with a domain server where for convenience
     * several parameters (eg isRoot) have default values.
     * @param user user token
     * @param name query name (key)
     * @param description description of the query (eg this query collects all altlasten objects older than yesterday)
     * @param statement sql statement executed when the query with key name is called
     * @throws java.rmi.RemoteException server error (eg query name already exists)
     * @return whether a query was added successfully
     */
    public int addQuery(User user,String name,String description,String statement) throws RemoteException;
    
    /**
     * adds a Queryparameter to a corresponding query eg
     * 
     * select * from ? 
     * 
     * has a the table name as parameter this parameter table name has to be registered
     * inaddtion to the query itself.
     * @param user user token
     * @param queryId id of they query where the parameter belongs to
     * @param typeId type of the parameter
     * @param paramkey key of the parameter (eg table_name)
     * @param description description of the parameter
     * @param isQueryResult states whether this parameter has to be assigned before the query is executed or whether the paramter
     * is the result of a subquery
     * @param queryPosition indicates the position of the parameter in the corresponding query
     * bginning with 0
     * eg
     * 
     * select ? from ? where ? = ? has 4 parameters with the positions 0-3
     * @throws java.rmi.RemoteException server error (eg the corresponding query does not exist)
     * @return whether the parameter was added succesfully
     */
    public boolean addQueryParameter(User user,int queryId,int typeId,String paramkey,String description,char isQueryResult,int queryPosition) throws RemoteException;
    
    //position set in order of the addition
    /**
     * adds a Queryparameter to a corresponding query eg
     * 
     * select * from ? 
     * 
     * has a the table name as parameter this parameter table name has to be registered
     * in addtion to the query itself.
     * 
     * for convenience some method parameteers are set with default values
     * @param user user token
     * @param queryId id of they query where the parameter belongs to
     * @param paramkey key of the parameter (eg table_name)
     * @param description description of the parameter
     * @throws java.rmi.RemoteException server error (eg the corresponding query does not exist)
     * @return whether the parameter was added succesfully
     */
    public boolean addQueryParameter(User user,int queryId,String paramkey,String description) throws RemoteException;
    
    /**
     * retrieves all available search options in the MIS
     * @param user user token
     * @throws java.rmi.RemoteException server errror 
     * @return list of available search otptions (eg full text search)
     */
    public HashMap getSearchOptions(User user) throws RemoteException;
        
    /**
     * retrieves search options available at a certain domain
     * @param user user token
     * @param domain domain offering search options eg (full text search)
     * @throws java.rmi.RemoteException server error
     * @return list of availabel search options
     */
    public HashMap getSearchOptions(User user,String domain) throws RemoteException;
    
    /**
     * main search method of the mis
     * @param user user token 
     * @param classIds class selection (similar to a sql from clause)
     * @param searchOptions indicates what kind of search is to be performed
     * @throws java.rmi.RemoteException server error
     * @return search results
     */
    public SearchResult search(User user, String[] classIds ,SearchOption[] searchOptions) throws RemoteException;
    
    
    
    
}
