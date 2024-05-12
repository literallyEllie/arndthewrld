buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
//    id("convention-kotlin")
//    id("convention-style")
    id("convention-test")
}

//plugins {
//    `maven-publish`
//    `java-library`
//    id("java")
//}

//group = "com.arndthewrld"
//version = "0.0.1-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}