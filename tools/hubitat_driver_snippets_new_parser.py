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

def getGenericTasmotaNewParseHeader():
    return """// parse() Generic Tasmota-device header BEGINS here
//logging("Parsing: ${description}", 0)
def events = []
def descMap = parseDescriptionAsMap(description)
def body
//logging("descMap: ${descMap}", 0)

missingChild = false

if (!state.mac || state.mac != descMap["mac"]) {
    logging("Mac address of device found ${descMap["mac"]}",1)
    state.mac = descMap["mac"]
}

prepareDNI()

if (descMap["body"] && descMap["body"] != "T04=") body = new String(descMap["body"].decodeBase64())

if (body && body != "") {
    if(body.startsWith("{") || body.startsWith("[")) {
        logging("========== Parsing Report ==========",99)
        def slurper = new JsonSlurper()
        def result = slurper.parseText(body)
        
        logging("result: ${result}",0)
        // parse() Generic header ENDS here
        """

def getGenericTasmotaNewParseFooter():
    return """// parse() Generic Tasmota-device footer BEGINS here
} else {
        //log.debug "Response is not JSON: $body"
    }
}

if(missingChild == true) {
    log.warn "DISABLED: Missing a child device, refreshing..."
    refresh()
}
if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) {
    curIP = getDataValue("ip")
    logging("Setting IP: $curIP", 1)
    events << createEvent(name: 'ip', value: curIP)
    events << createEvent(name: "ipLink", value: "<a target=\\"device\\" href=\\"http://$curIP\\">$curIP</a>")
}

return events
// parse() Generic footer ENDS here"""

def getTasmotaNewParserForBasicData():
    return """
// Standard Basic Data parsing

if (result.containsKey("StatusNET")) {
    logging("StatusNET: $result.StatusNET",99)
    result << result.StatusNET
}
if (result.containsKey("StatusFWR")) {
    logging("StatusFWR: $result.StatusFWR",99)
    result << result.StatusFWR
}
if (result.containsKey("StatusPRM")) {
    logging("StatusPRM: $result.StatusPRM",99)
    result << result.StatusPRM
}
if (result.containsKey("Status")) {
    logging("Status: $result.Status",99)
    result << result.Status
}
if (result.containsKey("StatusSTS")) {
    logging("StatusSTS: $result.StatusSTS",99)
    result << result.StatusSTS
}
if (result.containsKey("LoadAvg")) {
    logging("LoadAvg: $result.LoadAvg",99)
}
if (result.containsKey("Sleep")) {
    logging("Sleep: $result.Sleep",99)
}
if (result.containsKey("SleepMode")) {
    logging("SleepMode: $result.SleepMode",99)
}
if (result.containsKey("Vcc")) {
    logging("Vcc: $result.Vcc",99)
}
if (result.containsKey("Hostname")) {
    logging("Hostname: $result.Hostname",99)
}
if (result.containsKey("IPAddress") && (override == false || override == null)) {
    logging("IPAddress: $result.IPAddress",99)
    events << createEvent(name: "ip", value: "$result.IPAddress")
    //logging("ipLink: <a target=\\"device\\" href=\\"http://$result.IPAddress\\">$result.IPAddress</a>",10)
    events << createEvent(name: "ipLink", value: "<a target=\\"device\\" href=\\"http://$result.IPAddress\\">$result.IPAddress</a>")
}
if (result.containsKey("WebServerMode")) {
    logging("WebServerMode: $result.WebServerMode",99)
}
if (result.containsKey("Version")) {
    logging("Version: $result.Version",99)
    updateDataValue("firmware", result.Version)
}
if (result.containsKey("Module") && !result.containsKey("Version")) {
    // The check for Version is here to avoid using the wrong message
    logging("Module: $result.Module",50)
    events << createEvent(name: "module", value: "$result.Module")
}
// When it is a Template, it looks a bit different and is NOT valid JSON...
if (result.containsKey("NAME") && result.containsKey("GPIO") && result.containsKey("FLAG") && result.containsKey("BASE")) {  
    n = result.toMapString()
    n = n.replaceAll(', ',',')
    n = n.replaceAll('\\\\[','{').replaceAll('\\\\]','}')
    n = n.replaceAll('NAME:', '"NAME":"').replaceAll(',GPIO:\\\\{', '","GPIO":\\\\[')
    n = n.replaceAll('\\\\},FLAG', '\\\\],"FLAG"').replaceAll('BASE', '"BASE"')
    // TODO: Learn how to do this the right way in Groovy
    logging("Template: $n",50)
    events << createEvent(name: "templateData", value: "${n}")
}
if (result.containsKey("RestartReason")) {
    logging("RestartReason: $result.RestartReason",99)
}
if (result.containsKey("TuyaMCU")) {
    logging("TuyaMCU: $result.TuyaMCU",99)
    events << createEvent(name: "tuyaMCU", value: "$result.TuyaMCU")
}
if (result.containsKey("SetOption81")) {
    logging("SetOption81: $result.SetOption81",99)
}
if (result.containsKey("SetOption113")) {
    logging("SetOption113 (Hubitat enabled): $result.SetOption113",99)
}
if (result.containsKey("Uptime")) {
    logging("Uptime: $result.Uptime",99)
    // Even with "displayed: false, archivable: false" these events still show up under events... There is no way of NOT having it that way...
    //events << createEvent(name: 'uptime', value: result.Uptime, displayed: false, archivable: false)

    state.uptime = result.Uptime
    updateDataValue('uptime', result.Uptime)
}
"""

def getTasmotaNewParserForWifi():
    return """
// Standard Wifi Data parsing
if (result.containsKey("Wifi")) {
    if (result.Wifi.containsKey("AP")) {
        logging("AP: $result.Wifi.AP",99)
    }
    if (result.Wifi.containsKey("BSSId")) {
        logging("BSSId: $result.Wifi.BSSId",99)
    }
    if (result.Wifi.containsKey("Channel")) {
        logging("Channel: $result.Wifi.Channel",99)
    }
    if (result.Wifi.containsKey("RSSI")) {
        logging("RSSI: $result.Wifi.RSSI",99)
        quality = "${dBmToQuality(result.Wifi.RSSI)}%"
        if(device.currentValue('wifiSignal') != quality) events << createEvent(name: "wifiSignal", value: quality)
    }
    if (result.Wifi.containsKey("SSId")) {
        logging("SSId: $result.Wifi.SSId",99)
    }
}
"""

def getTasmotaNewParserForParentSwitch():
    return """
// Standard Switch Data parsing
Integer numSwitchesI = numSwitches.toInteger()
if (result.containsKey("POWER") && result.containsKey("POWER1") == false) {
    logging("parser: POWER (child): $result.POWER",1)
    //events << childSendState("1", result.POWER.toLowerCase())
    missingChild = callChildParseByTypeId("POWER1", [[name:"switch", value: result.POWER.toLowerCase()]], missingChild)
}
(1..16).each {i->
    //logging("POWER$i:${result."POWER$i"} '$result' containsKey:${result.containsKey("POWER$i")}", 1)
    if(result."POWER$i" != null) {
        logging("parser: POWER$i: ${result."POWER$i"}",1)
        missingChild = callChildParseByTypeId("POWER$i", [[name:"switch", value: result."POWER$i".toLowerCase()]], missingChild)
        //events << childSendState("1", result.POWER1.toLowerCase())
        //events << createEvent(name: "switch", value: (areAllChildrenSwitchedOn(result.POWER1.toLowerCase() == "on"?1:0) && result.POWER1.toLowerCase() == "on"? "on" : "off"))
    }
}
"""

def getTasmotaNewParserForEnergyMonitor():
    return """
// Standard Energy Monitor Data parsing
if (result.containsKey("StatusSNS")) {
    result << result.StatusSNS
}
if (result.containsKey("ENERGY")) {
    //logging("Has ENERGY...", 1)
    //if (!state.containsKey('energy')) state.energy = {}
    if (result.ENERGY.containsKey("Total")) {
        logging("Total: $result.ENERGY.Total kWh",99)
        //events << createEvent(name: "energyTotal", value: "$result.ENERGY.Total kWh")
        missingChild = callChildParseByTypeId("POWER1", [[name:"energyTotal", value:"$result.ENERGY.Total kWh"]], missingChild)
    }
    if (result.ENERGY.containsKey("Today")) {
        logging("Today: $result.ENERGY.Today kWh",99)
        //events << createEvent(name: "energyToday", value: "$result.ENERGY.Today kWh")
        missingChild = callChildParseByTypeId("POWER1", [[name:"energyToday", value:"$result.ENERGY.Today kWh"]], missingChild)
    }
    if (result.ENERGY.containsKey("Yesterday")) {
        logging("Yesterday: $result.ENERGY.Yesterday kWh",99)
        //events << createEvent(name: "energyYesterday", value: "$result.ENERGY.Yesterday kWh")
        missingChild = callChildParseByTypeId("POWER1", [[name:"energyYesterday", value:"$result.ENERGY.Yesterday kWh"]], missingChild)
    }
    if (result.ENERGY.containsKey("Current")) {
        logging("Current: $result.ENERGY.Current A",99)
        r = (result.ENERGY.Current == null) ? 0 : result.ENERGY.Current
        //events << createEvent(name: "current", value: "$r A")
        missingChild = callChildParseByTypeId("POWER1", [[name:"current", value:"$r A"]], missingChild)
    }
    if (result.ENERGY.containsKey("ApparentPower")) {
        logging("apparentPower: $result.ENERGY.ApparentPower VA",99)
        //events << createEvent(name: "apparentPower", value: "$result.ENERGY.ApparentPower VA")
        missingChild = callChildParseByTypeId("POWER1", [[name:"apparentPower", value:"$result.ENERGY.ApparentPower VA"]], missingChild)
    }
    if (result.ENERGY.containsKey("ReactivePower")) {
        logging("reactivePower: $result.ENERGY.ReactivePower VAr",99)
        //events << createEvent(name: "reactivePower", value: "$result.ENERGY.ReactivePower VAr")
        missingChild = callChildParseByTypeId("POWER1", [[name:"reactivePower", value:"$result.ENERGY.reactivePower VAr"]], missingChild)
    }
    if (result.ENERGY.containsKey("Factor")) {
        logging("powerFactor: $result.ENERGY.Factor",99)
        //events << createEvent(name: "powerFactor", value: "$result.ENERGY.Factor")
        missingChild = callChildParseByTypeId("POWER1", [[name:"powerFactor", value:"$result.ENERGY.Factor"]], missingChild)
    }
    if (result.ENERGY.containsKey("Voltage")) {
        logging("Voltage: $result.ENERGY.Voltage V",99)
        r = (result.ENERGY.Voltage == null) ? 0 : result.ENERGY.Voltage
        //events << createEvent(name: "voltageWithUnit", value: "$r V")
        //events << createEvent(name: "voltage", value: r, unit: "V")
        missingChild = callChildParseByTypeId("POWER1", [[name:"voltageWithUnit", value:"$r V"]], missingChild)
        missingChild = callChildParseByTypeId("POWER1", [[name:"voltage", value: r, unit: "V"]], missingChild)
    }
    if (result.ENERGY.containsKey("Power")) {
        logging("Power: $result.ENERGY.Power W",99)
        r = (result.ENERGY.Power == null) ? 0 : result.ENERGY.Power
        //events << createEvent(name: "powerWithUnit", value: "$r W")
        //events << createEvent(name: "power", value: r, unit: "W")
        missingChild = callChildParseByTypeId("POWER1", [[name:"powerWithUnit", value:"$r W"]], missingChild)
        missingChild = callChildParseByTypeId("POWER1", [[name:"power", value: r, unit: "W"]], missingChild)
        //state.energy.power = r
    }
}
// StatusPTH:[PowerDelta:0, PowerLow:0, PowerHigh:0, VoltageLow:0, VoltageHigh:0, CurrentLow:0, CurrentHigh:0]
"""

def getTasmotaNewParserForSensors():
    return """
// Standard Sensor Data parsing
// AM2301
// BME280
// BMP280
//logging("result instanceof Map: ${result instanceof Map}", 1)
for ( r in result ) {
    //logging("${r.key} instanceof Map: ${r.value instanceof Map}", 1)
    if((r.key == 'StatusSNS' || r.key == 'SENSOR') && r.value instanceof Map) {
        result << r
    }
}
for ( r in result ) {
    if(r.value instanceof Map && (r.value.containsKey("Humidity") || 
        r.value.containsKey("Temperature") || r.value.containsKey("Pressure") ||
        r.value.containsKey("Distance"))) {
        if (r.value.containsKey("Humidity")) {
            logging("Humidity: RH $r.value.Humidity%", 99)
            realHumidity = Math.round((r.value.Humidity as Double) * 100) / 100
            //events << createEvent(name: "humidity", value: "${getAdjustedHumidity(realHumidity)}", unit: "%")
            missingChild = callChildParseByTypeId(r.key, [[name: "humidity", value: String.format("%.2f", getAdjustedHumidity(realHumidity)), unit: "%"]], missingChild)
        }
        if (r.value.containsKey("Temperature")) {
            //Probably need this line below
            //state.realTemperature = convertTemperatureIfNeeded(r.value.Temperature.toFloat(), result.TempUnit, 1)
            realTemperature = r.value.Temperature.toFloat()
            logging("Temperature: ${getAdjustedTemp(realTemperature? realTemperature:0)}", 99)
            //events << createEvent(name: "temperature", value: "${getAdjustedTemp(realTemperature)}", unit: "&deg;${location.temperatureScale}")
            c = String.valueOf((char)(Integer.parseInt("00B0", 16)));
            missingChild = callChildParseByTypeId(r.key, [[name: "temperature", value: String.format("%.2f", getAdjustedTemp(realTemperature)), unit: "$c${location.temperatureScale}"]], missingChild)
        }
        if (r.value.containsKey("Pressure")) {
            logging("Pressure: $r.value.Pressure", 99)
            pressureUnit = "kPa"
            realPressure = Math.round((r.value.Pressure as Double) * 100) / 100
            adjustedPressure = getAdjustedPressure(realPressure)
            //events << createEvent(name: "pressure", value: "${adjustedPressure}", unit: "${pressureUnit}")
            missingChild = callChildParseByTypeId(r.key, [[name: "pressure", value: String.format("%.2f", adjustedPressure), unit: pressureUnit]], missingChild)
            // Since there is no Pressure tile yet, we need an attribute with the unit...
            //events << createEvent(name: "pressureWithUnit", value: "${adjustedPressure} ${pressureUnit}")
            missingChild = callChildParseByTypeId(r.key, [[name: "pressureWithUnit", value: String.format("%.2f $pressureUnit", adjustedPressure)]], missingChild)
        }
        if (r.value.containsKey("Distance")) {
            logging("Distance: $r.value.Distance cm", 99)
            realDistance = Math.round((r.value.Distance as Double) * 100) / 100
            //events << createEvent(name: "distance", value: "${realDistance}", unit: "cm")
            missingChild = callChildParseByTypeId(r.key, [[name: "distance", value: String.format("%.2f cm", realDistance), unit: "cm"]], missingChild)
        }
        // TODO: Add Distance!
    }
}
"""

def getTasmotaNewParserForRGBWDevice():
    return """
// Standard RGBW Device Data parsing
childDevice = getChildDeviceByActionType("POWER1")
if (result.containsKey("HSBColor")) {
    hsbColor = result.HSBColor.tokenize(",")
    hsbColor[0] = Math.round((hsbColor[0] as Integer) / 3.6)
    hsbColor[1] = hsbColor[1] as Integer
    hsbColor[2] = hsbColor[2] as Integer
    logging("hsbColor: ${hsbColor}", 1)
    if(childDevice.currentValue('hue') != hsbColor[0] ) missingChild = callChildParseByTypeId("POWER1", [[name: "hue", value: hsbColor[0]]], missingChild)
    if(childDevice.currentValue('saturation') != hsbColor[1] ) missingChild = callChildParseByTypeId("POWER1", [[name: "saturation", value: hsbColor[1]]], missingChild)
}
if (result.containsKey("Color")) {
    color = result.Color
    logging("Color: ${color}", 1)
    mode = "RGB"
    if(color.length() > 6 && color.startsWith("000000")) {
        mode = "CT"
    }
    state.colorMode = mode
    if(childDevice.currentValue('colorMode') != mode ) missingChild = callChildParseByTypeId("POWER1", [[name: "colorMode", value: mode]], missingChild)
}
if (result.containsKey("CT")) {
    t = Math.round(1000000/result.CT)
    if(childDevice.currentValue('colorTemperature') != t ) missingChild = callChildParseByTypeId("POWER1", [[name: "colorTemperature", value: t]], missingChild)
    logging("CT: $result.CT ($t)",99)
}
"""

def getTasmotaNewParserForDimmableDevice():
    return """
// Standard Dimmable Device Data parsing
childDevice = getChildDeviceByActionType("POWER1")
if (result.containsKey("Dimmer")) {
    dimmer = result.Dimmer
    logging("Dimmer: ${dimmer}", 1)
    state.level = dimmer
    if(childDevice?.currentValue('level') != dimmer ) missingChild = callChildParseByTypeId("POWER1", [[name: "level", value: dimmer]], missingChild)
}
if (result.containsKey("Wakeup")) {
    wakeup = result.Wakeup
    logging("Wakeup: ${wakeup}", 1)
    //events << createEvent(name: "wakeup", value: wakeup)
}
"""

def getTasmotaNewParserForRGBWIRRemote():
    return """
// Standard RGBW IR Remote Data parsing
if (result.containsKey("IrReceived")) {
    logging("Found key IrReceived in parse()", 1)
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
"""

def getGenericZigbeeParseHeader():
    return """// parse() Generic Zigbee-device header BEGINS here
logging("Parsing: ${description}", 0)
def events = []
def msgMap = zigbee.parseDescriptionAsMap(description)
logging("msgMap: ${msgMap}", 0)
// parse() Generic header ENDS here"""

def getGenericZigbeeParseFooter():
    return """// parse() Generic Zigbee-device footer BEGINS here

return events
// parse() Generic footer ENDS here"""
