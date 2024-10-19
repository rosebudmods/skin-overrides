plugins {
	id("maven-publish")
	// id("org.quiltmc.loom")
	id("fabric-loom")
	id("me.modmuss50.mod-publish-plugin") version "0.5.2"
}

base {
	archivesName.set(property("archives_base_name").toString())
}

val modVersion = property("mod.version").toString()
val mcVersion = property("deps.minecraft").toString()
val snapshot = property("mod.snapshot") != "false"
version = "$modVersion+$mcVersion"
group = property("maven_group").toString()

val scVersion = stonecutter.current.version
val hasUi = stonecutter.compare(scVersion, "1.19.4") >= 0
val hasNetworking = stonecutter.compare(scVersion, "1.20.6") >= 0
val awVersion =
	if (stonecutter.compare(scVersion, "1.21.2") >= 0) "1.21.2"
	else if (stonecutter.compare(scVersion, "1.20.2") >= 0) "1.20.2"
	else if (stonecutter.compare(scVersion, "1.20.1") >= 0) "1.20.1"
	else if (stonecutter.compare(scVersion, "1.19.4") >= 0) "1.19.4"
	else if (stonecutter.compare(scVersion, "1.19.3") >= 0) "1.19.3"
	else if (stonecutter.compare(scVersion, "1.19.2") >= 0) "1.19.2"
	else if (stonecutter.compare(scVersion, "1.17.1") >= 0) "1.17.1"
	else "1.15.2"

stonecutter {
	const("hasUi", hasUi)
	const("hasNetworking", hasNetworking)
	const("hasModMenu", property("deps.modmenu") != "none")
	swap("modVersion", "\"$modVersion\";")
}

if (stonecutter.current.isActive) {
	rootProject.tasks.register("client") {
		group = "project"
		dependsOn(tasks.named("runClient"))
	}

	rootProject.tasks.register("server") {
		group = "project"
		dependsOn(tasks.named("runServer"))
	}

	rootProject.tasks.register("buildCurrent") {
		group = "project"
		dependsOn(tasks.named("build"))
	}
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.

	maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }

	maven("https://api.modrinth.com/maven") { name = "Modrinth Maven" }

	maven("https://repo.inventivetalent.org/repository/public/") { name = "inventive-repo" }

	// for some reason using the terraformers maven version of
	// modmenu breaks quilt loader.
	// maven {
	// 	name "Terraformers"
	// 	url "https://maven.terraformersmc.com"
	// }
}

if (hasUi) {
	sourceSets["main"].java {
		srcDir("src/ui/java")
	}
}
if (hasNetworking) {
	sourceSets["main"].java {
		srcDir("src/networking/java")
	}
}

loom {
	// Loom and Loader both use this block in order to gather more information about your mod.
	mods {
		// This should match your mod id.
		create("skin_overrides") {
			// Tell Loom about each source set used by your mod here. This ensures that your mod's classes are properly transformed by Loader.
			sourceSet(sourceSets["main"])

			// If you shade (directly include classes, not JiJ) a dependency into your mod, include it here using one of these methods:
			// dependency("com.example.shadowedmod:1.2.3")
			// configuration("exampleShadedConfigurationName")
		}
	}

	accessWidenerPath = getRootProject().file("src/main/resources/aw/$awVersion.accesswidener")
}

dependencies {
	minecraft("com.mojang:minecraft:$mcVersion")
	// modImplementation("org.quiltmc:quilt-loader:${property("deps.quilt_loader")}")
	modImplementation("net.fabricmc:fabric-loader:0.16.7")

	val qm = property("deps.quilt_mappings").toString()
	if (!qm.contains(":"))
		mappings("org.quiltmc:quilt-mappings:$qm:intermediary-v2")
	else
		mappings(qm)

	// QSL is not a complete API; You will need Quilted Fabric API to fill in the gaps.
	// Quilted Fabric API will automatically pull in the correct QSL version.
	// modImplementation libs.quilted.fabric.api
	// modImplementation libs.bundles.quilted.fabric.api // If you wish to use Fabric API's deprecated modules, you can replace the above line with this one

	modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

	if (property("deps.modmenu") != "none")
		modImplementation("maven.modrinth:modmenu:${property("deps.modmenu")}")

	if (property("deps.ears") != "none")
		modRuntimeOnly("maven.modrinth:ears:${property("deps.ears")}")

	if (hasNetworking) {
		implementation("org.mineskin:java-client:2.1.1-SNAPSHOT")
		implementation("org.mineskin:java-client-apache:2.1.1-SNAPSHOT")
	}
}

tasks.processResources {
	val map = mapOf(
		"version" to version,
		"group" to project.group,
		"minecraft_version" to scVersion + if (snapshot) "-" else "",
		"access_widener" to awVersion,
		"modmenu_entrypoint" to if (hasUi) "modmenu" else ""
	)

	inputs.properties(map)

	filesMatching(listOf("quilt.mod.json", "fabric.mod.json")) {
		expand(map)
	}
}

java {
	// Still required by IDEs such as Eclipse and Visual Studio Code
	sourceCompatibility = JavaVersion.VERSION_17
	targetCompatibility = JavaVersion.VERSION_17

	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task if it is present.
	// If you remove this line, sources will not be generated.
	// withSourcesJar()

	// If this mod is going to be a library, then it should also generate Javadocs in order to aid with development.
	// Uncomment this line to generate them.
	// withJavadocJar()
}

tasks.jar {
	from(project.file("LICENSE"))

	exclude {
		it.name.endsWith(".accesswidener") && it.name != "$awVersion.accesswidener"
	}
}

publishMods {
	displayName = "skin overrides $modVersion for $mcVersion"
	file = tasks.remapJar.get().archiveFile
	changelog = rootProject.file("CHANGELOG.md").readText()
	type = STABLE

	modLoaders.add("quilt")
	modLoaders.add("fabric")

	dryRun = !providers.environmentVariable("MODRINTH_TOKEN").isPresent()
			|| property("pub.should_publish") == "false"

	val mcVersions = mutableListOf(mcVersion)
	mcVersions.addAll(
		property("pub.additional_versions").toString().split(" ")
			.filter(String::isNotEmpty))

	modrinth {
		projectId = "GON0Fdk5"
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		mcVersions.forEach(minecraftVersions::add)

		requires("fabric-api")
		optional("modmenu")
	}
}

// Configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}
