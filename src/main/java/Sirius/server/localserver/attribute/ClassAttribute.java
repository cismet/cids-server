package Sirius.server.localserver.attribute;

import Sirius.server.newuser.permission.Policy;


public class ClassAttribute extends Attribute implements java.io.Serializable
{
/////////////////members//////////////////////////////////////////

protected int classID;

protected int typeID;

//protected String fieldName;

//protected int foreignKeyClassID;


 

///////////////constructor///////////////////////////////////////

public ClassAttribute(String id,  int classID, String name, int typeID,Policy policy)
{
        super(id,name,"",policy);
        this.classID = classID;
        super.visible =true;
        this.typeID=typeID;
}



/////////////////////////methods///////////////////////////////////

public final int getClassID(){return classID;}

/** Getter for property fieldName.
 * @return Value of property fieldName.
 *
 */
//public final java.lang.String getFieldName() {
//    return fieldName;
//}
//
///** Setter for property fieldName.
// * @param fieldName New value of property fieldName.
// *
// */
//public final void setFieldName(java.lang.String fieldName) {
//    this.fieldName = fieldName;
//}
//
///** Getter for property foreignKeyClassID.
// * @return Value of property foreignKeyClassID.
// *
// */
//public final int getForeignKeyClassID() {
//    return foreignKeyClassID;
//}
//
///** Setter for property foreignKeyClassID.
// * @param foreignKeyClassID New value of property foreignKeyClassID.
// *
// */
//public final void setForeignKeyClassID(int foreignKeyClassID) {
//    this.foreignKeyClassID = foreignKeyClassID;
//}

/** Getter for property typeID.
 * @return Value of property typeID.
 *
 */
public final int getTypeID() {
    return typeID;
}

/** Setter for property typeID.
 * @param typeID New value of property typeID.
 *
 */
public final void setTypeID(int typeID) {
    this.typeID = typeID;
}

}