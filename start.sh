#!/bin/bash

# Unset Snap-related environment variables that cause library conflicts
unset GTK_PATH
unset LD_LIBRARY_PATH
unset SNAP
unset SNAP_NAME
unset SNAP_INSTANCE_NAME
unset SNAP_ARCH
unset SNAP_REVISION

# Force host libraries so WebKit avoids the snap runtime glibc
export LD_LIBRARY_PATH="/lib/x86_64-linux-gnu:/usr/lib/x86_64-linux-gnu"
export GIO_MODULE_DIR="/usr/lib/x86_64-linux-gnu/gio/modules"

# Load NVM
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh"

# Load Cargo
source "$HOME/.cargo/env"

# Run the application
npm run start