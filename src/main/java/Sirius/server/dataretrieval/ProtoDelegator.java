/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * ProtoDelegator.java
 *
 * Created on 24. September 2003, 15:15
 */
package Sirius.server.dataretrieval;
import Sirius.util.Names;

import Sirius.server.localserver.attribute.*;
import Sirius.server.middleware.types.*;
import Sirius.server.middleware.types.MOTraverse.*;

import java.net.URL;

import org.apache.log4j.Logger;

/**
 * Beinhaltet funktionen die in allen Protokolen verwendet werden k\u00F6nnen.
 *
 * @author   awindholz
 * @version  $Revision$, $Date$
 */
public class ProtoDelegator {

    //~ Instance fields --------------------------------------------------------

    protected Logger logger = Logger.getLogger(ProtoDelegator.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Erzeugt ein Objekt dieser Klasse mit Logger dieser Klasse.
     */
    public ProtoDelegator() {
    }

    /**
     * Erzeugt ein Objekt dieser Klasse mit \u00FCbergebenem Logger.
     *
     * @param  logger  DOCUMENT ME!
     */
    public ProtoDelegator(Logger logger) {
        this.logger = logger;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Liefert den ersten gefundenen MetaAttribut mit dem angegebenem Namen im \u00FCbergebenen MetaAttribut. Keine
     * Rekursion.
     *
     * @param   metaObject  DOCUMENT ME!
     * @param   name        DOCUMENT ME!
     *
     * @return  Attribut oder null wenn keiner gefunden.
     *
     * @name    Name des Attributes.
     */
    protected ObjectAttribute getSingleAttribute(MetaObject metaObject, String name) {
        ObjectAttribute[] mas = metaObject.getAttribs();

        for (int i = 0; i < mas.length; i++) {
            if (mas[i].getName().toLowerCase().equals(name.toLowerCase())) {
                return (ObjectAttribute)mas[i];
            }
        }
        return null;
    }

    /**
     * Liefert den Wert des Attributes aus dem \u00FCbergebenen MetaObject. Der Wert wird dabei als ein Objekt
     * deffiniert dass kein MetaObject ist. Keine Rekursion.
     *
     * @param   metaObject  das MetaObject in dem nach dem Attribut gesucht werden soll.
     * @param   name        Name des Attributes dessen Wert geliefert werden soll.
     *
     * @return  Wert des Attributes.
     *
     * @throws  DataRetrievalException  wenn kein Attribut nit \u00FCbergebenem Namen gefunden, oder wenn der gefundene
     *                                  Attribut ein MetaObject als wert enth\u00E4lt.
     */
    protected Object getSingleValue(MetaObject metaObject, String name) throws DataRetrievalException {
        ObjectAttribute ma = getSingleAttribute(metaObject, name);

        if (ma == null) {
            String message = "The attribute " + name + " within object "  // NOI18N
                + metaObject.getName() + " could not be found.";  // NOI18N

            throw new DataRetrievalException(message, logger);
        }

        if ((ma.getValue() instanceof MetaObject)) {
            String message = "The attribut " + name + " within object "  // NOI18N
                + metaObject.getName() + " does not contains a value, but a further object.";  // NOI18N

            String gebug = message + "ObjectAttribute-Value " + name  // NOI18N
                + " is an instanz of class MetaObject.";  // NOI18N
            if (logger.isDebugEnabled()) {
                logger.debug(gebug);
            }

            throw new DataRetrievalException(message, logger);
        }

        return ma.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   metaDataObject  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    protected MetaObject getParameterMO(MetaObject metaDataObject) throws DataRetrievalException {
        /*ObjectAttribute ma = this.getSingleAttribute(metaDataObject, Names.Data_Object_Type.ACCESS_PARAMETER);
         *
         *
         *
         * if(ma == null) { String message = "Data communication did not come to conditions, " + " because the attribute "
         * + Names.Data_Object_Type.ACCESS_PARAMETER + " was not found." + " Only objects of the class " +
         * Names.Data_Object_Type.getName() + " can be processed.";  throw new DataRetrievalException(message, logg); }
         *
         * Object datasourceMO = ma.getValue();
         *
         * if(datasourceMO == null || !(datasourceMO instanceof MetaObject)) { String message = "Data communication did
         * not come to conditions, " + " because the attribute " + Names.Data_Object_Type.ACCESS_PARAMETER + " does not
         * contain connecting data. ";  String gebug = message + "Attribute " + Names.Data_Object_Type.ACCESS_PARAMETER
         * + " is not an instance of MetaObject.";  logg.debug(gebug);  throw new DataRetrievalException(message, logg);
         * }
         *
         *return (MetaObject)datasourceMO;*/

        TypeVisitor tv = new AttrForNameWithinComplexAttr(Names.Data_Object_Type.getName());
        ObjectAttribute access_parameter = (ObjectAttribute)metaDataObject.accept(
                tv,
                Names.Data_Object_Type.ACCESS_PARAMETER);

        if (
            (access_parameter == null)
                    || (access_parameter.getValue() == null)
                    || !(access_parameter.getValue() instanceof MetaObject)) {
            String message = "Data communication did not come to conditions, "  // NOI18N
                + " because the attribute " + Names.Data_Object_Type.ACCESS_PARAMETER  // NOI18N
                + " does not contain connecting data. ";  // NOI18N

            String gebug = message + "Attribute " + Names.Data_Object_Type.ACCESS_PARAMETER  // NOI18N
                + " is not an instance of MetaObject.";  // NOI18N
            if (logger.isDebugEnabled()) {
                logger.debug(gebug);
            }

            throw new DataRetrievalException(message, logger);
        }

        return (MetaObject)access_parameter.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   mo  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    public String extractProtoName(MetaObject mo) throws DataRetrievalException {
        TypeVisitor tv = new AttrForNameWithinComplexAttr(Names.Data_Object_Type.ACCESS_PARAMETER);
        // TypeVisitor tv = new AttrForName();
        ObjectAttribute mas = (ObjectAttribute)mo.accept(tv, Names.Access_Parameter.DATA_SOURCE_CLASS);
        if (logger.isDebugEnabled()) {
            logger.debug(mas);
        }
        if (mas == null) {
            String message = "Protocol name could not be determined "  // NOI18N
                + " because the Attribute " + Names.Data_Object_Type.ACCESS_PARAMETER + " or the Attribute "  // NOI18N
                + Names.Access_Parameter.getName() + "." + Names.Access_Parameter.DATA_SOURCE_CLASS  // NOI18N
                + " were not found.";  // NOI18N
            if (logger.isDebugEnabled()) {
                logger.debug(message);
            }

            message = "Data source class type could not be determined. Only objects of the class "  // NOI18N
                + Names.Data_Object_Type.getName() + " can be processed.";  // NOI18N

            throw new DataRetrievalException(message, logger);
        }

        return (String)mas.getValue();
    }

    /**
     * DOCUMENT ME!
     *
     * @param   parameter  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     *
     * @throws  DataRetrievalException  DOCUMENT ME!
     */
    public String getURL(MetaObject parameter) throws DataRetrievalException {
        String message = "";
        try {
            ObjectAttribute attr_url = (ObjectAttribute)getSingleAttribute(parameter, Names.URL.getName());

            // TypeVisitor tv = new AttrForNameWithinComplexAttr(Names.Data_Object_Type.ACCESS_PARAMETER);
            // ObjectAttribute attr_url = (ObjectAttribute)parameter.accept(tv, Names.URL.getName());

            if (attr_url == null) {
                throw new DataRetrievalException("Parameter " + Names.URL.getName() + " was not found.", logger);  // NOI18N
            }

            Object mo_url = attr_url.getValue();
            if (!(mo_url instanceof MetaObject)) {
                throw new DataRetrievalException(
                    "URL-meta object was not found. Guarantee that value of the column ACCESS_PARAMETER.URL references an existing ID of the table URL",  // NOI18N
                    logger);
            }

            ObjectAttribute attr_url_base = (ObjectAttribute)getSingleAttribute(
                    (MetaObject)mo_url,
                    Names.URL.URL_BASE_ID);

            if (attr_url_base == null) {
                throw new DataRetrievalException("Parameter " + Names.URL.URL_BASE_ID + " was not found.", logger);  // NOI18N
            }

            Object mo_url_base = attr_url_base.getValue();
            if (!(mo_url_base instanceof MetaObject)) {
                throw new DataRetrievalException(
                    "URL_BASE-meta object was not found. Guarantee that value of the column URL.URL_BASE_ID references an existing ID of the table URL_BASE",  // NOI18N
                    logger);
            }

            String protokol;
            String server;
            String path;

            protokol = (String)getSingleValue((MetaObject)mo_url_base, Names.URL_BASE.PROT_PREFIX);
            server = (String)getSingleValue((MetaObject)mo_url_base, Names.URL_BASE.SERVER);
            path = (String)getSingleValue((MetaObject)mo_url_base, Names.URL_BASE.PATH);

            String url = protokol + server + path;

            if(logger.isInfoEnabled())
                logger.info("generated URL: " + url);  // NOI18N

            return url;
        } catch (java.lang.ClassCastException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(message, e);
            }
            throw e;
        }
/*
        try {

            return new URL(protokol, server, path);

        } catch( java.net.MalformedURLException e) {
            String message = "Fehler w\u00E4hrend umsertzung der URL-Daten.";
            throw new DataRetrievalException(message,e, logg);
        }*/
    }
}
