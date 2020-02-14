#!include:getHeaderLicense()

#!include:getDefaultImports()

metadata {
    // Do NOT rename the child driver name unless you also change the corresponding code in the Parent!
    definition (name: "Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        capability "Actuator"
        capability "Switch"                       // Attributes: switch - ENUM ["on", "off"]
        capability "Light"                        // Attributes: switch - ENUM ["on", "off"]
		capability "SwitchLevel"                  // Attributes: level - NUMBER
        capability "ChangeLevel"
        capability "ColorControl"                 // Attributes: RGB - STRING, color - STRING, colorName - STRING, hue - NUMBER, saturation - NUMBER
        capability "ColorTemperature"             // Attributes: colorName - STRING, colorTemperature - NUMBER
        capability "ColorMode"                    // Attributes: colorMode - ENUM ["CT", "RGB"]
        capability "Refresh"

        #!include:getMetadataCommandsForHandlingRGBWDevices()
        #!include:getMetadataCommandsForHandlingTasmotaRGBWDevices()
        #!include:getMetadataCommandsForHandlingTasmotaDimmerDevices()
    }

    preferences {
        #!include:getDefaultMetadataPreferences()
        input(name: "hideColorTemperatureCommands", type: "bool", title: addTitleDiv("Hide Color Temperature Commands"), description: addDescriptionDiv("Hides Color Temperature Commands"), defaultValue: false, displayDuringSetup: false, required: false)
        input(name: "hideModeCommands", type: "bool", title: addTitleDiv("Hide Mode Commands"), description: addDescriptionDiv("Hides Mode Commands"), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "hideColorCommands", type: "bool", title: addTitleDiv("Hide Color Commands"), description: addDescriptionDiv("Hides Color Commands"), defaultValue: true, displayDuringSetup: false, required: false)
    }

    // The below line needs to exist in ALL drivers for custom CSS to work!
    #!include:getMetadataCustomizationMethods()
}

#!include:getDeviceInfoFunction()

/* These functions are unique to each driver */
void parse(List<Map> description) {
    description.each {
        // TODO: Make sure the parent sends RGB and color! Or do we even need these?
        if (it.name in ["switch", "level", "RGB", "color", "colorName", "hue", "saturation",
                        "colorTemperature", "colorMode"]) {
            logging(it.descriptionText, 100)
            sendEvent(it)
        } else {
            log.warn "Got '$it.name' attribute data, but doesn't know what to do with it! Did you choose the right device type?"
        }
    }
}

void updated() {
    log.info "updated()"
    refresh()
}

void installed() {
    log.info "installed()"
    device.removeSetting("logLevel")
    device.updateSetting("logLevel", "100")
    refresh()
}

void refresh() {
    #!include:getChildComponentMetaConfigCommands()

    List commandsToHide = []
    if(hideColorTemperatureCommands == true) {
        commandsToHide.addAll(["setColorTemperature"])
    }
    if(hideModeCommands == null || hideModeCommands == true) {
        commandsToHide.addAll(["modeNext", "modePrevious", "modeSingleColor", "modeCycleUpColors", "modeCycleDownColors", "modeRandomColors"])
    }
    if(hideColorCommands == null || hideColorCommands == true) {
        commandsToHide.addAll(["colorWhite", "colorRed", "colorGreen", "colorBlue", "colorYellow", "colorCyan", "colorPink"])
    }
    if(commandsToHide != []) metaConfig = setCommandsToHide(commandsToHide, metaConfig=metaConfig)
    parent?.componentRefresh(this.device)
}

void on() {
    parent?.componentOn(this.device)
}

void off() {
    parent?.componentOff(this.device)
}

void setLevel(BigDecimal level) {
    parent?.componentSetLevel(this.device, level)
}

void setLevel(BigDecimal level, BigDecimal duration) {
    parent?.componentSetLevel(this.device, level, duration)
}

void startLevelChange(String direction) {
    parent?.componentStartLevelChange(this.device, direction)
}

void stopLevelChange() {
    parent?.componentStopLevelChange(this.device)
}

void setColor(Map colormap) {
    parent?.componentSetColor(this.device, colormap)
}

void setHue(BigDecimal hue) {
    parent?.componentSetHue(this.device, hue)
}

void setSaturation(BigDecimal saturation) {
    parent?.componentSetSaturation(this.device, saturation)
}

void setColorTemperature(BigDecimal colortemperature) {
    parent?.componentSetColorTemperature(this.device, colortemperature)
}

void colorWhite() {
    parent?.componentWhite(this.device)
}

void colorRed() {
    parent?.componentSetRGB(this.device, 255, 0, 0)
}

void colorGreen() {
    parent?.componentSetRGB(this.device, 0, 255, 0)
}

void colorBlue() {
    parent?.componentSetRGB(this.device, 0, 0, 255)
}

void colorYellow() {
    parent?.componentSetRGB(this.device, 255, 255, 0)
}

void colorCyan() {
    parent?.componentSetRGB(this.device, 0, 255, 255)
}

void colorPink() {
    parent?.componentSetRGB(this.device, 255, 0, 255)
}

void modeNext(BigDecimal speed=3) {
    parent?.componentModeNext(this.device, speed)
}

void modePrevious(BigDecimal speed=3) {
    parent?.componentModePrevious(this.device, speed)
}

void modeSingleColor(BigDecimal speed=3) {
    parent?.componentModeSingleColor(this.device, speed)
}

void modeCycleUpColors(BigDecimal speed=3) {
    parent?.componentModeCycleUpColors(this.device, speed)
}

void modeCycleDownColors(BigDecimal speed=3) {
    parent?.componentModeCycleDownColors(this.device, speed)
}

void modeRandomColors(BigDecimal speed=3) {
    parent?.componentModeRandomColors(this.device, speed)
}

void modeWakeUp(BigDecimal wakeUpDuration) {
    Integer level = device.currentValue('level')
    Integer nlevel = level > 10 ? level : 10
    modeWakeUp(wakeUpDuration, nlevel)
}

void modeWakeUp(BigDecimal wakeUpDuration, BigDecimal level) {
    parent?.componentModeWakeUp(this.device, wakeUpDuration, level)
}

/**
 * -----------------------------------------------------------------------------
 * Everything below here are LIBRARY includes and should NOT be edited manually!
 * -----------------------------------------------------------------------------
 * --- Nothing to edit here, move along! ---------------------------------------
 * -----------------------------------------------------------------------------
 */

#!include:getHelperFunctions('all-default')

#!include:getHelperFunctions('driver-metadata')

#!include:getHelperFunctions('styling')

#!include:getLoggingFunction(specialDebugLevel=True)
