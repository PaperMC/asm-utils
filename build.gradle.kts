import org.incendo.cloudbuildlogic.javadoclinks.JavadocLinksExtension
import org.incendo.cloudbuildlogic.jmp
import java.nio.file.Files
import kotlin.io.path.copyTo
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isDirectory

plugins {
    alias(libs.plugins.indra)
    alias(libs.plugins.indraCheckstyle)
    alias(libs.plugins.indraPublishing)
    alias(libs.plugins.javadocLinks)
}

allprojects {
    plugins.apply("net.kyori.indra")
    plugins.apply("net.kyori.indra.checkstyle")
    plugins.apply("net.kyori.indra.publishing")
    plugins.apply("org.incendo.cloud-build-logic.javadoc-links")

    indra {
        javaVersions {
            target(21)
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

    dependencies {
        if ("-runtime" !in project.name) {
            api(rootProject.libs.asm)
            testImplementation(rootProject.libs.asm)
            api(rootProject.libs.asmCommons)
            testImplementation(rootProject.libs.asmCommons)
        }

        compileOnlyApi(rootProject.libs.jspecify)
        testCompileOnly(rootProject.libs.jspecify)
        compileOnly(rootProject.libs.jetbrainsAnnotations)
        testCompileOnly(rootProject.libs.jetbrainsAnnotations)

        testImplementation(rootProject.libs.jupiterApi)
        testImplementation(rootProject.libs.jupiterParams)
        testRuntimeOnly(rootProject.libs.jupiterEngine)
        testRuntimeOnly(rootProject.libs.platformLauncher)
    }

    javadocLinks {
        override(startsWithAnyOf("org.ow2.asm:asm"), JavadocLinksExtension.LinkOverride.Simple("https://asm.ow2.io/javadoc"))
        override(rootProject.libs.jspecify, "https://jspecify.dev/docs/api/")
    }
}
val mainForNewTargets = sourceSets.create("mainForNewTargets")

val testDataSet = sourceSets.create("testData")
val testDataNewTargets = sourceSets.create("testDataNewTargets")

val filtered = tasks.register<FilterTestClasspath>("filteredTestClasspath") {
    outputDir.set(layout.buildDirectory.dir("filteredTestClasspath"))
    old.from(testDataSet.output)
    new.from(testDataNewTargets.output)
}

dependencies {
    api(mainForNewTargets.output)
    testRuntimeOnly(files(filtered.flatMap { it.outputDir })) // only have access to old targets at runtime, don't use them in actual tests
    testImplementation(testDataNewTargets.output)

    testDataNewTargets.implementationConfigurationName(mainForNewTargets.output)
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
