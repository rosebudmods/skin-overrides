plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.8" /* [SC] DO NOT EDIT */

stonecutter.tasks {
    order("publishMods")
    order("runClient")
    order("runServer")
}
