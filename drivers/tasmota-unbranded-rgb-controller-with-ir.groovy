#!include:getHeaderLicense()

/* Acknowledgements:
 * Inspired by work done by Eric Maycock (erocm123) and damondins.
 */


#!include:getDefaultImports()

metadata {
	definition (name: "Tasmota - Unbranded RGB Controller with IR (EXPERIMENTAL)", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch") {
        capability "Actuator"
		capability "Switch"
		capability "Sensor"
        capability "ColorControl"
        capability "ColorTemperature"
        capability "SwitchLevel"
        #!include:getDefaultMetadataCapabilities()
        
        #!include:getDefaultMetadataAttributes()

        #!include:getDefaultMetadataCommands()
        command "white"
        command "red"
        command "green"
        command "blue"
        command "modeNext"
        command "modePrevious"
        command "modeSingleColor"
        command "modeWakeUp"
        command "modeCycleUpColors"
        command "modeCycleDownColors"
        command "modeRandomColors"
	}

	simulator {
	}
    
    preferences {
        #!include:getDefaultMetadataPreferences()
        #!include:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting, True is default
	}
}

def setColor(value) {
    logging("setColor('${value}')", 10)
	if (value != null && value instanceof Map) {
        def h = value.containsKey("hue") ? value.hue : 0
        def s = value.containsKey("saturation") ? value.saturation : 0
        def b = value.containsKey("level") ? value.level : 0
        setHSB(h, s, b)
    } else {
        logging("setColor('${value}') called with an INVALID argument!", 10)
    }
}

def setColorTemperature(value) {
    logging("setColorTemperature('${value}')", 10)
    sendEvent(name: "colorTemperature", value: value)
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
    logging("setColorTemperature('${t}') ADJUSTED to Mired", 10)
    getAction(getCommandString("CT", "${t}"))
    white()
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
        b = state != null && state.containsKey("level") ? state.saturation : 0
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
    if (hsbcmd == "0,0,100") {
        //state.colorMode = "white"
        //sendEvent(name: "colorMode", value: "CT")
        if(callWhite) return(white(false))
        //return(getAction(getCommandString("hsbcolor", hsbcmd)))
    } else {
        sendEvent(name: "colorMode", value: "RGB")
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
    if (hsbcmd == "${hsbColor[0]},${hsbColor[1]},${hsbColor[2]}") state.colorMode = "white"
    state.hue = hsbColor['hue']
    state.saturation = hsbColor['saturation']
    state.level = hsbColor['level']
    
    return(getAction(getCommandString("Color1", rgbcmd)))
}

def setHue(h) {
    logging("setHue('${h}')", 10)
    return(setHSB(h, null, null))
}

def setSaturation(s) {
    logging("setSaturation('${s}')", 10)
    return(setHSB(null, s, null))
}

def setLevel(b) {
    logging("setLevel('${b}')", 10)
    //return(setHSB(null, null, b))
    return(setLevel(b, 0))
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

def on() {
	logging("on()", 50)
    def cmds = []
    h = null
    s = null
    b = null
    if(state != null) {
        h = state.containsKey("hue") ? state.hue : null
        s = state.containsKey("saturation") ? state.saturation : null
        b = state.containsKey("level") ? state.level : 100
    } else {
        h = null
        s = null
        b = 100
    }
    cmds << setHSB(h, s, b)
    cmds << getAction(getCommandString("Power", "On"))
    return cmds
}

def off() {
    logging("off()", 50)
	def cmds = []
    cmds << getAction(getCommandString("Power", "Off"))
    return cmds
}

#!include:getDeviceInfoFunction()

//#include:getGenericOnOffFunctions()

/* These functions are unique to each driver */
def parse(description) {
    #!include:getGenericTasmotaParseHeader()
            #!include:getTasmotaParserForBasicData()
            #!include:getTasmotaParserForWifi()
            if (result.containsKey("IrReceived")) {
                logging("Using key IrReceived in parse()",1)
                if (result.IrReceived.containsKey("Data")) {
                    irData = result.IrReceived.Data
                    if(irData == '0x00F7C03F') events << on()
                    if(irData == '0x00F740BF') events << off()
                    if(irData == '0x00F7E01F') events << white()
                    if(irData == '0x00F720DF') events << red()
                    if(irData == '0x00F7A05F') events << green()
                    if(irData == '0x00F7609F') events << blue()
                    if(irData == '0x00F728D7') events << yellow()
                    if(irData == '0x00F7A857') events << lightBlue()
                    if(irData == '0x00F76897') events << pink()
                    if(irData == '0x00F7C837') events << setLevel(30, 6)  // Fade button
                    if(irData == '0x00F7E817') events << setLevel(100, 6)  // Smooth button
                    
                    if(state == null || !state.containsKey('level') || state.level == null) state.level = 0
                    if(irData == '0x00F700FF') {
                        events << setLevel(state.level + 5) // Brightness +
                    }
                    if(irData == '0x00F7807F') {
                        events << setLevel(state.level - 5) // Brightness -
                    }
                }
                
            }
            if (result.containsKey("HSBColor")) {
                hsbColor = result.HSBColor.tokenize(",")
                hsbColor[0] = Math.round((hsbColor[0] as Integer) / 3.6)
                hsbColor[1] = hsbColor[1] as Integer
                hsbColor[2] = hsbColor[2] as Integer
                logging("hsbColor: ${hsbColor}", 1)
                if(hue != hsbColor[0] ) events << createEvent(name: "hue", value: hsbColor[0])
                if(saturation != hsbColor[1] ) events << createEvent(name: "saturation", value: hsbColor[1])
                if(level != hsbColor[2] ) events << createEvent(name: "level", value: hsbColor[2])
            }
            if (result.containsKey("Color")) {
                color = result.Color
                logging("Color: ${color.tokenize(",")}", 1)
            }
        #!include:getGenericTasmotaParseFooter()
}

def rgbToHSB(red, green, blue) {
    // All credits for this function goes to Joe Julian (joejulian):
    // https://gist.github.com/joejulian/970fcd5ecf3b792bc78a6d6ebc59a55f
    float r = red / 255f
    float g = green / 255f
    float b = blue / 255f
    float max = [r, g, b].max()
    float min = [r, g, b].min()
    float delta = max - min
    def hue = 0
    def saturation = 0
    if (max == min) {
        hue = 0
    } else if (max == r) {
        def h1 = (g - b) / delta / 6
        def h2 = h1.asType(int)
        if (h1 < 0) {
            hue = (360 * (1 + h1 - h2)).round()
        } else {
            hue = (360 * (h1 - h2)).round()
        }
        logging("rgbToHSB: red max=${max} min=${min} delta=${delta} h1=${h1} h2=${h2} hue=${hue}", 1)
    } else if (max == g) {
        hue = 60 * ((b - r) / delta + 2)
        logging("rgbToHSB: green hue=${hue}", 1)
    } else {
        hue = 60 * ((r - g) / (max - min) + 4)
        logging("rgbToHSB: blue hue=${hue}", 1)
    }
    
    // Convert hue to Hubitat value:
    hue = Math.round((hue) / 3.6)

    if (max == 0) {
        saturation = 0
    } else {
        saturation = delta / max * 100
    }
    
    def level = max * 100
    
    return [
        "hue": hue.asType(int),
        "saturation": saturation.asType(int),
        "level": level.asType(int),
    ]
}

// Fixed colours
def white() {
    logging("white()", 10)
    return(white(true))
}

def white(callSetHSB) {
    logging("white(callSetHSB=${String.valueOf(callSetHSB)})", 10)
    //if(callSetHSB) setHSB(0, 0, 100, false)
    l = state.level
    state.colorMode = "white"
    if (l < 0) l = 0
    l = Math.round(l * 2.55).toInteger()
    if (l > 255) l = 255
    lHex = l.toHexString(l)
    hexCmd = "#${lHex}${lHex}${lHex}${lHex}"
    logging("hexCmd='${hexCmd}'", 1)
    state.red = l
    state.green = l
    state.blue = l
    return(getAction(getCommandString("Color1", hexCmd)))
}

def red() {
    logging("red()", 10)
    return(setRGB(255, 0, 0))
}

def green() {
    logging("green()", 10)
    return(setRGB(0, 255, 0))
}

def blue() {
    logging("blue()", 10)
    return(setRGB(0, 0, 255))
}

def yellow() {
    logging("yellow()", 10)
    return(setRGB(255, 255, 0))
}

def lightBlue() {
    logging("lightBlue()", 10)
    return(setRGB(0, 255, 255))
}

def pink() {
    logging("pink()", 10)
    return(setRGB(255, 0, 255))
}

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
    state.mode = 1
    modeSet(state.mode)
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

def update_needed_settings() {
    #!include:getUpdateNeededSettingsTasmotaHeader()

    #!include:getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, '{"NAME":"RGB Controller","GPIO":[255,255,255,255,255,38,255,255,39,51,255,37,255],"FLAG":15,"BASE":18}')

    // Disabling these here, but leaving them if anyone needs them
    // If another driver has set SetOption81 to 1, the below might be needed, or you can use:
    // http://<device IP>/cm?user=admin&password=<your password>&cmnd=SetOption81%200
    // or without username and password:
    // http://<device IP>/cm?cmnd=SetOption81%200
    //cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)
    //cmds << getAction(getCommandString("LedPower", "1"))  // 1 = turn LED ON and set LedState 8
    //cmds << getAction(getCommandString("LedState", "8"))  // 8 = LED on when Wi-Fi and MQTT are connected.
    
    cmds << getAction(getCommandString("WebLog", "2")) // To avoid errors in the Hubitat logs, make sure this is 2

    #!include:getUpdateNeededSettingsTelePeriod()
    
    #!include:getUpdateNeededSettingsTasmotaFooter()
}

#!include:getDefaultFunctions()

#!include:getLoggingFunction()

#!include:getHelperFunctions('default')

#!include:getHelperFunctions('tasmota')
