package Sirius.util.image;


public class IntMapsImage extends java.util.Hashtable
{


		public IntMapsImage()
		{super();}


		public IntMapsImage(int initialCapacity, float loadFactor)
		{super(initialCapacity,loadFactor);}

		public void add(int id,Image aImage)
		{
			super.put(new Integer(id),aImage);
		}// end add


		public Image getImageValue(int id) throws Exception
		{
		Integer key = new Integer(id);

			if(super.containsKey(key))
			{
			java.lang.Object candidate = super.get(key);

			   if (candidate instanceof Image)
			   return ((Image) candidate);

			throw new java.lang.NullPointerException("Entry is not a Image:" + id);
			}// endif

		throw new java.lang.NullPointerException("No entry :"+ id);
		}

		/////// containsIntKey/////////////////////////////////
		public boolean containsIntKey(int key)
		{return super.containsKey(new Integer(key));}


}