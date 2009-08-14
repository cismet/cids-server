/*
 * ObjectHierarchy.java
 *
 * Created on 25. Mai 2004, 11:48
 */

package Sirius.server.localserver.object;
import java.sql.*;
import Sirius.server.sql.*;
import java.util.*;
import de.cismet.tools.collections.*;
/**
 * Generiert die Objekthierarchie f\u00FCr alle !!!!indizierten!!!! Strukturen
 * @author  schlob
 */
public class ObjectHierarchy
{
     private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
     
    // getFatherstmnt hierarchy
    protected MultiMap fatherStmnts = new MultiMap();
    
    protected MultiMap arrayFatherStmnts = new MultiMap();
    
    // liefert classId referenziert von classIds
    protected MultiMap classIdHierarchy = new MultiMap();
    
    protected DBConnectionPool conPool;
    
    
    
    /** Creates a new instance of ObjectHierarchy */
    public ObjectHierarchy(DBConnectionPool conPool) throws Exception
    {
        this.conPool=conPool;
        Connection con = conPool.getConnection().getConnection();
        
        Statement stmnt = con.createStatement();
        // schl\u00FCssel , vatertabellen name , Atrributname
        String initLookupTable = "select  a.foreign_key_references_to as child,a.class_id as father,c.primary_key_field as pk,c.table_name,a.field_name  from cs_attr a,cs_class  c where a.foreign_key ='T' and  a.class_id = c.id   and a.indexed=true ";
        //and a.indexed=true
        // and ( a.isarray is null or a.isarray = 'F') rausgenommen
        ResultSet rs = stmnt.executeQuery(initLookupTable);
        
        
        
        
        while(rs.next())
        {
            Integer key = new Integer(rs.getInt("child"));
            
            String pk = rs.getString("pk");
            
            int father = rs.getInt("father");
            
            
            
            
            //konstruiere select string f\u00FCr Vaterobjekt mit Auswahlkriterium = Objektid des Attributes
                  
            
            
            String value = "Select "+father+" as class_id ,"+pk + " as object_id" +" from "+rs.getString("table_name")+ " where "+ rs.getString("field_name") +" = ";
            
            
            logger.debug(" get Father key :: "+key + " value :: "+value);
            fatherStmnts.put(key,value);
            
            classIdHierarchy.put(new Integer(father),key);
            
            
        }
        
        
        
        // init array stmns notwendig da array merkmal nicht der primary key ist
        
        
        String initArrayLookupTable = "select cf.primary_key_field as father_pk,cc.primary_key_field as child_pk,a.array_key, a.foreign_key_references_to as child,a.class_id as father,cf.table_name as father_table, cc.table_name as child_table,a.field_name as attribute  from cs_attr a,cs_class  cf, cs_class cc where a.foreign_key ='T' and  a.class_id = cf.id and isarray ='T' and a.foreign_key_references_to =cc.id";
        
        rs = stmnt.executeQuery(initArrayLookupTable);
        
        while(rs.next())
        {
            
            String arrayKey = rs.getString("array_key");
            Integer key = new Integer(rs.getInt("child"));
            String father_pk = rs.getString("father_pk");
            int father = rs.getInt("father");
            String attribute = rs.getString("attribute");
            String child_table = rs.getString("child_table");
            String father_table = rs.getString("father_table");
            String child_pk = rs.getString("child_pk");
            
            String value = "Select "+father+" as class_id ,"+father_pk + " as object_id" +" from "+father_table+ " where "+ attribute +" in "
            +" (select "+ arrayKey+ " from " + child_table + " where  "+ child_pk+ " = ";  //? )
            
            logger.debug(" get Array Father key :: "+key + " value :: "+value);
            
            arrayFatherStmnts.put(key,value);
            classIdHierarchy.put(new Integer(father),key);
        }
        
    }
    
    
    
    
    public boolean classIsReferenced(int classId)
    {
        return fatherStmnts.containsKey(new Integer(classId) )|| arrayFatherStmnts.containsKey(new Integer(classId) ) ;
        
    }
    
    
    public boolean classIsArrayType(int classId)
    {
        
        return arrayFatherStmnts.containsKey(new Integer(classId) );
    }
    
    // delivers a collection of statements delivering class_id,object_id of a fatherobject
    public  Collection getFatherStatements(int classId,int objectId)
    {
        
        ArrayList result = new ArrayList();
        
        if( fatherStmnts.containsKey(new Integer(classId) ) )
        {
            //Liste
            Collection  statements = (LinkedList) fatherStmnts.get( new Integer(classId) );
            
            if(statements==null )
                return result;
            
            Iterator iter = statements.iterator();
            
            result = new ArrayList(statements.size());
            while(iter.hasNext())
            {
                result.add( iter.next().toString()+objectId);
                
            }
        }
        
        return result;
        
    }
    
    
    
    public  Collection getChildClassIds(int parentClassId)
    {
        // als Arraylist zur\u00FCckgeben
        ArrayList result = null;
        
        if( classIdHierarchy.containsKey(new Integer(parentClassId) ) )
        {
            //Liste
            Collection  ids = (LinkedList)classIdHierarchy.get( new Integer(parentClassId) );
            
          
            
            
            Iterator iter = ids.iterator();
            
            result = new ArrayList(ids.size());
            
            while(iter.hasNext())
            {
                Integer child = (Integer)iter.next();
                result.add(child );
                
                // recursion
                Collection childs =getChildClassIds(child.intValue());
                
                if(childs!=null)
                    result.addAll(childs);
                
                
            }
        }
        
        return result;
        
    }
    
    
    
    public Integer[] getExtendedClassList(int[] classIds)
    {
        // gesch\u00E4tzte kapazit\u00E4t
        java.util.ArrayList result = new java.util.ArrayList(classIds.length*2);
        
        for(int i =0;i<classIds.length;i++)
        {
            // add classid itself
            
            result.add(new Integer(classIds[i]));
            
            Collection c = this.getChildClassIds(classIds[i]);
            
            if(c!=null)
                
            {
                Iterator iter = c.iterator();
                
                while(iter.hasNext())
                {
                    result.add( iter.next());
                    
                }
            }
            
        }
        
        
        
        return ( Integer[] )result.toArray(new Integer[result.size()]);
        
    }
    
    
    public  Collection getArrayFatherStatements(int classId,int objectId)
    {
        
        ArrayList result = new ArrayList();
        
        if(  arrayFatherStmnts.containsKey(new Integer(classId) ) )
        {
            //Liste
            Collection  statements = (LinkedList) arrayFatherStmnts.get( new Integer(classId) );
            
            if(statements==null )
                return result;
            
            Iterator iter = statements.iterator();
            
            result = new ArrayList(statements.size());
            while(iter.hasNext())
            {
                result.add( iter.next().toString()+objectId+")");
                
            }
        }
        
        return result;
        
    }
    
    
}
