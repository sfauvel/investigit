package org.investigit.model;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
        exec(repo).forEach(out::println);;
    }

    public Stream<String> exec(Repository repo) throws GitAPIException {
        Iterable<RevCommit> log = new Git(repo).log().call();
        return StreamSupport.stream(log.spliterator(), false)
                .limit(limit)
                .map(this::format);
    }

    protected String format(RevCommit rev) {
        Date authorDate = rev.getAuthorIdent().getWhen();

        String format = new SimpleDateFormat(dateFormat).format(authorDate);
        return rev.getName().substring(0, 10) + " " + format + ": " + rev.getShortMessage();
    }
}
