import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.dotnetTest

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2024.03"

project {

    buildType(UnitTests)
    buildType(PythonUnitTests)
    buildType(TestComposite)
    buildType(Deployment)
    buildType(Hook)
}

object Deployment : BuildType({
    name = "Deployment"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    dependencies {
        dependency(PythonUnitTests) {
            snapshot {
            }

            artifacts {
                artifactRules = "bin.txt"
            }
        }
        snapshot(TestComposite) {
        }
    }
})

object Hook : BuildType({
    name = "Hook"

    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        showDependenciesChanges = true
    }
})

object PythonUnitTests : BuildType({
    name = "Python Unit Tests"

    artifactRules = """E:\bin.txt"""
})

object TestComposite : BuildType({
    name = "Test Composite"

    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        showDependenciesChanges = true
    }

    dependencies {
        snapshot(PythonUnitTests) {
        }
        snapshot(UnitTests) {
        }
    }
})

object UnitTests : BuildType({
    name = "Unit Tests"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dotnetTest {
            id = "dotnet"
            projects = "DotnetTeamCityTest.csproj"
            param("dotNetCoverage.dotCover.home.path", "%teamcity.tool.JetBrains.dotCover.CommandLineTools.DEFAULT%")
        }
    }

    requirements {
        equals("docker.server.osType", "linux")
    }
})
