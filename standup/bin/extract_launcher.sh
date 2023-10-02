#!/bin/bash

# Find directories
root_dir=$(pwd -P)

while [ ! -f "$root_dir/settings.gradle" ] && [ ! -f "$root_dir/settings.gradle.kts" ]; do
    echo "Checking $root_dir"
    # If "settings.gradle" not found in the current directory, move up one level
    root_dir=$(dirname "$root_dir")

    # Check if we have reached the root directory ("/")
    if [ "$root_dir" == "/" ]; then
        echo "File 'settings.gradle' not found in the directory hierarchy."
        exit 1
    fi
done

echo "Found root_dir: $root_dir"

su_dir="$root_dir/standup"
brand_dir="$su_dir/local/brand"
res_dir="$su_dir/src/main/res"

cd "$brand_dir" || exit 1  # Exit if 'cd' fails

# Remove PNG and SVG files
rm -f *.png *.svg

# Unzip Brand.zip
unzip -o "Brand.zip"

# Loop through files with "_*dpi.png" suffix
for file in *; do
    # Check if file exists
    if [[ -f "$file" ]]; then
        echo "# Found $file"

        if [[ $file =~ ic_su_launcher_.*\.png ]]; then
            # Extract the DPI suffix
	    dpi=$(echo "$file" | sed -n 's/.*_\(.*\)dpi\.png/\1/p')

            echo "Found DPI: $dpi"

            # Determine the target directory
            target_dir="$res_dir/mipmap-${dpi}"
            mkdir -p "$target_dir" || exit 1  # Create target directory with error handling

            # New filename after renaming
            new_file_name=$(echo "$file" | sed "s/_[^_]*dpi\.png/.png/")

            # Move the file
            mv "$file" "$target_dir/$new_file_name" || exit 1  # Move file with error handling
            echo "Moved $file -> $target_dir/$new_file_name"
        elif [[ $file =~ .*\.svg ]]; then
            echo "** Don't forget to import SVG asset: $file"
        else
            echo "Don't know how to process $file"
        fi
    fi
done

