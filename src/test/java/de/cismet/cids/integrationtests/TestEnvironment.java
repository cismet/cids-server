package de.cismet.cids.integrationtests;

/**
 *
 * @author Pascal Dihé <pascal.dihe@cismet.de>
 */
public class TestEnvironment {

    private static final String INTEGRATION_TESTS_ENABLED = "de.cismet.cids.integrationtests.enabled";

    public static boolean isIntegrationTestsEnabled() {
        //System.out.println(System.getProperty(INTEGRATION_TESTS_ENABLED));
        return "true".equalsIgnoreCase(System.getProperty(INTEGRATION_TESTS_ENABLED));
    }

    public static boolean isIntegrationTestsDisabled() {
        //System.out.println(System.getProperty(INTEGRATION_TESTS_ENABLED));
        return !"true".equalsIgnoreCase(System.getProperty(INTEGRATION_TESTS_ENABLED));
    }
}
