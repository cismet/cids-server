/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.middleware.types;

import java.io.Serializable;

import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public final class HistoryObject implements Serializable {

    //~ Instance fields --------------------------------------------------------

    private final Sirius.server.localserver._class.Class clazz;
    private final String jsonData;
    private final Date validFrom;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new HistoryObject object.
     *
     * @param  clazz      DOCUMENT ME!
     * @param  jsonData   DOCUMENT ME!
     * @param  validFrom  DOCUMENT ME!
     */
    public HistoryObject(final Sirius.server.localserver._class.Class clazz,
            final String jsonData,
            final Date validFrom) {
        this.clazz = clazz;
        this.jsonData = jsonData;
        this.validFrom = validFrom;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Sirius.server.localserver._class.Class getClazz() {
        return clazz;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public String getJsonData() {
        return jsonData;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Date getValidFrom() {
        return validFrom;
    }
}
