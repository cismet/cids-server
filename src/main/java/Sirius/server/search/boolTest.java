/*
 * boolTest.java
 *
 * Created on 9. August 2006, 14:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Sirius.server.search;

/**
 *
 * @author schlob
 */
public class boolTest
{
    private boolean c = false;
    /** Creates a new instance of boolTest */
    public boolTest()
    {
    }
    
    public void setC(boolean c)
    {this.c=c;}
    
    public boolean getC()
    {return c;}
    
    public static void main(String[] args)
    {
        
        boolTest b = new boolTest();
        
        System.out.println(b.getC());
       boolean a = true;
        b.setC(a);
          System.out.println(b.getC());
        
        
        
    }
       
    
}
