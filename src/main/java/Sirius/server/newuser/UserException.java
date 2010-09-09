/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.newuser;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class UserException extends Exception implements java.io.Serializable // java.rmi.ServerException
{

    //~ Static fields/initializers ---------------------------------------------


    //~ Instance fields --------------------------------------------------------

    private boolean wrongUserName = false;
    private boolean wrongPassword = false;
    private boolean wrongUserGroup = false;
    private boolean wrongLocalServer = false;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new UserException object.
     *
     * @param  detailMessage  DOCUMENT ME!
     */
    public UserException(final String detailMessage) {
        super(detailMessage);
    }

    /**
     * Creates a new UserException object.
     *
     * @param  detailMessage     DOCUMENT ME!
     * @param  wrongUserName     DOCUMENT ME!
     * @param  wrongPassword     DOCUMENT ME!
     * @param  wrongUserGroup    DOCUMENT ME!
     * @param  wrongLocalServer  DOCUMENT ME!
     */
    public UserException(final String detailMessage,
            final boolean wrongUserName,
            final boolean wrongPassword,
            final boolean wrongUserGroup,
            final boolean wrongLocalServer) {
        super(detailMessage);
        this.wrongUserName = wrongUserName;
        this.wrongPassword = wrongPassword;
        this.wrongUserGroup = wrongUserGroup;
        this.wrongLocalServer = wrongLocalServer;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean wrongUserName() {
        return wrongUserName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean wrongPassword() {
        return wrongPassword;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean wrongUserGroup() {
        return wrongUserGroup;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean wrongLocalServer() {
        return wrongLocalServer;
    }
}
