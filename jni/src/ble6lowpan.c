#include <stdio.h>
#include <stdbool.h>
#include <unistd.h>
#include <errno.h>
#include <fcntl.h>
#include <poll.h>
#include <signal.h>
#include <sys/ioctl.h>
#include <time.h>

#include "bluetooth.h"
#include "hci.h"
#include "hci_lib.h"

#include "util.h"

#include "com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService.h"

#define MAX_BLE_CONN              8
#define IPSP_UUID                 0x1820 /* IPSP service UUID */

#define EIR_FLAGS                 0x01  /* flags */
#define EIR_UUID16_SOME           0x02  /* 16-bit UUID, more available */
#define EIR_UUID16_ALL            0x03  /* 16-bit UUID, all listed */
#define EIR_UUID32_SOME           0x04  /* 32-bit UUID, more available */
#define EIR_UUID32_ALL            0x05  /* 32-bit UUID, all listed */
#define EIR_UUID128_SOME          0x06  /* 128-bit UUID, more available */
#define EIR_UUID128_ALL           0x07  /* 128-bit UUID, all listed */
#define EIR_NAME_SHORT            0x08  /* shortened local name */
#define EIR_NAME_COMPLETE         0x09  /* complete local name */
#define EIR_TX_POWER              0x0A  /* transmit power level */
#define EIR_DEVICE_ID             0x10  /* device ID */
#define EIR_MANUF_SPECIFIC_DATA   0xFF  /* manufacture specific data */

#define DEVICE_NAME_LEN           30
#define DEVICE_ADDR_LEN           18

#define CONTROLLER_PATH           "/opt/jble6lowpand/bin/6lowpan_control"

#define ERR_SET_SCAN_PARAMETER_FAILED       -1
#define ERR_ENABLE_SCAN_FAILED              -2
#define ERR_DISABLE_SCAN_FAILED             -4
#define ERR_OPENING_HCI_DEV                 -8
#define ERR_POLLING_HCI_DEV                 -16
#define ERR_RETRIEVING_SOCKET_OPTIONS       -32
#define ERR_COULD_NOT_FIND_HCI_DEV          -64

static volatile int signal_received;

static void sigint_handler(int sig)
{
	signal_received = sig;

	switch (sig) {
	case SIGINT:
	case SIGTERM:
		break;
	}
}

static bool parse_ip_service(uint8_t *eir, size_t eir_len, char *buf, size_t buf_len)
{
	size_t offset = 0;
	bool ipsp_service = false;
	uint8_t val[256];

	memset(val, 0, 256);

	while (offset < eir_len) {
		size_t field_len = eir[0];

		switch (eir[1]) {
		case EIR_UUID16_SOME:
		case EIR_UUID16_ALL:
			put_le16(IPSP_UUID, &val[0]);
			if (!memcmp(val, eir + 2, field_len/2))
				ipsp_service = true;
			break;
		case EIR_NAME_SHORT:
		case EIR_NAME_COMPLETE:
			if (field_len - 1 > buf_len)
				break;
			memcpy(buf, &eir[2], field_len - 1);
			break;
		case EIR_MANUF_SPECIFIC_DATA:
			break;
		}

		offset += field_len + 1;
		eir += field_len + 1;
	}

	return ipsp_service;
}

static int scan_ipsp_device(int dev_id, int timeout, char addresses[][DEVICE_ADDR_LEN], char names[][DEVICE_NAME_LEN]) {

	uint8_t own_type = LE_PUBLIC_ADDRESS;
	uint8_t scan_type = 0x01; /* Active scanning. */
	uint8_t filter_policy = 0x00;
	uint16_t interval = htobs(0x0010);
	uint16_t window = htobs(0x0004);
	uint8_t filter_dup = 0x01;

	unsigned char buf[HCI_MAX_EVENT_SIZE], *ptr;
	struct hci_filter nf, of;
	struct sigaction sa;
	socklen_t olen;
	struct pollfd pollfd;
	int poll_ret;
	bool scan_ret = false;
	int err;
	time_t start_time;
	time_t curr_time;
	double running_time;

	int client_i = 0;

	int err_code = 0;

	start_time = time(NULL);

    int dd = hci_open_dev(dev_id);

    printf("scanning .. dd = %d", dd);

    if (dd < 0) {
        perror("Could not open hci device");
        err_code = ERR_OPENING_HCI_DEV;
    } else {

        err = hci_le_set_scan_parameters(dd, scan_type, interval, window, own_type, filter_policy, 10000);
        if (err < 0) {
            perror("Set scan parameters failed");
            err_code = ERR_SET_SCAN_PARAMETER_FAILED;
        } else {

            err = hci_le_set_scan_enable(dd, 0x01, filter_dup, 10000);
            if (err < 0) {
                perror("Enable scan failed");
                err_code = ERR_ENABLE_SCAN_FAILED;
            } else {

                olen = sizeof(of);
                if (getsockopt(dd, SOL_HCI, HCI_FILTER, &of, &olen) < 0) {
                    printf("Could not get socket options\n");
                    err_code = ERR_RETRIEVING_SOCKET_OPTIONS;
                } else {
                    hci_filter_clear(&nf);
                    hci_filter_set_ptype(HCI_EVENT_PKT, &nf);
                    hci_filter_set_event(EVT_LE_META_EVENT, &nf);

                    if (setsockopt(dd, SOL_HCI, HCI_FILTER, &nf, sizeof(nf)) < 0) {
                        printf("Could not set socket options\n");
                    }

                    memset(&sa, 0, sizeof(sa));
                    sa.sa_flags = SA_NOCLDSTOP;
                    sa.sa_handler = sigint_handler;
                    sigaction(SIGINT, &sa, NULL);

                    while (timeout > 0 && client_i < MAX_BLE_CONN) {
                        evt_le_meta_event *meta;
                        le_advertising_info *info;
                        char addr[DEVICE_ADDR_LEN];
                        char name[DEVICE_NAME_LEN];

                        curr_time = time(NULL);
                        running_time = difftime(curr_time, start_time);

                        if (running_time > timeout)
                            break;

                        memset(name, 0, sizeof(name));
                        memset(addr, 0, sizeof(addr));
                        memset(buf, 0, sizeof(buf));

                        pollfd.fd = dd;
                        pollfd.events = POLLIN;

                        poll_ret = poll(&pollfd, 1, (timeout - running_time)*1000);
                        if (poll_ret < 0) {
                            printf("poll hci dev error\n");
                            break;
                        } else if (poll_ret == 0) {
                            /* poll timeout */
                            break;
                        } else {
                            if (pollfd.revents & POLLIN) {
                                while ((read(dd, buf, sizeof(buf))) < 0) {
                                    if (errno == EINTR && signal_received == SIGINT)
                                        timeout = -1; break;

                                    if (errno == EAGAIN || errno == EINTR)
                                        continue;

                                    timeout = -1; break;
                                }
                                if (timeout < 0)
                                    break;
                            }
                        }

                        ptr = buf + (1 + HCI_EVENT_HDR_SIZE);
                        meta = (void *) ptr;

                        if (meta->subevent != EVT_LE_ADVERTISING_REPORT)
                            continue;

                        info = (le_advertising_info *) (meta->data + 1);

                        ba2str(&info->bdaddr, addr);
                        if (parse_ip_service(info->data, info->length, name, sizeof(name) - 1)) {
                            memcpy(names[client_i], name, sizeof(name));
                            memcpy(addresses[client_i], addr, sizeof(addr));
                            client_i++;
                        }
                    }
                }

                setsockopt(dd, SOL_HCI, HCI_FILTER, &of, sizeof(of));

                err = hci_le_set_scan_enable(dd, 0x00, filter_dup, 10000);
                if (err < 0) {
                    perror("Disable scan failed");
                    err_code = ERR_DISABLE_SCAN_FAILED;
                }

            }

        }

        if (dd >= 0) {
            hci_close_dev(dd);
        }

    }

	if (err_code < 0) {
	    printf("scanning returning = error");
	    return err_code;
	}

    printf("scanning returning = success");
	return client_i;
}

static bool connect_device(char *addr, bool connect)
{
	int fd;
	bool ret = false;
	char command[64];

	fd = open(CONTROLLER_PATH, O_WRONLY);
	if (fd < 0) {
		perror("Can not open 6lowpan controller\n");
		return ret;
	}

	if (connect)
		snprintf(command, sizeof(command), "connect %s 1", addr);
	else
		snprintf(command, sizeof(command), "disconnect %s 0", addr);

	if (write(fd, command, sizeof(command)) > 0)
		ret = true;

	close(fd);

	return ret;
}

static int get_ipsp_connections(char addresses[][DEVICE_ADDR_LEN]) {
	/* Format "00:11:22:33:44:55 (type 1) */
	char buffer[27*MAX_BLE_CONN];
	char *pch;
	int fd;
	int i = 0;

	memset(buffer, 0, sizeof(buffer));
	fd = open(CONTROLLER_PATH, O_RDONLY);
	if (fd < 0) {
		perror("Can not open 6lowpan controller");
		return 0;
	}

	if (read(fd, buffer, sizeof(buffer)) > 0) {
		pch = strtok(buffer, " \n");
		while (pch != NULL) {
			if (strlen(pch) == 17) {
				strcpy(addresses[i], pch);
				i++;
			}
			pch = strtok (NULL, " \n");
		}
	}
	close(fd);
	return i;
}

static bool reset_device() {

    int ctl;

  	if ((ctl = socket(AF_BLUETOOTH, SOCK_RAW, BTPROTO_HCI)) < 0) {
  		perror("Can't open HCI socket.");
  		return false;
    }

    bool ret = true;

    int dev_id = hci_get_route(NULL);

	if (dev_id < 0) {
		perror("Could not find hci device");
		ret = false;
	}

	if (ioctl(ctl, HCIDEVDOWN, dev_id) < 0) {
		perror("Could not down hci device");
		ret = false;
    }

	if (ioctl(ctl, HCIDEVUP, dev_id) < 0) {
		if (errno == EALREADY) {
		    ret = false;
		} else {
		    perror("Could not initialize hci device");
		    ret = false;
		}
    }

    close(ctl);

    return ret;
}

JNIEXPORT jobjectArray JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_scanDevicesInternal(JNIEnv * env, jobject thisObj, jint timeout) {
    jobjectArray ret;
    int i;
    char addresses[MAX_BLE_CONN][DEVICE_ADDR_LEN];
    char names[MAX_BLE_CONN][DEVICE_NAME_LEN];

	int dev_id = hci_get_route(NULL);

	int num = 0;

    if (dev_id < 0) {
        perror("Could not find hci device");
        num = ERR_COULD_NOT_FIND_HCI_DEV;
    }

    if (num == 0) {
        num = scan_ipsp_device(dev_id, timeout, addresses, names);
    }

    if (num >= 0) {

        jclass cls = (*env)->FindClass(env, "com/spoohapps/farcommon/model/EUI48Address");
        ret = (jobjectArray)(*env)->NewObjectArray(env, num, cls, NULL);
        jmethodID constructor = (*env)->GetMethodID(env, cls, "<init>", "(Ljava/lang/String;Ljava/lang/String;)V");

        for (i = 0; i < num; i++) {
            jobject object = (*env)->NewObject(env, cls, constructor, (*env)->NewStringUTF(env, addresses[i]), (*env)->NewStringUTF(env, names[i]));
            (*env)->SetObjectArrayElement(env, ret, i, object);
        }

    } else {

        jclass exception = (*env)->FindClass(env, "com/spoohapps/jble6lowpand/NativeBle6LowpanIpspException");
        jmethodID constructor = (*env)->GetMethodID(env, exception, "<init>", "(I)V");
        (*env)->Throw(env, exception);

    }
    return ret;
}

JNIEXPORT jboolean JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_connectDevice(JNIEnv *env, jobject thisObj, jobject address) {
    jclass cls = (*env)->GetObjectClass(env, address);
    jmethodID getAddress = (*env)->GetMethodID(env, cls, "getAddress", "()Ljava/lang/String;");
    jstring jaddrstr = (jstring)(*env)->CallObjectMethod(env, address, getAddress);
	const char *addr = (*env)->GetStringUTFChars(env, jaddrstr, 0);
	char connect_to[DEVICE_ADDR_LEN];
	strcpy(connect_to, addr);
	(*env)->ReleaseStringUTFChars(env, jaddrstr, addr);
	bool result = connect_device(connect_to, true);
	return result;
}

JNIEXPORT jboolean JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_disconnectDevice(JNIEnv *env, jobject thisObj, jobject address) {
    jclass cls = (*env)->GetObjectClass(env, address);
    jmethodID getAddress = (*env)->GetMethodID(env, cls, "getAddress", "()Ljava/lang/String;");
    jstring jaddrstr = (jstring)(*env)->CallObjectMethod(env, address, getAddress);
	const char *addr = (*env)->GetStringUTFChars(env, jaddrstr, 0);
	char disconnect_from[DEVICE_ADDR_LEN];
	strcpy(disconnect_from, addr);
	(*env)->ReleaseStringUTFChars(env, jaddrstr, addr);
	bool result = connect_device(disconnect_from, false);
	return result;
}

JNIEXPORT jboolean JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_initializeDevice(JNIEnv *env, jobject thisObj) {
	bool result = reset_device();
	return result;
}

JNIEXPORT jobjectArray JNICALL Java_com_spoohapps_jble6lowpand_NativeBle6LowpanIpspService_getConnectedDevices(JNIEnv *env, jobject thisObj) {
    jobjectArray ret;
    int i;
    char addresses[MAX_BLE_CONN][DEVICE_ADDR_LEN];
    int num = get_ipsp_connections(addresses);
    jclass cls = (*env)->FindClass(env, "com/spoohapps/farcommon/model/EUI48Address");
    ret = (jobjectArray)(*env)->NewObjectArray(env, num, cls, NULL);
    jmethodID constructor = (*env)->GetMethodID(env, cls, "<init>", "(Ljava/lang/String;)V");
    for (i = 0; i < num; i++) {
        jobject object = (*env)->NewObject(env, cls, constructor, (*env)->NewStringUTF(env, addresses[i]));
    	(*env)->SetObjectArrayElement(env, ret, i, object);
    }
    return ret;
}