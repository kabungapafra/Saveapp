#!/bin/bash

# Target Directory
RES_DIR="app/src/main/res"

echo "Starting Global Branding Sync Phase 2 (Holdout Cleanup)..."

# 1. Broad Primary Blue Replacements (Including Holdouts)
# We catch vivid blues that were missed or are specific to certain items
PRIMARY_BLUES=("#342AEB" "#137FEC" "#1A3CCC" "#1E40AF" "#2D5BE3" "#2563EB" "#1976D2" "#3B82F6" "#2196F3" "#1D4ED8" "#1D7CF2" "#1A6EE0" "#002196F3" "#2196f3" "#303F9F" "#1D7CF2")
for old_color in "${PRIMARY_BLUES[@]}"; do
    echo "Replacing $old_color with #215DA1..."
    find "$RES_DIR" -type f -name "*.xml" -exec sed -i "s/$old_color/#215DA1/gi" {} +
done

# 2. Light Tint Replacements
# Catching specific light blue accents that haven't been unified
LIGHT_TINTS=("#BFDBFE" "#E0F2FE" "#93C5FD" "#F0F7FF" "#F5F7FF" "#EFF6FF" "#E3F2FD" "#E1EAFE" "#E6F0FD" "#DBEAFE" "#EBEFF6" "#DFEEF9" "#93A8D8")
for old_color in "${LIGHT_TINTS[@]}"; do
    echo "Replacing $old_color with #E9F2FB..."
    find "$RES_DIR" -type f -name "*.xml" -exec sed -i "s/$old_color/#E9F2FB/gi" {} +
done

# 3. Special case for opacity blues (e.g., #4D2563EB -> #4D215DA1)
# We find any hex ending in 2563EB and replace it with 215DA1 preserving the prefix
echo "Normalizing opacity-based hex codes..."
find "$RES_DIR" -type f -name "*.xml" -exec sed -i "s/2563EB/215DA1/gi" {} +
find "$RES_DIR" -type f -name "*.xml" -exec sed -i "s/1976D2/215DA1/gi" {} +
find "$RES_DIR" -type f -name "*.xml" -exec sed -i "s/137FEC/215DA1/gi" {} +

echo "Branding Sync Phase 2 Complete!"
