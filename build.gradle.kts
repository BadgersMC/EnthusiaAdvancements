plugins {
    kotlin("jvm") version "2.0.21"
    id("com.gradleup.shadow") version "8.3.5"
}

group = "io.github.badgersmc"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://nexus.frengor.com/repository/public/")
    maven("https://repo.artillex-studios.com/releases/")
}

dependencies {
    // Server-provided (not shaded)
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.frengor:ultimateadvancementapi-common:3.0.0-beta-1")
    compileOnly(files("../bell-claims/build/libs/LumaGuilds-2.0.0.jar"))
    compileOnly(files("../ItemShops/build/libs/ItemShops-1.1.1.jar"))
    compileOnly(files("../ARM-Guilds-Bridge/build/libs/ARM-Guilds-Bridge-1.0.0.jar"))
    compileOnly("com.artillexstudios:AxKothAPI:4")
    compileOnly(files("../enthusia-network/plugins/diary-keeper/target/DiaryKeeper-1.0.0.jar"))
    compileOnly(files("../enthusia-network/plugins/enthusia-currency/target/enthusia-currency-1.3.0.jar"))
    compileOnly(files("../enthusia-network/plugins/playtime-plugin/target/playtime-plugin-2.7.2.jar"))
    compileOnly(files("../enthusia-network/plugins/enthusia-commend/target/commend-events.jar"))

    // Shaded into JAR
    implementation("net.badgersmc:nexus-core:1.5.3")
    implementation("net.badgersmc:nexus-paper:1.5.3")
    implementation("com.typesafe:config:1.4.3")

    // Test — compileOnly deps need to be on test runtime classpath for mocking
    testImplementation("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    testImplementation("com.frengor:ultimateadvancementapi-common:3.0.0-beta-1")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.0.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("")
    relocate("net.badgersmc.nexus", "io.github.badgersmc.advancements.lib.nexus")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
