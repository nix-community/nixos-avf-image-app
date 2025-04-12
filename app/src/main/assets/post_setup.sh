#!/usr/bin/env bash

dd if=<(cat shared/image/root_part) of=/dev/vda2 oflag=direct bs=1G
rm -frv shared/image