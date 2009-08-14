
package Sirius.server.search.store;

import java.io.*;


import Sirius.server.newuser.*;


/** Repraesentiert ein Such-Profil **/
public class QueryData extends QueryInfo implements java.io.Serializable, Info
{
	
	
	/** Suchergebnisdaten **/
	protected byte[] data;

	//-----------------------------------------------------------------
        
        public QueryData(int id, String domain, String name, byte[] data)
	{
            
            super(id, name, domain, "");
            this.data = data;
	}
        
  /**
		@param id QueryId
		@param localServerName HeimatLocalServer
		@param name Name des Suchergebisses
		@param data Daten des Suchergebnisses
		@param isUserQuery handelt es sich um eine Suche von einem User oder UserGroup **/
	public QueryData(int id, String domain, String name, String fileName, byte[] data)
	{
            this(domain,name,fileName,data);
            this.id = id;
	
	}

	//-----------------------------------------------------------------
	/**
		@param localServerName HeimatLocalServer
		@param name Name der Suche
		@param data Daten der Suche
		@param isUserQuery handelt es sich um eine Suche von einem User oder UserGroup **/
	public QueryData(String domain, String name, String fileName, byte[] data)
		{
		 super(-1,name,domain,fileName);
		 this.data = data;
		 
                }
	//-----------------------------------------------------------------
	/** erzeugt leeres QueryObject mit der Id -1**/
	public QueryData()
	{
            this(-1, "", "", "",  new byte[0]);
	 
	}

	//--------------------------------------------------------------------
	
        /**@return QueryDaten **/
	public final byte[] getData() { return data; }

	/** ueberlaedt toString()-Methode von java.lang.Object**/
	public String toString()
	{
	 return "id:"+id+" lsName: "+domain+" name:"+name+" length: "+data.length;
	}

	
	/** @return true wenn id > 0, sonst false**/
	public boolean idIsValid(){ return id >= 0;}

}

