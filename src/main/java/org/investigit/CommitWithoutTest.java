package org.investigit;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.investigit.model.Commit;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CommitWithoutTest {


    private final int limit;
    private Predicate<String> excludeFilter = name -> false;

    public CommitWithoutTest(int limit) {
        this.limit = limit;
    }

    public CommitWithoutTest() {
        this(1000);
    }

    public CommitWithoutTest excludeFiles(Predicate<String> filter) {
        this.excludeFilter = filter;
        return this;
    }

    public List<Commit> exec(Repository repo) throws GitAPIException, IOException {
        InvestigateLog showLog = new InvestigateLog(System.out);
        return exec(showLog.getCommits(repo));
    }

    public List<Commit> exec(List<Commit> commits) {

        return commits.stream()
                .limit(limit)
                .filter(this::fileExcluded)
                .filter(commit -> !containTest(commit))
                .collect(Collectors.toList());
    }

    private boolean fileExcluded(Commit commit) {
        return commit.getFiles().keySet().stream()
                .filter(excludeFilter.negate()::test)
                .count() > 0;
    }

    private boolean containTest(Commit commit) {
        return commit.getFiles().keySet().stream().anyMatch(filename -> filename.endsWith("Test.java"));
    }
}
