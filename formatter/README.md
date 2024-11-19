# Formatting

This directory contains the Eclipse formatting configuration files used to enforce the same code style on all contributors.

## Eclipse
Import the formatting configs by Settings->Java->Code Style->"Import"

## IntelliJ
The following plugin extends the import capabilities of the Eclipse Formatter in IntelliJ.

In case you don't want to use it, you can still import the formatter xml in your code style settings. But note that not all configs are supported.
https://plugins.jetbrains.com/plugin/6546-adapter-for-eclipse-code-formatter

1. Install the plugin
2. Open the plugin configuration in settings
3. Add the formatter config files (see the screenshot below)
![IntelliJ_Formatter_Plugin.PNG](IntelliJ_Formatter_Plugin.PNG)

## Visual Code Studio
The following link provides a visual guide on how to set the up the eclipse formatter in VSCode.

https://code.visualstudio.com/docs/java/java-linting


## Formatting
This directory contains the Eclipse formatting configuration files used to enforce the same code style for all contributors.

## HowTo: Import and Use eclipse-js-formatter.xml in Eclipse
This guide explains how to import and configure the eclipse-js-formatter.xml file to format your JavaScript code consistently in Eclipse.

## Prerequisites

1. Eclipse IDE (version supporting JavaScript Development Tools, such as Eclipse IDE for Web Developers).
2. The eclipse-js-formatter.xml file from this repository.

## Step 1: Open the Eclipse Preferences
1. Launch Eclipse IDE.
2. Navigate to the preferences by clicking:
- On Windows/Linux: Window > Preferences
- On macOS: Eclipse > Preferences

## Step 2: Access the Formatter Settings
1. In the Preferences window, expand the following menu: JavaScript > Code Style > Formatter.
2. You will see an option to manage active profiles. Click the Import... button.

## Step 3: Import eclipse-js-formatter.xml
1. In the import dialog, locate the eclipse-js-formatter.xml file:
- If you cloned this repository, navigate to the formatter folder.
2. Select the eclipse-js-formatter.xml file and click Open.
3. The file will be imported as a new formatter profile.
Select the imported profile from the list and click Apply and Close.

## Step 4: Format Your Code
To apply the formatting to your JavaScript files:

1. Open any .js file in Eclipse.
2. Select the code (or press Ctrl + A / Cmd + A to select all).
3. Use the shortcut Ctrl + Shift + F (Windows/Linux) or Cmd + Shift + F (macOS) to format the selected code according to the rules defined in eclipse-js-formatter.xml.

Alternatively, you can configure auto-format on save:

1. Navigate to JavaScript > Editor > Save Actions in the preferences.
2. Enable Perform the selected actions on save.
3. Check Format source code and click Apply and Close.

## Step 5: Share the Formatter with Your Team (Optional)
To ensure consistent formatting across the team, distribute the eclipse-js-formatter.xml file and share this guide. Team members can follow these steps to configure their own Eclipse IDEs.

## Troubleshooting
The formatter profile does not appear after import: Ensure you selected the correct .xml file and that your Eclipse installation includes JavaScript Development Tools (JSDT).
Formatting is not applied: Verify that the imported profile is selected and enabled in the Formatter settings.
