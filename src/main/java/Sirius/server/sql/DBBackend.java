/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import Sirius.server.Shutdownable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public interface DBBackend extends Shutdownable {

    //~ Methods ----------------------------------------------------------------

    /**
     * Sets the number of retries the backend shall perform in case of an error during execution. A negative integer
     * will be interpreted as <code>0</code>. <b>The retry shall only take place if the underlying error indicates that
     * the retry could be senseful (e.g. connection drop, db restart ...).</b>
     *
     * @param  noOfRetries  the number of retries that shall be performed in case of an error during execution
     *
     * @see    #getRetriesOnError()
     */
    void setRetriesOnError(int noOfRetries);

    /**
     * Gets the current number of retries. <code>0</code> or a negative <code>int</code> means that the operation will
     * be performed exactly once, <code>1</code> means that the operation will be executed up to two times, <code>
     * 2</code> up to three times and so on. <b>The retry shall only take place if the underlying error indicates that
     * the retry could be senseful (e.g. connection drop, db restart ...).</b>
     *
     * @return  the current number of retries
     */
    int getRetriesOnError();

    /**
     * Returns a native JDBC {@link Connection}.
     *
     * @return  a native JDBC <code>Connection</code>
     *
     * @throws  SQLException  if any error occurs while acquiring the <code>Connection</code>
     */
    Connection getConnection() throws SQLException;

    /**
     * This operation shall be used to execute queries that related to the tables of the underlying cids system (cs_*).
     * The available query descriptors are defined within this interface. Any other given descriptor String results in a
     * {@link SQLException}.<br/>
     * The number of given parameters must match the parameter count of the statement described by the given descriptor.
     * If the number of the parameters mismatch a <code>SQLException</code> is thrown.<br/>
     * <br/>
     * This operation shall throw a <code>SQLException</code> if the {@link Shutdownable#shutdown()} has already been
     * called.
     *
     * @param   descriptor  one of the descriptors defined in this interface
     * @param   parameters  the parameters the underlying query will take
     *
     * @return  the {@link ResultSet} of the executed query
     *
     * @throws  SQLException  <ul>
     *                          <li>if the given descriptor is invalid</li>
     *                          <li>if the parameter count mismatches</li>
     *                          <li>if any other error occurs during execution</li>
     *                          <li>if shutdown has been called</li>
     *                        </ul>
     */
    ResultSet submitInternalQuery(final String descriptor, final Object... parameters) throws SQLException;

    /**
     * This operation shall be used to execute updates that related to the tables of the underlying cids system (cs_*).
     * The available query descriptors are defined within this interface. Any other given descriptor String results in a
     * {@link SQLException}.<br/>
     * The number of given parameters must match the parameter count of the statement described by the given descriptor.
     * If the number of the parameters mismatch a <code>SQLException</code> is thrown.<br/>
     * <br/>
     * This operation shall throw a <code>SQLException</code> if the {@link Shutdownable#shutdown()} has already been
     * called.
     *
     * @param   descriptor  one of the descriptors defined in this interface
     * @param   parameters  the parameters the underlying query will take
     *
     * @return  the row count affected by the executed update
     *
     * @throws  SQLException  <ul>
     *                          <li>if the given descriptor is invalid</li>
     *                          <li>if the parameter count mismatches</li>
     *                          <li>if any other error occurs during execution</li>
     *                          <li>if shutdown has been called</li>
     *                        </ul>
     */
    int submitInternalUpdate(final String descriptor, final Object... parameters) throws SQLException;

    /**
     * DOCUMENT ME!<br/>
     * <br/>
     * This operation shall throw a <code>SQLException</code> if the {@link Shutdownable#shutdown()} has already been
     * called.
     *
     * @param   descriptor  DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    ResultSet submitQuery(final String descriptor, final Object... parameters) throws SQLException;

    /**
     * DOCUMENT ME!<br/>
     * <br/>
     * This operation shall throw a <code>SQLException</code> if the {@link Shutdownable#shutdown()} has already been
     * called.
     *
     * @param   sqlID       DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    ResultSet submitQuery(final int sqlID, final java.lang.Object... parameters) throws SQLException;

    /**
     * DOCUMENT ME!<br/>
     * <br/>
     * This operation shall throw a <code>SQLException</code> if the {@link Shutdownable#shutdown()} has already been
     * called.
     *
     * @param   descriptor  DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    int submitUpdate(final String descriptor, final Object... parameters) throws SQLException;

    /**
     * DOCUMENT ME!<br/>
     * <br/>
     * This operation shall throw a <code>SQLException</code> if the {@link Shutdownable#shutdown()} has already been
     * called.
     *
     * @param   sqlID       DOCUMENT ME!
     * @param   parameters  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  SQLException  DOCUMENT ME!
     */
    int submitUpdate(final int sqlID, final Object... parameters) throws SQLException;
}
