/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * BasicTableExtractor.java
 *
 * Created on 28. April 2003, 15:54
 */
package Sirius.util.DBVersionChecker;

import java.util.*;

/**
 * Sollte so implementiert werden dass die Struktur der Datenbank, als eine bestimte Datenmodelle-Version identifiziert
 * werden kann. Die Struktur der Datenbank sollte im Konstruktor \u00FCbergeben werden. Die Datenmodell-Versionen werden
 * in einem konsistenten Datenmodell-Speichermodul gehalten und k\u00F6nnen daraus extrahiert werden. Die Version wird
 * durch einen String identifiziert.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public interface VersionChecker {

    //~ Methods ----------------------------------------------------------------

    /**
     * Vergleicht nacheinander die vorhandenen Datenmodel-Versionen mit der Struktur der Datenbank, dessen Version
     * festgestellt werden soll. Wird eine passende Version gefunden, so wird die Suche abgebrochen und der
     * zugeh\u00F6rige Name der Datenmodel-Version wird geliefert.
     *
     * @return  wenn Versionsckeck erfolgreich: der Name der Datenmodell-Version, sonst null.
     *
     * @throws  DBVersionException  wenn beim checken zu einem Fehler kommt.
     */
    String checkVersion() throws DBVersionException;

    /**
     * Liefert alle Datenmodel-Versionen die in dem Datenmodell-Speichermodul enthalten sind.
     *
     * @return  Bezeichnungen der Datenmodel-Versionenen.
     *
     * @throws  DBVersionException  wenn es zu einem Fehler kommt.
     */
    String[] getAllVersions() throws DBVersionException;

    /**
     * Vergleicht ob das Datenmodell einer bestimten Version entspricht.
     *
     * @param   version  der Versionsname.
     *
     * @return  true wenn das Datenmodel die Struktur der \u00FCbergebene Version besitzt.
     *
     * @throws  DBVersionException  wenn beim checken zu einem Fehler kommt.
     */
    boolean compareWithVersion(String version) throws DBVersionException;

    /**
     * Pr\u00FCft ob eine bestimmte Version in dem Datenmodell-Speichermodul enthalten ist.
     *
     * @param   version  der Versionsname.
     *
     * @return  true wenn die gesuchte Version in dem Datenmodell-Speichermodul enthalten ist, sonst false.
     *
     * @throws  DBVersionException  wenn es zu einem Fehler kommt.
     */
    boolean versionAvailable(String version) throws DBVersionException;
}
