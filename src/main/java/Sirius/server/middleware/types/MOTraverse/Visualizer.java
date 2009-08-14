/*
 * AttrForName.java
 *
 * Created on 15. September 2003, 14:35
 */

package Sirius.server.naming.middleware.types.MOTraverse;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;

/**
 * Sucht rekursiv in MetaObject's nach allen Attributen es wird nicht auf
 * Object-Name oder Attributname geachtet. Diese Klasse ermittelt ausschlieslich
 * die ObjectAttribute, MetaObject's werden nicht als ObjectAttribute erkannt sondern
 * als ein beh\u00E4lter f\u00FCr weitere ObjectAttributen.
 *
 * @author  awindholz
 */
public class Visualizer implements TypeVisitor {
    
    private int step = 0;
    private final String STEP_LENGTH = "   ";
    
    private final static String NEW_LINE = System.getProperty("line.separator");
    
    private String visualized = "";
    
    /** Creates a new instance of AttrForName */
    public Visualizer() {
    }
    
    /**
     * Liefert diesen Attribut oder wenn dieses Attribut einen MetaObject als
     * Wert enth\u00E4lt dann wird dieses besucht(visitMO(...)).
     *
     * @param moa Das MetaAttribut das besucht wird.
     * @param wird nicht verwendet.
     *
     * @return liefert diesen Attribut in einem Array der gr\u00F6se 1 zur\u00FCck oder
     * wenn der Attribut einen MetaObject als Wert enth\u00E4lt dann alle darin
     * enthaltenen Attribute.
     */
    public Object visitMA(ObjectAttribute moa, Object o) {
        
        Object value = moa.getValue();
        
        visualized += getPrefix() + STEP_LENGTH + moa.getName();
        
        if( value instanceof MetaObject) {
            
            ((MetaObject)value).accept(this, o);
            
        } else {
            visualized += ": " + value + NEW_LINE;
        }
        
        
        return visualized;
    }
    
    /**
     * Liefert alle Attribute in diesem und allen darunterliegenden MetaObject's.
     *
     * @param moa Das MetaObject das besucht wird.
     * @param wird nicht verwendet.
     *
     * @return liefert alle in diesem und allen darunterliegenden MetaObjecten
     * enthaltenen Attribute.
     */
    public Object visitMO(MetaObject moa, Object o) {
        
        step++;
        
        try {
            visualized += "-->/MetaObject " + moa.getName() + "/" + NEW_LINE;
        } catch (NullPointerException e) {
            visualized += "-->/MetaObject/" + NEW_LINE;
        }
        
        ObjectAttribute[] mas = moa.getAttribs();
        
        for(int i = 0; i < mas.length; i++) {
            mas[i].accept(this, o);
        }
        
        step--;
        
        return visualized;
    }
    
    private String getPrefix() {
        String ret = "";
        
        for(int i = 0; i < step; i++)
            ret += STEP_LENGTH;
        
        return ret;
    }
}