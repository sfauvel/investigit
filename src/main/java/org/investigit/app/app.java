package org.investigit.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class app {
    public static class CallGit {

        public static void main(String args[]) {

            String s = null;
            String path = "/home/sfauvel/Documents/projects/games/freecol/freecol-git";
            try {

                // run the Unix "ps -ef" command
                // using the Runtime exec method:
                Process p = Runtime.getRuntime().exec("git -C " + path + " log");

                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));

                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));

                // read the output from the command
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }

                // read any errors from the attempted command
                System.out.println("Here is the standard error of the command (if any):\n");
                while ((s = stdError.readLine()) != null) {
                    System.out.println(s);
                }

                System.exit(0);
            } catch (IOException e) {
                System.out.println("exception happened - here's what I know: ");
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
