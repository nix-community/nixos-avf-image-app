# nixos-avf-image-app

Companion app for [nixos avf image](https://github.com/nix-community/nixos-avf)

Contains:
- Rust proxy server for github graphql including nix flake+module
- Android app source code for nixos image installer

# Supports

- Installation on debug builds (places file in /sdcard/linux/images.tar.gt)
- Installation on prod builds using replace script (extracts image to /sdcard/Download/image and adds bash script to be run in VM for replacing the partitions)

> [!IMPORTANT]
> The image only works on Android 16+ ([ » Beta Program ](https://www.google.com/android/beta)) and on Android 15 flavours that have the Android 16 Terminal patches backported (example: GrapheneOS)

# Instructions for prod builds
- you need the regular debian image installed
- method is "install using replace method"
- it will ask for permission to acccess all files, you need to approve that
- it will download the image and then start the terminal
- you will need to run: bash /mnt/shared/image/[replace.sh](http://replace.sh/)
- after that the vm will stop
- reopen terminal and run again: bash /mnt/shared/image/[replace.sh](http://replace.sh/)
- this time it will take longer, after the restart it should start nixos

# Notes for dev

fastlane must be run in android studio's terminal
(otherwise it may fail to run aapt2 etc due to dynamic linking)
