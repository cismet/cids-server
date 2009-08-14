/*
 * BasicTableExtractor.java
 *
 * Created on 28. April 2003, 15:54
 */

package Sirius.util.DBVersionChecker;

import java.util.*;

/**
 * Sollte so implementiert werden dass die Struktur der Datenbank, als eine
 * bestimte Datenmodelle-Version identifiziert werden kann. Die Struktur der 
 * Datenbank sollte im Konstruktor \u00FCbergeben werden. Die Datenmodell-Versionen 
 * werden in einem konsistenten Datenmodell-Speichermodul gehalten und k\u00F6nnen
 * daraus extrahiert werden. Die Version wird durch einen String identifiziert.
 * 
 * @author  awindholz
 */
public interface VersionChecker {
    
    /**
     * Vergleicht nacheinander die vorhandenen Datenmodel-Versionen mit der 
     * Struktur der Datenbank, dessen Version festgestellt werden soll. Wird 
     * eine passende Version gefunden, so wird die Suche abgebrochen und der 
     * zugeh\u00F6rige Name der Datenmodel-Version wird geliefert.
     *
     * @return wenn Versionsckeck erfolgreich: der Name der Datenmodell-Version,
     * sonst null.
     * 
     * @throws wenn beim checken zu einem Fehler kommt.
     */
    public String checkVersion() throws DBVersionException;

    /**
     * Liefert alle Datenmodel-Versionen die in dem Datenmodell-Speichermodul
     * enthalten sind.
     *
     * @return Bezeichnungen der Datenmodel-Versionenen.
     *
     * @throws wenn es zu einem Fehler kommt.
     */
    public String[] getAllVersions() throws DBVersionException;
    
    /**
     * Vergleicht ob das Datenmodell einer bestimten Version entspricht.
     *
     * @param der Versionsname.
     *
     * @return true wenn das Datenmodel die Struktur der \u00FCbergebene Version 
     * besitzt.
     * 
     * @throws wenn beim checken zu einem Fehler kommt.
     */
    public boolean compareWithVersion(String version) throws DBVersionException;
    
    /**
     * Pr\u00FCft ob eine bestimmte Version in dem Datenmodell-Speichermodul 
     * enthalten ist.
     *
     * @param der Versionsname.
     *
     * @return true wenn die gesuchte Version in dem Datenmodell-Speichermodul
     * enthalten ist, sonst false.
     * 
     * @throws wenn es zu einem Fehler kommt.
     */
    public boolean versionAvailable(String version) throws DBVersionException;
}
