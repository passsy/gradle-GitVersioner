package com.pascalwelsch.gitversioner

const val YEAR_IN_SECONDS = 60 * 60 * 24 * 365

val NO_CHANGES = LocalChanges(0, 0, 0)


public open class GitVersioner internal constructor(private val gitInfoExtractor: GitInfoExtractor) {

    var baseBranch: String = "master"

    var yearFactor: Int = 1000

    var formatter: ((GitVersioner) -> CharSequence) = { versioner ->
        var name = "${versioner.versionCode()}"
        if (baseBranch != branchName) {
            // TODO make branch name interceptable
            name += "-${versioner.branchName}"
        }

        val featureCount = versioner.featureBranchCommits.count()
        if (featureCount > 0) {
            name += "-$featureCount"
        }
        if (versioner.localChanges != NO_CHANGES) {
            name += "(${versioner.localChanges})"
        }
        name
    }

    /**
     * base branch commit count + [timeComponent]
     */
    fun versionCode(): Int {
        if (!gitInfoExtractor.isGitProjectReady) {
            return -1 // this is actually a valid android versionCode
        }
        return baseBranchCommits.count() + timeComponent
    }

    /**
     * string representation powered by [formatter]
     */
    fun versionName(): String {
        if (!gitInfoExtractor.isGitProjectReady) {
            return "undefined"
        }

        return formatter.invoke(this).toString()
    }

    val localChanges: LocalChanges by lazy {
        if (!gitInfoExtractor.isGitProjectReady) NO_CHANGES else gitInfoExtractor.localChanges
    }

    val branchName: String?
            = if (!gitInfoExtractor.isGitProjectReady) null else gitInfoExtractor.currentBranch

    val baseBranchCommits: List<String> by lazy { gitInfoExtractor.commitsUpTo(baseBranch) }

    val featureBranchCommits: List<String> by lazy {
        gitInfoExtractor.commitsToHead.filter { !baseBranchCommits.contains(it) }
    }

    val currentSha1: String? = gitInfoExtractor.currentSha1

    //TODO test
    val currentSha1Short: String? = gitInfoExtractor.currentSha1

    /**
     * [yearFactor] based time component from initial commit to [featureBranchOriginCommit]
     */
    val timeComponent: Int by lazy {
        if (!gitInfoExtractor.isGitProjectReady) return@lazy 0
        val latestBaseCommit = featureBranchOriginCommit ?: return@lazy 0

        val timeToHead = gitInfoExtractor.commitDate(
                latestBaseCommit) - gitInfoExtractor.initialCommitDate
        return@lazy (timeToHead * yearFactor / YEAR_IN_SECONDS + 0.5).toInt()
    }

    /**
     * last commit in base branch which is parent of HEAD, most likely where the
     * feature branch was created or the last base branch commit which was merged
     * into the feature branch
     */
    val featureBranchOriginCommit by lazy {
        val commit = baseBranchCommits.firstOrNull()
        if(commit != null) return@lazy commit




        null
    }
}

data class LocalChanges(
        val filesChanged: Int = 0,
        val additions: Int = 0,
        val deletions: Int = 0
) {
    override fun toString(): String {
        return if (filesChanged == 0) "no changes" else "+$additions -$deletions"
    }
}