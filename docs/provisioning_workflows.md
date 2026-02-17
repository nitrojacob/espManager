[<- Back to Index](./index.md)

# Provisioning Workflows

The library supports three primary workflows for provisioning a device: SoftAP, BLE, and QR Code scanning. This guide provides a detailed walkthrough of each.

## 1. SoftAP Provisioning

In SoftAP mode, the ESP device creates its own Wi-Fi network (a software-enabled access point). The Android device connects to this temporary network to send the final Wi-Fi credentials.

### Step-by-Step Guide:

1.  **Scan for the Device's Network**: Use the `WiFiManager` to find the device's SoftAP network. This is typically identified by a specific prefix (e.g., `"PROV_"`).

2.  **Create an `ESPDevice`**: Once you have the SSID of the device's network, create an `ESPDevice` instance configured for SoftAP.

    ```java
    ESPProvisionManager provisionManager = ESPProvisionManager.getInstance(getApplicationContext());
    ESPDevice espDevice = provisionManager.createESPDevice(
        ESPConstants.TransportType.TRANSPORT_SOFTAP, 
        ESPConstants.SecurityType.SECURITY_1
    );
    ```

3.  **Connect to the Device**: Use the `connectWiFiDevice()` method to connect the Android phone to the device's SoftAP network. The library handles the complexities of switching Wi-Fi networks.

    ```java
    String deviceSsid = "PROV_MyESPDevice";
    String devicePassword = "my-softap-password"; // The password for the device's SoftAP, if any
    espDevice.connectWiFiDevice(deviceSsid, devicePassword);
    // Listen for DeviceConnectionEvent on the EventBus to know when the connection is established.
    ```

4.  **Provision the Device**: After a successful connection (`EVENT_DEVICE_CONNECTED`), you can send the home Wi-Fi credentials to the ESP device.

    ```java
    String homeNetworkSsid = "MyHomeWiFi";
    String homeNetworkPassword = "super-secret-password";

    espDevice.provision(homeNetworkSsid, homeNetworkPassword, new ProvisionListener() {
        @Override
        public void deviceProvisioningSuccess() {
            Log.d(TAG, "Device has successfully connected to the home network!");
        }

        @Override
        public void onProvisioningFailed(Exception e) {
            Log.e(TAG, "Provisioning failed", e);
        }
        // ... other callbacks for detailed status
    });
    ```

## 2. BLE Provisioning

In BLE mode, the Android device communicates with the ESP device over Bluetooth Low Energy. This method does not require changing Wi-Fi networks.

### Step-by-Step Guide:

1.  **Scan for BLE Devices**: Use `ESPProvisionManager` to scan for nearby devices. You can filter by a name prefix.

    ```java
    provisionManager.searchBleEspDevices("PROV_", new BleScanListener() {
        @Override
        public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
            // Device found, store the BluetoothDevice object
            Log.d(TAG, "Found device: " + device.getName());
            // You can now stop scanning and proceed to connect
        }
        // ... other scan callbacks
    });
    ```

2.  **Create an `ESPDevice`**: Once you have the `BluetoothDevice` object, create an `ESPDevice` instance for BLE.

    ```java
    ESPDevice espDevice = provisionManager.createESPDevice(
        ESPConstants.TransportType.TRANSPORT_BLE, 
        ESPConstants.SecurityType.SECURITY_1
    );
    ```

3.  **Connect to the Device**: Initiate the BLE connection. You will also need the primary service UUID from the scan result.

    ```java
    // Assumes 'foundDevice' is the BluetoothDevice from the scan
    // and 'serviceUuid' is the service UUID from the ScanResult
    espDevice.connectBLEDevice(foundDevice, serviceUuid);
    // Listen for DeviceConnectionEvent on the EventBus.
    ```

4.  **Provision the Device**: Once connected, the process is identical to SoftAP provisioning.

    ```java
    espDevice.provision(homeNetworkSsid, homeNetworkPassword, provisionListener);
    ```

## 3. QR Code Provisioning

This is the most user-friendly method. The user scans a QR code that contains all the necessary information to find and connect to the device.

### QR Code JSON Structure

The QR code must contain a JSON payload with the following structure:

```json
{
  "name": "PROV_MyESPDevice",
  "pop": "my-secret-pop",
  "transport": "softap",
  "security": 1
}
```

- **`name`**: The device name (SSID for SoftAP, BLE name for BLE).
- **`pop`**: The Proof of Possession (required for Security 1 and 2).
- **`transport`**: The transport type (`softap` or `ble`).

### Step-by-Step Guide

1.  **Initiate QR Code Scan**: Call `scanQRCode()` from the `ESPProvisionManager`. This method takes care of launching the camera and processing the result.

    ```java
    // 'cameraPreview' is a PreviewView in your layout
    provisionManager.scanQRCode(cameraPreview, this, new QRCodeScanListener() {
        @Override
        public void qrCodeScanned() {
            Log.d(TAG, "QR code scanned, attempting to find device...");
        }

        @Override
        public void deviceDetected(ESPDevice device) {
            Log.d(TAG, "Device detected! Ready to connect.");
            // The library has found the device and created the espDevice object for you.
            // You can now connect and provision.
            espDevice = device;
            espDevice.connectToDevice();
        }

        @Override
        public void onFailure(Exception e) {
            Log.e(TAG, "QR code provisioning failed", e);
        }
        // ... other callbacks
    });
    ```

2.  **Connect and Provision**: The `deviceDetected()` callback provides you with a fully configured `ESPDevice` object. You can then proceed to connect and provision it as you would in the other workflows.
