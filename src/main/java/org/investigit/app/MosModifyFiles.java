package org.investigit.app;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.investigit.InvestigateLog;
import org.investigit.MostModify;
import org.investigit.model.Commit;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class MosModifyFiles {
    final private static PrintStream out = System.out;

    /** Show only files with more commits than this limit. */
    private static final int AT_LEAST_COMMIT = 20;

    public static void main(String[] args) throws IOException, GitAPIException {
        InvestigateLog showLog = new InvestigateLog(out);

        File gitDir = new File(Config.path + "/.git");
        Repository repo = showLog.buildRepository(gitDir);


        List<Commit> commits = showLog.getCommits(repo, Config.COMMIT_LIMIT);
        List<Map.Entry<String, Integer>> commitList = new MostModify()
                .filter(file -> file.endsWith(".java"))
                .atLeast(AT_LEAST_COMMIT)
                .exec(commits);

        commitList
                .stream()
                .map(e -> e.getValue() + ": " + e.getKey())
                .forEach(out::println);
    }
}
