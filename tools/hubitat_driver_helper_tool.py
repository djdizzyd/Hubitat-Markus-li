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
import os
from pathlib import Path
import re
import pprint
import yaml
import ruamel.yaml

# Local imports
from hubitat_driver_snippets import *
from hubitat_driver_snippets_parser import *
from HubitatAJAXHelper import HubitatAJAXHelper

"""
  Hubitat driver and app developer tool
  WARNING: Do NOT run this script unless you know what it does, it may DELETE your data!
  NOTE: This is a Work In Progress, feel free to use it, but don't rely on it not changing completely!
"""

def getHelperFunctions(helperFunctionType):
    r = ''
    f = './helpers/helpers-' + helperFunctionType + '.groovy'
    if(os.path.isfile(f)):
        with open (f, "r") as rd:
            for l in rd:
                r += l
    else:
        # Yes, this should be specific, but it doesn't matter here...
        raise Exception("Helper function type '" + helperFunctionType + "' can't be included! File doesn't exist!")
    return(r)

def getOutputGroovyFile(inputGroovyFile, outputGroovyDir, alternateOutputFilename = None):
    #print('Using "' + str(inputGroovyFile) + '" to get path for "' + str(outputGroovyDir) + '"...')
    #print('Filename stem: ' + inputGroovyFile.stem)
    #print('Filename suffix: ' + inputGroovyFile.suffix)
    if(alternateOutputFilename != None):
        outputGroovyFile = outputGroovyDir / str(alternateOutputFilename + "-expanded" + inputGroovyFile.suffix)
    else:
        outputGroovyFile = outputGroovyDir / str(inputGroovyFile.stem + "-expanded" + inputGroovyFile.suffix)
    #print('outputGroovyFile: ' + str(outputGroovyFile))
    return(outputGroovyFile)

def checkForDefinitionString(l, alternateName = None, alternateNamespace = None, alternateVid = None):
    definitionPosition = l.find('definition (')
    if(definitionPosition != -1):
        ds = l[definitionPosition+11:].strip()
        # On all my drivers the definition row ends with ") {"
        print('Parsing Definition statement')
        print('{'+ds[1:-3]+'}')
        definitionDict = yaml.load(('{'+ds[1:-3]+' }').replace(':', ': '), Loader=yaml.FullLoader)
        print(definitionDict)
        if(alternateName != None):
            definitionDict['name'] = alternateName
        if(alternateNamespace != None):
            definitionDict['namespace'] = alternateNamespace
        if(alternateVid != None):
            definitionDict['vid'] = alternateVid
        #print(definitionDict)
        # Process this string
        # (name: "Tasmota - Tuya Wifi Touch Switch TEST (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        #PATTERN = re.compile(r'''((?:[^,"']|"[^"]*"|'[^']*')+)''')
        #PATTERN2 = re.compile(r'''((?:[^(){}:"']|"[^"]*"|'[^']*')+)''')
        #l1 = PATTERN.split(ds)[1::2]
        #d = {}
        #for p1 in l1:
        #    p1 = p1.strip()
        #    i = 0
        #    previousp2 = None
        #    for p2 in PATTERN2.split(p1)[1::2]:
        #        p2 = p2.strip()
        #        if(p2 != ''):
        #            if(i==0):
        #                previousp2 = p2.strip('"')
        #            else:
        #                #print('"' + previousp2 + '"="' + p2.strip('"') + '"')
        #                d[previousp2] = p2.strip('"')
        #            i += 1
        #print(d)
        
        ds = '[' + str(definitionDict)[1:-1] + ']'
        for k in definitionDict:
            definitionDict[k] = '"x' + definitionDict[k] + 'x"'
        newDefinition = (l[:definitionPosition]) + 'definition (' + yaml.dump(definitionDict, default_flow_style=False, sort_keys=False ).replace('\'"x', '"').replace('x"\'', '"').replace('\n', ', ')[:-2] + ') {\n'
        print(newDefinition)
        output = 'def getDeviceInfoByName(infoName) { \n' + \
            '    // DO NOT EDIT: This is generated from the metadata!\n' + \
            '    // TODO: Figure out how to get this from Hubitat instead of generating this?\n' + \
            '    deviceInfo = ' + ds + '\n' + \
            '    return(deviceInfo[infoName])\n' + \
            '}'
        #newDefinition = l
        return(newDefinition, output)
    else:
        return(None)

def makeTasmotaConnectDriverListV1(driversList):
    tsDriverList = '['
    for d in driversList:
        name = driversList[d]['name']
        # If it's a child driver, we don't need it in this list
        if ('child' not in name.lower() and driversList[d]['namespace'] == 'tasmota'):
            tsDriverList += '"' + name + '",\n'
    tsDriverList += ']'
    return(tsDriverList)

def makeTasmotaConnectDriverListV2(driversList):
    shortDriverMap = {
        'Tasmota - Sonoff TH Wifi Switch': 'Sonoff TH',
        'Tasmota - Sonoff PowR2': 'Sonoff POW',
        'Tasmota - Sonoff 2CH Wifi Switch': 'Sonoff Dual',
        'Tasmota - Sonoff 4CH Wifi Switch': 'Sonoff 4CH',
        'Tasmota - Sonoff IFan02 Wifi Controller': 'Sonoff IFan02',
        'Tasmota - Sonoff S31 Wifi Switch': 'Sonoff S31',
        'Tasmota - Sonoff S2X': 'Sonoff S2',
        'Tasmota - Sonoff SC': 'Sonoff SC',
        'Tasmota - Sonoff Bridge': 'Sonoff Bridge',
        'Tasmota - Tuya Wifi Touch Switch': 'Tuya',
    }
    i = 0
    tsDriverList = ''
    for d in driversList:
        name = driversList[d]['name']
        try:
            nameShort = shortDriverMap[name]
        except Exception:
            nameShort = name
        # If it's a child driver, we don't need it in this list
        if ('child' not in name.lower() and driversList[d]['namespace'] == 'tasmota'):
            tsDriverList += ('else ' if i > 0 else '') + \
                'if (selectedDevice?.value?.name?.startsWith("' + nameShort + '"))\n' + \
                '    deviceHandlerName = "' + name + '"\n'
    return(tsDriverList)

def expandGroovyFile(inputGroovyFile, outputGroovyDir, extraData = None, alternateOutputFilename = None, \
                     alternateName = None, alternateNamespace = None, alternateVid = None):
    outputGroovyFile = getOutputGroovyFile(inputGroovyFile, outputGroovyDir, alternateOutputFilename)
    print('Expanding "' + str(inputGroovyFile) + '" to "' + str(outputGroovyFile) + '"...')
    definitionString = None
    with open (outputGroovyFile, "w") as wd:
        with open (inputGroovyFile, "r") as rd:
            # Read lines in loop
            for l in rd:
                if(definitionString == None):
                    definitionString = checkForDefinitionString(l, alternateName = alternateName, alternateNamespace = alternateNamespace, alternateVid = alternateVid)
                    if(definitionString != None):
                        (l, definitionString) = definitionString
                includePosition = l.find('#!include:')
                if(includePosition != -1):
                    evalCmd = l[includePosition+10:].strip()
                    if(evalCmd == 'getDeviceInfoFunction()'):
                        print("Executing getDeviceInfoFunction()...")
                        if(definitionString == None):
                            print('ERROR: Missing Definition in file!')
                        output = definitionString
                    elif(evalCmd == 'makeTasmotaConnectDriverListV1()'):
                        print("Executing makeTasmotaConnectDriverListV1()...")
                        if(extraData == None):
                            print('ERROR: Missing extraData!')
                            output = ''
                        else:
                            output = makeTasmotaConnectDriverListV1(extraData)
                    elif(evalCmd == 'makeTasmotaConnectDriverListV2()'):
                        print("Executing makeTasmotaConnectDriverListV2()...")
                        if(extraData == None):
                            print('ERROR: Missing extraData!')
                            output = ''
                        else:
                            output = makeTasmotaConnectDriverListV2(extraData)
                    else:
                        output = eval(evalCmd)
                    if(includePosition > 0):
                        i = 0
                        wd.write(l[:includePosition])
                        for nl in output.splitlines():
                            if i != 0:
                                wd.write(' ' * (includePosition) + nl + '\n')
                            else:
                                wd.write(nl + '\n')
                            i += 1
                    else:
                        wd.write(output + '\n')
                else:
                    wd.write(l)
                #print(l.strip())
    print('DONE expanding "' + inputGroovyFile.name + '" to "' + outputGroovyFile.name + '"!')

def main():
    #startDir = os.getcwd()
    pp = pprint.PrettyPrinter(indent=4)
    driverDir = Path("./drivers")
    appsDir = Path("./apps")
    expandedDriversDir = driverDir / 'expanded'
    expandedAppsDir = appsDir / 'expanded'
    #HubitatAJAXHelper.saveConfig('192.168.1.1', 'username', 'password', 'hubitat_ajax_sample.cfg')
    hubitatAjax = HubitatAJAXHelper(None, 'hubitat_ajax.cfg')
    print(hubitatAjax.login())

    #codeVersion = hubitatAjax.get_driver_current_code_version(550)
    #print(codeVersion)
    
    driversDict = hubitatAjax.get_driver_list()
    #pp.pprint()
    usedDriversDict = {}

    driversFiles = [
        {'id': 550, 'file': driverDir / 'tasmota-tuya-wifi-touch-switch-child-test.groovy' },
        {'id': 513, 'file': driverDir / 'tasmota-sonoff-powr2.groovy' },
        {'id': 548, 'file': driverDir / 'tasmota-tuya-wifi-touch-switch.groovy' },
        {'id': 549, 'file': driverDir / 'tasmota-tuya-wifi-touch-switch-child.groovy' },
        {'id': 418, 'file': driverDir / 'tasmota-tuya-wifi-touch-switch-child.groovy', \
         'alternateOutputFilename': 'tasmota-tuya-wifi-touch-switch-legacy-child', \
         'alternateName': 'Tasmota - Tuya Wifi Touch Switch Legacy (Child)', \
         'alternateNamespace': 'tasmota-legacy'},
        {'id': 551, 'file': driverDir / 'tasmota-sonoff-s2x.groovy' },
        {'id': 554, 'file': driverDir / 'tasmota-sonoff-mini.groovy'},
        {'id': 560, 'file': driverDir / 'tasmota-sonoff-basic.groovy'},
        {'id': 556, 'file': driverDir / 'tasmota-sonoff-basic.groovy', \
         'alternateOutputFilename': 'tasmota-sonoff-basic-r3', \
         'alternateName': 'Tasmota - Sonoff Basic R3'},
        {'id': 552, 'file': driverDir / 'tasmota-generic-wifi-switch.groovy' },
        {'id': 553, 'file': driverDir / 'tasmota-s120-plug.groovy' },
        {'id': 557, 'file': driverDir / 'tasmota-ykyc-001-pm-plug.groovy' },
        {'id': 559, 'file': driverDir / 'tasmota-brilliant-bl20925-pm-plug.groovy' },
        {'id': 558, 'file': driverDir / 'tasmota-generic-pm-plug.groovy' },
    ]
    
    #driversFiles = [
    #    {'id': 557, 'file': driverDir / 'tasmota-ykyc-001-pm-plug.groovy' },
    #]

    j=0
    for d in driversFiles:
        aOF = None
        if('alternateOutputFilename' in d and d['alternateOutputFilename'] != ''):
            alternateVid = (d['alternateVid'] if 'alternateVid' in d else None)
            alternateNamespace = (d['alternateNamespace'] if 'alternateNamespace' in d else None)
            expandGroovyFile(d['file'], expandedDriversDir, alternateOutputFilename=d['alternateOutputFilename'], \
                             alternateName=d['alternateName'], alternateNamespace=alternateNamespace, \
                             alternateVid=alternateVid)
            aOF = d['alternateOutputFilename']
        else:
            expandGroovyFile(d['file'], expandedDriversDir)
        if(d['id'] != 0):
            j += 1
            r = hubitatAjax.push_driver_code(d['id'], getOutputGroovyFile(d['file'], expandedDriversDir, alternateOutputFilename=aOF))
            try:
                if 'source' in r:
                    r['source'] = '<hidden>'
                print(r)
                id = r['id']
                usedDriversDict[id] = driversDict[id]
            except Exception:
                id = 0
            print("Just worked on Driver ID " + str(id))
    print('Had '+str(j)+' drivers to work on for the apps...')
    
    #pp.pprint(usedDriversDict)
    #print(makeTasmotaConnectDriverListV1(usedDriversDict))

    appsFiles = [
        {'id': 97, 'file': appsDir / 'tasmota-connect.groovy' },
        {'id': 163, 'file': appsDir / 'tasmota-connect-test.groovy' },
    ]

    for a in appsFiles:
        expandGroovyFile(a['file'], expandedAppsDir, usedDriversDict)
        if(a['id'] != 0 and len(usedDriversDict) >= 14):
            print('Found ' + str(len(usedDriversDict)) + ' driver(s)...')
            r = hubitatAjax.push_app_code(a['id'], getOutputGroovyFile(a['file'], expandedAppsDir))
            try:
                id = r['id']
            except Exception:
                id = 0
            print("Just worked on App ID " + str(id))
        else:
            if(a['id'] == 0):
                print("Not making App updates since this app has no ID set yet! Skipped updating App with path: '" + str(a['file']) + "'")
            else:
                print("Not ready for App updates! Only " + str(len(usedDriversDict)) + " driver(s) currently active! Skipped updating App ID " + str(a['id']))
    
    #expandGroovyFile(driverDir / 'tasmota-sonoff-powr2.groovy', expandedDir)
    #hubitatAjax.push_driver_code(513, getOutputGroovyFile(driverDir / 'tasmota-sonoff-powr2.groovy', expandedDir))
    
    #hubitatAjax.logout()
    hubitatAjax.save_session()

if(Path('DEVELOPER').exists()):
    main()