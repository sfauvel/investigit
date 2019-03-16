package org.spike;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CommitMessage {
    private int limit;
    private String dateFormat;

    public CommitMessage() {
        this(1000, "dd/MM/yyyy");
    }

    public CommitMessage(int limit, String dateFormat) {
        this.limit = limit;
        this.dateFormat = dateFormat;
    }

    public void exec(Repository repo, PrintStream out) throws GitAPIException {
        Iterable<RevCommit> log = new Git(repo).log().call();
        for (RevCommit rev : log) {
            out.println(format(rev));
            if (limit-- < 0) return;
        }
    }

    protected String format(RevCommit rev) {
        Date authorDate = rev.getAuthorIdent().getWhen();

        String format = new SimpleDateFormat(dateFormat).format(authorDate);
        return rev.getName().substring(0, 10) + " " + format + ": " + rev.getShortMessage();
    }
}
