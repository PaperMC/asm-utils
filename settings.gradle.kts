rootProject.name = "reflection-rewriter"

include("proxy-generator")
project(":proxy-generator").name = rootProject.name + "-proxy-generator"

include("runtime")
project(":runtime").name = rootProject.name + "-runtime"
