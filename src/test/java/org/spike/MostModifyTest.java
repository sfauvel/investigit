package org.spike;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MostModifyTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Rule
    public GitRule gitRule = new GitRule();

    private ShowLog showLog;

    @Before
    public void init() {
        gitRule.setWorkingPath(folder.getRoot());

        showLog = new ShowLog(gitRule.printStream);
    }

    @Test
    public void should_find_commit_number_by_file() throws IOException, GitAPIException {
        gitRule.execInWorkingPath("git init");

        gitRule.createFile("fileA.txt", Arrays.asList("line A"))
                .gitCommitAll();
        gitRule.createFile("fileB.txt", Arrays.asList("line A"))
                .gitCommitAll();
        gitRule.createFile("fileA.txt", Arrays.asList("line A", "line B"))
                .gitCommitAll();
        gitRule.createFile("fileC.txt", Arrays.asList("line A"))
                .createFile("fileB.txt", Arrays.asList("line A", "line B"))
                .gitCommitAll();
        gitRule.createFile("fileA.txt", Arrays.asList("line A", "line B", "line C"))
                .createFile("fileB.txt", Arrays.asList("line A", "line B", "line C"))
                .gitCommitAll();
        gitRule.createFile("fileA.txt", Arrays.asList("line A", "line B", "line C", "line D"))
                .gitCommitAll();


        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());
        MostModify mostModify = new MostModify();
        List<Map.Entry<String, Integer>> list = mostModify.exec(commits);


        assertEquals("fileA.txt", list.get(0).getKey());
        assertEquals(4, list.get(0).getValue().intValue());

        assertEquals("fileB.txt", list.get(1).getKey());
        assertEquals(3, list.get(1).getValue().intValue());

        assertEquals("fileC.txt", list.get(2).getKey());
        assertEquals(1, list.get(2).getValue().intValue());

    }

    @Test
    public void should_filter_file() throws IOException, GitAPIException {
        gitRule.execInWorkingPath("git init");

        gitRule.createFile("fileA.txt", Arrays.asList("line A"))
                .createFile("fileB.txt", Arrays.asList("line A"))
                .createFile("file.properties", Arrays.asList("line A"))
                .gitCommitAll();

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        MostModify mostModify = new MostModify()
                .filter(filename -> filename.endsWith(".java"));

        List<Map.Entry<String, Integer>> list = mostModify.exec(commits);

        assertTrue(list.stream()
                .map(e -> e.getKey())
                .noneMatch(filename -> filename.equals("file.properties")));

    }


    @Test
    public void should_get_only_file_commited_more_than_x_times() throws IOException, GitAPIException {
        gitRule.execInWorkingPath("git init");

        gitRule.createFile("fileA.txt", Arrays.asList("A")).gitCommitAll();
        gitRule.createFile("fileA.txt", Arrays.asList("AB")).gitCommitAll();
        gitRule.createFile("fileB.txt", Arrays.asList("a")).gitCommitAll();
        gitRule.createFile("fileA.txt", Arrays.asList("ABC")).gitCommitAll();
        gitRule.createFile("fileB.txt", Arrays.asList("ab")).gitCommitAll();

        List<Commit> commits = showLog.getCommits(gitRule.buildRepository());

        MostModify mostModify = new MostModify()
                .atLeast(3);

        List<Map.Entry<String, Integer>> list = mostModify.exec(commits);

        assertEquals(1, list.size());
        assertEquals("fileA.txt", list.get(0).getKey());

    }

    @Test
    public void should_group() {
        List<Map.Entry<String, Integer>> commitsA = new ArrayList();
        commitsA.add(new AbstractMap.SimpleEntry<>("A",  10));
        commitsA.add(new AbstractMap.SimpleEntry<>("B",  8));
        commitsA.add(new AbstractMap.SimpleEntry<>("C",  15));


        List<Map.Entry<String, Integer>> commitsB = new ArrayList();
        commitsB.add(new AbstractMap.SimpleEntry<>("B",  4));
        commitsB.add(new AbstractMap.SimpleEntry<>("A",  2));
        commitsB.add(new AbstractMap.SimpleEntry<>("C",  12));


        MostModify mostModify = new MostModify();
        Map<String, List<Integer>> commits = mostModify.group(Arrays.asList(commitsA, commitsB));

        Assert.assertEquals(Arrays.asList(10, 2), commits.get("A"));
        Assert.assertEquals(Arrays.asList(8, 4), commits.get("B"));
        Assert.assertEquals(Arrays.asList(15, 12), commits.get("C"));
    }

    @Test
    public void should_group_when_not_all_keys() {
        List<Map.Entry<String, Integer>> commitsA = new ArrayList();
        commitsA.add(new AbstractMap.SimpleEntry<>("A",  10));
        commitsA.add(new AbstractMap.SimpleEntry<>("C",  15));


        List<Map.Entry<String, Integer>> commitsB = new ArrayList();
        commitsB.add(new AbstractMap.SimpleEntry<>("B",  4));
        commitsB.add(new AbstractMap.SimpleEntry<>("A",  2));


        MostModify mostModify = new MostModify();
        Map<String, List<Integer>> commits = mostModify.group(Arrays.asList(commitsA, commitsB));

        Assert.assertEquals(Arrays.asList(10, 2), commits.get("A"));
        Assert.assertEquals(Arrays.asList(0, 4), commits.get("B"));
        Assert.assertEquals(Arrays.asList(15, 0), commits.get("C"));
    }

}

