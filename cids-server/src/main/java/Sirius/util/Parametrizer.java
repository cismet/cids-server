/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.util;

import Sirius.server.dataretrieval.DataRetrievalException;
import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;
import Sirius.server.middleware.types.MOTraverse.*;

import org.apache.log4j.*;

import java.sql.*;

import java.util.*;

/**
 * Parametrisiert ein String anhand der Daten aus der MetaObject-Klasse. Trennzeichen: "{" und " }"<BR>
 * Grundform einer Parameterangabe:<br>
 * { name = [MetaObjectName.]Attributname } { id = MetaObjectName.Attributid } Wenn kein MetaObjectName angegeben wird
 * (nur f\u00FCr Name m\u00F6glich) werden alle Metaobjekte nach dem Attribut durchsucht.
 *
 * <p>Bsp 1: Select * from user where id = {name = userID}<BR>
 * dabei wird der String "{name = userID}" als ein Platzhalter erkannt und nach dem Abgranzungszeichen "=" in zwei Teile
 * gespalten. das erste Element(name) sagt aus dass das Attribut \u00FCber seinen Namen angesprochen werden soll. Der
 * Name des Attributes ist dann im 2-ten Element enthalten. Da der Attribut nicht qualifiziert wurde, wird nach allen
 * gleichnamigen ObjectAttribute aus allen MetaObjekten gesucht. Wird der Attribut gefunden, so wird sein Wert abgefragt
 * und im String an stelle des zugeh\u00F6rigen eingeklammerten Ausdruckes gesetzt.<br>
 * Wird z.B. ersetzt durch<BR>
 * </p>
 *
 * <p>Bsp 2: oder \u00E4quivalent zu oben nur mit dem Unterschied dass das Attr nicht \u00FCber seinen Namen sondern
 * seine id angesprochen wird: Select * from user where id = {id = Dataservice.7}<BR>
 * Dabei ist zu beachten dass in diesem Fall der Attributname qualifiziert sein muss.</p>
 *
 * <p>Beim Ansprechen \u00FCber Namen kann es vorkommen dass mehrere Attribute gefunden werden. In dem Fall wird der
 * Platzhalter in folgender Form ersetzt: IN (<param 1>, <param 2>,...,</p>
 * <object>
 *   <param n>), dabei wird ein im String evt vorhandenes "=" zeichen ersetzt. z.B: Annahme: Attribut mit dem Namen
 *   userID ist 2 mal vorhanden. select * from usr where id = {name = userID} ergebnis: select * from usr where id IN
 *   (123, 456)
 * </object>
 *
 * @author   Sascha Schlobinski
 * @version  1.0 erstellt am 06.10.1999
 * @since    DOCUMENT ME!
 */
public class Parametrizer {

    //~ Static fields/initializers ---------------------------------------------

    private static Logger logger = Logger.getLogger(Parametrizer.class);
    private static final char auf = '{';
    private static final char zu = '}';

    //~ Methods ----------------------------------------------------------------

    /**
     * Ersetzt die im String enthaltenen Platzhalter durch Attributwerte die sich in dem \u00FCbergebenem MetaObject
     * befindet. Dabei werden verscheidene Suchm\u00F6glichkeiten unterst\u00FCtzt: Die Suchm\u00F6glichkeiten werden
     * durch Parameter beeinfl\u00DFt:<br>
     * Grundform einer Parameterangabe:<br>
     * { name = [MetaObjectName.]Attributname } { id = MetaObjectName.Attributid } Wenn kein MetaObjectName angegeben
     * wird (nur f\u00FCr Name m\u00F6glich) werden alle Metaobjekte nach dem Attribut durchsucht.
     *
     * @param   string      Das Platzhalterbehaftete String.
     * @param   metaObject  Objekt dass die ObjectAttribute enth\u00E4lt die durch Platzhalter ersetzt werden sollen.
     *
     * @return  parametrisierte String.
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    public static String parametrize(final String string, final MetaObject metaObject) throws DataRetrievalException {
        if (logger.isDebugEnabled()) {
            logger.debug("before parameterise: " + string); // NOI18N
            // MetaObjectWrapper mow = new MetaObjectWrapper(mo);
        }

        int anfIndex = 0;
        int endIndex = -1;
        final int nameOrID;
        String parametrizedStr = ""; // NOI18N
        final String id = "id";      // NOI18N
        final String name = "name";  // NOI18N

        /* werden Daten abgelent die beschreiben wie das Attribut addressiert
         * werden soll: \u00FCber namen oder id, sowie der Wert. adressierung[0]: String "name" | "id" adressierung[1]:
         * der Name des Attributes wenn adressierung[0] = name adressierung[1]: ID des Attributes wenn adressierung[0] =
         * id
         */
        String[] adressierung;

        /*
         * Wenn kein Trennzeichen gefunden: keine parametrisierung erforderlich der \u00FCbergebene String wird
         * unver\u00E4nder zur\u00FCckgeliefert.
         */
        if (string.indexOf(auf, 0) == -1) {
            if (logger.isDebugEnabled()) {
                logger.debug("after parameterise: " + string); // NOI18N
            }
            return string;
        }

        while (true) {
            // neuer anfangsindex
            anfIndex = string.indexOf(auf, endIndex);
            if ((anfIndex == -1)) {
                break;
            }

            parametrizedStr += string.substring(endIndex + 1, anfIndex);

            // neues endindex
            endIndex = string.indexOf(zu, anfIndex);

            // wenn einer der indizes nicht gefunden wird soll die Suche beendet werden.
            if ((endIndex == -1)) {
                break;
            }

            adressierung = splitParam(string.substring(anfIndex, endIndex + 1));

            if (adressierung[0].equalsIgnoreCase(id)) {
                final ObjectAttribute mas = findAttributeById(metaObject, adressierung[1]);
                parametrizedStr = parametrizeElement(parametrizedStr, mas);
            } else if (adressierung[0].equalsIgnoreCase(name)) {
                final ObjectAttribute[] mas = findAttributeByName(metaObject, adressierung[1]);
                parametrizedStr = parametrizeElements(parametrizedStr, mas);
            } else {
                final String meldung = "Error while parameterising. Identifier: " + adressierung[0] // NOI18N
                            + " is not supported during the parameterisation. Use "                 // NOI18N
                            + "\"name\" or \"id\".";                                                // NOI18N
                throw new DataRetrievalException(meldung, logger);
            }
        }

        parametrizedStr += string.substring(endIndex + 1, string.length());

        System.out.println("after parameterise: " + parametrizedStr); // NOI18N
        if (logger.isDebugEnabled()) {
            logger.debug("after parameterise: " + parametrizedStr);   // NOI18N
        }

        return parametrizedStr;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject  Das MetaObject in dem nach Attributen gesucht werden soll.
     * @param   attrName    Name des Attributes mit dem Parametrisiert werden soll. Darf Qualifiziert werden.
     *
     * @return  der bisher parametrisierter String plus das Ergebnis der Verarbeitung des aktuellen Parameter.
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    private static ObjectAttribute findAttributeById(final MetaObject metaObject, final String attrName)
            throws DataRetrievalException {
        final String[] qualifiedAttrName = splitAttributeName(attrName);
        String meldung;
        ObjectAttribute[] ret;
        int attrID;
        TypeVisitor visitor;

        switch (qualifiedAttrName.length) {
            case 0: {
                attrID = parseID(attrName);

                visitor = new ForAttrIDAndObjName(qualifiedAttrName[0], attrID + ""); // NOI18N
                ret = (ObjectAttribute[])metaObject.accept(visitor, null);

                if (ret.length != 1) {
                    meldung = "Error while parameterising the attribute "                                  // NOI18N
                                + attrName + ". Several matched attributes was determined,"                // NOI18N
                                + " Qualify the attribute with the name of the corresponding meto object"; // NOI18N
                    throw new DataRetrievalException(meldung, logger);
                }

                break;
            }

            case 1: {
                meldung = "Error while parameterising the attribute "        // NOI18N
                            + attrName + ". Attribut ID must be qualified."; // NOI18N
                throw new DataRetrievalException(meldung, logger);
            }

            case 2: {
                attrID = parseID(qualifiedAttrName[1]);

                visitor = new ForAttrIDAndObjName(qualifiedAttrName[0], attrID + ""); // NOI18N
                ret = (ObjectAttribute[])metaObject.accept(visitor, null);

                if (ret.length != 1) {
                    meldung = "Error while parameterising. The count of  "                               // NOI18N
                                + "found object attributes(" + ret.length + ") for  Attribut ID "        // NOI18N
                                + attrName + " does not allow a further parameterising of the String. "; // NOI18N
                    throw new DataRetrievalException(meldung, logger);
                }

                break;
            }

            case 3: {
                meldung = "Error while parameterising. Three sub elements within the "      // NOI18N
                            + " parameter name found. Qualification of the parameter names" // NOI18N
                            + " according to the domain is not supported, at the moment.";  // NOI18N
                throw new DataRetrievalException(meldung, logger);
            }

            default: {
                meldung = "To many dots within the parameter name"; // NOI18N
                throw new DataRetrievalException(meldung, logger);
            }
        }
        return ret[0];
    }

    /**
     * DOCUMENT ME!
     *
     * @param   attrID  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    private static int parseID(final String attrID) throws DataRetrievalException {
        try {
            return Integer.parseInt(attrID);
        } catch (NumberFormatException e) {
            final String message = "Error while parameterising. Attribut ID " // NOI18N
                        + attrID + " is no integer.";                         // NOI18N
            throw new DataRetrievalException(message, e, logger);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaObject  DOCUMENT ME!
     * @param   attrName    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    private static ObjectAttribute[] findAttributeByName(final MetaObject metaObject, final String attrName)
            throws DataRetrievalException {
        final String[] qualifiedAttrName = splitAttributeName(attrName);
        String meldung;
        ObjectAttribute[] ret;
        TypeVisitor visitor;

        switch (qualifiedAttrName.length) {
            case 0: {
                visitor = new AttrForName();
                ret = (ObjectAttribute[])metaObject.accept(visitor, attrName);

                break;
            }

            case 1: {
                visitor = new AttrForName();
                ret = (ObjectAttribute[])metaObject.accept(visitor, attrName);

                break;
            }
            case 2: {
                // visitor = new ForAttrAndObjName(qualifiedAttrName[0]);
                // ret = (ObjectAttribute[])metaObject.accept(visitor, qualifiedAttrName[1]);

                // Suche nach Attributnamen und nicht MetaObject Namen,
                // da MetaObject meist null (TIME_SCALE)
                // besser: zus\u00E4tzliche Suche nach Class Name
                visitor = new AttrForNameWithinComplexAttr(qualifiedAttrName[0]);
                final Object object = metaObject.accept(visitor, qualifiedAttrName[1]);
                if (metaObject != null) {
                    ret = new ObjectAttribute[] { (ObjectAttribute)object };
                } else {
                    ret = new ObjectAttribute[0];
                }

                break;
            }

            case 3: {
                meldung = "Error while parameterising the attribute. Three sub elements within " // NOI18N
                            + "the parameter name found. Qualification of the parameter names"   // NOI18N
                            + " according to domains is not supported at the moment.";           // NOI18N
                throw new DataRetrievalException(meldung, logger);
            }

            default: {
                meldung = "To many dots in the parameter name."; // NOI18N
                throw new DataRetrievalException(meldung, logger);
            }
        }

        if (ret.length == 0) {
            final String message = "Error while parameterising. Attribut " // NOI18N
                        + attrName + " cannot be found.";                  // NOI18N
            throw new DataRetrievalException(message, logger);
        }

        return ret;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parametrized  der bisher parametrisierter String.
     * @param   ma            Metaattribut das an den bisher Parametrisierten String angeh\u00E4ngt werden soll.
     *
     * @return  DOCUMENT ME!
     */
    private static String parametrizeElement(
            String parametrized,
            final ObjectAttribute ma) {
        parametrized += ma.getValue();
        return parametrized;
    }

    /**
     * H\u00E4ngt an den bisher parametrisierten String die \u00FCbergebenen Parameter. Falls das Array die Gr\u00F6sse
     * 1 besitzt wird die Fkt parametrizeElement(String, ObjectAttribute) aufgerufen, ansonsten wird aus dem String der
     * letzte Token entnomen, verglichen ob er gleich "=" oder "IN" ist, wenn das der fall ist wird dieser ersetzt durch
     * foge " IN (<werteliste>)".
     *
     * @param   parametrized  der bisher parametrisierter String.
     * @param   mas           MetaAttribut-Array die an den bisher Parametrisierten String angeh\u00E4ngt werden sollen.
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    private static String parametrizeElements(
            String parametrized,
            final ObjectAttribute[] mas) throws DataRetrievalException {
        if (mas.length == 1) {
            return parametrizeElement(parametrized, mas[0]);
        }

        parametrized = parametrized.trim();

        final String lastTerm = parametrized.substring(
                    parametrized.lastIndexOf(" "), // NOI18N
                    parametrized.length()).trim();

        if (!lastTerm.equalsIgnoreCase("=") && !lastTerm.equalsIgnoreCase("IN")) {            // NOI18N
            final String meldung = "Error while parameterising. the search for"               // NOI18N
                        + " an attribute resulted in several matches, a Term = or IN was"     // NOI18N
                        + " expected before the wild-card character, but it does not exist."; // NOI18N
            throw new DataRetrievalException(meldung, logger);
        }

        parametrized = parametrized.substring(0, parametrized.lastIndexOf(" ")); // NOI18N

        parametrized += " IN ("; // NOI18N

        for (int i = 0; i < (mas.length - 1); i++) {
            parametrized += mas[i].getValue().toString();
            parametrized += ", "; // NOI18N
        }

        parametrized += mas[mas.length - 1].getValue().toString();

        return parametrized + ")"; // NOI18N
    }

    /**
     * Aus dem \u00FCbergebenem String werden die einschliessende Trennzeichen entfernt und das was \u00FCbrig bleibt
     * wird nach "="-Zeichen getrennt und in ein String Array geschrieben. Anschliesend werden die Werte ge-trim()-t.
     *
     * @param   paltzhalter  der paltzhalter aus dem String mit allen trennzeichen.
     *
     * @return  String Array mit Platzhalterdaten die wie Folgt angeordnet sind: return[0]: String "name" oder "id" und
     *          bedeutet ob der Attribut \u00FCber seinen Namen oder ID angescprochen werden soll. return[1]: Name oder
     *          ID des Attributes, dass angesprochen werden soll und zwar: der Name des Attributes wenn adressierung[0]
     *          = name, ID des Attributes wenn adressierung[0] = id.
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    private static String[] splitParam(final String paltzhalter) throws DataRetrievalException {
        final String delim = "="; // NOI18N

        final String str = paltzhalter.substring(1, paltzhalter.length() - 1).trim();

        final String[] strAr = str.split(delim);

        if (strAr.length != 2) {
            final String meldung = "Error while parameterising. The found parameter "     // NOI18N
                        + paltzhalter + " could not processed. Check the syntax of the  " // NOI18N
                        + " given parameter.";                                            // NOI18N
            throw new DataRetrievalException(meldung, logger);
        }

        for (int i = 0; i < strAr.length; i++) {
            strAr[i] = strAr[i].trim();
        }

        return strAr;
    }

    /**
     * Es wird versucht den \u00FCbergebenen String nach Punkt aufzusplitten.
     *
     * @param   attrName  der Name des Attributes. Kann durch Punkt mit dem MetaObjekt-Namen qualifiziert werden.
     *
     * @return  String Array mit Teilstrings wenn nindestens ein Punkt enthalten ist, sonst ein leeres Array.
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    private static String[] splitAttributeName(final String attrName) throws DataRetrievalException {
        // falls der Name des Attributes als ".<name>" angegeben wurde,
        // wird eine Exception ausgel\u00F6sst.
        if (attrName.trim().endsWith(".")) {                                                  // NOI18N
            final String meldung = "Error while parameterising the attribute "                // NOI18N
                        + attrName + ". The syntax of the  attribut name/id is not correct."; // NOI18N
            throw new DataRetrievalException(meldung, logger);
        }

        final String[] strAr = attrName.trim().split("\\."); // NOI18N

        if ((strAr.length == 2) && strAr[0].equals("")) {                                     // NOI18N
            final String meldung = "Error while parameterising the attribute "                // NOI18N
                        + attrName + ". The syntax of the  attribut name/id is not correct."; // NOI18N
            throw new DataRetrievalException(meldung, logger);
        }

        return strAr;
    }

/*    public static void main(String[] args) {
        try {
            String[] strAr = splitAttributeName("paramName");

            System.out.println(strAr.length);

            for(int i = 0; i < strAr.length;i++) {
                System.out.println(strAr[i]);
            }

            //String str = "select * from usr where login = {name = Datasource.login} AND pass = {id = Sensor.7}";
            //String str = "select * from usr where login = {name = Sensor.login} AND pass = {id = Sensor.7}";
            String str = "select * from usr where name in {name = login} AND pass = {id = Sensor.7}";
            System.out.println(parametrize(str, Sirius.dataretrieval.BSP.createJDBC_MO()));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }*/
} // end class