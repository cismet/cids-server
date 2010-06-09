/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.sql;

import java.sql.*;

/**
 * parametrisiert ein in der Tabelle statement enthaltenes sql-statement wie folgt.<BR>
 *
 * <p>typ :=<BR>
 * </p>
 *
 * <p>boolean -> Z<BR>
 * byte -> B<BR>
 * char -> C<BR>
 * String -> S<BR>
 * int -> I<BR>
 * float -> F<BR>
 * Date -> D<BR>
 * <BR>
 * typ[] -> [typ<BR>
 * </p>
 *
 * <p>Trennzeichen: §typ$<BR>
 * </p>
 *
 * <p>Bsp: Select * from user where id = §I$<BR>
 * </p>
 *
 * <p>wird z.B. ersetzt durch<BR>
 * </p>
 *
 * <p>Select * from user where id = 3<BR>
 * </p>
 *
 * @author   Sascha Schlobinski
 * @version  1.0 erstellt am 06.10.1999
 * @since    DOCUMENT ME!
 */

public class StatementParametrizer {

    //~ Static fields/initializers ---------------------------------------------

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(
            StatementParametrizer.class);

    //~ Methods ----------------------------------------------------------------

    /**
     * Hauptfunktionalit\u00E4t der Klasse Parametrizer siehe Klassenbeschreibung.<BR>
     *
     * @param   statement   java.lang.String statement
     * @param   parameters  java.lang.Object[] parameters
     *
     * @return  DOCUMENT ME!
     *
     * @throws  java.lang.Exception  DOCUMENT ME!
     * @throws  Exception            DOCUMENT ME!
     */

    public static final String parametrize(String statement, final java.lang.Object[] parameters)
            throws java.lang.Exception {
        // ersetze wildcards f\u00FCr like
        statement = statement.trim();

        final char auf = '§';
        final char zu = '$';
        String parameter = new String("");
        String join = new String();

        int parameterIndex = 0;

        int von = statement.indexOf(auf); // initialiserung mit dem ersten Treffer
        int bis = statement.indexOf(zu);

        if ((von == -1) || (bis == -1)) {
            throw new Exception("kein Parameter zum ersetzen da Trennzeichen nicht gefunden");
        }

        String parametrizedStmnt = statement.substring(0, von); // statement bis zum ersten parameter

        try {
            while ((parameterIndex < parameters.length) && (von != -1) && (bis != -1)) {
                parameter = statement.substring(von + 1, bis).trim();

                switch (parameter.charAt(0)) {
                    case ('['): {
                        // Array

                        if (parameter.length() > 2) {
                            throw new Exception("Syntaxfehler :" + parameter);
                        }

                        switch (parameter.charAt(1)) {
                            case ('I'): {
                                // Integer

                                if (parameters[parameterIndex] instanceof java.lang.Integer[]) {
                                    parametrizedStmnt += convertNumberArrayForSql((java.lang.Integer[])
                                            parameters[parameterIndex]);
                                } else {
                                    throw new Exception(
                                        "parameter passt nicht zum Typ im Statement"
                                                + " parameterIndex :"
                                                + parameterIndex
                                                + "  erwarteter Typ : Integer[]");
                                }

                                break;
                            }

                            case ('S'): {
                                // String upper case not case sensitive//jetzt lower case
                                if (parameters[parameterIndex] instanceof java.lang.String[]) {
                                    parametrizedStmnt += convertStringArrayForSql((java.lang.String[])
                                            parameters[parameterIndex],
                                            false);
                                } else {
                                    throw new Exception(
                                        "parameter passt nicht zum Typ im Statement"
                                                + " parameterIndex :"
                                                + parameterIndex
                                                + "  erwarteter Typ : String[]");
                                }

                                break;
                            }

                            case ('s'): {
                                // String lower case case sensitive
                                if (parameters[parameterIndex] instanceof java.lang.String[]) {
                                    parametrizedStmnt += convertStringArrayForSql((java.lang.String[])
                                            parameters[parameterIndex],
                                            true,
                                            true);
                                } else {
                                    throw new Exception(
                                        "parameter passt nicht zum Typ im Statement"
                                                + " parameterIndex :"
                                                + parameterIndex
                                                + "  erwarteter Typ : String[]");
                                }

                                break;
                            }

                            // rpiontek 07.03.2001 Bilden einer Liste von Tablennahmen oder Columnamen - Auch String
                            // aber ohne einfachen Anfuehrungszeigen '

                            case ('T'): {
                                // String upper case not case sensitive
                                if (parameters[parameterIndex] instanceof java.lang.String[]) {
                                    parametrizedStmnt += convertStringArrayForSql((java.lang.String[])
                                            parameters[parameterIndex],
                                            false,
                                            false);
                                } else {
                                    throw new Exception(
                                        "parameter passt nicht zum Typ im Statement"
                                                + " parameterIndex :"
                                                + parameterIndex
                                                + "  erwarteter Typ : String[]");
                                }

                                break;
                            }

                            // rpiontek 07.03.2001 Eine Liste z.b. durch ODER ,UND oder "," verknupft
                            // Syntax: �[M$ VERKNUEPFUNG �M]$
                            // z.B. �[M$ AND �M]$ ergibt: column1='value1' AND column2='value2' AND calumn3='value3'

                            case ('M'): {
                                // String
                                if (parameters[parameterIndex] instanceof java.lang.String[]) {
                                    statement.trim();
                                    final int ende = statement.indexOf(" ", bis + 2);
                                    join = " " + statement.substring(bis + 1, ende).trim().toUpperCase() + " ";
                                    parametrizedStmnt += convertConditionsArrayForSql(parameters, parameterIndex, join);
                                } else {
                                    throw new Exception(
                                        "parameter passt nicht zum Typ im Statement"
                                                + " parameterIndex :"
                                                + parameterIndex
                                                + "  erwarteter Typ : String[]");
                                }

                                break;
                            }

                            // case  ('B'):// Byte

                            case ('C'): {
                                // Character
                                if (parameters[parameterIndex] instanceof java.lang.Character[]) {
                                    parametrizedStmnt += convertStringArrayForSql((java.lang.Character[])
                                            parameters[parameterIndex],
                                            true,
                                            true);
                                } else {
                                    throw new Exception(
                                        "parameter passt nicht zum Typ im Statement"
                                                + " parameterIndex :"
                                                + parameterIndex
                                                + "  erwarteter Typ : Char[]");
                                }

                                break;
                            }

                            case ('F'): {
                                // floatingpoint

                                if (parameters[parameterIndex] instanceof java.lang.Double[]) {
                                    parametrizedStmnt += convertNumberArrayForSql((java.lang.Double[])
                                            parameters[parameterIndex]);
                                } else {
                                    throw new Exception(
                                        "parameter passt nicht zum Typ im Statement"
                                                + " parameterIndex :"
                                                + parameterIndex
                                                + "  erwarteter Typ : DOUBLE[]");
                                }

                                break;
                            }

                            default: {
                                throw new java.lang.Exception(
                                    "nicht unterst\u00FCtzter ArrayTyp oder kein Array :"
                                            + parameter.charAt(1));
                            }
                        } // end inner switch

                        break;
                    }

                    case ('I'): {
                        // Integer

                        if ((parameters[parameterIndex] instanceof java.lang.Integer) && (parameter.length() < 2)) {
                            parametrizedStmnt += parameters[parameterIndex];
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter Typ : Integer "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    case ('s'): {
                        // String

                        if ((parameters[parameterIndex] instanceof java.lang.String) && (parameter.length() < 2)) {
                            parametrizedStmnt += "'" + ((java.lang.String)parameters[parameterIndex]).replace('*', '%')
                                        + "'";
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter Typ : String "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    case ('S'): {
                        // String war upper ist jetzt lower

                        if ((parameters[parameterIndex] instanceof java.lang.String) && (parameter.length() < 2)) {
                            parametrizedStmnt += "'"
                                        + ((java.lang.String)parameters[parameterIndex]).replace('*', '%').toLowerCase()
                                        + "'";
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter Typ : String "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    // rpiontek 07.03.2001 Eine Liste z.b. durch ODER ,UND oder "," verknupft
                    // Syntax: �[M$ VERKNUEPFUNG �M]$
                    // z.B. �[M$ AND �M]$ ergibt: column1='value1' AND column2='value2' AND calumn3='value3'

                    case ('M'): {
                        // String

                        parametrizedStmnt.trim();
                        join.trim();
                        final int index = parametrizedStmnt.lastIndexOf(join);
                        if (index != -1) {
                            parametrizedStmnt = parametrizedStmnt.substring(0, index);
                        }
                        break;
                    }

                    // rpiontek 07.03.2001 ersetzen von Tablennahmen oder Columnamen - Auch String aber ohne einfachen
                    // Anfuehrungszeigen '
                    case ('T'): {
                        // String

                        if ((parameters[parameterIndex] instanceof java.lang.String) && (parameter.length() < 2)) {
                            parametrizedStmnt += ((java.lang.String)parameters[parameterIndex]).toUpperCase();
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter Typ : String "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    case ('B'): {
                        // Byte

                        if ((parameters[parameterIndex] instanceof java.lang.Byte) && (parameter.length() < 2)) {
                            parametrizedStmnt += parameters[parameterIndex];
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter Typ : Byte "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    case ('Z'): {
                        // Boolean

                        if ((parameters[parameterIndex] instanceof java.lang.Boolean) && (parameter.length() < 2)) {
                            parametrizedStmnt += parameters[parameterIndex];
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter Typ : Boolean "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    case ('C'): {
                        // Character

                        if ((parameters[parameterIndex] instanceof java.lang.Character) && (parameter.length() < 2)) {
                            parametrizedStmnt += parameters[parameterIndex];
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter Typ : Character "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    case ('F'): {
                        // floatingpoint

                        if ((parameters[parameterIndex] instanceof java.lang.Double) && (parameter.length() < 2)) {
                            parametrizedStmnt += parameters[parameterIndex];
                        } else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + "  erwarteter  Typ : Double "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    case ('D'): {
                        // Date

                        if ((parameters[parameterIndex] instanceof java.util.Date) && (parameter.length() < 2)) {
                            final java.sql.Date tmp = ((java.sql.Date)parameters[parameterIndex]);
                            // parametrizedStmnt+= "{ d '" + ((java.sql.Date) parameters[parameterIndex]) + "'}";
                            // parametrizedStmnt+= ("'" +(tmp.getMonth()+1)+"/"+ tmp.getDate() + "/" +(
                            // tmp.getYear()+1900) +"'");
                            parametrizedStmnt += ("'" + tmp + "'");
                            // logger.debug("sql-datum :"+tmp);
                        }
                        /*else if(parameters[parameterIndex] instanceof java.util.Date && parameter.length() < 2)
                         * {     java.util.Date tmp = (java.util.Date) parameters[parameterIndex];     java.sql.Date
                         * forStmnt = new java.sql.Date(tmp.getTime()); //zur formatierten Ausgabe
                         * parametrizedStmnt+= "{ d '" + forStmnt  + "'}";}*/
                        else {
                            throw new Exception(
                                "parameter passt nicht zum Typ im Statement"
                                        + " parameterIndex :"
                                        + parameterIndex
                                        + " erwarteter Typ : Date "
                                        + " parameter :"
                                        + parameter);
                        }

                        break;
                    }

                    default: {
                        throw new java.lang.Exception("nicht unterst\u00FCtzter Typ :" + parameter.charAt(0));
                    }
                } // end outer switch

                von = statement.indexOf(auf, bis); // neues von

                // logger.debug("von :"+von +" bis : "+bis +"length :"+statement.length());

                if (von != -1) {
                    parametrizedStmnt += statement.substring(bis + 1, von);                    // text zwischen 2
                                                                                               // Parametern
                } else {
                    if (bis == (statement.length() - 1))                                       // wenn kein rest mehr
                                                                                               // zum Anf\u00FCgen
                    {                                                                          /*nop*/
                    } else {
                        parametrizedStmnt += statement.substring(bis + 1, statement.length()); // rest bis zum
                                                                                               // Stringende
                    }

                    // break;
                }

                bis = statement.indexOf(zu, von); // neues bis

                parameterIndex++;
            } // end while
        } catch (Exception e) {
            logger.error("Info :: Fehler beim parmetrisieren von :" + statement, e);
            throw e;
        }

        return parametrizedStmnt;
    } // end parametrize()

//---------------------------------------

/*
public static final PreparedStatement parametrize(PreparedStatement statement, java.lang.Object[] parameters) throws java.lang.Exception
        {





        }


*/

//////////////////////////////////////////////////////////////////////

    /**
     * Wandelt Arrays von Integern oder Doubles in die entsprechende Sql Darstellung um<BR>
     * Bsp. int arr[] = {1,2,3}; in String s = (1,2,3);
     *
     * @param   arr  java.lang.Object[] array
     *
     * @return  java.lang.String
     */

    public static String convertNumberArrayForSql(final java.lang.Object[] arr) {
        String sql = new String("(");
        int i = 0;

        try {
            for (i = 0; i < arr.length; i++) {
                if (i == 0) {
                    sql += arr[i];
                } else {
                    sql += "," + arr[i];
                }
            }

            sql += ")";
        } catch (Exception e) {
            logger.error("<LS> ERROR :: convertNumberArrayForSql Error at " + i, e);
            sql = new String("()");
        }

        return sql;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   arr  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static String convertIntArrayForSql(final int[] arr) {
        String sql = new String("(");
        int i = 0;

        try {
            for (i = 0; i < arr.length; i++) {
                if (i == 0) {
                    sql += arr[i];
                } else {
                    sql += "," + arr[i];
                }
            }

            sql += ")";
        } catch (Exception e) {
            logger.error("<LS> ERROR :: convertIntArrayForSql Error at " + i, e);
            sql = new String("()");
        }

        return sql;
    }
////////////////////////////////////////////////////////////////////////

    /**
     * Wandelt Arrays von Strings oder Characters in die entsprechende Sql Darstellung um<BR>
     * Bsp. int String[] = {"eimer","ist","kaputt" }; in String s = ("'eimer','ist','kaputt'");
     *
     * @param   arr            java.lang.Object[] array
     * @param   caseSensitive  DOCUMENT ME!
     * @param   withComma      DOCUMENT ME!
     *
     * @return  java.lang.String
     */

    private static String convertStringArrayForSql(final java.lang.Object[] arr,
            final boolean caseSensitive,
            final boolean withComma) {
        // rpiontek
        String comma = new String();
        if (withComma) {
            comma = "'";
        }

        String sql = new String("(" + comma);
        int i = 0;

        try {
            for (i = 0; i < arr.length; i++) {
                if (i == 0) {
                    if (!caseSensitive) {
                        sql += ((java.lang.String)arr[i]).toUpperCase();
                    } else {
                        sql += ((java.lang.String)arr[i]);
                    }
                } else {
                    if (!caseSensitive) {
                        sql += comma + "," + comma + ((java.lang.String)arr[i]).toUpperCase();
                    } else {
                        sql += comma + "," + comma + ((java.lang.String)arr[i]);
                    }
                }
            }

            sql += comma + ")";
        } catch (Exception e) {
            logger.error("<LS> ERROR :: convertStringArrayForSql Error at " + i, e);
            sql = new String("()");
        }

        return sql;
    }
/////////////////////////////////////////////////////////////////////////7

    /**
     * DOCUMENT ME!
     *
     * @param   arr            DOCUMENT ME!
     * @param   caseSensitive  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private static String convertStringArrayForSql(final java.lang.Object[] arr, final boolean caseSensitive) {
        String sql = "(";
        int i = 0;

        try {
            for (i = 0; i < arr.length; i++) {
                if (i == 0) {
                    if (!caseSensitive) {
                        sql += ((java.lang.String)arr[i]).toLowerCase();
                    } else {
                        sql += ((java.lang.String)arr[i]);
                    }
                } else {
                    if (!caseSensitive) {
                        sql += "," + ((java.lang.String)arr[i]).toLowerCase();
                    } else {
                        sql += "," + ((java.lang.String)arr[i]);
                    }
                }
            }

            sql += ")";
        } catch (Exception e) {
            logger.error("<LS> ERROR ::  convertStringArrayForSql Error at " + i, e);
            sql = new String("()");
        }

        return sql;
    }

////////////////////////////////////////////////////////////////////////

    /**
     * Wandelt zwei Arrays von Strings oder Characters in die entsprechende Sql Darstellung um<BR>
     * Bsp. String[] array1= {"column1","column2","column3" }; String[] array2= {"value1", "value2", "value3" }; in
     * String s = "column1='value1' AND column2='value2' AND column3='value3'" Die Verknuepfung muss angegeben werden
     * ODER oder UND
     *
     * @param   parameters      java.lang.Object[] array
     * @param   parameterIndex  java.lang.String join
     * @param   join            DOCUMENT ME!
     *
     * @return  java.lang.String
     *
     * @throws  Exception  DOCUMENT ME!
     *
     * @author  rpiontek 07.03.2001
     */

    private static String convertConditionsArrayForSql(final java.lang.Object[] parameters,
            final int parameterIndex,
            final String join) throws Exception {
        StringBuffer resultString = new StringBuffer(" ");
        int i = 0;

        final String[] fields = (java.lang.String[])parameters[parameterIndex];
        final String[] values = (java.lang.String[])parameters[parameterIndex + 1];

        if (fields.length != values.length) {
            throw new Exception(
                "Es kann keine Where-Liste gebildet werden, da die Anzahl Felder ("
                        + fields.length
                        + ") mit der Anzahl der zu Werte ("
                        + values.length
                        + ") nicht uebereinstimmt");
        }
        try {
            for (i = 0; i < fields.length; i++) {
                resultString.append((java.lang.String)fields[i]);
                resultString.append(" = ");
                resultString.append("'" + (java.lang.String)values[i] + "'");
                if (i < (fields.length - 1)) {
                    resultString.append(join);
                }
                resultString.append(" ");
            }
        } catch (Exception e) {
            logger.error("<LS> ERROR :: convertConditionsArrayForSql Error at " + i, e);
            resultString = new StringBuffer();
        }

        return resultString.toString();
    }
} // end class
