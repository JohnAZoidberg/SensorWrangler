load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "java_deps",
    exports = [
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:commons_io_commons_io",
        "@maven//:com_github_JetBrains_Exposed_exposed_core",
        "@maven//:com_github_JetBrains_Exposed_exposed_jdbc",
        "@maven//:joda_time_joda_time",
        "@maven//:org_postgresql_postgresql",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_jetbrains_kotlin_kotlin_reflect",
        "@maven//:org_openjfx_javafx_base",
        "@maven//:org_openjfx_javafx_controls",
        "@maven//:org_openjfx_javafx_graphics",
    ],
)

java_binary(
    name = "Cli",
    main_class = "me.danielschaefer.sensorwrangler.Cli",
    # TODO: Not sure how to include the src/style.css. Maybe it doesn't work like this
    resources = glob(["src/main/resources/**"]) + ["src/stylesheet.css"],
    runtime_deps = ["//src/me/danielschaefer/sensorwrangler:cli_lib"],
)

java_binary(
    name = "Gui",
    main_class = "me.danielschaefer.sensorwrangler.Main",
    resources = glob(["src/main/resources/**"]),
    runtime_deps = ["//src/me/danielschaefer/sensorwrangler:app_lib"],
)
