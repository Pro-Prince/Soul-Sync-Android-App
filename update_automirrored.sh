#!/bin/bash
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/Icons\.AutoMirrored\.Filled\./Icons.AutoMirrored.Rounded./g'
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/Icons\.AutoMirrored\.Outlined\./Icons.AutoMirrored.Rounded./g'
