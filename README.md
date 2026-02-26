# espManager
Android app to manage a fleet of esp devices. Users can either manage/configure the devices locally or can provision them to connect to an iot broker.

## Connecting to the esp device
We use WiFi provisioning. The ESP device creates a wifi hotspot. The SSID, pasword etc of the ESP device is captured in a factory provided QR code, with same format as that of the espressif provisioning library.

<verbose>

{"ver":"v1","name":"mySSID","password":"myPassword","pop":"myPoP","transport":"softap","security":"1"}

<verbose/>

