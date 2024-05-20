buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

plugins {
    id("convention-test")
}

group = "com.arndthewrld"
version = "0.0.1"

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}