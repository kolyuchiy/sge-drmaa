/*
 * ShutdownHook.java
 *
 * Created on 13 Декабрь 2006 г., 22:11
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.pp.kolia.gridapp;

import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;

/**
 *
 * @author kolia
 */
public class ShutdownHook extends Thread {
    Session session = null;
    
    /** Creates a new instance of ShutdownHook */
    public ShutdownHook(Session session) {
        this.session = session;
    }
    
    public void run() {
        try {
            session.control(Session.JOB_IDS_SESSION_ALL, Session.TERMINATE);
            session.exit();
        } catch (NoActiveSessionException e) {
        } catch (DrmaaException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
