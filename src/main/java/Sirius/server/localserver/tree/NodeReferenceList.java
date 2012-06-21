/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.localserver.tree;
import Sirius.server.middleware.types.*;
//import Sirius.server.localserver.tree.link.*;
import Sirius.server.newuser.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class NodeReferenceList implements java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    protected java.util.ArrayList<Node> locals;
    protected java.util.ArrayList<Link> remotes;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new NodeReferenceList object.
     */
    public NodeReferenceList() {
        setLocals(new java.util.ArrayList<Node>(0));
        setRemotes(new java.util.ArrayList<Link>(0));
    }

    /**
     * Creates a new NodeReferenceList object.
     *
     * @param  nodes  DOCUMENT ME!
     */
    public NodeReferenceList(final java.util.ArrayList<Node> nodes) {
        setLocals(nodes);
        setRemotes(new java.util.ArrayList<Link>(0));
    }

    /**
     * Creates a new NodeReferenceList object.
     *
     * @param  nodes  DOCUMENT ME!
     */
    public NodeReferenceList(final Node[] nodes) {
        this(new java.util.ArrayList<Node>(java.util.Arrays.asList(nodes)));
    }

    /**
     * Creates a new NodeReferenceList object.
     *
     * @param  tree      DOCUMENT ME!
     * @param  children  DOCUMENT ME!
     * @param  ug        DOCUMENT ME!
     */
    public NodeReferenceList(final AbstractTree tree, final java.util.ArrayList<Link> children, final User u) {
        final int size = children.size();
        Link child = null;

        setLocals(new java.util.ArrayList<Node>(size));
        setRemotes(new java.util.ArrayList<Link>());

        for (int i = 0; i < size; i++) {
            try {
                child = children.get(i);

                if (child.isRemote()) {
                    remotes.add(child);
                } else {
                    final Node n = tree.getNode(child.getNodeId(), u);

                    // if null filtered (no permission)
                    if (n != null) {
                        locals.add(n);
                    }
                }
            } catch (Throwable e) {
                logger.error(
                    "<LS> ERROR :: fehler im NodeReferenceList Konstruktor" // NOI18N
                            + " index "                                     // NOI18N
                            + i
                            + "size"                                        // NOI18N
                            + size,
                    e);
            }
        }
    }

    /**
     * Creates a new NodeReferenceList object.
     *
     * @param  tree     DOCUMENT ME!
     * @param  nodeIDs  DOCUMENT ME!
     * @param  ug       DOCUMENT ME!
     */
    public NodeReferenceList(final AbstractTree tree, final java.util.Vector nodeIDs, final User u) {
        try {
            setLocals(new java.util.ArrayList<Node>(nodeIDs.size()));

            for (int i = 0; i < nodeIDs.size(); i++) {
                final Node n = tree.getNode(((Integer)nodeIDs.get(i)).intValue(), u);

                // if null filtered (no permission)
                if (n != null) {
                    locals.add(n);
                }
            }

            setRemotes(new java.util.ArrayList<Link>(0));
        } catch (Throwable e) {
            logger.error("<LS> ERROR :: Error in NodeReferenceList Construktor", e); // NOI18N
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Node[] getLocalNodes() {
        return locals.toArray(new Node[locals.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Link[] getRemoteLinks() {
        return (Link[])remotes.toArray(new Link[remotes.size()]);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  locals  DOCUMENT ME!
     */
    public void setLocals(final java.util.ArrayList<Node> locals) {
        this.locals = locals;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public java.util.ArrayList<Link> getRemotes() {
        return remotes;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  remotes  DOCUMENT ME!
     */
    public void setRemotes(final java.util.ArrayList<Link> remotes) {
        this.remotes = remotes;
    }
}
