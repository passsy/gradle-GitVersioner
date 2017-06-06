package com.pascalwelsch.gitversioner

import org.assertj.core.api.Assertions.assertThat
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

        assertThat(versioner.versionCode()).isEqualTo(11)
        assertThat(versioner.versionName()).isEqualTo("11")
        assertThat(versioner.baseBranchCommits).hasSize(11)
        assertThat(versioner.featureBranchCommits).hasSize(0)
        assertThat(versioner.branchName).isEqualTo("master")
        assertThat(versioner.currentSha1).isEqualTo("X")
        assertThat(versioner.baseBranch).isEqualTo("master")
        assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
        assertThat(versioner.yearFactor).isEqualTo(1000)
        assertThat(versioner.timeComponent).isEqualTo(0)
        assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
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

        assertThat(versioner.versionCode()).isEqualTo(0)
        assertThat(versioner.versionName()).isEqualTo("0-master-11")
        assertThat(versioner.baseBranchCommits).hasSize(0)
        assertThat(versioner.featureBranchCommits).hasSize(11)
        assertThat(versioner.branchName).isEqualTo("master")
        assertThat(versioner.currentSha1).isEqualTo("X")
        assertThat(versioner.baseBranch).isEqualTo("develop")
        assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
        assertThat(versioner.yearFactor).isEqualTo(1000)
        assertThat(versioner.timeComponent).isEqualTo(0)

        // no commit in both HEAD history and develop because develop is not part of the graph
        assertThat(versioner.featureBranchOriginCommit).isNull()
    }


    @Test
    fun `on feature branch`() {
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

        assertThat(versioner.versionCode()).isEqualTo(7)
        assertThat(versioner.versionName()).isEqualTo("7-feature/x-4")
        assertThat(versioner.baseBranchCommits).hasSize(7)
        assertThat(versioner.featureBranchCommits).hasSize(4)
        assertThat(versioner.branchName).isEqualTo("feature/x")
        assertThat(versioner.currentSha1).isEqualTo("X")
        assertThat(versioner.baseBranch).isEqualTo("master")
        assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
        assertThat(versioner.yearFactor).isEqualTo(1000)
        assertThat(versioner.timeComponent).isEqualTo(0)
        assertThat(versioner.featureBranchOriginCommit).isEqualTo("g")
    }

}