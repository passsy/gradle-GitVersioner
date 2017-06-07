package com.pascalwelsch.gitversioner

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.TimeUnit


@RunWith(JUnit4::class)
class GitVersionerTest {

    @Test
    fun `default - clean on default branch master`() {
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
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(11)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
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
    fun `default - with local changes - addSnapshot false`() {
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

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("X" to "master"), localChanges)
        val versioner = GitVersioner(git)
        versioner.addSnapshot = false

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(11)
            softly.assertThat(versioner.versionName()).isEqualTo("11(3 +5 -7)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(11)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `default - with local changes - addLocalChangesDetails false`() {
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

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("X" to "master"), localChanges)
        val versioner = GitVersioner(git)
        versioner.addLocalChangesDetails = false

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(11)
            softly.assertThat(versioner.versionName()).isEqualTo("11-SNAPSHOT")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(11)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `default - without local changes information`() {
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

        val localChanges = LocalChanges(3, 5, 7)
        val git = MockGitRepo(graph, "X", listOf("X" to "master"), localChanges)
        val versioner = GitVersioner(git)
        versioner.addLocalChangesDetails = false
        versioner.addSnapshot = false

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(11)
            softly.assertThat(versioner.versionName()).isEqualTo("11")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(11)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
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
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(11)
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

        val git = MockGitRepo(graph, "X", listOf("e" to "master", "X" to "orphan"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(0)
            softly.assertThat(versioner.versionName()).isEqualTo("0-orphan+1")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(1)
            softly.assertThat(versioner.branchName).isEqualTo("orphan")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isNull()
        }
    }

    @Test
    fun `on orphan few commits`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "b'", date = 150_030_000), // <-- HEAD, feature/x
                Commit(sha1 = "b'", parent = "a'", date = 150_020_000),
                Commit(sha1 = "a'", parent = null, date = 150_010_000), // <-- orphan

                Commit(sha1 = "e", parent = "d", date = 150_004_000), // <-- master
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("e" to "master", "X" to "feature/x"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(0)
            softly.assertThat(versioner.versionName()).isEqualTo("0-x+3")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(3)
            softly.assertThat(versioner.branchName).isEqualTo("feature/x")
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
    fun `one commit`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = null, date = 150_006_000) // <-- master, HEAD
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(1)
            softly.assertThat(versioner.versionName()).isEqualTo("1")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(1)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
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
    fun `short sha1`() {
        val graph = listOf(
                Commit(sha1 = "abcdefghijkl", parent = null, date = 150_006_000) // <-- master, HEAD
        )

        val git = MockGitRepo(graph, "abcdefghijkl", listOf("abcdefghijkl" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(1)
            softly.assertThat(versioner.versionName()).isEqualTo("1")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(1)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("abcdefghijkl")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("abcdefghijkl")
        }

        assertThat(versioner.currentSha1Short).isEqualTo("abcdefg").hasSize(7)
    }

    @Test
    fun `short sha1 - edge cases`() {

        assertThat(GitVersioner(GitInfoExtractorStub(currentSha1 = null)).currentSha1Short)
                .isNull()

        assertThat(GitVersioner(GitInfoExtractorStub(currentSha1 = "abc")).currentSha1Short)
                .isEqualTo("abc")

        assertThat(GitVersioner(GitInfoExtractorStub(currentSha1 = "abcdefghi")).currentSha1Short)
                .hasSize(7).isEqualTo("abcdefg")
    }

    @Test
    fun `timeComponent increased by 1 after ~8h`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "a", date = 150_000_000), // <-- HEAD, master
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(2)
            softly.assertThat(versioner.versionName()).isEqualTo("2")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }

        // now the same with larger difference between commits (9h) should increase by 1 with default yearFactor
        val graph2 = listOf(
                Commit(sha1 = "X", parent = "a",
                        date = 150_000_000 + TimeUnit.HOURS.toSeconds(9)), // <-- HEAD, master
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git2 = MockGitRepo(graph2, "X", listOf("X" to "master"))
        val versioner2 = GitVersioner(git2)

        assertSoftly { softly ->
            softly.assertThat(versioner2.versionCode()).isEqualTo(3)
            softly.assertThat(versioner2.versionName()).isEqualTo("3")
            softly.assertThat(versioner2.baseBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner2.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner2.branchName).isEqualTo("master")
            softly.assertThat(versioner2.currentSha1).isEqualTo("X")
            softly.assertThat(versioner2.baseBranch).isEqualTo("master")
            softly.assertThat(versioner2.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner2.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner2.timeComponent).isEqualTo(1)
            softly.assertThat(versioner2.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `test yearfactor`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "a", date = 150_000_000), // <-- HEAD, master
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(2)
            softly.assertThat(versioner.versionName()).isEqualTo("2")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }

        // add one year / yearFactor 1000
        val graph2 = listOf(
                Commit(sha1 = "X", parent = "a",
                        date = 150_000_000 + TimeUnit.DAYS.toSeconds(365)), // <-- HEAD, master
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git2 = MockGitRepo(graph2, "X", listOf("X" to "master"))
        val versioner2 = GitVersioner(git2)

        assertSoftly { softly ->
            softly.assertThat(versioner2.versionCode()).isEqualTo(1002)
            softly.assertThat(versioner2.versionName()).isEqualTo("1002")
            softly.assertThat(versioner2.baseBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner2.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner2.branchName).isEqualTo("master")
            softly.assertThat(versioner2.currentSha1).isEqualTo("X")
            softly.assertThat(versioner2.baseBranch).isEqualTo("master")
            softly.assertThat(versioner2.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner2.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner2.timeComponent).isEqualTo(1000)
            softly.assertThat(versioner2.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `custom year factor 1200`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "a",
                        date = 150_000_000 + TimeUnit.DAYS.toSeconds(365)), // <-- HEAD, master
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("X" to "master"))
        val versioner = GitVersioner(git)
        versioner.yearFactor = 1200

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(1202)
            softly.assertThat(versioner.versionName()).isEqualTo("1202")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1200)
            softly.assertThat(versioner.timeComponent).isEqualTo(1200)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }

    @Test
    fun `no git repo`() {
        val git = MockGitRepo(isGitProjectReady = false) // git initialized but nothing commited
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(-1)
            softly.assertThat(versioner.versionName()).isEqualTo("undefined")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
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
    fun `no commits`() {
        val git = MockGitRepo() // git initialized but nothing commited
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(0)
            softly.assertThat(versioner.versionName()).isEqualTo("0")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
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
            softly.assertThat(versioner.versionName()).isEqualTo("0-SNAPSHOT(3 +5 -7)")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isNull()
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(localChanges)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo(null)
        }
    }

    @Test
    fun verifyOpenVersioner() {
        // required because gradle generates a proxy for the versioner
        val extended = object : GitVersioner(MockGitRepo()) {}
        assertThat(extended).isNotNull()
    }

    @Test
    fun `custom name formatter`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j",
                        date = 150_010_000), // <-- fix_ABC-12345_nothing_special, HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000), // <-- master
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000),
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "fix_ABC-12345_nothing_special",
                listOf("i" to "master", "X" to "fix_ABC-12345_nothing_special"))
        val versioner = GitVersioner(git)
        versioner.shortNameFormatter = { v ->
            // use default formatter but only use the jira id when detected and drop rest
            GitVersioner.DEFAULT_SHORT_NAME_FORMATTER(v)
                    .replace(".*(ABC-\\d*).*".toRegex(), { it.groupValues[1] })
        }

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(9)
            softly.assertThat(versioner.versionName()).isEqualTo("9-ABC-12345+2")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(9)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner.branchName).isEqualTo("fix_ABC-12345_nothing_special")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("i")
        }
    }

    @Test
    fun `custom formatter`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j",
                        date = 150_010_000), // <-- fix_ABC-12345_nothing_special, HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000), // <-- master
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000),
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "fix_ABC-12345_nothing_special",
                listOf("i" to "master", "X" to "fix_ABC-12345_nothing_special"))
        val versioner = GitVersioner(git)
        versioner.formatter = { v ->
            "I do my own thing ${v.commitCount}"
        }

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(9)
            softly.assertThat(versioner.versionName()).isEqualTo("I do my own thing 11")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(9)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner.branchName).isEqualTo("fix_ABC-12345_nothing_special")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("i")
        }
    }

    @Test
    fun `no branch name - sha1 fallback`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000), // <-- master
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000),
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("i" to "master"))
        val versioner = GitVersioner(git)

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(9)
            softly.assertThat(versioner.versionName()).isEqualTo("9-X+2")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(9)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("i")
        }
    }

    @Test
    fun `custom name formatter - fails`() {
        val graph = listOf(
                Commit(sha1 = "X", parent = "j", date = 150_010_000), // <-- HEAD
                Commit(sha1 = "j", parent = "i", date = 150_009_000),
                Commit(sha1 = "i", parent = "h", date = 150_008_000), // <-- master
                Commit(sha1 = "h", parent = "g", date = 150_007_000),
                Commit(sha1 = "g", parent = "f", date = 150_006_000),
                Commit(sha1 = "f", parent = "e", date = 150_005_000),
                Commit(sha1 = "e", parent = "d", date = 150_004_000),
                Commit(sha1 = "d", parent = "c", date = 150_003_000),
                Commit(sha1 = "c", parent = "b", date = 150_002_000),
                Commit(sha1 = "b", parent = "a", date = 150_001_000),
                Commit(sha1 = "a", parent = null, date = 150_000_000)
        )

        val git = MockGitRepo(graph, "X", listOf("i" to "master"))
        val versioner = GitVersioner(git)
        // fail will be ignored, fallback default formatter
        versioner.shortNameFormatter = { throw Exception("fail!") }

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(9)
            softly.assertThat(versioner.versionName()).isEqualTo("9-X+2")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(9)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(2)
            softly.assertThat(versioner.branchName).isNull()
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("i")
        }
    }

    @Test
    fun `custom formatter - fails`() {
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
        // fail will be ignored, fallback default formatter
        versioner.formatter = { throw Exception("fail!") }

        assertSoftly { softly ->
            softly.assertThat(versioner.versionCode()).isEqualTo(11)
            softly.assertThat(versioner.versionName()).isEqualTo("11")
            softly.assertThat(versioner.baseBranchCommitCount).isEqualTo(11)
            softly.assertThat(versioner.featureBranchCommitCount).isEqualTo(0)
            softly.assertThat(versioner.branchName).isEqualTo("master")
            softly.assertThat(versioner.currentSha1).isEqualTo("X")
            softly.assertThat(versioner.baseBranch).isEqualTo("master")
            softly.assertThat(versioner.localChanges).isEqualTo(NO_CHANGES)
            softly.assertThat(versioner.yearFactor).isEqualTo(1000)
            softly.assertThat(versioner.timeComponent).isEqualTo(0)
            softly.assertThat(versioner.featureBranchOriginCommit).isEqualTo("X")
        }
    }
}