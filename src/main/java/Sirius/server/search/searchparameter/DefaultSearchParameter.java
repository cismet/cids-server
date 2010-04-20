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
    public DefaultSearchParameter(Object key, Object value, boolean isQueryResult) {
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
    public DefaultSearchParameter(Object key, Object value, boolean isQueryResult, int pos) {
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
    public DefaultSearchParameter(Object key, Object value, boolean isQueryResult, int pos, String description) {
        this(key, value, isQueryResult, pos);
        this.description = description;
    }

    //~ Methods ----------------------------------------------------------------

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public boolean isQueryResult() {
        return isQueryResult;
    }

    public void setValue(Object parameter) {
        this.value = parameter;
    }
    /////////////////////////////////////////////////////////////////

    public java.util.Collection values() throws Exception {
        if (value instanceof java.util.Collection) {
            return (java.util.Collection)value;
        } else {
            throw new Exception("no Collection");//NOI18N
        }
    }

    /////////////////////////////////////////////////////////////////////

    public boolean equals(Object o) {
        return key.equals(o);
    }

    //////////////////////////////////////////////////////////////////

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
    public int compareTo(Object o) {
        return pos - ((DefaultSearchParameter)o).pos;
    }

    public String toString() {
        if (value != null) {
            return value.toString();
        } else {
            return null;
        }
    }

    public String getDescription() {
        return description;
    }
}
