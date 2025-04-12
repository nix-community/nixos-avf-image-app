{ config, lib, pkgs, ... }:

with lib;

let
  cfg = config.services.nixos-image-proxy-server;
  nixos-image-proxy-server = pkgs.nixos-image-proxy-server;
  format = pkgs.formats.json {};
in
{
  options = {
    services.nixos-image-proxy-server = {
      enable = mkEnableOption "nixos-image-proxy-server";

      /* port = mkOption {
        description = "Port to listen at";
        type = types.int;
        default = 3333;
      };

      token = mkOption {
        description = "GitHub Token";
        type = types.str;
      }; */

      config = mkOption {
        description = "configuration";
        type = config.type;
      };

      openFirewall = mkOption {
        type = types.bool;
        default = false;
        description = "Open ports in the firewall for nixos-image-proxy-server.";
      };
    };
  };

  config = mkIf (cfg.enable) {
    networking.firewall = mkIf cfg.openFirewall {
      allowedTCPPorts = [ cfg.config.port ];
    };

    systemd.services.nixos-image-proxy-server = with pkgs; {
      wantedBy = [ "multi-user.target" ];
      after = [ "network.target" ];
      requires = [ "network-online.target" ];

      description = "nixos-image-proxy-server";

      environment.CONFIG = with builtins; format.generate "config.json" cfg.config;

      serviceConfig = {
        Type = "simple";
        DynamicUser = true;
        User = "nixos-image";
        ExecStart = "${nixos-image-proxy-server}/bin/nixos-image-proxy-server";
      };
    };
  };
}
