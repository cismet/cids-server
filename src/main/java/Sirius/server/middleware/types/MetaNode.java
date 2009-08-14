package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.Policy;


/**
*Organisatory Node (represents no Class or Object)
*
*/

public class MetaNode extends Sirius.server.middleware.types.Node
{
	public MetaNode(int id,String domain, String name,String description,boolean isLeaf,Policy policy,int iconFactory,String icon,boolean derivePermissionsFromClass,int classId)
	{super(id,name,domain,description,isLeaf,policy,iconFactory,icon,derivePermissionsFromClass);
        super.classId=classId;
        }

//	public MetaNode(MetaNode node)
//	{super(node);}

//	public MetaNode(Sirius.server.localserver.tree.node.Node node,String domain)
//	{super(node,domain);}

}
