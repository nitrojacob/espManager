[<- Back to Index](./index.md)

# Event Handling and Notifications

The library employs a robust event-driven architecture to provide real-time feedback on the provisioning process. It uses a combination of direct listeners for specific actions and a global event bus for broader application-wide notifications.

## 1. Direct Action Listeners

For specific, immediate asynchronous feedback on operations you initiate, the library provides several listener interfaces. These are ideal when you are performing an action and need to handle its outcome directly.

### Key Listeners:

-   **`ProvisionListener`**: This is the most detailed listener, providing callbacks for each stage of the provisioning process. It allows you to track the progress from sending the Wi-Fi configuration to the final success or failure.

    -   `onProvisioningFailed(Exception e)`
    -   `createSessionFailed(Exception e)`
    -   `wifiConfigSent()`
    -   `wifiConfigFailed(Exception e)`
    -   `wifiConfigApplied()`
    -   `wifiConfigApplyFailed(Exception e)`
    -   `deviceProvisioningSuccess()`
    -   `provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason reason)`

-   **`WiFiScanListener`**: Returns a list of available Wi-Fi networks found by the device via `onWifiListReceived()` or notifies of scan failure via `onWiFiScanFailed()`.

-   **`ResponseListener`**: Used for custom data communication via `sendDataToCustomEndPoint()`, providing `onSuccess(byte[] returnData)` and `onFailure(Exception e)` callbacks.

-   **`QRCodeScanListener`**: Provides feedback specifically for the QR code scanning workflow, including `qrCodeScanned()`, `deviceDetected(ESPDevice device)`, and `onFailure(Exception e)`.

## 2. Global Event Subscription with EventBus

For more decoupled, application-wide event handling, the library broadcasts key events using the **GreenRobot EventBus**. This is particularly useful for updating UI components or other parts of your application that are not directly responsible for initiating the provisioning process.

### Key Event Class:

-   **`DeviceConnectionEvent`**: This event is posted to the EventBus to notify about the device's connection status. It contains an `eventType` that can be one of the following:
    -   `ESPConstants.EVENT_DEVICE_CONNECTED`
    -   `ESPConstants.EVENT_DEVICE_CONNECTION_FAILED`
    -   `ESPConstants.EVENT_DEVICE_DISCONNECTED`

### Subscribing to Events:

To receive these notifications, you need to register a subscriber with the EventBus and create a method annotated with `@Subscribe`.

**Usage Example:**

```java
// 1. Register your class (e.g., an Activity or Fragment) with the EventBus
@Override
protected void onStart() {
    super.onStart();
    EventBus.getDefault().register(this);
}

// 2. Unregister to prevent memory leaks
@Override
protected void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
}

// 3. Create a subscriber method to handle the event
@Subscribe(threadMode = ThreadMode.MAIN)
public void onDeviceConnectionEvent(DeviceConnectionEvent event) {
    switch (event.getEventType()) {
        case ESPConstants.EVENT_DEVICE_CONNECTED:
            // Device is connected, update UI or proceed
            Log.d(TAG, "Device Connected");
            break;
        case ESPConstants.EVENT_DEVICE_CONNECTION_FAILED:
            // Connection failed, show an error message
            Log.e(TAG, "Device Connection Failed");
            break;
        case ESPConstants.EVENT_DEVICE_DISCONNECTED:
            // Device disconnected
             Log.w(TAG, "Device Disconnected");
            break;
    }
}
```

By combining direct listeners with global event subscriptions, you can build a responsive and robust user interface that accurately reflects the state of the provisioning process.
