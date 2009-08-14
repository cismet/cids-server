//------------------------------------------------------------------------------
//
// Project   : NamingServerMonitor
// File name : JamingServerMonitor.java
// Author    : Rene Wendling
// Date      : 24.10.2000
//
//------------------------------------------------------------------------------
//

package Sirius.util.NamingServerMonitor;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

//import NamingServerMonitor.*;


public class NamingServerMonitor extends JPanel implements Runnable, ActionListener
{
	/** Referenz auf den PortScan  **/
	private PortScan portscan;
	
	private  java.lang.Object[] columnnamesForBounds={"URL"};
	/** Tabelle  **/
	private JTable boundsTable;

	/** zeigt eventuell auftretende Fehlermeldungen an **/
	private JLabel messageLabel;

	/** Wert fuer das UpdateIntervall **/
	private int updateIntervall = 60;

	/** Ueberschrift fuer den CentralServerMonitor **/
	private String panelHeader;
	
	private JTextField enterHost;
	
	public String[] bounds;
	
	private java.rmi.registry.Registry rmiRegistry;
	
	public NamingServerMonitor monitor;
	
	public static String host;
	
	//----------------------------------------------------------------------------------------
	/** Konstruktor
	     **/
	public NamingServerMonitor ()
	{
		if(host != null)
		{
			try
			{
			
				portscan = new PortScan(enterHost.getText());
			
				//java.rmi.registry.Registry rmiRegistry;
			
				System.out.println("\u00FCbermittelter Port: " + portscan.getPort());
			
				rmiRegistry = LocateRegistry.getRegistry(enterHost.getText(), portscan.getPort());
				bounds = new String[rmiRegistry.list().length];
				bounds = rmiRegistry.list();
			

				//update();
			}
			catch(Exception e)
			{
				messageLabel.setForeground(Color.red);
				message("Registry not on Port: " + portscan.getPort());
			}
		
				System.out.println("L\u00E4nge: " + bounds.length);
		
				for (int i=0; i<bounds.length; i++)
					System.out.println("Inhalt: " + bounds[i]);
		
				initMainPanel();
			}
		
		else
		{
			bounds = new String[0];
			initMainPanel();
		}
	
	 
	}
//----------------------------------------------------------------------------------------

//----------------------------------------------------------------------------------------

	/** Funktion zum initialisieren des Layouts **/
	private void initMainPanel()
	{
	
	//	JTable boundsTable = new JTable(bounds[], columnnamesForBounds);
		boundsTable 		 = new JTable(new TableModel(TableModel.convertToMatrix(bounds),columnnamesForBounds));
		
		// JTabbedPane erzeugen und Tabellen hinzufuegen
		JTabbedPane boundsPanel = new JTabbedPane();
	  boundsPanel.add("bounds", new JScrollPane(boundsTable));
		
		messageLabel = new JLabel();

		// UpdateButton fuer manuelles Update
		JButton updateButton = new JButton("update");
		updateButton.setActionCommand("update");
		updateButton.addActionListener (new UpdateListener(this));
		//updateButton.addActionListener(new MonitorUpdateListener());
		
		
		
		//Panel f\u00FCr timePanel und hostPanel
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());
		
		

		// Panel fuer updateIntervall Einstellungen
		JPanel timePanel = new JPanel();
		timePanel.setBorder(BorderFactory.createTitledBorder("Intervall for automatical update"));
		
		ButtonGroup buttonGroup = new ButtonGroup();
		JRadioButton oneMin = (new JRadioButton("all 1 Minute"));
		oneMin.setActionCommand("all 1 Minute");
		oneMin.addActionListener (this);
		JRadioButton fiveMin = new JRadioButton("all 5 Minutes");
		fiveMin.setActionCommand("all 5 Minutes");
		oneMin.addActionListener (this);
	  JRadioButton tenMin = new JRadioButton("all 10 Minutes");
		tenMin.setActionCommand("all 10 Minutes");
		oneMin.addActionListener (this);
		
		timePanel.add(oneMin);
		timePanel.add(fiveMin);
		timePanel.add(tenMin);
		oneMin.setSelected(true);
		setUpdateIntervall(60);

		buttonGroup.add(oneMin); 
		buttonGroup.add(fiveMin);
		buttonGroup.add(tenMin);	
		
		
		JPanel hostPanel = new JPanel();
		hostPanel.setLayout(new BorderLayout());
		enterHost = new JTextField();
		hostPanel.setBorder(BorderFactory.createTitledBorder("Enter IP - Address"));
	
		
		hostPanel.add(enterHost, BorderLayout.CENTER);
		
		containerPanel.add(hostPanel, BorderLayout.NORTH);
		containerPanel.add(timePanel, BorderLayout.SOUTH);

	
		// MessageLabel und MessagePanel
		JPanel messagePanel = new JPanel();
		messagePanel.setBorder(BorderFactory.createTitledBorder("Messages"));
		messagePanel.add(messageLabel);

		JPanel buttonAndMessagePanel = new JPanel();
		buttonAndMessagePanel.setLayout(new BorderLayout());
		buttonAndMessagePanel.add(updateButton, BorderLayout.NORTH);
		buttonAndMessagePanel.add(messagePanel, BorderLayout.CENTER);
		
		// HauptPaneleistellungen
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createTitledBorder(panelHeader));
	  add(containerPanel, BorderLayout.NORTH);
		add(boundsPanel, BorderLayout.CENTER);
		add(buttonAndMessagePanel, BorderLayout.SOUTH);
		//add(hostPanel, BorderLayout.WEST);
	}
	
//----------------------------------------------------------------------------------------
	public void setUpdateIntervall(int intervall) { this.updateIntervall = intervall; }
//----------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------
	public void message(String message) { messageLabel.setText(message); }
//----------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------
	public  void setHost(String host) { host = host; }
//----------------------------------------------------------------------------------------
//----------------------------------------------------------------------------------------
	public  String getHost() { return host; }
//----------------------------------------------------------------------------------------


	/** 	**/
	private void update()
	{
	 try
	 {
	  portscan = new PortScan(enterHost.getText());
		
		rmiRegistry = LocateRegistry.getRegistry(enterHost.getText(), portscan.getPort());
		bounds = new String[rmiRegistry.list().length];
		bounds = rmiRegistry.list();
		}
	 catch(Exception e)
	 {
		messageLabel.setForeground(Color.red);
		message("Registry not on Port: " + portscan.getPort());
	 }
	}
//----------------------------------------------------------------------------------------

public void updateTables()
{
	update();
	try
	{
		TableModel tmboundsTable = (TableModel)boundsTable.getModel();
		
		tmboundsTable.setDataVector(TableModel.convertToMatrix(bounds),columnnamesForBounds);
			
		
		message("last update: " + (new Date(System.currentTimeMillis())));
	}
	catch (IllegalArgumentException e)
	{
		boundsTable.setModel(new TableModel());
	}
}

//----------------------------------------------------------------------------------------
	/** Schleife zum automatischen aktualisieren der Tabellen, Intervall kann ueber Variable
	    updateIntervall gesetzt werden. **/
	public void run()
	{
	 while (true)
		{
		 try
		 {
		  Thread t = Thread.currentThread();
			messageLabel.setForeground(new Color(102,102,153));
			message("last update: "+(new Date(System.currentTimeMillis() ) ) );
			t.sleep(updateIntervall*1000);
			updateTables();
		 }
		 catch (InterruptedException e) {}
		}
	}
//----------------------------------------------------------------------------------------
	
	/** MainFunktion zum testen des CentralServerMonitor **/
	public static void main(String args[])
	{
		//setHost("134.96.158.158");
		NamingServerMonitor monitor = new NamingServerMonitor();
		
		JFrame frame = new JFrame("NamingServerMonitor");

		frame.getContentPane().add(monitor);
		frame.setSize(400,400);
		frame.setVisible(true);
	}

//----------------------------------------------------------------------------------------
public void actionPerformed(ActionEvent event)
	{
		String command = event.getActionCommand();
		if (command.equals("all 1 Minute"))
			setUpdateIntervall(60);
		else if (command.equals("all 5 Minutes"))
			setUpdateIntervall(300);
		else if (command.equals("all 10 Minutes"))
			setUpdateIntervall(600);
	}



//----------------------------------------------------------------------------------------
public class UpdateListener implements ActionListener
{
 protected NamingServerMonitor monitor;
	
 public UpdateListener(NamingServerMonitor monitor)
 {
	this.monitor = monitor;
 }
	
 public void actionPerformed(ActionEvent event)
 {
	monitor.updateTables();
 }
}
//----------------------------------------------------------------------------------------
}



