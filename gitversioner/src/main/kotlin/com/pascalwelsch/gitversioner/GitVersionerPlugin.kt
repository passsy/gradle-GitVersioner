package com.pascalwelsch.gitversioner

import org.gradle.api.Plugin
import org.gradle.api.Project

public class GitVersionerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        println("ALIVE")

        val rootProject = project.rootProject
        if (project != rootProject) {
            throw IllegalStateException(
                    "Register the 'com.pascalwelsch.gitversioner' plugin only once " +
                            "in the root project bulid.gradle.")
        }


        // add extension to root project, makes sense only once per project
        val versioner = rootProject.extensions.create("gitVersioner", GitVersioner::class.java)
        versioner.rootProject = rootProject

        //println("config: ")
        //versioner.printYourself()
    }
}
