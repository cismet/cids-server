package Sirius.server.middleware.types;



import Sirius.server.newuser.permission.*;
import Sirius.util.*;

public class Link implements java.io.Serializable,Groupable
{
    // protected int id;
    protected int nodeId;
    protected boolean remote;
    protected String domain;
    
    public Link(int nodeId, String domain)
    {
        this.nodeId=nodeId;
        this.domain=domain;
        
        
    }
    
    
    
    public String getGroup()
    {return domain;}
    
    public String getDomain()
    {return domain;}
    
    
    public int getNodeId()
    {return nodeId;}
    
    public int getId()
    {return getNodeId();}
    
    public boolean isRemote()
    {
        return remote;
    }
    
    public void setRemote(boolean remote)
    {
        this.remote = remote;
    }
    
    
    
    
    
    
    
}