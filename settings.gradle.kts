pluginManagement {
	repositories {
		maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
		maven("https://maven.kikugie.dev/snapshots")
		// Currently needed for Intermediary and other temporary dependencies
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		gradlePluginPortal()
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.5-beta.5"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"

	shared {
		// note: the 1.17.1 and 1.18.2 versions currently have no differences.
		// the publish script will only publish a single version.
		// the different versions are listed here to ensure compilation works.
		versions(
				"1.15.2", "1.16.5",
				"1.17.1", "1.18.2",
				"1.19.2", "1.19.3", "1.19.4",
				"1.20.1", "1.20.2", "1.20.4", "1.20.6",
				"1.21.1", "1.21.3", "1.21.4", "1.21.5"
		)

		vcsVersion = "1.21.5"
	}

	create(rootProject)
}
