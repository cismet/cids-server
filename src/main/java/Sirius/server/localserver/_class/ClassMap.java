package Sirius.server.localserver._class;


import java.util.*;

public class ClassMap extends java.util.Hashtable
{
    //constructor
    public ClassMap()
    {super();}
    
    //constructor
    public ClassMap(int capacity, float factor)
    {super(capacity,factor);}
    
    
    //constructor
    public ClassMap(int capacity)
    {super(capacity);}
    
    
    
    ////////////////////////////////////////////////////
    
    public void add(int key,Class value)throws Exception
    {
        Integer Key = new Integer(key);
        super.put(Key,value);
        if(!super.containsKey(Key))
            throw new java.lang.Exception("Couldn't add class ID:" +key);
    }// end add
    
    
    ///////////////////////////////////////////////////////////////
    
    public Class getClass(int key) throws Exception
    {
        Integer Key = new Integer(key); //map accepts objects only
        if(super.containsKey(Key))
        {
            java.lang.Object candidate = super.get(Key);
            
            //if (candidate instanceof Class)
            return (Class) candidate;
            
        }
        else
            return null;
        //throw new java.lang.NullPointerException("Entry is not a Class ID :" +key);
       // }// endif
    
    //throw new java.lang.NullPointerException("No entry ClassID :"+key); // to be changed in further versions when exception concept is accomplished
    }// end getClass



/////// containsIntKey/////////////////////////////////

public boolean containsIntKey(int key)
{return super.containsKey(new Integer(key));}


public void rehash()
{super.rehash();}


public Vector getAll()
{return new Vector(this.values());}


}// end of class ClassMap
