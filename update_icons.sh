#!/bin/bash
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/Icons\.Filled\./Icons.Rounded./g'
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/Icons\.Default\./Icons.Rounded./g'
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/Icons\.Outlined\./Icons.Rounded./g'
