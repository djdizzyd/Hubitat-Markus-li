#!include:getHeaderLicense()

#!include:getDefaultParentImports()

definition(
    name: "Tasmota Device Handler",
    namespace: "tasmota",
    author: "Markus Liljergren (markus-li)",
    description: "Device Manager for Tasmota",
    category: "Convenience",
    //TODO: Replace these icons???
    iconUrl:   "",
    iconX2Url: "",
    iconX3Url: ""
) {
    appSetting "defaultTasmotaPassword"
}

preferences {
     page(name: "mainPage", title: "", install: true, uninstall: true)
     page(name: "deleteDevice")
     page(name: "refreshDevices")
     page(name: "resultPage")
     page(name: "configureTasmotaDevice")
     page(name: "addDevices", title: "Add Tasmota-based Device", content: "addDevices")
     page(name: "manuallyAdd")
     page(name: "manuallyAddConfirm")
}

// https://docs.smartthings.com/en/latest/smartapp-developers-guide/preferences-and-settings.html#preferences-and-settings

#!include:getHelperFunctions('device-configurations')

def getMillisSinceDate(myDate) {
    
    //myDate
    return now() - myDate.getTime()
}

def getTimeStringSinceMillis(millis) {
    def seconds = (int) (millis / 1000) % 60
    def minutes = (int) (millis / (1000*60)) % 60
    def hours = (int) (millis / (1000*60*60)) % 24
    def days = (int) (millis / (1000*60*60*24))
    return String.format("%dT%02d:%02d:%02d", days, hours, minutes, seconds)
}

def getTimeStringSinceDate(myDate) {
    return getTimeStringSinceMillis(getMillisSinceDate(myDate))
}

def getTimeStringSinceDateWithMaximum(myDate, maxMillis) {
    def millis = getMillisSinceDate(myDate)
    return [time:getTimeStringSinceMillis(millis), red:millis > maxMillis]
}

def getAppTitle() {
    section(getElementStyle('title', getMaterialIcon('build', 'icon-large') + "${app.label}" + getCSSStyles())){
        }
}

def mainPage() {
	dynamicPage(name: "mainPage", nextPage: null, uninstall: true, install: true) {
        getAppTitle() // Also contains the CSS
        
        // Hubitat green: #81BC00
        // box-shadow: 2px 3px #A9A9A9
        installCheck()
        initializeAdditional()

        if (state.appInstalled == 'COMPLETE') {
            section(getElementStyle('header', getMaterialIcon('settings_applications') + "Configure App"), hideable: true, hidden: false){
                getElementStyle('separator')
                //input(name: "sendToAWSwitch", type: "bool", defaultValue: "false", title: "Use App Watchdog to track this apps version info?", description: "Update App Watchdog", submitOnChange: "true")}
                generate_preferences(configuration_model_debug())

            
            //input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
            //href "deviceDiscoveryCancel", title:"Cancel Discover Device", description:""
            }
            section(getElementStyle('header', getMaterialIcon('library_add') + "Install New Devices"), hideable: true, hidden: false){
                href("deviceDiscovery", title:getMaterialIcon('', 'he-discovery_1') + "Discover Devices", description:"")
                href("manuallyAdd", title:getMaterialIcon('', 'he-add_1') + "Manually Add Device", description:"")
            }
            section(getElementStyle('header', getMaterialIcon('playlist_add') + 'Grant Access to Additional Devices'), hideable: true, hidden: true){
                paragraph("Select the devices to grant access to, if the device doesn't use a compatible driver it will be ignored, so selecting too many or the wrong ones, doesn't matter. Easiest is probably to just select all devices. Only Parent devices are shown.")
                input(name:	"devicesSelected", type: "capability.refresh", title: "Available Devices", multiple: true, required: false, submitOnChange: true)
            }
            section(getElementStyle('header', getMaterialIcon('', 'he-settings1') + "Configure Devices"), hideable: true, hidden: false){ 
                paragraph('<div style="margin: 8px;">All devices below use a compatible driver, if any device is missing, add them above in "Grant Access to Additional Devices". Newly selected devices will not be shown until after you\'ve pressed Done.</div>')
                
                //input(name: "refreshDevices", type: "bool", defaultValue: "false", submitOnChange: true, title: "Refresh Devices", description: "Refresh Devices Desc")
                href("resultPage", title:getMaterialIcon('autorenew') + "Result Page", description: "")
                href("refreshDevices", title:getMaterialIcon('autorenew') + "Refresh Devices", description: "")

                state.devices.each { rawDev ->
                    cDev = getTasmotaDevice(rawDev.deviceNetworkId)
                    //getLastActivity()
                    if(cDev != null) {
                        href("configureTasmotaDevice", title:"${getMaterialIcon('', 'he-bulb_1 icon-small')} $cDev.label", description:"", params: [did: cDev.deviceNetworkId])
                        
                        lastActivity = getTimeStringSinceDateWithMaximum(cDev.getLastActivity(), 2*60*60*1000)
                        
                        wifiSignalQuality = cDev.currentState('wifiSignal')
                        wifiSignalQualityRed = true
                        if(wifiSignalQuality != null) {
                            wifiSignalQuality = wifiSignalQuality.value
                            quality = extractInt(wifiSignalQuality)
                            wifiSignalQualityRed = quality < 50
                        }
                        uptime = "${cDev.getDeviceDataByName('uptime')}"
                        firmware = "${cDev.getDeviceDataByName('firmware')}"
                        driverVersion = "${cDev.getDeviceDataByName('driver')}"
                        driverName = "${getDeviceDriverName(cDev)}"
                        getDeviceTable([[href:getDeviceConfigLink(cDev.id)],
                                        [data:rawDev['data']['ip']],
                                        //[data:runDeviceCommand(getTasmotaDevice(cDev.deviceNetworkId), 'getDeviceDataByName', ['uptime'])],])
                                        [data:uptime, red:uptime == "null"],
                                        [data:lastActivity['time'], red:lastActivity['red']],
                                        [data:"${wifiSignalQuality}", red:wifiSignalQualityRed],
                                        [data:firmware, red:firmware == "null"],
                                        [data:driverVersion, red:driverVersion == "null"],
                                        [data:driverName, red:driverName == "null"],])
                                // it.label
                            //btnParagraph([[href:getDeviceConfigLink(cDev.id), target:"_blank", title:"Config"],
                            //            [href:getDeviceTasmotaConfigLink(cDev['data']['ip']), target:"_blank", title:'Tasmota&nbsp;Web&nbsp;Config (' + cDev['data']['ip'] + ')']],
                            //)
                                //paragraph('<table style="border-spacing: 10px 0px"><tr><td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceConfigLink(it.id)}" + '" target="deviceConfig">Config</a></td>' + 
                                        //' | <a href="' + "${getDeviceLogLink(it.id)}" + '" target="deviceConfig">Log</a>' +
                                //        '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceTasmotaConfigLink(it['data']['ip'])}" + '" target="deviceWebConfig">Tasmota&nbsp;Web&nbsp;Config (' + it['data']['ip'] + ')</a></td></tr></table>' )
                        
                        //%7B%22did%22%3A%225002915AFD0A%22%7D
                        //%7B%22did%22%3A%222CF43222E6AD%22%7D
                    }

                }
                it = test
            }
            section(getElementStyle('header', "More things"), hideable: true, hidden: true){
                paragraph("Select the devices to configure, if the device doesn't use a compatible driver it will be ignored, so selecting too many or the wrong ones, doesn't matter. Easiest is probably to just select all devices. Only Parent devices are shown.")
                
                input(name:	"devicesAvailable", type: "enum", title: "Available Devices", multiple: true, required: false, submitOnChange: true, options: state.devicesSelectable)
            }
        } else {
            section(getElementStyle('subtitle', "Configure")){
                generate_preferences(configuration_model_debug())
            }
        }
        footer()

    }
}

def refreshDevices(){
    logging("refreshDevices()", 1)
    numDevices = 0
    numDevicesSuccess = 0
    getAllTasmotaDevices().each {
        numDevices += 1
        try{
            //logging("BEFORE Refreshing Device \"${it.label}\" (${it.id})", 1)
            it.refresh()
            logging("AFTER Refreshing Device \"${it.label}\" (${it.id})", 1)
            numDevicesSuccess += 1
        } catch(e) {
            log.warn("Failed to Refresh Device \"${it.label}\" (${it.id})")
        }
    }
    result = "COMPLETE REFRESH FAILURE!"
    if(numDevicesSuccess == numDevices) {
        result = "All $numDevices Device(s) have been refreshed!"
    } else {
        result = "PARTIAL FAILURE: $numDevicesSuccess of $numDevices Device(s) have been refreshed! (${numDevices - numDevicesSuccess} failed!)"
    }
    updatedAdditional()
    resultPage("refreshDevices", "Devices Refreshed", result)
}

def resultPage(){
    logging("resultPage()", 1)
    resultPage("resultPage", "Result Page", "My little result...")
}

def resultPage(name, title, result, nextPage = "mainPage"){
    logging("resultPage(name = $name, title = $title, result = $result, nextPage = $nextPage)", 1)

    dynamicPage(name: name, title: "", nextPage: nextPage) {
        getAppTitle() // Also contains the CSS

        section(getElementStyle('header', getMaterialIcon('done') + "Action Completed"), hideable: true, hidden: false){
            paragraph("<div style=\"font-size: 16px;\">${result}</div>")
        }
    }
}

def getElementStyle(style, content=""){
    switch (style) {
        case 'header':
            //return '<div style="font-weight: bold; color:#fff;">' + content + '</div>'
            return content
            break
        case 'title':
            return '<h2 style="font-weight: bold; color:#382e2b;">' + content + '</h2>'
            break
        case 'subtitle':
            return '<div style="font-weight: bold; color:#382e2b;">' + content + '</div>'
            break
        case 'line':
            return '<hr style="height: 1px; border: 0px; background-color:#382e2b;"></hr>'
        case 'separator':
            return '\n<hr style="background-color:#1A77C9; height: 1px; border: 0;"></hr>'
            break
    }
}

def getMaterialIcon(iconName, extraClass='') {
    // Browse icons here
    // https://material.io/resources/icons/?style=baseline
    // known HE icons (set as class): he-bulb_1, he-settings1, he-file1, he-default_dashboard_icon, he-calendar1
    // he-discovery_1, he-add_2, he-door_closed
    return '<i class="material-icons icon-position ' + extraClass + '">' + iconName + '</i>'
}

//#.form div.mdl-cell h4.pre {
def getCSSStyles() {
    return '''<style>
/* General App Styles */
.btn {
    font-family: "Roboto","Helvetica","Arial",sans-serif;
}
.mdl-card, .mdl-switch__label, .mdl-textfield  {
    font-size: 14px;
    font-family: "Roboto","Helvetica","Arial",sans-serif;
}
.btn-sub {
    padding: 2px 30px 2px 2px;
}
div.mdl-button--raised {
    font-weight: bold; 
    color:#fff; 
    background-color:#81bc00; 
    border: 1px solid;
}
div.mdl-button--raised:hover, div.mdl-button--raised:focus {
    color: #212121;
    background-color:#91d844; 
}
.btn-sub.hrefElem:before {
    top: calc(50% - 0.75625rem);
}
div.mdl-button--raised h4.pre {
    font-weight: bold; 
    color: #fff;
    vertical-align: middle;
}

/* Icon Styles */
.icon-position {
    margin-right: 12px;
    vertical-align: middle;
}
.icon-tiny {
    margin-right: 8px;
    font-size: 14px;
}
.icon-small {
    margin-right: 8px;
    font-size: 18px;
}
.icon-large {
    margin-right: 12px;
    font-size: 32px;
}

/* Configure Devices List Styles */
#collapse4 .hrefElem::before {
    filter: invert(100%);
}
#collapse4 .hrefElem:hover::before, #collapse4 .hrefElem:focus::before {
    filter: invert(0%);
}
#collapse4 table .hrefElem::before {
    filter: invert(0%);
}
#collapse4 .btn-block {
    color: #f5f5f5;
    background-color: #382e2b;
    
    font-size: 14px;
    /*font-size: calc(100% + 0.08vw);*/
    max-width: inherit;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}
#collapse4 .btn-block span {
    white-space: nowrap !important;
    max-width: inherit;
}
#collapse4 .btn-block:hover, #collapse4 .btn-block:focus {
    color: #212121;
    background-color: #e0e0e0;
}
#collapse4 div.mdl-textfield {
    margin: 0px;
}
.device-config_table {
    border-spacing: 2px 0px;
    table-layout:fixed;
    width: 100%
}
.device-config_td {
    text-align: center;
    vertical-align: middle;
}
.device-config_btn {
    width: 100%;
}
.device-config_table th, .device-config_table td {
    font-family: "Roboto","Helvetica","Arial",sans-serif;
    font-size: 13px;
    vertical-align: middle;
    width: 100%;
}
.device-config_table th div, .device-config_td div, .device-config_table td a {
    text-align: center;
    white-space: nowrap !important;
    max-width: inherit;
    overflow: hidden;
    text-overflow: ellipsis;
    width: 100%;
    display: block;
}
.device-config_btn_icon {
    text-align: center;
    width: 100%;
}

/* Action Buttons */
#collapse4 [name*="refreshDevices"] {
    float: right !important;
}
#collapse4 [name*="resultPage"] {
    float: left !important;
}
#collapse4 [name*="refreshDevices"], #collapse4 [name*="resultPage"] {
    color: #000;
    width: 170px !important;
    min-width: 170px;
    background: rgba(158,158,158,.2);
    border: none;
    margin-left: 0px;
    text-align: center !important;
    vertical-align: middle;
    line-height: 36px;
    padding-right: unset;
    padding: 0px 16px;
    display:inline;
}
#collapse4 .mdl-cell--12-col:nth-of-type(2), #collapse4 .mdl-cell--12-col:nth-of-type(3) {
    width: 50% !important;
    display:inline !important;
}
#collapse4 [name*="refreshDevices"] span, #collapse4 [name*="resultPage"] span {
    font-weight: 500;
    text-align: center !important;
    white-space: nowrap !important;
}
#collapse4 [name*="refreshDevices"]::before, #collapse4 [name*="resultPage"]::before {
    content: "";
}

@media (min-width: 840px)
.mdl-cell--8-col, .mdl-cell--8-col-desktop.mdl-cell--8-col-desktop {
    width: calc(76.6666666667% - 16px);
}
</style>'''
}

def btnParagraph(buttons, extra="") {
    //getDeviceConfigLink(it.id)
    def content = '<table style="border-spacing: 10px 0px"><tr>'
    buttons.each {
        //content += '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp">'
        content += '<td>'
        
        content += '<a style="color: #000;" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
        
        content += '<button type="button" class="btn btn-default hrefElem btn-lg mdl-button--raised mdl-shadow--2dp btn-sub">'
        
        content += "${it['title']}"
        //content += '<i class="material-icons icon-position hrefElemAlt">arrow_drop_down</i>'
        content += '</button></a></td>'

//                                '<td class="btn btn-default btn-lg hrefElem mdl-button--raised mdl-shadow--2dp"><a style="color: #000;" href="' + "${getDeviceTasmotaConfigLink(it['data']['ip'])}" + '" target="deviceWebConfig">Tasmota&nbsp;Web&nbsp;Config (' + it['data']['ip'] + ')</a></td> )
    }
    content += '</tr></table>' // + extra
    paragraph(content) 
}

def getDeviceTableCell(deviceInfoEntry, link=true) {
    it = deviceInfoEntry
    def content = '<td class="device-config_td ' + "${it['td_class']}" + '">'
        
    if(link == true) {
        content += '<a class="device-config_btn ' + "${it['class']}" + '" href="' + "${it['href']}" + '" target="' +"${it['target']}" + '">'
    }
    
    //content += '<button type="button" class="btn btn-default hrefElem btn-lg mdl-button--raised mdl-shadow--2dp btn-sub">'
    extraTitle = ""
    if(it['title'] != null && it['title'].indexOf('material-icons') == -1) {
        extraTitle = "title=\"${it['title']}\""
    }
    if(it['red'] == true) {
        
            content += "<div ${extraTitle} style=\"color: red;\" >${it['title']}</div>"
        
    } else {
        content += "<div ${extraTitle} >${it['title']}</div>"
    }
    //content += '<i class="material-icons icon-position hrefElemAlt">arrow_drop_down</i>'
    //content += '</button></a></td>'
    if(link == true) {
        content += '</a>'
    }
    content += '</td>'

    return content
}

def getDeviceTable(deviceInfo, extra="") {
    //getDeviceConfigLink(it.id)
    def content = '<table class="device-config_table"><tr>'
    content += '<th style="width: 40px;"><div>Config</div></th>'
    content += '<th style="width: 100px;"><div>Tasmota&nbsp;Config</div></th>'
    content += '<th style="width: 80px;"><div>Uptime</div></th>'
    content += '<th style="width: 80px;"><div>Heartbeat</div></th>'
    content += '<th style="width: 33px;"><div>Wifi</div></th>'
    content += '<th style="width: 100px;"><div>Firmware</div></th>'
    content += '<th style="width: 60px;"><div>Driver</div></th>'
    content += '<th style=""><div>Type</div></th>'
    content += '</tr><tr>'

    // Config Link
    content += getDeviceTableCell([href:deviceInfo[0]['href'], 
        target:'_blank', title:getMaterialIcon('', 'he-settings1 icon-tiny device-config_btn_icon')])

    // Tasmota Web Config Link
    content += getDeviceTableCell([class:'device-config_btn', href:getDeviceTasmotaConfigLink(deviceInfo[1]['data']), 
        target:'_blank', title:deviceInfo[1]['data']])

    // Tasmota Device Uptime
    content += getDeviceTableCell([title:deviceInfo[2]['data'], red:deviceInfo[2]['red']], false)

    // Tasmota Heartbeat
    content += getDeviceTableCell([title:deviceInfo[3]['data'], red:deviceInfo[3]['red']], false)

    // Wifi Signal Quality
    content += getDeviceTableCell([title:deviceInfo[4]['data'], red:deviceInfo[4]['red']], false)

    // Firmware Version
    content += getDeviceTableCell([title:deviceInfo[5]['data'], red:deviceInfo[5]['red']], false)

    // Driver Version
    content += getDeviceTableCell([title:deviceInfo[6]['data'], red:deviceInfo[6]['red']], false)

    // Driver Type
    content += getDeviceTableCell([title:deviceInfo[7]['data'], red:deviceInfo[7]['red']], false)

    content += '</tr>'
    content += '<tr>'

    content += '</tr></table>' // + extra
    paragraph(content) 
}

def configureTasmotaDevice(params) {
    if (params?.did || params?.params?.did) {
        if (params.did) {
            //getTasmotaDevice
            //
            state.currentDeviceId = params.did
            state.currentDisplayName = getTasmotaDevice(params.did).label
            logging("params.did: $params.did, label: ${getTasmotaDevice(params.did)?.label}", 1)
        } else {
            logging("params.params.did: $params.params.did", 1)
            state.currentDeviceId = params.params.did
            state.currentDisplayName = getTasmotaDevice(params.params.did)?.label
        }
    }
    device = getTasmotaDevice(state.currentDeviceId)
    state.currentDisplayName = device.label
    logging("state.currentDeviceId: ${state.currentDeviceId}, label: ${device.label}", 1)
    //if (device != null) device.configure()
    dynamicPage(name: "configureTasmotaDevice", title: "Configure Tasmota-based Devices created with this app", nextPage: null) {
            section {
                app.updateSetting("${state.currentDeviceId}_label", device.label)
                input "${state.currentDeviceId}_label", "text", title:"Device Name", description: "", required: false
                href "changeName", title:"Change Device Name", description: "Edit the name above and click here to change it"
            }
            section {
                href "deleteDevice", title:"Delete $state.label", description: ""
            }
    }
    // device = getChildDevice(did)
    //dynamicPage(name: "configureTasmotaDevice", nextPage: null, uninstall: false, install: false) {
    //    paragraph('Device: <a href="' + "${getDeviceConfigLink(device.deviceId)}" + '" target="deviceConfig">' + device.label + '</a>' + 
    //                          ' - <a href="' + "${getDeviceLogLink(device.deviceId)}" + '" target="deviceConfig">Device&nbsp;Log</a>')
    //}
}

def manuallyAdd(){
   dynamicPage(name: "manuallyAdd", title: "Manually add a Tasmota-based Device", nextPage: "manuallyAddConfirm") {
		section {
			paragraph "This process will manually create a Tasmota-based Device with the entered IP address. Tasmota Connect then communicates with the device to obtain additional information from it. Make sure the device is on and connected to your wifi network."
            input "deviceType", "enum", title:"Device Type", description: "", required: true, options: 
                #!include:makeTasmotaConnectDriverListV1()
            input "ipAddress", "text", title:"IP Address", description: "", required: true 
		}
    }
}

def manuallyAddConfirm(){
   if ( ipAddress =~ /^(?:[0-9]{1,3}\.){3}[0-9]{1,3}$/) {
       logging("Creating Tasmota-based Wifi Device with dni: ${convertIPtoHex(ipAddress)}", 1)
       addChildDevice("tasmota", deviceType ? deviceType : "Tasmota - Generic Wifi Switch/Plug", "${convertIPtoHex(ipAddress)}", location.hubs[0].id, [
           "label": (deviceType ? deviceType : "Tasmota - Generic Wifi Switch/Plug") + " (${ipAddress})",
           "data": [
           "ip": ipAddress,
           "port": "80" 
           ]
       ])
   
       app.updateSetting("ipAddress", "")
            
       dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Tasmota-based Device", nextPage: "mainPage") {
		   section {
			   paragraph "The device has been added. Press next to return to the main page."
	    	}
       }
    } else {
        dynamicPage(name: "manuallyAddConfirm", title: "Manually add a Tasmota-based Device", nextPage: "mainPage") {
		    section {
			    paragraph "The entered ip address is not valid. Please try again."
		    }
        }
    }
}

def deleteDevice(){
    try {
        unsubscribe()
        deleteChildDevice(state.currentDeviceId)
        dynamicPage(name: "deleteDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "The device has been deleted. Press next to continue"
            } 
        }
    
	} catch (e) {
        dynamicPage(name: "deleteDevice", title: "Deletion Summary", nextPage: "mainPage") {
            section {
                paragraph "Error: ${(e as String).split(":")[1]}."
            } 
        }
    
    }
}

def getDeviceDriverName(device) {
    //getTasmotaDevice device.deviceNetworkId
    driverName = 'Unknown'
    try {
        driverName = runDeviceCommand(device, 'getDeviceInfoByName', args=['name'])
    } catch(e) {
        logging("Failed getting DriverName ($e), trying again...", 1)
        device = getTasmotaDevice(device.deviceNetworkId)
        driverName = runDeviceCommand(device, 'getDeviceInfoByName', args=['name'])
    }
    if (driverName.startsWith("Tasmota - ")) driverName = driverName.substring(10)
    logging("Found Driver Name: '$driverName'", 0)
    return driverName
}

def getDeviceConfigLink(deviceId) {
    ///device/edit/
    return("http://${location.hub.localIP}/device/edit/${deviceId}")
}

def getDeviceLogLink(deviceId) {
    ///device/edit/
    return("http://${location.hub.localIP}/logs#dev${deviceId}")
}

def getDeviceTasmotaConfigLink(deviceIP) {
    ///device/edit/
    return("http://${deviceIP}/")
}

def installCheck() {
	state.appInstalled = app.getInstallationState()
	
	if (state.appInstalled != 'COMPLETE') {
		section{paragraph "Please hit 'Done' to finish installing '${app.label}'"}
  	}
  	else {
    	logging("Parent Installed OK", 1)
  	}
}

def footer() {
    section() {
        paragraph(getElementStyle('line'))
        paragraph('<div style="color:#382e2b; text-align:center">' + app.label + ' - Copyright&nbsp;2020&nbsp;Markus&nbsp;Liljergren - <a href="https://github.com/markus-li/Hubitat/tree/release" target="_blank">GitHub repo</a></div>')
    }
}



/*
	installedAdditional

	Purpose: initialize the app
	Note: if present, called from installed() in all drivers/apps
    installed() does NOT call initalize() by default, so if needed, call it here.
*/
def installedAdditional() {
    logging("installedAdditional()", 1)
	initialize()
}

def uninstalled() {
    logging("uninstalled()", 1)
    unsubscribe()
    unschedule()
}

def updatedAdditional() {
    logging("updatedAdditional()", 1)
	unsubscribe()
    unschedule()
    devices = getAllTasmotaDevices()
    
    //app.removeSetting("devicesAvailable")
    //app.updateSetting("devicesAvailable", devices)
    //devicesSelected = devices
    state.devices = devices.sort({ a, b -> a["label"] <=> b["label"] })
    def devicesSelectable = []
    state.devices.each { devicesSelectable << ["${it.deviceNetworkId}":"${it.label}"] }

    logging("devicesSelectable: ${devicesSelectable}", 1)
    state.devicesSelectable = devicesSelectable
	initialize()
}

def runDeviceCommand(device, cmd, args=[]) {
    def jsonSlurper = new JsonSlurper()
    logging("runDeviceCommand(device=${device.deviceId.toString()}, cmd=${cmd}, args=${args})", 0)
    // Since Refresh is defined as a command in all my drivers, 
    // it can be used to forward the call to deviceCommand.
    //device.parse(JsonOutput.toJson([cmd: cmd, args: args]), 1)
    
    device.refresh(JsonOutput.toJson([cmd: cmd, args: args]))
    //device.deviceCommand(JsonOutput.toJson([cmd: cmd, args: args]))
    r = jsonSlurper.parseText(device.getDataValue('appReturn'))
    device.updateDataValue('appReturn', null)
    return r
}

// 
def getAllTasmotaDevices() {
    toRemove = []
    devicesFiltered = []
    devicesSelected.eachWithIndex { it, i ->
        namespace = 'unknown'
        try {
            //runDeviceCommand(it, 'setDeviceInfoAsData', ['namespace'])
            //namespace = runDeviceCommand(it, 'getDeviceInfoByName', ['namespace'])
            //runDeviceCommand(it, 'testing', [])
            namespace = it.getDataValue('namespace')
        } catch(e) {
            // This only fails when a device is of no interest to us anyway, so mute it...
            logging("Device ID: ${it.deviceId.toString()}, e: ${e}", 1)
        }

        logging("Device ID: ${it.deviceId.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        //logging(it.getProperties().toString(), 1)
        //logging(it.parentAppId, 1)
        //parentAppId
        if(namespace == 'tasmota' && it.parentAppId != app.id) {
            devicesFiltered << it
        }
    }
    childDevices = getChildDevices()
    logging("getChildDevices: ${getChildDevices()}", 1)
    childDevices.eachWithIndex { it, i ->
        namespace = 'unknown'
        try {
            //runDeviceCommand(it, 'setDeviceInfoAsData', ['namespace'])
            namespace = runDeviceCommand(it, 'getDeviceInfoByName', ['namespace'])
            //runDeviceCommand(it, 'testing', [])
            //namespace = it.getDataValue('namespace')
        } catch(e) {
            // This only fails when a device is of no interest to us anyway, so mute it...
            logging("Device ID: ${it.id.toString()}, e: ${e}", 1)
        }

        logging("Device ID: ${it.id.toString()}, Parent ID: ${it.parentAppId.toString()}, name: ${it.getName()}, namespace: ${namespace}, deviceNetworkId: ${it.deviceNetworkId}, i: ${i}", 0)
        //logging(it.getProperties().toString(), 1)
        //logging(it.parentAppId, 1)
        //parentAppId
        if(namespace == 'tasmota') {
            devicesFiltered << it
        }
    }
    //devicesFiltered << childDevices
    return devicesFiltered
}

def getTasmotaDevice(deviceNetworkId) {
    r = getChildDevice(deviceNetworkId)
    if(r == null) {
        devicesSelected.each {
            //logging("'" + it.deviceNetworkId + "' =? '" + deviceNetworkId + "'", 1)
            if(it.deviceNetworkId == deviceNetworkId) {
                //logging("'" + it.deviceNetworkId + "' == '" + deviceNetworkId + "' (dev: ${it})", 1)
                r = it
            }
        }
    }
    return r
}

/*
	initializeAdditional

	Purpose: initialize the app
	Note: if present, called from initialize() in all drivers/apps
    Called when Done is pressed in the App
*/
def initializeAdditional() {
    logging("initializeAdditional()", 1)
    //getAllTasmotaDevices()
    //ssdpSubscribe()
    //runEvery5Minutes("ssdpDiscover")
}

#!include:getLoggingFunction()

#!include:getHelperFunctions('app-default')

#!include:getHelperFunctions('tasmota')