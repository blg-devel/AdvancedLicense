plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val targetCompatibility = JavaVersion.VERSION_1_8.toString()

allprojects {
    apply(plugin = "java")

    group = "me.leoko.advancedlicense"
    version = "2.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    dependencies {
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }

    tasks.withType<JavaCompile> {
        sourceCompatibility = JavaVersion.current().toString()
        targetCompatibility = targetCompatibility
    }

    tasks.test {
        useJUnitPlatform()
    }
}

subprojects {
    apply(plugin = "com.github.johnrengelman.shadow")

    tasks.jar {
        dependsOn(tasks.shadowJar)
    }
}
