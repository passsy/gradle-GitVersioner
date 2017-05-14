package com.pascalwelsch.gitversioner

class GitVersion {

    String name

    int version = 0

    String branchName

    String shortBranch = ""

    int branchVersion = 0

    int localChanges = 0

    String commit

    @Override
    String toString() {
        if (name == null || name.isEmpty()) {
            return super.toString() +
                    "name='" + name + '\'' +
                    ", version=" + version +
                    ", branchName='" + branchName + '\'' +
                    ", shortBranch='" + shortBranch + '\'' +
                    ", branchVersion=" + branchVersion +
                    '}';
        }
        return name;

    }
}
