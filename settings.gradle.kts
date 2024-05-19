pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        jcenter()
        gradlePluginPortal()
        maven("https://jitpack.io") // Moved JitPack repository inside pluginManagement
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io") // Use maven("https://jitpack.io") instead of maven{url 'https://jitpack.io'}
    }
}

rootProject.name = "Kanaku pulla"
include(":app")
