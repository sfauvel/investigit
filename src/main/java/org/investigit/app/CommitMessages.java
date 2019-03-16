package org.investigit.app;

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
import org.investigit.InvestigateLog;
import org.investigit.MostModify;
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
public class CommitMessages {


    final private static PrintStream out = System.out;

    public static void main(String[] args) throws IOException, GitAPIException {
        InvestigateLog investigate = new InvestigateLog(out);

        File gitDir = new File(Config.path + "/.git");
        Repository repo = investigate.buildRepository(gitDir);

        CommitMessage commitMessage = new CommitMessage(1000, "dd/MM/yyyy");
        commitMessage.exec(repo, out);

    }


}
