load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

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
    artifacts = [
        #"com.expedia:graphql-kotlin:1.0.0-RC5",
        #"com.expedia:graphql-kotlin-schema-generator:1.0.0-RC5",
        #"com.expedia:graphql-kotlin-federation:1.0.0-RC5",
        #"com.graphql-java:graphql-java:13.0",
        #"org.opentest4j:opentest4j:1.1.1",
        #"org.apiguardian:apiguardian-api:1.0.0",
        #"org.junit.platform:junit-platform-commons:1.4.2",
        #"org.junit.jupiter:junit-jupiter-api:5.4.2",
        #"org.junit.jupiter:junit-jupiter-params:5.4.2",
        #"org.apache.logging.log4j:log4j-core:2.16.0",
        # SensorWrangler
#        # For saving the configuration
#        "com.fasterxml.jackson.core:jackson-databind:2.10.1"
#        # *Tail*ing a file
#        "commons-io:commons-io:2.6"
#        # Recording to databases
#        "exposed-core-0.18.1"
#        "exposed-jdbc-0.18.1
#        "joda-time:joda-time:2.5"
#        "org.postgresql:postgresql:42.2.5"
#        # Connecting to ANT+ sensors
#        # TODO: Build it from source [j-antplus](https://github.com/glever/j-antplus)
#        # Logging (also run-time dependency of some other dependencies)
#        "org.slf4j:slf4j-api:1.7.11"
    ],
    repositories = [
        "https://maven-central.storage.googleapis.com/repos/central/data/",
        "https://repo1.maven.org/maven2",
    ],
)

http_archive(
    name = "rules_pkg",
    sha256 = "4ba8f4ab0ff85f2484287ab06c0d871dcb31cc54d439457d28fd4ae14b18450a",
    url = "https://github.com/bazelbuild/rules_pkg/releases/download/0.2.4/rules_pkg-0.2.4.tar.gz",
)
