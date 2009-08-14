/*******************************************************************************

 	Copyright (c)	:	EIG (Environmental Informatics Group) 
						<br> http://www.htw-saarland.de/eig
						<br> Prof. Dr. Reiner Guettler 
						<br> Prof. Dr. Ralf Denzer 
 						
						<br> HTWdS 
						<br> Hochschule fuer Technik und Wirtschaft des Saarlandes
						<br> Goebenstr. 40 
 						<br> 66117 Saarbruecken 
 						<br> Germany 

	Programmers		:	Bernd Kiefer 
	<br>
 	Project			:	WuNDA 2 
	Version			:	1.0 
 	Purpose			:	
	Created			:	
	History			: 

*******************************************************************************/
package Sirius.server.registry.events;

import java.awt.event.*;
import Sirius.server.registry.*;
import Sirius.server.registry.monitor.*;


/** ActionListener fuer die Einstellungen fuer das automatische Update des
    RegistryMonitor.
	  Hier wird das ZeitIntervall fuer die Update-Schleife gesetzt. **/
public class MonitorIntervallListener implements ActionListener
{
 /** Referenz auf RegistryMonitor **/
 protected RegistryMonitor registryMonitor;
	
	
 public MonitorIntervallListener(RegistryMonitor registryMonitor)
 {
	this.registryMonitor = registryMonitor;
 }	
	
 public void actionPerformed(ActionEvent event)
 {
		String command = event.getActionCommand();
		if (command.equals("all 1 Minute"))
			registryMonitor.setUpdateIntervall(60);
		else if (command.equals("all 5 Minutes"))
			registryMonitor.setUpdateIntervall(300);
		else if (command.equals("all 10 Minutes"))
			registryMonitor.setUpdateIntervall(600);
 }
}
