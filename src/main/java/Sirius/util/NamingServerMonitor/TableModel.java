//------------------------------------------------------------------------------
//
// Project   : NamingServerMonitor
// File name : TableModel.java
// Author    : Rene Wendling
// Date      : 24.10.2000
//
//------------------------------------------------------------------------------
//

package Sirius.util.NamingServerMonitor;

import javax.swing.table.*;
import java.util.*;

	
	
public class TableModel extends DefaultTableModel
{
 public TableModel(java.lang.Object[][] bounds,java.lang.Object[] cnames)
 { super(bounds,cnames); }

 public TableModel()
 { super(); }

 public boolean isCellEditable(int row, int column)
 {return false;}
	
	
 public static java.lang.Object[][] convertToMatrix(String[] bounds)
 {
	java.lang.Object[][] matrix = new java.lang.Object[bounds.length][];
	for(int i =0; i<bounds.length;i++)
	{
		java.lang.Object[] columnVals= new java.lang.Object[1];
		columnVals[0] = bounds[i];
		
		matrix[i] = columnVals;
 	}
 return matrix;
}



		
		
	
 
 public static java.util.Vector convertToVector(java.lang.Object obs[])
 {
	return DefaultTableModel.convertToVector(obs);
 }
}

