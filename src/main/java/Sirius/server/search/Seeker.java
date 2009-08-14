package Sirius.server.search;

import Sirius.server.localserver.tree.*;
import Sirius.server.localserver.*;
import Sirius.server.sql.*;
import java.util.*;
//import  Sirius.search.WundaSearch.*;
//import Sirius.server.localserver.tree.node.*;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;
import Sirius.server.localserver._class.*;
import Sirius.server.localserver.object.*;
import Sirius.server.newuser.permission.PermissionHolder;
import Sirius.server.search.searchparameter.*;

public class Seeker {

    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());
    // public static final int MAX_HITS = 500;
    protected AbstractTree tree;
    protected DBConnectionPool conPool;
    protected ClassCache classCache;
    protected ObjectFactory objectFactory;
    protected ObjectHierarchy hierarchy;
    protected String domain;
    private DBServer dbServer;

    public Seeker(DBServer dbServer) {
        try {
            this.domain = dbServer.getSystemProperties().getServerName();

            // reference to the catalogue to be able to deliver nodes
            tree = dbServer.getTree();

            conPool = dbServer.getConnectionPool();

            // to get classes from classIDs
            classCache = dbServer.getClassCache();

            objectFactory = dbServer.getObjectFactory();

            hierarchy = new ObjectHierarchy(conPool);

        } catch (Throwable e) {
            logger.error(e);
        }

    }
    //---------------------------------------------------------------------------------------------------

    public SearchResult search(Query query, int[] classIds, UserGroup ug, int recursionLevel) throws Throwable {


        // enth\u00E4lt die Anzahl der updated datasets
        if (query.isUpdate()) {
            return new SearchResult(new Integer(conPool.getConnection().submitUpdate(query)));
        //xxx performantere Bedingung einfallen lassen
        }
        if (query.getResultType() == SearchResult.NODE) {

            SearchResult result = new SearchResult(new Sirius.server.middleware.types.MetaObjectNode[0]);

            // Objekte werden bei addAll unter der Verwendung des Filters hinzugef\u00FCgt

            if (recursionLevel == 0) {
                result.setFilter(classIds);
            // expandiere Klassenliste und setzte den Systemparameter cs_classids falls vorhanden
            // SearchParameter classParam = (SearchParameter)query.getParameter(SearchParameter.CLASSIDS);
            }
            query.setParameter(SearchParameter.CLASSIDS, StatementParametrizer.convertNumberArrayForSql(hierarchy.getExtendedClassList(classIds)));
//            
//            if(classParam!=null) // classId parameter vorhanden
//                classParam.setValue(StatementParametrizer.convertNumberArrayForSql(hierarchy.getExtendedClassList(classIds)));
//            
//            

            //Vector von Objectarrays
            //Zugriff auf Knoten
            Vector obs = null;
            if (query.getStatement() == null) //with caching
            {
                obs = (Vector) new DefaultResultHandler().handle(conPool.getConnection().submitQuery(query), query);
            } else // takes the queries statement
            {
                obs = (Vector) new DefaultResultHandler().handle(conPool.getConnection().executeQuery(query), query);
            }
            java.util.ArrayList<Node> nodes = new java.util.ArrayList<Node>(obs.size());

            //zuordnung der Objekte zu Knoten

            for (int i = 0; i < obs.size(); i++) {

                java.lang.Object[] object = (java.lang.Object[]) obs.get(i);

                int object_id = new Integer(object[1].toString()).intValue();
                int class_id = new Integer(object[0].toString()).intValue();

                String objectName = null;
                try {
                    objectName = object[2].toString();
                } catch (Exception skip) {
                    logger.debug("ObjectName=null");
                }


                //objectid@classId
                String key = constructKey(object_id, class_id);

                // logger.debug("objectkey found ::"+key);


                if (hierarchy.classIsReferenced(class_id)) {
                    Collection fatherStmnts = hierarchy.getFatherStatements(class_id, object_id);

                    // logger.debug("fatherstmnts" +fatherStmnts +" for classid" +class_id);


                    fatherStmnts.addAll(hierarchy.getArrayFatherStatements(class_id, object_id));

                    //logger.debug("fatherstmnts after array" +fatherStmnts+" for classid" +class_id);



                    Iterator iter = fatherStmnts.iterator();

                    while (iter.hasNext()) {
                        String recursiveCall = iter.next().toString();
                        logger.debug("rekursiver Aufruf der suche " + recursiveCall);

                        result.addAllAndFilter(this.search(new Query(new SystemStatement(true, -1, "", false, SearchResult.NODE, recursiveCall), domain), classIds, ug, recursionLevel++).getNodes());

                    }
                }


                
                PermissionHolder ph=classCache.getClass(class_id).getPermissions();
                //TODO Iconfactory setzen
                Node objectNode = new MetaObjectNode(-1, objectName, null, domain, object_id, class_id, true,ph.getPolicy(),-1,null,true);
                // Thorsten Rechte request 
                objectNode.setPermissions(ph);

                // java.util.ArrayList<Node>  v = tree.getObjectNodes(  key, null  ) ; //intValue()+"@"+domain,null /*userGroup*/

                //java.util.ArrayList<Node>  v = 

                //logger.debug("Knoten gefunden :"+v.size());

//               if(v.size()>0)// aufruf in der Ausgabe !
                // logger.debug("Knoten konnten hinzugef\u00FCgt werden == "+nodes.addAll(v));
//                  nodes.addAll(v);

                nodes.add(objectNode);
            }

            //logger.debug(nodes.size()+"  Knoten gefunden");

            Node[] n = new Node[0];

            if (nodes.size() > 0) {
                n = nodes.toArray(new Node[nodes.size()]);
            }
            Vector filtered = new Vector(n.length);

            //node Konvertierung und filtern
            for (int i = 0; i < n.length; i++) {
                //only object are found
                // logger.debug(n[i] + " is of type"+ n[i].getClass());

                MetaObjectNode on = (MetaObjectNode) n[i];

                if (on.getPermissions().hasPermission(ug.getKey(), PermissionHolder.READPERMISSION)) // readPermission
                {

                    // objectzuordnung abgeschaltet
                    filtered.add(on);
                } else {
                    logger.info("UserGroup " + ug + "has no Read Permission for node " + on);
                }


            }


            if (filtered.size() > 0) {
                result.addAllAndFilter((Sirius.server.middleware.types.MetaObjectNode[]) filtered.toArray(new Sirius.server.middleware.types.MetaObjectNode[filtered.size()]));
            }
            return result;


        } else if (query.getResultType() == SearchResult.OBJECT) {

            Vector objects = null;

            if (query.getStatement() == null) //with caching
            {
                objects = (Vector) new DefaultResultHandler().handle(conPool.getConnection().submitQuery(query), query);
            } else // takes the queries statement
            {
                //query wird hier ausgefuehrt, resulat ist vector<object[]>
                objects = (Vector) new DefaultResultHandler().handle(conPool.getConnection().executeQuery(query), query);
            }
            Sirius.server.middleware.types.MetaObject[] metaObject = new Sirius.server.middleware.types.MetaObject[objects.size()];

            for (int i = 0; i < objects.size(); i++) {

                int classID;
                int objectID;
                //Query hat als resultat classID und objectID  in form eines object[] geliefert
                java.lang.Object[] object = (java.lang.Object[]) objects.get(i);

                try {

                    classID = Integer.parseInt(object[0].toString());
                    objectID = Integer.parseInt(object[1].toString());

                } catch (Throwable e) {

                    Exception ex = new Exception("The results of the Query do not possess the necessary structure. Results of the Query must be tuple (class_id, object_id).");
                    ex.setStackTrace(e.getStackTrace());
                    throw ex;
                }

                metaObject[i] = new Sirius.server.middleware.types.DefaultMetaObject(objectFactory.getObject(objectID, classID), domain);
                metaObject[i].setAllClasses(classCache.getClassHashMap());
            }

            // hier kein Maxhits beachtet
            return new SearchResult(metaObject);

        } else // kein Knoten
        {

            // vorsicht
            return new SearchResult(new StringResultHandler().handle(conPool.getConnection().submitQuery(query), query));



        }

    }

    private String constructKey(int oid, int cid) {
        return oid + "@" + cid;
    }
}// end seeker





