package org.spike;

import java.util.List;
import java.util.stream.Collectors;

public class NoTestWithCommit {
    public List<Commit> exec(List<Commit> commits) {

        return commits.stream()
                .filter(commit -> !containTest(commit))
                .collect(Collectors.toList());
    }

    private boolean containTest(Commit commit) {
        return commit.getFiles().keySet().stream().anyMatch(filename -> filename.endsWith("Test.java"));
    }
}
