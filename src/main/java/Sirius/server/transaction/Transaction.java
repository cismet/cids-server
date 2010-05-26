/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * Transasction.java
 *
 * Created on 10. August 2004, 10:41
 */
package Sirius.server.transaction;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class Transaction implements java.io.Serializable, Sirius.util.Mapable {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = 55938615230420810L;

    //~ Instance fields --------------------------------------------------------

    // **name (identifier) der auszuf\u00FChrenden Transaktion -> Methode/
    protected String name;

    // ** parameter Liste der Methode*/
    protected Object[] params;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new instance of Transasction.
     */
    public Transaction() {
        name = "";
        params = new Object[0];
    }

    /**
     * Creates a new Transaction object.
     *
     * @param  name    DOCUMENT ME!
     * @param  params  DOCUMENT ME!
     */
    public Transaction(final String name, final Object[] params) {
        this.name = name;
        this.params = params;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Getter for property name.
     *
     * @return  Value of property name.
     */
    public java.lang.String getName() {
        return name;
    }

    /**
     * Setter for property name.
     *
     * @param  name  New value of property name.
     */
    public void setName(final java.lang.String name) {
        this.name = name;
    }

    /**
     * Getter for property params.
     *
     * @return  Value of property params.
     */
    public java.lang.Object[] getParams() {
        return this.params;
    }

    /**
     * Setter for property params.
     *
     * @param  params  New value of property params.
     */
    public void setParams(final java.lang.Object[] params) {
        this.params = params;
    }

    @Override
    public Object constructKey(final Sirius.util.Mapable m) {
        return m.getKey();
    }

    @Override
    public Object getKey() {
        return name;
    }
}
