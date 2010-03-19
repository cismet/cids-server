/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*******************************************************************************

        Copyright (c)   :       EIG (Environmental Informatics Group)
                                                <br> http://www.htw-saarland.de/eig
                                                <br> Prof. Dr. Reiner Guettler
                                                <br> Prof. Dr. Ralf Denzer

                                                <br> HTWdS
                                                <br> Hochschule fuer Technik und Wirtschaft des Saarlandes
                                                <br> Goebenstr. 40
                                                <br> 66117 Saarbruecken
                                                <br> Germany

        Programmers             :       Bernd Kiefer
        <br>
        Project                 :       WuNDA 2
        Version                 :       1.0
        Purpose                 :
        Created                 :
        History                 :

*******************************************************************************/
package Sirius.server.search.store;

/**
 * Stellt Basisinformationen fuer Suchprofile und Profilergebnisse zur Verfuegung. *
 *
 * @version  $Revision$, $Date$
 */

public interface Info {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  InfoId *
     */
    int getID();
    /**
     * DOCUMENT ME!
     *
     * @return  InfoName *
     */
    String getName();

    /**
     * DOCUMENT ME!
     *
     * @return  HeimatLocalServerName*
     */
    String getDomain();
}
