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

class HubitatCodeBuilder:

    def __init__(self):
        self.v = False

    def getHelperFunctions(self, helper_function_type):
        r = ''
        f = './helpers/helpers-' + helper_function_type + '.groovy'
        if(os.path.isfile(f)):
            with open (f, "r") as rd:
                for l in rd:
                    r += l
        else:
            # Yes, this should be specific, but it doesn't matter here...
            raise Exception("Helper function type '" + helper_function_type + "' can't be included! File doesn't exist!")
        return(r)

    def getOutputGroovyFile(self, input_groovy_file, output_groovy_dir, alternate_output_filename = None):
        #print('Using "' + str(input_groovy_file) + '" to get path for "' + str(output_groovy_dir) + '"...')
        #print('Filename stem: ' + input_groovy_file.stem)
        #print('Filename suffix: ' + input_groovy_file.suffix)
        if(alternate_output_filename != None):
            output_groovy_file = output_groovy_dir / str(alternate_output_filename + "-expanded" + input_groovy_file.suffix)
        else:
            output_groovy_file = output_groovy_dir / str(input_groovy_file.stem + "-expanded" + input_groovy_file.suffix)
        #print('output_groovy_file: ' + str(output_groovy_file))
        return(output_groovy_file)

    def _checkForDefinitionString(self, l, alternate_name = None, alternate_namespace = None, alternate_vid = None):
        definition_position = l.find('definition (')
        if(definition_position != -1):
            ds = l[definition_position+11:].strip()
            # On all my drivers the definition row ends with ") {"
            print('Parsing Definition statement')
            #print('{'+ds[1:-3]+'}')
            definition_dict = yaml.load(('{'+ds[1:-3]+' }').replace(':', ': '), Loader=yaml.FullLoader)
            print(definition_dict)
            if(alternate_name != None):
                definition_dict['name'] = alternate_name
            if(alternate_namespace != None):
                definition_dict['namespace'] = alternate_namespace
            if(alternate_vid != None):
                definition_dict['vid'] = alternate_vid
            #print(definition_dict)
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
            definition_dict_original = definition_dict.copy()
            ds = '[' + str(definition_dict)[1:-1] + ']'
            for k in definition_dict:
                definition_dict[k] = '"x' + definition_dict[k] + 'x"'
            new_definition = (l[:definition_position]) + 'definition (' + yaml.dump(definition_dict, default_flow_style=False, sort_keys=False ).replace('\'"x', '"').replace('x"\'', '"').replace('\n', ', ')[:-2] + ') {\n'
            #print(new_definition)
            output = 'def getDeviceInfoByName(infoName) { \n' + \
                '    // DO NOT EDIT: This is generated from the metadata!\n' + \
                '    // TODO: Figure out how to get this from Hubitat instead of generating this?\n' + \
                '    deviceInfo = ' + ds + '\n' + \
                '    return(deviceInfo[infoName])\n' + \
                '}'
            #new_definition = l
            return(new_definition, output, definition_dict_original)
        else:
            return(None)

    def _makeTasmotaConnectDriverListV1(self, drivers_list):
        ts_driver_list = '['
        for d in drivers_list:
            name = drivers_list[d]['name']
            # If it's a child driver, we don't need it in this list
            if ('child' not in name.lower() and drivers_list[d]['namespace'] == 'tasmota' and \
                name.startswith('DO NOT USE') == False):
                ts_driver_list += '"' + name + '",\n'
        ts_driver_list += ']'
        return(ts_driver_list)

    def _makeTasmotaConnectDriverListV2(self, drivers_list):
        short_driver_map = {
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
        ts_driver_list = ''
        for d in drivers_list:
            name = drivers_list[d]['name']
            try:
                name_short = short_driver_map[name]
            except Exception:
                name_short = name
            # If it's a child driver, we don't need it in this list
            if ('child' not in name.lower() and drivers_list[d]['namespace'] == 'tasmota' and \
                name.startswith('DO NOT USE') == False):
                ts_driver_list += ('else ' if i > 0 else '') + \
                    'if (selectedDevice?.value?.name?.startsWith("' + name_short + '"))\n' + \
                    '    deviceHandlerName = "' + name + '"\n'
        return(ts_driver_list)

    def expandGroovyFile(self, input_groovy_file, output_groovy_dir, extra_data = None, alternate_output_filename = None, \
                        alternate_name = None, alternate_namespace = None, alternate_vid = None, \
                        alternate_template = None, alternate_module = None):
        output_groovy_file = self.getOutputGroovyFile(input_groovy_file, output_groovy_dir, alternate_output_filename)
        r = {'file': output_groovy_file, 'name': ''}
        
        print('Expanding "' + str(input_groovy_file) + '" to "' + str(output_groovy_file) + '"...')
        definitionString = None
        with open (output_groovy_file, "w") as wd:
            with open (input_groovy_file, "r") as rd:
                # Read lines in loop
                for l in rd:
                    if(definitionString == None):
                        definitionString = self._checkForDefinitionString(l, alternate_name = alternate_name, alternate_namespace = alternate_namespace, alternate_vid = alternate_vid)
                        if(definitionString != None):
                            (l, definitionString, definition_dict_original) = definitionString
                            r['name'] = definition_dict_original['name']
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
                            if(extra_data == None):
                                print('ERROR: Missing extra_data!')
                                output = ''
                            else:
                                output = self._makeTasmotaConnectDriverListV1(extra_data)
                        elif(evalCmd == 'makeTasmotaConnectDriverListV2()'):
                            print("Executing makeTasmotaConnectDriverListV2()...")
                            if(extra_data == None):
                                print('ERROR: Missing extra_data!')
                                output = ''
                            else:
                                output = self._makeTasmotaConnectDriverListV2(extra_data)
                        elif(alternate_template != None and alternate_template != '' and evalCmd.startswith('getUpdateNeededSettingsTasmotaDynamicModuleCommand(')):
                            print("Executing getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, '" + alternate_template + "')...")
                            output = getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, alternate_template)
                        elif(alternate_module != None and alternate_module != '' and evalCmd.startswith('getUpdateNeededSettingsTasmotaDynamicModuleCommand(')):
                            print("Executing getUpdateNeededSettingsTasmotaDynamicModuleCommand(" + alternate_module + ")...")
                            output = getUpdateNeededSettingsTasmotaDynamicModuleCommand(alternate_module)
                        else:
                            try:
                                output = eval(evalCmd)
                            except NameError:
                                output = eval('self.' + evalCmd)
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
        print('DONE expanding "' + input_groovy_file.name + '" to "' + output_groovy_file.name + '"!')
        return(r)

    def makeDriverList(self, generic_drivers, specific_drivers, base_repo_url, base_raw_repo_url):
        with open ('DRIVERLIST', "w") as wd:
            wd.write('**Generic Drivers**\n')
            for d in sorted(generic_drivers, key = lambda i: i['name']) :
                url = base_repo_url + d['file']
                urlRaw = base_raw_repo_url + d['file']
                wd.write('* [' + d['name'] + '](' + url + ') - Import URL: [RAW](' + urlRaw + ')\n')
            wd.write('\n**Device-specific Drivers**\n')
            for d in sorted(specific_drivers, key = lambda i: i['name']):
                url = base_repo_url + d['file']
                urlRaw = base_raw_repo_url + d['file']
                if(d['name'] != 'TuyaMCU Wifi Touch Switch Legacy (Child)' and \
                    d['name'].startswith('DO NOT USE') == 0):
                    wd.write('* [' + d['name'] + '](' + url + ') - Import URL: [RAW](' + urlRaw + ')\n')
