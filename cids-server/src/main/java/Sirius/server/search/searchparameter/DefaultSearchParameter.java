/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * DefaultSearchParameter.java
 *
 * Created on 21. November 2003, 09:57
 */
package Sirius.server.search.searchparameter;

/**
 * DOCUMENT ME!
 *
 * @author   schlob
 * @version  $Revision$, $Date$
 */
public class DefaultSearchParameter implements SearchParameter, Comparable, java.io.Serializable {

    //~ Instance fields --------------------------------------------------------

    /** Creates a new instance of DefaultSearchParameter. */

    protected Object key;

    protected Object value;

    protected boolean isQueryResult;

    protected int pos;

    protected String description;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new DefaultSearchParameter object.
     *
     * @param  key            DOCUMENT ME!
     * @param  value          DOCUMENT ME!
     * @param  isQueryResult  DOCUMENT ME!
     */
    public DefaultSearchParameter(final Object key, final Object value, final boolean isQueryResult) {
        this.key = key;
        this.value = value;
        this.isQueryResult = isQueryResult;
        this.pos = 0;
    }

    /**
     * Creates a new DefaultSearchParameter object.
     *
     * @param  key            DOCUMENT ME!
     * @param  value          DOCUMENT ME!
     * @param  isQueryResult  DOCUMENT ME!
     * @param  pos            DOCUMENT ME!
     */
    public DefaultSearchParameter(final Object key, final Object value, final boolean isQueryResult, final int pos) {
        this(key, value, isQueryResult);
        this.pos = pos;
    }

    /**
     * Creates a new DefaultSearchParameter object.
     *
     * @param  key            DOCUMENT ME!
     * @param  value          DOCUMENT ME!
     * @param  isQueryResult  DOCUMENT ME!
     * @param  pos            DOCUMENT ME!
     * @param  description    DOCUMENT ME!
     */
    public DefaultSearchParameter(final Object key,
            final Object value,
            final boolean isQueryResult,
            final int pos,
            final String description) {
        this(key, value, isQueryResult, pos);
        this.description = description;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getKey() {
        return key;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public boolean isQueryResult() {
        return isQueryResult;
    }

    @Override
    public void setValue(final Object parameter) {
        this.value = parameter;
    }
    /////////////////////////////////////////////////////////////////

    @Override
    public java.util.Collection values() throws Exception {
        if (value instanceof java.util.Collection) {
            return (java.util.Collection)value;
        } else {
            throw new Exception("no Collection"); // NOI18N
        }
    }

    /////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(final Object o) {
        return key.equals(o);
    }

    //////////////////////////////////////////////////////////////////

    @Override
    public int getQueryPosition() {
        return pos;
    }
    /**
     * //////////////////////////////////////////////////////////////////// can be sorted by query position.
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public int compareTo(final Object o) {
        return pos - ((DefaultSearchParameter)o).pos;
    }

    @Override
    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    @Override
    public String getDescription() {
        return description;
    }
}
