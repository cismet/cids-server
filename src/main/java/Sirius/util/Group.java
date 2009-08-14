package Sirius.util;

public class Group
{
	private String group;
	private int[] items;



	public Group(String group,Groupable[] items)
	{
		this.group = group;
		this.items = getIDs(items);

	}

//-------------------------------------------------------------------------------

	private final int[] getIDs(Groupable[] items)
	{


		int[] ids = new int[items.length];

		//extract ids from groupables
		for(int i =0;i<ids.length;i++)
		{
			ids[i] = items[i].getId();

		}

		return ids;

	}

//------------------------------------------------------------------------------------


public final String getGroup(){return group;}

public final int[] getIDs(){return items;}




}
