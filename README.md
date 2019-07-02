# VivaldiModder
### What is VivaldiModder?
VivaldiModder is a simple program for applying custom CSS and JavaScript modifications to local installations of the [Vivaldi web browser](https://vivaldi.com). VivaldiModder provides an interface for listing and editing mod files, and patching multiple Vivaldi installations at once with automatic back-up of any files that would be overwritten.

![Screenshot 1](../eccbce9e9ab4df1af53aff9ace801c769e4a92a0/screenshots/user_interface.png?raw=true)

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

#### Running
1) Run the .jar file using `javaw -jar VivaldiModder-[VERSION].jar [config-file]`.
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
