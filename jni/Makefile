PROJECT_NAME := ble6lowpan

export OUTPUT_FILENAME
#MAKEFILE_NAME := $(CURDIR)/$(word $(words $(MAKEFILE_LIST)),$(MAKEFILE_LIST))
MAKEFILE_NAME := $(MAKEFILE_LIST)
MAKEFILE_DIR := $(dir $(MAKEFILE_NAME) ) 

GNU_INSTALL_ROOT := /usr

JAVA_INCLUDE_PATH := "$(JAVA_HOME)/include"
JAVA_LINUX_INCLUDE_PATH := "$(JAVA_HOME)/include/linux"

MK := mkdir
RM := rm -rf

#echo suspend
ifeq ("$(VERBOSE)","1")
NO_ECHO := 
else
NO_ECHO := @
endif

# Toolchain commands
CC              := '$(GNU_INSTALL_ROOT)/bin/gcc'
AS              := '$(GNU_INSTALL_ROOT)/bin/as'
AR              := '$(GNU_INSTALL_ROOT)/bin/ar' -r
LD              := '$(GNU_INSTALL_ROOT)/bin/ld'
NM              := '$(GNU_INSTALL_ROOT)/bin/nm'
OBJDUMP         := '$(GNU_INSTALL_ROOT)/bin/objdump'
OBJCOPY         := '$(GNU_INSTALL_ROOT)/bin/objcopy'
SIZE            := '$(GNU_INSTALL_ROOT)/bin/size'

C_SOURCE_FILES += \
$(abspath external/bluez/lib/bluetooth.c) \
$(abspath external/bluez/lib/hci.c) \
$(abspath external/bluez/src/shared/mainloop.c) \
$(abspath src/ble6lowpan.c) \

INC_PATHS  = -I$(JAVA_INCLUDE_PATH)
INC_PATHS += -I$(JAVA_LINUX_INCLUDE_PATH)
INC_PATHS += -I$(abspath external/bluez/lib)
INC_PATHS += -I$(abspath external/bluez/src/shared)

all:
	$(CC) -o $(abspath libs/libble6lowpan.so) -lc -shared -fPIC $(INC_PATHS) $(C_SOURCE_FILES)