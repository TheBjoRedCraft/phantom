plugins {
    kotlin("jvm") version "2.4.20"
    kotlin("plugin.serialization") version "2.4.10"
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("de.eldoria.plugin-yml.paper") version "0.9.0"
}

group = "dev.thebjoredcraft.phantom"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    implementation("com.github.TheBjoRedCraft.railway:railway-database:1.0.7")
    compileOnly("io.papermc.paper:paper-api:26.3.build.201-stable")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-api:2.23.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-folia-core:2.23.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.12.0")
}

kotlin {
    jvmToolchain(25)
}

tasks {
    runServer {
        minecraftVersion("26.3")
    }
}

paper {
    main = "dev.thebjoredcraft.phantom.PhantomMain"
    apiVersion = "1.21"
    foliaSupported = true

    authors = listOf("TheBjoRedCraft")
}