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
package Sirius.server.registry.events;

import java.awt.event.*;

import Sirius.server.registry.*;
import Sirius.server.registry.monitor.*;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class MonitorUpdateListener implements ActionListener {

    //~ Instance fields --------------------------------------------------------

    protected RegistryMonitor registryMonitor;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MonitorUpdateListener object.
     *
     * @param  registryMonitor  DOCUMENT ME!
     */
    public MonitorUpdateListener(RegistryMonitor registryMonitor) {
        this.registryMonitor = registryMonitor;
    }

    //~ Methods ----------------------------------------------------------------

    public void actionPerformed(ActionEvent event) {
        registryMonitor.updateTables();
    }
}
