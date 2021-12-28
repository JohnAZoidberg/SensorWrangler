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
