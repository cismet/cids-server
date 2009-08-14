/*
 * TransactionExecTest.java
 *
 * Created on 10. August 2004, 14:01
 */

package Sirius.server.transaction;
import java.util.*;
/**
 *
 * @author  schlob
 */
public class TransactionExecTest
{
    
    /** Creates a new instance of TransactionExecTest */
    public TransactionExecTest()
    {
    }
    
    public static void main(String[] args)
    {
            t test = new t();
            
            String mama ="printMama";
            String param = "luv ya ";
            
            Object[] params = new Object[1];
            
            params[0]=param;
            
            
            TransactionExecuter tex = new TransactionExecuter(test);
            
            Transaction x = new Transaction (mama,params);
            
            ArrayList l = new ArrayList();
            
            l.add(x);
            
            
            //----------
            
            tex.execute(l);
            
            //------------
            
            
            
            
            
            
    
    }
    
}

class t
{

    t()
    {}
    
   public void printMama(String param)
    {System.out.println(param+" MAMA");}

}