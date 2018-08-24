# jble6lowpand
Bluetooth low energy 6lowpan connector daemon.
## The Basics
The application implements functionality to scan for and connect to bluetooth low-energy devices advertising the Internet Protocol Support Profile (IPSP).

This application uses jni for communication with the bluetooth hardware. It uses the bluetooth protocol stack for linux (bluez) and the bluetooth_6lowpan kernel module to maintain a 6lowpan over bluetooth low-energy. For this reason the daemon is currently only supported on linux.

The application exposes a REST API to allow for communication and control.

The application uses the gradle application plugin for ease of installation / distribution.

### Whitelist
To determine which devices to connect, the application uses a file-based whitelist. The location of the whitelist is configurable.

## Getting Started
### Prerequesites
Before the application can be used, you need a linux machine with access to bluetooth hardware, and with support for the bluetooth_6lowpan kernel module.

[Provision a Raspberry Pi for use with jble6lowpand](https://github.com/frazieje/jble6lowpand/wiki/Provisioning-Raspberry-Pi-for-use-with-jble6lowpand)

Or to just enable the kernel module on a linux install:
``` bash
$ sudo modprobe bluetooth_6lowpan
```

### Getting Started Using Docker
You can use docker (and docker-compose if necessary) to easily build and run this application.

Clone the repository
``` bash
$ cd ~
$ git clone git@github.com:frazieje/jble6lowpand.git
```

Initialize the bluez submodule
``` bash
$ cd ~/jble6lowpand
$ git submodule update --init --recursive
```

Build docker image
``` bash
$ cd ~/jble6lowpand
$ sudo docker-compose build
```

Run the application:
``` bash
$ cd ~/jble6lowpand
$ sudo docker-compose up
```

### Getting Started Running Standalone (without Docker)
To run the application in standalone you will need to install some prerequisites and build the native components

Install gcc and bluez:
``` bash
$ sudo apt-get install gcc bluez
```

Clone the repository: 
``` bash
$ cd ~
$ git clone git@github.com:frazieje/jble6lowpand.git
```

Initialize the bluez submodule
``` bash
$ cd ~/jble6lowpand
$ git submodule update --init --recursive
```

Build the native components:
``` bash
$ cd ~/jble6lowpand/jni
$ make
```

Build the application using gradle:
``` bash
$ cd ~/jble6lowpand
$ ./gradlew build
```

Install the application:
``` bash
$ cp build/distributions/jble6lowpand.tar /opt/
$ cd /opt
$ tar -xvf jble6lowpand.tar
```

Configure the application:
``` bash
$ cd /opt/jble6lowpand
$ ln /sys/kernel/debug/bluetooth/6lowpan_control bin/6lowpan_control
$ echo "whitelistPath=/opt/jble6lowpand/knowndevices.conf" > "jble6lowpand.conf"
$ echo "controllerPort=8080" >> "jble6lowpand.conf"
```

Run the application:
``` bash
$ cd /opt/jble6lowpand
$ bin/jble6lowpand -configFile /opt/jble6lowpand/jble6lowpand.conf
```

## Using the Application

Check the application status:
``` bash
$ curl localhost:8080
```

Add a bluetooth device to the whitelist:
``` bash
$ curl -H "Content-Type: application/json" \
  -X POST \
  -d '{"name":"DEVICE1"}' \
  localhost:8080/00-AA-BB-CC-DD-EE
```

Remove a bluetooth device from the whitelist:
``` bash
$ curl -X DELETE \
  localhost:8080/00-AA-BB-CC-DD-EE
```



