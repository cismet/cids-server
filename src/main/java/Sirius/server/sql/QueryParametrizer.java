package Sirius.server.sql;

import java.sql.*;



public class QueryParametrizer
{
     private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(QueryParametrizer.class);
    
    

	/**
			  * Hauptfunktionalit\u00E4t der Klasse Parametrizer siehe Klassenbeschreibung <BR>
			  *
			  * @param   java.lang.String statement
			  * @param   java.lang.Object[] parameters
			  * @return
			  * @exeption java.lang.Exception
		  */


	public static final String parametrize(String statement, java.lang.Object[] parameters) throws java.lang.Exception
	{
                        if(statement!=null)
			statement = statement.trim();
                        else 
                           logger.error("Stmnt in parametrizer NUll");
                        
                        // nothing to parametrize
                        if(parameters.length==0)
                            return statement;
                        
                        String parametrizedStmnt = "";

			final char chr = '?';
			
                        int start =0;
                        int hit=statement.indexOf(chr);
                        int i =0;
			
                        
                        while(hit != -1 && i < parameters.length)
                        {
                
                             parametrizedStmnt += statement.substring(start,hit);//statement bis zum ersten parameter

                             parametrizedStmnt += parameters[i++].toString();

                             start=hit+1;

                             hit =  statement.indexOf(chr,start);

                            // logger.debug( parametrizedStmnt + " hit :"+hit+" start :"+start);
                        }
			
                        parametrizedStmnt+=statement.substring(start,statement.length());// rest after last '?'
                        
                       logger.debug("INFO Stment :  "+parametrizedStmnt);
		
	return parametrizedStmnt;



	}// end parametrize()

//---------------------------------------


/*


public static void main(String[] args) throws Exception
{
    Object[] param = {"altlasten","5"};
   System.out.println( parametrize("select * from ? where id = ? and emil = 34",param ) );

}
*/


}// end class
