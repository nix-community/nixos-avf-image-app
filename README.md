# nixos-avf-image-app

Companion app for [nixos avf image](https://github.com/nix-community/nixos-avf)

Contains:
- Rust proxy server for github graphql including nix flake+module
- Android app source code for nixos image installer

# Supports

- Installation on debug builds (places file in /sdcard/linux/images.tar.gt)
- Installation on prod builds using replace script (extracts image to /sdcard/Download/image and adds bash script to be run in VM for replacing the partitions)

> [!IMPORTANT]
> The image only works on Android 16+. Additionally this has only been tested on Pixels, this may not work with other virtualization Engines from different vendors.

# Notes for dev on nixos

fastlane must be run in android studio's terminal
(otherwise it may fail to run aapt2 etc due to dynamic linking)
