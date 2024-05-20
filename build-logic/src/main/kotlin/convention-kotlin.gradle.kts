import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

//    tasks.withType<JavaCompile> {
//            options.encoding = "UTF-8"
//            sourceCompatibility = "17"
//            targetCompatibility = "17"
//
//    }

val libs = the<LibrariesForLibs>()
val implementation by configurations

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.serialization)
}