package Sirius.server.sql;


import de.cismet.tools.Sorter;
import java.sql.*;
import java.net.URL;
import java.math.*;
import java.util.*;
import Sirius.util.*;
import Sirius.server.property.*;
import Sirius.server.search.*;
import Sirius.server.*;
import Sirius.server.search.searchparameter.*;



/**
 * Datenbankverbindung <BR>
 *
 *
 * @version   1.0  erstellt am 05.10.1999
 * @since
 * @author    Sascha Schlobinski
 *
 */



public class DBConnection
{
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    public static boolean charToBool(char bool)
    {return((bool==(byte)'T')|| (bool == (byte)'t'));}
    
    public static boolean stringToBool(String bool)
    {
        if (bool == null|| bool.length()==0)
            return false;
        else
            return charToBool(bool.charAt(0));
    }
    
    private	Connection  con;
    private StatementCache cache;
    
    
    protected final DBClassifier dbc;
    
    
    
    ///////////////////////////////////////
    
    protected DBConnection(DBClassifier dbc) throws Throwable
    {
        
        this.dbc=dbc;
        
        
        
        try
        {
            
            logger.debug("driver  :" + dbc.driver);
            
            
            Class.forName(dbc.driver); // can raise an ClassNotFoundExc.
            
          
            con = DriverManager.getConnection(dbc.url,dbc.login,dbc.pwd);// can raise an SQl EXc.
            
              if(dbc.driver.equals("org.postgresql.Driver"))
            {
                ((org.postgresql.PGConnection)con).addDataType("geometry","org.postgis.PGgeometry"); 
                ((org.postgresql.PGConnection)con).addDataType("box3d","org.postgis.PGbox3d");
                logger.debug("postgis datatypes added to connection");
                
            }
            
            logger.debug("connection established to "+this.dbc);
            
            
            cache = new StatementCache(con);
            
            
            
            
            
        }
        
        catch(java.lang.ClassNotFoundException e)
        {
            
            logger.error("<LS> ERROR :: "+e.getMessage()+" Driver Not Found",e );
            throw new ServerExitError(" Driver Not Found" ,e);
        }
        catch(java.sql.SQLException e)
        {
            ExceptionHandler.handle(e);
            logger.error("<LS> ERROR :: could not connect to "+dbc,e);
            throw new ServerExitError(" could not connect to db" ,e);
        }
        
        catch (java.lang.Exception e)
        {
            logger.error("<LS> ERROR :: "+e.getMessage(), e);
            throw new ServerExitError(e);
            
        }
        
        
        
    }
    
    
    
    
    
    
    
    
    
    //////////////////////////////////////
    
    /**
     * Der momentan angemeldete DBUser<BR>
     *
     * @param
     * @return  java.lang.String User
     * @exeption
     */
    
    
    public final String getUser()
    { return dbc.login;}
    
    
    ///////////////////////////////////////
    
    
    /**
     * Das Passwort des momentan angemeldete DBUsers<BR>
     *
     * @param
     * @return  java.lang.String passwd
     * @exeption
     */
    
    
    public final String getPassword()
    {return dbc.pwd;}
    
    /**
     * url des jdbc-Drivers.
     */
    public final String getURL()
    { return dbc.url;}
    
    /**
     * Klasse des jdbc-Drivers.
     */
    public final String getDriver()
    { return dbc.driver;}
    
    public Connection getConnection()
    {return con;}
    
    
  
    ////////////////////////////////////////
    
    
    /**
     * Setzt das descriptor zugeordnete Statement (Select) ab<BR>
     *
     * @param	java.lang.String descriptor
     * @return  java.sql.ResultSet
     * @exeption java.sql.SQLException
     */
    
    
    
    
    
    //    public ResultSet submitQuery(String descriptor)
    //    {
    //
    //        try
    //        {
    //            String sqlStmnt = fetchStatement(descriptor);
    //
    //            return (con.createStatement()).executeQuery(sqlStmnt);
    //        }
    //        catch (Exception e)
    //        {
    //            ExceptionHandler.handle(e);
    //        }
    //
    //        return null;
    //
    //    }
    //
    ///////////////////////////////////////////////////////////////////////////////////
    
    
    public ResultSet submitQuery(String descriptor,java.lang.Object[] parameters)
    {
        logger.debug("submitQuery: " + descriptor);
        
        try
        {
            String sqlStmnt = fetchStatement(descriptor);
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt,parameters);
            logger.debug("info :: "+sqlStmnt);
            
            return (con.createStatement()).executeQuery(sqlStmnt);
        }
        catch(Exception e)
        {
            logger.error(" Fehler bei SubmitQuery",e);
            ExceptionHandler.handle(e);
            
        }
        
        
        return null;
        
        
    }
    
    
    
    
    
    
    ///////////////////////////////////////////////
    
    /**
     * Setzt das Statement mit der id SqlID (Select) ab<BR>
     *
     * @param	int sqlID
     * @return  java.sql.ResultSet
     * @exeption java.sql.SQLException
     */
    
    
    
    
    
    //    public ResultSet submitQuery(int sqlID) throws java.sql.SQLException,Exception
    //    {
    //
    //        String sqlStmnt = fetchStatement(sqlID);
    //
    //        //System.out.println(sqlStmnt);
    //        return (con.createStatement()).executeQuery(sqlStmnt);
    //
    //    }
    
    
    
    ////////////////////////////////////////////////////
    
    
    
    public ResultSet submitQuery(int sqlID,java.lang.Object[] parameters) throws java.sql.SQLException,Exception
    {
        logger.debug("submitQuery: " + sqlID);
        
        String sqlStmnt = fetchStatement(sqlID);
        
        logger.debug("Statement :"+sqlStmnt );
        
        try
        {
            sqlStmnt =QueryParametrizer.parametrize(sqlStmnt,parameters);
        }
        catch(Exception e)
        {
           logger.error(e);
            throw e;
        }
        logger.debug("Statement :"+sqlStmnt );
        return (con.createStatement()).executeQuery(sqlStmnt);
        
    }
    
    //////////////////////////////////////////////////////////////////////////////////
    
    public ResultSet submitQuery(Query q)throws java.sql.SQLException,Exception
    {
        logger.debug("submitQuery: " + q.getKey() + ", batch: " + q.isBatch());
        
        
        logger.debug("query object :: "+q);
        Collection tmp = q.getParameterList();
        
        
        Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);
        
        Sorter.quickSort(params);
        
        
        
        if(q.getQueryIdentifier().getName().equals(""))
            return submitQuery(q.getQueryIdentifier().getQueryId(),params);
        else
            return submitQuery(q.getQueryIdentifier().getName(),params);
        
    }
    
    
    
    public int submitUpdate(Query q)throws java.sql.SQLException,Exception
    {
        logger.debug("submitUpdate: " + q.getKey() + ", batch: " + q.isBatch());
        
        Collection tmp = q.getParameterList();
        Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);
        
        Sorter.quickSort(params);
        
        if(q.isBatch())
        {
            if(q.getQueryIdentifier().getName().equals(""))
                return submitUpdateBatch(q.getQueryIdentifier().getQueryId(),params);
            else
                return submitUpdateBatch(q.getQueryIdentifier().getName(),params);
        }
        else
        {
            if(q.getQueryIdentifier().getName().equals(""))
                return submitUpdate(q.getQueryIdentifier().getQueryId(),params);
            else
                return submitUpdate(q.getQueryIdentifier().getName(),params);
            
        }
    }
    
    ////////////////////////////////////////////
    
    
    /**
     * Setzt das descriptor zugeordnete Statement (Update|Insert|Delete) ab.<BR>
     *
     * @param	java.lang.String descriptor
     * @return  int
     * @exeption java.sql.SQLException
     */
    
    
    //    public int submitUpdate(String descriptor) throws java.sql.SQLException, Exception// returns abs(rows effected)
    //    {
    //
    //        String sqlStmnt = fetchStatement(descriptor);
    //
    //
    //        return (con.createStatement()).executeUpdate(sqlStmnt);
    //
    //
    //    }
    
    /////////////////////////////////////////////
    
    
    public int submitUpdate(String descriptor,java.lang.Object[] parameters) throws java.sql.SQLException,Exception// returns abs(rows effected)
    {
        logger.debug("submitUpdate: " + descriptor);
        
        String sqlStmnt = fetchStatement(descriptor);
        
        
        try
        {
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt,parameters);
        }catch(Exception e)
        {
           logger.error(e);
            throw e;
        }
        
        return (con.createStatement()).executeUpdate(sqlStmnt);
        
        
    }
    
    
    
    
    
    
    
    ///////////////////////////////////////////////////////////////////
    
    
    /**
     * Setzt das Statement mit der id sqlID (Update|Insert|Delete) ab.<BR>
     *
     * @param	int sqlID
     * @return  int rowsEffected
     * @exeption java.sql.SQLException
     */
    
    
    //    public int submitUpdate(int sqlID) throws java.sql.SQLException,Exception// returns abs(rows effected)
    //    {
    //
    //        String sqlStmnt = fetchStatement(sqlID);
    //
    //        return (con.createStatement()).executeUpdate(sqlStmnt);
    //
    //
    //    }
    
    
    ////////////////////////////////////////////////////////////
    
    
    public int submitUpdate(int sqlID,java.lang.Object[] parameters) throws java.sql.SQLException,Exception// returns abs(rows effected)
    {
       logger.debug("submitUpdate: " + sqlID);
        
        String sqlStmnt = fetchStatement(sqlID);
        
        try
        {
            sqlStmnt = QueryParametrizer.parametrize(sqlStmnt,parameters);
        }catch(Exception e)
        {
            logger.error(e);
            throw e;
        }
        
        return (con.createStatement()).executeUpdate(sqlStmnt);
        
        
    }
    
    
    /////////////////////////////////////////////////////////////////
    
    /**
     * Holt das descriptor zugeordnete Statement aus der Statement-Tabelle.<BR>
     *
     * @param	java.lang.String descriptor
     * @param	boolean effectsChanges
     * @return  java.lang.String sqlStatement
     * @exeption java.sql.SQLException
     */
    
    
    
    
    
    public String fetchStatement(String descriptor) throws java.sql.SQLException,Exception
    {
        
        logger.debug("fetchStatement: " + descriptor);
/*
if(!dbc.cacheStatements)
{
        char changes = 'F';
 
        if(effectsChanges)
        changes='T';
 
 
        String sqlStmnt = "SELECT statement from system_statement where id = " + cache.getStatement(descriptor).getID() +
                                   "and effect_Changes = '"+ changes +"' and number_of_param =" +parameterNum;
 
        ResultSet id = (con.createStatement()).executeQuery(sqlStmnt);
 
        id.next();
        return id.getString("Statement").trim();
}
 */
        
        
        if(cache.containsStatement(descriptor))
        {
            return cache.getStatement(descriptor).getStatement();
        }
        else
        {
            return null;
        }
        
        //if(tmp.effectsChanges() == effectsChanges && tmp.getNumberOfParameters() == parameterNum)
        
        
        //throw new Exception("SystemStatement stimmt nicht mit den Aufrufoptionen \u00FCberein");
        
    }
    
    ///////////////////////////////////////////////////////////
    
    
    /**
     * Holt das Statement mit der id sqlID aus der Statement-Tabelle.<BR>
     *
     * @param	int sqlID
     * @param	boolean effectsChanges
     * @return  java.lang.String sqlStatement
     * @exeption java.sql.SQLException
     */
    
    public  String fetchStatement(int sqlID) throws java.sql.SQLException,Exception
    
    {
      logger.debug("fetchStatement: " + sqlID);
/*
if(!dbc.cacheStatements)
{
        char changes = 'F';// Informix knows no boolean xxxxxxxxxxx
 
        if(effectsChanges)
        changes='T';
 
 
        String sqlStmnt = "SELECT statement from system_statement where (id = " + sqlID+ ") and (effect_Changes = '"+ changes +"') and (number_of_param =" +parameterNum+")";
 
        //System.out.println("Statement :" + sqlStmnt);
 
 
        ResultSet id = (con.createStatement()).executeQuery(sqlStmnt);
 
        id.next();
        return id.getString("Statement").trim();
 
}
 
 */
        if(cache.containsStatement(sqlID))
            return cache.getStatement(sqlID).getStatement();
        
        //if(tmp.effectsChanges() == effectsChanges && tmp.getNumberOfParameters() == parameterNum)
        //return tmp.getStatement();
        //
        //throw new Exception("SystemStatement stimmt nicht mit den Aufrufoptionen \u00FCberein");
        return null;
    }
    ////////////////////////////////////////////
    
    
    public StatementCache getStatementCache()
    {return cache;}
    
    
    public ResultSet executeQuery(Query q) throws java.sql.SQLException,Exception
    {
        logger.debug("executeQuery: " + q.getKey() + ", batch: " + q.isBatch());
        
        if(q.getStatement()==null) // sql aus dem cache
            return submitQuery(q);
        else
        {
            // nimm statement as is
            String sqlStmnt = q.getStatement();
            
            
            try
            {
                
                Collection tmp = q.getParameterList();
                Comparable[] params = (Comparable[])tmp.toArray(new Comparable[tmp.size()]);
                Sorter.quickSort(params);
                sqlStmnt =QueryParametrizer.parametrize(sqlStmnt,params);
                
                
            }
            catch(Exception e)
            {
               logger.error(e);
                throw e;
            }
            
           logger.debug("INFO executeQuery :: "+sqlStmnt );
            return (con.createStatement()).executeQuery(sqlStmnt);
            
            
            
            
        }
        
        
        
        
        
    }
    
    
    ///////////////////////
    
    public int submitUpdateBatch(int qid,java.lang.Object[] parameters)throws java.sql.SQLException,Exception
    {
        logger.debug("submitUpdateBatch: " + qid);
        
        String updateBatch = fetchStatement(qid);
        
        
        try
        {
            updateBatch = QueryParametrizer.parametrize(updateBatch,parameters);
        }catch(Exception e)
        {
           logger.error(e);
            throw e;
        }
        
        
        StringTokenizer tokenizer = new StringTokenizer(updateBatch,";");
        
        String[] updates = new String[tokenizer.countTokens()];
        
        for(int i=0;i<updates.length;i++)
            updates[i]=tokenizer.nextToken();
        
        int rowsEffected =0;
        
        for(int i=0;i<updates.length;i++)
            rowsEffected += (con.createStatement()).executeUpdate(updates[i]);
        
        return rowsEffected;
    }
    
    
    
    
    
    
    public int submitUpdateBatch(String queryname,java.lang.Object[] parameters)throws java.sql.SQLException,Exception
    {
       logger.debug("submitUpdateBatch: " + queryname);
        
        String updateBatch = fetchStatement(queryname);
        
        
        try
        {
            updateBatch = QueryParametrizer.parametrize(updateBatch,parameters);
        }catch(Exception e)
        {
            logger.error(e);
            throw e;
        }
        
        
        StringTokenizer tokenizer = new StringTokenizer(updateBatch,";");
        
        String[] updates = new String[tokenizer.countTokens()];
        
        for(int i=0;i<updates.length;i++)
            updates[i]=tokenizer.nextToken();
        
        int rowsEffected =0;
        
        for(int i=0;i<updates.length;i++)
            rowsEffected += (con.createStatement()).executeUpdate(updates[i]);
        
        return rowsEffected;
    }
}// end class DBCOnnection
