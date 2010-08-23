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

import Sirius.server.registry.*;
import Sirius.server.registry.monitor.*;

import java.awt.event.*;

/**
 * ActionListener fuer die Einstellungen fuer das automatische Update des RegistryMonitor. Hier wird das ZeitIntervall
 * fuer die Update-Schleife gesetzt. *
 *
 * @version  $Revision$, $Date$
 */
public class MonitorIntervallListener implements ActionListener {

    //~ Instance fields --------------------------------------------------------

    /** Referenz auf RegistryMonitor.* */
    protected RegistryMonitor registryMonitor;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new MonitorIntervallListener object.
     *
     * @param  registryMonitor  DOCUMENT ME!
     */
    public MonitorIntervallListener(final RegistryMonitor registryMonitor) {
        this.registryMonitor = registryMonitor;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void actionPerformed(final ActionEvent event) {
        final String command = event.getActionCommand();
        if (command.equals("all 1 Minute")) {  // NOI18N
            registryMonitor.setUpdateIntervall(60);
        } else if (command.equals("all 5 Minutes")) {  // NOI18N
            registryMonitor.setUpdateIntervall(300);
        } else if (command.equals("all 10 Minutes")) {  // NOI18N
            registryMonitor.setUpdateIntervall(600);
        }
    }
}
