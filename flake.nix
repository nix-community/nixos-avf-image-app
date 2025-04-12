{
  description = "A very basic flake";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs?ref=nixos-unstable";
  };

  outputs = { self, nixpkgs }:

    let
      supportedSystems = [ "x86_64-linux" ];
      forAllSystems = f: nixpkgs.lib.genAttrs supportedSystems (system: f system);
    in

    {
      overlays.default = final: prev: {
        nixos-image-proxy-server = prev.callPackage ./package.nix { };
      };

      defaultPackage = forAllSystems (system: (import nixpkgs {
        inherit system;
        overlays = [ self.overlays.default ];
      }).nixos-image-proxy-server);

      nixosModules.nixos-image-proxy-server = import ./module.nix;

    };

}
