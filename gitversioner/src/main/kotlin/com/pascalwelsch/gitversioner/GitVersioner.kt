package com.pascalwelsch.gitversioner

import org.gradle.api.Project
import java.util.*


public open class GitVersioner {

    internal lateinit var rootProject: Project

    private val gitExtractor: GitVersionExtractor by lazy { GitVersionExtractor(rootProject!!) }

    var defaultBranch: String = "master"

    var formatter: VersionFormatter = object : VersionFormatter {
        override fun format(info: GitVersionInformation): String {
            return "default formatter!"
        }
    }

    fun printYourself() {
        println("\tdefaultBranch: $defaultBranch")
        val info = GitVersionInformation(branch = "master", isSnapshot = true,
                localChangesCount = 0)
        println("\tformatter: $formatter => ${formatter.format(info)}")

        println("\tversionName: ${versionName()}")
        println("\tversionCode: ${versionCode()}")
        with(gitExtractor) {
            println("\tbranch $currentBranch")
            println("\tsha1 $currentSha1")
            println("\tinitial commit date ${Date(initialCommitDate * 1000)}")
            println("\tlocal changes $localChanges")

            val featureBranchCommitCount = commitsFrom(defaultBranch, currentSha1).count()
            println("\tfeature branch commit count $featureBranchCommitCount")
            println("\tcommit count $commitCount")
        }
    }

    fun versionCode(): Int {
        if (gitExtractor.isGitProjectReady) {
            return 20
        }
        return 1000
    }

    fun versionName(): String {
        if (gitExtractor.isGitProjectReady) {
            return "git ready"
        }
        return "name-1000"
    }

}

public data class GitVersionInformation(
        val branch: String,
        val isSnapshot: Boolean,
        val localChangesCount: Long
)

public interface VersionFormatter {
    fun format(info: GitVersionInformation): String
}