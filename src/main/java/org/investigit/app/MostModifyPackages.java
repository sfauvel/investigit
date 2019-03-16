package org.investigit.app;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.investigit.InvestigateLog;
import org.investigit.model.Commit;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * Show packages with most commits.
 */
public class MostModifyPackages {
    final private static PrintStream out = System.out;

    public static void main(String[] args) throws IOException, GitAPIException {
        InvestigateLog showLog = new InvestigateLog(out);

        File gitDir = new File(Config.path + "/.git");
        Repository repo = showLog.buildRepository(gitDir);

        List<Commit> commits = showLog.getCommits(repo, Config.COMMIT_LIMIT);

        Map<String, Integer> collect = showLog.commitByPackage(commits);
        collect
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> System.out.println(e.getKey() + ": " + e.getValue()));

    }
}
