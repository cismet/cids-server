/*
 * StatementCache.java
 *
 * Created on 22. November 2003, 09:54
 */

package Sirius.server.sql;
import de.cismet.tools.collections.*;
import java.sql.*;
import java.util.*;

/**
 *
 * @author  schlob
 */
public class StatementCache
{
    
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    /** contains all cached objects */
    protected StatementMap statements;// holds Statements referenced by their IDs
    protected HashMap<java.lang.String,java.lang.Integer> nameAssociatesID;// holds ids of statements referenced by statement names
    
    //-----------------------------------
    
    StatementCache(Connection con)
    {
        
        
        
        statements = new StatementMap(50);  // allocation of the hashtable
        nameAssociatesID = new HashMap();
        
        logger.debug("Vor Queries Laden");
        try
        {
            
            
            String queriesThere = "select count(*) from cs_query";
            
            String queryStmnt = "SELECT * from cs_query";//"SELECT * from cs_query q,cs_java_class c where result = c.id";
            
            String paramStmnt = "SELECT * from cs_query_parameter";
            
            
            
            ResultSet queryTest = (con.createStatement()).executeQuery(queriesThere);
            int queryNo =0;
            
            if(queryTest.next())
                queryNo=queryTest.getInt(1);
            
            if(queryNo==0)
            {
                
                logger.error("<LS> ERROR :: keine Systemstatemnts in cs_query vorhanden ");
                throw new Exception("<LS> ERROR :: keine Systemstatemnts in cs_query vorhanden ");
            }
            
            
            ResultSet stmntTable = (con.createStatement()).executeQuery(queryStmnt);
            
            while(stmntTable.next())//add all objects to the hashtable
            {
                SystemStatement tmp = new SystemStatement(stmntTable.getBoolean("is_root"),stmntTable.getInt("id"),stmntTable.getString("name").trim(),stmntTable.getBoolean("is_update"),stmntTable.getBoolean("is_batch"),stmntTable.getInt("result"),stmntTable.getString("statement").trim(),stmntTable.getString("descr"));
                boolean conjunction=false;
                tmp.setUnion(stmntTable.getBoolean("is_union"));
                try
                {
                  //  logger.debug("conjunction vom Typ "+stmntTable.getObject("conjunction").getClass());
                   
                    conjunction  = stmntTable.getBoolean("conjunction"); // getBoolean buggy??
                   
                    logger.debug("conjunction vor dem setzen" +conjunction);
                    tmp.setConjunction(conjunction);
                }
                catch (SQLException ex)
                {
                    logger.error("is_conjunction not supported! Please update your  query schema!!",ex);
                    tmp.setConjunction(false);// standardverhalten
                }
                try
                {
                    
                    tmp.setSearch(stmntTable.getBoolean("is_search"));
                    
                } catch (SQLException ex)
                {
                    logger.error("is_search nicht vorhanden -> update der metadatenbank",ex);
                }
               
                statements.add(tmp.getID(),tmp);
                nameAssociatesID.put(tmp.getName(),tmp.getID());
                
                logger.debug("cached statement :"+tmp.getName()+" changes ?"+tmp.getStatement() + " conjuction ??"+tmp.isConjunction() + "conjunctionresult" + conjunction);
                
            }// end while
            
            logger.debug("statement hash elements #"+statements.size() +" elements"+statements);
            
            
            ResultSet paramTable = (con.createStatement()).executeQuery(paramStmnt);
            
            int query_id =0;
            
            while(paramTable.next())
            {
                SystemStatement s=null;
                query_id = paramTable.getInt("query_id");
                s = statements.getStatement(query_id);
                //xxx new Searchparameter
    
            }
            
            logger.debug("Queries aus Datenbank geladen");
     
        }
        
        catch (java.lang.Exception e)
        {
            logger.error("Exception beim Query laden" ,e);
            ExceptionHandler.handle(e);
            
        }
        
        
        
    }// end of constructor
    
    
    
    //-----------------------------------------------------------------------------
    
    
    
    protected SystemStatement getStatement(int id) throws Exception
    {return statements.getStatement(id);}
    
    
    public SystemStatement getStatement(String name) throws Exception
    {return statements.getStatement(nameAssociatesID.get(name));}
    
    
    public Collection values()
    {return statements.values();}
    
    
    public int size()
    {return statements.size();}
    
    
    public boolean containsStatement(int id)
    {
        return statements.containsIntKey(id);
        
    }
    
    public boolean containsStatement(String key)
    {
        return nameAssociatesID.containsKey(key);
        
    }
    
    
  
    
    
}// end of class statement cache