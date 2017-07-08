# Java on CircleCI with Bouncy Castle and JCE Unlimited

## Build Status

[![Circle CI](https://circleci.com/gh/revof11/java-circleci/tree/master.svg?style=svg)](https://circleci.com/gh/revof11/java-circleci/tree/master)

## About

CircleCI ([http://circleci.com](http://circleci.com)) is a powerful online Continuous
Integration (CI) tool that provides a shared CI environment for just about any modern
software project.  With simple management and configuration, CircleCI can automatically
figure out how to build your applications in many cases.  However, in some instances
you need to run some extra configuration.
 
This is a simple Java configuration that shows how you can get the Bouncy Castle and
JCE Unlimited policy files set up on CircleCI for your JVM-based project.  The basic
concept is that you lay out an overlay file structure and drop it on top of the Java
SDK/JRE provided by CircleCI by default.  That's it!  There's nothing to it!

## Instructions

### Basics

1. Setup your basic project (Gradle, Maven, whatever)
2. Create a `.jdk-overlay` with all the files you want installed on top of the existing JDK
3. Create a new `circle.yml` file similar to what is in this project
4. Commit
5. Wait for the build to complete
6. ...
7. Profit

### Detailed Explanation

#### 1. Setup Your Project

This should not be anything new.  I really don't care what build library you are
using nor do I care if you are using Java, Scala or any of the other JVM languages.
The key here is that you set up your project normally, as if nothing else needed
to be done.  You don't need to add **any** extra information or configuration to
the core.
  
#### 2. Create the Overlay

Create a `.jdk-overlay` with all the files you want installed on top of the existing JDK.

The premise of this is pretty simple: set up all the new files and files to be
overwritten in the same structure you installed them locally and we will copy and/or
overwrite the files before the build starts.  This follows a convention similar to
the [Heroku JDK Customization](https://devcenter.heroku.com/articles/customizing-the-jdk)
paradigm.  I liked how this was done and decided it was how I wanted to approach
things in **all** of my projects.  If you look at this project's file structure,
you will see exactly how things are laid out for the JRE on my system.  Don't get
too picky about it as this is just a quick/dirty example of how to get it rolling.
More care should be taken for an actual production deployment in how things are
set up and which files are dropped where.
  
You can grab the files your need (for Java 8) from here:
  
  - [Legion of the Bouncy Castle](https://www.bouncycastle.org/latest_releases.html)
  - [Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)
  
We can now tell CircleCI how to make use of our overlay.

#### 3. Rsync the Overlay

We know that CircleCI will have a `$JAVA_HOME` environment variable ready for us.
We also know that CircleCI will provide us a base option that is an [Ubuntu Linux](https://circleci.com/docs/environment)
system.  This means we know where Java is installed and that we can use the Linux
standard [`rsync`](http://manpages.ubuntu.com/manpages/raring/man1/rsync.1.html)
command to do what we need.  So here's what we need:
  
  1. Create a `circle.yml` file in your project root
  2. Specify your Java version with [one of the predefined constants](https://circleci.com/docs/environment#java)
  2. Create a *dependencies* element with a *pre* sub-element
  3. Add the `rsync` command to execute the overlay.
  

You should have the following somewhere in your YML:
  
```
machine:
  java:
    version: oraclejdk8

dependencies:
  pre:
    - sudo rsync -r --include=*/ --include=*.jar --exclude=* .jdk-overlay/jre $JAVA_HOME
```

It doesn't matter what files you add to your overlay as long as they are in the
proper structure.  Here's a breakdown of that command:
  
  - Use `sudo` so we have permission to write over the JDK files
  - Use `-r` so that the command is executed recursively
  - Use the first `--include` to ensure that we copy files **and** directories from the root of the `.jdk-overlay` directory
  - Use the second `--include` to instruct `rsync` to only deal with Jar files (obviously you should change this if you have other file types included)
  - Use `--exclude` to ensure that everything else gets excluded (another reason we need the first `--include`, but I had problems exluding one or the other)
  - Specify the source directory to copy
  - Specify the target for the overlay (yes, leave off the `/jre` from the end)

#### 4. Add Your Files & Push

All that's left now is to get the build running.  Add your files to Git and then
execute a push.  From there your CircleCI integration with your GitHub account
should take over and all should be happy.  You're notice in our classes and test
classes in this project that we specifically look for the Bouncy Castle license
and an appropriate key length from within Java core.  That's it!
