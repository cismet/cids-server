package Sirius.server.registry.monitor;

import Sirius.server.newuser.*;

import javax.swing.table.*;
import java.util.*;
import Sirius.server.*;

	
	
public class MonitorTableModel extends DefaultTableModel
{
 public MonitorTableModel(java.lang.Object[][] servers,java.lang.Object[] cnames)
 { super(servers,cnames); }

 public MonitorTableModel()
 { super(); }

 public boolean isCellEditable(int row, int column)
 {return false;}
	
	
 public static java.lang.Object[][] convertToMatrix(Server[] servers)
 {
	java.lang.Object[][] matrix = new java.lang.Object[servers.length][];
	for(int i =0; i<servers.length;i++)
	{
		java.lang.Object[] columnVals= new java.lang.Object[3];
		columnVals[0] = servers[i].getName();
		columnVals[1] = servers[i].getIP();
		columnVals[2] = servers[i].getPort();
		matrix[i] = columnVals;
 	}
 return matrix;
}


public static java.lang.Object[][] convertToMatrix(Vector users)
{
	java.lang.Object[][] matrix = new java.lang.Object[users.size()][];
	for(int i =0; i<users.size(); i++)
	{
		java.lang.Object[] columnVals = new java.lang.Object[6];
		
		columnVals[0] = new Integer(0);//new Integer(((User)users.get(i)).getID());
		columnVals[1] = ((User)users.get(i)).getName();
		columnVals[2] = ((User)users.get(i)).getDomain();
		columnVals[3] = ((User)users.get(i)).getUserGroup();
		columnVals[4] = new Boolean(((User)users.get(i)).isValid());
		columnVals[5] = new Boolean(((User)users.get(i)).isAdmin());
		matrix[i] = columnVals;
	}
	return matrix;
}
		
		
	
 
 public static java.util.Vector convertToVector(java.lang.Object obs[])
 {
	return DefaultTableModel.convertToVector(obs);
 }
}
