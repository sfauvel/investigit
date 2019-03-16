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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Display a table with commit number by packages.
 * Each column group a number of commits (COMMIT_PER_COLUMN).
 */
public class ModifyPackagesHistory {
    final private static PrintStream out = System.out;
    private static final int COMMIT_PER_COLUMN = 50;

    public static void main(String[] args) throws IOException, GitAPIException {
        InvestigateLog investigate = new InvestigateLog(out);

        File gitDir = new File(Config.path + "/.git");
        Repository repo = investigate.buildRepository(gitDir);

        List<Commit> commits = investigate.getCommits(repo, Config.COMMIT_LIMIT);

        List<Map<String, Integer>> collectSplit = IntStream.range(0, Math.floorDiv(commits.size(), COMMIT_PER_COLUMN))
                .mapToObj(i -> commits.subList(i * COMMIT_PER_COLUMN, (i + 1) * COMMIT_PER_COLUMN))
                .map(investigate::commitByPackage)
                .collect(Collectors.toList());

        Map<String, List<Integer>> stringListMap = new MostModify().groupListOfMap(collectSplit);

        stringListMap.forEach((key, value) -> System.out.println(key + " | "
                + value.stream().map(String::valueOf).collect(Collectors.joining("|"))));


    }
}
