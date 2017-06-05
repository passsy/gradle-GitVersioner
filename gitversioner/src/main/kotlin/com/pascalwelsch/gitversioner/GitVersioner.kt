package com.pascalwelsch.gitversioner

import org.gradle.api.Project
import java.util.*
import kotlin.system.measureTimeMillis


public open class GitVersioner {

    internal lateinit var rootProject: Project

    private val gitExtractor: GitVersionExtractor by lazy { GitVersionExtractor(rootProject!!) }

    var defaultBranch: String = "master"

    var formatter: ((GitVersionInformation) -> CharSequence) = { "default formatter!" }

    fun printYourself() {

        val took = measureTimeMillis {

            println("\tdefaultBranch: $defaultBranch")
            val info = GitVersionInformation(branch = gitExtractor.currentBranch, isSnapshot = gitExtractor.localChanges > 0,
                    localChangesCount = gitExtractor.localChanges)
            val name = formatter.invoke(info)
            println("\tformatter: $formatter => $name")

            println("\tversionName: ${versionName()}")
            println("\tversionCode: ${versionCode()}\n")
            with(gitExtractor) {
                println("\tbranch $currentBranch")
                println("\tsha1 $currentSha1")
                println("\tinitial commit date ${Date(initialCommitDate * 1000)}")
                println("\tHEAD commit date ${Date(headCommitDate * 1000)}")
                println("\tlocal changes $localChanges")

                val allCommits = commitsUpTo("HEAD")
                val defaultBranchCommits = commitsUpTo(defaultBranch)

                val split = allCommits.groupBy { defaultBranchCommits.contains(it) }

                val commitsOnFeatureBranch = split[false] ?: emptyList()
                val commitsOnDefaultBranch = split[true] ?: emptyList()

                println("\tall commit count ${allCommits.count()}")
                println("\tcommits on feature branch ${commitsOnFeatureBranch.count()}")

                println("\tlatest default branch commit: ${commitsOnDefaultBranch.first()}")
            }
        }

        println("\ntook: ${took}ms")
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
        val localChangesCount: Int
)

public interface VersionFormatter {
    fun format(info: GitVersionInformation): String
}