#!/bin/bash

set -euxo pipefail

IMG_LOC=/mnt/shared/image
VM_LOC=/mnt/internal/linux

STEP_MARKER=step_2

# Only adds vda3
step_1() {
  BEFORE=$(echo -e '],\n            "writable": true')
  # variables are from crosvm not bash
  # shellcheck disable=SC2016
  AFTER=$(echo -e ',{"label":"nixos", "path": "$PAYLOAD_DIR/nixos_root", "writable": true, "guid": "13ac699a-4b83-4618-923d-b69fe90e379e"}],\n            "writable": true')

  sudo truncate -s 8GiB "$VM_LOC/nixos_root"

  # TODO: use deterministic nixos root guid
  VM_CONFIG=$(sudo cat "$VM_LOC/vm_config.json")
  VM_REPLACED=${VM_CONFIG/"$BEFORE"/"$AFTER"}
  echo "$VM_REPLACED" | sudo tee "$VM_LOC/vm_config.json"

  touch "$STEP_MARKER"

  sudo reboot
}

# This replaces uefi, etc
step_2() {
  sudo chmod 777 /dev/vda3
  size=$(du "$IMG_LOC/root_part" | grep -o "[0-9]*")
  iters=$(( size / ( 1024 * 250 ) ))
  for i in $(seq 0 $iters); do
    dd "if=$IMG_LOC/root_part" "of=/dev/vda3" bs=250M count=1 "seek=$i" "skip=$i"
    sync
  done
  # dd "if=$IMG_LOC/root_part" "of=/dev/vda3" bs=250M oflag=sync

  cp "$IMG_LOC/efi_part" .
  sudo umount /boot/efi
  sudo rm "$VM_LOC/efi_part"
  sync
  sleep 3s
  sudo dd if=efi_part bs=1G oflag=direct "of=$VM_LOC/efi_part"
  sync

  # NOTE: we can't just copy root_part as virtiofs from crosvm seems to
  # magically break in most circumstances that involve larger writes
  # We somehow made it work for efi and

  cp "$IMG_LOC/vm_config.json" .
  sudo cp vm_config.json "$VM_LOC/vm_config.json"

  cp "$IMG_LOC/build_id" .
  sudo cp build_id "$VM_LOC/build_id"

  sudo rm "$VM_LOC/root_part"
  sudo mv "$VM_LOC/nixos_root" "$VM_LOC/root_part"

  rm -rfv "$IMG_LOC"

  sudo reboot
}

if [ ! -e "$STEP_MARKER" ]; then
  step_1
else
  step_2
fi
