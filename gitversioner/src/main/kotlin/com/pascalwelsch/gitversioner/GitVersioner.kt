package com.pascalwelsch.gitversioner

import java.util.concurrent.TimeUnit

private val YEAR_IN_SECONDS = TimeUnit.DAYS.toSeconds(365)

public val NO_CHANGES = LocalChanges(0, 0, 0)

public open class GitVersioner internal constructor(private val gitInfoExtractor: GitInfoExtractor) {

    public var baseBranch: String = "master"

    public var yearFactor: Int = 1000

    public var addSnapshot: Boolean = true

    public var addLocalChangesDetails: Boolean = true

    public var formatter: ((GitVersioner) -> CharSequence) = DEFAULT_FORMATTER

    public var shortNameFormatter: ((GitVersioner) -> CharSequence) = DEFAULT_SHORT_NAME_FORMATTER

    //TODO add offset
    /**
     * base branch commit count + [timeComponent]
     */
    public fun versionCode(): Int {
        if (!gitInfoExtractor.isGitProjectReady) {
            return -1 // this is actually a valid android versionCode
        }

        val commitComponent = baseBranchCommits.size
        return commitComponent + timeComponent
    }

    /**
     * string representation powered by [formatter]
     */
    public fun versionName(): String {
        if (!gitInfoExtractor.isGitProjectReady) {
            return "undefined"
        }
        try {
            return formatter(this).toString()
        } catch (e: Throwable) {
            println("formatter failed to generate a correct name, using default formatter")
            return DEFAULT_FORMATTER(this).toString()
        }
    }

    /**
     * the current local changes (files changed, additions, deletions). [NO_CHANGES] when no changes detected
     */
    public val localChanges: LocalChanges by lazy {
        if (!gitInfoExtractor.isGitProjectReady) NO_CHANGES else gitInfoExtractor.localChanges
    }

    /**
     * the name of the branch HEAD is currently on
     */
    public val branchName: String?
            = if (!gitInfoExtractor.isGitProjectReady) null else gitInfoExtractor.currentBranch

    /**
     * all commits in [baseBranch] without the [featureBranchCommits]
     */
    public val baseBranchCommitCount: Int by lazy { baseBranchCommits.count() }

    /**
     * commits on feature branch not in [baseBranch]
     */
    public val featureBranchCommitCount: Int by lazy { featureBranchCommits.count() }

    /**
     * all commits together, from initial commit to HEAD
     */
    public val commitCount: Int by lazy { baseBranchCommitCount + featureBranchCommitCount }

    /**
     * full sha1 of current commit
     *
     * @see [currentSha1Short]
     */
    public val currentSha1: String? by lazy { gitInfoExtractor.currentSha1 }

    /**
     * 7 char sha1 of current commit
     */
    public val currentSha1Short: String? by lazy { gitInfoExtractor.currentSha1?.take(7) }

    /**
     * [yearFactor] based time component from initial commit to [featureBranchOriginCommit]
     */
    public val timeComponent: Int by lazy {
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
    public val featureBranchOriginCommit: String? by lazy { baseBranchCommits.firstOrNull() }


    /**
     * commits of base branch in history of current commit (HEAD).
     */
    private val baseBranchCommits: List<String> by lazy {
        val baseCommits = gitInfoExtractor.commitsUpTo(baseBranch)
        baseCommits.forEach { baseCommit ->
            if (gitInfoExtractor.commitsToHead.contains(baseCommit)) {
                return@lazy baseCommits
            }
        }

        return@lazy emptyList<String>()
    }

    /**
     * commits on the feature branch not merged into [baseBranch]
     */
    private val featureBranchCommits: List<String> by lazy {
        gitInfoExtractor.commitsToHead.filter { !baseBranchCommits.contains(it) }
    }


    companion object {

        @JvmStatic
        public val DEFAULT_FORMATTER: ((GitVersioner) -> CharSequence) = { versioner ->
            with(versioner) {
                val sb = StringBuilder(versioner.versionCode().toString())
                val hasCommits = featureBranchCommitCount > 0 || baseBranchCommitCount > 0
                if (baseBranch != branchName && hasCommits) {
                    // add branch identifier for
                    val shortName = try {
                        shortNameFormatter(versioner)
                    } catch (e: Throwable) {
                        println("shortNameFormatter failed to generate a correct name, using default formatter")
                        DEFAULT_SHORT_NAME_FORMATTER(versioner).toString()
                    }

                    sb.append("-").append(shortName)
                }

                val featureCount = featureBranchCommits.count()
                if (featureCount > 0) {
                    sb.append("+").append(featureCount)
                }
                if (localChanges != NO_CHANGES) {
                    if (addSnapshot) {
                        sb.append("-SNAPSHOT")
                    }
                    if (addLocalChangesDetails) {
                        sb.append("(").append(localChanges).append(")")
                    }
                }
                sb.toString()
            }
        }

        @JvmStatic
        public val DEFAULT_SHORT_NAME_FORMATTER: ((GitVersioner) -> CharSequence) = { versioner ->
            var name: String? = null
            if (name == null) {
                // use branch name from git
                if (versioner.branchName != null && !versioner.branchName.isEmpty()) {
                    name = versioner.branchName
                }
            }
            if (name == null) {
                // get branch name on jenkins
                name = System.getenv("BRANCH") ?: System.getenv("BRANCH_NAME")
            }
            if (name == null) {
                // nothing found fallback to sha1
                name = versioner.currentSha1Short
            }
            if (name == null) {
                // fallback, i.e. when git not initialized
                name = "undefined"
            }

            name.replace("feature/", "").replace("addon/", "")
        }
    }
}

public data class LocalChanges(
        val filesChanged: Int = 0,
        val additions: Int = 0,
        val deletions: Int = 0
) {
    override fun toString(): String {
        return "$filesChanged +$additions -$deletions"
    }
}