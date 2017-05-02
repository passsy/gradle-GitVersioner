package com.pascalwelsch.gitversioner

import org.gradle.api.Plugin
import org.gradle.api.Project

class GitVersionerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def gitVersionExtension = project.extensions.create('gitVersioner', GitVersion)
    }
}
