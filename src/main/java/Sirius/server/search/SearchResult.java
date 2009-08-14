/*
 * SearchResult.java
 *
 * Created on 19. November 2003, 14:46
 */

package Sirius.server.search;
import Sirius.server.middleware.types.*;
import Sirius.server.middleware.types.Node;
import Sirius.server.search.searchparameter.*;
import java.util.*;
/**
 *
 * @author  schlob
 */
public class SearchResult implements java.io.Serializable
{
    private final static transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SearchResult.class);
    
    //vor\u00FCbergehend
    public static int NODE = 1;
    public static int COLLECTION =2;
    public static int VALUE = 3;
    public static int OBJECT = 4;
    
    public static int MAX_HITS = 500;
    
    protected HashSet filter = new HashSet();
    protected boolean filterSet=false;
    
    
    protected Object data;
    
    private HashSet retainer;
    
    
    
    public SearchResult(MetaObjectNode[] nodes)
    {
        
        this.data=new HashSet();
        
        
        
        for (int i=0;i<nodes.length;i++)
            ((HashSet) this.data).add(nodes[i]);
        
        
        
    }
    
    public SearchResult(SearchResult srs)
    {
        
        this.data=srs.data;
        
    }
    
    public SearchResult(MetaObject[] data)
    {
        
        this.data=data;
    }
    
    public SearchResult(Object data)
    {
        
        this.data=data;
    }
    
    public boolean isObject()
    {
        return data instanceof MetaObject[];
    }
    
    public boolean isNode()
    {
        return data instanceof MetaObjectNode[] ||  data instanceof HashSet;
    }
    
    
    public boolean isSearchParameter()
    {
        
        return ! (isNode() || isObject());
        
    }
    
    
    public  MetaObjectNode[] getNodes() throws Exception
    {
        
        if(isNode())
        {
            // System.out.println("nodes: " + ((Collection)this.data).size());
            return (MetaObjectNode[])((Collection)this.data).toArray( new MetaObjectNode[((Collection)this.data).size()]);
        }
        else
            throw new Exception("SearchResult.data no Node[]");
    }
    
    
    public MetaObject[] getObjects() throws Exception
    {
        
        if(isObject())
            return (MetaObject[])data;
        else
            throw new Exception("SearchResult.data no MetaObject[]");
    }
    
    public Object getSearchParameter() throws Exception
            
    {
        if(isSearchParameter())
            return data;
        else
            throw new Exception("SearchResult.data no SearchParameter");
        
        
    }
    
    
    public java.lang.Object getResult()
    {
        return data;
    }
    
    
    
    public void addAll(Sirius.server.middleware.types.MetaObjectNode[] nodes) throws Exception
    {
        logger.debug("addAll nodes gerufen");
        
        HashSet result = new HashSet(nodes.length);
        
        // f\u00FCge nodes Hashset hinzu
        for(int i =0;i<nodes.length;i++)
            result.add(nodes[i]);
       
          logger.debug("neu dazu:"+result + " retainer :"+ retainer + " schon drinn:"+ ((HashSet)data));
          
          
          ((HashSet)data).addAll(result);
          
        
          
        if(retainerSet())
          data=intersect(intersect(result,retainer),(HashSet)data);
       
//        else
//           ((HashSet)data).addAll(result);
      
           
        
    }
    
    
    public void addAll(SearchResult sr) throws Exception
    {
        
        if(sr.isNode())
            addAll(sr.getNodes());
        else if (sr.isSearchParameter())
        {
            logger.debug("Info :: kein merging von SearchParametern data wird \u00FCberschrieben");
            this.data=sr.data;
        }
        
        
        
    }
    
    
    
    public void addAllAndFilter(Sirius.server.middleware.types.MetaObjectNode[] nodes) throws Exception
    {
        logger.debug("addAllandfilter nodes gerufen");
        // no filtering necessary
        if(!filterSet)
        {
            addAll(nodes);
        }
        else
        {
            //Vector tmp = new Vector(nodes.length);
            
            for (int i=0;i<nodes.length /*&&  ((HashSet) this.data).size()<=MAX_HITS*/;i++)
            {
                MetaObjectNode o = null;
                
                if(nodes[i] instanceof Sirius.server.middleware.types.MetaObjectNode)
                {
                    o = (MetaObjectNode)nodes[i];
                }
                else
                {
                    if(logger!=null)logger.error("tried to add a node that was no node:-) type:"+nodes[i].getClass()+"\n Knoten enth\u00E4lt"+nodes[i]);
                    // element auslassen n\u00E4chstes probieren
                    continue;
                }
                
                if ( filter.contains(new Integer(o.getClassId())))
                    ((HashSet) this.data).add(o);
                
                
            }
            
            // addAll((MetaObjectNode[])tmp.toArray(new MetaObjectNode[tmp.size()]));
        }
        
        
        
        
    }
    
    
    
    public void setFilter(int[] classIds)
    {
        filter = new HashSet(classIds.length);
        
        for(int i =0;i<classIds.length;i++)
            filter.add(new Integer(classIds[i]));
        
        filterSet=true;
        
        
    }
    
    public void setFilterActive(boolean filterSet)
    {
        this.filterSet=filterSet;
    }
    
    public boolean isFilterActive()
    {
        return filterSet;
    }
    
    
    public boolean isFull()
    {
        if(isObject())
            return ((MetaObject[])data).length>=MAX_HITS;
        else if(isNode())
            // return  ((Node[])data).length>=MAX_HITS;
            return ((Collection)this.data).size() >=MAX_HITS;
        else
            return false; // na ja
    }
    
    
     public boolean isEmpty()
    {
        if(isObject())
            return ((MetaObject[])data).length==0;
        else if(isNode())
           
            return ((Collection)this.data).size()==0;
        else
            return true; // na ja
    }
    
    public int capacity()
    {
        if(isObject())
            return MAX_HITS-((MetaObject[])data).length;
        else if(isNode())
            return  MAX_HITS-((Collection)this.data).size();
        else
            return MAX_HITS;// na ja
        
    }
    
    public HashSet getRetainer()
    {
        return retainer;
    }
    
    public void setRetainer(HashSet retainer)
    {
        this.retainer = retainer;
    }
    
    public boolean retainerSet()
    {
        return retainer!=null;
    }
    
    
    public static HashSet intersect(HashSet a,HashSet b)
    {
        logger.debug("intersect \na "+a +"\nb" +b);
        
        HashSet c = new HashSet();
        
        Iterator<Node> iter = a.iterator();
        
        while(iter.hasNext())
        {
            Object o = iter.next();
            logger.debug("check whether element of a is in b"+o);
            if(b.contains(o))
            {
                c.add(o);
                  logger.debug("mutual element added to c"+o);
            }
            else
                 logger.debug("element  not added to c as it is not in b"+o);
        }
    
    return c;
    }
    
}
