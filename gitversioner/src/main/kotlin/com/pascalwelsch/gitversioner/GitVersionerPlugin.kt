package com.pascalwelsch.gitversioner

import org.gradle.api.Plugin
import org.gradle.api.Project

public class GitVersionerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val rootProject = project.rootProject
        if (project != rootProject) {
            throw IllegalStateException(
                    "Register the 'com.pascalwelsch.gitversioner' plugin only once " +
                            "in the root project build.gradle.")
        }

        // add extension to root project, makes sense only once per project
        val gitVersionExtractor = ShellGitInfoExtractor(rootProject)
        val gitVersioner = rootProject.extensions.create("gitVersioner",
                GitVersioner::class.java, gitVersionExtractor)

        project.task("gitVersion").apply {
            group = "Help"
            description = "displays the version information extracted from git history"
            doLast {
                with(gitVersioner) {
                    println("""
                        |
                        |GitVersioner Plugin
                        |-------------------
                        |VersionCode: ${versionCode()}
                        |VersionName: ${versionName()}
                        """.replaceIndentByMargin())
                }
            }
        }
    }
}
