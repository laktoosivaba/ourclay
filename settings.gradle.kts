pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven(
            url = "https://gitlab.com/claysolutions/public/maven/-/raw/releases"
//            url = "${rootDir}/libs/maven-releases"
        )
    }
}

rootProject.name = "Clay Key"
include(":app")

