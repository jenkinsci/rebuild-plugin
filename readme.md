TODO:

DONE remove config options from job config screen

DONE enable "auto rebuilding"

change icon

DONE create pipeline DSL to use with plugin

create the other side of promote, a global promote object
 - should contain things like the commit hash to build
 - the last promoted hash or build number
 - the last promoted version number
 - maybe version number should be a real object with properties like major, minor, patch etc

can we get changelogs?

## Read Me

# About

Project for building PromoteRebuilder jenkins plugin.

Plugin provides variables global mobileCiSupport and stagePromotion.

# Setup

Checkout project

Run 'mvn clean verify' to run all the tests.

After that you should be able to run tests from IDE such as IntelliJ as well.

# Building project

To create a new build artifact update pom.xml with version name, then run hpi:create.

Output will be {project}/target/promoterebuild.hpi

# Plugin dependancies

Probably makes sense to try and keep plugin dependencies up to date with the jenkins environment that plugin will be installed on. Tests don't mean much unless they are reflective of where the plugin will actually run.

// TODO what jenkins stuff should match what dependancies in pom.xml.

