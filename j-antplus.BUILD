# Bazel BUILD file to build j-antplus
# From https://github.com/glever/j-antplus
load("@rules_java//java:defs.bzl", "java_library")
load("@rules_jvm_external//:defs.bzl", "javadoc")

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
        "@maven//:org_apache_commons_commons_lang3",
    ],
)

java_library(
    name = "testing_deps",
    exports = [
        "@maven//:junit_junit",
    ],
)

# Manual tests that you can use to try out the capabilities
java_library(
    name = "test_stat_lib",
    srcs = glob(["src/test/java/be/glever/anttest/stats/*.java"]),
    deps = [":j_antplus"],
)
# TODO: Use a macro to avoid duplication
java_binary(
    name = "CadenceTest",
    main_class = "be.glever.anttest.CadenceTest_Main",
    srcs = glob(["src/test/java/be/glever/anttest/CadenceTest_Main.java"]),
    deps = [":j_antplus", ":test_stat_lib"],
)
java_binary(
    name = "FecTest",
    main_class = "be.glever.anttest.FecTest_Main",
    srcs = glob(["src/test/java/be/glever/anttest/FecTest_Main.java"]),
    deps = [":j_antplus", ":test_stat_lib"],
)
java_binary(
    name = "HrmTest",
    main_class = "be.glever.anttest.HrmTest_Main",
    srcs = glob(["src/test/java/be/glever/anttest/HrmTest_Main.java"]),
    deps = [":j_antplus", ":test_stat_lib"],
)
java_binary(
    name = "PowerTest",
    main_class = "be.glever.anttest.PowerTest_Main",
    srcs = glob(["src/test/java/be/glever/anttest/PowerTest_Main.java"]),
    deps = [":j_antplus", ":test_stat_lib"],
)
java_binary(
    name = "SpeedAndCadenceTest",
    main_class = "be.glever.anttest.SpeedAndCadenceTest_Main",
    srcs = glob(["src/test/java/be/glever/anttest/SpeedAndCadenceTest_Main.java"]),
    deps = [":j_antplus", ":test_stat_lib"],
)
java_binary(
    name = "SpeedCadenceTest",
    main_class = "be.glever.anttest.SpeedCadenceTest_Main",
    srcs = glob(["src/test/java/be/glever/anttest/SpeedCadenceTest_Main.java"]),
    deps = [":j_antplus", ":test_stat_lib"],
)
java_binary(
    name = "SpeedTest",
    main_class = "be.glever.anttest.SpeedTest_Main",
    srcs = glob(["src/test/java/be/glever/anttest/SpeedTest_Main.java"]),
    deps = [":j_antplus", ":test_stat_lib"],
)

# Automated tests
java_test(
    name = "junit_tests",
    srcs = glob([
        "src/test/java/be/glever/ant/**/*.java",
        "src/test/java/be/glever/AllTests.java",
    ]),
    test_class = "AllTests",
    deps = [
        "//:testing_deps",
        "//:j_antplus",
    ],
)

java_library(
    visibility = ["//visibility:public"],
    name = "j_antplus",
    srcs = glob(["src/main/java/**/*.java"]),
    resources = glob(["src/main/resources/**"]),
    deps = ["//:java_deps"],
)

javadoc(
    name = "j_antplus_javadoc",
    deps = [
      ":j_antplus"
    ],
)
