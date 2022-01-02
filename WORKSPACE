load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "new_git_repository")

rules_kotlin_version = "v1.5.0-beta-4"
rules_kotlin_sha = "6cbd4e5768bdfae1598662e40272729ec9ece8b7bded8f0d2c81c8ff96dc139d"
http_archive(
    name = "io_bazel_rules_kotlin",
    urls = ["https://github.com/bazelbuild/rules_kotlin/releases/download/%s/rules_kotlin_release.tgz" % rules_kotlin_version],
    sha256 = rules_kotlin_sha,
)

load("@io_bazel_rules_kotlin//kotlin:repositories.bzl", "kotlin_repositories", "versions")

kotlin_repositories()

load("@io_bazel_rules_kotlin//kotlin:core.bzl", "kt_register_toolchains")

kt_register_toolchains()

http_archive(
    name = "rules_jvm_external",
    sha256 = versions.RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % versions.RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % versions.RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    # TODO: Try to update the versions
    artifacts = [
        # For saving the configuration
        "com.fasterxml.jackson.core:jackson-databind:2.10.1",
        # *Tail*ing a file
        "commons-io:commons-io:2.6",
        # Recording to databases
        "com.github.JetBrains.Exposed:exposed-core:0.18.1",
        "com.github.JetBrains.Exposed:exposed-jdbc:0.18.1",
        "joda-time:joda-time:2.5",
        "org.postgresql:postgresql:42.2.5",
        # Logging (also run-time dependency of some other dependencies)
        "org.slf4j:slf4j-api:1.7.11",
        "org.jetbrains.kotlin:kotlin-reflect:1.5.0",

        "org.openjfx:javafx-base:11.0.1",
        "org.openjfx:javafx-base:win:11.0.1",
        "org.openjfx:javafx-base:linux:11.0.1",

        "org.openjfx:javafx-controls:11.0.1",
        "org.openjfx:javafx-controls:win:11.0.1",
        "org.openjfx:javafx-controls:linux:11.0.1",

        "org.openjfx:javafx-graphics:11.0.1",
        "org.openjfx:javafx-graphics:win:11.0.1",
        "org.openjfx:javafx-graphics:linux:11.0.1",
        "org.openjfx:javafx-graphics:mac:11.0.1",


        # j-antplus dependencies
        "javax.usb:usb-api:1.0.2",
        # slf4j already present above
        "io.projectreactor:reactor-core:3.3.4.RELEASE",
        "org.reactivestreams:reactive-streams:1.0.3",
        "org.usb4java:usb4java:1.3.0",
        "ch.qos.logback:logback-core:1.2.3",
        "ch.qos.logback:logback-classic:1.2.3",
        "junit:junit:4.13", # Only for testing
    ],
    repositories = [
        "https://maven-central.storage.googleapis.com/repos/central/data/",
        "https://repo1.maven.org/maven2",
        "https://jitpack.io",
    ],
)

http_archive(
    name = "rules_pkg",
    sha256 = "4ba8f4ab0ff85f2484287ab06c0d871dcb31cc54d439457d28fd4ae14b18450a",
    url = "https://github.com/bazelbuild/rules_pkg/releases/download/0.2.4/rules_pkg-0.2.4.tar.gz",
)

### j-antplus dependency ###
# Multiple ways of getting it.

# For local development.
# Needs a BUILD and WORKSPACE file (give in the bazel branch of JohnAZoidberg/j-antplus)
#local_repository(
#    name = "j_antplus",
#    path = "/home/zoid/cloudhome/projects/sensorwrangler/j-antplus",
#)

new_git_repository(
    name = "j_antplus",
    # Optionally use the bazel branch with BUILD and WORKSPACE file,
    # or use the j-antplus.BUILD file in this repo.
    #commit = "be48a010c5020b0c6a123847061a3569ecfbac28", # bazel branch

    commit = "6c7dddc809303fd664bb3ab8f1197a40731578ef", # master branch
    shallow_since = "1593867481 +0200",
    remote = "https://github.com/johnazoidberg/j-antplus",
    build_file = "@//:j-antplus.BUILD",
)

# http_archive is preferreed to new_git_repository.
# See: https://docs.bazel.build/versions/main/external.html#repository-rules
## TODO: Doesn't work. Not sure why. Can't find the BUILD file in the other repo
#jantplus_rev = "be48a010c5020b0c6a123847061a3569ecfbac28"
#http_archive(
#    name = "j_antplus",
#    url = "https://github.com/JohnAZoidberg/j-antplus/archive/%s.tar.gz" % jantplus_rev,
#    #sha256 = "8d0ae32b26a8229b8bef650e56811ed6efb36b9695c182a7f1d8c878b2d1be5a",
#    # TODO: Also doesn't work because then it can't find the dependencies
#    #build_file = "@//:j-antplus.BUILD",
#)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

rules_detekt_version = "0.5.0"
rules_detekt_sha = "65203efa2c7f252a9fbeba0abe651cd32c316858d3dbad550a5a34cf48bbe404"

http_archive(
    name = "rules_detekt",
    sha256 = rules_detekt_sha,
    strip_prefix = "bazel_rules_detekt-{v}".format(v = rules_detekt_version),
    url = "https://github.com/buildfoundation/bazel_rules_detekt/archive/v{v}.tar.gz".format(v = rules_detekt_version),
)

load("@rules_detekt//detekt:dependencies.bzl", "rules_detekt_dependencies")
rules_detekt_dependencies()

load("@rules_detekt//detekt:toolchains.bzl", "rules_detekt_toolchains")
rules_detekt_toolchains()
