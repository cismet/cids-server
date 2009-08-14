package Sirius.server.newuser.permission;
import Sirius.util.*;

public class PermissionHolderMap extends java.util.Hashtable
{
	public PermissionHolderMap(int size, float loadFactor){super(size,loadFactor);}
        
        public PermissionHolderMap(int size){super(size);}

	public PermissionHolder getPermissionHolder(String key)
	{
		return (PermissionHolder)get(key);

	}

	public void add(String key , PermissionHolder ph)
	{
		put(key,ph);
	}
        
        
        public void add(Mapable m)
        {
                put(m.getKey(), m);
        
        
        }

	public void rehash(){super.rehash();}




}