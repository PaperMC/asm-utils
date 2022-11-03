rootProject.name = "reflection-rewriter"

include("proxy-generator")
project(":proxy-generator").name = rootProject.name + "-proxy-generator"
