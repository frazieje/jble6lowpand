# jble6lowpand
Bluetooth low energy 6lowpan connector daemon.
## The Basics
The application implements functionality to scan for and connect to bluetooth low-energy devices advertising the Internet Protocol Support Profile (IPSP).

This application uses jni for communication with the bluetooth hardware. It uses the bluetooth protocol stack for linux (bluez) and the bluetooth_6lowpan kernel module to maintain a 6lowpan over bluetooth low-energy. For this reason the daemon is currently only supported on linux.

The application exposes a REST to allow for communication and control by other java processes, such as a command line interface or an application server.

The application uses the gradle application plugin for ease of installation / distribution.

### Whitelist
To determine which devices to connect, the application uses a file-based whitelist. The location of the whitelist is configurable.

## Using the daemon
### Prerequesites
Before the application can be used, you need a linux machine with access to bluetooth hardware, and with support for the bluetooth_6lowpan kernel module.
### Getting started

Install bluez:
``` bash
$ sudo apt-get install bluez
```

Enable the kernel module:
``` bash
$ sudo modprobe bluetooth_6lowpan
```

Clone the repository and use gradle to build the application: 
``` bash
$ cd ~
$ git clone git@github.com:frazieje/jble6lowpand.git
$ cd jble6lowpand
$ gradle build
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
$ echo "whitelistPath=/opt/jble6lowpand/knowndevices.conf" > "jble6lowpand.conf"
$ echo "controllerPort=8080" >> "jble6lowpand.conf"
```

Run the application:
``` bash
$ cd /opt/jble6lowpand
$ bin/jble6lowpand -configFile /opt/jble6lowpand/jble6lowpand.conf
```

Check the application status:
``` bash
$ curl localhost:8080
```

Add a bluetooth device to the whitelist:
``` bash
$ curl -H "Content-Type: application/json" \
  -X POST \
  -d '{"name":"DEVICE1","data":"AKq7zN3u"}' \
  localhost:8080/00-AA-BB-CC-DD-EE
```

Remove a bluetooth device from the whitelist:
``` bash
$ curl -X DELETE \
  localhost:8080/00-AA-BB-CC-DD-EE
```



