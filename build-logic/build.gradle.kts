plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradle.kotlin.dsl)
    implementation(libs.gradle.plugin.kotlin.withVersion(embeddedKotlinVersion))
    implementation(libs.gradle.plugin.publish)
    implementation(libs.gradle.plugin.checkstyle)
    implementation(libs.gradle.plugin.javadocLinks)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}

fun Provider<MinimalExternalModuleDependency>.withVersion(version: String): Provider<String> {
    return map { "${it.module.group}:${it.module.name}:$version" }
}
