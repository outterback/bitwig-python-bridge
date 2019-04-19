# bitwig-python-bridge

Code to bridge Bitwig Studio 2.5 and Python via a controller extension.

Access the Bitwig Studio Java API from a Python console. Contains code for the Bitwig Controller Extension in Python Bridge/, code for interactivity with python in remote_py/, and a pre-compiled .bwextension file [here](./Python Bridge/target/).

[Video demo on Youtube](https://youtu.be/kZjMfeKIVQY)

## Dependencies
Versions stated are the versions I've used. Others may or may not work.

### Python
Install these by `pip install -r remote_py/requirements.txt`

* Py4j 0.10.8.1
* Keyboard 0.13.3

### Java
* Py4j 0.10.8.1

## Why?

This is a fun thing to play with as it enables you to automate things you do can do in the Bitwig Controller Java API, without a controller. It enables you to interact with the API via a REPL. It enables you to create macros and bind them to hotkeys. You could write your own midi note editor GUI, and use that instead of Bitwigs. You can enable Clyphx like scripting, and match certain specific functions to things you name appropriately in Bitwig Studio. 

## How?

This is basically an extremely basic use case of [Py4j](https://www.py4j.org/) applied to the Bitwig Studio Java API. The controller extension just defines a few member variables in the controller script, pointing to interesting objects in Bitwig Studio's API, and then exposes them to Python via the Py4j Bridge. Check the file PythonBridgeExtension.java, it is unnecessarily simple.

## Usage

a) Use the pre-baked extension, located [here](./Python Bridge/target/PythonBridge.bwextension)
b) Compile the plugin yourself by running `mvn install` in the Python Bridge folder.

Copy the PythonBridge.bwextension file to your Bitwig Studio Extensions folder. Add it as a controller script, and enable it. Since it doesn't have any midi ports, it will require you to manually activate it via the power switch icon next to the script name in the list of controllers.

Two example scripts are provided, they are identical except that one displays how to enable OS global keybindings to scripts done in Python.
Run remote_py/example.py in an interactive console, e.g. IPython via the %run magic command. If you wish to try the example with keyboard bindings, you will need to run the console with sudo on Linux. Details as to why are found [here](https://pypi.org/project/keyboard/).

## Troubleshooting & Known Bugs

* If you are editing the Java code, recompiling it, and reinitializing it in Java, the gateway server will close. The connection from a running python console will be lost. Just rerun the Python code and it'll be back up*.

* There are some sleep() commands sprinkled in the python code. Sometimes, Bitwig doesn't act until we've asked it to do the next thing. Try removing them and seeing the results. I haven't investigated a different way to mitigate this.
