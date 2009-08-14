/*
 * MetaObjectProtoMgr.java
 *
 * Created on 8. September 2003, 13:08
 */

package Sirius.server.dataretrieval;

import java.util.*;
import Sirius.server.middleware.types.*;
import Sirius.server.middleware.types.MOTraverse.*;

/**
 * Diese Klasse verwalten die Protokole die Meta-Objekte verarbeiten k\u00F6nnen um
 * Data-Objekte aufzufinden. Die Verwendung ist \u00E4quivalent zu dem Ladekonzept
 * der JDBC-Treiber durch DriverManager-Klasse.<br>
 *<br>
 * Class.forName(<Implementation des MetaObjectProto>);<br>
 * MetaObjectProto mop = MetaObjectProtoMgr.getProtocol(<URL-String>);<br>
 *  mit<br>
 * <Implementation des MetaObjectProto>: Vollqualifizierter Name der Klasse die
 * das Interface MetaObjectProto implementiert.<br>
 * <URL-String>: URL-String das den Protokol enth\u00E4lt welcher verwendet werden
 * soll.<br>
 *
 * @author  awindholz
 */
public class MetaObjectProtoMgr {
    
    private static Hashtable protocolMap = new Hashtable(5,5);
    //    private static Vector protocols = new Vector(5,5);
    
    /**
     * Registriert den \u00FCbergebenen MetaObjectProto mit seiner bezeichnung, die
     * durch Aufruf der Funktion getDataSourceClass() ermittelt wird. Die
     * Protokolbezeichnung wird in keleinbuchstaben umgewandelt damit die
     * Protokolnamen caseinsensitiv behandelt werden k\u00F6nnen.
     *
     * @param mop das Protokol das registriert werden soll.
     *
     * @return falls ein gleichnamige Protokol bereits enthalten war, wird dieser
     * zur\u00FCckgegeben, sonst null.
     */
    public static MetaObjectProto register(MetaObjectProto mop) {
        String protoName = mop.getDataSourceClass().trim().toLowerCase();
        
        return (MetaObjectProto)protocolMap.put(protoName, mop);
    }
    
    /**
     * Deregistriert ein Protokol.
     *
     * @param das zu entfernende Protokol.
     *
     * @return das entfernte Protokol.
     */
    public static MetaObjectProto deregister(MetaObjectProto mop) {
        return (MetaObjectProto)protocolMap.remove(mop.getDataSourceClass().trim().toLowerCase());
    }
    
    /**
     * Liefert den implements Manager registrierten Protokol anhand dessen namen.
     * Wenn keiner gefunden wurde wird null geliefert. Der eingabeparameter
     * wird zun\u00E4chst in kleinbuchstaben umgewandelt.
     *
     * @param Name des Protokols dass ermittelt werden soll. Angaben werden
     * caseinsensitiv behandelt.
     *
     * @return der zugeh\u00F6rige Protokol oder null wenn keiner gefunden.
     */
    public static MetaObjectProto getProtocol(String dataSourceClass) {
        return (MetaObjectProto)protocolMap.get(dataSourceClass.trim().toLowerCase());
    }
    
    public static MetaObjectProto getProtocol(MetaObject metaObject) throws DataRetrievalException {
        
        String protoName = new ProtoDelegator().extractProtoName(metaObject);
        
        return MetaObjectProtoMgr.getProtocol(protoName);
    }
} // class end

////////////////////////////////////////////////////////////////////////////////
/*
    public static boolean register(MetaObjectProto mop) {
                                if(protocols.contains(mop))
                                        return false;
 
        protocols.add(mop);
 
        return true;
    }
 
 
    /**
 * Durchl\u00E4uft alle registrierten MetaObjectProto-Objekte und liefert einen
 * davon dass den Protokol unterst\u00FCtzt das im \u00FCbergebenen String beschrieben
 * wird. Wenn keiner gefunden wurde wird null geliefert.
 */
/*    public static MetaObjectProto getProtocol(String url) {
        MetaObjectProto mop;
 
        Enumeration e = protocols.elements();
 
        while(e.hasMoreElements()) {
            mop = (MetaObjectProto)e.nextElement();
            if(mop.acceptsURL(url)) return mop;
        }
 
        return null;
    }
 
    public static boolean deregister(MetaObjectProto mop) {
        return protocols.removeElement(mop);
    }
 */
