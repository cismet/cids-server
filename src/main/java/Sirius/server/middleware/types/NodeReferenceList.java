//package Sirius.server.middleware.types;
//
//
//
//import Sirius.server.localserver.tree.*;
////import Sirius.server.localserver.tree.link.*;
//import Sirius.server.newuser.*;
//
//public class NodeReferenceList implements java.io.Serializable
//{
//      protected Link[] links; // verweise auf einen anderen localserver
//      protected Node[] nodes; // lokale Knoten
//
//
////---------------------------------------------------------------------------------------
//
//      public NodeReferenceList(Link[] links,Node[] nodes,Sirius.server.localserver.DBServer dbServer, UserGroup ug) throws Exception
//              {
//                      this.links = links;
//                      this.nodes = nodes;
//
//
////
////                    for (int i = 0;i < nodes.length; i++)
////                    {
////                            if(nodes[i] instanceof ObjectNode)
////                            {
////                                     oNode = (ObjectNode)nodes[i];
//////
//////
////                                // object wird nicht mehr gesetzt
////                                this.nodes[i] = new MetaObjectNode( oNode,dbServer.getSystemProperties().getServerName() );
////                                }
////                            else if(nodes[i] instanceof Sirius.server.localserver.tree.node.ClassNode)
////                                    this.nodes[i] = new MetaClassNode((Sirius.server.localserver.tree.node.ClassNode)nodes[i],dbServer.getSystemProperties().getServerName());
////                            else if(nodes[i] instanceof Sirius.server.localserver.tree.node.Node)
////                                    this.nodes[i] = new MetaNode(nodes[i],dbServer.getSystemProperties().getServerName());
////                            else
////                            throw new Exception("Knotenkonversion fehlgeschlagen da kein knoten wenn dies erreicht wird liegt ein COmpilerfehler vor");
////
////                    }
//
//      }
//
////---------------------------------------------------------------------------------------
//
///*
//      public NodeReferenceList(Sirius.localserver.tree.NodeReferenceList children,Sirius.localserver.DBServer dbServer) throws Exception
//      {  this(children.getRemoteLinks(),children.getLocalNodes(),dbServer);}
//*/
//
//      public NodeReferenceList(Sirius.server.localserver.tree.NodeReferenceList children,Sirius.server.localserver.DBServer dbServer, UserGroup ug) throws Exception
//              {  this(children.getRemoteLinks(),children.getLocalNodes(),dbServer,ug);}
//
//
//      public NodeReferenceList()
//      {links = new Link[0]; nodes = new Node[0];}
//
//      public Node[] getNodes()
//      {return nodes;}
//
//      public Link[] getLinks()
//      {return links;}
//
//
//
//
//
//}
