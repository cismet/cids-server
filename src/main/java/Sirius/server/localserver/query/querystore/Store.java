package Sirius.server.localserver.query.querystore;



import Sirius.server.newuser.*;
import Sirius.server.property.*;
import java.sql.*;
import java.rmi.*;
import Sirius.server.sql.*;
import Sirius.server.search.store.*;
import java.io.*;
import java.util.*;
/** Der QueryServer speichert Benutzer- und Gruppensuchprofile sowie Suchergebnisse
 * eines Benutzers in der Datenbank und stellt diese zur Verf\u00FCgung **/

public class Store
{
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    //--------Prepared Statements ------------------------------
    
    /** SQL-Befehl zum Speichern eines Suche-Profiles von einem User**/
    protected PreparedStatement storeUserQuery;
    
    /** SQL-Befehl zum Speichern eines Suche-Profiles von einem User**/
    protected PreparedStatement storeUserGroupQuery;
    
    
    /** SQL-Befehl zum Abfragen von QueryInfos von einem User**/
    protected PreparedStatement getUserQueryInfo;
    
    protected PreparedStatement getFileName;
    
    protected PreparedStatement getUserGroupQueryInfo;
    
    protected PreparedStatement getUserGroupInfo;
    
    /** SQL-Befehl zum Abfragen eines Suche-Profiles einer UserGroup**/
    protected PreparedStatement getUserQuery;
    
    /** SQL-Befehl zum Abfragen eines Suche-Profiles einer UserGroup**/
    protected PreparedStatement getUserGroupQuery;
    
    /** SQL-Befehl, loescht Such-Profil eines Users**/
    protected PreparedStatement deleteUserQuery;
    
    /** SQL-Befehl, loescht Such-Profil eines Users**/
    protected PreparedStatement deleteUserQueryGroupAssoc;
    
    
    /** SQL-Befehl, aktualisiert Suchergebnis eines Users**/
    protected PreparedStatement updateUserQuery;
    
    protected PreparedStatement getUserGroupId;
    
    
    //-----------------------------------------
    
    /** SQL-Befehl, ermittelt die groesste store**/
    protected PreparedStatement maxId;
    
    /** Serverkonfiguration **/
    protected ServerProperties properties;
    
    protected File qsd;
    
    
    
    //---------------------------------------------------------------
    /** Konstruktor
     * @param con Connection zur Datenbank, bekommt der QueryServer vom LocalServer uebergeben
     * @param properties ServerKonfiguration, bekommt der QueryServer vom LocalServer uebergeben**/
    public Store(Connection con, ServerProperties properties)
    {
        try
        {
            storeUserQuery=con.prepareStatement("INSERT INTO cs_query_store (id, user_id,name, file_name) VALUES (?,?,?,?)");
            
            storeUserGroupQuery=con.prepareStatement("INSERT INTO cs_query_store_ug_assoc (ug_id, domainname, query_store_id, permission) VALUES (?,(select distinct id from cs_domain where name = ?),?,1)");
            
            getUserQueryInfo=con.prepareStatement("SELECT * FROM cs_query_store WHERE user_id = ?");
            
            getUserGroupInfo=con.prepareStatement("SELECT ug_id,query_store_id FROM cs_query_store_ug_assoc,cs_query_store WHERE user_id = ? and cs_query_store.id = cs_query_store_ug_assoc.query_store_id");
            
            getUserGroupQueryInfo=con.prepareStatement("SELECT id,name,file_name FROM cs_query_store_ug_assoc,cs_query_store WHERE ug_id = ? and cs_query_store.id = cs_query_store_ug_assoc.query_store_id");
            
            getUserQuery=con.prepareStatement("SELECT file_name,name FROM cs_query_store WHERE id = ? ");
            
            updateUserQuery=con.prepareStatement("UPDATE cs_query_store SET file_name = ? WHERE id =?");
            
            deleteUserQuery=con.prepareStatement("DELETE from cs_query_store WHERE id = ?");
            
            deleteUserQueryGroupAssoc=con.prepareStatement("DELETE from cs_query_store_ug_assoc WHERE query_store_id = ?");
            
            maxId = con.prepareStatement("SELECT MAX(id) FROM cs_query_store");
            
            getUserGroupId = con.prepareStatement("SELECT distinct id from cs_ug where upper(name) = upper(?) and domain = ( select id from cs_domain where upper(name)=upper(?))");
            
            //-----------------------------------------------------------------------------------------
            
            this.properties = properties;
            
            
            // Erzeuge Verzeichnisse f\u00FCr den Querystore
            qsd = new File(properties.getQueryStoreDirectory());
            qsd.mkdirs();
            
            
        }
        catch(Exception e)
        {
            ExceptionHandler.handle(e);
            
        }
    }
    
    
    
    //-------------------------------------------------------------------------
    /** speichert Such-Profil von einem User
     * @param user User, der das Profil speichern will
     * @param query das Profil, das gespeichert werden soll
     * @return true, wenn gespeichert **/
    public boolean storeQuery(User user, QueryData data)
    {
        int effected = 0;
        
        try
        {
            int maxId =data.getID();
            
            if(maxId==-1)// new Query need to be stored completely
            {
                maxId =getMaxId();
                
                storeUserQuery.setInt(1,maxId );
                storeUserQuery.setInt(2,user.getId());
                storeUserQuery.setString(3, data.getName());
                
                // filename constructed during runtime
                
                String fileName = createFilename( data,user,maxId);
                
                writeFile(data.getData(),fileName);
                
                storeUserQuery.setString(4, fileName);
                
                effected = storeUserQuery.executeUpdate();
                
            }
            else //change file only
            {
                
                
                deleteFile(data.getFileName());
                writeFile(data.getData(),data.getFileName());
                
                effected=1;
                
                
            }
            
            
            HashSet ugs = data.getUserGroups();
            
            logger.debug("user group in storeQuery"+ugs);
            
            
            if(!ugs.isEmpty())
            {
                logger.debug("ugs is not empty try to insert userGroupProfile");
                
                Iterator iter = ugs.iterator();
                while(iter.hasNext())
                {
                    String ugKey = (String)iter.next();
                    
                    Object[] ugk = UserGroup.parseKey(ugKey);
                    
                    if(properties.getServerName().equalsIgnoreCase((String)ugk[1]));
                    ugk[1]="LOCAL";
                    
                    getUserGroupId.setString(1,(String)ugk[0]);
                    getUserGroupId.setString(2,(String)ugk[1]);
                    
                    ResultSet ugidSet = getUserGroupId.executeQuery();
                    
                    int ugid = -1;
                    if(ugidSet.next())
                        ugid =ugidSet.getInt(1);
                    
                    logger.debug(ugid+" usergroupid f\u00FCr "+ugKey);
                    if(ugid==-1)
                        break; // raus da:-)
                    
                    logger.debug("vor insert ugProfile f\u00FCr ugid =="+ugid);
                   
                    storeUserGroupQuery.setInt(1,ugid); // ck[1] == domainname nur f\u00FCr lokale ugs
                    storeUserGroupQuery.setString(2,(String)ugk[1]);
                    storeUserGroupQuery.setInt(3,maxId);
                    
                    logger.debug("beim insert in UserProfile wurden datens\u00E4tze hinzugef\u00FCgt #="+effected);
                    effected+=storeUserGroupQuery.executeUpdate();
                    logger.debug("beim insert in UserGroupProfile + Userprofile wurden datens\u00E4tze hinzugef\u00FCgt #="+effected);
                }
                
            }
            
            
            
            
        }
        catch(Exception e)
        {
            ExceptionHandler.handle(e);
            
        }
        
        return    effected >0;
    }
    
    
    
    //-------------------------------------------------------------------------
    
    public QueryInfo[] getQueryInfos(User user)
    {
        HashMap result = new HashMap(10,10);
        try
        {
            getUserQueryInfo.setInt(1,user.getId());
            logger.debug("try to retrieve UserQueryInfo in getQueryInfos(usr)");
            ResultSet rs =  getUserQueryInfo.executeQuery();
            String domain = properties.getServerName();
            while(rs.next())
            {
                logger.debug("try to retrieve UserQueryInfo in getQueryInfos(usr) result retrieved try to getInt(id)");
                int id =rs.getInt("id");
                result.put(new Integer(id),new QueryInfo(id,(rs.getString("name")).trim() ,domain ,rs.getString("file_name")));
                
            }
            
            
           getUserGroupInfo.setInt(1,user.getId());
           
           logger.debug("try to retrieve UserGroupinfos for"+user);
           ResultSet rs2 = getUserGroupInfo.executeQuery();
            
            
            int qs_id=0;
            int ug_id=0;
            while(rs2.next())
            {
                qs_id = rs2.getInt("query_store_id"); // xxx
                ug_id= rs2.getInt("ug_id");
                
                // add userGroup to QueryInfo of this user
                ((QueryInfo)result.get(new Integer(qs_id))).addUserGroup(ug_id+"@"+domain);
                
            }
            
            
        }
        catch(Exception e)
        {
            ExceptionHandler.handle(e);
            
        }
        
        
        return (QueryInfo[])result.values().toArray(new QueryInfo[result.size()] );
    }
    
    //-------------------------------------------------------------------------
    
    public QueryInfo[] getQueryInfos(UserGroup ug)
    {
        HashMap result = new HashMap(10,10);
        try
        {
            getUserGroupQueryInfo.setInt(1,ug.getId());
            
            logger.debug("try to retrieve UserGroupQueryInfo in getQueryInfos(ug)");
            ResultSet rs =  getUserGroupQueryInfo.executeQuery();
            String domain = properties.getServerName();
            while(rs.next())
            {
                 logger.debug("try to retrieve UserGroupQueryInfo in getQueryInfos(ug) result retrieved try to getInt(id)");
               
                 int id =rs.getInt("id");
                result.put(new Integer(id),new QueryInfo(id,(rs.getString("name")).trim() ,domain ,rs.getString("file_name")));
                
            }
            
            // interessiert hier nicht oder ??? xxx
            /*   getUserGroupInfo.setInt(1,ug.getID());
               rs = getUserGroupQueryInfo.executeQuery();
             
             
               int qs_id=0;
               int ug_id=0;
               while(rs.next())
                {
                    qs_id = rs.getInt("id");
                    ug_id= rs.getInt(ug_id);
             
                    // add userGroup to QueryInfo of this user
                   ((QueryInfo)result.get(new Integer(qs_id))).addUserGroup(ug_id+"@"+domain);
             
                }
             
             */
        }
        catch(Exception e)
        {
            ExceptionHandler.handle(e);
            
        }
        
        
        return (QueryInfo[])result.values().toArray(new QueryInfo[result.size()] );
    }
    
    
    //-------------------------------------------------------------------------
    
    public QueryData getQuery(int queryId)
    {
        ResultSet rs = null;
        QueryData q = null;
        
        try
        {
            
            getUserQuery.setInt(1,queryId);
            rs = getUserQuery.executeQuery();
            
            
            String fileName = null;
            String name=null;
            //String domain=null;
            byte[] data = new byte[0];
            
            if(rs.next())
            {
                
                fileName = rs.getString("file_name").trim();
                rs.getString("name").trim();
                
            }
            
            data = readFile(fileName);
            
            logger.debug("info :: data " +data + data[0]+ data[1]+data[2]);
            
            
            q = new QueryData(queryId,properties.getServerName(),name ,fileName,data);// file auslesen
            
            
        }
        
        catch(Exception e)
        {
            ExceptionHandler.handle(e);
            
        }
        return q;
    }
    
    
    
    //-------------------------------------------------------------------------------
    /** loescht ein Such-Profil oder ein Suchergebnis
     * @param id UserId, UserGroupId oder queryResultId
     * @param tableName Konstante, welche Tabelle angesprochen werden soll, {@link {@link #USR_PROFILE USR_PROFILE}
     * , {@link #UG_PROFILE UG_PROFILE} oder {@link #USR_MEMORY, USR_MEMORY}
     * @return true, wenn erfolgreich geloescht, sonst false **/
    
    
    //erg\u00E4nzen durch QueryInfo
    public boolean delete(int id )
    {
        int effected = 0 ;
        String fileName=null;
        
        try
        {
            //delete file first
            getUserQuery.setInt(1, id);
            ResultSet rs = getUserQuery.executeQuery();
            
            if(rs.next())
            {
                fileName = rs.getString("file_name").trim();
                
            }
            
            deleteFile(fileName);
            
            
            deleteUserQuery.setInt(1,id);
            deleteUserQueryGroupAssoc.setInt(1,id);
            // delete store entry
            effected = deleteUserQuery.executeUpdate();
            
            //delete user group assocs
            effected+=deleteUserQueryGroupAssoc.executeUpdate();
            
        }
        catch(Exception e)
        {
            ExceptionHandler.handle(e);
            
        }
        
        return   effected > 0 ;
    }
    
    
    //------------------------------------------------------------------------------
    
    public boolean updateQuery(User user,QueryData data)
    { return storeQuery(user,data);}
    
    
    ///////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////
    
    private int getMaxId() throws Exception
    {
        
        ResultSet rs = maxId.executeQuery();
        
        if(rs.next())
        {
            //max+1
            return rs.getInt(1)+1;
            
        }
        
        //first entry
        return 0;
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    private void writeFile(byte[] data, String fileName)
    {
        
        
        try
        {
            File outputFile = new File(qsd,fileName);
            
            
            FileOutputStream out = new FileOutputStream(outputFile);
            
            
            out.write(data,0,data.length);
            
            
            out.close();
        }
        catch(Exception e)
        {
            logger.error("<LS> ERROR :: ",e);
            
        }
        
    }
    
    
    private byte[] readFile(String fileName)
    {
        byte[] data=null;
        
        try
        {
            File inFile;
            FileInputStream stream;
            
            inFile = new File(qsd,fileName);
            
            data = new byte[(int) inFile.length()];
            stream = new FileInputStream(inFile);
            
            //read the file into data
            int bytesRead = stream.read(data,0,(int) inFile.length());
            
            if (bytesRead == -1) // error occured during readingprocess
                throw new Exception("read fehlgeschlagen");
            else if (bytesRead != (int) inFile.length())
                throw new Exception("Information wahrscheinlich Fehlerhaft");
            
            stream.close();
        }
        catch(Exception e)
        {
            logger.error("<LS> ERROR :: ",e);
            data= new byte[0];
        }
        
        
        return data;
        
    }
    
    
    private void deleteFile(String fileName)
    {
        File f = new File(qsd,fileName);
        f.delete();
        
    }
    
    ///////////////////////////////////////////////////
    ///////////////////////////////////////////////
    
    public String createFilename(QueryData data, User user,int id)
    {
        return id+user.getName()+data.getName()+System.currentTimeMillis()+".str";
        
    }
    
    
    
    
    
    
} // end class

