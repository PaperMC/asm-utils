import org.gradle.accessors.dm.LibrariesForLibs
import org.incendo.cloudbuildlogic.javadoclinks.JavadocLinksExtension

plugins {
    id("net.kyori.indra")
    id("org.incendo.cloud-build-logic.javadoc-links")
}

val libs = the<LibrariesForLibs>()

dependencies {
    api(libs.asm)
    testImplementation(libs.asm)
    api(libs.asmCommons)
    testImplementation(libs.asmCommons)
}

javadocLinks {
    override(startsWithAnyOf("org.ow2.asm:asm"), JavadocLinksExtension.LinkOverride.Simple("https://asm.ow2.io/javadoc"))
}
