package org.spike;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class NoTestWithCommitTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public GitRule gitRule = new GitRule();

    private ShowLog showLog;

    private NoTestWithCommit noTestWithCommit = new NoTestWithCommit();

    @Before
    public void init() {
        gitRule.setWorkingPath(folder.getRoot());
        gitRule.execInWorkingPath("git init");

        showLog = new ShowLog(gitRule.printStream);
    }

    @Test
    public void should_find_all_commits_if_no_tests() throws IOException, GitAPIException {
        gitRule.createFile("fileA.java", Arrays.asList("line A"))
                .gitCommitAll();

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        List<Commit> commitsWithoutTest = noTestWithCommit.exec(commits);

        assertEquals(1, commitsWithoutTest.size());

    }

    @Test
    public void should_find_no_commits_if_a_test_is_commited_with_other_files() throws IOException, GitAPIException {
        gitRule.createFile("fileA.java", Arrays.asList("line A"))
                .createFile("fileATest.java", Arrays.asList("line A"))
                .gitCommitAll();

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        List<Commit> commitsWithoutTest = noTestWithCommit.exec(commits);

        assertEquals(0, commitsWithoutTest.size());

    }

    @Test
    public void should_find_only_commits_with_no_tests() throws IOException, GitAPIException {
        gitRule.createFile("fileA.java", Arrays.asList("line A"))
                .createFile("fileATest.java", Arrays.asList("line A"))
                .gitCommitAll("A");
        gitRule.createFile("fileB.java", Arrays.asList("line A"))
                .gitCommitAll("B");
        gitRule.createFile("fileC.java", Arrays.asList("line A"))
                .createFile("fileCTest.java", Arrays.asList("line A"))
                .gitCommitAll("C");

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        List<Commit> commitsWithoutTest = noTestWithCommit.exec(commits);

        assertEquals(1, commitsWithoutTest.size());
        assertEquals("B", commitsWithoutTest.get(0).getShortMessage());

    }
}