package com.pascalwelsch.gitversioner

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4


@RunWith(JUnit4::class)
class GitVersionerTest {

    @Test
    fun `clean on default branch master`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- master, HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000),
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000),
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(11)
            softly.assertThat(versioner.versionName()).isEqualTo("11")
            softly.assertThat(versioner.baseBranchCommits).hasSize(11)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `base branch not in history`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- master, HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000),
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000),
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"))
        val versioner = GitVersioner(git).apply {
            baseBranch = "develop"
        }

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(0)
            softly.assertThat(versioner.versionName()).isEqualTo("0-master+11")
            softly.assertThat(versioner.baseBranchCommits).hasSize(0)
            softly.assertThat(versioner.featureBranchCommits).hasSize(11)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("develop")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)

            // no commit in both HEAD history and develop because develop is not part of the graph
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

    //TODO orphan case not correctly handled
    @Test
    fun `on orphan initial commit`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = null, date = 150_010_000), // <-- HEAD, orphan
                Commit(sha1 = "e", parent = "d", date = 150_004_000), // <-- master
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("e" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(-1)
            softly.assertThat(versioner.versionName()).isEqualTo("orphan+1")
            softly.assertThat(versioner.baseBranchCommits).hasSize(0)
            softly.assertThat(versioner.featureBranchCommits).hasSize(1)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)

            // no commit in both HEAD history and develop because develop is not part of the graph
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

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

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x+4(+5 -7)")
            softly.assertThat(versioner.baseBranchCommits).hasSize(7)
            softly.assertThat(versioner.featureBranchCommits).hasSize(4)
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

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x+4")
            softly.assertThat(versioner.baseBranchCommits).hasSize(7)
            softly.assertThat(versioner.featureBranchCommits).hasSize(4)
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

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x")
            softly.assertThat(versioner.baseBranchCommits).hasSize(7)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
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

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x(+2 -0)")
            softly.assertThat(versioner.baseBranchCommits).hasSize(7)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
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

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x(+0 -2)")
            softly.assertThat(versioner.baseBranchCommits).hasSize(7)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
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

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(7)
            softly.assertThat(versioner.versionName()).isEqualTo("7-feature/x+1")
            softly.assertThat(versioner.baseBranchCommits).hasSize(7)
            softly.assertThat(versioner.featureBranchCommits).hasSize(1)
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
    fun `one commit`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = null, date = 150_006_000) // <-- master, HEAD
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(1)
            softly.assertThat(versioner.versionName()).isEqualTo("1")
            softly.assertThat(versioner.baseBranchCommits).hasSize(1)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `no commits`() {
        val git = MockGitRepo() // git initialized but nothing commited
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(0)
            softly.assertThat(versioner.versionName()).isEqualTo("0")
            softly.assertThat(versioner.baseBranchCommits).hasSize(0)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isNull()
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo(null)
        }
    }

    @Test
    fun `no git repo`() {
        val git = MockGitRepo(isGitProjectReady = false) // git initialized but nothing commited
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(-1)
            softly.assertThat(versioner.versionName()).isEqualTo("undefined")
            softly.assertThat(versioner.baseBranchCommits).hasSize(0)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isNull()
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo(null)
        }
    }

    @Test
    fun `no commits - local changes`() {

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(localChanges = localChanges) // git initialized but nothing commited
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(0)
            softly.assertThat(versioner.versionName()).isEqualTo("0(+5 -7)")
            softly.assertThat(versioner.baseBranchCommits).hasSize(0)
            softly.assertThat(versioner.featureBranchCommits).hasSize(0)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isNull()
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo(null)
        }
    }

}