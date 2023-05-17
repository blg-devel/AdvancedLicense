repositories {
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") //Spigot
}

dependencies {
    implementation(project(mapOf("path" to ":")))
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
}
