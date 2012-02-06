/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package Sirius.server.registry;

import Sirius.server.observ.*;

import java.util.Vector;

/**
 * DOCUMENT ME!
 *
 * @version  $Revision$, $Date$
 */
public class Observable {

    //~ Instance fields --------------------------------------------------------

    protected RemoteObservable rmobs;

    /** Vector, der die registrierten Observer enthaelt.* */
    protected Vector observers;

    /** Flag, spielt beim Muster Observer, Observable eine Rolle.* */
    protected boolean changed;
    private final transient org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(this.getClass());

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new Observable object.
     *
     * @param  rmobs  DOCUMENT ME!
     */
    public Observable(final RemoteObservable rmobs) {
        this.rmobs = rmobs;

        observers = new Vector(10, 10);

        changed = false;
    }

    //~ Methods ----------------------------------------------------------------

    // ===================================================================================
    // ==================== Implementierung des Interfaces Observable ====================
    // ===================================================================================

    /**
     * Indicates that this object has no longer changed, or that it has already notified all of its observers of its
     * most recent change, so that the hasChanged method will now return false. *
     */
    public synchronized void clearChanged() {
        changed = false;
    }

    /**
     * Fragt den Status ab, ob sich etwas geaendert hat.*
     *
     * @return  DOCUMENT ME!
     */
    public synchronized boolean hasChanged() {
        return changed;
    }

    /**
     * Markiert, das sich etwas in der Registry geaendert hat, die hasChanged-Methode liefert nun true.*
     */
    public synchronized void setChanged() {
        changed = true;
    }

    /**
     * liefert die Anzahl der registierten Observer/Beobachter.
     *
     * @return  Anzahl der Observer *
     */
    public synchronized int countObservers() {
        return observers.size();
    }

    /**
     * Fuegt einen Observer/Beobachter hinzu, dieser wird dann spaeter bei Aenderungen automatisch aktualisiert.
     *
     * @param  ob  ein Objekt, welches das Sirius.Observ.RemoteObserver Interface implementiert *
     */
    public synchronized void addObserver(final RemoteObserver ob) {
        if (!observers.contains(ob)) {
            observers.addElement(ob);
            if (logger.isDebugEnabled()) {
                logger.debug(" Info Observer registered::" + ob.toString() + "\n"); // NOI18N
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------
    /**
     * wenn ein Observer heruntergefahren wird, meldet er sich ueber diese Funktion bei der Registry als Observer ab. *
     *
     * @param  ob  DOCUMENT ME!
     */
    public synchronized void deleteObserver(final RemoteObserver ob) {
        if (observers.contains(ob)) {
            observers.removeElement(ob);
            if (logger.isDebugEnabled()) {
                logger.debug("Info <REG> Observer removed::" + ob.toString() + "\n"); // NOI18N
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Info <REG> Observer not found" + ob);                       // NOI18N
        }
    }

    // -----------------------------------------------------------------------------------------------------

    /**
     * loescht die Liste der Observer.*
     */
    public synchronized void deleteObservers() {
        observers = new Vector();
    }
    /**
     * -----------------------------------------------------------------------------------------------------
     */
    public void notifyObservers() {
        performNotify(null);
    }
    /**
     * -----------------------------------------------------------------------------------------------------
     *
     * @param  r  DOCUMENT ME!
     */
    public void notifyObservers(final java.rmi.Remote r) {
        performNotify(r);
    }
    /**
     * -----------------------------------------------------------------------------------------------------
     *
     * @param  s  DOCUMENT ME!
     */
    public void notifyObservers(final java.io.Serializable s) {
        performNotify(s);
    }

    // -----------------------------------------------------------------------------------------------------
    /**
     * Diese Methode wird von {@link #notifyObservers() notifyObservers()},
     * {@link #notifyObservers(Remote r) notifyObservers(Remote r)} und
     * {@link #notifyObservers(Remote r) notifyObservers(Remote r)} aufgerufen. In dieser Methode werden alle
     * registrierten Observer benachrichtigt *
     *
     * @param  arg  DOCUMENT ME!
     */
    public void performNotify(final java.lang.Object arg) {
        if (!hasChanged()) {
            return;
        } else {
            for (int i = 0; i < observers.size(); i++) {
                try {
                    final RemoteObserver r = (RemoteObserver)observers.elementAt(i);
                    r.update(rmobs, arg);
                } catch (Exception e) {
                    logger.error("Exception in performNotify:: ", e); // NOI18N
                }
            }
            clearChanged();
        }
    }

    // end class
}
