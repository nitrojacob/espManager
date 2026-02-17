[<- Back to Index](./index.md)

# Security and Session Management

Security is a fundamental aspect of the provisioning process. The ESP-IDF Provisioning library provides a robust framework for establishing a secure session with the device, ensuring that all communication, including the delivery of sensitive Wi-Fi credentials, is encrypted and protected.

## 1. Security Levels

The library supports three levels of security, which are determined by the device's firmware configuration.

-   **Security 0 (`SECURITY_0`)**: This is the most basic level and offers no encryption. All communication is sent in plaintext. This mode is suitable only for development or in environments where security is not a concern.

-   **Security 1 (`SECURITY_1`)**: This is the recommended security level for most applications. It uses a **Proof of Possession (PoP)** to establish a secure, encrypted session. The PoP is a secret string that is known to both the Android application and the ESP device, but it is never transmitted over the air. It is used to derive a shared secret that encrypts the entire session.

-   **Security 2 (`SECURITY_2`)**: This is the highest security level, designed for scenarios that require an additional layer of authentication. It uses a username and a password (the PoP) to establish a secure session. This is less common and is typically used in enterprise or industrial environments.

## 2. Proof of Possession (PoP)

The Proof of Possession is the cornerstone of Security 1 and 2. It is a shared secret that proves to the device that the app has the right to provision it.

-   **How it Works**: Instead of sending the PoP directly, both the app and the device use it as input to a cryptographic algorithm. The resulting values are exchanged and verified. If they match, a secure session is established.
-   **Setting the PoP**: You must set the PoP in your Android code before attempting to connect to the device. The PoP should be obtained securely, for example, by scanning it from a QR code or having the user enter it manually.

    ```java
    // Set the PoP before connecting
    espDevice.setProofOfPossession("my-secret-pop-string");
    ```

## 3. Session Establishment

The secure session is established automatically by the library when you initiate an action that requires communication with the device, such as `provision()` or `sendDataToCustomEndPoint()`.

### The Handshake Process:

1.  **Initiation**: When you call a method like `provision()`, the library first checks if a session is already established.
2.  **Session Initialization**: If no session exists, it calls `initSession()` internally.
3.  **Security Handshake**: Based on the configured security level, the library and the device perform a handshake:
    -   For `SECURITY_1` or `SECURITY_2`, this involves the cryptographic exchange using the PoP.
    -   For `SECURITY_0`, this step is skipped.
4.  **Session Established**: Once the handshake is successful, the session is considered established, and all subsequent communication is encrypted (for Security 1 and 2).
5.  **Data Transmission**: The library then proceeds with the original action (e.g., sending the provisioning data).

If the handshake fails (e.g., due to an incorrect PoP), the `onFailure()` or equivalent error callback in your listener will be triggered, and the provisioning process will be aborted.
