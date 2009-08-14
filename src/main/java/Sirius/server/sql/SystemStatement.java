/*
 * SystemStatement.java
 *
 * Created on 21. November 2003, 18:19
 */

package Sirius.server.sql;
import java.util.*;
import Sirius.server.search.searchparameter.*;
import Sirius.util.collections.MultiMap;
/**
 *
 * @author  schlob
 */
public class SystemStatement
{
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    private boolean root;
    private int id;
    private String name;
    private boolean isUpdate;
    private boolean isBatch;
    private boolean isUnion;
    
    /**parameters for this level of the query*/
    protected MultiMap parameters;
    
    //
    private String statement;
    
    private int result;
    
    private String description;
    
    private boolean conjunction;
    
    private boolean search;
    
    //-----------------------------------------------------------------------------------
    
    public SystemStatement(boolean root,int id,String name,boolean isUpdate,int result,String statement)
    {
        this.root=root;
        this.id = id;
        this.name = name;
        this.isUpdate = isUpdate;
        this.statement = statement;
        this.result=result;
        this.parameters= new MultiMap();
        this.isBatch=false;
        this.conjunction=false;
        
        
    }
    
    //-----------------------------------------------------------------------------------
    
    public SystemStatement(boolean root,int id,String name,boolean isUpdate,boolean isBatch,int result,String statement,String description)
    {
        this(root,id,name,isUpdate,result,statement);
        this.description=description;
        this.isBatch=isBatch;
        
        
        
    }
    
    //-------------------------------------------------------------------------------------
    
    
    public int getID()
    {return id;}
    
    public String getName()
    {return name;}
    
    public int getResultType()
    {return result;}
    
    public MultiMap getParameters()
    {return parameters;}
    
 
    
    public String getStatement()
    {return statement;}
    
    //public boolean toBePrepared(){return toBePrepared;}
    
    
    public void addParameter(SearchParameter p)
    {
        parameters.put(p.getKey(), p);
    }
    
    /** Getter for property root.
     * @return Value of property root.
     *
     */
    public boolean isRoot()
    {
        return root;
    }
    
    /** Setter for property root.
     * @param root New value of property root.
     *
     */
    public void setRoot(boolean root)
    {
        this.root = root;
    }
    
    /** Getter for property isUpdate.
     * @return Value of property isUpdate.
     *
     */
    public boolean isUpdate()
    {
        return isUpdate;
    }
    
    /** Setter for property isUpdate.
     * @param isUpdate New value of property isUpdate.
     *
     */
    public void setIsUpdate(boolean isUpdate)
    {
        this.isUpdate = isUpdate;
    }
    
    public String getDescription()
    {return description;}
    
    /**
     * Getter for property isBatch.
     * @return Value of property isBatch.
     */
    public boolean isBatch()
    {
        return isBatch;
    }
    
    /**
     * Setter for property isBatch.
     * @param isBatch New value of property isBatch.
     */
    public void setIsBatch(boolean isBatch)
    {
        this.isBatch = isBatch;
    }
    
    /**
     * Getter for property isUnion.
     * @return Value of property isUnion.
     */
    public boolean isUnion()
    {
        return isUnion;
    }
    
    /**
     * Setter for property isUnion.
     * @param isUnion New value of property isUnion.
     */
    public void setUnion(boolean isUnion)
    {
        this.isUnion = isUnion;
    }
    
    public boolean isConjunction()
    {
        //logger.debug("isConj gerufen result ="+conjunction);
        return conjunction;
    }
    
    public void setConjunction(boolean conjunction)
    {
       // logger.debug("vor dem setzen setConj gerufen neu ="+conjunction  );
        this.conjunction = conjunction;
        //logger.debug(" nach dem setzensetConj gerufen neu ="+conjunction );
    }

    public boolean isSearch()
    {
        return search;
    }

    public void setSearch(boolean search)
    {
        this.search = search;
    }
    
}// end of class SystemStatement
