/**
 *  Copyright 2020 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Generic Z-Wave Water/Temp/Humidity Sensor
 *
 *  Author: SmartThings
 *  Date: 2020-07-22
 */

metadata {
	definition(name: "aerQ Sensor", namespace: "Aeotec", author: "Chris", mnmn: "SmartThings", vid: "aerq-custom", ocfDeviceType: "oic.d.thermostat") {
		capability "Temperature Measurement"
		capability "Relative Humidity Measurement"
		capability "Dew Point"
		capability "Mold Health Concern"
		capability "Battery"
		capability "Sensor"
		capability "Health Check"
		capability "Configuration"

		attribute "updateNeeded", "string"
		attribute "parameter1", "number"
		attribute "parameter2", "number"
		attribute "parameter3", "number"
		attribute "parameter4", "number"
		attribute "parameter64", "number"

		// Aeotec Aerq Temperature and Humidity Sensor
		fingerprint mfr:"0371", prod:"0002", model:"0009", deviceJoinName: "Aeotec Multipurpose Sensor", mnmn: "SmartThings", vid: "aeotec-temp-humidity" //EU
		fingerprint mfr:"0371", prod:"0102", model:"0009", deviceJoinName: "Aeotec Multipurpose Sensor", mnmn: "SmartThings", vid: "aeotec-temp-humidity" //US
		fingerprint mfr:"0371", prod:"0202", model:"0009", deviceJoinName: "Aeotec Multipurpose Sensor", mnmn: "SmartThings", vid: "aeotec-temp-humidity" //AU
		// POPP Mold Detector
		fingerprint mfr:"0154", prod:"0004", model:"0014", deviceJoinName: "POPP Multipurpose Sensor"	//EU
	}

	tiles(scale: 2) {
		multiAttributeTile(name: "temperature", type: "generic", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute("device.temperature", key: "PRIMARY_CONTROL") {
				attributeState "temperature", label: '${currentValue}°',
						backgroundColors: [
								[value: 31, color: "#153591"],
								[value: 44, color: "#1e9cbb"],
								[value: 59, color: "#90d2a7"],
								[value: 74, color: "#44b621"],
								[value: 84, color: "#f1d801"],
								[value: 95, color: "#d04e00"],
								[value: 96, color: "#bc2323"]
						]
			}
		}
		valueTile("humidity", "device.humidity", inactiveLabel: false, width: 2, height: 2) {
			state "humidity", label: '${currentValue}% humidity', unit: ""
		}
		valueTile("dewPoint", "device.dewPoint", inactiveLabel: false, width: 2, height: 2) {
			state "dewPoint", label: '${currentValue}° dewPoint', unit: ""
		}
		valueTile("battery", "device.battery", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "battery", label: '${currentValue}% battery', unit: ""
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label: "", action: "refresh.refresh", icon: "st.secondary.refresh"
		}

		main "temperature", "humidity", "dewPoint"
		details(["temperature", "humidity", "dewPoint", "battery"])
	}
	preferences {
		section {
			input(
				title: "Threshold settings determine if there is sufficient change to temperature or humidity to send an unsolicited report. Operates at the same time as Periodic reports.",
				type: "paragraph",
				element: "paragraph"
			)
			input(
				title: "Min temp change to report",
				description: "Minimum change of temperature to cause an unsolicited report; checked on threshold interval (scale: .1°)",
				name: "thresholdTemperatureValue",
				type: "number",
				range: "1..100",
				defaultValue: 10
			)
			input(
				title: "Min humidity change to report",
				description: "Minimum change of humidity % to cause an unsolicited report; checked on threshold interval (scale: 1%)",
				name: "thresholdHumidityValue",
				type: "number",
				range: "1..20",
				defaultValue: 5
			)
			input(
				title: "Threshold interval",
				description: "Determines how often aerQ Sensor checks for minimum changes based on Parameter 1 and 2 or when the Sensor wakes up by button pressing (scale: minutes)",
				name: "thresholdInterval",
				type: "number",
				range: "1..255",
				defaultValue: 15
			)
			input(
				title: "Periodic settings determine how often both temperature and humidity are reported. Operates at the same time as threshold reports.",
				type: "paragraph",
				element: "paragraph"
			)
			input(
				title: "Periodic reports",
				description: "Defines the time interval to send an unsolicited report (scale: seconds)",
				name: "periodicReportValue",
				type: "number",
				range: "900..65535",
				defaultValue: 43200
			)
			input(
				title: "The Temperature Scale setting will take a manual wake up or two to set; the following temperature sensor reports will change the temperature unit and value appropriately.",
				type: "paragraph",
				element: "paragraph"
			)
			input(
				title: "Temperature unit:",
				description: "Sets the temperature unit to C or F (1 = Celsius, 2 = Fahrenheit)",
				name: "temperatureScaleSetting",
				type: "number",
				range: "1..2",
			)
			input(
				title: "PARAMETERS END - All configurations will take place after aerQ Sensor has been woken up. You can wait up to 12 hours or immediately wakeup aerQ by tapping its button.",
				type: "paragraph",
				element: "paragraph"
			)
		}
	}
}

def installed() {
	sendEvent(name: "checkInterval", value: 15 * 60, displayed: false, data: [protocol: "zwave", hubHardwareId: device.hub.hardwareID])
	// device doesn't send it on inclusion by itself, so event is needed to populate plugin
	sendEvent(name: "moldHealthConcern", value: "good", displayed: false)

	def cmds = [
		secure(zwave.batteryV1.batteryGet()),
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x05)), // humidity
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x01)), // temperature
		secure(zwave.sensorMultilevelV5.sensorMultilevelGet(sensorType: 0x0B)), // dew point
		secure(zwave.wakeUpV2.wakeUpNoMoreInformation())
	]

	response(cmds)
}

def updated() {
	def cmds = []
	cmds << sendEvent(name: "updateNeeded", value: "true", displayed: false)
	response(cmds)
}

def parse(String description) {
	def results = []

	if (description.startsWith("Err")) {
		results += createEvent(descriptionText: description, displayed: true)
	} else {
		def cmd = zwave.parse(description)
		if (cmd) {
			results += zwaveEvent(cmd)
		}
	}

	log.debug "parse() result ${results.inspect()}"

	return results
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpIntervalReport cmd) {
	log.debug "Wake Up Interval Report: ${cmd}"
	def result = []

	if (!state.lastbat || (new Date().time) - state.lastbat > 53 * 60 * 60 * 1000) {
		result << response(secure(zwave.batteryV1.batteryGet()))
	}

	result << [createEvent(descriptionText: "${device.displayName} woke up", isStateChange: false)]
	result << response(secure(zwave.wakeUpV1.wakeUpNoMoreInformation()))
	result
}

def zwaveEvent(physicalgraph.zwave.commands.configurationv2.ConfigurationReport cmd) {
	switch (cmd.parameterNumber) {
		case 0x01:
			state.parameter1 = cmd.scaledConfigurationValue
			sendEvent(name: "parameter1", value: cmd.scaledConfigurationValue, displayed: false)
			break
		case 0x02:
			state.parameter2 = cmd.scaledConfigurationValue
			sendEvent(name: "parameter2", value: cmd.scaledConfigurationValue, displayed: false)
			break
		case 0x03:
			state.parameter3 = cmd.scaledConfigurationValue
			sendEvent(name: "parameter3", value: cmd.scaledConfigurationValue, displayed: false)
			break
		case 0x04:
			state.parameter4 = cmd.scaledConfigurationValue
			if(state.parameter4 < 0) {
				state.parameter4 = state.parameter4 + 65536
			}
			sendEvent(name: "parameter4", value: state.parameter4, displayed: false)
			break
		case 0x40:
			state.parameter64 = cmd.scaledConfigurationValue
			sendEvent(name: "parameter64", value: state.parameter64, displayed: false)
			break
		default:
			log.debug "Setting unknown parameter"
			break
	}

	checkParameterValues()
}

def checkParameterValues() {
	//if parameter settings fail somehow, wakeup can cause parameter settings to update again the next time. When all settings are true, then stop parameter updates the next time.
	if (state.parameter1 == thresholdTemperatureValue && state.parameter2 == thresholdHumidityValue && state.parameter4 == periodicReportValue && state.parameter64 == temperatureScaleSetting) {
		sendEvent(name: "updateNeeded", value: "false", displayed: false)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.notificationv3.NotificationReport cmd) {
	log.debug "Event: ${cmd.event}, Notification type: ${cmd.notificationType}"

	def value
	def description

	if (cmd.notificationType == 0x10) {  // Mold Environment Detection
		switch (cmd.event) {
			case 0x00:
				value = "good"
				description = "Mold environment not detected"
				break
			case 0x02:
				value = "unhealthy"
				description = "Mold environment detected"
				break
			default:
				log.warn "Not handled event type for Mold Environment Detection: ${cmd.event}"
				return
		}

		createEvent(name: "moldHealthConcern", value: value, descriptionText: description, isStateChange: true, displayed: true)
	}
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
	def map = [name: "battery", unit: "%", isStateChange: true]
	state.lastbatt = now()

	if (cmd.batteryLevel == 0xFF) {
		map.value = 1
		map.descriptionText = "$device.displayName battery is low!"
	} else {
		map.value = cmd.batteryLevel
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.sensormultilevelv5.SensorMultilevelReport cmd) {
	def map = [:]

	switch (cmd.sensorType) {
		case 0x01:
			map.name = "temperature"
			map.unit = temperatureScale
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
			map.displayed = true
			map.isStateChange = true
			break
		case 0x05:
			map.name = "humidity"
			map.value = cmd.scaledSensorValue.toInteger()
			map.unit = "%"
			map.displayed = true
			map.isStateChange = true
			break
		case 0x0B:
			map.name = "dewpoint"
			map.unit = temperatureScale
			map.value = convertTemperatureIfNeeded(cmd.scaledSensorValue, cmd.scale == 1 ? "F" : "C", cmd.precision)
			map.displayed = true
			map.isStateChange = true
			break
		default:
			map.descriptionText = cmd.toString()
	}

	createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.commands.wakeupv2.WakeUpNotification cmd) {
	def cmds = []
	def result = createEvent(descriptionText: "$device.displayName woke up", isStateChange: false)

	if (!state.lastbatt || (now() - state.lastbatt) >= 10 * 60 * 60 * 1000) {
		cmds += [
			"delay 1000",
			secure(zwave.batteryV1.batteryGet()),
			"delay 2000"
		]
	}

	cmds += secure(zwave.wakeUpV2.wakeUpIntervalSet(seconds:43200, nodeid:zwaveHubNodeId))
	cmds += secure(zwave.wakeUpV2.wakeUpIntervalGet())

	if (device.currentValue("updateNeeded") == "true") {
		if (thresholdTemperatureValue != state.parameter1 && thresholdTemperatureValue) {
			cmds += response(secure(zwave.configurationV1.configurationSet(parameterNumber: 1, size: 1, scaledConfigurationValue: thresholdTemperatureValue)))
			cmds += response(secure(zwave.configurationV1.configurationGet(parameterNumber: 1)))
		}
		if (thresholdHumidityValue != state.parameter2 && thresholdHumidityValue) {
			cmds += response(secure(zwave.configurationV1.configurationSet(parameterNumber: 2, size: 1, scaledConfigurationValue: thresholdHumidityValue)))
			cmds += response(secure(zwave.configurationV1.configurationGet(parameterNumber: 2)))
		}
		if (thresholdHumidityValue != state.parameter3 && thresholdInterval) {
			cmds += response(secure(zwave.configurationV1.configurationSet(parameterNumber: 3, size: 1, scaledConfigurationValue: thresholdInterval)))
			cmds += response(secure(zwave.configurationV1.configurationGet(parameterNumber: 3)))
		}
		if (periodicReportValue != state.parameter4 && periodicReportValue) {
			cmds += response(secure(zwave.configurationV1.configurationSet(parameterNumber: 4, size: 2, scaledConfigurationValue: periodicReportValue)))
			cmds += response(secure(zwave.configurationV1.configurationGet(parameterNumber: 4)))
		}
		if (temperatureScaleSetting != state.parameter64 && temperatureScaleSetting) {
			cmds += response(secure(zwave.configurationV1.configurationSet(parameterNumber: 64, size: 1, scaledConfigurationValue: temperatureScaleSetting)))
			cmds += response(secure(zwave.configurationV1.configurationGet(parameterNumber: 64)))
		}
	}

	cmds += secure(zwave.wakeUpV2.wakeUpNoMoreInformation())

	[result, response(cmds)]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.warn "Unhandled command: ${cmd}"
}

private secure(cmd) {
	if (zwaveInfo.zw.contains("s")) {
		zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
	} else {
		cmd.format()
	}
}