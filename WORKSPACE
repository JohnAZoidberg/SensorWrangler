load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "new_git_repository", "git_repository")

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

register_toolchains("//:kotlin_toolchain")

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
        "io.github.microutils:kotlin-logging-jvm:2.1.14",

        "org.jetbrains.kotlin:kotlin-reflect:1.5.0",

        "org.openjfx:javafx-base:11.0.1",
        "org.openjfx:javafx-base:win:11.0.1",
        "org.openjfx:javafx-base:linux:11.0.1",
        "org.openjfx:javafx-base:mac:11.0.1",

        "org.openjfx:javafx-controls:11.0.1",
        "org.openjfx:javafx-controls:win:11.0.1",
        "org.openjfx:javafx-controls:linux:11.0.1",
        "org.openjfx:javafx-controls:mac:11.0.1",

        "org.openjfx:javafx-graphics:11.0.1",
        "org.openjfx:javafx-graphics:win:11.0.1",
        "org.openjfx:javafx-graphics:linux:11.0.1",
        "org.openjfx:javafx-graphics:mac:11.0.1",

        # j-antplus dependencies
        # Need to include them here because Bazel doesn't consider WORKSPACE of
        # subprojects. See: https://github.com/bazelbuild/bazel/issues/1943
        "javax.usb:usb-api:1.0.2",
        "io.projectreactor:reactor-core:3.3.4.RELEASE",
        "org.reactivestreams:reactive-streams:1.0.3",
        "org.usb4java:usb4java:1.3.0",
        "org.usb4java:usb4java-javax:1.3.0",
        "ch.qos.logback:logback-core:1.2.3",
        "ch.qos.logback:logback-classic:1.2.3",
        "org.apache.commons:commons-lang3:3.8.1",
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
#local_repository(
#    name = "j_antplus",
#    path = "/home/zoid/cloudhome/projects/j-antplus",
#)

# Fetch from GitHub and use bazel BUILD files from there to build it
# Still depends on this workspace!
git_repository(
    name = "j_antplus",
    commit = "83c35b5b7fb61198fc522c331deaf6a24c04c1df", # master branch
    shallow_since = "1593867481 +0200",
    remote = "https://github.com/johnazoidberg/j-antplus",
)
