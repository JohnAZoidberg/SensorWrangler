load("@rules_java//java:defs.bzl", "java_binary", "java_library")
load("@rules_kotlin//kotlin:core.bzl", "kt_kotlinc_options", "kt_javac_options", "define_kt_toolchain")

package(default_visibility = ["//visibility:public"])

# Build with:
# > bazel build --extra_toolchains='//:werror_toolchain'
# to build with -Werror enabled. Useful for CI.
kt_kotlinc_options(
    name = "kotlinc_werror",
    warn = "error",
)
kt_javac_options(
    name = "javac_werror",
    warn = "error",
)
define_kt_toolchain(
    name = "werror_toolchain",
    kotlinc_options = ":kotlinc_werror",
    javac_options = ":javac_werror",
)

java_library(
    name = "lib_deps",
    exports = [
        # Miscellaneous
        "@maven//:commons_io_commons_io",
        "@maven//:org_jetbrains_kotlin_kotlin_reflect",

        # Configuration Import/Export
        "@maven//:com_fasterxml_jackson_core_jackson_databind",

        # Logging
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:ch_qos_logback_logback_core",
        "@maven//:ch_qos_logback_logback_classic",
        "@maven//:io_github_microutils_kotlin_logging_jvm",
    ],
)

java_library(
    name = "gui_deps",
    exports = [
        "@maven//:org_openjfx_javafx_base",
        "@maven//:org_openjfx_javafx_graphics",
        "@maven//:org_openjfx_javafx_controls",
    ] + select({
        "@platforms//os:linux": [
            "@maven//:org_openjfx_javafx_base_linux",
            "@maven//:org_openjfx_javafx_controls_linux",
            "@maven//:org_openjfx_javafx_graphics_linux",
        ],
        "@platforms//os:windows": [
           "@maven//:org_openjfx_javafx_graphics_win",
           "@maven//:org_openjfx_javafx_base_win",
           "@maven//:org_openjfx_javafx_controls_win",
        ],
        "@platforms//os:macos": [
           "@maven//:org_openjfx_javafx_graphics_mac",
           "@maven//:org_openjfx_javafx_base_mac",
           "@maven//:org_openjfx_javafx_controls_mac",
        ],
    }),
)

java_binary(
    name = "Cli",
    main_class = "me.danielschaefer.sensorwrangler.Cli",
    classpath_resources = glob(["src/main/resources/cli/logback.xml"]),
    runtime_deps = [
        "//src/me/danielschaefer/sensorwrangler:cli_lib"
    ],
)

java_binary(
    name = "Gui",
    main_class = "me.danielschaefer.sensorwrangler.Main",
    classpath_resources = glob(["src/main/resources/gui/logback.xml"]),
    resources = glob(["src/main/resources/*"]),
    runtime_deps = [
        "//src/me/danielschaefer/sensorwrangler:gui_lib",
    ],
)
