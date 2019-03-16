package org.investigit.model;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;

import java.util.HashMap;
import java.util.Map;

public class Commit {

    private final Map<String, ModificationType> files;
    private final RevCommit revCommit;

    public Commit(RevCommit rev) {
        this(rev, new HashMap<>());
    }

    public Commit(RevCommit rev, Map<String, ModificationType> files) {
        revCommit = rev;
        this.files = files;
    }

    public String getShortMessage() {
        return revCommit.getShortMessage();
    }

    public RevTree getTree() {
        return revCommit.getTree();
    }

    public void setFiles(Map<String, ModificationType> files) {
        this.files.clear();
        files.forEach((filename, modification) -> this.files.put(filename, modification));
    }

    public Map<String, ModificationType> getFiles() {
        return files;
    }
}
