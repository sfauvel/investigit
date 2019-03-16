package org.investigit;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GitRule extends TestWatcher {
    Runtime rt = Runtime.getRuntime();

    private File workingPath;

    private ByteArrayOutputStream os;
    public PrintStream printStream;

    public File getWorkingPath() {
        return workingPath;
    }

    public void setWorkingPath(File workingPath) {
        this.workingPath = workingPath;
        System.out.println("Working Path:" + workingPath.getAbsolutePath());
    }

    @Override
    protected void starting(Description description) {
        os = new ByteArrayOutputStream();
        try {
            printStream = new PrintStream(os, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void finished(Description description) {
        printStream.close();
    }

    private File getGitFolder() {
        return new File(workingPath.getAbsolutePath(), ".git");
    }

    public String getOutput() throws UnsupportedEncodingException {
        return new String(os.toByteArray(), "UTF-8");
    }

    public GitRule gitCommitAll() {
        return gitCommitAll("commit");
    }

    public GitRule gitCommitAll(final String commitMessage) {
        execInWorkingPath("git add --all");
        execInWorkingPath("git -c \"user.name=java\" -c \"user.email=java@java.org\" commit -am \"" + commitMessage + "\"");
        return this;
    }

    public GitRule createFile(String filename, List<String> lines) throws FileNotFoundException, UnsupportedEncodingException {
        try (PrintWriter writer = new PrintWriter(Paths.get(workingPath.getAbsolutePath(), filename).toFile(), "UTF-8")) {
            lines.forEach(writer::println);
        }
        return this;
    }

    public void execInWorkingPath(String cmd) {
        List<String> commands = Arrays.asList("/bin/sh", "-c", "cd " + workingPath.getAbsolutePath() + ";" + cmd);
        exec(commands.toArray(new String[0]));
    }


    private void exec(String[] command) {
        try {

            Process proc = rt.exec(command, new String[]{}, workingPath);

            PrintOutput errorReported = new PrintOutput(proc.getErrorStream(), "ERROR");
            PrintOutput outputMessage = new PrintOutput(proc.getInputStream(), "OUTPUT");
            errorReported.start();
            outputMessage.start();
            int result = proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Repository buildRepository() throws IOException {
        return InvestigateLog.buildRepository(Paths.get(getWorkingPath().getAbsolutePath(), "/.git").toFile());
    }

    private class PrintOutput extends Thread {
        InputStream is = null;

        PrintOutput(InputStream is, String type) {
            this.is = is;
        }

        public void run() {
            String s = null;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                while ((s = br.readLine()) != null) {
                    System.out.println(s);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
