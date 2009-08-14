/*
 * Transasction.java
 *
 * Created on 10. August 2004, 10:41
 */

package Sirius.server.transaction;

/**
 *
 * @author  schlob
 */
public class Transaction implements java.io.Serializable,Sirius.util.Mapable
{
    //**name (identifier) der auszuf\u00FChrenden Transaktion -> Methode/
    protected String name;
    
    //** parameter Liste der Methode*/
    protected Object[] params;
    
    /** Creates a new instance of Transasction */
    public Transaction()
    {
        name = "";
        params = new Object[0];
    }
    
    public Transaction(String name,Object[] params)
    {
        this.name=name;
        this.params = params;
    }
    
    /**
     * Getter for property name.
     * @return Value of property name.
     */
    public java.lang.String getName()
    {
        return name;
    }    
    
    /**
     * Setter for property name.
     * @param name New value of property name.
     */
    public void setName(java.lang.String name)
    {
        this.name = name;
    }    
    
    /**
     * Getter for property params.
     * @return Value of property params.
     */
    public java.lang.Object[] getParams()
    {
        return this.params;
    }    
    
    /**
     * Setter for property params.
     * @param params New value of property params.
     */
    public void setParams(java.lang.Object[] params)
    {
        this.params = params;
    }    
    
    public Object constructKey(Sirius.util.Mapable m)
    {
        
        return m.getKey();
        
    }
    
    public Object getKey()
    {
        
        return name;
    }
    
}
