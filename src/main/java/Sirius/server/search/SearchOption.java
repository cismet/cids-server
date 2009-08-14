package Sirius.server.search;

import java.util.*;
import java.sql.*;
import Sirius.server.sql.*;
import Sirius.server.newuser.*;
import Sirius.server.middleware.types.*;
import Sirius.server.search.searchparameter.*;



/**
 * Short concise description.
 * Additional verbose description.
 * @param descriptor description.
 * @param value description.
 * @param searchMask description.
 * @return description.
 * @exception Exception description.
 * @see package.class
 */
public class SearchOption implements java.io.Serializable {
    protected HashSet classes;
    
    protected HashSet userGroups;
    
    // Dieser Suchoption zugeordnete Query
    protected Query query;
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    
    /**
     * Short concise description.
     * Additional verbose description.
     * @param descriptor description.
     * @param value description.
     * @param searchMask description.
     * @return description.
     * @exception Exception description.
     * @see package.class
     */
    public SearchOption(Query query) {
        this(query,new HashSet(),new HashSet());
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    public SearchOption(Query query,HashSet classes,HashSet userGroups) {
        
        this.query = query;
        this.classes = classes;
        this.userGroups = userGroups;
        
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    public final boolean isSelectable(String classKey, String userGroupKey) {
        
       if(classes.size()>0&&userGroups.size()>0)
            return classes.contains(classKey)&&userGroups.contains(userGroupKey);
        else
            return true;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    public final boolean isSelectable(Collection c,Collection ugs) {
        if(classes.size()>0&&userGroups.size()>0)
           return classes.containsAll(c)&&userGroups.containsAll(ugs);
        else
            return true;
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    public final boolean isSelectable(UserGroup ug) {
        if(userGroups.size()>0)
            return userGroups.contains(ug.getKey());
        else
            return true;
        
    }
    
    
    public final boolean isSelectable(MetaClass c) {
       if(classes.size()>0)
           return classes.contains(c.getKey());
       else
            return true;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    public final Query getQuery(){return query;}
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    public final String getQueryId(){return this.getQuery().getQueryIdentifier().getKey().toString();}
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    public final void addUserGroup(String userGroupKey) {
        userGroups.add(userGroupKey);
        
    }
    
    //////////////////////////////////////////////////////////////////////////////////////////
    
    public final void addClass(String classKey) {
        classes.add(classKey);
        
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    
    
    public void setSearchParameter(SearchParameter parameter) throws Exception {
        this.query.setParameter(parameter);
    }
    
    public void setDefaultSearchParameter(Object key, Object value) throws Exception {
        this.query.setParameter(new DefaultSearchParameter(key, value, false));
    }
    
    public Iterator getParameterNames() {
        return this.query.getParameterKeys();
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////
    
    
    
}
