/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RemoteTester.java
 *
 * Created on 31. August 2004, 08:55
 */
package Sirius.server.middleware.impls.proxy;

import Sirius.server.middleware.interfaces.proxy.*;
import Sirius.server.middleware.interfaces.proxy.SearchService;
import Sirius.server.middleware.types.*;
import Sirius.server.newuser.*;

import java.rmi.*;
import java.rmi.registry.*;

import java.util.Iterator;

import de.cismet.cids.server.connectioncontext.ClientConnectionContext;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class RemoteTester {

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of RemoteTester.
     */
    public RemoteTester() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   args  the command line arguments
     *
     * @throws  Throwable  DOCUMENT ME!
     */
    public static void main(final String[] args) throws Throwable {
        final String domain = "WUNDA_BLAU"; // NOI18N

        // security alles unsinn :-)
        System.setSecurityManager(
            new RMISecurityManager() {

                @Override
                public void checkConnect(final String host, final int port) {
                }
                @Override
                public void checkConnect(final String host, final int port, final Object context) {
                }
            });

        // rmi registry lokaliseren
        final java.rmi.registry.Registry rmiRegistry = LocateRegistry.getRegistry(1099);

        // lookup des callservers
        final Remote r = (Remote)Naming.lookup("rmi://localhost/callServer"); // NOI18N

        // ich weiss, dass die server von callserver implementiert werden
        final SearchService ss = (SearchService)r;
        final CatalogueService cat = (CatalogueService)r;
        final MetaService meta = (MetaService)r;
        final UserService us = (UserService)r;

//        System.out.println("server contacted :: "+r);

        // Benutzergruppe anlegen (muss auf dem Server existieren)
        // UserGroup ug = new UserGroup(0,"Administratoren",domain );

        // user anlegen muss auf dem server existieren
        // User u = new User(0,  "admin",domain, ug );

        // oder mit login
        // ug_domain,ug_name,u_domain,u_name,password
        final User u = us.getUser(
                domain,
                "Administratoren",
                domain,
                "admin",
                "x",
                getClientConnectionContext()); // NOI18N

        System.out.println(u + "  user token retrieved"); // NOI18N

        // meta.getC

        // Beispiel:
        // hole alle Klassen einer Dom\u00E4ne
        // hole dir dann f\u00FCr jede Klasse ein ObjektTemplate
// MetaClass[] cs = meta.getClasses(u,domain);
//
// for(int i=0;i<cs.length;i++)
// {
// System.out.println("NEW instance ::"+meta.getInstance(u,cs[i]));
//
// }

// System.out.println("!!getInstance durchgelaufen !!");

        // Beispiel:
        // Template f\u00FCr eine Object der ersten Klasse
        // MetaObject mo = meta.getInstance(u,cs[0]);
        final MetaObject mo = meta.getMetaObject(
                u,
                5646,
                6,
                "WUNDA_BLAU",
                getClientConnectionContext());   // NOI18N
        System.out.println("metaobject::" + mo); // NOI18N

// alle attribute des Objects
        // ObjectAttribute[] attribs = mo.getAttribs();

        final java.util.Collection col1 = mo.getTraversedAttributesByType(Class.forName(""));                            // NOI18N
        final java.util.Collection col2 = mo.getAttributesByType(Class.forName("com.vividsolutions.jts.geom.Geometry")); // NOI18N
        final java.util.Collection col3 = mo.getAttributesByType(Class.forName("com.vividsolutions.jts.geom.Geometry"),  // NOI18N
                1);
        final java.util.Collection col4 = mo.getAttributesByType(Class.forName("com.vividsolutions.jts.geom.Geometry"),  // NOI18N
                2);
        final java.util.Collection col5 = mo.getAttributesByType(Class.forName("com.vividsolutions.jts.geom.Geometry"),  // NOI18N
                3);

        Iterator iter = col1.iterator();
        System.out.println("!!!!!!!!!!!! traversiert !!!!!!!!!!!!!!!!!!!"); // NOI18N
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }

        iter = col2.iterator();
        System.out.println("!!!!!!!!!!!! level 0 !!!!!!!!!!!!!!!!!!!"); // NOI18N
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }

        iter = col3.iterator();
        System.out.println("!!!!!!!!!!!! level 1 !!!!!!!!!!!!!!!!!!!"); // NOI18N
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }

        iter = col4.iterator();
        System.out.println("!!!!!!!!!!!! level 2 !!!!!!!!!!!!!!!!!!!"); // NOI18N
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }

        iter = col5.iterator();
        System.out.println("!!!!!!!!!!!! level 3 !!!!!!!!!!!!!!!!!!!"); // NOI18N
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static ClientConnectionContext getClientConnectionContext() {
        return ClientConnectionContext.createDeprecated();
    }
}

////        // Ausgabe ob das Attribut aus einem String erzeugt werden kann -- macht hier jetzt keinen Sinn soll nur ein Beispiel sein
////        for(int i=0;i<attribs.length;i++)
////            System.out.println(attribs[i].getName()+" ::  "+attribs[i] +" :: "+ attribs[i].isStringCreateable()+"  :: "+attribs[i].objectCreator);
////
//
////        System.out.println("catalog test");
////
////        // besorge alle einstiegsknoten von servern auf denen u bekannt ist
////        Node[] roots = cat.getRoots(u);
////
////        for(int i =0;i<roots.length;i++)
////            System.out.println(roots[i]);
////
////        //n\u00E4chstes level des ersten root knotens
////        if(roots.length>0)
////        {
////            Node[] childs = cat.getChildren(u,roots[0].getID(), roots[0].getDomain());
////
////            System.out.println("Kinder von "+roots[0]);
////
////            for(int i=0;i< childs.length;i++)
////                System.out.println(childs[i]);
////
////        }
//
//        //SystemStatement sysS=new SystemStatement(true,1,"",false,SearchResult.COLLECTION,"select distinct lage_oder_kategorie  from luftbildschraegaufnahmen");
//        //Query q=new Query(sysS, "WUNDA_BLAU");
////        Query q=new Query(new QueryIdentifier("WUNDA_BLAU","singleStringQuery"));
////        HashMap h=new HashMap();
////        h.put("whereclause", "1=1");
////        h.put("field", "lage_oder_kategorie");
////        h.put("table", "luftbildschraegaufnahmen");
////
////        q.setParameters(h);
////        q.setResultType(SearchResult.COLLECTION);
////
////        HashMap searchOptions=ss.getSearchOptions(u,"WUNDA_BLAU");
////        SearchOption so=(SearchOption)searchOptions.get("singleStringQuery@WUNDA_BLAU");
////        so.setDefaultSearchParameter("whereclause", "1=1");
////        so.setDefaultSearchParameter("field", "lage_oder_kategorie");
////        so.setDefaultSearchParameter("table", "luftbildschraegaufnahmen");
////        so.getQuery().setResultType(SearchResult.COLLECTION);
////
////        so.addUserGroup("0");
//
////        SearchOption[] soa=new SearchOption[1];
////        soa[0]=so;
////        SearchResult sr=ss.search(u, new String[]{}, soa);
////
////
////        System.out.println(sr.getResult());
////        System.out.println(sr.getResult().getClass());
////        Vector v=(Vector)sr.getResult();
////        Iterator it=v.iterator();
////        while (it.hasNext()) {
////            Object o=it.next();
////            if (o instanceof String) {
////                System.out.println(o.toString());
////            }
////            else if (o instanceof String[]) {
////                String[] sa=(String[])o;
////                System.out.println("[]"+sa[0]);
////            }
////
////        }
////
////    }
//
//
//
//
