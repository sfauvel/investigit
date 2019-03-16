package org.investigit.model;

public enum ModificationType {
    Add, Delete, Modify, Rename, Undefined;

    public String firstLetter() {
        return name().substring(0, 1);
    }
}
