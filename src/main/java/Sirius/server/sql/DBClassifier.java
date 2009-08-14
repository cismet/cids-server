package Sirius.server.sql;

import java.util.*;



public class DBClassifier //implements java.lang.Comparable,Sirius.server.property.Createable
{
    
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    protected int noOfConnections;// indcluding this one
    
    protected String url;
    
    protected String login;
    
    protected String pwd;
    
    protected String driver;
    
    protected String sqlDialect;
    
   
    
    //////////////////////////////////////////////////////////////////////
    
    
    public DBClassifier()
    {
        noOfConnections=0;
        url = "";
        login="";
        pwd="";
        driver="org.postgresql.Driver";
        setSqlDialect("org.hibernate.dialect.PostgreSQLDialect");
       
        
    }
    
     public DBClassifier(String url,String login,String pwd,String driver,int noOfConnections, String sqlDialect)
    {
        this(url,login,pwd,driver,noOfConnections);
        this.sqlDialect=sqlDialect;
       
    
    }
    
    //////////////////////////////////////////////////////////////////////
    
    public DBClassifier(String url,String login,String pwd,String driver,int noOfConnections)
    {
        this(url,login,pwd,driver);
        this.noOfConnections=noOfConnections;
       
    
    }
    
    //////////////////////////////////////////////////////////////////////
    
    public DBClassifier(String url,String login,String pwd,String driver)
    {
        this();
        this.url=url;
        this.login=login;
        this.pwd=pwd;
        this.driver = driver;
        
        
        
    }
    
    
    ///////////////////////////////////////////////////////////////////////
//    
//    public boolean equals(DBClassifier dbc)
//    {
//        if(dbc==this)
//            return true;
//        
//        else if(url.equals(dbc.url)&&login.equals(dbc.login)&&pwd.equals(dbc.pwd)&&driver.equals(dbc.driver))
//            return true;
//        
//        else
//            return false;
//        
//    }
//  
    
    /////////////////////////////////////////////////////////////////////////
    
    public String toString()
    {
        
        return url+"|"+login+"|"+pwd+"|"+driver+"|"+noOfConnections;
        
    }
    
    
//    /////////////////////////////////////////////////////////////////////////
//    
//    public java.lang.Object createObject(String constructorArgs,String delimiter)
//    {
//        String[] args = DBClassifier.tokenizeString(constructorArgs,delimiter);
//        
//        if(args.length == 7)
//        {
//            
//            return new DBClassifier(args[0],args[1],args[2],args[3],args[4],new Boolean(args[5]).booleanValue(),new Integer(args[6]).intValue());
//        }
//        else
//        {
//            logger.error("<LS> ERROR :: creatObject falsche Anzahl ConstructorParameter");
//            return null;
//        }
//        
//        
//    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    
    private static final String[] tokenizeString(String s,String delimiter)
    {
        
        StringTokenizer tokenizer = new StringTokenizer(s,delimiter);
        String[] stringArray = new String[tokenizer.countTokens()];
        int i=0;
        
        while (tokenizer.hasMoreTokens())
        {
            stringArray[i++]=tokenizer.nextToken();
        }
        
        return stringArray;
        
        
        
    }
    
    
    ///////////////////////////////////////////////////////////////////////////
    
    protected void setNoOfConnections(int n)
    {
        
        noOfConnections =n;
        
        
    }
    
    /**
     * Getter for property driver.
     * @return Value of property driver.
     */
    public java.lang.String getDriver()
    {
        return driver;
    }
    
    /**
     * Setter for property driver.
     * @param driver New value of property driver.
     */
    public void setDriver(java.lang.String driver)
    {
        this.driver = driver;
    }
    
   
    
    /**
     * Getter for property login.
     * @return Value of property login.
     */
    public java.lang.String getLogin()
    {
        return login;
    }
    
    /**
     * Setter for property login.
     * @param login New value of property login.
     */
    public void setLogin(java.lang.String login)
    {
        this.login = login;
    }
    
    /**
     * Getter for property pwd.
     * @return Value of property pwd.
     */
    public java.lang.String getPwd()
    {
        return pwd;
    }
    
    /**
     * Setter for property pwd.
     * @param pwd New value of property pwd.
     */
    public void setPwd(java.lang.String pwd)
    {
        this.pwd = pwd;
    }
    
    /**
     * Getter for property url.
     * @return Value of property url.
     */
    public java.lang.String getUrl()
    {
        return url;
    }
    
    /**
     * Setter for property url.
     * @param url New value of property url.
     */
    public void setUrl(java.lang.String url)
    {
        this.url = url;
    }
    
    // end class

    public String getSqlDialect()
    {
        return sqlDialect;
    }

    public void setSqlDialect(String sqlDialect)
    {
        this.sqlDialect = sqlDialect;
    }
}
