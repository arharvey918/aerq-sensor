# aërQ

This repository contains a custom device handler (DH) for the aërQ Temperature and Humidity sensor.

It is based on code from SmartThings and Aeotec's ccheng (see References).

## Installation

***referenced from [Aeotec support](https://aeotec.freshdesk.com/support/solutions/articles/6000244682-setup-a%C3%ABrq-temp-and-humidity-sensor-with-smartthings)***

### Install the Handler

1. Sign in to the SmartThings IDE at <https://account.smartthings.com>
2. Go to "My Device Handlers"
3. Create a new Device Handler by clicking on "New Device Handler" button in the upper-right corner.
4. Click on "From Code."
5. Copy the code from aeotec-aerq.groovy file in this repository and paste it into the code section
6. Click Save
7. Click Publish > For Me

### Add the aërQ sensor

1. Pair the aërQ sensor with your hub
2. In the SmartThings IDE at <https://account.smartthings.com>, select the newly paired sensor
3. If the Type field is not set to aerQ sensor, it is still using the generic Z-wave driver. Click on the Display name for the sensor.
4. Click the Edit button
5. Change the Type to aerQ Sensor
6. Click Update
7. You may need to restart your SmartThings app for it to pick up the change

You should now be able to see the Settings option for the sensor in the app.

## References

- <https://help.aeotec.com/support/solutions/articles/6000227918-a%C3%ABrq-temperature-and-humidity-sensor-user-guide>-
- <https://github.com/SmartThingsCommunity/SmartThingsPublic/blob/master/devicetypes/smartthings/zwave-mold-detector.src/zwave-mold-detector.groovy>
- <https://github.com/Aeotec-ccheng/SmartThingsPublic/blob/aerQ/devicetypes/smartthings/aeotec-aerq.src/aeotec-aerq.groovy>
- <https://aeotec.freshdesk.com/support/solutions/articles/6000244682-setup-a%C3%ABrq-temp-and-humidity-sensor-with-smartthings>
