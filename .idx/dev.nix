{ pkgs, ... }: {
  channel = "stable-24.05";

  packages = [
    pkgs.openjdk17_headless
    pkgs.gradle
  ];

  env = {
    JAVA_HOME = "${pkgs.openjdk17}";
    ANDROID_HOME = "~/android-sdk";
  };

  idx = {
    extensions = [
      "redhat.java"
      "vscjava.vscode-gradle"
    ];
  };
}