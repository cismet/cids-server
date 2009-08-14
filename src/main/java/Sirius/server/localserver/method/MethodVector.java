package Sirius.server.localserver.method;


import java.util.*;
import java.util.Vector;



////////// Class MethodVector//////////////////////////////////////

public class MethodVector extends java.util.Vector
{
//constructors
public MethodVector(){super();}
public MethodVector(int initialCapacity){super(initialCapacity);}
public MethodVector(int initialCapacity, int capacityIncrement){super(initialCapacity,capacityIncrement);}
public MethodVector(Collection c){super(c);}

////////////////////methods/////////////////////////////////////////

public Method at(int index) throws Exception
{
if (size() > index)
 {
 java.lang.Object method = super.get(index);
 if (method instanceof Method)
    return (Method) method;
 throw new  java.lang.NullPointerException();
}
throw new java.lang.IndexOutOfBoundsException();
}

public Method[] convertToArray()
{
	return(Method[])toArray(new Method[size()]);

}// end of convertToArray


}// end of class AttributeVector
