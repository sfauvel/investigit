package org.investigit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.investigit.model.Commit;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class CommitWithoutTest {


    private final int limit;

    public CommitWithoutTest(int limit) {
        this.limit = limit;
    }

    public CommitWithoutTest() {
        this(1000);
    }

    public List<Commit> exec(Repository repo) throws GitAPIException, IOException {
        InvestigateLog showLog = new InvestigateLog(System.out);
        return exec(showLog.getCommits(repo));
    }

    public List<Commit> exec(List<Commit> commits) {

        return commits.stream()
                .limit(limit)
                .filter(commit -> !containTest(commit))
                .collect(Collectors.toList());
    }

    private boolean containTest(Commit commit) {
        return commit.getFiles().keySet().stream().anyMatch(filename -> filename.endsWith("Test.java"));
    }
}
