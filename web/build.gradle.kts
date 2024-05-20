plugins {
    application
    id("convention-kotlin")
    id("convention-style")
    id("convention-test")
    kotlin("plugin.serialization") version libs.versions.kotlin.get() // doesn't like buildscript..
}

group = "com.arndthewrld.web"
version = "0.0.1-SNAPSHOT"

dependencies {
    // Utils
    implementation(libs.avaje.config)

    // JSON
    implementation(libs.jackson.core)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.datetime)

    // di
    implementation(libs.koin)

    // Pac (Security)
    implementation(libs.pac4j.javalin)
    // https://mvnrepository.com/artifact/org.pac4j/pac4j-oauth
    implementation(libs.pac4j.oauth)
    implementation(libs.jwt)

    // db
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.datetime)
    implementation(libs.hikaricp)
    implementation(libs.postgres)
    implementation(libs.h2)

    // web
    implementation(libs.javalin)

    // logging
    implementation(libs.kermit)
    implementation(libs.slf4j.simple)
}

application {
    mainClass.set("com.arndthewrld.app.AppKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.arndthewrld.app.AppKt"
    }
    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
