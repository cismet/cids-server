/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.query.querystore;

import Sirius.server.newuser.*;
import Sirius.server.property.*;
import Sirius.server.search.store.*;
import Sirius.server.sql.*;

import java.io.*;

import java.rmi.*;

import java.sql.*;

import java.util.*;
/**
 * Der QueryServer speichert Benutzer- und Gruppensuchprofile sowie Suchergebnisse eines Benutzers in der Datenbank und
 * stellt diese zur Verf\u00FCgung.*
 *
 * @version  $Revision$, $Date$
 */

public class Store {

    //~ Instance fields --------------------------------------------------------

    // --------Prepared Statements ------------------------------

    /** SQL-Befehl zum Speichern eines Suche-Profiles von einem User.* */
    protected PreparedStatement storeUserQuery;

    /** SQL-Befehl zum Speichern eines Suche-Profiles von einem User.* */
    protected PreparedStatement storeUserGroupQuery;

    /** SQL-Befehl zum Abfragen von QueryInfos von einem User.* */
    protected PreparedStatement getUserQueryInfo;

    protected PreparedStatement getFileName;

    protected PreparedStatement getUserGroupQueryInfo;

    protected PreparedStatement getUserGroupInfo;

    /** SQL-Befehl zum Abfragen eines Suche-Profiles einer UserGroup.* */
    protected PreparedStatement getUserQuery;

    /** SQL-Befehl zum Abfragen eines Suche-Profiles einer UserGroup.* */
    protected PreparedStatement getUserGroupQuery;

    /** SQL-Befehl, loescht Such-Profil eines Users.* */
    protected PreparedStatement deleteUserQuery;

    /** SQL-Befehl, loescht Such-Profil eines Users.* */
    protected PreparedStatement deleteUserQueryGroupAssoc;

    /** SQL-Befehl, aktualisiert Suchergebnis eines Users.* */
    protected PreparedStatement updateUserQuery;

    protected PreparedStatement getUserGroupId;

    // -----------------------------------------

    /** SQL-Befehl, ermittelt die groesste store.* */
    protected PreparedStatement maxId;

    /** Serverkonfiguration.* */
    protected ServerProperties properties;

    protected File qsd;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    // ---------------------------------------------------------------
    /**
     * Konstruktor.
     *
     * @param  con         Connection zur Datenbank, bekommt der QueryServer vom LocalServer uebergeben
     * @param  properties  ServerKonfiguration, bekommt der QueryServer vom LocalServer uebergeben*
     */
    public Store(final Connection con, final ServerProperties properties) {
        try {
            storeUserQuery = con.prepareStatement(
                    "INSERT INTO cs_query_store (id, user_id,name, file_name) VALUES (?,?,?,?)");

            storeUserGroupQuery = con.prepareStatement(
                    "INSERT INTO cs_query_store_ug_assoc (ug_id, domainname, query_store_id, permission) VALUES (?,(select distinct id from cs_domain where name = ?),?,1)");

            getUserQueryInfo = con.prepareStatement("SELECT * FROM cs_query_store WHERE user_id = ?");

            getUserGroupInfo = con.prepareStatement(
                    "SELECT ug_id,query_store_id FROM cs_query_store_ug_assoc,cs_query_store WHERE user_id = ? and cs_query_store.id = cs_query_store_ug_assoc.query_store_id");

            getUserGroupQueryInfo = con.prepareStatement(
                    "SELECT qs.id, qs.name,qs.file_name FROM cs_query_store_ug_assoc qsa, cs_query_store qs WHERE qsa.ug_id = ? and qs.id = qsa.query_store_id");

            getUserQuery = con.prepareStatement("SELECT file_name,name FROM cs_query_store WHERE id = ? ");

            updateUserQuery = con.prepareStatement("UPDATE cs_query_store SET file_name = ? WHERE id =?");

            deleteUserQuery = con.prepareStatement("DELETE from cs_query_store WHERE id = ?");

            deleteUserQueryGroupAssoc = con.prepareStatement(
                    "DELETE from cs_query_store_ug_assoc WHERE query_store_id = ?");

            maxId = con.prepareStatement("SELECT MAX(id) FROM cs_query_store");

            getUserGroupId = con.prepareStatement(
                    "SELECT distinct id from cs_ug where upper(name) = upper(?) and domain = ( select id from cs_domain where upper(name)=upper(?))");

            // -----------------------------------------------------------------------------------------

            this.properties = properties;

            // Erzeuge Verzeichnisse f\u00FCr den Querystore
            qsd = new File(properties.getQueryStoreDirectory());
            qsd.mkdirs();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    //~ Methods ----------------------------------------------------------------

    // -------------------------------------------------------------------------
    /**
     * speichert Such-Profil von einem User.
     *
     * @param   user  User, der das Profil speichern will
     * @param   data  query das Profil, das gespeichert werden soll
     *
     * @return  true, wenn gespeichert *
     */
    public boolean storeQuery(final User user, final QueryData data) {
        int effected = 0;

        try {
            int maxId = data.getID();

            if (maxId == -1) // new Query need to be stored completely
            {
                maxId = getMaxId();

                storeUserQuery.setInt(1, maxId);
                storeUserQuery.setInt(2, user.getId());
                storeUserQuery.setString(3, data.getName());

                // filename constructed during runtime

                final String fileName = createFilename(data, user, maxId);

                writeFile(data.getData(), fileName);

                storeUserQuery.setString(4, fileName);

                effected = storeUserQuery.executeUpdate();
            } else // change file only
            {
                deleteFile(data.getFileName());
                writeFile(data.getData(), data.getFileName());

                effected = 1;
            }

            final HashSet ugs = data.getUserGroups();
            if (logger.isDebugEnabled()) {
                logger.debug("user group in storeQuery" + ugs);
            }

            if (!ugs.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ugs is not empty try to insert userGroupProfile");
                }

                final Iterator iter = ugs.iterator();
                while (iter.hasNext()) {
                    final String ugKey = (String)iter.next();

                    final Object[] ugk = UserGroup.parseKey(ugKey);

                    if (properties.getServerName().equalsIgnoreCase((String)ugk[1])) {
                        ;
                    }
                    ugk[1] = "LOCAL";

                    getUserGroupId.setString(1, (String)ugk[0]);
                    getUserGroupId.setString(2, (String)ugk[1]);

                    final ResultSet ugidSet = getUserGroupId.executeQuery();

                    int ugid = -1;
                    if (ugidSet.next()) {
                        ugid = ugidSet.getInt(1);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug(ugid + " usergroupid f\u00FCr " + ugKey);
                    }
                    if (ugid == -1) {
                        break; // raus da:-)
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("vor insert ugProfile f\u00FCr ugid ==" + ugid);
                    }

                    storeUserGroupQuery.setInt(1, ugid); // ck[1] == domainname nur f\u00FCr lokale ugs
                    storeUserGroupQuery.setString(2, (String)ugk[1]);
                    storeUserGroupQuery.setInt(3, maxId);
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "beim insert in UserProfile wurden datens\u00E4tze hinzugef\u00FCgt #="
                            + effected);
                    }
                    effected += storeUserGroupQuery.executeUpdate();
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "beim insert in UserGroupProfile + Userprofile wurden datens\u00E4tze hinzugef\u00FCgt #="
                            + effected);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return effected > 0;
    }

    /**
     * -------------------------------------------------------------------------
     *
     * @param   user  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public QueryInfo[] getQueryInfos(final User user) {
        final HashMap result = new HashMap(10, 10);
        try {
            getUserQueryInfo.setInt(1, user.getId());
            if (logger.isDebugEnabled()) {
                logger.debug("try to retrieve UserQueryInfo in getQueryInfos(usr)");
            }
            final ResultSet rs = getUserQueryInfo.executeQuery();
            final String domain = properties.getServerName();
            while (rs.next()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "try to retrieve UserQueryInfo in getQueryInfos(usr) result retrieved try to getInt(id)");
                }
                final int id = rs.getInt("id");
                result.put(
                    new Integer(id),
                    new QueryInfo(id, (rs.getString("name")).trim(), domain, rs.getString("file_name")));
            }

            getUserGroupInfo.setInt(1, user.getId());
            if (logger.isDebugEnabled()) {
                logger.debug("try to retrieve UserGroupinfos for" + user);
            }
            final ResultSet rs2 = getUserGroupInfo.executeQuery();

            int qs_id = 0;
            int ug_id = 0;
            while (rs2.next()) {
                qs_id = rs2.getInt("query_store_id"); // xxx
                ug_id = rs2.getInt("ug_id");

                // add userGroup to QueryInfo of this user
                ((QueryInfo)result.get(new Integer(qs_id))).addUserGroup(ug_id + "@" + domain);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return (QueryInfo[])result.values().toArray(new QueryInfo[result.size()]);
    }

    /**
     * -------------------------------------------------------------------------
     *
     * @param   ug  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public QueryInfo[] getQueryInfos(final UserGroup ug) {
        final HashMap result = new HashMap(10, 10);
        try {
            getUserGroupQueryInfo.setInt(1, ug.getId());
            if (logger.isDebugEnabled()) {
                logger.debug("try to retrieve UserGroupQueryInfo in getQueryInfos(ug)");
            }
            final ResultSet rs = getUserGroupQueryInfo.executeQuery();
            final String domain = properties.getServerName();
            while (rs.next()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "try to retrieve UserGroupQueryInfo in getQueryInfos(ug) result retrieved try to getInt(id)");
                }

                final int id = rs.getInt("id");
                result.put(
                    new Integer(id),
                    new QueryInfo(id, (rs.getString("name")).trim(), domain, rs.getString("file_name")));
            }

            // interessiert hier nicht oder ??? xxx
            /*   getUserGroupInfo.setInt(1,ug.getID());
             * rs = getUserGroupQueryInfo.executeQuery();  int qs_id=0; int ug_id=0; while(rs.next()) {     qs_id =
             * rs.getInt("id");     ug_id= rs.getInt(ug_id);      // add userGroup to QueryInfo of this user
             * ((QueryInfo)result.get(new Integer(qs_id))).addUserGroup(ug_id+"@"+domain);  }
             */
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return (QueryInfo[])result.values().toArray(new QueryInfo[result.size()]);
    }

    /**
     * -------------------------------------------------------------------------
     *
     * @param   queryId  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public QueryData getQuery(final int queryId) {
        ResultSet rs = null;
        QueryData q = null;

        try {
            getUserQuery.setInt(1, queryId);
            rs = getUserQuery.executeQuery();

            String fileName = null;
            final String name = null;
            // String domain=null;
            byte[] data = new byte[0];

            if (rs.next()) {
                fileName = rs.getString("file_name").trim();
                rs.getString("name").trim();
            }

            data = readFile(fileName);
            if (logger.isDebugEnabled()) {
                logger.debug("info :: data " + data);
            }

            q = new QueryData(queryId, properties.getServerName(), name, fileName, data); // file auslesen
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
        return q;
    }

    // -------------------------------------------------------------------------------
    /**
     * loescht ein Such-Profil oder ein Suchergebnis.
     *
     * @param   id  UserId, UserGroupId oder queryResultId
     *
     * @return  true, wenn erfolgreich geloescht, sonst false *
     */

    // erg\u00E4nzen durch QueryInfo
    public boolean delete(final int id) {
        int effected = 0;

        try {
            // delete file first
            getUserQuery.setInt(1, id);
            final ResultSet rs = getUserQuery.executeQuery();

            if (rs.next()) {
                final String fileName = rs.getString("file_name").trim();
                deleteFile(fileName);
            }

            deleteUserQuery.setInt(1, id);
            deleteUserQueryGroupAssoc.setInt(1, id);
            // delete store entry
            effected = deleteUserQuery.executeUpdate();

            // delete user group assocs
            effected += deleteUserQueryGroupAssoc.executeUpdate();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return effected > 0;
    }

    /**
     * ------------------------------------------------------------------------------
     *
     * @param   user  DOCUMENT ME!
     * @param   data  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean updateQuery(final User user, final QueryData data) {
        return storeQuery(user, data);
    }

    /**
     * /////////////////////////////////////////////////////////////////////////////
     * //////////////////////////////////////////////////////////////////////////////
     * /////////////////////////////////////////////////////////////////////////////
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    private int getMaxId() throws Exception {
        final ResultSet rs = maxId.executeQuery();

        if (rs.next()) {
            // max+1
            return rs.getInt(1) + 1;
        }

        // first entry
        return 0;
    }

    ////////////////////////////////////////////////////////////////////////////////////////

    /**
     * DOCUMENT ME!
     *
     * @param  data      DOCUMENT ME!
     * @param  fileName  DOCUMENT ME!
     */
    private void writeFile(final byte[] data, final String fileName) {
        try {
            final File outputFile = new File(qsd, fileName);

            final FileOutputStream out = new FileOutputStream(outputFile);

            out.write(data, 0, data.length);

            out.close();
        } catch (Exception e) {
            logger.error("<LS> ERROR :: ", e);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   fileName  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private byte[] readFile(final String fileName) {
        byte[] data = null;

        try {
            final File inFile;
            final FileInputStream stream;

            inFile = new File(qsd, fileName);

            data = new byte[(int)inFile.length()];
            stream = new FileInputStream(inFile);

            // read the file into data
            final int bytesRead = stream.read(data, 0, (int)inFile.length());

            if (bytesRead == -1) { // error occured during readingprocess
                throw new Exception("read fehlgeschlagen");
            } else if (bytesRead != (int)inFile.length()) {
                throw new Exception("Information wahrscheinlich Fehlerhaft");
            }

            stream.close();
        } catch (Exception e) {
            logger.error("<LS> ERROR :: ", e);
            data = new byte[0];
        }

        return data;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  fileName  DOCUMENT ME!
     */
    private void deleteFile(final String fileName) {
        final File f = new File(qsd, fileName);
        f.delete();
    }

    /**
     * ///////////////////////////////////////////////// /////////////////////////////////////////////
     *
     * @param   data  DOCUMENT ME!
     * @param   user  DOCUMENT ME!
     * @param   id    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String createFilename(final QueryData data, final User user, final int id) {
        return id + user.getName() + data.getName() + System.currentTimeMillis() + ".str";
    }
} // end class
