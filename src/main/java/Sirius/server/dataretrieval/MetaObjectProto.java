/*
 * MetaObjectProto.java
 *
 * Created on 8. September 2003, 13:23
 */

package Sirius.server.dataretrieval;

import Sirius.server.middleware.types.*;

/**
 * Dieses Interface bildet eine Schnittstelle zum Implementieren der Protokole 
 * die Meta-Objekte verarbeiten k\u00F6nnen um Data-Objekte aufzufinden. Die 
 * implementierende Klasse wird im MetaObjectProtoMgr registriert.
 *
 * @author  awindholz
 */
public interface MetaObjectProto {
    
    /**
     * Sollte so implementiert werden dass es den \u00FCbergebenen MetaObject
     * verwendet um alle ben\u00F6tigten Metadaten zu ermitteln, anhand diesser
     * sollte eine Datenquelle auf passende Weise angesprochen und da raus die 
     * Daten abgefragt werden. Anschliessend Sollten die Daten serialisiert und
     * in einem DataObject zusammen mit Beschreibung der Daten zur\u00FCckgegeben werden.
     */
    public DataObject getDataObject(MetaObject metaDataObject)
    throws DataRetrievalException;
    
   /**
    * Sollte den String lefern mit dem diese Klasse bei dem MetaObjektProtoMgr
    * registriert werden soll. Case-sensitivit\u00E4t wird nicht beachtet da im
    * MetaObjektProtoMgr der string zu einem Lower-case String konvertiert wird.
    *
    * @return bezeichnung dieses Protokols.
    */
   public String getDataSourceClass();

} // class end
//////////////////////////// SENSE ///////////////////////////////////////
    /*
     * Sollte so implementiert werden dass es erkennt ob es sich bei dem 
     * \u00FCbergebenem Parameter um instantion der selben Klasse handelt. Diese 
     * Fkt wird ben\u00F6tigt um korrekt deregistrierung durchf\u00FChren zu k\u00F6nnen.<br>
     * Z.B: return (o instanceof JDBCProto);
     *
     * @param o Objekt das verglichen werden soll.
     *
     * @return true wenn o eine instantion der implementierenden Klasse ist.
     */
//    public boolean equals(Object o);

    /*
     * Sollte so implementier werden dass es den \u00FCbergebenen String nach 
     * Protokol \u00FCberpr\u00FCft und entscheidet ob diese Klasse dieses Protokol 
     * verarbeiten kann.
     *
     * @param String url URL die (u.a.) das Protokol-Bezeichnung enth\u00E4lt f\u00FCr 
     * welchen ein Protokol gesucht wird.
     *
     * @return frue wenn im String \u00FCbergebene Protokol unterst\u00FCtzt wird, sonst
     * false.
     */
//    public boolean acceptsURL(String url);
