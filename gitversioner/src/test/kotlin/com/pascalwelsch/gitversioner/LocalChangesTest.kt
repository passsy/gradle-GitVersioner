package com.pascalwelsch.gitversioner

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LocalChangesTest {

    @Test
    fun `parse insertions and additions`() {
        val diff = parseShortStats("3 files changed, 161 insertions(+), 84 deletions(-)")
        assertThat(diff).isEqualTo(LocalChanges(3, 161, 84))
    }

    @Test
    fun `parse insertions only`() {
        val diff = parseShortStats("1 file changed, 13 insertions(+)")
        assertThat(diff).isEqualTo(LocalChanges(1, 13, 0))
    }

    @Test
    fun `parse deletions only`() {
        val diff = parseShortStats("1 file changed, 14 deletions(-)")
        assertThat(diff).isEqualTo(LocalChanges(1, 0, 14))
    }

    @Test
    fun `parse crap`() {
        val diff = parseShortStats("nothing useful")
        assertThat(diff).isEqualTo(NO_CHANGES)
    }

    @Test
    fun `parse crap 2`() {
        val diff = parseShortStats("2 nothing useful, 3 with commas, 4 and digits")
        assertThat(diff).isEqualTo(NO_CHANGES)
    }

    @Test
    fun `toString conversion`() {
        assertThat(LocalChanges(1, 0, 14).toString()).isEqualTo("1 +0 -14")
        assertThat(LocalChanges(0, 0, 0).toString()).isEqualTo("0 +0 -0")
        assertThat(LocalChanges(2, 3, 4).toString()).isEqualTo("2 +3 -4")
        assertThat(LocalChanges(1, 2, 0).toString()).isEqualTo("1 +2 -0")
    }
}