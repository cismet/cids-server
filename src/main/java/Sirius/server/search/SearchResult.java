/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
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
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class SearchResult implements java.io.Serializable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 5975527842318427668L;

    private static final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            SearchResult.class);

    // vor\u00FCbergehend
    public static int NODE = 1;
    public static int COLLECTION = 2;
    public static int VALUE = 3;
    public static int OBJECT = 4;

    public static int MAX_HITS = 500;

    //~ Instance fields --------------------------------------------------------

    protected HashSet filter = new HashSet();
    protected boolean filterSet = false;

    protected Object data;

    private HashSet retainer;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new SearchResult object.
     *
     * @param  srs  DOCUMENT ME!
     */
    public SearchResult(final SearchResult srs) {
        this.data = srs.data;
    }

    /**
     * Creates a new SearchResult object.
     *
     * @param  nodes  DOCUMENT ME!
     */
    public SearchResult(final MetaObjectNode[] nodes) {
        this.data = new HashSet();

        for (int i = 0; i < nodes.length; i++) {
            ((HashSet)this.data).add(nodes[i]);
        }
    }

    /**
     * Creates a new SearchResult object.
     *
     * @param  data  DOCUMENT ME!
     */
    public SearchResult(final MetaObject[] data) {
        this.data = data;
    }

    /**
     * Creates a new SearchResult object.
     *
     * @param  data  DOCUMENT ME!
     */
    public SearchResult(final Object data) {
        this.data = data;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isObject() {
        return data instanceof MetaObject[];
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isNode() {
        return (data instanceof MetaObjectNode[]) || (data instanceof HashSet);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isSearchParameter() {
        return !(isNode() || isObject());
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public MetaObjectNode[] getNodes() throws Exception {
        if (isNode()) {
            // System.out.println("nodes: " + ((Collection)this.data).size());
            return (MetaObjectNode[])((Collection)this.data).toArray(
                    new MetaObjectNode[((Collection)this.data).size()]);
        } else {
            throw new Exception("SearchResult.data no Node[]");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public MetaObject[] getObjects() throws Exception {
        if (isObject()) {
            return (MetaObject[])data;
        } else {
            throw new Exception("SearchResult.data no MetaObject[]");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public Object getSearchParameter() throws Exception {
        if (isSearchParameter()) {
            return data;
        } else {
            throw new Exception("SearchResult.data no SearchParameter");
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public java.lang.Object getResult() {
        return data;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodes  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void addAll(final Sirius.server.middleware.types.MetaObjectNode[] nodes) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("addAll nodes gerufen");
        }

        final HashSet result = new HashSet(nodes.length);

        // f\u00FCge nodes Hashset hinzu
        for (int i = 0; i < nodes.length; i++) {
            result.add(nodes[i]);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("neu dazu:" + result + " retainer :" + retainer + " schon drinn:" + ((HashSet)data));
        }

        ((HashSet)data).addAll(result);

        if (retainerSet()) {
            data = intersect(intersect(result, retainer), (HashSet)data);
        }

//        else
//           ((HashSet)data).addAll(result);

    }

    /**
     * DOCUMENT ME!
     *
     * @param   sr  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void addAll(final SearchResult sr) throws Exception {
        if (sr.isNode()) {
            addAll(sr.getNodes());
        } else if (sr.isSearchParameter()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Info :: kein merging von SearchParametern data wird \u00FCberschrieben");
            }
            this.data = sr.data;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   nodes  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    public void addAllAndFilter(final Sirius.server.middleware.types.MetaObjectNode[] nodes) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("addAllandfilter nodes gerufen");
        }
        // no filtering necessary
        if (!filterSet) {
            addAll(nodes);
        } else {
            // Vector tmp = new Vector(nodes.length);

            for (int i = 0; i < nodes.length /*&&  ((HashSet) this.data).size()<=MAX_HITS*/; i++) {
                MetaObjectNode o = null;

                if (nodes[i] instanceof Sirius.server.middleware.types.MetaObjectNode) {
                    o = (MetaObjectNode)nodes[i];
                } else {
                    if (logger != null) {
                        logger.error(
                            "tried to add a node that was no node:-) type:"
                            + nodes[i].getClass()
                            + "\n Knoten enth\u00E4lt"
                            + nodes[i]);
                    }
                    // element auslassen n\u00E4chstes probieren
                    continue;
                }

                if (filter.contains(new Integer(o.getClassId()))) {
                    ((HashSet)this.data).add(o);
                }
            }

            // addAll((MetaObjectNode[])tmp.toArray(new MetaObjectNode[tmp.size()]));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  classIds  DOCUMENT ME!
     */
    public void setFilter(final int[] classIds) {
        filter = new HashSet(classIds.length);

        for (int i = 0; i < classIds.length; i++) {
            filter.add(new Integer(classIds[i]));
        }

        filterSet = true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  filterSet  DOCUMENT ME!
     */
    public void setFilterActive(final boolean filterSet) {
        this.filterSet = filterSet;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFilterActive() {
        return filterSet;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isFull() {
        if (isObject()) {
            return ((MetaObject[])data).length >= MAX_HITS;
        } else if (isNode()) {
            // return  ((Node[])data).length>=MAX_HITS;
            return ((Collection)this.data).size() >= MAX_HITS;
        } else {
            return false; // na ja
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isEmpty() {
        if (isObject()) {
            return ((MetaObject[])data).length == 0;
        } else if (isNode()) {
            return ((Collection)this.data).size() == 0;
        } else {
            return true; // na ja
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int capacity() {
        if (isObject()) {
            return MAX_HITS - ((MetaObject[])data).length;
        } else if (isNode()) {
            return MAX_HITS - ((Collection)this.data).size();
        } else {
            return MAX_HITS; // na ja
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public HashSet getRetainer() {
        return retainer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  retainer  DOCUMENT ME!
     */
    public void setRetainer(final HashSet retainer) {
        this.retainer = retainer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean retainerSet() {
        return retainer != null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   a  DOCUMENT ME!
     * @param   b  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static HashSet intersect(final HashSet a, final HashSet b) {
        if (logger.isDebugEnabled()) {
            logger.debug("intersect \na " + a + "\nb" + b);
        }

        final HashSet c = new HashSet();

        final Iterator<Node> iter = a.iterator();

        while (iter.hasNext()) {
            final Object o = iter.next();
            if (logger.isDebugEnabled()) {
                logger.debug("check whether element of a is in b" + o);
            }
            if (b.contains(o)) {
                c.add(o);
                if (logger.isDebugEnabled()) {
                    logger.debug("mutual element added to c" + o);
                }
            } else if (logger.isDebugEnabled()) {
                logger.debug("element  not added to c as it is not in b" + o);
            }
        }

        return c;
    }
}
