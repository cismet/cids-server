package de.cismet.cids.integrationtests;

import java.util.Properties;

/**
 * Heler class to initialize Log4j
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
public abstract class TestBase {

    static {
        final Properties log4jProperties = new Properties();
        log4jProperties.put("log4j.appender.Remote", "org.apache.log4j.net.SocketAppender");
        log4jProperties.put("log4j.appender.Remote.remoteHost", "localhost");
        log4jProperties.put("log4j.appender.Remote.port", "4445");
        log4jProperties.put("log4j.appender.Remote.locationInfo", "true");
        log4jProperties.put("log4j.rootLogger", "ALL,Remote");
        org.apache.log4j.PropertyConfigurator.configure(log4jProperties);
    }
}
