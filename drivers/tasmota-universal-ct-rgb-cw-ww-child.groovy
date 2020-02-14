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
        capability "LightEffects"                 // Attributes: effectName - STRING, lightEffects - JSON_OBJECT

        //lightEffects = [1: "Effect Name", 2: "Other effect", 3: "etc..."] to JSON...
        attribute  "effectNumber", "number"

        #!include:getMetadataCommandsForHandlingRGBWDevices()
        #!include:getMetadataCommandsForHandlingTasmotaRGBWDevices()
        #!include:getMetadataCommandsForHandlingTasmotaDimmerDevices()
    }

    preferences {
        #!include:getDefaultMetadataPreferences()
        input(name: "hideColorTemperatureCommands", type: "bool", title: addTitleDiv("Hide Color Temperature Commands"), description: addDescriptionDiv("Hides Color Temperature Commands"), defaultValue: false, displayDuringSetup: false, required: false)
        input(name: "hideEffectCommands", type: "bool", title: addTitleDiv("Hide Effect Commands"), description: addDescriptionDiv("Hides Effect Commands"), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "hideColorCommands", type: "bool", title: addTitleDiv("Hide Color Commands"), description: addDescriptionDiv("Hides Color Commands"), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "isAddressable", type: "bool", title: addTitleDiv("Addressable Light"), description: addDescriptionDiv("Treat as an Addressable Light"), defaultValue: false, displayDuringSetup: false, required: false)
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
        } else if(it.name == "effectNumber") {
            logging(it.descriptionText, 100)
            sendEvent(name: "effectName", value: getLightEffectNameByNumber(it.value), isStateChange: false)
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

    metaConfig = setStateVariablesToHide(["mode", "effectnumber"], metaConfig=metaConfig)

    List commandsToHide = ["setEffect", "setNextEffect", "setPreviousEffect"]
    if(hideColorTemperatureCommands == true) {
        commandsToHide.addAll(["setColorTemperature"])
    }
    if(hideEffectCommands == null || hideEffectCommands == true) {
        commandsToHide.addAll(["setEffectWithSpeed", "setNextEffectWithSpeed", "setPreviousEffectWithSpeed", "modeWakeUp", "setEffectSingleColor", "setEffectCycleUpColors", "setEffectCycleDownColors", "setEffectRandomColors"])
    }
    if(hideColorCommands == null || hideColorCommands == true) {
        commandsToHide.addAll(["colorWhite", "colorRed", "colorGreen", "colorBlue", "colorYellow", "colorCyan", "colorPink"])
    }
    if(commandsToHide != []) metaConfig = setCommandsToHide(commandsToHide, metaConfig=metaConfig)
    
    Map lightEffects = [:]
    if(isAddressable == true) {
        lightEffects = [0: "Single Color", 1: "Wake Up", 2: "Cycle Up Colors", 3: "Cycle Down Colors", 
                        4: "Random Colors", 5: "Clock Mode", 6: "Candlelight Pattern", 7: "RGB Pattern",
                        8: "Christmas Pattern", 9: "Hanukkah Pattern", 10: "Kwanzaa Pattern",
                        11: "Rainbow Pattern", 12: "Fire Pattern"]
    } else {
        lightEffects = [0: "Single Color", 1: "Wake Up", 2: "Cycle Up Colors", 3: "Cycle Down Colors", 
                        4: "Random Colors"]
    }
    sendEvent(name: "lightEffects", value: JsonOutput.toJson(lightEffects))
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

void setEffect(BigDecimal effectnumber) {
    setEffectWithSpeed(effectnumber, 2)
}

Map getLightEffects() {
    String lightEffectsJSON = device.currentValue('lightEffects')
    Map lightEffects = [0: "Undefined"]
    if(lightEffectsJSON != null) {
        log.debug "lightEffectsJSON = $lightEffectsJSON"
        JsonSlurper jsonSlurper = new JsonSlurper()
        lightEffects = jsonSlurper.parseText(lightEffectsJSON)
    }
    return lightEffects
}

String getLightEffectNameByNumber(BigDecimal effectnumber) {
    Map lightEffects = getLightEffects()
    lightEffects.get(effectnumber.toString(), 'Unknown')
}

void setNextEffect() {
    setNextEffectWithSpeed(2)
}

void setPreviousEffect() {
    setPreviousEffectWithSpeed(2)
}

void setEffectWithSpeed(BigDecimal effectnumber, BigDecimal speed=3) {
    state.effectnumber = effectnumber
    parent?.componentSetEffect(this.device, effectnumber, speed)
}

void setNextEffectWithSpeed(BigDecimal speed=3) {
    logging("setNextEffectWithSpeed()", 10)
    if (state.effectnumber != null && state.effectnumber < getLightEffects().size() - 1) {
        state.effectnumber = state.effectnumber + 1
    } else {
        state.effectnumber = 0
    }
    setEffectWithSpeed(state.effectnumber, speed)
}

void setPreviousEffectWithSpeed(BigDecimal speed=3) {
    logging("setPreviousEffectWithSpeed()", 10)
    if (state.effectnumber != null && state.effectnumber > 0) {
        state.effectnumber = state.effectnumber - 1
    } else {
        state.effectnumber = getLightEffects().size() - 1
    }
    setEffectWithSpeed(state.effectnumber, speed)
}

void setEffectSingleColor(BigDecimal speed=3) {
    setEffectWithSpeed(0, speed)
}

void setEffectCycleUpColors(BigDecimal speed=3) {
    setEffectWithSpeed(2, speed)
}

void setEffectCycleDownColors(BigDecimal speed=3) {
    setEffectWithSpeed(3, speed)
}

void setEffectRandomColors(BigDecimal speed=3) {
    setEffectWithSpeed(4, speed)
}

void modeWakeUp(BigDecimal wakeUpDuration) {
    Integer level = device.currentValue('level')
    Integer nlevel = level > 10 ? level : 10
    modeWakeUp(wakeUpDuration, nlevel)
}

void modeWakeUp(BigDecimal wakeUpDuration, BigDecimal level) {
    state.effectnumber = 1
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
