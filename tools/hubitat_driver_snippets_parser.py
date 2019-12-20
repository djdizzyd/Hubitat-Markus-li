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

def getGenericTasmotaParseHeader():
    return """// parse() Generic Tasmota-device header BEGINS here
//log.debug "Parsing: ${description}"
def events = []
def descMap = parseDescriptionAsMap(description)
def body
//log.debug "descMap: ${descMap}"

if (!state.mac || state.mac != descMap["mac"]) {
    logging("Mac address of device found ${descMap["mac"]}",1)
    state.mac = descMap["mac"]
}

if (useIPAsID) {
    hexIPAddress = setDeviceNetworkId(ipAddress, true)
    if(hexIPAddress != null && state.dni != hexIPAddress) {
        state.dni = hexIPAddress
        updateDNI()
    }
}
else if (state.mac != null && state.dni != state.mac) { 
    state.dni = setDeviceNetworkId(state.mac)
    updateDNI()
}

if (descMap["body"] && descMap["body"] != "T04=") body = new String(descMap["body"].decodeBase64())

if (body && body != "") {
    if(body.startsWith("{") || body.startsWith("[")) {
        logging("========== Parsing Report ==========",99)
        def slurper = new JsonSlurper()
        def result = slurper.parseText(body)
        
        logging("result: ${result}",0)
        // parse() Generic header ENDS here
        """

def getGenericTasmotaParseFooter():
    return """// parse() Generic Tasmota-device footer BEGINS here
} else {
        //log.debug "Response is not JSON: $body"
    }
}

if (!device.currentValue("ip") || (device.currentValue("ip") != getDataValue("ip"))) events << createEvent(name: 'ip', value: getDataValue("ip"))

return events
// parse() Generic footer ENDS here"""

def getTasmotaParserForBasicData():
    return """
// Standard Basic Data parsing
if (result.containsKey("POWER")) {
    logging("POWER: $result.POWER",99)
    events << createEvent(name: "switch", value: result.POWER.toLowerCase())
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
if (result.containsKey("IPAddress") && override == false) {
    logging("IPAddress: $result.IPAddress",99)
    events << createEvent(name: "ip", value: "$result.IPAddress")
}
if (result.containsKey("WebServerMode")) {
    logging("WebServerMode: $result.WebServerMode",99)
}
if (result.containsKey("Version")) {
    logging("Version: $result.Version",99)
}
if (result.containsKey("Module") && !result.containsKey("Version")) {
    // The check for Version is here to avoid using the wrong message
    logging("Module: $result.Module",50)
    events << createEvent(name: "module", value: "$result.Module")
}
// When it is a Template, it looks a bit different
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
    // Even with "displayed: false, archivable: false" these events still show up under events...
    //events << createEvent(name: 'uptime', value: result.Uptime, displayed: false, archivable: false)
    state.uptime = result.Uptime
}
"""

def getTasmotaParserForWifi():
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
    }
    if (result.Wifi.containsKey("SSId")) {
        logging("SSId: $result.Wifi.SSId",99)
    }
}
"""

def getTasmotaParserForTuyaSwitch():
    return """
// Standard TuyaSwitch Data parsing
if (result.containsKey("POWER1")) {
    logging("POWER1: $result.POWER1",1)
    childSendState("1", result.POWER1.toLowerCase())
    events << createEvent(name: "switch", value: (areAllChildrenSwitchedOn(result.POWER1.toLowerCase() == "on"?1:0) && result.POWER1.toLowerCase() == "on"? "on" : "off"))
}
if (result.containsKey("POWER2")) {
    logging("POWER2: $result.POWER2",1)
    childSendState("2", result.POWER2.toLowerCase())
    events << createEvent(name: "switch", value: (areAllChildrenSwitchedOn(result.POWER2.toLowerCase() == "on"?2:0) && result.POWER2.toLowerCase() == "on"? "on" : "off"))
}
if (result.containsKey("POWER3")) {
    logging("POWER3: $result.POWER3",1)
    childSendState("3", result.POWER3.toLowerCase())
    events << createEvent(name: "switch", value: (areAllChildrenSwitchedOn(result.POWER3.toLowerCase() == "on"?3:0) && result.POWER3.toLowerCase() == "on"? "on" : "off"))
}
if (result.containsKey("POWER4")) {
    logging("POWER4: $result.POWER4",1)
    childSendState("4", result.POWER4.toLowerCase())
    events << createEvent(name: "switch", value: (areAllChildrenSwitchedOn(result.POWER4.toLowerCase() == "on"?4:0) && result.POWER4.toLowerCase() == "on" ? "on" : "off"))
}
"""

def getTasmotaParserForEnergyMonitor():
    return """
// Standard Energy Monitor Data parsing
if (result.containsKey("StatusSNS")) {
    if (result.StatusSNS.containsKey("ENERGY")) {
        if (result.StatusSNS.ENERGY.containsKey("Total")) {
            logging("Total: $result.StatusSNS.ENERGY.Total kWh",99)
            events << createEvent(name: "energyTotal", value: "$result.StatusSNS.ENERGY.Total kWh")
        }
        if (result.StatusSNS.ENERGY.containsKey("Today")) {
            logging("Today: $result.StatusSNS.ENERGY.Today kWh",99)
            events << createEvent(name: "energyToday", value: "$result.StatusSNS.ENERGY.Today kWh")
        }
        if (result.StatusSNS.ENERGY.containsKey("Yesterday")) {
            logging("Yesterday: $result.StatusSNS.ENERGY.Yesterday kWh",99)
            events << createEvent(name: "energyYesterday", value: "$result.StatusSNS.ENERGY.Yesterday kWh")
        }
        if (result.StatusSNS.ENERGY.containsKey("Current")) {
            logging("Current: $result.StatusSNS.ENERGY.Current A",99)
            events << createEvent(name: "current", value: "$result.StatusSNS.ENERGY.Current A")
        }
        if (result.StatusSNS.ENERGY.containsKey("ApparentPower")) {
            logging("apparentPower: $result.StatusSNS.ENERGY.ApparentPower VA",99)
            events << createEvent(name: "apparentPower", value: "$result.StatusSNS.ENERGY.ApparentPower VA")
        }
        if (result.StatusSNS.ENERGY.containsKey("ReactivePower")) {
            logging("reactivePower: $result.StatusSNS.ENERGY.ReactivePower VAr",99)
            events << createEvent(name: "reactivePower", value: "$result.StatusSNS.ENERGY.ReactivePower VAr")
        }
        if (result.StatusSNS.ENERGY.containsKey("Factor")) {
            logging("powerFactor: $result.StatusSNS.ENERGY.Factor",99)
            events << createEvent(name: "powerFactor", value: "$result.StatusSNS.ENERGY.Factor")
        }
        if (result.StatusSNS.ENERGY.containsKey("Voltage")) {
            logging("Voltage: $result.StatusSNS.ENERGY.Voltage V",99)
            events << createEvent(name: "voltage", value: "$result.StatusSNS.ENERGY.Voltage V")
        }
        if (result.StatusSNS.ENERGY.containsKey("Power")) {
            logging("Power: $result.StatusSNS.ENERGY.Power W",99)
            events << createEvent(name: "power", value: "$result.StatusSNS.ENERGY.Power W")
        }
    }
}
if (result.containsKey("ENERGY")) {
    //logging("Has ENERGY...", 1)
    if (result.ENERGY.containsKey("Total")) {
        logging("Total: $result.ENERGY.Total kWh",99)
        events << createEvent(name: "energyTotal", value: "$result.ENERGY.Total kWh")
    }
    if (result.ENERGY.containsKey("Today")) {
        logging("Today: $result.ENERGY.Today kWh",99)
        events << createEvent(name: "energyToday", value: "$result.ENERGY.Today kWh")
    }
    if (result.ENERGY.containsKey("Yesterday")) {
        logging("Yesterday: $result.ENERGY.Yesterday kWh",99)
        events << createEvent(name: "energyYesterday", value: "$result.ENERGY.Yesterday kWh")
    }
    if (result.ENERGY.containsKey("Current")) {
        logging("Current: $result.ENERGY.Current A",99)
        events << createEvent(name: "current", value: "$result.ENERGY.Current A")
    }
    if (result.ENERGY.containsKey("ApparentPower")) {
        logging("apparentPower: $result.ENERGY.ApparentPower VA",99)
        events << createEvent(name: "apparentPower", value: "$result.ENERGY.ApparentPower VA")
    }
    if (result.ENERGY.containsKey("ReactivePower")) {
        logging("reactivePower: $result.ENERGY.ReactivePower VAr",99)
        events << createEvent(name: "reactivePower", value: "$result.ENERGY.ReactivePower VAr")
    }
    if (result.ENERGY.containsKey("Factor")) {
        logging("powerFactor: $result.ENERGY.Factor",99)
        events << createEvent(name: "powerFactor", value: "$result.ENERGY.Factor")
    }
    if (result.ENERGY.containsKey("Voltage")) {
        logging("Voltage: $result.ENERGY.Voltage V",99)
        events << createEvent(name: "voltage", value: "$result.ENERGY.Voltage V")
    }
    if (result.ENERGY.containsKey("Power")) {
        logging("Power: $result.ENERGY.Power W",99)
        events << createEvent(name: "power", value: "$result.ENERGY.Power W")
    }
}
// StatusPTH:[PowerDelta:0, PowerLow:0, PowerHigh:0, VoltageLow:0, VoltageHigh:0, CurrentLow:0, CurrentHigh:0]
"""

def getTasmotaParserForTHMonitor():
    return """
// Standard Energy Monitor Data parsing
resultTH = null
if (result.containsKey("AM2301")) {
    resultTH = result.AM2301
}
if (result.containsKey("BME280")) {
    resultTH = result.BME280
}
if (result.containsKey("BMP280")) {
    resultTH = result.BMP280
}
if (result.containsKey("StatusSNS")) {
    logging("Using key StatusSNS in parse()",1)
    if (result.StatusSNS.containsKey("AM2301")) {
        resultTH = result.StatusSNS.AM2301
    }
    if (result.StatusSNS.containsKey("BME280")) {
        resultTH = result.StatusSNS.BME280
    }
    if (result.StatusSNS.containsKey("BMP280")) {
        resultTH = result.StatusSNS.BMP280
    }
}
if (result.containsKey("SENSOR")) {
    logging("Using key SENSOR in parse()",1)
    if (result.SENSOR.containsKey("AM2301")) {
        resultTH = result.StatusSNS.AM2301
    }
    if (result.SENSOR.containsKey("BME280")) {
        resultTH = result.SENSOR.BME280
    }
    if (result.SENSOR.containsKey("BMP280")) {
        resultTH = result.SENSOR.BMP280
    }
}
if(resultTH != null) {
    if (resultTH.containsKey("Humidity")) {
        logging("Humidity: RH $resultTH.Humidity %",99)
        state.realHumidity = Math.round((resultTH.Humidity as Double) * 100) / 100
        events << createEvent(name: "humidity", value: "${getAdjustedHumidity(state.realHumidity)}", unit: "%")
    }
    if (resultTH.containsKey("Temperature")) {
        //Probably need this line below
        //state.realTemperature = convertTemperatureIfNeeded(resultTH.Temperature.toFloat(), result.TempUnit, 1)
        state.realTemperature = resultTH.Temperature.toFloat()
        logging("Temperature: ${getAdjustedTemp(state.realTemperature? state.realTemperature:0)}",99)
        events << createEvent(name: "temperature", value: "${getAdjustedTemp(state.realTemperature)}", unit: "${location.temperatureScale}")
    }
    if (resultTH.containsKey("Pressure")) {
        logging("Pressure: $resultTH.Pressure $result.PressureUnit",99)
        state.realPressure = Math.round((resultTH.Pressure as Double) * 100) / 100
        adjustedPressure = getAdjustedPressure(state.realPressure)
        events << createEvent(name: "pressure", value: "${adjustedPressure}", unit: "${result.PressureUnit}")
        // Since there is no Pressure tile yet, we need an attribute with the unit...
        events << createEvent(name: "pressureWithUnit", value: "${adjustedPressure} ${result.PressureUnit}")
    }
}
"""


def getGenericZigbeeParseHeader():
    return """// parse() Generic Zigbee-device header BEGINS here
logging("Parsing: ${description}", 10)
def events = []
def msgMap = zigbee.parseDescriptionAsMap(description)
logging("msgMap: ${msgMap}", 10)
// parse() Generic header ENDS here"""

def getGenericZigbeeParseFooter():
    return """// parse() Generic Zigbee-device footer BEGINS here

return events
// parse() Generic footer ENDS here"""
