/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * RMServer.java
 *
 * Created on 23. November 2006, 18:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package Sirius.server.registry.rmplugin.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.Iterator;

/**
 * This interface serve as an super interface for the Remote Object RMRegistryServer because a object can only be
 * exported one times. Over the extended interfaces you can define which functionallity ther server should have
 *
 * @author   Sebastian
 * @version  $Revision$, $Date$
 */
public interface RMRegistryServer extends Remote, RMForwarder, RMRegistry {
}
