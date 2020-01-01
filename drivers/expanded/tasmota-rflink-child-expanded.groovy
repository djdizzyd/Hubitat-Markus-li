 /**
 *  Copyright 2019 Markus Liljergren
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at:
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/* Default imports */
import groovy.json.JsonSlurper


metadata {
    definition (name: "Tasmota - DO NOT USE RFLink (Child)", namespace: "tasmota", author: "Markus Liljergren", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/master/drivers/expanded/tasmota-rflink-child-expanded.groovy") {
        capability "Switch"
        capability "Actuator"

        
        // Default Preferences
        input(name: "runReset", description: "<i>For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct.<br/>Type RESET and then press 'Save Preferences' to DELETE all Preferences and return to DEFAULTS.</i>", title: "<b>Settings</b>", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        generate_preferences(configuration_model_debug())
        
    }
}

def getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Tasmota - DO NOT USE RFLink (Child)', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/master/drivers/expanded/tasmota-rflink-child-expanded.groovy']
    return(deviceInfo[infoName])
}

/* These functions are unique to each driver */
void on() { 
    logging("$device on",1)
    parent.childOn(device.deviceNetworkId)
}

void off() {
    logging("$device off",1)
    parent.childOff(device.deviceNetworkId)
}

def parseParentData(parentData) {
    logging("parseParentData(parentData=${parentData})",1)
}

/* Default functions go here */
private def getDriverVersion() {
    logging("getDriverVersion()", 50)
	def cmds = []
    comment = ""
    if(comment != "") state.comment = comment
    sendEvent(name: "driverVersion", value: "v0.9.2 for Tasmota 7.x (Hubitat version)")
    return cmds
}


/* Helper functions included in all drivers */
def ping() {
    logging("ping()", 50)
    refresh()
}

def installed() {
	logging("installed()", 50)
	configure()
    try {
        // In case we have some more to run specific to this driver
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/*
	initialize

	Purpose: initialize the driver
	Note: also called from updated() in most drivers
*/
void initialize()
{
    logging("initialize()", 50)
	unschedule()
    // disable debug logs after 30 min, unless override is in place
	if (logLevel != "0") {
        if(runReset != "DEBUG") {
            log.warn "Debug logging will be disabled in 30 minutes..."
        } else {
            log.warn "Debug logging will NOT BE AUTOMATICALLY DISABLED!"
        }
        runIn(1800, logsOff)
    }
}

def configure() {
    logging("configure()", 50)
    def cmds = []
    cmds = update_needed_settings()
    try {
        // Run the getDriverVersion() command
        newCmds = getDriverVersion()
        if (newCmds != null && newCmds != []) cmds = cmds + newCmds
    } catch (MissingMethodException e) {
        // ignore
    }
    if (cmds != []) cmds
}

def generate_preferences(configuration_model)
{
    def configuration = new XmlSlurper().parseText(configuration_model)
   
    configuration.Value.each
    {
        if(it.@hidden != "true" && it.@disabled != "true"){
        switch(it.@type)
        {   
            case ["number"]:
                input "${it.@index}", "number",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "list":
                def items = []
                it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                input "${it.@index}", "enum",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}",
                    options: items
            break
            case ["password"]:
                input "${it.@index}", "password",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "decimal":
               input "${it.@index}", "decimal",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    range: "${it.@min}..${it.@max}",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
            case "boolean":
               input "${it.@index}", "boolean",
                    title:"<b>${it.@label}</b>\n" + "${it.Help}",
                    description: "<i>${it.@description}</i>",
                    defaultValue: "${it.@value}",
                    displayDuringSetup: "${it.@displayDuringSetup}"
            break
        }
        }
    }
}

def update_current_properties(cmd)
{
    def currentProperties = state.currentProperties ?: [:]
    currentProperties."${cmd.name}" = cmd.value

    if (state.settings?."${cmd.name}" != null)
    {
        if (state.settings."${cmd.name}".toString() == cmd.value)
        {
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: true)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: true)
        }
    }
    state.currentProperties = currentProperties
}

/*
	logsOff

	Purpose: automatically disable debug logging after 30 mins.
	Note: scheduled in Initialize()
*/
void logsOff(){
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled..."
        // Setting logLevel to "0" doesn't seem to work, it disables logs, but does not update the UI...
        //device.updateSetting("logLevel",[value:"0",type:"string"])
        //app.updateSetting("logLevel",[value:"0",type:"list"])
        // Not sure which ones are needed, so doing all... This works!
        device.clearSetting("logLevel")
        device.removeSetting("logLevel")
        state.settings.remove("logLevel")
    } else {
        log.warn "OVERRIDE: Disabling Debug logging will not execute with 'DEBUG' set..."
        if (logLevel != "0") runIn(1800, logsOff)
    }
}

def configuration_model_debug()
{
'''
<configuration>
<Value type="list" index="logLevel" label="Debug Log Level" description="Under normal operations, set this to None. Only needed for debugging. Auto-disabled after 30 minutes." value="0" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Insanely Verbose" value="-1" />
    <Item label="Very Verbose" value="1" />
    <Item label="Verbose" value="10" />
    <Item label="Reports+Status" value="50" />
    <Item label="Reports" value="99" />
    </Value>
</configuration>
'''
}

/* Logging function included in all drivers */
private def logging(message, level) {
    if (logLevel != "0"){
        switch (logLevel) {
        case "-1": // Insanely verbose
            if (level >= 0 && level < 99 || level == 100)
                log.debug "$message"
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99 || level == 100)
                log.debug "$message"
        break
        case "10": // A little less
            if (level >= 10 && level < 99 || level == 100)
                log.debug "$message"
        break
        case "50": // Rather chatty
            if (level >= 50 )
                log.debug "$message"
        break
        case "99": // Only parsing reports
            if (level >= 99 )
                log.debug "$message"
        break
        }
    }
}

