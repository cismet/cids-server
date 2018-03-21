/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cids.server.connectioncontext.loggers;

import org.apache.log4j.Logger;

import org.openide.util.lookup.ServiceProvider;

import java.io.FileWriter;
import java.io.IOException;

import java.util.Map;

import de.cismet.cids.server.connectioncontext.ConnectionContextFilterRuleSet;
import de.cismet.cids.server.connectioncontext.ConnectionContextLog;
import de.cismet.cids.server.connectioncontext.ConnectionContextLogger;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
@ServiceProvider(service = ConnectionContextLogger.class)
public class FileWriterConnectionContextLogger extends AbstractConnectionContextLogger {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(FileWriterConnectionContextLogger.class);
    private static final String PROPERTY__LOG_FILE = "logFile";
    private static final String PROPERTY__LOG_FORMAT = "logFormat";
    private static final String NAME = "FileWriter";

    //~ Instance fields --------------------------------------------------------

    private FileWriter fileWriter;
    private String logFormat;

    //~ Methods ----------------------------------------------------------------

    @Override
    public void configure(final Object config) {
        if (config instanceof Map) {
            final Map map = (Map)config;
            if (map.containsKey(PROPERTY__LOG_FILE)) {
                final String filePathName = (String)map.get(PROPERTY__LOG_FILE);
                FileWriter fileWriter = null;
                try {
                    fileWriter = new FileWriter(filePathName, true);
                } catch (final IOException ex) {
                    LOG.error(ex, ex);
                }
                this.fileWriter = fileWriter;

                if (map.containsKey(PROPERTY__LOG_FORMAT)) {
                    this.logFormat = (String)map.get(PROPERTY__LOG_FORMAT);
                }
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void log(final ConnectionContextLog contextLog) {
        if (this.fileWriter != null) {
            for (final ConnectionContextFilterRuleSet filterRuleSet : getSatisfiedFilterRuleSets(contextLog)) {
                final Map<String, Object> params = filterRuleSet.getLoggerParams();

                if (contextLog != null) {
                    try {
                        this.fileWriter.write(contextLog.toString(this.logFormat) + "\n");
                        this.fileWriter.flush();
                    } catch (final IOException ex) {
                        LOG.error("Error while writing connection context log", ex);
                    }
                }
            }
        }
    }
}
