package com.spoohapps.jble6lowpand;

public class NativeBle6LowpanIpspException extends DeviceServiceException {

    private static final int ERR_SET_SCAN_PARAMETER_FAILED = -1;
    private static final int ERR_ENABLE_SCAN_FAILED = -2;
    private static final int ERR_DISABLE_SCAN_FAILED = -4;
    private static final int ERR_OPENING_HCI_DEV = -8;
    private static final int ERR_POLLING_HCI_DEV = -16;
    private static final int ERR_RETRIEVING_SOCKET_OPTIONS = -32;
    private static final int ERR_COULD_NOT_FIND_HCI_DEV = -64;

    public NativeBle6LowpanIpspException(int errorCode) {
        super(getMessageForCode(errorCode));
    }

    private static String getMessageForCode(int errorCode) {

        switch(errorCode) {

            case ERR_SET_SCAN_PARAMETER_FAILED:
                return "Error: setting scan parameters";
            case ERR_ENABLE_SCAN_FAILED:
                return "Error: enabling le scan";
            case ERR_DISABLE_SCAN_FAILED:
                return "Error: disabling le scan";
            case ERR_OPENING_HCI_DEV:
                return "Error: opening hci device";
            case ERR_POLLING_HCI_DEV:
                return "Error: polling hci device";
            case ERR_RETRIEVING_SOCKET_OPTIONS:
                return "Error: retrieving socket options";
            case ERR_COULD_NOT_FIND_HCI_DEV:
                return "Error: could not find hci device";
            default:
                return "Error: unknwon error";

        }

    }

}
