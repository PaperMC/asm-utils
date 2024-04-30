import org.incendo.cloudbuildlogic.jmp
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString

plugins {
    val indraVer = "3.1.3"
    id("net.kyori.indra") version indraVer
    id("net.kyori.indra.checkstyle") version indraVer
    id("net.kyori.indra.publishing") version indraVer
    id("org.incendo.cloud-build-logic.javadoc-links") version "0.0.14"
}

allprojects {
    plugins.apply("net.kyori.indra")
    plugins.apply("net.kyori.indra.checkstyle")
    plugins.apply("net.kyori.indra.publishing")
    plugins.apply("org.incendo.cloud-build-logic.javadoc-links")

    indra {
        javaVersions {
            target(17)
        }

        publishSnapshotsTo("paperSnapshots", "https://repo.papermc.io/repository/maven-snapshots/")
        publishReleasesTo("paperReleases", "https://repo.papermc.io/repository/maven-releases/")
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

    dependencies {
        if ("-runtime" !in project.name) {
            val asmVer = "9.6"
            val asm = "org.ow2.asm:asm:$asmVer"
            api(asm)
            testImplementation(asm)
            val asmCommons = "org.ow2.asm:asm-commons:$asmVer"
            api(asmCommons)
            testImplementation(asmCommons)
        }

        val checker = "org.checkerframework:checker-qual:3.42.0"
        compileOnlyApi(checker)
        testCompileOnly(checker)

        val junitVer = "5.10.2"
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVer")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVer")
    }
}

val testDataSet = sourceSets.create("testData")
val testDataNewTargets = sourceSets.create("testDataNewTargets")

dependencies {
    val oldRoot = layout.buildDirectory.dir("java/testData").get().asFile.toPath()
    val newRoot = layout.buildDirectory.dir("java/testDataNewTargets").get().asFile.toPath()
    testImplementation(testDataSet.output.filter {
        if (it.toPath().startsWith(oldRoot)) {
            !newRoot.resolve(oldRoot.relativize(it.toPath()).invariantSeparatorsPathString).exists()
        } else {
            true
        }
    })
    testImplementation(testDataNewTargets.output)
}
