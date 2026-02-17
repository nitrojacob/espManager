# ESP-IDF Provisioning Android Library: A Comprehensive Guide

## Overview

The ESP-IDF Provisioning Android library is a powerful tool for configuring and commissioning Espressif devices. It provides a flexible and extensible framework for both SoftAP and BLE provisioning, enabling your Android application to seamlessly connect to and configure ESP devices.

This documentation provides a comprehensive guide to the library's architecture, features, and usage, with detailed explanations and code examples.

### Key Features:

- **Dual-Transport Support**: Provision devices over both Wi-Fi (SoftAP) and Bluetooth Low Energy (BLE).
- **Flexible Security**: Supports multiple security levels (Security 0, 1, and 2) to protect the provisioning process.
- **QR Code Provisioning**: Streamlines the user experience by allowing quick device setup through QR code scanning.
- **Session Management**: Establishes a secure session with the device for reliable and protected communication.
- **Event-Driven Architecture**: Uses listeners and a global event bus to provide real-time feedback on the provisioning status.
- **Custom Data Communication**: Enables sending and receiving custom data to and from the device, extending its functionality beyond provisioning.

## Table of Contents

1.  **[Core Components](./core_components.md)**
    -   `ESPProvisionManager`
    -   `ESPDevice`
2.  **[Provisioning Workflows](./provisioning_workflows.md)**
    -   SoftAP Provisioning
    -   BLE Provisioning
    -   QR Code Provisioning
3.  **[Security and Session Management](./security_and_session.md)**
    -   Security Levels (0, 1, and 2)
    -   Proof of Possession (PoP)
    -   Session Establishment
4.  **[Event Handling and Notifications](./event_handling.md)**
    -   Direct Action Listeners
    -   Global Event Subscription with EventBus
5.  **[Custom Data Communication](./custom_communication.md)**
    -   Device-Side (Firmware) Implementation
    -   Android Implementation
