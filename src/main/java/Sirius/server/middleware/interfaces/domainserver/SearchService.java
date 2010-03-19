/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public interface SearchService extends Remote {

    //~ Methods ----------------------------------------------------------------

    /**
     * add single query root and leaf returns a query_id.
     *
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     * @param   resultType   DOCUMENT ME!
     * @param   isUpdate     DOCUMENT ME!
     * @param   isBatch      DOCUMENT ME!
     * @param   isRoot       DOCUMENT ME!
     * @param   isUnion      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    int addQuery(
            String name,
            String description,
            String statement,
            int resultType,
            char isUpdate,
            char isBatch,
            char isRoot,
            char isUnion) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   name         DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     * @param   statement    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    int addQuery(String name, String description, String statement) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   queryId        DOCUMENT ME!
     * @param   typeId         DOCUMENT ME!
     * @param   paramkey       DOCUMENT ME!
     * @param   description    DOCUMENT ME!
     * @param   isQueryResult  DOCUMENT ME!
     * @param   queryPosition  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean addQueryParameter(
            int queryId,
            int typeId,
            String paramkey,
            String description,
            char isQueryResult,
            int queryPosition) throws RemoteException;
    /**
     * position set in order of the addition.
     *
     * @param   queryId      DOCUMENT ME!
     * @param   paramkey     DOCUMENT ME!
     * @param   description  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    boolean addQueryParameter(int queryId, String paramkey, String description) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    HashMap getSearchOptions(User user) throws RemoteException;

    /**
     * DOCUMENT ME!
     *
     * @param   user      DOCUMENT ME!
     * @param   classIds  DOCUMENT ME!
     * @param   query     DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  RemoteException  DOCUMENT ME!
     */
    SearchResult search(User user, int[] classIds, Query query) throws RemoteException;
}
