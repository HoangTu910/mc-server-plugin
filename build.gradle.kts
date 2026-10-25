import java.security.MessageDigest

plugins {
    java
}

group = "com.example"
version = "0.1.0"

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(
        "io.papermc.paper:paper-api:26.2.build.134-stable"
    )

    testImplementation("io.papermc.paper:paper-api:26.2.build.134-stable")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

// Zips resourcepack/ into the client resource pack and writes its SHA-1,
// which server.properties needs (resource-pack-sha1).
val resourcePackZip = tasks.register<Zip>("resourcePackZip") {
    group = "build"
    description = "Builds the client resource pack zip."
    from("resourcepack")
    archiveFileName.set("survival-resourcepack.zip")
    destinationDirectory.set(layout.buildDirectory.dir("resourcepack"))
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    doLast {
        val zip = archiveFile.get().asFile
        val sha1 = MessageDigest.getInstance("SHA-1")
            .digest(zip.readBytes())
            .joinToString("") { "%02x".format(it) }
        File(zip.parentFile, "${zip.name}.sha1").writeText(sha1)
        println("Resource pack: ${zip.path}")
        println("SHA-1: $sha1")
    }
}

tasks.build {
    dependsOn(resourcePackZip)
}
