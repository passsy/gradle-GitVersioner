package com.pascalwelsch.gitversioner

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def gitVersionExtension = project.extensions.create('gitVersioner', GitVersion)
        GitVersion gitVersion = new GitVersionGenerator(project, gitVersionExtension).generateVersionName()
        project.extensions.add("gitVersion", gitVersion)
        project.extensions.add("gitVersionName", gitVersion.name)
    }
}
