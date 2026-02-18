{
  description = "SensorWrangler - Sensor data visualization tool";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};

        # Common packages needed for JavaFX
        javafxDeps = with pkgs; [
          libx11
          libxxf86vm
          libxtst
          libxrender
          libxi
          gtk3
          glib
          pango
          cairo
          gdk-pixbuf
          freetype
          fontconfig
          alsa-lib
          libGL
          libglvnd
          mesa
        ];

        # Create a wrapped Bazel that can run in an FHS environment
        bazelFHS = pkgs.buildFHSEnv {
          name = "bazel";
          targetPkgs = pkgs: with pkgs; [
            bazel
            jdk21
            gcc
            glibc
            zlib
            bash
            coreutils
            findutils
            gnugrep
            gnused
            gawk
            diffutils
            patch
            file
            which
            git
          ] ++ javafxDeps;
          runScript = "bazel";
        };

        # Wrapper script for running the GUI
        sensorWranglerRun = pkgs.writeShellScriptBin "sensorwrangler" ''
          cd ${./.}
          exec ${bazelFHS}/bin/bazel run //:Gui -- "$@"
        '';

        # FHS environment for running the app
        runFHS = pkgs.buildFHSEnv {
          name = "sensorwrangler";
          targetPkgs = pkgs: with pkgs; [
            jdk21
            bash
          ] ++ javafxDeps;
          runScript = "bash";
        };
      in
      {
        packages.default = sensorWranglerRun;

        apps.default = {
          type = "app";
          program = "${sensorWranglerRun}/bin/sensorwrangler";
        };

        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            bazelFHS
            jdk21
          ];

          shellHook = ''
            export JAVA_HOME="${pkgs.jdk21}"
            echo "SensorWrangler dev shell"
            echo "  Bazel: $(bazel --version 2>/dev/null || echo 'wrapped')"
            echo "  Java:  $(java -version 2>&1 | head -1)"
            echo ""
            echo "Commands:"
            echo "  bazel build //:Gui   - Build the GUI"
            echo "  bazel run //:Gui     - Run the GUI"
          '';
        };
      }
    );
}
