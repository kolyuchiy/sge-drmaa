/*
 * Main.java
 *
 * Created on 13 Декабрь 2006 г., 21:13
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.pp.kolia.gridapp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.ggf.drmaa.DrmaaException;
import org.ggf.drmaa.JobInfo;
import org.ggf.drmaa.JobTemplate;
import org.ggf.drmaa.Session;
import org.ggf.drmaa.SessionFactory;

/**
 *
 * @author kolia
 */
public class Main {
    private final static String SGE_ROOT = "/usr/local/n1ge";
    private final static String WORKING_DIR = "/default/gridapp";
    private final static String JOB_NAME = "primer";
    private final static int PARTITION = 2;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -jar gridapp.jar <start> <end>");
            System.exit(0);
        }
        
        SessionFactory factory = SessionFactory.getFactory();
        final Session session = factory.getSession();
        
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(session));
        
        try {
            session.init("");
            System.out.println("Version: " + session.getDrmaaImplementation());
            
            JobTemplate jt = session.createJobTemplate();
            jt.setJobName(JOB_NAME);
            jt.setRemoteCommand(SGE_ROOT + "/gridapp/primer.sh");
            jt.setArgs(args);
            jt.setWorkingDirectory(SGE_ROOT + WORKING_DIR);
            jt.setJobCategory("primer");
            
            long start = Long.parseLong(args[0]);
            long end = Long.parseLong(args[1]);
            long range = (end - start) / PARTITION;
            
            List<String> jobIds = Collections.synchronizedList(new ArrayList(8));
            double cpuTime = 0.0;
            
            Thread monitor = new JobStatusMonitor(session, jobIds);
            monitor.setDaemon(true);
            monitor.start();
            
            int count = 0;
            for (count = 0; count < PARTITION-1; count++) {
                jt.setArgs(new String[] {
                    Long.toString(start + count*range),
                    Long.toString(start + (count+1)*range - 1)
                });
                jobIds.add(session.runJob(jt));
            }
            
            jt.setArgs(new String[] {
                Long.toString(start + count*range),
                Long.toString(end)
            });
            jobIds.add(session.runJob(jt));
            
            session.deleteJobTemplate(jt);
            
            for (count = 0; count < PARTITION; count++) {
                String jobId = jobIds.get(count);
                JobInfo info = session.wait(jobId, Session.TIMEOUT_WAIT_FOREVER);
                
                if (!info.hasExited()) {
                    System.err.println("Job " + jobId + " terminated abnormally");
                    System.exit(1);
                }
                printExitStatus(info);
                
                Map usage = info.getResourceUsage();
                cpuTime += Double.parseDouble((String)usage.get("cpu"));
                printJobOutput(jobId);
            }
            
            System.out.println("CPU time: " + cpuTime);
            
        } catch (DrmaaException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printExitStatus(JobInfo info) {
        if (info.hasExited()) {
            System.out.println("Job exited with code " + info.getExitStatus());
        } else if (info.hasSignaled()) {
            System.out.println("Job exited on signal " + info.getTerminatingSignal());
            
            if (info.hasCoreDump()) {
                System.out.println("Core dumped");
            }
        } else if (info.wasAborted()) {
            System.out.println("Job never ran");
        } else {
            System.out.println("Exit status is unknown");
        }
    }
    
    private static void printJobOutput(String jobId) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(SGE_ROOT + WORKING_DIR + "/" + JOB_NAME + ".o" + jobId));
            System.out.println("Job " + jobId + " output:");
            while (reader.ready()) {
                System.out.println(reader.readLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Job output file not found");
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Error reading job output file");
            System.exit(1);
        }
    }
    
}
