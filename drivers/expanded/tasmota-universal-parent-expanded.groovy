 /**
 *  Copyright 2020 Markus Liljergren
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

// BEGIN:getDefaultParentImports()
/* Default Imports */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.security.MessageDigest   // Used for MD5 calculations
/* Default Parent Imports */
// END:  getDefaultParentImports()


metadata {
	definition (name: "Tasmota - Universal Parent", namespace: "tasmota", author: "Markus Liljergren", vid: "generic-switch", importURL: "https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-universal-parent-expanded.groovy") {
        // BEGIN:getDefaultMetadataCapabilities()
        // Default Capabilities
        capability "Refresh"
        capability "Configuration"
        // END:  getDefaultMetadataCapabilities()
        capability "PresenceSensor"
        
        // BEGIN:getDefaultParentMetadataAttributes()
        // Default Parent Attributes
        attribute   "ip", "string"
        attribute   "ipLink", "string"
        attribute   "module", "string"
        attribute   "templateData", "string"
        attribute   "wifiSignal", "string"
        // END:  getDefaultParentMetadataAttributes()
        // BEGIN:getDefaultMetadataAttributes()
        // Default Attributes
        attribute   "driver", "string"
        // END:  getDefaultMetadataAttributes()

        // BEGIN:getMetadataCommandsForHandlingChildDevices()
        // Commands for handling Child Devices
        //command "childOn"
        //command "childOff"
        //command "recreateChildDevices"
        command "deleteChildren"
        // END:  getMetadataCommandsForHandlingChildDevices()
        // BEGIN:getDefaultMetadataCommands()
        // Default Commands
        command "reboot"
        // END:  getDefaultMetadataCommands()
	}

	preferences {
        // BEGIN:getDefaultParentMetadataPreferences()
        // Default Parent Preferences
        input(name: "runReset", description: addDescriptionDiv("For details and guidance, see the release thread in the <a href=\"https://community.hubitat.com/t/release-tasmota-7-x-firmware-with-hubitat-support/29368\"> Hubitat Forum</a>. For settings marked as ADVANCED, make sure you understand what they do before activating them. If settings are not reflected on the device, press the Configure button in this driver. Also make sure all settings really are saved and correct."), title: addTitleDiv("Settings"), displayDuringSetup: false, type: "paragraph", element: "paragraph")
        generate_preferences(configuration_model_debug())
        // END:  getDefaultParentMetadataPreferences()

        input(name: "deviceConfig", type: "enum", title: addTitleDiv("Device Configuration"), 
            description: addDescriptionDiv("Select a Device Configuration (default: Generic Device)<br/>'Generic Device' doesn't configure device Template and/or Module on Tasmota. Child devices and types are auto-detected as well as auto-created and does NOT depend on this setting."), 
            options: getDeviceConfigurationsAsListOption(), defaultValue: "01generic-device", 
            displayDuringSetup: true, required: false)

        // BEGIN:getMetadataPreferencesForHiding()
        // Preferences for Hiding
        input(name: "hideExtended", type: "bool", title: addTitleDiv("Hide Extended Settings"), description: addDescriptionDiv("Hides extended settings, usually not needed."), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "hideAdvanced", type: "bool", title: addTitleDiv("Hide Advanced Settings"), description: addDescriptionDiv("Hides advanced settings, usually not needed anyway."), defaultValue: true, displayDuringSetup: false, required: false)
        // END:  getMetadataPreferencesForHiding()

        // BEGIN:getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting
        // Default Preferences for Tasmota
        generate_preferences(configuration_model_tasmota())
        input(name: "ipAddress", type: "string", title: addTitleDiv("Device IP Address"), description: addDescriptionDiv("Set this as a default fallback for the auto-discovery feature."), displayDuringSetup: true, required: false)
        input(name: "port", type: "number", title: addTitleDiv("Device Port"), description: addDescriptionDiv("The http Port of the Device (default: 80)"), displayDuringSetup: true, required: false, defaultValue: 80)
        input(name: "override", type: "bool", title: addTitleDiv("Override IP"), description: addDescriptionDiv("Override the automatically discovered IP address and disable auto-discovery."), displayDuringSetup: true, required: false)
        input(name: "telePeriod", type: "string", title: addTitleDiv("Update Frequency"), description: addDescriptionDiv("Tasmota sensor value update interval, set this to any value between 10 and 3600 seconds. See the Tasmota docs concerning telePeriod for details. This is NOT a poll frequency. Button/switch changes are immediate and are NOT affected by this. This ONLY affects SENSORS and reporting of data such as UPTIME. (default = 300)"), displayDuringSetup: true, required: false)
        input(name: "disableModuleSelection", type: "bool", title: addTitleDiv("Disable Automatically Setting Module and Template"), description: "ADVANCED: " + addDescriptionDiv("Disable automatically setting the Module Type and Template in Tasmota. Enable for using custom Module or Template settings directly on the device. With this disabled, you need to set these settings manually on the device."), displayDuringSetup: true, required: false)
        input(name: "moduleNumber", type: "number", title: addTitleDiv("Module Number"), description: "ADVANCED: " + addDescriptionDiv("Module Number used in Tasmota. If Device Template is set, this value is IGNORED. (default: -1 (use the default for the driver))"), displayDuringSetup: true, required: false, defaultValue: -1)
        input(name: "deviceTemplateInput", type: "string", title: addTitleDiv("Device Template"), description: "ADVANCED: " + addDescriptionDiv("Set this to a Device Template for Tasmota, leave it EMPTY to use the driver default. Set it to 0 to NOT use a Template. NAME can be maximum 14 characters! (Example: {\"NAME\":\"S120\",\"GPIO\":[0,0,0,0,0,21,0,0,0,52,90,0,0],\"FLAG\":0,\"BASE\":18})"), displayDuringSetup: true, required: false)
        input(name: "useIPAsID", type: "bool", title: addTitleDiv("IP as Network ID"), description: "ADVANCED: " + addDescriptionDiv("Not needed under normal circumstances. Setting this when not needed can break updates. This requires the IP to be static or set to not change in your DHCP server. It will force the use of IP as network ID. When in use, set Override IP to true and input the correct Device IP Address. See the release thread in the Hubitat forum for details and guidance."), displayDuringSetup: true, required: false)
        // END:  getDefaultMetadataPreferencesForTasmota(True) # False = No TelePeriod setting
        // BEGIN:getDefaultMetadataPreferencesLast()
        // Default Preferences - Last
        input(name: "hideDangerousCommands", type: "bool", title: addTitleDiv("Hide Dangerous Commands"), description: addDescriptionDiv("Hides Dangerous Commands, such as 'Delete Children'."), defaultValue: true, displayDuringSetup: false, required: false)
        input(name: "disableCSS", type: "bool", title: addTitleDiv("Disable CSS"), description: addDescriptionDiv("CSS makes the driver more user friendly. Disable the use of CSS in the driver by enabling this. Does NOT affect HE resource usage either way."), defaultValue: false, displayDuringSetup: false, required: false)
        // END:  getDefaultMetadataPreferencesLast()
	}

    // The below line needs to exist in ALL drivers for custom CSS to work!
    // BEGIN:getMetadataCustomizationMethods()
    // Here getPreferences() can be used to get the above preferences
    metaDataExporter()
    if(isCSSDisabled() == false) {
        preferences {
            input(name: "hiddenSetting", description: "" + getDriverCSSWrapper(), title: "None", displayDuringSetup: false, type: "paragraph", element: "paragraph")
        }
    }
    // END:  getMetadataCustomizationMethods()
}

// BEGIN:getDeviceInfoFunction()
public getDeviceInfoByName(infoName) { 
    // DO NOT EDIT: This is generated from the metadata!
    // TODO: Figure out how to get this from Hubitat instead of generating this?
    deviceInfo = ['name': 'Tasmota - Universal Parent', 'namespace': 'tasmota', 'author': 'Markus Liljergren', 'vid': 'generic-switch', 'importURL': 'https://raw.githubusercontent.com/markus-li/Hubitat/release/drivers/expanded/tasmota-universal-parent-expanded.groovy']
    //logging("deviceInfo[${infoName}] = ${deviceInfo[infoName]}", 1)
    return(deviceInfo[infoName])
}
// END:  getDeviceInfoFunction()


/*
    DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)

    Device configurations and functions for using them
*/
TreeMap getDeviceConfigurations() {
    // To add more devices, just add them below ;)
    // Don't forget that BOTH the App and the Universal driver needs to have this configuration.
    // If you add a device and it works well for you, please share your
    // configuration in the Hubitat Community Forum.
    //
    // typeId HAS to be unique
    // 
    // For the rest of the settings, see below to figure it out :P
    deviceConfigurations = [
        [typeId: 'sonoff-basic-r3', 
         name: 'Sonoff Basic R3',
         module: 1,
         installCommands: [["SetOption81", "0"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_basic_R3.html'],

        [typeId: 'tuyamcu-ce-wf500d-dimmer',
         name: 'TuyaMCU CE Smart Home WF500D Dimmer',
         template: '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}',
         installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
         ],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home-WF500D.html'],

        [typeId: 'ce-la-2-w3-wall-outlet',
         name: 'CE Smart Home LA-2-W3 Wall Outlet',
         template: '{"NAME":"CE LA-2-W3","GPIO":[255,255,255,255,157,17,0,0,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LA-2-W3.html'],

        [typeId: 'ce-lq-2-w3-wall-outlet',
         name: 'CE Smart Home LQ-2-W3 Wall Outlet',
         template: '{"NAME":"CE LQ-2-W3","GPIO":[255,255,255,255,255,17,255,255,21,255,255,255,255],"FLAG":15,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/ce_smart_home_LQ-2-W3.html'],

        [typeId: 'awp02l-n-plug',
         name: 'AWP02L-N Plug',
         template: '{"NAME":"AWP02L-N","GPIO":[57,0,56,0,0,0,0,0,0,17,0,21,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/hugoai_awp02l-n.html'],

        [typeId: 'cyyltf-bifans-j23-plug',
         name: 'CYYLTF BIFANS J23 Plug',
         template: '{"NAME":"CYYLTF J23","GPIO":[56,0,0,0,0,0,0,0,21,17,0,0,0],"FLAG":1,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/cyyltd_bifans_J23.html'],

        [typeId: 'gosund-wp3-plug',
         name: 'Gosund WP3 Plug',
         template: '{"NAME":"Gosund WP3","GPIO":[0,0,0,0,17,0,0,0,56,57,21,0,0],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/gosund_wp3.html'],

        [typeId: 'sk03-pm-outdoor-plug',
         name: 'SK03 Power Monitor Outdoor Plug',
         template: '{"NAME":"SK03 Outdoor","GPIO":[17,0,0,0,133,132,0,0,131,57,56,21,0],"FLAG":0,"BASE":57}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/SK03_outdoor.html'],

        [typeId: 'aoycocr-x10s-pm-plug',
         name: 'Aoycocr X10S Power Monitor Plug',
         template: '{"NAME":"Aoycocr X10S","GPIO":[56,0,57,0,21,134,0,0,131,17,132,0,0],"FLAG":0,"BASE":45}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/aoycocr_X10S.html'],

        [typeId: 'brilliant-20699-rgbw-bulb',
         name: 'Brilliant 20699 800lm RGBW Bulb',
         template: '{"NAME":"Brilliant20699","GPIO":[0,0,0,0,141,140,0,0,37,142,0,0,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: 'https://templates.blakadder.com/brilliant_20699.html'],

        [typeId: 'sonoff-sv',
         name: 'Sonoff SV',
         template: '{"NAME":"Sonoff SV","GPIO":[17,255,0,255,255,255,0,0,21,56,255,0,0],"FLAG":1,"BASE":3}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_SV.html'],

        [typeId: 'sonoff-th',
         name: 'Sonoff TH',
         template: '{"NAME":"Sonoff TH","GPIO":[17,255,0,255,255,0,0,0,21,56,255,0,0],"FLAG":0,"BASE":4}',
         installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
         deviceLink: 'https://templates.blakadder.com/sonoff_TH.html'],

        [typeId: 'sonoff-pow',
         name: 'Sonoff POW',
         template: '{"NAME":"Sonoff Pow","GPIO":[17,0,0,0,0,130,0,0,21,132,133,52,0],"FLAG":0,"BASE":6}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_Pow.html'],

        [typeId: 'sonoff-s31',
         name: 'Sonoff S31',
         template: '{"NAME":"Sonoff S31","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":41}',
         installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
         deviceLink: 'https://templates.blakadder.com/sonoff_S31.html'],

        [typeId: 'kmc-4-pm-plug',
         name: 'KMC 4 Power Monitor Plug',
         template: '{"NAME":"KMC 4 Plug","GPIO":[0,56,0,0,133,132,0,0,130,22,23,21,17],"FLAG":0,"BASE":36}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/kmc-4.html'],

        [typeId: 'awp04l-pm-plug',
         name: 'AWP04L Power Monitor Plug',
         template: '{"NAME":"AWP04L","GPIO":[57,255,255,131,255,134,0,0,21,17,132,56,255],"FLAG":0,"BASE":18}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/awp04l.html'],

        [typeId: 'sonoff-4ch-pro',
         name: 'Sonoff 4CH Pro',
         template: '{"NAME":"Sonoff 4CH Pro","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":23}',
         installCommands: [],
         deviceLink: 'https://templates.blakadder.com/sonoff_4CH_Pro.html'],

        [typeId: 'unbranded-rgbwwcw-controller-type-1',
         name: 'Unbranded RGBWWCW Controller (Type 1)',
         template: '{"NAME":"CtrlRGBWWCW-T1","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}',
         installCommands: [["WebLog", "2"]],
         deviceLink: ''],
        
        // Tasmota Drivers WITH their own base-file
        [typeId: 'tuyamcu-touch-switch-1-button',
        name: 'TuyaMCU Touch Switch - 1 button',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,0"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-2-button',
        name: 'TuyaMCU Touch Switch - 2 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,0"], ["TuyaMCU", "14,0"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-3-button',
        name: 'TuyaMCU Touch Switch - 3 buttons',
        module: 54,
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,0"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'tuyamcu-touch-switch-4-button',
        name: 'TuyaMCU Touch Switch - 4 buttons',
        module: 54,
        template: '',
        installCommands: [["TuyaMCU", "11,1"], ["TuyaMCU", "12,2"], 
                          ["TuyaMCU", "13,3"], ["TuyaMCU", "14,4"], ["SetOption81", "1"]],
        deviceLink: ''],

        [typeId: 'sonoff-powr2', 
        name: 'Sonoff POW R2',
        template: '{"NAME":"Sonoff Pow R2","GPIO":[17,145,0,146,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":43}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_Pow_R2.html'],

        [typeId: 'sonoff-s20', 
        name: 'Sonoff S20',
        template: '{"NAME":"Sonoff S20","GPIO":[17,255,255,255,0,0,0,0,21,56,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"], ["LedPower", "1"], ["LedState", "8"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S20.html'],

        [typeId: 'sonoff-s26', 
        name: 'Sonoff S26',
        template: '{"NAME":"Sonoff S26","GPIO":[17,255,255,255,0,0,0,0,21,158,0,0,0],"FLAG":0,"BASE":8}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_S26.html'],

        [typeId: 'sonoff-mini', 
        name: 'Sonoff Mini',
        template: '{"NAME":"Sonoff Mini","GPIO":[17,0,0,0,9,0,0,0,21,56,0,0,255],"FLAG":0,"BASE":1}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_mini.html'],

        [typeId: 'sonoff-basic',
        name: 'Sonoff Basic',
        module: 1,
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/sonoff_basic.html'],

        [typeId: 's120-plug' ,
        name: 'S120 USB Charger Plug',
        template: '{"NAME":"S120 Plug","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'brilliantsmart-20676-plug' ,
        name: 'BrilliantSmart 20676 USB Charger Plug',
        template: '{"NAME":"Brilliant20676","GPIO":[0,0,0,0,0,21,0,0,0,52,90,0,0],"FLAG":0,"BASE":18}',
        installCommands: [["SetOption81", "1"]],
        deviceLink: 'https://templates.blakadder.com/brilliantsmart_20676.html'],

        [typeId: 'brilliant-bl20925-pm-plug', 
        name: 'Brilliant Lighting BL20925 PM Plug',
        template: '{"NAME":"BL20925","GPIO":[0,0,0,17,133,132,0,0,131,158,21,0,0],"FLAG":0,"BASE":52}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/brilliant_BL20925.html'],

        [typeId: 'prime-ccrcwfii113pk-plug', 
        name: 'Prime CCRCWFII113PK Plug',
        template: '{"NAME":"PrimeCCRC13PK","GPIO":[0,0,0,0,57,56,0,0,21,122,0,0,0],"FLAG":0,"BASE":18}',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/prime_CCRCWFII113PK.html'],

        [typeId: 'tuyamcu-wifi-dimmer', 
        name: 'TuyaMCU Wifi Dimmer',
        module: 54,
        installCommands: [["SetOption66", "0"], // Set publishing TuyaReceived to MQTT to DISABLED
        ],
        deviceLink: ''],

        [typeId: 'unbranded-rgb-controller-with-ir-type-1' ,
        name: 'Unbranded RGB Controller with IR (Type 1)',
        template: '{"NAME":"RGB Controller","GPIO":[0,0,0,0,0,38,0,0,39,51,0,37,0],"FLAG":15,"BASE":18}',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: 'sonoff-4ch',
        name: 'Sonoff 4CH',
        template: '{"NAME":"Sonoff 4CH","GPIO":[17,255,255,255,23,22,18,19,21,56,20,24,0],"FLAG":0,"BASE":7}',
        installCommands: [],
       deviceLink: 'https://templates.blakadder.com/sonoff_4CH.html'],

        [typeId: 'tuyamcu-znsn-wifi-curtain-wall-panel',
        comment: 'NOT GENERIC - read the instructions',
        name: 'TuyaMCU ZNSN Wifi Curtain Wall Panel',
        module: 54,
        // TODO: Add special handling for Rule-commands, DON'T use Backlog!
        // TODO: Add the special parsing for this
        installCommands: [["WebLog", "2"],  // A good idea for dimmers
                        ['Mem1', '100'],  // Updated with the current Curtain location
                        ['Mem2', '11'],   // Step for each increase
                        ['Mem3', '1'],    // delay in 10th of a second (1 = 100ms)
                        ['Mem4', '9'],    // Motor startup steps
                        ['Mem5', '1'],    // Extra step when opening
                        ['Delay', '15'],   // Set delay between Backlog commands
                        ['Rule1', 'ON Dimmer#State DO Mem1 %value%; ENDON'],
                        ['Rule1', '+ ON TuyaReceived#Data=55AA00070005650400010277 DO Backlog Var1 %mem1%; Var2 Go; Var5 C; Add1 %mem2%; Sub1 %mem4%; Var4 %mem2%; Event Go; ENDON'],
                        ['Rule1', '+ ON Event#Go DO Backlog Dimmer %var1%; Event %var5%%var1%; Event %var2%2; ENDON'],
                        ['Rule1', '+ ON Event#Go2 DO Backlog Add1 %var4%; Delay %mem3%; Event %var1%; Event %var2%;  ENDON'],
                        ['Rule1', '+ ON Event#O-7 DO Var2 sC; ENDON ON Event#O-8 DO Var2 sC; ENDON ON Event#O-9 DO Var2 sC; ENDON ON Event#O-10 DO Var2 sC; ENDON ON Event#O-11 DO Var2 sC; ENDON'],
                        ['Rule1', '1'],
                        ['Rule2', 'ON TuyaReceived#Data=55AA00070005650400010176 DO Backlog Var1 %mem1%; Var2 Go; Var5 O; Sub1 %mem2%; Add1 %mem4%; Var4 %mem2%; Add4 %mem5%; Mult4 -1; Event Go; ENDON'],
                        ['Rule2', '+ ON Event#sC DO Backlog Var2 sC2; Event sC2; ENDON'],
                        ['Rule2', '+ ON Event#sC2 DO Backlog Var2 sC2; TuyaSend4 101,1; ENDON'],
                        ['Rule2', '+ ON TuyaReceived#Data=55AA00070005650400010075 DO Var2 sC3; ENDON'],
                        ['Rule2', '+ ON Event#C107 DO Var2 sC; ENDON ON Event#C108 DO Var2 sC; ENDON ON Event#C109 DO Var2 sC; ENDON ON Event#C110 DO Var2 sC; END ON ON Event#C111 DO Var2 sC; ENDON'],
                        ['Rule2', '1'],
                        ['Rule3', 'ON Event#C100 DO Var2 sC; ENDON ON Event#C101 DO Var2 sC; ENDON ON Event#C102 DO Var2 sC; ENDON ON Event#C103 DO Var2 sC; ENDON ON Event#C104 DO Var2 sC; ENDON ON Event#C105 DO Var2 sC; ENDON ON Event#C106 DO Var2 sC; ENDON ON Event#O0 DO Var2 sC; ENDON ON Event#O-1 DO Var2 sC; ENDON ON Event#O-2 DO Var2 sC; ENDON ON Event#O-3 DO Var2 sC; ENDON ON Event#O-4 DO Var2 sC; ENDON ON Event#O-5 DO Var2 sC; ENDON ON Event#O-6 DO Var2 sC; ENDON ON Event#O-12 DO Var2 sC; ENDON'],
                        ['Rule3', '1']],
        deviceLink: '',
        open: ["TuyaSend4", "101,0"],
        stop: ["TuyaSend4", "101,1"],
        close: ["TuyaSend4", "101,2"],],
        
        // https://tasmota.github.io/docs/#/devices/Sonoff-RF-Bridge-433pi 
        [typeId: 'sonoff-rf-bridge-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'https://templates.blakadder.com/sonoff_RF_bridge.html'],
        
        [typeId: 'rflink-parent' , 
        notForUniversal: true,
        comment: 'Functional - Need feedback',
        name: '',
        template: '',
        installCommands: [],
        deviceLink: 'http://www.rflink.nl/blog2/wiring'],
        
        // Generic Tasmota Devices:
        [typeId: '01generic-device',
        comment: 'Works with most devices' ,
        name: 'Generic Device',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-switch-plug',
        comment: 'Works as Plug/Outlet with Alexa' ,
        name: 'Generic Switch/Plug',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-switch-light',
         comment: 'Works as Light with Alexa' ,
        name: 'Generic Switch/Light',
        template: '',
        installCommands: [],
        deviceLink: ''],

        [typeId: '01generic-rgb-rgbw-controller-bulb-dimmer', 
        comment: 'RGB+WW+CW should all work properly',
        name: 'Generic RGB/RGBW Controller/Bulb/Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],

        [typeId: '01generic-thp-device' ,
        name: 'Generic Temperature/Humidity/Pressure Device',
        template: '',
        installCommands: [["TempRes", (tempRes == '' || tempRes == null ? "1" : tempRes)]],
        deviceLink: ''],

        [typeId: '01generic-dimmer' ,
        name: 'Generic Dimmer',
        template: '',
        installCommands: [["WebLog", "2"]],
        deviceLink: ''],
    ]

    deviceConfigurationsMap = [:] as TreeMap
    deviceConfigurations.each{
        deviceConfigurationsMap[it["typeId"]] = it
    }
    return deviceConfigurationsMap
}

def getDeviceConfiguration(String typeId) {
    deviceConfigurationsMap = getDeviceConfigurations()
    try{
        return deviceConfigurationsMap[typeId]
    } catch(e) {
        log.warn "Failed to retrieve Device Configuration '$typeId': $e"
        return null
    }
}

def getDeviceConfigurationsAsListOption() {
    deviceConfigurationsMap = getDeviceConfigurations()
    def items = []
    deviceConfigurationsMap.each { k, v ->
        label = v["name"]
        if(v.containsKey("comment") && v["comment"].length() > 0) {
            label += " (${v["comment"]})"
        }
        if(!(v.containsKey("notForUniversal") && v["notForUniversal"] == true)) {
            items << ["${v["typeId"]}":"$label"] 
        }
    }
    return items
}

/*
    --END-- DEVICE CONFIGURATIONS METHODS (helpers-device-configurations)
*/

/* These functions are unique to each driver */

// Called from installed()
def installedAdditional() {
    // This runs from installed()
	logging("installedAdditional()", 50)

    // Do NOT call updatedAdditional() form here!

    //createChildDevices()
}

// Called from updated()
def updatedAdditional() {
    logging("updatedAdditional()", 1)
    //Runs when saving settings
    setDisableCSS(disableCSS)
}

def getDriverCSS() {
    // Executed on page load, put CSS used by the driver here.
    
    // This does NOT execute in the NORMAL scope of the driver!

    r = ""
    // "Data" is available when this runs
    
    //r += getCSSForCommandsToHide(["deleteChildren"])
    //r += getCSSForCommandsToHide(["overSanta", "on", "off"])
    //r += getCSSForStateVariablesToHide(["settings", "mac"])
    //r += getCSSForStateVariablesToHide(["alertMessage", "mac", "dni", "oldLabel"])
    //r += getCSSForCurrentStatesToHide(["templateData", "tuyaMCU", "needUpdate"])
    //r += getCSSForDatasToHide(["metaConfig2", "preferences", "appReturn", "namespace"])
    //r += getCSSToChangeCommandTitle("configure", "Run Configure3")
    //r += getCSSForPreferencesToHide(["numSwitches", "deviceTemplateInput"])
    //r += getCSSForPreferenceHiding('<none>', overrideIndex=getPreferenceIndex('<none>', returnMax=true) + 1)
    //r += getCSSForHidingLastPreference()
    r += '''
    /*form[action*="preference"]::before {
        color: green;
        content: "Hi, this is my content"
    }
    form[action*="preference"] div[for^=preferences] {
        color: blue;
    }*/
    div#stateComment {
        display: inline;
    }
    /*div#stateComment:after {
        color: red;
        display: inline;
        visibility: visible;
        position: absolute;
        bottom: 150%;
        left: 400%;
        white-space: nowrap;
    }*/
    div#stateComment:after {
        color: #382e2b;
        visibility: visible;
        position: relative;
        white-space: nowrap;
        display: inline;
    }
    /*div#stateComment:after {
        color: #382e2b;
        display: inline;
        visibility: visible;
        position: fixed;
        left: 680px;
        white-space: nowrap;
        top: 95px;
    }*/
    /*
    div#stateComment:after {
        color: #5ea767;
        display: inline;
        visibility: visible;
        position: absolute;
        left: 120px;
        white-space: nowrap;
        bottom: -128px;
        height: 36px;
        vertical-align: middle;
    }*/
    div#stateCommentInside {
        display: none;
    }
    li[id*='stateCommentInside'] {
        /*visibility: hidden;*/
        /*position: absolute;*/
        display: list-item;
    }
    '''
    return r
}

def refreshAdditional(metaConfig) {
    
    //logging("this.binding.variables = ${this.binding.variables}", 1)
    //logging("settings = ${settings}", 1)
    //logging("getDefinitionData() = ${getDefinitionData()}", 1)
    //logging("getPreferences() = ${getPreferences()}", 1)
    //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
    //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
    
    metaConfig = setStateVariablesToHide(['mac'], metaConfig=metaConfig)
    logging("hideExtended=$hideExtended, hideAdvanced=$hideAdvanced", 1)
    if(hideExtended == null || hideExtended == true) {
        metaConfig = setPreferencesToHide(['hideAdvanced', 'ipAddress', 'override', 'telePeriod'], metaConfig=metaConfig)
    }
    if(hideExtended == null || hideExtended == true || hideAdvanced == null || hideAdvanced == true) {
        metaConfig = setPreferencesToHide(['disableModuleSelection', 'moduleNumber', 'deviceTemplateInput', 'useIPAsID', 'port', 'disableCSS'], metaConfig=metaConfig)
    }
    if(hideDangerousCommands == null || hideDangerousCommands == true) {
        metaConfig = setCommandsToHide(['deleteChildren'], metaConfig=metaConfig)
    }
    if(deviceConfig == null) deviceConfig = "01generic-device"
    deviceConfigMap = getDeviceConfiguration(deviceConfig)
    logging("deviceConfigMap=$deviceConfigMap", 1)
    try{
        if(deviceConfigMap.containsKey('comment') && 
           deviceConfigMap['comment'] != null &&
           deviceConfigMap['comment'].length() > 0) {
            logging("Settings state.comment...", 1)
            setStateCommentInCSS(deviceConfigMap['comment'], metaConfig=metaConfig) 
            //state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\">${deviceConfigMap['comment']}</div></div>"
            //metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
            state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\"></div></div>"
        } else {
            logging("Hiding state.comment...", 1)
            state.comment = "<div id=\"stateComment\"><div id=\"stateCommentInside\"></div></div>"
            metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
        }
    } catch(e2) {
        log.warn e2
        metaConfig = setStateVariablesToHide(['comment'], metaConfig=metaConfig)
    }

    

    /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
    metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
    metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
    metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/
    //metaConfig = clearThingsToHide()
    //setDisableCSS(false, metaConfig=metaConfig)
    /*metaConfig = setCommandsToHide([])
    metaConfig = setStateVariablesToHide([], metaConfig=metaConfig)
    metaConfig = setCurrentStatesToHide([], metaConfig=metaConfig)
    metaConfig = setDatasToHide([], metaConfig=metaConfig)
    metaConfig = setPreferencesToHide([], metaConfig=metaConfig)*/
}

/* The parse(description) function is included and auto-expanded from external files */
def parse(description) {
    // BEGIN:getGenericTasmotaNewParseHeader()
    // parse() Generic Tasmota-device header BEGINS here
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
    
    // END:  getGenericTasmotaNewParseHeader()
        events << parseResult(result)
    // BEGIN:getGenericTasmotaNewParseFooter()
    // parse() Generic Tasmota-device footer BEGINS here
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
        events << createEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$curIP\">$curIP</a>")
    }
    
    return events
    // parse() Generic footer ENDS here
    // END:  getGenericTasmotaNewParseFooter()
}

def parseResult(result) {

    def events = []
    events << updatePresence("present", createEventCall=true)
    logging("parseResult: $result", 1)
    // BEGIN:getTasmotaNewParserForBasicData()
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
        //logging("ipLink: <a target=\"device\" href=\"http://$result.IPAddress\">$result.IPAddress</a>",10)
        events << createEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$result.IPAddress\">$result.IPAddress</a>")
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
        n = n.replaceAll('\\[','{').replaceAll('\\]','}')
        n = n.replaceAll('NAME:', '"NAME":"').replaceAll(',GPIO:\\{', '","GPIO":\\[')
        n = n.replaceAll('\\},FLAG', '\\],"FLAG"').replaceAll('BASE', '"BASE"')
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
    // END:  getTasmotaNewParserForBasicData()
    // BEGIN:getTasmotaNewParserForParentSwitch()
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
    // END:  getTasmotaNewParserForParentSwitch()
    // BEGIN:getTasmotaNewParserForEnergyMonitor()
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
    // END:  getTasmotaNewParserForEnergyMonitor()
    // BEGIN:getTasmotaNewParserForDimmableDevice()
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
    // END:  getTasmotaNewParserForDimmableDevice()
    // BEGIN:getTasmotaNewParserForRGBWDevice()
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
    // END:  getTasmotaNewParserForRGBWDevice()
    // BEGIN:getTasmotaNewParserForSensors()
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
    // END:  getTasmotaNewParserForSensors()
    // BEGIN:getTasmotaNewParserForWifi()
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
    // END:  getTasmotaNewParserForWifi()

    return events
}

// Call order: installed() -> configure() -> initialize() -> updated() -> update_needed_settings()
def update_needed_settings() {
    // BEGIN:getUpdateNeededSettingsTasmotaHeader()
    // updateNeededSettings() Generic header BEGINS here
    def cmds = []
    def currentProperties = state.currentProperties ?: [:]
    
    state.settings = settings
    
    def configuration = new XmlSlurper().parseText(configuration_model_tasmota())
    def isUpdateNeeded = "NO"
    
    if(runReset != null && runReset == 'RESET') {
        for ( e in state.settings ) {
            logging("Deleting '${e.key}' with value = ${e.value} from Settings", 50)
            // Not sure which ones are needed, so doing all...
            device.clearSetting("${e.key}")
            device.removeSetting("${e.key}")
            state.settings.remove("${e.key}")
        }
    }
    
    prepareDNI()
    
    // updateNeededSettings() Generic header ENDS here
    // END:  getUpdateNeededSettingsTasmotaHeader()

    // Get the Device Configuration
    if(deviceConfig == null) deviceConfig = "01generic-device"
    deviceConfigMap = getDeviceConfiguration(deviceConfig)
    
    deviceTemplateInput = deviceConfigMap?.template
    moduleNumber = deviceConfigMap?.module
    if(deviceTemplateInput == "") deviceTemplateInput = null
    if(moduleNumber == "") moduleNumber = null

    logging("update_needed_settings: deviceConfigMap=$deviceConfigMap, deviceTemplateInput=$deviceTemplateInput, moduleNumber=$moduleNumber", 0)

    // BEGIN:getUpdateNeededSettingsTasmotaDynamicModuleCommand()
    // Tasmota Module and Template selection command (autogenerated)
    cmds << getAction(getCommandString("Module", null))
    cmds << getAction(getCommandString("Template", null))
    if(disableModuleSelection == null) disableModuleSelection = false
    moduleNumberUsed = moduleNumber
    if(moduleNumber == null || moduleNumber == -1) moduleNumberUsed = -1
    useDefaultTemplate = false
    defaultDeviceTemplate = ''
    if(deviceTemplateInput != null && deviceTemplateInput == "0") {
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
    }
    if(deviceTemplateInput == null || deviceTemplateInput == "") {
        // We should use the default of the driver
        useDefaultTemplate = true
        defaultDeviceTemplate = ''
    }
    if(deviceTemplateInput != null) deviceTemplateInput = deviceTemplateInput.replaceAll(' ','')
    if(disableModuleSelection == false && ((deviceTemplateInput != null && deviceTemplateInput != "") ||
                                           (useDefaultTemplate && defaultDeviceTemplate != ""))) {
        if(useDefaultTemplate == false && deviceTemplateInput != null && deviceTemplateInput != "") {
            usedDeviceTemplate = deviceTemplateInput
        } else {
            usedDeviceTemplate = defaultDeviceTemplate
        }
        logging("Setting the Template soon...", 10)
        logging("templateData = ${device.currentValue('templateData')}", 10)
        if(usedDeviceTemplate != '') moduleNumberUsed = 0  // This activates the Template when set
        if(usedDeviceTemplate != null && device.currentValue('templateData') != null && device.currentValue('templateData') != usedDeviceTemplate) {
            logging("The template is NOT set to '${usedDeviceTemplate}', it is set to '${device.currentValue('templateData')}'",10)
            urlencodedTemplate = URLEncoder.encode(usedDeviceTemplate).replace("+", "%20")
            // The NAME part of th Device Template can't exceed 14 characters! More than that and they will be truncated.
            // TODO: Parse and limit the size of NAME
            cmds << getAction(getCommandString("Template", "${urlencodedTemplate}"))
        } else if (device.currentValue('module') == null){
            // Update our stored value!
            cmds << getAction(getCommandString("Template", null))
        }else if (usedDeviceTemplate != null) {
            logging("The template is set to '${usedDeviceTemplate}' already!",10)
        }
    } else {
        logging("Can't set the Template...", 10)
        logging(device.currentValue('templateData'), 10)
        //logging("deviceTemplateInput: '${deviceTemplateInput}'", 10)
        //logging("disableModuleSelection: '${disableModuleSelection}'", 10)
    }
    if(disableModuleSelection == false && moduleNumberUsed != null && moduleNumberUsed >= 0) {
        logging("Setting the Module soon...", 10)
        logging("device.currentValue('module'): '${device.currentValue('module')}'", 10)
        if(moduleNumberUsed != null && device.currentValue('module') != null && !(device.currentValue('module').startsWith("[${moduleNumberUsed}:") || device.currentValue('module') == '0')) {
            logging("This DOESN'T start with [${moduleNumberUsed} ${device.currentValue('module')}",10)
            cmds << getAction(getCommandString("Module", "${moduleNumberUsed}"))
        } else if (moduleNumberUsed != null && device.currentValue('module') != null){
            logging("This starts with [${moduleNumberUsed} ${device.currentValue('module')}",10)
        } else if (device.currentValue('module') == null){
            // Update our stored value!
            cmds << getAction(getCommandString("Module", null))
        } else {
            logging("Module is set to '${device.currentValue('module')}', and it's set to be null, report this to the creator of this driver!",10)
        }
    } else {
        logging("Setting the Module has been disabled!", 10)
    }
    // END:  getUpdateNeededSettingsTasmotaDynamicModuleCommand()

    // TODO: Process device-type specific settings here...

    installCommands = deviceConfigMap?.installCommands
    if(installCommands == null || installCommands == '') installCommands = []
    runInstallCommands(installCommands)

    //
    // https://tasmota.github.io/docs/#/Commands
    //SetOption66
    //Set publishing TuyaReceived to MQTT  »6.7.0
    //0 = disable publishing TuyaReceived over MQTT (default)
    //1 = enable publishing TuyaReceived over MQTT
    //cmds << getAction(getCommandString("SetOption66", "1"))

    //cmds << getAction(getCommandString("SetOption81", "0")) // Set PCF8574 component behavior for all ports as inverted (default=0)

    // BEGIN:getUpdateNeededSettingsTasmotaFooter()
    cmds << getAction(getCommandString("TelePeriod", "${getTelePeriod()}"))
    // updateNeededSettings() Generic footer BEGINS here
    cmds << getAction(getCommandString("SetOption113", "1")) // Hubitat Enabled
    // Disabling Emulation so that we don't flood the logs with upnp traffic
    //cmds << getAction(getCommandString("Emulation", "0")) // Emulation Disabled
    cmds << getAction(getCommandString("HubitatHost", device.hub.getDataValue("localIP")))
    logging("HubitatPort: ${device.hub.getDataValue("localSrvPortTCP")}", 1)
    cmds << getAction(getCommandString("HubitatPort", device.hub.getDataValue("localSrvPortTCP")))
    cmds << getAction(getCommandString("FriendlyName1", URLEncoder.encode(device.displayName.take(32)))) // Set to a maximum of 32 characters
    
    if(override == true) {
        cmds << sync(ipAddress)
    }
    
    //logging("Cmds: " +cmds,1)
    sendEvent(name:"needUpdate", value: isUpdateNeeded, displayed:false, isStateChange: false)
    return cmds
    // updateNeededSettings() Generic footer ENDS here
    // END:  getUpdateNeededSettingsTasmotaFooter()
}

// Calls TO Child devices
Boolean callChildParseByTypeId(String deviceTypeId, event, missingChild) {
    event.each{
        if(it.containsKey("descriptionText") == false) {
            it["descriptionText"] = "'$it.name' set to '$it.value'"
        }
    }
    try {
        cd = getChildDevice("$device.id-$deviceTypeId")
        if(cd != null) {
            cd.parse(event)
        } else {
            // We're missing a device...
            log.warn("childParse() can't FIND the device ${cd?.displayName}! Did you delete something?")
            missingChild = true
        }
    } catch(e) {
        log.warn("childParse() can't send parse event to device ${cd?.displayName}! Error=$e")
        missingChild = true
    }
    return missingChild
}

void childParse(cd, event) {
    try {
        getChildDevice(cd.deviceNetworkId).parse(event)
    } catch(e) {
        log.warn("childParse() can't send parse event to device ${cd?.displayName}! Error=$e")
    }
}

String getDeviceActionType(String childDeviceNetworkId) {
    return childDeviceNetworkId.tokenize("-")[1]
}

// Calls FROM Child devices
void componentOn(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentOn(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    getAction(getCommandString("$actionType", "1"))
    //childParse(cd, [[name:"switch", value:"on", descriptionText:"${cd.displayName} was turned on"]])
}

void componentOff(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentOff(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    getAction(getCommandString("$actionType", "0"))
    //childParse(cd, [[name:"switch", value:"off", descriptionText:"${cd.displayName} was turned off"]])
}

void componentSetColor(cd, value) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColor(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${value}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setColor(value)
}

void componentSetHue(cd, h) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColor(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${h}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setHue(h)
}

void componentSetColorTemperature(cd, value) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetColorTemperature(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${value}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setColorTemperature(value)
}

void componentSetLevel(cd, level) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setLevel(level)
}

void componentSetLevel(cd, level, ramp) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetLevel(cd=${cd.displayName} (${cd.deviceNetworkId}), level=${level}, ramp=${ramp}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setLevel(level, ramp)
}

void componentSetSaturation(cd, s) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentSetSaturation(cd=${cd.displayName} (${cd.deviceNetworkId}), s=${s}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    setSaturation(s)
}

void componentStartLevelChange(cd, direction) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStartLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId}), direction=${direction}) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    startLevelChange(direction)
}

void componentStopLevelChange(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentStopLevelChange(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    stopLevelChange()
}

void componentRefresh(cd) {
    actionType = getDeviceActionType(cd.deviceNetworkId)
    logging("componentRefresh(cd=${cd.displayName} (${cd.deviceNetworkId})) actionType=${getDeviceActionType(cd.deviceNetworkId)}", 1)
    refresh()
}

/*
    -----------------------------------------------------------------------------
    Everything below here are LIBRARY includes and should NOT be edited manually!
    -----------------------------------------------------------------------------
    --- Nothings to edit here, move along! --------------------------------------
    -----------------------------------------------------------------------------
*/

// BEGIN:getDefaultFunctions()
/* Default functions go here */
private def getDriverVersion() {
    //comment = ""
    //if(comment != "") state.comment = comment
    version = "v0.9.5T"
    logging("getDriverVersion() = ${version}", 50)
    sendEvent(name: "driver", value: version)
    updateDataValue('driver', version)
    return version
}
// END:  getDefaultFunctions()


// BEGIN:getGetChildDriverNameMethod()
def getChildDriverName() {
    deviceDriverName = getDeviceInfoByName('name')
    if(deviceDriverName.toLowerCase().endsWith(' (parent)')) {
        deviceDriverName = deviceDriverName.substring(0, deviceDriverName.length()-9)
    }
    childDriverName = "${deviceDriverName} (Child)"
    logging("childDriverName = '$childDriverName'", 1)
    return(childDriverName)
}
// END:  getGetChildDriverNameMethod()


// BEGIN:getLoggingFunction(specialDebugLevel=True)
/* Logging function included in all drivers */
private def logging(message, level) {
    if (infoLogging == true) {
        logLevel = 100
    }
    if (debugLogging == true) {
        logLevel = 1
    }
    if (logLevel != "0"){
        switch (logLevel) {
        case "-1": // Insanely verbose
            if (level >= 0 && level < 100)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "1": // Very verbose
            if (level >= 1 && level < 99)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "10": // A little less
            if (level >= 10 && level < 99)
                log.debug "$message"
            else if (level == 100)
                log.info "$message"
        break
        case "50": // Rather chatty
            if (level >= 50 )
                log.debug "$message"
        break
        case "99": // Only parsing reports
            if (level >= 99 )
                log.debug "$message"
        break
        
        case "100": // Only special debug messages, eg IR and RF codes
            if (level == 100 )
                log.info "$message"
        break
        }
    }
}
// END:  getLoggingFunction(specialDebugLevel=True)


/*
    ALL DEFAULT METHODS (helpers-all-default)

    Helper functions included in all drivers/apps
*/

/*
    ALL DEBUG METHODS (helpers-all-debug)

    Helper Debug functions included in all drivers/apps
*/
def configuration_model_debug() {
    if(!isDeveloperHub()) {
        if(!isDriver()) {
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
        }
        return '''
<configuration>
<Value type="bool" index="debugLogging" label="Enable debug logging" description="" value="true" submitOnChange="true" setting_type="preference" fw="">
<Help></Help>
</Value>
<Value type="bool" index="infoLogging" label="Enable descriptionText logging" description="" value="true" submitOnChange="true" setting_type="preference" fw="">
<Help></Help>
</Value>
</configuration>
'''
    } else {
        if(!isDriver()) {
            app.removeSetting("debugLogging")
            app.updateSetting("debugLogging", "false")
            app.removeSetting("infoLogging")
            app.updateSetting("infoLogging", "false")
        }
        return '''
<configuration>
<Value type="list" index="logLevel" label="Debug Log Level" description="Under normal operations, set this to None. Only needed for debugging. Auto-disabled after 30 minutes." value="-1" submitOnChange="true" setting_type="preference" fw="">
<Help>
</Help>
    <Item label="None" value="0" />
    <Item label="Insanely Verbose" value="-1" />
    <Item label="Very Verbose" value="1" />
    <Item label="Verbose" value="10" />
    <Item label="Reports+Status" value="50" />
    <Item label="Reports" value="99" />
    // BEGIN:getSpecialDebugEntry()
    <Item label="descriptionText" value="100" />
    // END:  getSpecialDebugEntry()
</Value>
</configuration>
'''
    }
}

/*
    --END-- ALL DEBUG METHODS (helpers-all-debug)
*/

def isDriver() {
    try {
        // If this fails, this is not a driver...
        getDeviceDataByName('_unimportant')
        logging("This IS a driver!", 0)
        return true
    } catch (MissingMethodException e) {
        logging("This is NOT a driver!", 0)
        return false
    }
}

def deviceCommand(cmd) {
    def jsonSlurper = new JsonSlurper()
    cmd = jsonSlurper.parseText(cmd)
    logging("deviceCommand: ${cmd}", 0)
    r = this."${cmd['cmd']}"(*cmd['args'])
    logging("deviceCommand return: ${r}", 0)
    updateDataValue('appReturn', JsonOutput.toJson(r))
}

/*
	initialize

	Purpose: initialize the driver/app
	Note: also called from updated()
*/
// Call order: installed() -> configure() -> updated() -> initialize()
void initialize() {
    logging("initialize()", 100)
	unschedule()
    // disable debug logs after 30 min, unless override is in place
	if (logLevel != "0" && logLevel != "100") {
        if(runReset != "DEBUG") {
            log.warn "Debug logging will be disabled in 30 minutes..."
        } else {
            log.warn "Debug logging will NOT BE AUTOMATICALLY DISABLED!"
        }
        runIn(1800, logsOff)
    }
    if(isDriver()) {
        if(!isDeveloperHub()) {
            device.removeSetting("logLevel")
            device.updateSetting("logLevel", "0")
        } else {
            device.removeSetting("debugLogging")
            device.updateSetting("debugLogging", "false")
            device.removeSetting("infoLogging")
            device.updateSetting("infoLogging", "false")
        }
    }
    try {
        // In case we have some more to run specific to this driver/app
        initializeAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
    refresh()
}

/*
	logsOff

	Purpose: automatically disable debug logging after 30 mins.
	Note: scheduled in Initialize()
*/
void logsOff() {
    if(runReset != "DEBUG") {
        log.warn "Debug logging disabled..."
        // Setting logLevel to "0" doesn't seem to work, it disables logs, but does not update the UI...
        //device.updateSetting("logLevel",[value:"0",type:"string"])
        //app.updateSetting("logLevel",[value:"0",type:"list"])
        // Not sure which ones are needed, so doing all... This works!
        if(isDriver()) {
            device.clearSetting("logLevel")
            device.removeSetting("logLevel")
            device.updateSetting("logLevel", "0")
            state.settings.remove("logLevel")
            device.clearSetting("debugLogging")
            device.removeSetting("debugLogging")
            device.updateSetting("debugLogging", "false")
            state.settings.remove("debugLogging")
            
        } else {
            //app.clearSetting("logLevel")
            // To be able to update the setting, it has to be removed first, clear does NOT work, at least for Apps
            app.removeSetting("logLevel")
            app.updateSetting("logLevel", "0")
            app.removeSetting("debugLogging")
            app.updateSetting("debugLogging", "false")
        }
    } else {
        log.warn "OVERRIDE: Disabling Debug logging will not execute with 'DEBUG' set..."
        if (logLevel != "0" && logLevel != "100") runIn(1800, logsOff)
    }
}

def isDeveloperHub() {
    return generateMD5(location.hub.zigbeeId) == "125fceabd0413141e34bb859cd15e067"
    //return false
}

def getEnvironmentObject() {
    if(isDriver()) {
        return device
    } else {
        return app
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

def generate_preferences(configuration_model) {
    def configuration = new XmlSlurper().parseText(configuration_model)
   
    configuration.Value.each {
        if(it.@hidden != "true" && it.@disabled != "true") {
            switch(it.@type) {   
                case "number":
                    input("${it.@index}", "number",
                        title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                        description: makeTextItalic(it.@description),
                        range: "${it.@min}..${it.@max}",
                        defaultValue: "${it.@value}",
                        submitOnChange: it.@submitOnChange == "true",
                        displayDuringSetup: "${it.@displayDuringSetup}")
                    break
                case "list":
                    def items = []
                    it.Item.each { items << ["${it.@value}":"${it.@label}"] }
                    input("${it.@index}", "enum",
                        title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                        description: makeTextItalic(it.@description),
                        defaultValue: "${it.@value}",
                        submitOnChange: it.@submitOnChange == "true",
                        displayDuringSetup: "${it.@displayDuringSetup}",
                        options: items)
                    break
                case "password":
                    input("${it.@index}", "password",
                            title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                            description: makeTextItalic(it.@description),
                            submitOnChange: it.@submitOnChange == "true",
                            displayDuringSetup: "${it.@displayDuringSetup}")
                    break
                case "decimal":
                    input("${it.@index}", "decimal",
                            title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                            description: makeTextItalic(it.@description),
                            range: "${it.@min}..${it.@max}",
                            defaultValue: "${it.@value}",
                            submitOnChange: it.@submitOnChange == "true",
                            displayDuringSetup: "${it.@displayDuringSetup}")
                    break
                case "bool":
                    input("${it.@index}", "bool",
                            title:"${addTitleDiv(it.@label)}" + "${it.Help}",
                            description: makeTextItalic(it.@description),
                            defaultValue: "${it.@value}",
                            submitOnChange: it.@submitOnChange == "true",
                            displayDuringSetup: "${it.@displayDuringSetup}")
                    break
            }
        }
    }
}

/*
    General Mathematical and Number Methods
*/
float round2(float number, int scale) {
    int pow = 10;
    for (int i = 1; i < scale; i++)
        pow *= 10;
    float tmp = number * pow;
    return ( (float) ( (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) ) ) / pow;
}

def generateMD5(String s) {
    MessageDigest.getInstance("MD5").digest(s.bytes).encodeHex().toString()
}

def extractInt(String input) {
  return input.replaceAll("[^0-9]", "").toInteger()
}

/*
    --END-- ALL DEFAULT METHODS (helpers-all-default)
*/

/*
    DRIVER METADATA METHODS (helpers-driver-metadata)

    These methods are to be used in (and/or with) the metadata section of drivers and
    is also what contains the CSS handling and styling.
*/

// These methods can be executed in both the NORMAL driver scope as well
// as the Metadata scope.
private getMetaConfig() {
    // This method can ALSO be executed in the Metadata Scope
    metaConfig = getDataValue('metaConfig')
    if(metaConfig == null) {
        metaConfig = [:]
    } else {
        metaConfig = parseJson(metaConfig)
    }
    return metaConfig
}

def isCSSDisabled(metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    disableCSS = false
    if(metaConfig.containsKey("disableCSS")) disableCSS = metaConfig["disableCSS"]
    return disableCSS
}

// These methods are used to set which elements to hide. 
// They have to be executed in the NORMAL driver scope.


private saveMetaConfig(metaConfig) {
    updateDataValue('metaConfig', JsonOutput.toJson(metaConfig))
}

private setSomethingToHide(String type, something, metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    def oldData = []
    something = something.unique()
    if(!metaConfig.containsKey("hide")) {
        metaConfig["hide"] = [type:something]
    } else {
        //logging("setSomethingToHide 1 else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
        if(metaConfig["hide"].containsKey(type)) {
            //logging("setSomethingToHide 1 hasKey else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
            metaConfig["hide"][type].addAll(something)
        } else {
            //logging("setSomethingToHide 1 noKey else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
            metaConfig["hide"][type] = something
        }
        //metaConfig["hide"]["$type"] = oldData
        //logging("setSomethingToHide 2 else: something: '$something', type:'$type' (${metaConfig["hide"]}) containsKey:${metaConfig["hide"].containsKey(type)}", 1)
    }
    saveMetaConfig(metaConfig)
    logging("setSomethingToHide() = ${metaConfig}", 1)
    return metaConfig
}

private clearTypeToHide(type, metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    something = something.unique()
    if(!metaConfig.containsKey("hide")) {
        metaConfig["hide"] = ["$type":[]]
    } else {
        metaConfig["hide"]["$type"] = []
    }
    saveMetaConfig(metaConfig)
    logging("clearTypeToHide() = ${metaConfig}", 1)
    return metaConfig
}

def clearThingsToHide(metaConfig=null) {
    metaConfig = setSomethingToHide("other", [], metaConfig=metaConfig)
    metaConfig["hide"] = [:]
    saveMetaConfig(metaConfig)
    logging("clearThingsToHide() = ${metaConfig}", 1)
    return metaConfig
}

def setDisableCSS(valueBool, metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["disableCSS"] = valueBool
    saveMetaConfig(metaConfig)
    logging("setDisableCSS(valueBool = $valueBool) = ${metaConfig}", 1)
    return metaConfig
}

def setStateCommentInCSS(stateComment, metaConfig=null) {
    if(metaConfig==null) metaConfig = getMetaConfig()
    metaConfig["stateComment"] = stateComment
    saveMetaConfig(metaConfig)
    logging("setStateCommentInCSS(stateComment = $stateComment) = ${metaConfig}", 1)
    return metaConfig
}

def setCommandsToHide(commands, metaConfig=null) {
    metaConfig = setSomethingToHide("command", commands, metaConfig=metaConfig)
    logging("setCommandsToHide(${commands})", 1)
    return metaConfig
}

def clearCommandsToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("command", metaConfig=metaConfig)
    logging("clearCommandsToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setStateVariablesToHide(stateVariables, metaConfig=null) {
    metaConfig = setSomethingToHide("stateVariable", stateVariables, metaConfig=metaConfig)
    logging("setStateVariablesToHide(${stateVariables})", 1)
    return metaConfig
}

def clearStateVariablesToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("stateVariable", metaConfig=metaConfig)
    logging("clearStateVariablesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setCurrentStatesToHide(currentStates, metaConfig=null) {
    metaConfig = setSomethingToHide("currentState", currentStates, metaConfig=metaConfig)
    logging("setCurrentStatesToHide(${currentStates})", 1)
    return metaConfig
}

def clearCurrentStatesToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("currentState", metaConfig=metaConfig)
    logging("clearCurrentStatesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setDatasToHide(datas, metaConfig=null) {
    metaConfig = setSomethingToHide("data", datas, metaConfig=metaConfig)
    logging("setDatasToHide(${datas})", 1)
    return metaConfig
}

def clearDatasToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("data", metaConfig=metaConfig)
    logging("clearDatasToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

def setPreferencesToHide(preferences, metaConfig=null) {
    metaConfig = setSomethingToHide("preference", preferences, metaConfig=metaConfig)
    logging("setPreferencesToHide(${preferences})", 1)
    return metaConfig
}

def clearPreferencesToHide(metaConfig=null) {
    metaConfig = clearTypeToHide("preference", metaConfig=metaConfig)
    logging("clearPreferencesToHide(metaConfig=${metaConfig})", 1)
    return metaConfig
}

// These methods are for executing inside the metadata section of a driver.
def metaDataExporter() {
    //log.debug "getEXECUTOR_TYPE = ${getEXECUTOR_TYPE()}"
    filteredPrefs = getPreferences()['sections']['input'].name[0]
    //log.debug "filteredPrefs = ${filteredPrefs}"
    if(filteredPrefs != []) updateDataValue('preferences', "${filteredPrefs}".replaceAll("\\s",""))
}

// These methods are used to add CSS to the driver page
// This can be used for, among other things, to hide Commands
// They HAVE to be run in getDriverCSS() or getDriverCSSWrapper()!

/* Example usage:
r += getCSSForCommandsToHide(["off", "refresh"])
r += getCSSForStateVariablesToHide(["alertMessage", "mac", "dni", "oldLabel"])
r += getCSSForCurrentStatesToHide(["templateData", "tuyaMCU", "needUpdate"])
r += getCSSForDatasToHide(["preferences", "appReturn"])
r += getCSSToChangeCommandTitle("configure", "Run Configure2")
r += getCSSForPreferencesToHide(["numSwitches", "deviceTemplateInput"])
r += getCSSForPreferenceHiding('<none>', overrideIndex=getPreferenceIndex('<none>', returnMax=true) + 1)
r += getCSSForHidingLastPreference()
r += '''
form[action*="preference"]::before {
    color: green;
    content: "Hi, this is my content"
}
form[action*="preference"] div.mdl-grid div.mdl-cell:nth-of-type(2) {
    color: green;
}
form[action*="preference"] div[for^=preferences] {
    color: blue;
}
h3, h4, .property-label {
    font-weight: bold;
}
'''
*/

def getDriverCSSWrapper() {
    metaConfig = getMetaConfig()
    disableCSS = isCSSDisabled(metaConfig=metaConfig)
    defaultCSS = '''
    /* This is part of the CSS for replacing a Command Title */
    div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell p::after {
        visibility: visible;
        position: absolute;
        left: 50%;
        transform: translate(-50%, 0%);
        width: calc(100% - 20px);
        padding-left: 5px;
        padding-right: 5px;
        margin-top: 0px;
    }
    /* This is general CSS Styling for the Driver page */
    h3, h4, .property-label {
        font-weight: bold;
    }
    .preference-title {
        font-weight: bold;
    }
    .preference-description {
        font-style: italic;
    }
    '''
    r = "<style>"
    
    if(disableCSS == false) {
        r += "$defaultCSS "
        try{
            // We always need to hide this element when we use CSS
            r += " ${getCSSForHidingLastPreference()} "
            
            if(disableCSS == false) {
                if(metaConfig.containsKey("hide")) {
                    if(metaConfig["hide"].containsKey("command")) {
                        r += getCSSForCommandsToHide(metaConfig["hide"]["command"])
                    }
                    if(metaConfig["hide"].containsKey("stateVariable")) {
                        r += getCSSForStateVariablesToHide(metaConfig["hide"]["stateVariable"])
                    }
                    if(metaConfig["hide"].containsKey("currentState")) {
                        r += getCSSForCurrentStatesToHide(metaConfig["hide"]["currentState"])
                    }
                    if(metaConfig["hide"].containsKey("data")) {
                        r += getCSSForDatasToHide(metaConfig["hide"]["data"])
                    }
                    if(metaConfig["hide"].containsKey("preference")) {
                        r += getCSSForPreferencesToHide(metaConfig["hide"]["preference"])
                    }
                }
                if(metaConfig.containsKey("stateComment")) {
                    r += "div#stateComment:after { content: \"${metaConfig["stateComment"]}\" }"
                }
                r += " ${getDriverCSS()} "
            }
        }catch(MissingMethodException e) {
            if(!e.toString().contains("getDriverCSS()")) {
                log.warn "getDriverCSS() Error: $e"
            }
        } catch(e) {
            log.warn "getDriverCSS() Error: $e"
        }
    }
    r += " </style>"
    return r
}

def getCommandIndex(cmd) {
    commands = device.getSupportedCommands().unique()
    i = commands.findIndexOf{ "$it" == cmd}+1
    //log.debug "getCommandIndex: Seeing these commands: '${commands}', index=$i}"
    return i
}

def getCSSForCommandHiding(cmdToHide) {
    i = getCommandIndex(cmdToHide)
    r = ""
    if(i > 0) {
        r = "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i){display: none;}"
    }
    return r
}

def getCSSForCommandsToHide(commands) {
    r = ""
    commands.each {
        r += getCSSForCommandHiding(it)
    }
    return r
}

def getCSSToChangeCommandTitle(cmd, newTitle) {
    i = getCommandIndex(cmd)
    r = ""
    if(i > 0) {
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p {visibility: hidden;}"
        r += "div.mdl-card__title div.mdl-grid div.mdl-grid .mdl-cell:nth-of-type($i) p::after {content: '$newTitle';}"
    }
    return r
}

def getStateVariableIndex(stateVariable) {
    stateVariables = state.keySet()
    i = stateVariables.findIndexOf{ "$it" == stateVariable}+1
    //log.debug "getStateVariableIndex: Seeing these State Variables: '${stateVariables}', index=$i}"
    return i
}

def getCSSForStateVariableHiding(stateVariableToHide) {
    i = getStateVariableIndex(stateVariableToHide)
    r = ""
    if(i > 0) {
        r = "ul#statev li.property-value:nth-of-type($i){display: none;}"
    }
    return r
}

def getCSSForStateVariablesToHide(stateVariables) {
    r = ""
    stateVariables.each {
        r += getCSSForStateVariableHiding(it)
    }
    return r
}

def getCSSForCurrentStatesToHide(currentStates) {
    r = ""
    currentStates.each {
        r += "ul#cstate li#cstate-$it {display: none;}"
    }
    return r
}

def getDataIndex(data) {
    datas = device.getData().keySet()
    i = datas.findIndexOf{ "$it" == data}+1
    //log.debug "getDataIndex: Seeing these Data Keys: '${datas}', index=$i}"
    return i
}

def getCSSForDataHiding(dataToHide) {
    i = getDataIndex(dataToHide)
    r = ""
    if(i > 0) {
        r = "table.property-list tr li.property-value:nth-of-type($i) {display: none;}"
    }
    return r
}

def getCSSForDatasToHide(datas) {
    r = ""
    datas.each {
        r += getCSSForDataHiding(it)
    }
    return r
}

def getPreferenceIndex(preference, returnMax=false) {
    filteredPrefs = getPreferences()['sections']['input'].name[0]
    //log.debug "getPreferenceIndex: Seeing these Preferences first: '${filteredPrefs}'"
    if(filteredPrefs == [] || filteredPrefs == null) {
        d = getDataValue('preferences')
        //log.debug "getPreferenceIndex: getDataValue('preferences'): '${d}'"
        if(d != null && d.length() > 2) {
            try{
                filteredPrefs = d[1..d.length()-2].tokenize(',')
            } catch(e) {
                // Do nothing
            }
        }
        

    }
    i = 0
    if(returnMax == true) {
        i = filteredPrefs.size()
    } else {
        i = filteredPrefs.findIndexOf{ "$it" == preference}+1
    }
    //log.debug "getPreferenceIndex: Seeing these Preferences: '${filteredPrefs}', index=$i"
    return i
}

def getCSSForPreferenceHiding(preferenceToHide, overrideIndex=0) {
    i = 0
    if(overrideIndex == 0) {
        i = getPreferenceIndex(preferenceToHide)
    } else {
        i = overrideIndex
    }
    r = ""
    if(i > 0) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-of-type($i) {display: none;} "
    }else if(i == -1) {
        r = "form[action*=\"preference\"] div.mdl-grid div.mdl-cell:nth-last-child(2) {display: none;} "
    }
    return r
}

def getCSSForPreferencesToHide(preferences) {
    r = ""
    preferences.each {
        r += getCSSForPreferenceHiding(it)
    }
    return r
}
def getCSSForHidingLastPreference() {
    return getCSSForPreferenceHiding(null, overrideIndex=-1)
}

/*
    --END-- DRIVER METADATA METHODS (helpers-driver-metadata)
*/

/*
    STYLING (helpers-styling)

    Helper functions included in all Drivers and Apps using Styling
*/
def addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

def addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

def makeTextBold(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

def makeTextItalic(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<i>$s</i>"
    } else {
        return "$s"
    }
}

/*
    --END-- STYLING METHODS (helpers-styling)
*/

/*
    DRIVER DEFAULT METHODS (helpers-driver-default)

    General Methods used in ALL drivers except some CHILD drivers
    Though some may have no effect in some drivers, they're here to
    maintain a general structure
*/

// Since refresh, with any number of arguments, is accepted as we always have it declared anyway, 
// we use it as a wrapper
// All our "normal" refresh functions take 0 arguments, we can declare one with 1 here...
def refresh(cmd) {
    deviceCommand(cmd)
}
// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
// Calls installed() -> [configure() -> [update_needed_settings(), updated() -> [updatedAdditional(), initialize() -> refresh() -> refreshAdditional()], installedAdditional()]
def installed() {
	logging("installed()", 100)
    
	configure()
    try {
        // In case we have some more to run specific to this Driver
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

// Call order: installed() -> configure()
def configure() {
    logging("configure()", 100)
    def cmds = []
    if(isDriver()) {
        cmds = update_needed_settings()
        try {
            // Run the getDriverVersion() command
            newCmds = getDriverVersion()
            if (newCmds != null && newCmds != []) cmds = cmds + newCmds
        } catch (MissingMethodException e) {
            // ignore
        }
    }
    if (cmds != []) cmds
}

def update_current_properties(cmd) {
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
    --END-- DRIVER DEFAULT METHODS (helpers-driver-default)
*/

/*
    CHILD DEVICES METHODS (helpers-childDevices)

    Helper functions included when using Child devices
    
    NOTE: MUCH of this is not in use any more, needs cleaning...
*/

// Get the button number
private channelNumber(String dni) {
    def ch = dni.split("-")[-1] as Integer
    return ch
}

def childOn(String dni) {
    // Make sure to create an onOffCmd that sends the actual command
    onOffCmd(1, channelNumber(dni))
}

def childOff(String dni) {
    // Make sure to create an onOffCmd that sends the actual command
    onOffCmd(0, channelNumber(dni))
}

private childSendState(String currentSwitchNumber, String state) {
    def childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${currentSwitchNumber}")}
    if (childDevice) {
        logging("childDevice.sendEvent ${currentSwitchNumber} ${state}",1)
        childDevice.sendEvent(name: "switch", value: state, type: type)
    } else {
        logging("childDevice.sendEvent ${currentSwitchNumber} is missing!",1)
    }
}

private areAllChildrenSwitchedOn(Integer skip = 0) {
    def children = getChildDevices()
    boolean status = true
    Integer i = 1
    children.each {child->
        if (i!=skip) {
  		    if(child.currentState("switch")?.value == "off") {
                status = false
            }
        }
        i++
    }
    return status
}

private sendParseEventToChildren(data) {
    def children = getChildDevices()
    children.each {child->
        child.parseParentData(data)
    }
    return status
}

private void createChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("createChildDevices: creating $numSwitchesI device(s)",1)
    
    // If making changes here, don't forget that recreateDevices need to have the same settings set
    for (i in 1..numSwitchesI) {
        // https://community.hubitat.com/t/composite-devices-parent-child-devices/1925
        // BEGIN:getCreateChildDevicesCommand()
        try {
        addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: true])
                } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                    log.error "'${getChildDriverName()}' driver can't be found! Did you forget to install the child driver?"
                }
        // END:  getCreateChildDevicesCommand()
    }
}

def recreateChildDevices() {
    Integer numSwitchesI = numSwitches.toInteger()
    logging("recreateChildDevices: recreating $numSwitchesI device(s)",1)
    def childDevice = null

    for (i in 1..numSwitchesI) {
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$i")}
        if (childDevice) {
            // The device exists, just update it
            childDevice.setName("${getDeviceInfoByName('name')} #$i")
            childDevice.setDeviceNetworkId("$device.id-$i")  // This doesn't work right now...
            logging(childDevice.getData(), 10)
            // We leave the device Label alone, since that might be desired by the user to change
            //childDevice.setLabel("$device.displayName $i")
            //.setLabel doesn't seem to work on child devices???
        } else {
            // No such device, we should create it
            // BEGIN:getCreateChildDevicesCommand()
            try {
            addChildDevice("${getDeviceInfoByName("namespace")}", "${getChildDriverName()}", "$device.id-$i", [name: "${getFilteredDeviceDriverName()} #$i", label: "${getFilteredDeviceDisplayName()} $i", isComponent: true])
                    } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                        log.error "'${getChildDriverName()}' driver can't be found! Did you forget to install the child driver?"
                    }
            // END:  getCreateChildDevicesCommand()
        }
    }
    if (numSwitchesI < 4) {
        // Check if we should delete some devices
        for (i in 1..4) {
            if (i > numSwitchesI) {
                childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$i")}
                if (childDevice) {
                    logging("Removing child #$i!", 10)
                    deleteChildDevice(childDevice.deviceNetworkId)
                }
            }
        }
    }
}

def deleteChildren() {
	logging("deleteChildren()", 100)
	def children = getChildDevices()
    
    children.each {child->
  		deleteChildDevice(child.deviceNetworkId)
    }
}

/*
    --END-- CHILD DEVICES METHODS (helpers-childDevices)
*/

/*
    TEMPERATURE HUMIDITY METHODS (helpers-temperature-humidity)

    Helper functions included in all drivers with Temperature and Humidity
*/
private getAdjustedTemp(value) {
    if(tempRes == null || tempRes == '') {
        decimalLimit = 10
    } else {
        decimalLimit = 10**(tempRes as Integer) // 10 to the power of tempRes
    }
    value = Math.round((value as Double) * decimalLimit) / decimalLimit
	if (tempOffset) {
	   return value =  value + Math.round(tempOffset * decimalLimit) / decimalLimit
	} else {
       return value
    }
}

private getAdjustedHumidity(value) {
    value = Math.round((value as Double) * 100) / 100

	if (humidityOffset) {
	   return value =  value + Math.round(humidityOffset * 100) / 100
	} else {
       return value
    }
}

private getAdjustedPressure(value) {
    value = Math.round((value as Double) * 100) / 100

	if (pressureOffset) {
	   return value =  value + Math.round(pressureOffset * 100) / 100
	} else {
       return value
    }   
}

/*
    --END-- TEMPERATURE HUMIDITY METHODS (helpers-temperature-humidity)
*/

/*
    TASMOTA METHODS (helpers-tasmota)

    Helper functions included in all Tasmota drivers
*/

// Call order: installed() -> configure() -> updated() -> initialize() -> refresh()
def refresh() {
	logging("refresh()", 100)
    def cmds = []
    
    if(isDriver()) {
        // Clear all old state variables, but ONLY in a driver!
        state.clear()

        // Retrieve full status from Tasmota
        cmds << getAction(getCommandString("Status", "0"), callback="parseConfigureChildDevices")
        getDriverVersion()

        updateDataValue('namespace', getDeviceInfoByName('namespace'))

        //logging("this.binding.variables = ${this.binding.variables}", 1)
        //logging("settings = ${settings}", 1)
        //logging("getDefinitionData() = ${getDefinitionData()}", 1)
        //logging("getPreferences() = ${getPreferences()}", 1)
        //logging("getSupportedCommands() = ${device.getSupportedCommands()}", 1)
        //logging("Seeing these commands: ${device.getSupportedCommands()}", 1)
        
        /*metaConfig = setCommandsToHide(["on", "hiAgain2", "on"])
        metaConfig = setStateVariablesToHide(["uptime"], metaConfig=metaConfig)
        metaConfig = setCurrentStatesToHide(["needUpdate"], metaConfig=metaConfig)
        metaConfig = setDatasToHide(["namespace"], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide(["port"], metaConfig=metaConfig)*/

        // This should be the first place we access metaConfig here, so clear and reset...
        metaConfig = clearThingsToHide()
        metaConfig = setCommandsToHide([], metaConfig=metaConfig)
        metaConfig = setStateVariablesToHide(['settings', 'colorMode', 'red', 'green', 'blue', 
            'mired', 'level', 'saturation', 'mode', 'hue'], metaConfig=metaConfig)
        
        metaConfig = setCurrentStatesToHide(['needUpdate'], metaConfig=metaConfig)
        //metaConfig = setDatasToHide(['preferences', 'namespace', 'appReturn', 'metaConfig'], metaConfig=metaConfig)
        metaConfig = setDatasToHide(['namespace', 'appReturn'], metaConfig=metaConfig)
        metaConfig = setPreferencesToHide([], metaConfig=metaConfig)
    }
    try {
        // In case we have some more to run specific to this driver
        refreshAdditional(metaConfig)
    } catch (MissingMethodException e1) {
        // ignore
        try {
            // In case we have some more to run specific to this driver
            refreshAdditional()
        } catch (MissingMethodException e2) {
            // ignore
        }
    }
    return cmds
}

def reboot() {
	logging("reboot()", 10)
    getAction(getCommandString("Restart", "1"))
}

// Call order: installed() -> configure() -> updated() 
def updated() {
    logging("updated()", 10)
    def cmds = [] 
    if(isDriver()) {
        cmds = update_needed_settings()
        //sendEvent(name: "checkInterval", value: 2 * 15 * 60 + 2 * 60, displayed: false, data: [protocol: "lan", hubHardwareId: device.hub.hardwareID])
        sendEvent(name:"needUpdate", value: device.currentValue("needUpdate"), displayed:false, isStateChange: false)
    }
    logging(cmds, 0)
    try {
        // Also run initialize(), if it exists...
        initialize()
        updatedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
    if (cmds != [] && cmds != null) cmds
}

/*
    // Stress-testing runInstallCommands() 
    installCommands = []
    installCommands.add(["rule1", 'ON Var1#Value DO Var4 0; ENDON'])
    installCommands.add(["rule2", 'ON Var2#Value DO Var4 0; ENDON'])
    installCommands.add(["rule3", 'ON Var3#Value DO Var4 0; ENDON'])
    installCommands.add(["var1", "0"])
    installCommands.add(["var2", "0"])
    installCommands.add(["var3", "0"])
    (1..8).each {
        installCommands.add(["rule1", "+ ON Var1#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule2", "+ ON Var2#Value DO Var4 $it; ENDON"])
        installCommands.add(["rule3", "+ ON Var3#Value DO Var4 $it; ENDON"])
        installCommands.add(["add1", "1"])
        installCommands.add(["add2", "1"])
        installCommands.add(["add3", "1"])
    }
    installCommands.add(["rule1", '0'])
    installCommands.add(["rule2", '0'])
    installCommands.add(["rule3", '0'])
    logging("refreshAdditional installCommands=$installCommands", 1)
    runInstallCommands(installCommands)
*/
def runInstallCommands(installCommands) {
    // Runs install commands as defined in helpers-device-configurations
    // Called from update_needed_settings() in parent drivers
    logging("runInstallCommands(installCommands=$installCommands)", 1)
    def cmds = []
    backlogs = []
    rule1 = []
    rule2 = []
    rule3 = []
    installCommands.each {cmd->
        if(cmd[0].toLowerCase() == "rule1") {
            rule1.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else if(cmd[0].toLowerCase() == "rule2") {
            rule2.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else if(cmd[0].toLowerCase() == "rule3") {
            rule3.add([command: cmd[0], value:urlEscape(cmd[1])])
        } else {
            backlogs.add([command: cmd[0], value:urlEscape(cmd[1])])
        }
    }

    // Backlog inter-command delay in milliseconds
    cmds << getAction(getCommandString("SetOption34", "20"))
    pauseExecution(100)
    // Maximum 30 commands per backlog call
    while(backlogs.size() > 0) {
        cmds << getAction(getMultiCommandString(backlogs.take(10)))
        backlogs = backlogs.drop(10)
        // If we run this too fast Tasmota can't keep up, 1000ms is enough when 20ms between commands...
        if(backlogs.size() > 0) pauseExecution(1000)
        // REALLY don't use pauseExecution often... NOT good for performance...
    }

    [rule1, rule2, rule3].each {
        //logging("rule: $it", 1)
        it.each {rule->
            // Rules can't run in backlog!
            cmds << getAction(getCommandString(rule["command"], rule["value"]))
            //logging("cmd=${rule["command"]}, value=${rule["value"]}", 1)
            pauseExecution(100)
            // REALLY don't use pauseExecution often... NOT good for performance...
        }
    }
    cmds << getAction(getCommandString("SetOption34", "200"))
}

def updatePresence(String presence, createEventCall=false) {
    // presence - ENUM ["present", "not present"]
    if(presence == "present") {
        timeout = getTelePeriod()
        timeout += (timeout * 0.1 > 60 ? Math.round(timeout * 0.1) : 60)
        //log.warn "Setting as present with timeout: $timeout"
        runIn(timeout, "updatePresence", [data: "not present"])
    } else {
        log.warn "Presence time-out reached, setting device as 'not present'!"
    }
    if(createEventCall == true) {
        return createEvent(name: "presence", value: presence)
    } else {
        return sendEvent(name: "presence", value: presence)
    }
}

def parseDescriptionAsMap(description) {
    // Used by parse(description) to get descMap
	description.split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
        
        if (nameAndValue.length == 2) map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
        else map += [(nameAndValue[0].trim()):""]
	}
}

private getAction(uri, callback="parse") { 
    logging("Using getAction for '${uri}'...", 0)
    return httpGetAction(uri, callback=callback)
}

def parse(asyncResponse, data) {
    // Parse called by default when using asyncHTTP
    def events = []
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            events << parseResult(asyncResponse.getJson())
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
    return events
}


/*
    Methods related to configureChildDevices()

    configureChildDevices() detects which child devices to create/update and does the creation/updating
*/

def parseConfigureChildDevices(asyncResponse, data) {
    if(asyncResponse != null) {
        try{
            logging("parse(asyncResponse.getJson() 2= \"${asyncResponse.getJson()}\", data = \"${data}\")", 1)
            configureChildDevices(asyncResponse, data)
        } catch(MissingMethodException e1) {
            log.error e1
        } catch(e1) {
            try{
                logging("parse(asyncResponse.data = \"${asyncResponse.data}\", data = \"${data}\") e1=$e1", 1)
            } catch(e2) {
                logging("parse(asyncResponse.data = null, data = \"${data}\") Is the device online? e2=$e2", 1)
            }
        }
    } else {
        logging("parse(asyncResponse.data = null, data = \"${data}\")", 1)
    }
}

def containsKeyInSubMap(aMap, key) {
    hasKey = false
    aMap.find {
        try{
            hasKey = it.value.containsKey(key)
        } catch(e) {

        }
        hasKey == true
    }
    return hasKey
}

def numOfKeyInSubMap(aMap, key) {
    numKeys = 0
    aMap.each {
        try{
            if(it.value.containsKey(key)) numKeys += 1
        } catch(e) {
            // Do nothing
        }
    }
    return numKeys
}

def numOfKeysIsMap(aMap) {
    numKeys = 0
    aMap.each {
        if(it.value instanceof java.util.Map) numKeys += 1
    }
    return numKeys
}

TreeMap getKeysWithMapAndId(aMap) {
    def foundMaps = [:] as TreeMap
    aMap.each {
        if(it.value instanceof java.util.Map) {
            foundMaps[it.key] = it.value
        }
    }
    return foundMaps
}

def configureChildDevices(asyncResponse, data) {
    // This detects which child devices to create/update and does the creation/updating
    def statusMap = asyncResponse.getJson()
    logging("configureChildDevices() statusMap=$statusMap", 1)
    // Use statusMap to determine which Child Devices we should create

    // The built-in Generic Components are:
    //
    // Acceleration Sensor
    // Contact Sensor
    // Contact/Switch
    // CT
    // Dimmer
    // Metering Switch
    // Motion Sensor
    // RGB
    // RGBW
    // Smoke Detector
    // Switch
    // Temperature Sensor
    // Water Sensor

    // {"StatusSTS":{"Time":"2020-01-26T01:13:27","Uptime":"15T02:59:27","UptimeSec":1306767,
    // "Heap":26,"SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER1":"OFF",
    // "POWER2":"OFF","POWER3":"OFF","POWER4":"OFF","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:CF:11","Channel":1,"RSSI":62,"LinkCount":37,"Downtime":"0T00:05:48"}}}

    // With a dimmer:
    // {"StatusSTS":{"Time":"2020-01-26T11:58:10","Uptime":"0T00:01:20","UptimeSec":80,"Heap":26,
    // "SleepMode":"Dynamic","Sleep":50,"LoadAvg":19,"MqttCount":0,"POWER":"OFF","Dimmer":0,
    // "Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB+CW+WW light:
    // {"StatusSTS":{"Time":"2020-01-26T12:07:57","Uptime":"0T00:06:58","UptimeSec":418,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF62","HSBColor":"0,0,0","Channel":[0,0,0,100,38],"CT":250,"Scheme":0,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":96,"LinkCount":1,"Downtime":"0T00:00:06"}}}
    

    // With an RGB+W light:
    // {"StatusSTS":{"Time":"2020-01-26T12:11:56","Uptime":"0T00:00:26","UptimeSec":26,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"000000FF","HSBColor":"0,0,0","Channel":[0,0,0,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":90,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With an RGB light:
    // {"StatusSTS":{"Time":"2020-01-26T12:14:15","Uptime":"0T00:00:19","UptimeSec":19,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FFFFFF","HSBColor":"0,0,100","Channel":[100,100,100],"Scheme":0,"Fade":"ON",
    // "Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":98,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW+WW ("CT" is available):
    // {"StatusSTS":{"Time":"2020-01-26T12:16:48","Uptime":"0T00:00:17","UptimeSec":17,"Heap":28,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":100,
    // "Color":"FF62","HSBColor":"0,0,0","Channel":[100,38],"CT":250,"Fade":"ON","Speed":10,
    // "LedTable":"ON","Wifi":{"AP":1,"SSId":"network","BSSId":"4A:11:11:12:D9:11",
    // "Channel":1,"RSSI":94,"LinkCount":1,"Downtime":"0T00:00:06"}}}

    // With CW or WW (PWM1 configured on the correct pin), just the same as a normal dimmer...
    // {"StatusSTS":{"Time":"2020-01-26T12:19:51","Uptime":"0T00:01:15","UptimeSec":75,"Heap":27,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":99,"MqttCount":0,"POWER":"ON","Dimmer":71,
    // "Fade":"ON","Speed":10,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":88,"LinkCount":1,"Downtime":"0T00:00:25"}}}

    // Addressable RGB light (has the attribute "Width")
    // {"StatusSNS":{"Time":"2020-01-26T12:57:30","SR04":{"Distance":8.579}}}
    // {"StatusSTS":{"Time":"2020-01-26T12:57:30","Uptime":"0T00:02:14","UptimeSec":134,"Heap":21,
    // "SleepMode":"Dynamic","Sleep":10,"LoadAvg":113,"MqttCount":0,"POWER1":"ON","POWER2":"ON",
    // "Dimmer":100,"Color":"00FF00","HSBColor":"120,100,100","Channel":[0,100,0],"Scheme":13,
    // "Width":2,"Fade":"OFF","Speed":1,"LedTable":"ON","Wifi":{"AP":1,"SSId":"network",
    // "BSSId":"4A:11:11:12:D9:11","Channel":1,"RSSI":100,"Signal":-40,"LinkCount":1,
    // "Downtime":"0T00:00:09"}}}

    // {"StatusSNS":{"Time":"2020-01-26T01:24:16","BMP280":{"Temperature":23.710,"Pressure":1017.6},
    // "PressureUnit":"hPa","TempUnit":"C"}}

    // Multiple temperature sensors:
    // {"Time":"2020-01-26T17:45:30","DS18B20-1":{"Id":"000008BD38BF","Temperature":26.1},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":25.1},"DS18B20-3":{"Id":"000008C02C3A",
    // "Temperature":25.3},"TempUnit":"C"}
    
    // For DS18B20, us ID to distinguish them? Then you can't replace them...
    // For AM2301 the GPIO used is appended.
    // {"StatusSNS":{"Time":"2020-01-26T20:54:10","DS18B20-1":{"Id":"000008BD38BF","Temperature":25.8},
    // "DS18B20-2":{"Id":"000008BD9714","Temperature":24.7},"DS18B20-3":{"Id":"000008C02C3A","Temperature":24.9},
    // "AM2301-12":{"Temperature":25.1,"Humidity":66.4},"AM2301-14":{"Temperature":null,"Humidity":null},"TempUnit":"C"}}

    // D5 = GPIO14
    // D6 = GPIO12
    // D7 = GPIO13

    // Distance Sensor
    // {"StatusSNS":{"Time":"2020-01-26T13:52:19","SR04":{"Distance":11.667}}}

    // {"NAME":"ControlRGBWWCW","GPIO":[17,0,0,0,0,40,0,0,38,39,37,41,0],"FLAG":0,"BASE":18}

    // result: [Time:2020-01-30T11:30:43, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // result: [Time:2020-01-30T11:31:12, DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4], AM2301-12:[Temperature:24.2, Humidity:68.0], AM2301-14:[Temperature:24.0, Humidity:68.1], TempUnit:C]
    // [hasEnergy:false, numTemperature:5, numHumidity:2, numPressure:0, numDistance:0, sensorMap:[AM2301-12:[Temperature:24.2, Humidity:68.1], AM2301-14:[Temperature:24.0, Humidity:68.1], DS18B20-1:[Id:000008BD38BF, Temperature:25.3], DS18B20-2:[Id:000008BD9714, Temperature:24.3], DS18B20-3:[Id:000008C02C3A, Temperature:24.4]], numSwitch:0, isDimmer:false, isAddressable:false, isRGB:false, hasCT:false]

    // SENSOR = {"Time":"2020-01-30T19:15:08","SR04":{"Distance":73.702}}

    // Switch or Metering Switch are the two most likely ones
    deviceInfo = [:]
    deviceInfo["hasEnergy"] = false
    deviceInfo["numTemperature"] = 0
    deviceInfo["numHumidity"] = 0
    deviceInfo["numPressure"] = 0
    deviceInfo["numDistance"] = 0
    deviceInfo["numSensorGroups"] = 0
    deviceInfo["sensorMap"] = [:] as TreeMap
    if(statusMap.containsKey("StatusSNS")) {
        sns = statusMap["StatusSNS"]
        deviceInfo["hasEnergy"] = sns.containsKey("ENERGY")
        deviceInfo["sensorMap"] = getKeysWithMapAndId(sns)
        deviceInfo["numSensorGroups"] = deviceInfo["sensorMap"].size()
        deviceInfo["numTemperature"] = numOfKeyInSubMap(sns, "Temperature")
        deviceInfo["numHumidity"] = numOfKeyInSubMap(sns, "Humidity")
        deviceInfo["numPressure"] = numOfKeyInSubMap(sns, "Pressure")
        deviceInfo["numDistance"] = numOfKeyInSubMap(sns, "Distance")
    }

    deviceInfo["numSwitch"] = 0
    deviceInfo["isDimmer"] = false
    deviceInfo["isAddressable"] = false
    deviceInfo["isRGB"] = false
    deviceInfo["hasCT"] = false
    if(statusMap["StatusSTS"] != null) {
        sts = statusMap["StatusSTS"]
        deviceInfo["isDimmer"] = sts.containsKey("Dimmer")
        deviceInfo["isAddressable"] = sts.containsKey("Width")
        if(sts.containsKey("Color")) deviceInfo["isRGB"] = sts["Color"].length() >= 6
        deviceInfo["hasCT"] = sts.containsKey("CT")

        if(sts["POWER"] != null) {
            // This only exist if there is ONLY one switch/bulb
            deviceInfo["numSwitch"] = 1
        } else {
            i = 1
            while(sts["POWER$i"] != null) {
                i += 1
            }
            deviceInfo["numSwitch"] = i - 1
        }
    }
    logging("Device info found: $deviceInfo", 100)
    // Create the devices, if needed

    // Switches
    if(deviceInfo["numSwitch"] > 0) {
        if(deviceInfo["numSwitch"] > 1 && (
            deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
            deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                log.warn "There's more than one switch and the device is either dimmable, addressable, RGB or has CT capability. This is not fully supported yet, please report which device and settings you're using to the developer."
            }
        
        driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Switch"]
        if(deviceInfo["hasEnergy"] && (deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false)) {
            if(deviceInfo["isDimmer"]) {
                // TODO: Make a Component Dimmer with Metering
                driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
            } else {
                driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Metering Switch"]
            }
        } else {
            if(deviceInfo["hasEnergy"]) {
                log.warn "This device reports Metering Capability AND has RGB, Color Temperature or is Addressable. Metering values will be ignored... This is NOT supported and may result in errors, please report it to the developer."
            }
            if((deviceInfo["isDimmer"] == true || deviceInfo["isAddressable"] == true || 
                deviceInfo["isRGB"] == true || deviceInfo["hasCT"] == true)) {
                if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal Dimmer (Child)", "Generic Component Dimmer"]
                } else if(deviceInfo["isAddressable"] == false && deviceInfo["isRGB"] == false && deviceInfo["hasCT"] == true) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component CT"]
                } else if(deviceInfo["isRGB"] == true && deviceInfo["hasCT"] == false) {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGB"]
                } else {
                    driverName = ["Tasmota - Universal CT/RGB/RGB+CW+WW (Child)", "Generic Component RGBW"]
                }
            }
        }
        
        for(i in 1..deviceInfo["numSwitch"]) {
            namespace = "tasmota"
            childId = "POWER$i"
            childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
            childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
            logging("createChildDevice: POWER$i", 1)
            createChildDevice(namespace, driverName, childId, childName, childLabel)
            
            // Once the first switch is created we only support one type... At least for now...
            driverName = ["Tasmota - Universal Switch (Child)", "Generic Component Switch"]
        }
    }
    
    // Sensors
    deviceInfo["sensorMap"].each {
        namespace = "tasmota"
        driverName = ["Tasmota - Universal Multisensor (Child)"]
        childId = "${it.key}"
        childName = getChildDeviceNameRoot(keepType=true) + " ${getMinimizedDriverName(driverName[0])} ($childId)"
        childLabel = "${getMinimizedDriverName(device.getLabel())} ($childId)"
        createChildDevice(namespace, driverName, childId, childName, childLabel)
    }

    // Finally let the default parser have the data as well...
    parseResult(statusMap)
}

String getChildDeviceNameRoot(Boolean keepType=false) {
    childDeviceNameRoot = getDeviceInfoByName('name')
    if(childDeviceNameRoot.toLowerCase().endsWith(' (parent)')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-9)
    } else if(childDeviceNameRoot.toLowerCase().endsWith(' parent')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(0, childDeviceNameRoot.length()-7)
    }
    if(keepType == false && childDeviceNameRoot.toLowerCase().startsWith('tasmota - ')) {
        childDeviceNameRoot = childDeviceNameRoot.substring(10, childDeviceNameRoot.length())
    }
    return childDeviceNameRoot
}

String getMinimizedDriverName(String driverName) {
    // Remove parts we don't need from the string 
    logging("getMinimizedDriverName(driverName=$driverName)", 1)
    if(driverName.toLowerCase().endsWith(' (child)')) {
        driverName = driverName.substring(0, driverName.length()-8)
    } else if(driverName.toLowerCase().endsWith(' child')) {
        driverName = driverName.substring(0, driverName.length()-6)
    }
    if(driverName.toLowerCase().endsWith(' (parent)')) {
        driverName = driverName.substring(0, driverName.length()-9)
    } else if(driverName.toLowerCase().endsWith(' parent')) {
        driverName = driverName.substring(0, driverName.length()-7)
    }
    if(driverName.toLowerCase().startsWith('tasmota - ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    if(driverName.toLowerCase().startsWith('universal ')) {
        driverName = driverName.substring(10, driverName.length())
    }
    driverName = driverName.replaceAll("Generic Component ", "")
    logging("getMinimizedDriverName(driverName=$driverName) end", 1)
    return driverName
}

def getChildDeviceByActionType(String actionType) {
    return childDevices.find{it.deviceNetworkId.endsWith("-$actionType")}
}

private void createChildDevice(String namespace, List driverName, String childId, String childName, String childLabel) {
    childDevice = childDevices.find{it.deviceNetworkId.endsWith("-$childId")}
    if(!childDevice && childId.toLowerCase().startsWith("power")) {
        // If this driver was used to replace an "old" parent driver, rename the child Network ID
        logging("Looking for $childId, ending in ${childId.substring(5)}", 1)
        childDevice = childDevices.find{it.deviceNetworkId.endsWith("-${childId.substring(5)}")}
        if(childDevice) {
            logging("Setting new Network ID for $childId to '$device.id-$childId'", 1)
            childDevice.setDeviceNetworkId("$device.id-$childId")
        }
    }
    if (childDevice) {
        // The device exists, just update it
        childDevice.setName(childName)
        logging(childDevice.getData(), 10)
    } else {
        s = childName.size()
        for(i in 0..s) {
            if(driverName[i].toLowerCase().startsWith('generic component')) {
                currentNamespace = "hubitat"
            } else {
                currentNamespace = namespace
            }
            try {
                addChildDevice(currentNamespace, driverName[i], "$device.id-$childId", [name: childName, label: childLabel, isComponent: false])
                logging("Created child device '$childLabel' using driver '${driverName[i]}'...", 100)
            } catch (com.hubitat.app.exception.UnknownDeviceTypeException e) {
                if(i == s) {
                    log.error "'${driverName[i]}' driver can't be found! Did you forget to install the child driver?"
                } else {
                    log.warn "'${driverName[i]}' driver can't be found! Trying another driver: ${driverName[i+1]}..."
                }
            }
        }
    }
}

/*
    Tasmota IP Settings and Wifi status
*/
private setDeviceNetworkId(macOrIP, isIP = false) {
    def myDNI
    if (isIP == false) {
        myDNI = macOrIP
    } else {
        logging("About to convert ${macOrIP}...", 0)
        myDNI = convertIPtoHex(macOrIP)
    }
    logging("Device Network Id should be set to ${myDNI} from ${macOrIP}", 0)
    return myDNI
}

def prepareDNI() {
    // Called from update_needed_settings() and parse(description)
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
}

private updateDNI() {
    // Called from:
    // preapreDNI()
    // httpGetAction(uri, callback="parse")
    // postAction(uri, data)
    if (state.dni != null && state.dni != "" && device.deviceNetworkId != state.dni) {
        logging("Device Network Id will be set to ${state.dni} from ${device.deviceNetworkId}", 0)
        device.deviceNetworkId = state.dni
    }
}

def getTelePeriod() {
    return (telePeriod != null && telePeriod.isInteger() ? telePeriod.toInteger() : 300)
}

private getHostAddress() {
    if (port == null) {
        port = 80
    }
    if (override == true && ipAddress != null){
        return "${ipAddress}:${port}"
    }
    else if(getDeviceDataByName("ip") && getDeviceDataByName("port")){
        return "${getDeviceDataByName("ip")}:${getDeviceDataByName("port")}"
    }else{
	    return "${ip}:80"
    }
}

private String convertIPtoHex(ipAddress) {
    String hex = null
    if(ipAddress != null) {
        hex = ipAddress.tokenize( '.' ).collect {  String.format( '%02X', it.toInteger() ) }.join()
        logging("Get this IP in hex: ${hex}", 0)
    } else {
        hex = null
        if (useIPAsID) {
            logging('ERROR: To use IP as Network ID "Device IP Address" needs to be set and "Override IP" needs to be enabled! If this error persists, consult the release thread in the Hubitat Forum.')
        }
    }
    return hex
}

def sync(ip, port = null) {
    def existingIp = getDataValue("ip")
    def existingPort = getDataValue("port")
    logging("Running sync()", 1)
    if (ip && ip != existingIp) {
        updateDataValue("ip", ip)
        sendEvent(name: 'ip', value: ip)
        sendEvent(name: "ipLink", value: "<a target=\"device\" href=\"http://$ip\">$ip</a>")
        logging("IP set to ${ip}", 1)
    }
    if (port && port != existingPort) {
        updateDataValue("port", port)
        logging("Port set to ${port}", 1)
    }
}

def dBmToQuality(dBm) {
    def quality = 0
    if(dBm > 0) dBm = dBm * -1
    if(dBm <= -100) {
        quality = 0
    } else if(dBm >= -50) {
        quality = 100
    } else {
        quality = 2 * (dBm + 100)
    }
    logging("DBM: $dBm (${quality}%)", 0)
    return quality
}

/*
    Tasmota Preferences Related
*/
def configuration_model_tasmota() {
'''
<configuration>
<Value type="password" byteSize="1" index="password" label="Device Password" description="REQUIRED if set on the Device! Otherwise leave empty." min="" max="" value="" setting_type="preference" fw="">
<Help>
</Help>
</Value>
</configuration>
'''
}

/*
    HTTP Tasmota API Related
*/
private httpGetAction(uri, callback="parse") { 
  updateDNI()
  
  def headers = getHeader()
  logging("Using httpGetAction for 'http://${getHostAddress()}$uri'...", 0)
  def hubAction = null
  try {
    /*hubAction = new hubitat.device.HubAction(
        method: "GET",
        path: uri,
        headers: headers
    )*/
    hubAction = asynchttpGet(
        callback,
        [uri: "http://${getHostAddress()}$uri",
        headers: headers]
    )
  } catch (e) {
    log.error "Error in httpGetAction(uri): $e ('$uri')"
  }
  return hubAction    
}

private postAction(uri, data) { 
  updateDNI()

  def headers = getHeader()

  def hubAction = null
  try {
    hubAction = new hubitat.device.HubAction(
    method: "POST",
    path: uri,
    headers: headers,
    body: data
  )
  } catch (e) {
    log.error "Error in postAction(uri, data): $e ('$uri', '$data')"
  }
  return hubAction    
}

def getCommandString(command, value) {
    def uri = "/cm?"
    if (password) {
        uri += "user=admin&password=${password}&"
    }
	if (value) {
		uri += "cmnd=${command}%20${value}"
	}
	else {
		uri += "cmnd=${command}"
	}
    return uri
}

def getMultiCommandString(commands) {
    def uri = "/cm?"
    if (password) {
        uri += "user=admin&password=${password}&"
    }
    uri += "cmnd=backlog%20"
    if(commands.size() > 30) {
        log.warn "Backlog only supports 30 commands, the last ${commands.size() - 30} will be ignored!"
    }
    commands.each {cmd->
        if(cmd.containsKey("value")) {
          uri += "${cmd['command']}%20${cmd['value']}%3B%20"
        } else {
          uri += "${cmd['command']}%3B%20"
        }
    }
    return uri
}

private String urlEscape(url) {
    return(URLEncoder.encode(url).replace("+", "%20"))
}

private String convertPortToHex(port) {
	String hexport = port.toString().format( '%04X', port.toInteger() )
    return hexport
}

private encodeCredentials(username, password) {
	def userpassascii = "${username}:${password}"
    def userpass = "Basic " + userpassascii.bytes.encodeBase64().toString()
    return userpass
}

private getHeader(userpass = null) {
    def headers = [:]
    headers.put("Host", getHostAddress())
    headers.put("Content-Type", "application/x-www-form-urlencoded")
    if (userpass != null)
       headers.put("Authorization", userpass)
    return headers
}

/*
    --END-- TASMOTA METHODS (helpers-tasmota)
*/

/*
    RGBW METHODS (helpers-rgbw)

    Helper functions included in all drivers using RGB, RGBW or Dimmers
    These methods are NOT specific to Tasmota
*/
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
    // This is separated to be able to reuse functions between platforms
    return(whiteForPlatform())
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

/*
    --END-- RGBW METHODS (helpers-rgbw)
*/

/*
    TASMOTA RGBW METHODS (helpers-tasmota-rgbw)

    Helper functions included in all Tasmota drivers using RGB, RGBW or Dimmers
    These methods ARE specific to Tasmota
*/
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
    //if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
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
        //if(device.currentValue("colorMode") != "RGB" ) sendEvent(name: "colorMode", value: "RGB")
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
    //if(device.currentValue("colorMode") != "RGB" ) sendEvent(name: "colorMode", value: "RGB")
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
            if (duration > 5400) {
                log.warn "Maximum supported dimming duration is 5400 seconds due to current implementation method used."
                duration = 5400 // Maximum duration is 1.5 hours
            } 
            cLevel = state.level
            
            levelDistance = l - cLevel
            direction = 1
            if(levelDistance < 0) {
                direction = -1
                levelDistance = levelDistance * -1
            }
            steps = 13
            increment = Math.round(((levelDistance as Float)  / steps) as Float)
            if(increment <= 1 && levelDistance < steps) {
                steps = levelDistance
            }
            // Each Backlog command has 200ms delay, deduct that delay and add 1 second extra
            duration = ((duration as Float) - (2 * steps * 0.2) + 1) as Float
            stepTime = round2((duration / steps) as Float, 1)
            stepTimeTasmota = Math.round((stepTime as Float) * 10)
            lastStepTime = round2((stepTime + (duration - (stepTime * steps)) as Float), 1)
            lastStepTimeTasmota = Math.round((lastStepTime as Float) * 10)
            fadeCommands = []
            cmdLevel = cLevel
            fadeCommands.add([command: "Fade", value: "1"])
            fadeCommands.add([command: "Speed", value: "20"])
            if(steps > 0) {
                (1..steps).each{
                    cmdLevel += (increment * direction)
                    if(direction == 1 && (cmdLevel > l || it == steps)) cmdLevel = l
                    if(direction == -1 && (cmdLevel < l || it == steps)) cmdLevel = l
                    if(it != steps) {
                        fadeCommands.add([command: "Delay", value: "$stepTimeTasmota"])
                    } else {
                        fadeCommands.add([command: "Delay", value: "$lastStepTimeTasmota"])
                    }
                    fadeCommands.add([command: "Dimmer", value: "$cmdLevel"])
                }
            } else {
                fadeCommands.add([command: "Dimmer", value: "$l"])
            }
            fadeCommands.add([command: "Fade", value: "0"])
            cmdData = [cLevel:cLevel, levelDistance:levelDistance, direction:direction, steps:steps, increment:increment, stepTime:stepTime, lastStepTime:lastStepTime]
            //fadeCommands = "Fade 1;Speed ${speed};Dimmer ${l};Delay ${duration};Fade 0"
            logging("fadeCommands: '" + fadeCommands + "', cmdData=$cmdData", 1)
            return(getAction(getMultiCommandString(fadeCommands)))
        }
   }
}

def stopLevelChange() {
    // Since sending a backlog command without arguments will cancel any current level change we have, 
    // then that is what we do...
    cmds = []
    cmds << getAction(getMultiCommandString([[command: "Fade", value: "0"]]))
    cmds << getAction(getCommandString("Backlog", null))
    return cmds
}

def startLevelChange(String direction) {
    cLevel = state.level
    delay = 30
    if(direction == "up") {
        if(cLevel != null) {
            delay = Math.round(((delay / 100) * (100-cLevel)) as Float)
        }
        setLevel(100, delay)
    } else {
        if(cLevel != null) {
            delay = Math.round(((delay / 100) * (cLevel)) as Float)
        }
        setLevel(0, delay)
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
    state.colorMode = "CT"
    //if(device.currentValue("colorMode") != "CT" ) sendEvent(name: "colorMode", value: "CT")
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

/*
    --END-- TASMOTA RGBW METHODS (helpers-tasmota-rgbw)
*/
