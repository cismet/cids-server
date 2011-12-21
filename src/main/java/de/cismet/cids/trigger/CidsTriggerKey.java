/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.trigger;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
public class CidsTriggerKey {

    //~ Static fields/initializers ---------------------------------------------

    public static final String ALL = "CIDSTRIGGERKEYFORALLDOMAINSANDTABLES";
    public static final CidsTriggerKey FORALL = new CidsTriggerKey(ALL, ALL);

    //~ Instance fields --------------------------------------------------------

    private String domain;
    private String table;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsTriggerKey object.
     *
     * @param  domain  DOCUMENT ME!
     * @param  table   DOCUMENT ME!
     */
    public CidsTriggerKey(final String domain, final String table) {
        assert (domain != null);
        assert (table != null);
        this.domain = domain;
        this.table = table;
    }

    //~ Methods ----------------------------------------------------------------

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
     * @param  domain  DOCUMENT ME!
     */
    public void setDomain(final String domain) {
        this.domain = domain;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getTable() {
        return table;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  table  DOCUMENT ME!
     */
    public void setTable(final String table) {
        this.table = table;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CidsTriggerKey other = (CidsTriggerKey)obj;
        if ((this.domain == null) ? (other.domain != null) : (!this.domain.equals(other.domain))) {
            return false;
        }
        if ((this.table == null) ? (other.table != null) : (!this.table.equals(other.table))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = (67 * hash) + ((this.domain != null) ? this.domain.hashCode() : 0);
        hash = (67 * hash) + ((this.table != null) ? this.table.hashCode() : 0);
        return hash;
    }
}
