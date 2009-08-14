/*
 * StatementMap.java
 *
 * Created on 22. November 2003, 09:56
 */

package Sirius.server.sql;
import java.util.*;
/**
 *
 * @author  schlob
 */
public class StatementMap extends Hashtable
{

//constructor/////////////////////

//constructor
public StatementMap(int capacity)
{super(capacity);}


///////////////////////////////////////

public void add(int key,SystemStatement value)throws Exception
{
Integer Key = new Integer(key);
super.put(Key,value);

if(!super.containsKey(Key))
throw new java.lang.Exception("Couldn't add SystemStatement ID:" +key);
}// end add


/////////////////////////////////////////

public SystemStatement getStatement(int key) throws Exception
{
Integer Key = new Integer(key); //map accepts objects only
if(super.containsKey(Key))
{
	java.lang.Object candidate = super.get(Key);

		 if (candidate instanceof SystemStatement)
		 return (SystemStatement) candidate;

	throw new java.lang.NullPointerException("Entry is not a SystemStatement ID:"+ key);
}// endif

throw new java.lang.NullPointerException("No entry ID :" + key); // to be changed in further versions when exception concept is accomplished
}// end getStatemnt



/////// containsIntKey/////////////////////////////////


public boolean containsIntKey(int key)
{return super.containsKey(new Integer(key));}

public void rehash(){super.rehash();}


}// end of class StatementMap

