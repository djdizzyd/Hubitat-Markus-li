#  Copyright 2019 Markus Liljergren
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

"""
  Snippets used by hubitat-driver-helper-tool
"""

def getDefaultMetadataCapabilities():
    return """
// Default Capabilities
capability "Refresh"
capability "Configuration"
"""

def getDefaultMetadataCapabilitiesForEnergyMonitor():
    return """
// Default Capabilities for Energy Monitor
capability "VoltageMeasurement"
capability "PowerMeter"
capability "EnergyMeter"
"""

def getDefaultMetadataCapabilitiesForTHMonitor():
    return """
// Default Capabilities for TH Monitor
capability "Sensor"
capability "TemperatureMeasurement"
capability "RelativeHumidityMeasurement"
capability "PressureMeasurement"
"""

def getDefaultParentMetadataAttributes():
    return """
// Default Parent Attributes
attribute   "ip", "string"
attribute   "ipLink", "string"
attribute   "module", "string"
attribute   "templateData", "string"
attribute   "wifiSignal", "string"
"""

def getDefaultMetadataAttributes():
    return """
// Default Attributes
attribute   "driver", "string"
"""

def getDefaultMetadataAttributesForEnergyMonitor():
    return """
// Default Attributes for Energy Monitor
attribute   "current", "string"
attribute   "apparentPower", "string"
attribute   "reactivePower", "string"
attribute   "powerFactor", "string"
attribute   "energyToday", "string"
attribute   "energyYesterday", "string"
attribute   "energyTotal", "string"
attribute   "voltageWithUnit", "string"
attribute   "powerWithUnit", "string"
"""

def getDefaultMetadataAttributesForDimmableLights():
    return """
// Default Attributes for Dimmable Lights
attribute   "wakeup", "string"
"""

def getDefaultMetadataAttributesForTHMonitor():
    return """
// Default Attributes for Pressure Sensor
attribute   "pressureWithUnit", "string"
"""

def getLearningModeAttributes():
    return """
// Attributes used for Learning Mode
attribute   "status", "string"
attribute   "actionSeen", "number"
attribute   "actionData", "json_object"
"""

def getMinimumChildAttributes():
    return """
// Attributes used by all Child Drivers
attribute   "driver", "string"
"""

def getDefaultMetadataCommands():
    return """
// Default Commands
command "reboot"
"""

def getMetadataCommandsForHandlingChildDevices():
    return """
// Commands for handling Child Devices
//command "childOn"
//command "childOff"
//command "recreateChildDevices"
command "deleteChildren"
"""

def getMetadataCommandsForHandlingRGBWDevices():
    return """
// Commands for handling RGBW Devices
command "colorWhite"
command "colorRed"
command "colorGreen"
command "colorBlue"
command "colorYellow"
command "colorCyan"
command "colorPink"
"""

def getMetadataCommandsForHandlingTasmotaRGBWDevices():
    return """
// Commands for handling Tasmota RGBW Devices
command "setEffectWithSpeed", [[name:"Effect number*", type: "NUMBER", description: "Effect number to enable"],
    [name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setNextEffectWithSpeed", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setPreviousEffectWithSpeed", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectSingleColor", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectCycleUpColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectCycleDownColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
command "setEffectRandomColors", [[name:"Speed", type: "NUMBER", description: "1..40 = set speed, it represents the time in 0.5s to fade from 0 to 100%"]]
"""

def getMetadataCommandsForHandlingTasmotaDimmerDevices():
    return """
// Commands for handling Tasmota Dimmer Devices
command "modeWakeUp", [[name:"Wake Up Duration*", type: "NUMBER", description: "1..3000 = set wake up duration in seconds"],
                       [name:"Level", type: "NUMBER", description: "1..100 = target dimming level"] ]
"""

def getLearningModeCommands():
    return """
// Commands used for Learning Mode
command("actionStartLearning")
command("actionSave")
command("actionPauseUnpauseLearning")
"""

def getMetadataCustomizationMethods():
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    return """
// Here getPreferences() can be used to get the above preferences
metaDataExporter()
if(isCSSDisabled() == false) {
    preferences {
        input(name: "hiddenSetting", description: "" + getDriverCSSWrapper(), title: "None", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    }
}
"""

def getDefaultParentMetadataPreferences():
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    return """
// Default Parent Preferences
input(name: "runReset", description: addDescriptionDiv("For details and guidance, see the release thread in the <a href=\\\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\\\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct."), title: addTitleDiv("Settings"), displayDuringSetup: false, type: "paragraph", element: "paragraph")
generate_preferences(configuration_model_debug())
"""

def getDefaultMetadataPreferences():
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    return """
// Default Preferences
generate_preferences(configuration_model_debug())
"""

def getDefaultMetadataPreferencesLast():
    #input(description: "Once you change values on this page, the corner of the 'configuration' icon will change to orange until all configuration parameters are updated.", title: "Settings", displayDuringSetup: false, type: "paragraph", element: "paragraph")
    return """
// Default Preferences - Last
input(name: "hideDangerousCommands", type: "bool", title: addTitleDiv("Hide Dangerous Commands"), description: addDescriptionDiv("Hides Dangerous Commands, such as 'Delete Children'."), defaultValue: true, displayDuringSetup: false, required: false)
input(name: "disableCSS", type: "bool", title: addTitleDiv("Disable CSS"), description: addDescriptionDiv("CSS makes the driver more user friendly. Disable the use of CSS in the driver by enabling this. Does NOT affect HE resource usage either way."), defaultValue: false, displayDuringSetup: false, required: false)
"""

def getMetadataPreferencesForHiding():
    return """
// Preferences for Hiding
input(name: "hideExtended", type: "bool", title: addTitleDiv("Hide Extended Settings"), description: addDescriptionDiv("Hides extended settings, usually not needed."), defaultValue: true, displayDuringSetup: false, required: false)
input(name: "hideAdvanced", type: "bool", title: addTitleDiv("Hide Advanced Settings"), description: addDescriptionDiv("Hides advanced settings, usually not needed anyway."), defaultValue: true, displayDuringSetup: false, required: false)
"""

def getDefaultMetadataPreferencesForTasmota(includeTelePeriod=True):
    return """
// Default Preferences for Tasmota
generate_preferences(configuration_model_tasmota())
input(name: "ipAddress", type: "string", title: addTitleDiv("Device IP Address"), description: addDescriptionDiv("Set this as a default fallback for the auto-discovery feature."), displayDuringSetup: true, required: false)
input(name: "port", type: "number", title: addTitleDiv("Device Port"), description: addDescriptionDiv("The http Port of the Device (default: 80)"), displayDuringSetup: true, required: false, defaultValue: 80)
input(name: "override", type: "bool", title: addTitleDiv("Override IP"), description: addDescriptionDiv("Override the automatically discovered IP address and disable auto-discovery."), displayDuringSetup: true, required: false)
input(name: "useIPAsID", type: "bool", title: addTitleDiv("IP as Network ID"), description: addDescriptionDiv("Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance."), displayDuringSetup: true, required: false)
""" + ("""input(name: "telePeriod", type: "string", title: addTitleDiv("Update Frequency"), description: addDescriptionDiv("Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This ONLY affects SENSORS and reporting of data such as UPTIME. (default = 300)"), displayDuringSetup: true, required: false)""" if includeTelePeriod else "") + """
input(name: "disableModuleSelection", type: "bool", title: addTitleDiv("Disable Automatically Setting Module and Template"), description: "ADVANCED: " + addDescriptionDiv("Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device."), displayDuringSetup: true, required: false)
input(name: "moduleNumber", type: "number", title: addTitleDiv("Module Number"), description: "ADVANCED: " + addDescriptionDiv("Module Number used in Tasmota. If Device Template is set, this value is IGNORED. (default: -1 (use the default for the driver))"), displayDuringSetup: true, required: false, defaultValue: -1)
input(name: "deviceTemplateInput", type: "string", title: addTitleDiv("Device Template"), description: "ADVANCED: " + addDescriptionDiv("Set this to a Device Template for Tasmota, leave it EMPTY to use the Device Configuration Default. Set it to 0 to NOT use a Template. NAME can be maximum 14 characters! (Example: {\\\"NAME\\\":\\\"S120\\\",\\\"GPIO\\\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\\\"FLAG\\\":0,\\\"BASE\\\":18})"), displayDuringSetup: true, required: false)

"""

def getDefaultMetadataPreferencesForTHMonitor():
    return """
// Default Preferences for Temperature Humidity Monitor
input(name: "tempOffset", type: "decimal", title: addTitleDiv("Temperature Offset"), description: addDescriptionDiv("Adjust the temperature by this many degrees (in Celcius)."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "tempRes", type: "enum", title: addTitleDiv("Temperature Resolution"), description: addDescriptionDiv("Temperature sensor resolution (0..3 = maximum number of decimal places, default: 1)<br/>NOTE: If the 3rd decimal is a 0 (eg. 24.720) it will show without the last decimal (eg. 24.72)."), options: ["0", "1", "2", "3"], defaultValue: "1", displayDuringSetup: true, required: false)
input(name: "tempUnitConversion", type: "enum", title: addTitleDiv("Temperature Unit Conversion"), description: "", defaultValue: "1", required: true, multiple: false, options:[["1":"none"], ["2":"Celsius to Fahrenheit"], ["3":"Fahrenheit to Celsius"]], displayDuringSetup: false)
input(name: "humidityOffset", type: "decimal", title: addTitleDiv("Humidity Offset"), description: addDescriptionDiv("Adjust the humidity by this many percent."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "pressureOffset", type: "decimal", title: addTitleDiv("Pressure Offset"), description: addDescriptionDiv("Adjust the pressure value by this much."), displayDuringSetup: true, required: false, range: "*..*")
input(name: "pressureUnitConversion", type: "enum", title: addTitleDiv("Pressure Unit Conversion"), description: addDescriptionDiv("(default: kPa)"), options: ["mbar", "kPa", "inHg", "mmHg", "atm"], defaultValue: "kPa")
"""

def getDefaultMetadataPreferencesForParentDevices(numSwitches=1):
    return '''
// Default Preferences for Parent Devices
input(name: "numSwitches", type: "enum", title: addTitleDiv("Number of Relays"), description: addDescriptionDiv("Set the number of buttons/relays on the device (default ''' + str(numSwitches) + ''')"), options: ["1", "2", "3", "4", "5", "6"], defaultValue: "''' + str(numSwitches) + '''", displayDuringSetup: true, required: true)
'''

def getDefaultMetadataPreferencesForParentDevicesWithUnlimitedChildren(numSwitches=1):
    return '''
// Default Preferences for Parent Devices
input(name: "numSwitches", type: "number", title: addTitleDiv("Number of Children"), description: addDescriptionDiv("Set the number of children (default ''' + str(numSwitches) + ''')"), defaultValue: "''' + str(numSwitches) + '''", displayDuringSetup: true, required: true)
'''

