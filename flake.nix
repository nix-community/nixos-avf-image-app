{
  description = "A very basic flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
    android-nixpkgs = {
      url = "github:tadfisher/android-nixpkgs";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { self, nixpkgs, android-nixpkgs }:

    let
      supportedSystems = [ "x86_64-linux" ];
      forAllSystems = f: nixpkgs.lib.genAttrs supportedSystems (system: f system);

      nixpkgsFor = system: import nixpkgs {
        inherit system;
        config.allowUnfree = true;
        overlays = [ self.overlays.default ];
      };

      androidSdkFor = system: android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
        cmdline-tools-latest
        build-tools-35-0-0
        platform-tools
        platforms-android-36
        emulator
      ]);
    in

    {
      overlays.default = final: prev: {
        nixos-image-proxy-server = prev.callPackage ./package.nix { };
      };

      packages = forAllSystems (system: {
        nixos-image-proxy-server = (nixpkgsFor system).nixos-image-proxy-server;
        default = self.packages.${system}.nixos-image-proxy-server;
      });

      apps = forAllSystems (system:
        let
          pkgs = nixpkgsFor system;
          androidSdk = androidSdkFor system;
          
          # Helper script for building Android app
          buildScript = pkgs.writeShellScriptBin "build-android" ''
            if [ ! -f ./gradlew ]; then
              echo "Error: ./gradlew not found. Please run this command from the project root."
              exit 1
            fi
            
            export ANDROID_HOME="${androidSdk}/share/android-sdk"
            export ANDROID_SDK_ROOT="${androidSdk}/share/android-sdk"
            export JAVA_HOME="${pkgs.jdk17.home}"
            
            # Explicitly pass the aapt2 override to Gradle
            AAPT2_PATH="${androidSdk}/share/android-sdk/build-tools/35.0.0/aapt2"
            
            echo "Building Android App (Debug)..."
            exec ./gradlew assembleDebug \
              -Pandroid.aapt2FromMavenOverride="$AAPT2_PATH" \
              "$@"
          '';

          # Helper script for installing Android app
          installScript = pkgs.writeShellScriptBin "install-app" ''
            APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
            if [ ! -f "$APK_PATH" ]; then
              echo "Error: APK not found at $APK_PATH."
              echo "Please run 'nix run .#build-android' first."
              exit 1
            fi
            
            export ANDROID_HOME="${androidSdk}/share/android-sdk"
            export ANDROID_SDK_ROOT="${androidSdk}/share/android-sdk"
            
            echo "Installing $APK_PATH..."
            ${pkgs.android-tools.adb}/bin/adb install -r "$APK_PATH"
          '';

          # Helper script for logcat
          logcatScript = pkgs.writeShellScriptBin "logcat-app" ''
            PACKAGE_NAME="io.mkg20001.nixosimage"
            
            export ANDROID_HOME="${androidSdk}/share/android-sdk"
            export ANDROID_SDK_ROOT="${androidSdk}/share/android-sdk"
            
            echo "Starting logcat for package: $PACKAGE_NAME (Ctrl+C to stop)"
            
            # Try to get PID, fall back to grep if pidof fails
            PID=$(${pkgs.android-tools.adb}/bin/adb shell pidof -s "$PACKAGE_NAME" 2>/dev/null)
            if [ -n "$PID" ]; then
              exec ${pkgs.android-tools.adb}/bin/adb logcat --pid=$PID "$@"
            else
              echo "Could not get PID for $PACKAGE_NAME. Falling back to grep."
              exec ${pkgs.android-tools.adb}/bin/adb logcat "$@" | ${pkgs.gnugrep}/bin/grep "$PACKAGE_NAME"
            fi
          '';

          # Helper script to run everything
          runAppScript = pkgs.writeShellScriptBin "run-app" ''
            set -e
            PACKAGE_NAME="io.mkg20001.nixosimage"
            ACTIVITY_NAME=".MainActivity"

            # 1. Build
            ${buildScript}/bin/build-android
            
            # 2. Install
            ${installScript}/bin/install-app
            
            export ANDROID_HOME="${androidSdk}/share/android-sdk"
            export ANDROID_SDK_ROOT="${androidSdk}/share/android-sdk"
            
            # 3. Launch
            echo "Launching $PACKAGE_NAME/$ACTIVITY_NAME..."
            ${pkgs.android-tools.adb}/bin/adb shell am start -n "$PACKAGE_NAME/$ACTIVITY_NAME"
            
            # 4. Logcat
            ${logcatScript}/bin/logcat-app
          '';
        in
        {
          build-android = {
            type = "app";
            program = "${buildScript}/bin/build-android";
          };
          install-app = {
            type = "app";
            program = "${installScript}/bin/install-app";
          };
          logcat-app = {
            type = "app";
            program = "${logcatScript}/bin/logcat-app";
          };
          run-app = {
            type = "app";
            program = "${runAppScript}/bin/run-app";
          };
        }
      );

      nixosModules.nixos-image-proxy-server = import ./module.nix;

      devShells = forAllSystems (system:
        let
          pkgs = nixpkgsFor system;
          androidSdk = androidSdkFor system;
          
          # Helper script for the dev shell (build)
          buildAndroidInShell = pkgs.writeShellScriptBin "build-android" ''
             ./gradlew assembleDebug \
              -Pandroid.aapt2FromMavenOverride="${androidSdk}/share/android-sdk/build-tools/35.0.0/aapt2" \
              "$@"
          '';
          
          # Helper script for the dev shell (install)
          installAppInShell = pkgs.writeShellScriptBin "install-app" ''
            APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
            if [ ! -f "$APK_PATH" ]; then
              echo "Error: APK not found at $APK_PATH."
              echo "Please run 'build-android' first."
              exit 1
            fi
            adb install -r "$APK_PATH"
          '';
          
          # Helper script for the dev shell (logcat)
          logcatAppInShell = pkgs.writeShellScriptBin "logcat-app" ''
            PACKAGE_NAME="io.mkg20001.nixosimage"
            echo "Starting logcat for package: $PACKAGE_NAME (Ctrl+C to stop)"
            PID=$(adb shell pidof -s "$PACKAGE_NAME" 2>/dev/null)
            if [ -n "$PID" ]; then
              exec adb logcat --pid=$PID "$@"
            else
              echo "Could not get PID for $PACKAGE_NAME. Falling back to grep."
              exec adb logcat "$@" | grep "$PACKAGE_NAME"
            fi
          '';

          # Helper script for the dev shell (run-app)
          runAppInShell = pkgs.writeShellScriptBin "run-app" ''
             set -e
             build-android
             install-app
             echo "Launching io.mkg20001.nixosimage/.MainActivity..."
             adb shell am start -n io.mkg20001.nixosimage/.MainActivity
             logcat-app
          '';
        in
        {
          default = pkgs.mkShell {
            buildInputs = with pkgs; [
              androidSdk
              jdk17
              gradle
              buildAndroidInShell
              installAppInShell
              logcatAppInShell
              runAppInShell
              # Add android-tools for adb in devShell's PATH
              android-tools
              gnugrep # For grep in logcat-app fallback
            ];

            ANDROID_HOME = "${androidSdk}/share/android-sdk";
            ANDROID_SDK_ROOT = "${androidSdk}/share/android-sdk";
            JAVA_HOME = pkgs.jdk17.home;

            shellHook = ''
              # Write the android configuration to local.properties
              echo "sdk.dir=${androidSdk}/share/android-sdk" > local.properties
              echo "android.aapt2FromMavenOverride=${androidSdk}/share/android-sdk/build-tools/35.0.0/aapt2" >> local.properties
              
              echo "Android SDK 36 environment loaded."
              echo "local.properties updated with Nix paths."
              echo "Use 'run-app' to build, install, launch, and log."
            '';
          };
        }
      );
    };
}
