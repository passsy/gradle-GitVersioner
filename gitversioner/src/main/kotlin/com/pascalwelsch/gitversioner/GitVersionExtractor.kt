package com.pascalwelsch.gitversioner

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.gradle.api.Project

internal class GitVersionExtractor(val project: Project) {

    internal val currentSha1: String by lazy { "git rev-parse HEAD".execute().text().trim() }

    internal val currentBranch: String by lazy { "git symbolic-ref --short -q HEAD".execute().text().trim() }

    internal val localChanges: Int by lazy { "git diff-index HEAD".execute().text().trim().lines().size }

    internal val initialCommitDate: Long by lazy {
        "git log --pretty=format:'%at' --max-parents=0".execute().text().replace("\'",
                "").trim().toLong()
    }

    internal val commitCount: Int by lazy { commitsUpTo("HEAD").count() }

    internal val isGitProjectReady: Boolean by lazy {
        val result = "git status".execute()

        when (result.exitValue()) {
            0 -> true
            69 -> {
                println("git returned with error 69\n" +
                        "If you are a mac user that message is telling you is that you need to open the " +
                        "application XCode on your Mac OS X/macOS and since it hasn’t run since the last " +
                        "update, you need to accept the new license EULA agreement that’s part of the " +
                        "updated XCode.")
                false
            }
            else -> {
                println("ERROR: can't generate a git version, this is not a git project")
                println(" -> Not a git repository (or any of the parent directories): .git")
                false
            }
        }
    }

    internal fun commitsUpTo(rev: String): List<String> {
        var result = "git rev-list $rev".execute()
        if (result.exitValue() != 0) {
            result = "git rev-list origin/$rev".execute()
        }

        val list = result.text().trim().lines().map { it.trim() }

        println("git rev-list $rev -> ${list.count()}")
        return list
    }

    internal fun commitsFrom(from: String, to: String): List<String> {
        var result = "git rev-list $from..$to".execute()
        if (result.exitValue() != 0) {
            result = "git rev-list origin/$from..$to".execute()
        }

        val list = result.text().trim().lines().map { it.trim() }

        println("git rev-list $from..$to -> ${list.count()}")
        return list
    }

    private fun Process.text(): String = ProcessGroovyMethods.getText(this)

    private fun String.execute(): Process {
        val status = ProcessGroovyMethods.execute(this, emptyArray<String>(), project.projectDir)
        status.waitFor()
        return status
    }
}

