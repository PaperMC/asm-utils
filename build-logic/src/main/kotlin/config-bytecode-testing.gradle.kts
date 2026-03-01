import java.nio.file.Files
import kotlin.io.path.*

plugins {
    id("config-java")
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
