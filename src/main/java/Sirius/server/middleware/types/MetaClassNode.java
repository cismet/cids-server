package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.Policy;

/**
*ClassNode in this package reprents a class (in the sirius context) wrapped with navigational metainfo (Node)
*e.g. to insert a class  in the catalogue use an ClassNode
*This Type is to be used as return value of an RMI Method
*/

public class MetaClassNode extends Node implements Comparable
{


	public MetaClassNode(int id,String domain ,int classID, String name,String description,boolean isLeaf,Policy policy,int iconFactory,String icon,boolean derivePermissionsFromClass,int classId)
	{
		super(id,name,domain,description,isLeaf,policy,iconFactory,icon,derivePermissionsFromClass);
		this.classId=classID;
	}

//	public MetaClassNode(MetaClassNode node)
//	{
//		super(node);
//		this.classId=node.getClassID();
//
//	}


//	public MetaClassNode(Sirius.server.localserver.tree.node.ClassNode node,String domain) throws Exception
//		{
//			super(node,domain);
//			this.classId=node.getClassID();
//
//	}

//--------------------------------------








}