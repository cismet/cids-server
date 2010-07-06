/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.search.store;

import Sirius.server.newuser.*;

import java.io.*;

/**
 * Repraesentiert ein Such-Profil.*
 *
 * @version  $Revision$, $Date$
 */
public class QueryData extends QueryInfo implements java.io.Serializable, Info {

    //~ Static fields/initializers ---------------------------------------------

    /** Use serialVersionUID for interoperability. */
    private static final long serialVersionUID = -878146480754175149L;

    //~ Instance fields --------------------------------------------------------

    /** Suchergebnisdaten.* */
    protected byte[] data;

    //~ Constructors -----------------------------------------------------------

    /**
     * erzeugt leeres QueryObject mit der Id -1.*
     */
    public QueryData() {
        this(-1, "", "", "", new byte[0]);//NOI18N
    }

    // --------------------------------------------------------------------

    /**
     * -----------------------------------------------------------------
     *
     * @param  id      DOCUMENT ME!
     * @param  domain  DOCUMENT ME!
     * @param  name    DOCUMENT ME!
     * @param  data    DOCUMENT ME!
     */
    public QueryData(final int id, final String domain, final String name, final byte[] data) {
        super(id, name, domain, "");//NOI18N
        this.data = data;
    }

    // -----------------------------------------------------------------
    /**
     * Creates a new QueryData object.
     *
     * @param  domain    localServerName HeimatLocalServer
     * @param  name      Name der Suche
     * @param  fileName  isUserQuery handelt es sich um eine Suche von einem User oder UserGroup *
     * @param  data      Daten der Suche
     */
    public QueryData(final String domain, final String name, final String fileName, final byte[] data) {
        super(-1, name, domain, fileName);
        this.data = data;
    }
    // -----------------------------------------------------------------

    /**
     * Creates a new QueryData object.
     *
     * @param  id        QueryId
     * @param  domain    localServerName HeimatLocalServer
     * @param  name      Name des Suchergebisses
     * @param  fileName  isUserQuery handelt es sich um eine Suche von einem User oder UserGroup *
     * @param  data      Daten des Suchergebnisses
     */
    public QueryData(final int id, final String domain, final String name, final String fileName, final byte[] data) {
        this(domain, name, fileName, data);
        this.id = id;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  QueryDaten *
     */
    public final byte[] getData() {
        return data;
    }

    /**
     * ueberlaedt toString()-Methode von java.lang.Object.*
     *
     * @return  DOCUMENT ME!
     */
    @Override
    public String toString() {
        return "id:" + id + " lsName: " + domain + " name:" + name + " length: " + data.length;//NOI18N
    }

    /**
     * DOCUMENT ME!
     *
     * @return  true wenn id > 0, sonst false*
     */
    public boolean idIsValid() {
        return id >= 0;
    }
}
