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

    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.test {
    useJUnitPlatform()
}