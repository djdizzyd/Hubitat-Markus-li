/* Helper functions included in all drivers */
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
            sendEvent(name:"needUpdate", value:"NO", displayed:false, isStateChange: false)
        }
        else
        {
            sendEvent(name:"needUpdate", value:"YES", displayed:false, isStateChange: false)
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

private def getFilteredDeviceDriverName() {
    deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    return deviceDriverName
}

private def getFilteredDeviceDisplayName() {
    device_display_name = device.displayName.replace(' (parent)', '').replace(' (Parent)', '')
    return device_display_name
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
    #!include:getSpecialDebugEntry()
</Value>
</configuration>
'''
}