package org.spike;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;

import java.util.HashMap;
import java.util.Map;

public class Commit {

    private final Map<String, ShowLog.ModificationType> files;
    private final RevCommit revCommit;

    public Commit(RevCommit rev) {
        this(rev, new HashMap<>());
    }

    public Commit(RevCommit rev, Map<String, ShowLog.ModificationType> files) {
        revCommit = rev;
        this.files = files;
    }

    public String getShortMessage() {
        return revCommit.getShortMessage();
    }

    public RevTree getTree() {
        return revCommit.getTree();
    }

    public void setFiles(Map<String, ShowLog.ModificationType> files) {
        this.files.clear();
        files.forEach((filename, modification) -> this.files.put(filename, modification));
    }

    public Map<String, ShowLog.ModificationType> getFiles() {
        return files;
    }
}
