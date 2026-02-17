[<- Back to Index](./index.md)

# Custom Data Communication

Beyond provisioning, the library provides a powerful mechanism for bidirectional communication with your ESP device. This allows your application to interact with custom firmware endpoints, enabling a wide range of functionality, such as:

-   Toggling relays or LEDs.
-   Sending configuration parameters (e.g., sensor sensitivity).
-   Requesting device status or sensor readings.

This communication is handled by the `sendDataToCustomEndPoint()` method in the `ESPDevice` class, which sends data to a specified endpoint on the device and receives a response.

## 1. How It Works

1.  **Session Establishment**: Custom data communication requires a secure session. The library automatically establishes this session if one is not already active when you call `sendDataToCustomEndPoint()`. The session ensures that all communication is encrypted and secure.

2.  **Endpoint Path**: You must define a unique string identifier, or "path," for each custom endpoint in your device's firmware. This same path is used in the Android app to direct the data to the correct handler on the device.

3.  **Data Exchange**: Data is sent and received as a raw byte array (`byte[]`). This gives you the flexibility to use any data format you prefer, such as JSON, plain text, or a custom binary format.

## 2. Device-Side (Firmware) Implementation

On your ESP device firmware (using the ESP-IDF), you need to register a corresponding handler for each custom endpoint. This is typically done using the `protocomm_add_ep()` function.

```c
// Example C code for an ESP-IDF project

#include "protocomm.h"
#include "esp_log.h"
#include "cJSON.h"

static const char *TAG = "CUSTOM_DATA";

// Handler function for a custom endpoint
esp_err_t custom_data_handler(uint32_t session_id, const uint8_t *inbuf, ssize_t inlen, uint8_t **outbuf, ssize_t *outlen, void *priv_data) {
    if (inbuf == NULL || inlen < 1) {
        ESP_LOGE(TAG, "Invalid data received");
        return ESP_ERR_INVALID_ARG;
    }

    // Assuming the input is a JSON string, e.g., {"command":"get_status"}
    cJSON *json = cJSON_Parse((const char *)inbuf);
    if (json == NULL) {
        ESP_LOGE(TAG, "Failed to parse JSON");
        return ESP_FAIL;
    }

    cJSON *command = cJSON_GetObjectItem(json, "command");
    char *response_str = NULL;

    if (cJSON_IsString(command) && (strcmp(command->valuestring, "get_status") == 0)) {
        // Prepare a JSON response
        response_str = "{\"status\": \"all_ok\"}";
    } else {
        response_str = "{\"status\": \"unknown_command\"}";
    }

    *outbuf = (uint8_t *)strdup(response_str);
    *outlen = strlen(response_str);

    cJSON_Delete(json);
    return ESP_OK;
}

// In your application startup code:
void register_custom_endpoint() {
    protocomm_add_ep("device-config", custom_data_handler, NULL);
}
```

-   The endpoint name (`"device-config"` in the example) must exactly match the `path` string used in the Android app.
-   The handler function is responsible for processing the incoming data and providing a response.

## 3. `sendDataToCustomEndPoint` Method (Android)

```java
public void sendDataToCustomEndPoint(final String path, final byte[] data, final ResponseListener listener)
```

-   **`path`**: A `String` representing the custom endpoint on the device firmware (e.g., `"device-config"`).
-   **`data`**: A `byte[]` array containing the payload you want to send.
-   **`listener`**: A `ResponseListener` callback to handle the outcome of the operation.
    -   `onSuccess(byte[] returnData)`: Called when the device successfully processes the request. The `returnData` parameter contains the device's response.
    -   `onFailure(Exception e)`: Called if the communication fails at any point.

## 4. Comprehensive Usage Example (Android)

This example demonstrates sending a JSON command to the `"device-config"` endpoint and parsing the JSON response.

```java
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import org.json.JSONException;

// Assume 'espDevice' is an initialized and connected ESPDevice object

private void getDeviceStatus() {
    // 1. Define the endpoint path and create the JSON payload
    String endpointPath = "device-config";
    JSONObject command = new JSONObject();
    try {
        command.put("command", "get_status");
    } catch (JSONException e) {
        Log.e(TAG, "Failed to create JSON command", e);
        return;
    }

    // Convert the payload to a byte array
    byte[] payload = command.toString().getBytes(StandardCharsets.UTF_8);

    // 2. Send the data to the custom endpoint
    espDevice.sendDataToCustomEndPoint(endpointPath, payload, new ResponseListener() {
        @Override
        public void onSuccess(byte[] returnData) {
            // 3. Process the successful response from the device
            if (returnData == null || returnData.length == 0) {
                Log.w(TAG, "Received empty response from device.");
                return;
            }
            String responseJson = new String(returnData, StandardCharsets.UTF_8);
            Log.d(TAG, "Received response: " + responseJson);
            try {
                JSONObject response = new JSONObject(responseJson);
                String status = response.getString("status");
                
                // Update UI or application state based on the device status
                Log.i(TAG, "Device status is: " + status);

            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON response from device", e);
            }
        }

        @Override
        public void onFailure(Exception e) {
            // 4. Handle any communication errors
            Log.e(TAG, "Failed to send custom data", e);
            // Display an error message to the user
        }
    });
}
```
