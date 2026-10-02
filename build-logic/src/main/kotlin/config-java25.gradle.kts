import org.gradle.accessors.dm.LibrariesForLibs
import org.incendo.cloudbuildlogic.jmp

plugins {
    id("net.kyori.indra")
    id("net.kyori.indra.publishing")
    id("net.kyori.indra.checkstyle")
    id("org.incendo.cloud-build-logic.javadoc-links")
}

val libs = the<LibrariesForLibs>()

indra {
    javaVersions {
        target(25)
        strictVersions(true)
    }

    publishSnapshotsTo("paperSnapshots", "https://artifactory.papermc.io/artifactory/snapshots/")
    publishReleasesTo("paperReleases", "https://artifactory.papermc.io/artifactory/releases/")
    signWithKeyFromProperties("signingKey", "signingPassword")

    apache2License()

    github("PaperMC", "asm-utils") {
        ci(true)
    }

    configurePublications {
        pom {
            developers {
                jmp()
                developer {
                    id = "Machine-Maker"
                    name = "Jake Potrebic"
                    url = "https://github.com/Machine-Maker"
                }
                developer {
                    id = "kennytv"
                    name = "Nassim Jahnke"
                    url = "https://github.com/kennytv"
                }
            }
        }
    }
}

repositories {
    mavenCentral()
}

val mockitoAgent = configurations.create("mockitoAgent")

dependencies {
    compileOnlyApi(libs.jspecify)
    testCompileOnly(libs.jspecify)
    compileOnly(libs.jetbrainsAnnotations)
    testCompileOnly(libs.jetbrainsAnnotations)

    mockitoAgent(libs.mockito.core) { isTransitive = false }
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.junit)
    testImplementation(libs.assertj)
    testImplementation(libs.jupiterApi)
    testImplementation(libs.jupiterParams)
    testRuntimeOnly(libs.jupiterEngine)
    testRuntimeOnly(libs.platformLauncher)
}

tasks {
    test {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
    }
}

javadocLinks {
    override(libs.jspecify, "https://jspecify.dev/docs/api/")
}
