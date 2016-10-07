# IBM UrbanCode DB2 Tools Plug-in
---
Note: This is not the plug-in distributable! This is the source code. At this time, the plug-in must be built locally.

### License
This plug-in is protected under the [Eclipse Public 1.0 License](http://www.eclipse.org/legal/epl-v10.html)

### Compatibility
	This plug-in requires version 6.1.1 or later of IBM UrbanCode Deploy.

### Installation
	No special steps are required for installation.
	See Installing plug-ins in IBM UrbanCode Deploy. Download this zip file if you wish to skip the
	manual build step. Otherwise, download the entire IBM-DB2-Tools-UCD and
	run the "ant" command in the top level folder. This should compile the code and create
	a new distributable zip within the releases folder. Use this command if you wish to make
	your own changes to the plugin.

### History
    Version 13
        Community GitHub Release
### Steps  
    - Run DB2 Command: Execute a DB2 script
    - Run DB2 Script: Execute a DB2 command

### Build

A few build dependencies are not publicly available at the plug-in's initial GitHub release. Please find the following dependencies in your local
instance of IBM UrbanCode Deploy and place them in the ./lib directory.
    - CommonsUtils.jar
    - NativeProcess.jar
    - shell.jar
    - WinAPI.jar
    - 'native' folder -> This contains the WinAPI.dill files. Copy this entire folder into the lib directory.

Once copied over, run the build command as specified below...

### How to build the plug-in from eclipse client:

1. Expand the Groovy project that you checked-out from example template.
2. Open build.xml file and execute it as an Ant Build operation (Run As -> Ant Build)
3. The built plug-in is located at releases/IBM-DB2-Tools-UCD-vdev.zip

### How to build the plug-in from command line:

1. Navigate to the base folder of the project through command line.
2. Make sure that there is build.xml file there, and then execute 'ant' command.
3. The built plug-in is located at releases/IBM-DB2-Tools-UCD-vdev.zip
