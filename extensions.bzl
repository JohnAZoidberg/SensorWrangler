"""Module extensions for non-BCR dependencies."""

load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")

def _non_module_deps_impl(ctx):
    git_repository(
        name = "j_antplus",
        commit = "c992400132d1c1715f5ba002b24c020f16744acf",
        remote = "https://github.com/johnazoidberg/j-antplus",
    )

non_module_deps = module_extension(
    implementation = _non_module_deps_impl,
)
