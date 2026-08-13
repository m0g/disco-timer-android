{
  description = "Disco Timer development environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
          config.android_sdk.accept_license = true;
        };

        # Keep in sync with app/build.gradle.kts (compileSdk / buildToolsVersion).
        # The SDK lives in the read-only nix store, so AGP cannot auto-install
        # anything it is missing -- every version it asks for must be listed here.
        platformVersion = "36";
        buildToolsVersion = "36.0.0";

        # Android SDK (no Android Studio needed)
        androidComposition = pkgs.androidenv.composeAndroidPackages {
          platformVersions = [ platformVersion ];
          buildToolsVersions = [ buildToolsVersion ];
          includeEmulator = false;
          includeNDK = false;
          includeSources = false;
          includeSystemImages = false;
        };
        androidSdk = androidComposition.androidsdk;
        sdkRoot = "${androidSdk}/libexec/android-sdk";

        jdk = pkgs.jdk17;

        # Fish config for nix shell with distinct appearance
        fishConfig = pkgs.writeText "nix-fish-config.fish" ''
          # Distinct prompt for nix shell
          function fish_prompt
            set_color magenta --bold
            echo -n "[nix] "
            set_color cyan
            echo -n (prompt_pwd)
            set_color yellow
            echo -n " ❯ "
            set_color normal
          end

          # Show environment info on shell entry
          set_color green --bold
          echo "╭─────────────────────────────────────╮"
          echo "│    Disco Timer Dev Environment      │"
          echo "╰─────────────────────────────────────╯"
          set_color normal
          echo "  JDK:     "(java -version 2>&1 | head -1 | cut -d'"' -f2)
          echo "  Gradle:  "(gradle --version 2>/dev/null | grep '^Gradle' | cut -d' ' -f2)" (project uses ./gradlew)"
          echo "  Android: SDK ${platformVersion}, build-tools ${buildToolsVersion}, adb"
          echo ""
        '';
      in
      {
        devShells.default = pkgs.mkShell {
          packages = [
            # Shell
            pkgs.fish
            pkgs.git

            # For Android development (no Android Studio)
            androidSdk
            pkgs.gradle_9 # AGP 9 / the ./gradlew wrapper both need Gradle 9.x
            jdk
          ];

          ANDROID_HOME = sdkRoot;
          ANDROID_SDK_ROOT = sdkRoot;
          JAVA_HOME = jdk.home;

          # On Linux the aapt2 AGP pulls from Maven is not patchelf'd and cannot
          # run; point it at the one from the nix SDK instead.
          GRADLE_OPTS = pkgs.lib.optionalString pkgs.stdenv.isLinux
            "-Dorg.gradle.project.android.aapt2FromMavenOverride=${sdkRoot}/build-tools/${buildToolsVersion}/aapt2";

          shellHook = ''
            # local.properties (gitignored) wins over ANDROID_HOME, so keep it
            # pointing at the nix SDK rather than an Android Studio install.
            if [ ! -f local.properties ] || ! grep -qxF "sdk.dir=${sdkRoot}" local.properties; then
              echo "sdk.dir=${sdkRoot}" > local.properties
            fi

            # Only drop into fish for interactive shells, so that
            # `nix develop -c ./gradlew ...` and direnv keep working.
            case "$-" in
              *i*) exec fish --init-command "source ${fishConfig}" ;;
            esac
          '';
        };
      }
    );
}
