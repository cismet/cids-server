/*
 * AllAttributesToStringConverter.java
 *
 * Created on 20. M\u00E4rz 2007, 15:41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package de.cismet.cids.tools.tostring;

/**
 *
 * @author VerkennisR
 */
import Sirius.server.localserver.attribute.ObjectAttribute;
import Sirius.server.middleware.types.MetaObject;
import java.io.Serializable;
import java.util.HashMap;

// Referenced classes of package de.cismet.cids.tools.tostring:
//            ToStringConverter

public class AllAttributesToStringConverter extends ToStringConverter
        implements Serializable {
    
    public  String convert(Sirius.server.localserver.object.Object o,HashMap classes) {
        String stringRepresentation="";
        ObjectAttribute[] attrs = o.getAttribs();
        for(int i = 0; i< attrs.length;i++) {
            if(!attrs[i].referencesObject())
                stringRepresentation+=(attrs[i].toString()+ " ");
            else
                stringRepresentation+= ( ( (MetaObject)attrs[i].getValue()).toString(classes) + " " );
        }
        return stringRepresentation;
    }
    
    public  String convert(de.cismet.cids.tools.tostring.StringConvertable o) {
        String stringRepresentation="";
        if(o instanceof Sirius.server.localserver.object.Object) {
            ObjectAttribute[] attrs = ((Sirius.server.localserver.object.Object)o).getAttribs();
            for(int i = 0; i< attrs.length;i++) {
                stringRepresentation+=( attrs[i].toString() + " ");
            }
        } else if(o instanceof Sirius.server.localserver.attribute.ObjectAttribute) {
            return ((ObjectAttribute)o).getValue().toString();
        }
        return stringRepresentation;
    }
}
