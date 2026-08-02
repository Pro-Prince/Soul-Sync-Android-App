#!/bin/bash
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/androidx\.compose\.material\.icons\.rounded/androidx.compose.material.icons.twotone/g'
find app/src/main/java/com/example -type f -name "*.kt" -print0 | xargs -0 sed -i 's/androidx\.compose\.material\.icons\.automirrored\.rounded/androidx.compose.material.icons.automirrored.twotone/g'
