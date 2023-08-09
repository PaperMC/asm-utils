plugins {
    val indraVer = "3.1.2"
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
            val asm = "org.ow2.asm:asm:9.5"
            api(asm)
            testImplementation(asm)
        }

        val checker = "org.checkerframework:checker-qual:3.37.0"
        compileOnlyApi(checker)
        testCompileOnly(checker)

        val junitVer = "5.9.3"
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVer")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVer")
    }
}
