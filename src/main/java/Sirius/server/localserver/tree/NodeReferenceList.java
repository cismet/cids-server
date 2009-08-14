package Sirius.server.localserver.tree;


//import Sirius.server.localserver.tree.node.*;
import Sirius.server.middleware.types.*;
//import Sirius.server.localserver.tree.link.*;
import Sirius.server.newuser.*;

public class NodeReferenceList implements java.io.Serializable
{
    private transient final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    
    protected  java.util.ArrayList<Node> locals;
    protected  java.util.ArrayList<Link> remotes;
    
    public NodeReferenceList(AbstractTree tree,java.util.ArrayList<Link> children,UserGroup ug)
    {
        
        
        int size = children.size();
        Link child = null;
        
        setLocals(new java.util.ArrayList<Node>(size));
        setRemotes(new java.util.ArrayList<Link>());
        
        for(int i=0;i < size ; i++)
        {
            
            try
            {
                child = children.get(i);
                
                if(child.isRemote())
                    remotes.add(child);
                else
                {
                    Node n = tree.getNode(child.getNodeId(),ug);
                    
                    //if null filtered (no permission)
                    if(n!=null)
                        locals.add(n);
                }
            }
            catch (Throwable e)
            {
                logger.error("<LS> ERROR :: fehler im NodeReferenceList Konstruktor"+" index "+i+"size"+size,e);
                
                
            }
        }
        
        
    }
    
    
    public NodeReferenceList(AbstractTree tree , java.util.Vector nodeIDs, UserGroup ug)
    {
        try
        {
            setLocals(new java.util.ArrayList<Node>(nodeIDs.size()));
            
            for(int i = 0; i< nodeIDs.size();i++)
            {
                Node n = tree.getNode(  ((Integer)nodeIDs.get(i)).intValue()  ,ug );
                
                //if null filtered (no permission)
                if(n!=null)
                    locals.add(  n );
            }
            
            setRemotes(new java.util.ArrayList<Link>(0));
        }
        catch (Throwable e)
        {
            logger.error("<LS> ERROR :: fehler im NodeReferenceList Konstruktor",e);
            
            
        }
        
        
    }
    
    public NodeReferenceList(java.util.ArrayList<Node> nodes)
    {
        
        setLocals(nodes);
        setRemotes(new java.util.ArrayList<Link>(0));
 
    }
    
     public NodeReferenceList()
    {
        
        setLocals(new java.util.ArrayList<Node>(0));
        setRemotes(new java.util.ArrayList<Link>(0));
 
    }
    
     public NodeReferenceList(Node[] nodes)
    {
        
        this( new java.util.ArrayList<Node>( java.util.Arrays.asList(nodes)   ) );
 
    }
    
    
    public Node[] getLocalNodes()
    {
        return locals.toArray(new Node[locals.size()]);
    }
    
    public Link[] getRemoteLinks()
    {
        return (Link[])remotes.toArray(new Link[remotes.size()]);
    }
    
    public void setLocals(java.util.ArrayList<Node> locals)
    {
        this.locals = locals;
    }
    
    public java.util.ArrayList<Link> getRemotes()
    {
        return remotes;
    }
    
    public void setRemotes( java.util.ArrayList<Link> remotes)
    {
        this.remotes = remotes;
    }
    
    
    
    
}
