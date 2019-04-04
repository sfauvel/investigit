package org.investigit.model;

import org.eclipse.jgit.revwalk.RevCommit;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class Formatter {
    public static String commitAndFiles(Commit commit) {
        return commit.getShortMessage() +
                commit.getFiles().keySet().stream()
                        .map(name -> "\t-"+name)
                        .collect(Collectors.joining("\n", "\n", ""));
    }

    public static String idAndMessage(RevCommit rev, String dateFormat) {
        Date authorDate = rev.getAuthorIdent().getWhen();

        String format = new SimpleDateFormat(dateFormat).format(authorDate);
        return rev.getName().substring(0, 10) + " " + format + ": " + rev.getShortMessage();
    }
}
