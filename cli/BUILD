package(default_visibility = ["@graknlabs_grakn_core//__subpackages__"])

load("@rules_pkg//:pkg.bzl", "pkg_tar")
load("@graknlabs_bazel_distribution//common:rules.bzl", "java_deps")

exports_files(
    glob(["conf/**"]),
    visibility = ["//visibility:public"],
)

java_library(
    name = "cli",
    srcs = glob(["*.java"]),
    deps = [
        "//dependencies/maven/artifacts/info/picocli:picocli",
        "//dependencies/maven/artifacts/org/apache/commons:commons-compress",
    ],
    runtime_deps = [
        "//dependencies/maven/artifacts/ch/qos/logback:logback-classic",
        "//dependencies/maven/artifacts/ch/qos/logback:logback-core",
    ],
    tags = ["maven_coordinates=io.grakn.cli:grakn-cli:{pom_version}"],
)

java_binary(
    name = "cli-binary",
    main_class = "grakn.core.cli.GraknCli",
    runtime_deps = ["cli"],
    tags = ["maven_coordinates=io.grakn.cli:grakn-cli-binary:{pom_version}"],
)

java_deps(
    name = "cli-deps",
    target = ":cli-binary",
    java_deps_root = "cli/services/lib/",
    visibility = ["//:__pkg__"],
    maven_name = True,
)

pkg_tar(
    name = "cli-component",
    deps = [":cli-deps"],
    files = {
        "//cli:conf/logback.xml": "cli/conf/logback.xml",
    },
    extension = "tgz",
    visibility = ["//visibility:public"],
)