# VivaldiModder
[![Build Status](https://travis-ci.com/Komposten/VivaldiModder.svg?branch=master)](https://travis-ci.com/Komposten/VivaldiModder) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=Komposten_VivaldiModder&metric=alert_status)](https://sonarcloud.io/dashboard?id=Komposten_VivaldiModder) [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=Komposten_VivaldiModder&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=Komposten_VivaldiModder)
### What is VivaldiModder?
VivaldiModder is a simple program for applying custom CSS and JavaScript modifications to local installations of the [Vivaldi web browser](https://vivaldi.com). VivaldiModder provides an interface for listing and editing mod files, and patching multiple Vivaldi installations at once with automatic back-up of any files that would be overwritten.

![Screenshot 1](../a00ea1d1a93f8b294d109dd8aa01d521c325d402/screenshots/user_interface.png?raw=true)

### Features
- Simple user interface for selecting mod files and Vivaldi installation directories.
- Patch multiple Vivaldi installations at once.
- Automatic back-up of files before overwriting them.
- Automatically add scripts and styles to browser.html (if you don't include a browser.html file in the files to copy).

### Running VivaldiModder
**Operating system support**

VivaldiModder has been tested on Windows (Windows 10) and Linux (Zorin OS 15). It will probably work on Mac OSX as well.

**Download a pre-built version**
1) Download an existing [release](https://github.com/Komposten/VivaldiModder/releases).
2) Extract the .zip archive.

**Or build the latest version using Maven**
1) Clone the repository.
2) Open a command prompt and navigate to the repository's root folder.
3) Run `mvn package`. Requires a JDK and Maven.
4) Find the .jar and required libraries in `/target/packaged`.

#### Running with Java installed
If you have Java installed, there are two ways to run VivaldiModder:
1) By double-clicking the `VivaldiModder-[VERSION].jar` (on Windows)
2) By using `javaw -jar VivaldiModder-[VERSION].jar [config-file]` in a shortcut or cmd/terminal (on Linux: use `java` instead of `javaw`).
    - `config-file` is a path to the config file to save the mod configurations to. `config.ini` is used as default if this parameter is not specified.

#### Running without installing Java
VivaldiModder requires Java to run. However, if you don't want to install Java you can download a "copy-and-run" version that doesn't install anything and only runs when you tell it to run.
1) Download a compressed (.zip or .tar.gz) Java runtime (and dev kit) from https://jdk.java.net/ and unzip it to your preferred location.
2) Run VivaldiModder using `C:\path\to\java\bin\javaw.exe -jar VivaldiModder-[VERSION].jar [config-file]` in a shortcut or cmd/terminal (on Linux: use `java` instead of `javaw.exe`). 
    - `config-file` is a path to the config file to save the mod configurations to. `config.ini` is used as default if this parameter is not specified.
  

### Config file format
The config files used by VivaldiModder use the format outlined below. Normally, you don't need to edit these files manually.
```
# Path to the mod directory
mod.dir=C:\path\to\modDir

# Paths to the Application folders inside your Vivaldi installations
vivaldi.dirs="C:\path\to\Vivaldi\Application","C:\path\to\another\Vivaldi\Application"

# Instructions for mod file copying
someStyle.css>\resources\vivaldi\style
someScript.js>\resources\vivaldi\scripts
someImage.jpg>\resources\vivaldi\style

# |exclude prevents a file from being added to browser.html
someScript2.js>\resources\vivaldi\scripts|exclude
```

### Dependencies
- [Komposten's Utilities](https://github.com/Komposten/Utilities)
- [Jsoup](https://jsoup.org)

### License
This program is free software as long as the terms of the GNU GPL v3 license (or later versions, at your option) are complied with. See [LICENSE](LICENSE) for the full license text.

I reserve the exclusive right to re-license the code.
