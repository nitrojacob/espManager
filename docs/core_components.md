[<- Back to Index](./index.md)

# Core Components

## 1. `ESPProvisionManager`

`ESPProvisionManager` is the singleton entry point for the entire provisioning process. Its primary role is to discover ESP devices in the vicinity and to create an `ESPDevice` instance that you can then interact with.

### Key Responsibilities:

- **Device Discovery**: Provides methods to scan for both BLE and SoftAP devices. You can scan for all devices or filter by a specific name prefix.
- **`ESPDevice` Instantiation**: Acts as a factory to create `ESPDevice` objects, configured for a specific transport (`TRANSPORT_BLE` or `TRANSPORT_SOFTAP`) and security type.
- **QR Code Scanning**: Offers a high-level API to handle QR code scanning using the device's camera, which simplifies the initial connection process for the end-user.

### Usage Examples:

#### Getting an Instance

Since `ESPProvisionManager` is a singleton, you get an instance by calling `getInstance()`.

```java
Context appContext = getApplicationContext();
ESPProvisionManager provisionManager = ESPProvisionManager.getInstance(appContext);
```

#### Creating an `ESPDevice`

Before you can communicate with a device, you need to create an `ESPDevice` object. You must specify the transport and security type you intend to use.

```java
// For a device that will be provisioned over SoftAP with Security Level 1
ESPDevice espDevice = provisionManager.createESPDevice(
    ESPConstants.TransportType.TRANSPORT_SOFTAP, 
    ESPConstants.SecurityType.SECURITY_1
);
```

## 2. `ESPDevice`

`ESPDevice` represents a single, specific ESP device that your application is interacting with. This class handles the actual communication with the device, including connecting, establishing a secure session, and sending provisioning or custom data.

### Key Responsibilities:

- **Device Connection**: Manages the low-level details of connecting to the device, whether through BLE (`connectBLEDevice()`) or Wi-Fi (`connectWiFiDevice()`).
- **Session Management**: Establishes a secure, encrypted session with the device based on the configured security level. This is handled automatically when you attempt to send data.
- **Provisioning**: Provides the `provision()` method to send network credentials (Wi-Fi or Thread) to the device.
- **Network Scanning**: Allows you to request a scan for nearby Wi-Fi or Thread networks from the perspective of the ESP device (`scanNetworks()`).
- **Custom Data Exchange**: Features the `sendDataToCustomEndPoint()` method, which allows for sending and receiving arbitrary data to and from custom endpoints defined in your device's firmware.

### Usage Example:

This example shows the basic lifecycle of interacting with an `ESPDevice`.

```java
// Assume 'espDevice' is an ESPDevice object created by the ESPProvisionManager

// 1. Set the Proof of Possession (if required for Security 1 or 2)
espDevice.setProofOfPossession("my-secret-pop");

// 2. Connect to the device (this example is for SoftAP)
// The deviceConnectionEvent will be sent over the EventBus
espDevice.connectToDevice();

// 3. Once connected, you can provision the device.
// You will get feedback via the ProvisionListener.
espDevice.provision("MyHomeWiFi", "WiFiPassword", new ProvisionListener() {
    @Override
    public void deviceProvisioningSuccess() {
        Log.d(TAG, "Provisioning was successful!");
    }

    @Override
    public void onProvisioningFailed(Exception e) {
        Log.e(TAG, "Provisioning failed", e);
    }

    // Other listener methods for more granular feedback...
});
```
