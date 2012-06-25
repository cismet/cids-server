/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server;

import org.apache.log4j.Logger;

import java.io.Serializable;

import java.lang.reflect.Field;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 *
 * @author   martin.scholl@cismet.de
 * @version  $Revision$, $Date$
 */
public abstract class Shutdown extends AbstractShutdownable implements Serializable {

    //~ Static fields/initializers ---------------------------------------------

    private static final transient Logger LOG = Logger.getLogger(Shutdown.class);

    public static final int PRIORITY_LATEST = Integer.MAX_VALUE;
    public static final int PRIORITY_LATER = Integer.MAX_VALUE / 2;
    public static final int PRIORITY_NORMAL = 0;
    public static final int PRIORITY_EARLIER = Integer.MIN_VALUE / 2;
    public static final int PRIORITY_EARLIEST = Integer.MIN_VALUE;

    //~ Instance fields --------------------------------------------------------

    private final Map<Integer, Set<Shutdownable>> shutdowns;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Shutdown object.
     */
    protected Shutdown() {
        shutdowns = new HashMap<Integer, Set<Shutdownable>>();
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * Adds all shutdownable fields of the subclass to the shutdown list using normal priority. It uses reflection to
     * get private declared fields and sets them accessible. Thus the shutdown sequence can be harmed if the
     * {@link SecurityManager} does not allow access to the subclass' private fields.
     */
    private void addShutdownableFields() {
        final Field[] fields = this.getClass().getDeclaredFields();
        for (final Field field : fields) {
            try {
                field.setAccessible(true);
                final Object fieldValue = field.get(this);
                if (fieldValue instanceof Shutdownable) {
                    final Shutdownable sd = (Shutdownable)fieldValue;
                    if ((sd != null) && (getPriority(sd) == null)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("adding shutdownable field: " + sd); // NOI18N
                        }
                        addShutdown(sd);
                    }
                }
            } catch (final Exception ex) {
                LOG.warn("shutdown probably incomplete: cannot add shutdownable field: " + field, ex); // NOI18N
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   o  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Shutdown createShutdown(final Object o) {
        final Shutdown shutdown = new Shutdown() {
            };

        final Field[] fields = o.getClass().getDeclaredFields();
        for (final Field field : fields) {
            try {
                field.setAccessible(true);
                final Object fieldValue = field.get(o);
                if (fieldValue instanceof Shutdownable) {
                    final Shutdownable sd = (Shutdownable)fieldValue;
                    if ((sd != null) && (shutdown.getPriority(sd) == null)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("adding shutdownable field: " + sd); // NOI18N
                        }
                        shutdown.addShutdown(sd);
                    }
                }
            } catch (final Exception ex) {
                LOG.warn("shutdown probably incomplete: cannot add shutdownable field: " + field, ex); // NOI18N
            }
        }

        return shutdown;
    }

    @Override
    protected void internalShutdown() throws ServerExitError {
        addShutdownableFields();

        final Integer[] priorities = shutdowns.keySet().toArray(new Integer[shutdowns.size()]);
        Arrays.sort(priorities);

        for (final Integer priority : priorities) {
            for (final Shutdownable shutdownable : shutdowns.get(priority)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("shutting down: " + shutdownable); // NOI18N
                }

                if (!shutdownable.isDown()) {
                    shutdownable.shutdown();
                }
            }
        }

        shutdowns.clear();
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sd  DOCUMENT ME!
     */
    protected final void addShutdown(final Shutdownable sd) {
        addShutdown(PRIORITY_NORMAL, sd);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  priority  DOCUMENT ME!
     * @param  sd        DOCUMENT ME!
     */
    protected final synchronized void addShutdown(final int priority, final Shutdownable sd) {
        if ((sd == null) || sd.isDown() || sd.equals(this)) {
            return;
        }

        removeShutdown(sd);

        if (shutdowns.containsKey(priority)) {
            shutdowns.get(priority).add(sd);
        } else {
            final Set<Shutdownable> shutdownables = new HashSet<Shutdownable>(5);
            shutdownables.add(sd);
            shutdowns.put(priority, shutdownables);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   sd  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Integer getPriority(final Shutdownable sd) {
        if (sd == null) {
            return null;
        }

        synchronized (shutdowns) {
            for (final Integer priority : shutdowns.keySet()) {
                for (final Shutdownable shutdownable : shutdowns.get(priority)) {
                    if (sd.equals(shutdownable)) {
                        return priority;
                    }
                }
            }
        }

        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sd  DOCUMENT ME!
     */
    protected final synchronized void removeShutdown(final Shutdownable sd) {
        if ((sd == null) || isDown()) {
            return;
        }

        final Integer priority = getPriority(sd);
        if (priority != null) {
            shutdowns.get(priority).remove(sd);
        }
    }
}
