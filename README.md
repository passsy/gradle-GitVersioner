# Git Versioner for gradle

Version numbers are hard. 
It was easier with SVN where the revision number got increased for every commit. 
Revision `342` was clearly older than revision `401`. 
This is not possible in git because branching is so common (and that's a good thing). 
`342` commits could mean multiple commits on different branches.
Not even the latest common commit in history is clear.
This projects aims to bring the SVN simplicity and more back to git for your gradle (android) project.

####Read the story behind on [medium](https://medium.com/@passsy/use-different-build-numbers-for-every-build-automatically-using-a-gradle-script-35577cd31b19#.g8quoji2e)

## Idea

Just count the commits of the main branch (`master` or `develop` in most cases) as the base revision.
The commits on the feature branch are counted too, but are shown separately.

This technique is often used and far better than just a SHA1 of the latest commit. 
But I think it gives too much insights of the project. 
Once a client knows `commit count == version number` they start asking why the commit count is so high/low for the latest release.

That's why this versioner adds the project age (initial commit to latest commit) as seconds part to the revision.
By default, one year equals `1000`.
This means that the revision count increases every `8.67` hours.
When you started your project half a year ago and you have `325` commits the revision is something around `825`.

When working on a feature branch this versioner adds a two char identifier of the branch name and the commit count since branching.
When you are building and you have uncommited files it adds the count of the uncommited files and `"-SNAPSHOT"`


## Reading the Version

#### Normal build number
```
1083
```

`1083`: number of commits + time component. this revision is in the `master` branch. 

#### On a feature branch
```
1083-dm4
```

`-dm4`: `4` commits since branching from revision `1083`. First two `[a-z]` chars of the base64 encoded branch name. Clients don't have to know about your information and typos in branch names. But you have to be able to distinguish between different builds of different branches.

#### Build with local changes
```
1083-dm4(6)-SNAPSHOT
```

`(6)-SNAPSHOT`: 6 uncommited but changed files. Hopefully nothing a client will ever see. But you know that your version is a work in progress version with some local changes

## Get it

Configure the plugin in you top level `build.gradle`. This makes total sense because it's the revision of the top level project and not of a single module.

```gradle
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    // ...
}

// Optional: configure the versioner
ext.gitVersioner = [
        defaultBranch           : "develop",  // default "master"
        stableBranches          : ["master", "someOtherBranch"], // default [], the feature branch postfix (-dm4(6)) will not be appended on stable branches, all commits are included into the version number calculation
        yearFactor              : 1200, 	  // default "1000", increasing every 8.57h
        snapshotEnabled         : false,      // default false, the "-SNAPSHOT" postfix
        localChangesCountEnabled: false       // default false, the (<commitCount>) before -SNAPSHOT
]
// import the script which runs the version generation
apply from: 'https://raw.githubusercontent.com/passsy/gradle-GitVersioner/master/git-versioner.gradle'

// variable `gitVersionName` can be used everywhere to get the revision name
println("versionName: $gitVersionName") // output: "versionName: 1083-dm4(6)-SNAPSHOT"
```
Consider using this [cache plugin](https://github.com/kageiit/gradle-url-cache-plugin) for offline support.

All inforamtion is not only available as a single `String`. You can create your own pattern using the `ext.gitVersion` Object

```gradle
// get granular information with variable `gitVersion` of type `GitVersion`
println("version: ${gitVersion.version}") // output "version: 1083"

// see all available attributes
class GitVersion {
    String name;
    int version;
    String branchName;
    String shortBranch;
    int branchVersion;
    int localChanges;
}
```

### Android 

Display the version in your android app

app `build.gradle`
```gradle
android {
    defaultConfig {
        ...

        buildConfigField 'String', 'REVISION', "\"$gitVersionName\""
    }
}
```

in your `Activity`
```java

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	Toast.makeText(this, BuildConfig.REVISION, Toast.LENGTH_SHORT).show();
    }
```

# License

```
Copyright 2016 Pascal Welsch

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
