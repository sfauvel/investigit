package org.investigit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.investigit.app.Config;
import org.investigit.model.Commit;
import org.investigit.model.CommitMessage;
import org.investigit.model.ModificationType;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.summingInt;

/**
 * Retrieve commits from repository.
 */
public class InvestigateLog {


    final private PrintStream out;


    public static Repository buildRepository(File gitDir) throws IOException {
        return new FileRepositoryBuilder()
                .setGitDir(gitDir)
                .setMustExist(true)
                .build();
    }

    public InvestigateLog(PrintStream out) {
        this.out = out;
    }

    public Map<String, Integer> commitByPackage(List<Commit> commits) {
        List<Entry<String, Integer>> commitList = new MostModify()
                .filter(file -> file.endsWith(".java"))
                .exec(commits);

        // Group by package
        return commitList.stream()
                .collect(Collectors.groupingBy(e1 -> new File(e1.getKey()).getParent(), summingInt(Entry::getValue)));
    }

    private static RevCommit getHeadCommit(Repository repository) throws Exception {
        try (Git git = new Git(repository)) {
            Iterable<RevCommit> history = git.log().setMaxCount(1).call();
            return history.iterator().next();
        }
    }

    public void displayCommit(List<Commit> commits) {
        for (Commit commit : commits) {
            out.println("\nCommit: " + commit.getTree().getName()/* + ", name: " + rev.getName() + ", id: " + rev.getId().getName() */);
            out.println("Message: " + commit.getShortMessage());
            commit.getFiles().entrySet().forEach(e -> out.println(e.getValue() + ": " + e.getKey()));
        }
    }

    private void modifyTogether(List<Commit> commits) {
        HashMap<String, Integer> coupledFiles = new HashMap<>();
        for (Commit commit : commits) {
            Set<String> files = commit.getFiles().keySet();
            for (String fileA : files) {
                for (String fileB : files) {
                    if (fileA != fileB) {
                        String key1 = fileA + " | " + fileB;
                        String key2 = fileB + " | " + fileA;
                        String key = key1.compareTo(key2) > 0 ? key1 : key2;
                        coupledFiles.put(key, coupledFiles.getOrDefault(key, 0) + 1);
                    }
                }
            }
        }
        coupledFiles.entrySet().stream()
                .filter(e -> e.getValue() > 200 * 2)
                .map(e -> e.getValue() / 2 + ":" + e.getKey())
                .forEach(out::println);

    }

    private void commitCalendarToFile(List<Commit> commits) throws FileNotFoundException {

        List<String> filenames = commits.stream()
                .flatMap(commit -> commit.getFiles().keySet().stream())
                .distinct()
                .collect(Collectors.toList());


//        PrintStream out = out;
        PrintStream out = new PrintStream(new File("output.txt"));

        out.println(filenames.stream().collect(Collectors.joining("|")));
        for (Commit commit : commits) {

            out.println(
                    filenames.stream()
                            .map(filename -> commit.getFiles().getOrDefault(filename, ModificationType.Undefined))
                            .map(ModificationType::firstLetter)
                            .collect(Collectors.joining("|")));
        }

    }

    private void commitCalendarToImage(List<Commit> commits) throws FileNotFoundException {

        List<String> filenames = commits.stream()
                .flatMap(commit -> commit.getFiles().keySet().stream())
                .distinct()
                .collect(Collectors.toList());


        List<String> filenamesToShow = filenames.stream()
                .filter(name -> name.startsWith("src/net/sf/freecol"))
                .collect(Collectors.toList());


        List<Commit> commitsToShow = commits.subList(0, 1000);

        createImage(commitsToShow, filenamesToShow, "history");
    }

    private void createImage(List<Commit> commits, List<String> filenames, final String outputFilename) {
        out.println("========================================");
        filenames.forEach(out::println);
        out.println("========================================");
        out.println("Create image with " + commits.size() + " commits and " + filenames.size() + " files");
        out.println("========================================");

        BufferedImage image = new BufferedImage(commits.size(), filenames.size(), BufferedImage.TYPE_INT_RGB);
        for (int commitAxe = 0; commitAxe < commits.size(); commitAxe++) {
            for (int fileAxe = 0; fileAxe < filenames.size(); fileAxe++) {
                String filename = filenames.get(fileAxe);

                boolean isModified = commits.get(commitAxe).getFiles().containsKey(filename);

                Graphics2D g = image.createGraphics();// not sure on this line, but this seems more right
                g.setColor(isModified ? Color.BLACK : Color.WHITE);
                g.fillRect(fileAxe, commitAxe, 1, 1); // give the whole image a white background

            }
        }

        try {
            ImageIO.write(image, "png", new File(outputFilename + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Commit> getCommits(Repository repository) throws GitAPIException, IOException {
        return getCommits(repository, -1);
    }

    public List<Commit> getCommits(Repository repository, long limit) throws GitAPIException, IOException {

        RenameDetector rd = new RenameDetector(repository);
        int renameScore = rd.getRenameScore();

        List<Commit> commits = new ArrayList<>();
        try (Git git = new Git(repository)) {

            long nb = 0;
            for (RevCommit currentCommit : git.log().call()) {

                if (!commits.isEmpty()) {
                    Commit commit = last(commits);
                    List<DiffEntry> call = getDiffEntries(git, commit.getTree(), currentCommit.getTree());
                    commit.setFiles(toFileModifications(call, renameScore));

                }

                commits.add(new Commit(currentCommit));

                nb++;
                if (nb == limit) {
                    return commits;
                }
            }

            Commit commit = last(commits);
            List<DiffEntry> call = getDiffEntries(git, commit.getTree());
            commit.setFiles(toFileModifications(call, renameScore));

        }

        return commits;
    }

    private Commit last(List<Commit> commits) {
        return commits.get(commits.size() - 1);
    }

    private Map<String, ModificationType> toFileModifications(List<DiffEntry> call, int renameScore) {
        return call.stream().collect(Collectors.toMap(
                diffEntry -> diffEntry.getChangeType() == DiffEntry.ChangeType.DELETE ? diffEntry.getOldPath() : diffEntry.getNewPath(),
                diffEntry -> getModificationType(diffEntry, renameScore)
        ));
    }

    private List<DiffEntry> getDiffEntries(Git git, RevTree firstTree) throws IOException, GitAPIException {
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            return getDiffEntries(git,
                    new EmptyTreeIterator(),
                    new CanonicalTreeParser(null, reader, firstTree));
        }
    }

    private List<DiffEntry> getDiffEntries(Git git, RevTree firstTree, RevTree tree) throws IOException, GitAPIException {
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
            List<DiffEntry> diffEntries = getDiffEntries(git,
                    new CanonicalTreeParser(null, reader, tree),
                    new CanonicalTreeParser(null, reader, firstTree));

            RenameDetector rd = new RenameDetector(git.getRepository());
            rd.addAll(diffEntries);
            return rd.compute(reader, null);
        }
    }

    private List<DiffEntry> getDiffEntries(Git git, AbstractTreeIterator oldTree, AbstractTreeIterator newTree) throws GitAPIException {
        return git.diff()
                .setOldTree(oldTree)
                .setNewTree(newTree)
                .call();
    }

    private ModificationType getModificationType(DiffEntry diffEntry, int renameScore) {
        if (diffEntry.getScore() > renameScore) {
            return ModificationType.Rename;
        }

        switch (diffEntry.getChangeType()) {
            case ADD:
                return ModificationType.Add;
            case DELETE:
                return ModificationType.Delete;
            case MODIFY:
                return ModificationType.Modify;
            default:
                return ModificationType.Undefined;
        }
    }

    private static <T> Set<T> mergeSets(Set<T>... sets) {
        Set<T> ids = new HashSet<>();
        for (Set<T> set : sets) {
            ids.addAll(set);
        }
        return ids;
    }

    private static AbstractMap.SimpleEntry<String, ModificationType> addFilename(
            String id,
            Map<String, ModificationType> files,
            Map<String, String> filesInLastCommit,
            Map<String, String> filesInCurrentCommit) {

        ModificationType type = ModificationType.Undefined;
        String filename;
        if (filesInCurrentCommit.containsKey(id)) {
            filename = filesInCurrentCommit.get(id);
            type = files.containsKey(filename) ? ModificationType.Modify : ModificationType.Delete;
        } else if (filesInLastCommit.containsKey(id)) {
            filename = filesInLastCommit.get(id);
            type = files.containsKey(filename) ? ModificationType.Modify : ModificationType.Add;
        } else {
            throw new RuntimeException("Id not found: " + id);
        }

        return new AbstractMap.SimpleEntry<String, ModificationType>(filename, type);

    }

}
