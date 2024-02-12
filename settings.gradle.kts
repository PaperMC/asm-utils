plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "asm-utils"

include("reflection-rewriter/rewriter")
project(":reflection-rewriter/rewriter").name = "reflection-rewriter"

include("reflection-rewriter/proxy-generator")
project(":reflection-rewriter/proxy-generator").name = "reflection-rewriter-proxy-generator"

include("reflection-rewriter/runtime")
project(":reflection-rewriter/runtime").name = "reflection-rewriter-runtime"
