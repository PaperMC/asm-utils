plugins {
    val indraVer = "3.1.3"
    id("net.kyori.indra") version indraVer
    id("net.kyori.indra.checkstyle") version indraVer
    id("net.kyori.indra.publishing") version indraVer
}

allprojects {
    plugins.apply("net.kyori.indra")
    plugins.apply("net.kyori.indra.checkstyle")
    plugins.apply("net.kyori.indra.publishing")

    indra {
        javaVersions {
            target(17)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        if ("-runtime" !in project.name) {
            val asm = "org.ow2.asm:asm:9.6"
            api(asm)
            testImplementation(asm)
            val asmCommons = "org.ow2.asm:asm-commons:9.6"
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
