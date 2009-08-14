/*
 * TypeVisitor.java
 *
 * Created on 15. September 2003, 14:21
 */
package Sirius.server.middleware.types;

import Sirius.server.localserver.attribute.*;

/**
 * Deffiniert eine Schnittstelle zum Traversieren der Metatypen MetaObject und
 * MetaAttribute. 
 *
 * @author  awindholz
 */
public interface TypeVisitor {
  
    /**
     * Implementation dieser Fkt wird in der Klasse MetaObject aufgerufen wenn
     * die MetaObject.accept(...)-Funktion aufgerufen wird.
     *
     * @param moa Das MetaObject das besucht wird.
     * @param o implementationsabh\u00E4ngige Parameter. 
     *
     * @return  implementationsabh\u00E4ngige Returnwert. 
     */
    public Object visitMO(MetaObject moa, Object o);
    
    /**
     * Implementation dieser Fkt wird in der Klasse MetaAttribute aufgerufen wenn
     * die MetaAttribute.accept(...)-Funktion aufgerufen wird.
     *
     * @param moa Das MetaAttribute das besucht wird.
     * @param o implementationsabh\u00E4ngige Parameter. 
     *
     * @return  implementationsabh\u00E4ngige Returnwert. 
     */
    public Object visitMA(ObjectAttribute moa, Object o);
}
