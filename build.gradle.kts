import org.incendo.cloudbuildlogic.jmp
import java.nio.file.Files
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isDirectory

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

val filtered = tasks.register<FilterTestClasspath>("filteredTestClasspath") {
    outputDir.set(layout.buildDirectory.dir("filteredTestClasspath"))
    old.from(testDataSet.output)
    new.from(testDataNewTargets.output)
}

dependencies {
    testImplementation(files(filtered.flatMap { it.outputDir }))
    testImplementation(testDataNewTargets.output)
}

abstract class FilterTestClasspath : DefaultTask() {
    @get:InputFiles
    abstract val old: ConfigurableFileCollection

    @get:InputFiles
    abstract val new: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Inject
    abstract val fsOps: FileSystemOperations

    @TaskAction
    fun run() {
        if (!outputDir.get().asFile.toPath().exists()) {
            outputDir.get().asFile.mkdirs()
        } else {
            fsOps.delete {
                delete(outputDir.get())
            }
            outputDir.get().asFile.mkdirs()
        }

        val newExisting = mutableListOf<String>()
        for (file in new.files) {
            if (file.exists()) {
                Files.walk(file.toPath()).use { s ->
                    s.forEach {
                        if (it.isDirectory()) {
                            return@forEach
                        }
                        newExisting += file.toPath().relativize(it).invariantSeparatorsPathString
                    }
                }
            }
        }
        for (file in old.files) {
            if (file.exists()) {
                Files.walk(file.toPath()).use { s ->
                    s.forEach {
                        if (it.isDirectory()) {
                            return@forEach
                        }
                        val rel = file.toPath().relativize(it).invariantSeparatorsPathString
                        if (rel !in newExisting) {
                            it.copyTo(outputDir.get().asFile.toPath().resolve(rel).also { f -> f.parent.createDirectories() })
                        }
                    }
                }
            }
        }
    }
}
