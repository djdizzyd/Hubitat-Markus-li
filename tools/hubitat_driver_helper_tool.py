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

def getOutputGroovyFile(inputGroovyFile, outputGroovyDir):
    #print('Using "' + str(inputGroovyFile) + '" to get path for "' + str(outputGroovyDir) + '"...')
    #print('Filename stem: ' + inputGroovyFile.stem)
    #print('Filename suffix: ' + inputGroovyFile.suffix)
    outputGroovyFile = outputGroovyDir / str(inputGroovyFile.stem + "-expanded" + inputGroovyFile.suffix)
    #print('outputGroovyFile: ' + str(outputGroovyFile))
    return(outputGroovyFile)

def checkForDefinitionString(l):
    definitionPosition = l.find('definition (')
    if(definitionPosition != -1):
        ds = l[definitionPosition+11:].strip()
        # Process this string
        # (name: "Tasmota - Tuya Wifi Touch Switch TEST (Child)", namespace: "tasmota", author: "Markus Liljergren") {
        PATTERN = re.compile(r'''((?:[^,"']|"[^"]*"|'[^']*')+)''')
        PATTERN2 = re.compile(r'''((?:[^(){}:"']|"[^"]*"|'[^']*')+)''')
        l1 = PATTERN.split(ds)[1::2]
        d = {}
        for p1 in l1:
            p1 = p1.strip()
            i = 0
            previousp2 = None
            for p2 in PATTERN2.split(p1)[1::2]:
                p2 = p2.strip()
                if(p2 != ''):
                    if(i==0):
                        previousp2 = p2.strip('"')
                    else:
                        #print('"' + previousp2 + '"="' + p2.strip('"') + '"')
                        d[previousp2] = p2.strip('"')
                    i += 1
        ds = '[' + str(d)[1:-1] + ']'
        output = 'def getDeviceInfoByName(infoName) { \n' + \
            '    // DO NOT EDIT: This is generated from the metadata!\n' + \
            '    // TODO: Figure out how to get this from Hubitat instead of generating this?\n' + \
            '    deviceInfo = ' + ds + '\n' + \
            '    return(deviceInfo[infoName])\n' + \
            '}'
        return(output)
    else:
        return(None)

def expandGroovyFile(inputGroovyFile, outputGroovyDir):
    outputGroovyFile = getOutputGroovyFile(inputGroovyFile, outputGroovyDir)
    print('Expanding "' + str(inputGroovyFile) + '" to "' + str(outputGroovyFile) + '"...')
    definitionString = None
    with open (outputGroovyFile, "w") as wd:
        with open (inputGroovyFile, "r") as rd:
            # Read lines in loop
            for l in rd:
                if(definitionString == None):
                    definitionString = checkForDefinitionString(l)
                includePosition = l.find('#!include:')
                if(includePosition != -1):
                    evalCmd = l[includePosition+10:].strip()
                    if(evalCmd == 'getDeviceInfoFunction()'):
                        print("Executing getDeviceInfoFunction()...")
                        if(definitionString == None):
                            print('ERROR: Missing Definition in file!')
                        output = definitionString
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

def main(run=False):
    #Set run to True to run, this is here to make sure anyone using this has read the sourcecode...
    if(run):
        #startDir = os.getcwd()
        driverDir = Path("./drivers")
        appDir = Path("./apps")
        expandedDir = driverDir / 'expanded'
        #HubitatAJAXHelper.saveConfig('192.168.1.1', 'username', 'password', 'hubitat_ajax_sample.cfg')
        hubitatAjax = HubitatAJAXHelper(None, 'hubitat_ajax.cfg')
        print(hubitatAjax.login())

        #codeVersion = hubitatAjax.get_driver_current_code_version(550)
        #print(codeVersion)
        
        #hubitatAjax.push_app_code(97, appDir / 'tasmota-connect.groovy')
        #hubitatAjax.push_app_code(163, appDir / 'tasmota-connect-test.groovy')
        
        driverFiles = [
            {'id': 550, 'file': driverDir / 'tasmota-tuya-wifi-touch-switch-child-test.groovy' },
            {'id': 513, 'file': driverDir / 'tasmota-sonoff-powr2.groovy' },
            {'id': 548, 'file': driverDir / 'tasmota-tuya-wifi-touch-switch.groovy' },
            {'id': 549, 'file': driverDir / 'tasmota-tuya-wifi-touch-switch-child.groovy' },
        ]
        for d in driverFiles:
            expandGroovyFile(d['file'], expandedDir)
            hubitatAjax.push_driver_code(d['id'], getOutputGroovyFile(d['file'], expandedDir))
        #expandGroovyFile(driverDir / 'tasmota-sonoff-powr2.groovy', expandedDir)
        #hubitatAjax.push_driver_code(513, getOutputGroovyFile(driverDir / 'tasmota-sonoff-powr2.groovy', expandedDir))
        
        #hubitatAjax.logout()
        hubitatAjax.save_session()

main()