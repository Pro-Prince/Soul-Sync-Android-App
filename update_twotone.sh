#!/bin/bash
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/Icons\.Rounded\./Icons.TwoTone./g'
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/Icons\.AutoMirrored\.Rounded\./Icons.AutoMirrored.TwoTone./g'
