package org.investigit;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.investigit.model.Commit;
import org.investigit.model.ModificationType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InvestigateLogTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public GitRule gitRule = new GitRule();

    private InvestigateLog showLog;

    @Before
    public void init() {
        gitRule.setWorkingPath(folder.getRoot());
        gitRule.execInWorkingPath("git init");

        showLog = new InvestigateLog(gitRule.printStream);
    }

    @Test
    public void should_find_all_commit_messages() throws Exception {

        gitRule.createFile("fileA.txt", Arrays.asList("The first line", "The second line"));
        gitRule.gitCommitAll("first commit");
        gitRule.createFile("fileB.txt", Arrays.asList("The first line", "The second line"));
        gitRule.gitCommitAll("second commit");

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());
        showLog.displayCommit(commits);

        String output = gitRule.getOutput();
        assertTrue("output=[" + output + "]", output.contains("first commit"));
        assertTrue("output=[" + output + "]", output.contains("second commit"));
    }

    @Test
    public void should_find_all_commit_with_files() throws IOException, GitAPIException, InterruptedException {
        gitRule.createFile("fileA.txt", Arrays.asList("File A", "The first line", "The second line"));
        gitRule.gitCommitAll("first commit");
        gitRule.createFile("fileB.txt", Arrays.asList("File B", "The first line", "The second line"));
        gitRule.gitCommitAll("second commit");
        gitRule.createFile("fileC.txt", Arrays.asList("File C", "The first line", "The second line"));
        gitRule.gitCommitAll("third commit");

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        int index = 0;
        assertEquals("third commit", commits.get(index).getShortMessage());
        assertEquals(ModificationType.Add, commits.get(index).getFiles().get("fileC.txt"));
        index++;
        assertEquals("second commit", commits.get(index).getShortMessage());
        assertEquals(ModificationType.Add, commits.get(index).getFiles().get("fileB.txt"));
        index++;
        assertEquals("first commit", commits.get(index).getShortMessage());
        assertEquals(ModificationType.Add, commits.get(index).getFiles().get("fileA.txt"));
    }


    @Test
    public void should_find_delete_files() throws IOException, GitAPIException, InterruptedException {
        gitRule.createFile("fileA.txt", Arrays.asList("File A", "The first line", "The second line"));
        gitRule.createFile("fileB.txt", Arrays.asList("File B", "The first line", "The second line"));
        gitRule.createFile("fileC.txt", Arrays.asList("File C", "The first line", "The second line"));
        gitRule.gitCommitAll("first commit");
        gitRule.execInWorkingPath("rm fileB.txt");
        gitRule.gitCommitAll("delete file");

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        Commit commit = commits.get(0);
        assertEquals("delete file", commit.getShortMessage());
        assertEquals(ModificationType.Delete, commit.getFiles().get("fileB.txt"));
    }

    @Test
    public void should_find_modify_files() throws IOException, GitAPIException, InterruptedException {
        gitRule.createFile("fileA.txt", Arrays.asList("File A", "The first line", "The second line"));
        gitRule.createFile("fileB.txt", Arrays.asList("File B", "The first line", "The second line"));
        gitRule.createFile("fileC.txt", Arrays.asList("File C", "The first line", "The second line"));
        gitRule.gitCommitAll("first commit");
        gitRule.createFile("fileB.txt", Arrays.asList("File B", "The first line", "The second line", "The third line"));
        gitRule.gitCommitAll("modify file");

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        Commit commit = commits.get(0);
        assertEquals("modify file", commit.getShortMessage());
        assertEquals(ModificationType.Modify, commit.getFiles().get("fileB.txt"));
    }

    @Test
    public void should_find_rename_files() throws IOException, GitAPIException, InterruptedException {
        gitRule.createFile("fileA.txt", Arrays.asList("File A", "The first line", "The second line"));
        gitRule.createFile("fileB.txt", Arrays.asList("File B", "The first line", "The second line"));
        gitRule.createFile("fileC.txt", Arrays.asList("File C", "The first line", "The second line"));
        gitRule.gitCommitAll("first commit");
        gitRule.execInWorkingPath("mv fileB.txt fileRename.txt");
        gitRule.gitCommitAll("rename file");

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        Commit commit = commits.get(0);
        assertEquals("rename file", commit.getShortMessage());
        assertEquals(ModificationType.Rename, commit.getFiles().get("fileRename.txt"));
//        assertEquals(InvestigateLog.ModificationType.Delete, commit.files.get("fileB.txt"));
//        assertEquals(InvestigateLog.ModificationType.Add, commit.files.get("fileRename.txt"));
    }

    @Test
    public void should_get_commits_on_current_branch() throws IOException, GitAPIException, InterruptedException {
        gitRule.createFile("fileA.txt", Arrays.asList("File A", "The first line", "The second line"))
            .gitCommitAll("A");

        gitRule.createFile("fileB.txt", Arrays.asList("File B", "The first line", "The second line"))
                .gitCommitAll("B");

        gitRule.execInWorkingPath("git checkout -b develop");

        gitRule.createFile("fileInDevelop.txt", Arrays.asList("File C", "The first line", "The second line"))
                .gitCommitAll("commit in develop");

        gitRule.execInWorkingPath("git checkout master");

        gitRule.createFile("fileInDevelop.txt", Arrays.asList("File C", "The first line", "The second line"))
                .gitCommitAll("C");

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());
        assertEquals(3, commits.size());
        assertEquals("C,B,A", commits.stream().map(Commit::getShortMessage).collect(Collectors.joining(",")));
    }


}
