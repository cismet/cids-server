package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.Policy;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class MetaObjectNode extends Node implements Comparable
{
    private transient final static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(MetaObjectNode.class);
    
    protected int objectId;
    
    
    
    protected MetaObject theObject;
    
//-----------------------------------------------
    
    public MetaObjectNode(int id,String localServerName,MetaObject theObject, String name,String description,boolean isLeaf,Policy policy,int iconFactory,String icon,boolean derivePermissionsFromClass)
    {
        super(id,name ,localServerName,description,isLeaf,policy,iconFactory,icon,derivePermissionsFromClass);
        this.theObject = theObject;
        if(theObject!=null)
        {
            objectId=theObject.getID();
            classId=theObject.getClassID();
        }
        else
        {
            objectId=-1;
            classId=-1;
        }
        
    }
    
    public MetaObjectNode(int id, String name,String description,String domain,int objectId,int classId,boolean isLeaf,Policy policy,int iconFactory,String icon,boolean derivePermissionsFromClass)
    {
        super(id,name ,domain,description,isLeaf,policy,iconFactory,icon,derivePermissionsFromClass);
        
        this.objectId=objectId;
        this.classId=classId;
        
        
    }
    
//------------------------------------------------
    
    
//    public MetaObjectNode(int id,String localServerName,String name,String description,boolean isLeaf)
//    {
//        super(id,name,localServerName,description,isLeaf);
//
//    }
    
    
//--------------------------------------------------
    
    
//    public MetaObjectNode(int id,String localServerName,String name,String description)
//    {
//        super(id,name,localServerName,description,false);
//
//    }
    
    
    
//-----------------------------------------------
    
//    public MetaObjectNode(MetaObjectNode node)
//    {
//        super(node);
//        //this.theObject = node.getObject();
//        this.classId=node.getClassId();
//        this.objectId=node.getObjectId();
//    }
    
    
    public String getDescription()
    
    {
        
        return super.getDescription();
    }
    
    
    
//----------------------------------------------------------
    
//    public MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode node,Sirius.server.localserver.object.Object object,String domain) throws Exception
//    {
//        super(node,domain);
//        this.theObject = new MetaObject(object,domain);
//        this.objectId=object.getID();
//        this.classId=object.getClassID();
//    }
//
//    //Bugfix
//    public MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode node,Sirius.server.localserver.object.Object object,String domain, UserGroup ug) throws Exception
//    {
//        super(node,domain);
//        this.theObject = new MetaObject(object.filter(ug),domain);
//        this.objectId=object.getID();
//        this.classId=object.getClassID();
//
//    }
    
//    public MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode node,MetaObject object,String localServerName) throws Exception
//    {
//        super(node,localServerName);
//        this.theObject = object;
//        this.objectId=object.getID();
//        this.classId=object.getClassID();
//    }
//
//    public MetaObjectNode(Sirius.server.localserver.tree.node.ObjectNode node,String localServerName) throws Exception
//    {
//        super(node,localServerName);
//        this.objectId=node.getObjectID();
//        this.classId=node.getClassID();
//    }
//
    
    
    
//------------------------------------------------
    
    public MetaObject getObject()
    {return theObject;}
    
//---------------------------------------------------
    
    public void setObject(MetaObject theObject)
    {this.theObject = theObject; if(theObject!=null)
     { this.classId = theObject.getClassID();this.objectId=theObject.getID();}}
    
    public boolean objectSet()
    {return theObject!=null;}
    
    public int getObjectId()
    {return objectId;}
    
   
    
    
    public int hashCode()
    {
        HashCodeBuilder hb =   new HashCodeBuilder();
      
       
        hb.append(id);
        hb.append(classId);
        hb.append(objectId);
        hb.append(domain);
        
        return hb.toHashCode();
        
    }
    
    public boolean equals(Object other)
    {
        
        if ( !(other instanceof MetaObjectNode) ) return false;
        
        MetaObjectNode o = (MetaObjectNode)other;
        
        return id==o.id && domain.equals(o.domain) && objectId==o.objectId && classId==o.classId;
        
        
    }
    
    
}