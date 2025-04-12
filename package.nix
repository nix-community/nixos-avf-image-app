{ rustPlatform }:

rustPlatform.buildRustPackage rec {
  name = "nixos-image-proxy-server";

  src = ./.;

  cargoLock = {
    lockFile = ./Cargo.lock;
  };
}