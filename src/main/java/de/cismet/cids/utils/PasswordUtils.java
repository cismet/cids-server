/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cids.utils;

import org.apache.log4j.Logger;

import de.cismet.tools.PasswordEncrypter;

/**
 * Helper for reading passwords from configuration files. The passwords can be stored in plain text or encrypted with
 * the {@link PasswordEncrypter}.
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class PasswordUtils {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(PasswordUtils.class);

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PasswordUtils object.
     */
    private PasswordUtils() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Decrypts the given password, if it is encrypted. Supported formats of the PasswordEncrypter:
     *
     * <ul>
     *   <li><code>aes::...</code> AES/GCM, see {@link PasswordEncrypter#encryptAes(char[], boolean)}. Requires the AES
     *     key in the environment variable <code>PASSWORDENCRYPTER_AESKEY</code> or in the property <code>
     *     PasswordEncrypter.aesKey</code> of the <code>de/cismet/tools/PasswordEncrypter.properties</code> on the
     *     classpath.</li>
     *   <li><code>{...}</code> PBEWithMD5AndDES, requires the master key in the <code>
     *     de/cismet/tools/PasswordEncrypter.properties</code> on the classpath.</li>
     *   <li><code>crypt::...</code> legacy format.</li>
     * </ul>
     *
     * <p>Passwords without one of these formats are returned unchanged.</p>
     *
     * @param   password  the password as read from the configuration
     *
     * @return  the decrypted password or <code>null</code>, if the given password is <code>null</code>
     */
    public static String decrypt(final String password) {
        if (password == null) {
            return null;
        }

        try {
            final char[] chars = password.toCharArray();
            final char[] decrypted = PasswordEncrypter.isAesEncrypted(chars) ? PasswordEncrypter.decryptAes(chars, true)
                                                                             : PasswordEncrypter.decrypt(chars, true);
            final String result = new String(decrypted);
            PasswordEncrypter.wipe(decrypted);

            return result;
        } catch (final Exception ex) {
            LOG.error("Cannot decrypt password. The password will be used as it is.", ex);

            return password;
        }
    }
}
