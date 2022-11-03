plugins {
    `java-library`
    `maven-publish`
}

allprojects {
    plugins.apply("java-library")
    plugins.apply("maven-publish")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        val asm = "org.ow2.asm:asm:9.4"
        api(asm)
        testImplementation(asm)

        val checker = "org.checkerframework:checker-qual:3.25.0"
        compileOnlyApi(checker)
        testCompileOnly(checker)
    }

    publishing.publications.create<MavenPublication>("maven") {
        from(components["java"])
    }

    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    tasks.withType<Javadoc> {
        options.encoding = Charsets.UTF_8.name()
    }
    tasks.withType<ProcessResources> {
        filteringCharset = Charsets.UTF_8.name()
    }
}
