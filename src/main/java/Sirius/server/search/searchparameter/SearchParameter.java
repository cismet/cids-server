package Sirius.server.search.searchparameter;

import java.util.*;

public interface SearchParameter
{
    public static String CLASSIDS = "cs_classids";

	public Object getKey();
        
        public Object getValue();
        
        public String getDescription();
        
        public Collection values() throws Exception;
        
        public void setValue(Object parameter);
        
        public boolean isQueryResult();
        
        public int getQueryPosition();
        
       

}