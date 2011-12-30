/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = ClassLoadingPackagePrefixProvider.class)
public class DefaultClassLoadingPackagePrefixProvider implements ClassLoadingPackagePrefixProvider {

    //~ Methods ----------------------------------------------------------------

    @Override
    public String getClassLoadingPackagePrefix() {
        return "de.cismet.cids.custom";
    }
}
