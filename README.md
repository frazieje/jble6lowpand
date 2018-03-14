# jble6lowpand
Bluetooth low energy 6lowpan connector daemon.
## The Basics
The project implements an Apache Commons daemon to scan for and connect to bluetooth low-energy devices advertising the Internet Protocol Support Profile (IPSP).

This project uses jni for communication with the bluetooth hardware. It uses the bluetooth protocol stack for linux (bluez) and the bluetooth_6lowpan kernel module to maintain a 6lowpan over bluetooth low-energy. For this reason the daemon is currently only supported on linux.

The daemon exposes RMI methods to allow for communication and control by other java processes, such as a command line interface or an application server.

### Whitelist
To determine which devices to connect, the daemon uses a file-based whitelist. The location of the whitelist is configurable.
## Using the daemon
### Prerequesites
Before the daemon can be used, you need a linux machine with access to bluetooth hardware, and with support for the bluetooth_6lowpan kernel module.
### Getting started
Clone this repository to /usr/local/jble6lowpand

After cloning the repository, use gradle to build the daemon: 
``` bash
$ gradle build
```

Install bluez and jsvc:
``` bash
$ sudo apt-get install bluez
$ sudo apt-get install jsvc
```

Enable the kernel module:
``` bash
$ sudo modprobe bluetooth_6lowpan
```

Start and use the daemon:
``` bash
# start the daemon
$ jble6lowpand start

# stop the daemon
$ jble6lowpand stop

# restart the daemon
$ jble6lowpand restart

# list connected devices
$ jble6lowpand list
```

