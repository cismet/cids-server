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
package Sirius.server.localserver.user;

import java.util.Calendar;
import java.util.Date;

/**
 * DOCUMENT ME!
 *
 * @author   thorsten
 * @version  $Revision$, $Date$
 */
@org.openide.util.lookup.ServiceProvider(service = LoginRestriction.class)
public class TimedLoginRestriction implements LoginRestriction {

    //~ Instance fields --------------------------------------------------------

    private float from;
    private float to;

    //~ Methods ----------------------------------------------------------------

    @Override
    public boolean isLoginAllowed() {
        final Calendar rightNow = Calendar.getInstance();
        final float test = new Float(rightNow.get(Calendar.HOUR_OF_DAY))
                    + (new Float(rightNow.get(Calendar.MINUTE)) / 60.0f);
        if (from == to) {
            return false;
        } else if (from > to) {
            // for example if from is 6 and to is 5, then it is open from 6 to 5 o'clock on the next day
            // and the current time should not be between 5 and 6, because during this time, it is closed
            return !((test >= to) && (test <= from));
        } else {
            return (test >= from) && (test <= to);
        }
    }

    @Override
    public String getKey() {
        return "TIMEDOPENING";
    }

    @Override
    public void configure(final String config) {
        final String[] parts = config.split(",");
        final String fromS = parts[0].trim();
        final String toS = parts[1].trim();
        final String[] fromArrParts = fromS.split(":");
        final String[] toArrParts = toS.split(":");
        from = new Float(fromArrParts[0]) + (new Float(fromArrParts[1]) / 60.0f);
        to = new Float(toArrParts[0]) + (new Float(toArrParts[1]) / 60.0f);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  args  DOCUMENT ME!
     */
    public static void main(final String[] args) {
    }
}
