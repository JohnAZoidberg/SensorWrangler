# Bazel BUILD file to build j-antplus
# From https://github.com/glever/j-antplus
load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "java_deps",
    exports = [
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:javax_usb_usb_api",
        "@maven//:io_projectreactor_reactor_core",
        "@maven//:org_reactivestreams_reactive_streams",
        "@maven//:org_usb4java_usb4java",
        "@maven//:org_usb4java_usb4java_javax",
        "@maven//:ch_qos_logback_logback_core",
        "@maven//:ch_qos_logback_logback_classic",
    ],
)

java_library(
    name = "testing_deps",
    exports = [
        "@maven//:junit_junit",
    ],
)

java_library(
    visibility = ["//visibility:public"],
    name = "j_antplus",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = ["@j_antplus//:java_deps"],
)
