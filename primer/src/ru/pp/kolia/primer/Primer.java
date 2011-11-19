
package ru.pp.kolia.primer;

public class Primer {
   
   public static void main (String[] args) {
      long start = 0L;
      long end = 0L;
      
      if (args.length != 2) {
         printUsage ();
         System.exit (1);         
      }
      
      try {
         start = Long.parseLong (args[0]);
      }
      catch (NumberFormatException e) {
         System.err.println (Long.toString (start) + " is not a number");
         printUsage ();
         System.exit (1);
      }
      
      try {
         end = Long.parseLong (args[1]);
      }
      catch (NumberFormatException e) {
         System.err.println (Long.toString (end) + " is not a number");
         printUsage ();
         System.exit (1);
      }

      if (end < start) {
         System.err.println ("End of range may not be less than beginning of range.");
         printUsage ();
         System.exit (1);
      }
      
      calculate (start, end);
   }

   public static void calculate (long start, long end) {
      for (long i = start; i < end; i++) {
         long limit = (long)Math.sqrt (i);
         long candidate = 2;
         
         while (((i % candidate) != 0) && (candidate <= limit)) {
            candidate++;
         }
         
         if (candidate > limit) {
            System.out.println (Long.toString (i));
         }
      }
   }
   
   private static void printUsage () {
      System.out.println ("Usage: java -jar primer.jar <start> <end>");
   }
} 
