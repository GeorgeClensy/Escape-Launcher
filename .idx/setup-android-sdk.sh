#!/usr/bin/env bash

# Exit on error
set -e

# Variables
SDK_URL="https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"
SDK_DIR="$HOME/android-sdk"
CMDLINE_TOOLS_DIR="$SDK_DIR/cmdline-tools"
PROFILE_FILE="$HOME/.bashrc" # or .zshrc if using zsh

# Create directories
mkdir -p "$CMDLINE_TOOLS_DIR"

# Download SDK
echo "Downloading Android SDK command line tools..."
curl -L "$SDK_URL" -o /tmp/commandlinetools.zip

# Unzip
echo "Unzipping SDK..."
unzip -q /tmp/commandlinetools.zip -d /tmp
mv /tmp/cmdline-tools "$CMDLINE_TOOLS_DIR/latest"

# Clean up
rm /tmp/commandlinetools.zip

# Set environment variables
echo "Configuring environment variables..."
{
    echo ""
    echo "# Android SDK"
    echo "export ANDROID_SDK_ROOT=$SDK_DIR"
    echo "export PATH=\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools:\$PATH"
} >> "$PROFILE_FILE"

# Apply changes to current session
export ANDROID_SDK_ROOT=$SDK_DIR
export PATH=$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH

# Accept licenses
echo "Installing SDK packages for headless Gradle builds..."
yes | sdkmanager --licenses >/dev/null

# Install essential packages
sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"

echo "Android SDK setup complete. You can now build with Gradle in a headless environment."
echo "You will need to set the sdk.dir=/home/user/android-sdk in local.properties"
echo "You will need to manually import google-services.json"