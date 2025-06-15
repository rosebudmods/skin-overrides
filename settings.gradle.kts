pluginManagement {
	repositories {
		maven("https://maven.kikugie.dev/snapshots")
		// Currently needed for Intermediary and other temporary dependencies
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		gradlePluginPortal()
	}
}

plugins {
	id("dev.kikugie.stonecutter") version "0.5.2"
}

stonecutter {
	kotlinController = true
	centralScript = "build.gradle.kts"

	shared {
		versions(
				"1.19.4",
				"1.20.1", "1.20.2", "1.20.4", "1.20.6",
				"1.21.1", "1.21.3", "1.21.4", "1.21.5", "1.21.6"
		)

		vcsVersion = "1.21.6"
	}

	create(rootProject)
}
