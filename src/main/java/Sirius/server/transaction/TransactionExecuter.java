/*
 * TransactionExecuter.java
 *
 * Created on 10. August 2004, 11:35
 */

package Sirius.server.transaction;
import java.util.*;
import java.lang.reflect.*;

/**
 *
 * @author  schlob
 */
public class TransactionExecuter
{
     private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    
    protected Object o;
    
    protected HashMap methods;
    
    /** Creates a new instance of TransactionExecuter */
    public TransactionExecuter(Object o)
    {
        this.o=o;
        
        Method[] ms = o.getClass().getMethods();
        
        methods = new HashMap(ms.length);
        
        for(int i =0;i<ms.length;i++)
        {
            String methodName =  ms[i].getName();
            
           logger.debug("methodname registered  "+methodName);
            methods.put(methodName, ms[i]);
        }
        
        
        
        
        
    }
    
    
    
    public int execute(ArrayList transactions)
    {
        int successfull = 0;
        
        Iterator iter = transactions.iterator();
        
        while(iter.hasNext())
        {
            
            Transaction t = (Transaction)iter.next();
            
            if(methods.containsKey(t.getName()))
            {
                Method m = (Method)methods.get(t.getName());
                
                try
                {
                    m.invoke(o,t.getParams());
                    successfull++;
                }
                catch(Exception e)
                {logger.error("failed to execute "+t.getName(),e);}
                
                
            }
            else
            {
                logger.error("failed to execute "+t.getName()+" as method doesn't exist here");
                
            }
            
            
        }
        
        return successfull;
    }
    
}
