package org.investigit.app;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.investigit.CommitWithoutTest;
import org.investigit.InvestigateLog;
import org.investigit.model.Formatter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Retrieve commits from repository.
 */
public class CommitWithoutTestApp {


    final private static PrintStream out = System.out;
    final private static File gitDir = new File(Config.path + "/.git");

    public static void main(String[] args) throws IOException, GitAPIException {

        Repository repo = new InvestigateLog(out).buildRepository(gitDir);

        CommitWithoutTest commitWithoutTest = new CommitWithoutTest(100);
        commitWithoutTest.exec(repo).stream()
                .map(Formatter::commitAndFiles)
                .forEach(out::println);

    }


}
