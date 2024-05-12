import kotlinx.kover.gradle.plugin.dsl.AggregationType
import kotlinx.kover.gradle.plugin.dsl.MetricType
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.jetbrains.kotlinx.kover")
}

val excludeClasses = listOf(
    "*.di.*"
)

koverReport {
    defaults {
        html { onCheck = true }
        xml { onCheck = true }

        log {
            onCheck = true
            coverageUnits = MetricType.INSTRUCTION
            aggregationForGroup = AggregationType.COVERED_PERCENTAGE
        }

        verify {
            onCheck = true
            rule {
                filters {
                    excludes { classes(excludeClasses) }
                }
                bound {
                    minValue = 60
                    metric = MetricType.INSTRUCTION
                    aggregation = AggregationType.COVERED_PERCENTAGE
                }
            }
        }
    }
}

val libs = the<LibrariesForLibs>()
val testImplementation = configurations.findByName("testImplementation")

dependencies {
    testImplementation?.let { testImplementation ->
        testImplementation(libs.kotlin.coroutines.core)
        testImplementation(libs.test.junit)
        testImplementation(libs.test.kotlin.common)
        testImplementation(libs.test.kotlin.coroutines)
        testImplementation(libs.test.kotlin.junit)
        testImplementation(libs.test.mockk)
    }
}

rootProject.dependencies {
    kover(project)
}

tasks.withType<Test> {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        showCauses = true
        showExceptions = true
        showStackTraces = true
        showStandardStreams = true
    }
}