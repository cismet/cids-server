/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import Sirius.server.newuser.permission.*;

import Sirius.util.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Link implements java.io.Serializable, Groupable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 1635588337104297051L;

    //~ Instance fields --------------------------------------------------------

    // protected int id;
    protected int nodeId;
    protected boolean remote;
    protected String domain;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Link object.
     *
     * @param  nodeId  DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     */
    public Link(final int nodeId, final String domain) {
        this.nodeId = nodeId;
        this.domain = domain;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getGroup() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getDomain() {
        return domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getNodeId() {
        return nodeId;
    }

    @Override
    public int getId() {
        return getNodeId();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean isRemote() {
        return remote;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  remote  DOCUMENT ME!
     */
    public void setRemote(final boolean remote) {
        this.remote = remote;
    }
}
