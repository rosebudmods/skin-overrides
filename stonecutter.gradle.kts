plugins {
    id("dev.kikugie.stonecutter")
    id("org.quiltmc.loom") version "1.8.3" apply false
}

stonecutter active "1.21.2" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("build")
}

stonecutter registerChiseled tasks.register("chiseledPublish", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
