#!/bin/bash

# Move to project top directory
pushd `dirname $0`

# Sorry, the prebuilt binary repository is private now.
# We need to clear licenses for redistribution.
git clone git@github.com:fjt7tdmi/rafi-prebuilt-binary.git

popd
