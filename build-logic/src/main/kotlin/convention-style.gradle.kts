import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.accessors.dm.LibrariesForLibs
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
    id("org.jlleitschuh.gradle.ktlint")
}

val libs = the<LibrariesForLibs>()

detekt {
    config.setFrom(files("${rootProject.rootDir}/detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<Detekt> {
    reports.html.required.set(true)
}

val detektMain = tasks.findByName("detektMain")
if (detektMain != null) {
    val check by tasks
    check.dependsOn(detektMain)
}

ktlint {
    version.set(libs.versions.ktlint.cli.get())
    reporters {
        reporter(ReporterType.HTML)
    }
}

spotless {
    format("misc") {
        target("*.gradle", "*.gitignore")
        indentWithSpaces()
        trimTrailingWhitespace()
        endWithNewline()
    }
    json {
        target("*.json")
        simple()
    }
    yaml {
        target("*.yml", "*.yaml")
        jackson()
    }
}