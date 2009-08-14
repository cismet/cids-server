package Sirius.server.localserver.attribute;

import java.util.*;
import java.util.Vector;


 
////////// Class AttributeVector//////////////////////////////////////

public class AttributeVector extends java.util.Vector implements java.io.Serializable
{
//constructors
public AttributeVector(){super();}
public AttributeVector(int initialCapacity){super(initialCapacity);}
public AttributeVector(int initialCapacity, int capacityIncrement){super(initialCapacity,capacityIncrement);}
public AttributeVector(Collection c){super(c);}

////////////////////methods/////////////////////////////////////////

public Attribute at(int index) throws Exception
{
if (size() > index)
 {
 java.lang.Object attrib = super.get(index);
 if (attrib instanceof Attribute)
    return (Attribute) attrib;
 throw new  java.lang.NullPointerException();
}
throw new java.lang.IndexOutOfBoundsException();
}

///////// converts to LinkArray/////////////////////
public Attribute[] convertToArray()
{

 return   (Attribute[]) toArray(new Attribute[size()]);

}// end of convertToArray


public java.lang.Object clone()
{
	return new AttributeVector((Vector)super.clone());

}

}// end of class AttributeVector

