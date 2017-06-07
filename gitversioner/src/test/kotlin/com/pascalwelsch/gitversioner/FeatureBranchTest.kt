package com.pascalwelsch.gitversioner

import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class FeatureBranchTest {

    @Test
    fun `on feature branch - few commits - local changes`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- feature/x, HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000),
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000), // <-- master
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("g" to "master", "X" to "feature/x"), localChanges)
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x+4-SNAPSHOT(3 +5 -7)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(4)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("g")
        }
    }

    @Test
    fun `on feature branch - few commits - local changes - attachDiffToSnapshot false`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- feature/x, HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000),
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000), // <-- master
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("g" to "master", "X" to "feature/x"), localChanges)
        val versioner = GitVersioner(git)

        versioner.attachDiffToSnapshot = false

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x+4-SNAPSHOT")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(4)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("g")
        }
    }

    @Test
    fun `on feature branch - few commits`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- feature/x, HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000),
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000), // <-- master
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("g" to "master", "X" to "feature/x"))
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x+4")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(4)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("g")
        }
    }

    @Test
    fun `on feature branch - no commits`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "f", date = 150_006_000), // <-- master, feature/x, HEAD
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "feature/x", "X" to "master"))
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `on feature branch - no commits - local changes (additions only)`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "f", date = 150_006_000), // <-- master, feature/x, HEAD
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val localChanges = LocalChanges(1, 2, 0)
        val git = MockGitRepo(graph, "X", listOf("X" to "feature/x", "X" to "master"), localChanges)
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x-SNAPSHOT(1 +2 -0)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `on feature branch - no commits - local changes (deletions only)`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "f", date = 150_006_000), // <-- master, feature/x, HEAD
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val localChanges = LocalChanges(1, 0, 2)
        val git = MockGitRepo(graph, "X", listOf("X" to "feature/x", "X" to "master"), localChanges)
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x-SNAPSHOT(1 +0 -2)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `on feature branch - one commit`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "g", date = 150_006_000), // <-- feature/x, HEAD
                Commit(sha1 = "g", parent = "f", date = 150_006_000), // <-- master
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "feature/x", "g" to "master"))
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x+1")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(7)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(1)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("g")
        }
    }

    @Test
    fun `no feature branch name - like a jenkins PR build`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "g", date = 150_006_000), // <-- HEAD
                Commit(sha1 = "g", parent = "f", date = 150_006_000),
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000), // <-- master
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("e" to "master"))
        val versioner = GitVersioner(git)

        SoftAssertions.assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(5)
            softly.assertThat(versioner.versionName()).isEqualTo("5+3")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(5)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(3)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("e")
        }
    }


}