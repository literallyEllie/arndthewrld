plugins {
    `kotlin-dsl`
    `java-library`
    id("convention-kotlin")
    id("convention-style")
    id("convention-test")
}

group = "com.arndthewrld.web"
version = "0.0.1-SNAPSHOT"

dependencies {
    // commons
    implementation(libs.avaje.config)
    implementation(libs.jackson.core)
    implementation(libs.jackson.kotlin)
    implementation(libs.slf4j.api)

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
//    implementation(libs.mongodb.driver.sync)
    implementation(libs.hikaricp)
    implementation(libs.postgres)

    // web
    implementation(libs.javalin)
}