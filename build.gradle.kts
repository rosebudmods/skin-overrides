plugins {
	id("maven-publish")
	id("fabric-loom") version "1.10-SNAPSHOT"
	id("me.modmuss50.mod-publish-plugin") version "0.5.2"
}

class ModData {
	val id = property("mod.id").toString()
	val version = property("mod.version").toString()
	val supportsSnapshot = property("mod.supports_snapshot") != "false"
	val group = property("maven_group").toString()

	val mc = ModMinecraft()
	val deps = ModDependencies()
}

class ModMinecraft {
	/** the release/friendly name of this version (e.g. 1.21, 1.21.5) */
	val release = stonecutter.current.project
	/** the actual supported version (e.g. 25w03a, 1.21-pre1) */
	val version = optProperty("deps.minecraft")?.toString() ?: release

	val dep = "com.mojang:minecraft:$version"
}

class ModDependencies {
	val fabricLoader = "net.fabricmc:fabric-loader:${property("deps.fabric_loader")}"
	val parchment = optProperty("deps.parchment")?.let { "org.parchmentmc.data:parchment-$it@zip" }

	val fabricApi = "net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}"
	val modmenu = optProperty("deps.modmenu")?.let { "maven.modrinth:modmenu:$it" }
	val modmenuWorks = property("deps.modmenu.works") != "false"

	val compat = properties
		.filter { it.key.startsWith("compat.") && it.value != "none" }
		.map { "maven.modrinth:${it.key.substring(7)}:${it.value}" }
}

fun optProperty(name: String) = if (property(name) == "none") null else property(name)

val mod = ModData()

base {
	archivesName = mod.id
}

version = "${mod.version}+${mod.mc.release}"
group = mod.group

val awVersion = versionFrom("1.21.6", "1.21.4", "1.20.6", "1.20.4", "1.20.2", "1.20.1", "1.19.4")
val mixinFile = versionFrom("1.21.6", "1.20.2", "1.19.4") + ".mixins.json"

fun versionFrom(vararg versions: String): String = versions.find { stonecutter.eval(mod.mc.release, ">=$it") }.orEmpty()

stonecutter {
	const("hasModMenu", property("deps.modmenu") != "none")
	swap("modVersion", "\"${mod.version}\";")
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

	maven("https://api.modrinth.com/maven") { name = "Modrinth Maven" }

	maven("https://maven.parchmentmc.org/") { name = "ParchmentMC" }

	// for some reason using the terraformers maven version of
	// modmenu breaks quilt loader.
	// maven {
	// 	name "Terraformers"
	// 	url "https://maven.terraformersmc.com"
	// }
}

loom {
	// Loom and Loader both use this block in order to gather more information about your mod.
	mods {
		create(mod.id) {
			// Tell Loom about each source set used by your mod here. This ensures that your mod's classes are properly transformed by Loader.
			sourceSet(sourceSets["main"])

			// If you shade (directly include classes, not JiJ) a dependency into your mod, include it here using one of these methods:
			// dependency("com.example.shadowedmod:1.2.3")
			// configuration("exampleShadedConfigurationName")
		}
	}

	accessWidenerPath = getRootProject().file("src/main/resources/aw/$awVersion.accesswidener")

	runConfigs.all {
		runDir = "../../run"
	}
}

dependencies {
	minecraft(mod.mc.dep)
	modImplementation(mod.deps.fabricLoader)

	mappings(loom.layered {
		mod.deps.parchment?.let { parchment(it) }
		officialMojangMappings()
	})

	// QSL is not a complete API; You will need Quilted Fabric API to fill in the gaps.
	// Quilted Fabric API will automatically pull in the correct QSL version.
	// modImplementation libs.quilted.fabric.api
	// modImplementation libs.bundles.quilted.fabric.api // If you wish to use Fabric API's deprecated modules, you can replace the above line with this one

	modImplementation(mod.deps.fabricApi)

	mod.deps.modmenu?.let {
		if (mod.deps.modmenuWorks) modImplementation(it)
		else modCompileOnly(it)
	}

	mod.deps.compat.forEach { modRuntimeOnly(it) }

	// include httpmime (it will show up in a dev environment, but don't believe its lies)
	implementation("org.apache.httpcomponents:httpmime:4.5.14")?.let { include(it) }

	compileOnly("com.github.spotbugs:spotbugs-annotations:4.9.3")
}

tasks.processResources {
	val map = mapOf(
		"version" to version,
		"group" to project.group,
		"minecraft_version" to mod.mc.release + if (mod.supportsSnapshot) "-" else "",
		"access_widener" to awVersion,
		"mixin_file" to mixinFile,
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
				|| it.name.endsWith(".mixins.json") && it.name != mixinFile
	}
}

publishMods {
	displayName = "skin overrides ${mod.version} for ${mod.mc.release}"
	file = tasks.remapJar.get().archiveFile
	changelog = rootProject.file("CHANGELOG.md").readText()
	type = STABLE

	modLoaders.add("quilt")
	modLoaders.add("fabric")

	dryRun = !providers.environmentVariable("MODRINTH_TOKEN").isPresent()
			|| property("pub.should_publish") == "false"

	val mcVersions = mutableListOf(mod.mc.version)

	// add additional versions
	mcVersions.addAll(
		property("pub.additional_versions").toString().split(" ")
			.filter(String::isNotEmpty))

	// if the release version is different to the dependency version, add it
	if (mod.mc.version != mod.mc.release)
		mcVersions.add(mod.mc.release)

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
