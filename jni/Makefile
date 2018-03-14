PROJECT_NAME := ble6lowpand

export OUTPUT_FILENAME
#MAKEFILE_NAME := $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))
MAKEFILE_NAME := $(MAKEFILE_LIST)
MAKEFILE_DIR := $(dir $(MAKEFILE_NAME) ) 

GNU_INSTALL_ROOT := E:/SysGCC/Raspberry
GNU_VERSION := 4.9.2
GNU_PREFIX := arm-linux-gnueabihf

JAVA_INCLUDE_PATH := "E:/SysGCC/Raspberry/arm-linux-gnueabihf/sysroot/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include"
JAVA_LINUX_INCLUDE_PATH := "E:/SysGCC/Raspberry/arm-linux-gnueabihf/sysroot/usr/lib/jvm/jdk-8-oracle-arm32-vfp-hflt/include/linux"

MK := mkdir
RM := rm -rf

#echo suspend
ifeq ("$(VERBOSE)","1")
NO_ECHO := 
else
NO_ECHO := @
endif

# Toolchain commands
CC              := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-gcc'
AS              := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-as'
AR              := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-ar' -r
LD              := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-ld'
NM              := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-nm'
OBJDUMP         := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-objdump'
OBJCOPY         := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-objcopy'
SIZE            := '$(GNU_INSTALL_ROOT)/bin/$(GNU_PREFIX)-size'

C_SOURCE_FILES += \
$(abspath external/bluez/lib/bluetooth.c) \
$(abspath external/bluez/lib/hci.c) \
$(abspath external/bluez/src/shared/mainloop.c) \
$(abspath src/ble6lowpand.c) \

INC_PATHS  = -I$(JAVA_INCLUDE_PATH)
INC_PATHS += -I$(JAVA_LINUX_INCLUDE_PATH)
INC_PATHS += -I$(abspath external/bluez/lib)
INC_PATHS += -I$(abspath external/bluez/src/shared)

all:
	$(CC) -o libs/libble6lowpand.so -lc -shared -fPIC $(INC_PATHS) $(C_SOURCE_FILES) 