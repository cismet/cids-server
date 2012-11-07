/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.tools.search.clientstuff;

import javax.swing.ImageIcon;

/**
 * DOCUMENT ME!
 *
 * @author   stefan
 * @version  $Revision$, $Date$
 */
public abstract class AbstractCidsSearch implements CidsSearch {

    //~ Instance fields --------------------------------------------------------

    private final String name;
    private final ImageIcon icon;
    private final CidsSearchStatementGenerator stmntGenerator;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new AbstractCidsSearch object.
     *
     * @param  name      DOCUMENT ME!
     * @param  icon      DOCUMENT ME!
     * @param  stmntGen  DOCUMENT ME!
     */
    public AbstractCidsSearch(final String name, final ImageIcon icon, final CidsSearchStatementGenerator stmntGen) {
        this.stmntGenerator = stmntGen;
        this.name = name;
        this.icon = icon;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ImageIcon getIcon() {
        return this.icon;
    }
}
