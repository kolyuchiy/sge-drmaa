/*
 * JobStatusMonitor.java
 *
 * Created on 14 Декабрь 2006 г., 22:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.pp.kolia.gridapp;

import java.util.ArrayList;
import java.util.List;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.ExitTimeoutException;
import org.ggf.drmaa.InvalidJobException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.NoActiveSessionException;
import org.ggf.drmaa.Session;

/**
 *
 * @author kolia
 */
public class JobStatusMonitor extends Thread {
    private Session session;
    private List<String> jobIds;
    
    /** Creates a new instance of JobStatusMonitor */
    public JobStatusMonitor(Session session, List<String> jobIds) {
        this.session = session;
        this.jobIds = jobIds;
    }
    
    public void run() {
        while (true) {
            ArrayList<String> ids = new ArrayList<String>(jobIds);
            
            for (String jobId : ids) {
                try {
                    printJobStatus(jobId);
                } catch (NoActiveSessionException e) {
                    return;
                } catch (InvalidJobException e) {
                } catch (DrmaaException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
                
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    private void printJobStatus(String jobId)
    throws DrmaaException {
        int status = session.getJobProgramStatus(jobId);
        switch (status) {
            case Session.UNDETERMINED:
                System.out.println("Job " + jobId + " status unknown");
                break;
            case Session.QUEUED_ACTIVE:
                System.out.println("Job " + jobId + " is pending");
                break;
            case Session.SYSTEM_ON_HOLD:
            case Session.USER_ON_HOLD:
            case Session.USER_SYSTEM_ON_HOLD:
                System.out.println("Job " + jobId + " is on hold");
                break;
            case Session.RUNNING:
                System.out.println("Job " + jobId + " is running");
                break;
            case Session.SYSTEM_SUSPENDED:
            case Session.USER_SUSPENDED:
                System.out.println("Job " + jobId + " is suspended");
                break;
            case Session.DONE:
                System.out.println("Job " + jobId + " has completed");
                break;
            case Session.FAILED:
                System.out.println("Job " + jobId + " has failed");
                break;
        }
    }
    
}
