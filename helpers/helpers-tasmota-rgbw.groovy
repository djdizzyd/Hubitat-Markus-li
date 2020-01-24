/* Helper functions included in all Tasmota drivers using RGB, RGBW or Dimmers */
def setColorTemperature(value) {
    logging("setColorTemperature('${value}')", 10)
    if(device.currentValue('colorTemperature') != value ) sendEvent(name: "colorTemperature", value: value)
    // 153..500 = set color temperature from 153 (cold) to 500 (warm) for CT lights
    // Tasmota use mired to measure color temperature
    t = value != null ?  (value as Integer) : 0
    // First make sure we have a Kelvin value we can more or less handle
    // 153 mired is approx. 6536K
    // 500 mired = 2000K
    if(t > 6536) t = 6536
    if(t < 2000) t = 2000
    t = Math.round(1000000/t)
    if(t < 153) t = 153
    if(t > 500) t = 500
    state.mired = t
    state.hue = 0
    state.saturation = 0
    state.colorMode = "CT"
    if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
    logging("setColorTemperature('${t}') ADJUSTED to Mired", 10)
    getAction(getCommandString("CT", "${t}"))
}

def setHSB(h, s, b) {
    logging("setHSB('${h}','${s}','${b}')", 10)
    return(setHSB(h, s, b, true))
}

def setHSB(h, s, b, callWhite) {
    logging("setHSB('${h}','${s}','${b}', callWhite=${String.valueOf(callWhite)})", 10)
    adjusted = False
    if(h == null || h == 'NaN') {
        h = state != null && state.containsKey("hue") ? state.hue : 0
        adjusted = True
    }
    if(s == null || s == 'NaN') {
        s = state != null && state.containsKey("saturation") ? state.saturation : 0
        adjusted = True
    }
    if(b == null || b == 'NaN') {
        b = state != null && state.containsKey("level") ? state.level : 0
        adjusted = True
    }
    if(adjusted) {
        logging("ADJUSTED setHSB('${h}','${s}','${b}'", 1)
    }
    adjustedH = Math.round(h*3.6)
    if( adjustedH > 360 ) { adjustedH = 360 }
    if( b < 0 ) b = 0
    if( b > 100 ) b = 100
    hsbcmd = "${adjustedH},${s},${b}"
    logging("hsbcmd = ${hsbcmd}", 1)
    state.hue = h
    state.saturation = s
    state.level = b
    state.colorMode = "RGB"
    if (hsbcmd.startsWith("0,0,")) {
        //state.colorMode = "white"
        //if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
        return(white())
        //return(getAction(getCommandString("hsbcolor", hsbcmd)))
    } else {
        if(device.currentValue("colorMode") != "RGB" ) sendEvent(name: "colorMode", value: "RGB")
        return(getAction(getCommandString("HsbColor", hsbcmd)))
    }
}

def setRGB(r,g,b) {   
    logging("setRGB('${r}','${g}','${b}')", 10)
    adjusted = False
    if(r == null || r == 'NaN') {
        r = 0
        adjusted = True
    }
    if(g == null || g == 'NaN') {
        g = 0
        adjusted = True
    }
    if(b == null || b == 'NaN') {
        b = 0
        adjusted = True
    }
    if(adjusted) {
        logging("ADJUSTED setRGB('${r}','${g}','${b}')", 1)
    }
    rgbcmd = "${r},${g},${b}"
    logging("rgbcmd = ${rgbcmd}", 1)
    state.red = r
    state.green = g
    state.blue = b
    // Calculate from RGB values
    hsbColor = rgbToHSB(r, g, b)
    logging("hsbColor from RGB: ${hsbColor}", 1)
    state.colorMode = "RGB"
    if(device.currentValue("colorMode") != "RGB" ) sendEvent(name: "colorMode", value: "RGB")
    //if (hsbcmd == "${hsbColor[0]},${hsbColor[1]},${hsbColor[2]}") state.colorMode = "white"
    state.hue = hsbColor['hue']
    state.saturation = hsbColor['saturation']
    state.level = hsbColor['level']
    
    return(getAction(getCommandString("Color1", rgbcmd)))
}

def setLevel(l, duration) {
    if (duration == 0) {
        if (state.colorMode == "RGB") {
            return(setHSB(null, null, l))
        } else {
            state.level = l
            return(getAction(getCommandString("Dimmer", "${l}")))
        }
    }
    else if (duration > 0) {
        if (state.colorMode == "RGB") {
            return(setHSB(null, null, l))
        } else {
            if (duration > 10) {duration = 10}
            delay = duration * 10
            fadeCommand = "Fade 1;Speed ${duration};Dimmer ${l};Delay ${delay};Fade 0"
            logging("fadeCommand: '" + fadeCommand + "'", 1)
            return(getAction(getCommandString("Backlog", urlEscape(fadeCommand))))
        }
   }
}

def whiteForPlatform() {
    logging("whiteForPlatform()", 10)
    l = state.level
    //state.colorMode = "white"
    if (l < 10) l = 10
    l = Math.round(l * 2.55).toInteger()
    if (l > 255) l = 255
    lHex = l.toHexString(l)
    hexCmd = "#${lHex}${lHex}${lHex}${lHex}${lHex}"
    logging("hexCmd='${hexCmd}'", 1)
    state.hue = 0
    state.saturation = 0
    state.red = l
    state.green = l
    state.blue = l
    if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
    return(getAction(getCommandString("Color1", hexCmd)))
}

// Functions to set RGBW Mode
def modeSet(mode) {
    logging("modeSet('${mode}')", 10)
    getAction(getCommandString("Scheme", "${mode}"))
}

def modeNext() {
    logging("modeNext()", 10)
    if (state.mode < 4) {
        state.mode = state.mode + 1
    } else {
        state.mode = 0
    }
    modeSet(state.mode)
}

def modePrevious() {
    if (state.mode > 0) {
        state.mode = state.mode - 1
    } else {
        state.mode = 4
    }
    modeSet(state.mode)
}

def modeSingleColor() {
    state.mode = 0
    modeSet(state.mode)
}

def modeWakeUp() {
    logging("modeWakeUp()", 1)
    state.mode = 1
    modeSet(state.mode)
}

def modeWakeUp(wakeUpDuration) {
    level = device.currentValue('level')
    nlevel = level > 10 ? level : 10
    logging("modeWakeUp(wakeUpDuration ${wakeUpDuration}, current level: ${nlevel})", 1)
    modeWakeUp(wakeUpDuration, nlevel)
}

def modeWakeUp(wakeUpDuration, level) {
    logging("modeWakeUp(wakeUpDuration ${wakeUpDuration}, level: ${level})", 1)
    state.mode = 1
    wakeUpDuration = wakeUpDuration < 1 ? 1 : wakeUpDuration > 3000 ? 3000 : wakeUpDuration
    level = level < 1 ? 1 : level > 100 ? 100 : level
    state.level = level
    getAction(getMultiCommandString([[command: "WakeupDuration", value: "${wakeUpDuration}"],
                                    [command: "Wakeup", value: "${level}"]]))
}

def modeCycleUpColors() {
    state.mode = 2
    modeSet(state.mode)
}

def modeCycleDownColors() {
    state.mode = 3
    modeSet(state.mode)
}

def modeRandomColors() {
    state.mode = 4
    modeSet(state.mode)
}