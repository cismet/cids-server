/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.interfaces.proxy;

import Sirius.server.newuser.User;
import Sirius.server.search.SearchOption;
import Sirius.server.search.SearchResult;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Collection;
import java.util.HashMap;

import de.cismet.cids.server.search.CidsServerSearch;

/**
 * encapsulates query, search mechanisms.
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public interface SearchService extends Remote {

    //~ Methods ----------------------------------------------------------------

    // add single query root and leaf returns a query_id
    /**
     * enables user to register queries with a domain server.
     *
     * @param   user         user token
     * @param   name         name of the query (key)
     * @param   description  description of the query
     * @param   statement    sql statement to be executed when the query with the key name is called
     * @param   resultType   result type of the query when executed (eg node)
     * @param   isUpdate     indicates whether the query affects changes
     * @param   isBatch      indicates whether the query consists of several queries (batch)
     * @param   isRoot       indicates whether the query is a subquery which itself is not executable
     * @param   isUnion      indicates whether this query is part of a union statement
     *
     * @return  query addes succesfully
     *
     * @throws  RemoteException  server error (eg query name already exists)
     */
    int addQuery(
            User user,
            String name,
            String description,
            String statement,
            int resultType,
            char isUpdate,
            char isBatch,
            char isRoot,
            char isUnion) throws RemoteException;

    /**
     * enables user to register queries with a domain server where for convenience several parameters (eg isRoot) have
     * default values.
     *
     * @param   user         user token
     * @param   name         query name (key)
     * @param   description  description of the query (eg this query collects all altlasten objects older than
     *                       yesterday)
     * @param   statement    sql statement executed when the query with key name is called
     *
     * @return  whether a query was added successfully
     *
     * @throws  RemoteException  server error (eg query name already exists)
     */
    int addQuery(User user, String name, String description, String statement) throws RemoteException;

    /**
     * adds a Queryparameter to a corresponding query eg.
     *
     * <p>select * from ?</p>
     *
     * <p>has a the table name as parameter this parameter table name has to be registered inaddtion to the query
     * itself.</p>
     *
     * @param   user           user token
     * @param   queryId        id of they query where the parameter belongs to
     * @param   typeId         type of the parameter
     * @param   paramkey       key of the parameter (eg table_name)
     * @param   description    description of the parameter
     * @param   isQueryResult  states whether this parameter has to be assigned before the query is executed or whether
     *                         the paramter is the result of a subquery
     * @param   queryPosition  indicates the position of the parameter in the corresponding query bginning with 0 eg
     *
     *                         <p>select ? from ? where ? = ? has 4 parameters with the positions 0-3</p>
     *
     * @return  whether the parameter was added succesfully
     *
     * @throws  RemoteException  server error (eg the corresponding query does not exist)
     */
    boolean addQueryParameter(
            User user,
            int queryId,
            int typeId,
            String paramkey,
            String description,
            char isQueryResult,
            int queryPosition) throws RemoteException;

    // position set in order of the addition
    /**
     * adds a Queryparameter to a corresponding query eg.
     *
     * <p>select * from ?</p>
     *
     * <p>has a the table name as parameter this parameter table name has to be registered in addtion to the query
     * itself.</p>
     *
     * <p>for convenience some method parameteers are set with default values</p>
     *
     * @param   user         user token
     * @param   queryId      id of they query where the parameter belongs to
     * @param   paramkey     key of the parameter (eg table_name)
     * @param   description  description of the parameter
     *
     * @return  whether the parameter was added succesfully
     *
     * @throws  RemoteException  server error (eg the corresponding query does not exist)
     */
    boolean addQueryParameter(User user, int queryId, String paramkey, String description) throws RemoteException;

    /**
     * retrieves all available search options in the MIS.
     *
     * @param   user  user token
     *
     * @return  list of available search otptions (eg full text search)
     *
     * @throws  RemoteException  server errror
     */
    HashMap getSearchOptions(User user) throws RemoteException;

    /**
     * retrieves search options available at a certain domain.
     *
     * @param   user    user token
     * @param   domain  domain offering search options eg (full text search)
     *
     * @return  list of availabel search options
     *
     * @throws  RemoteException  server error
     */
    HashMap getSearchOptions(User user, String domain) throws RemoteException;

    /**
     * main search method of the mis.
     *
     * @param   user           user token
     * @param   classIds       class selection (similar to a sql from clause)
     * @param   searchOptions  indicates what kind of search is to be performed
     *
     * @return  search results
     *
     * @throws  RemoteException  server error
     */
    SearchResult search(User user, String[] classIds, SearchOption[] searchOptions) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user          DOCUMENT ME!
     * @param   serverSearch  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    Collection customServerSearch(final User user, CidsServerSearch serverSearch) throws RemoteException;
}
