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

# Local imports
from hubitat_driver_snippets import *
from hubitat_driver_snippets_parser import *
from hubitat_codebuilder import HubitatCodeBuilder

class HubitatCodeBuilderTasmota(HubitatCodeBuilder):

    #def __init__(self, **kwargs):
        # Use this for extracting any arguments you need
        #self.data = kwargs.pop('data', True)
    #    super().__init__(**kwargs)

    def setUsedDriverList(self, used_driver_list):
        self.used_driver_list = used_driver_list

    def _makeTasmotaConnectDriverListV1(self):
        ts_driver_list = '['
        for d in self.used_driver_list:
            name = self.used_driver_list[d]['name']
            # If it's a child driver, we don't need it in this list
            if ('child' not in name.lower() and self.used_driver_list[d]['namespace'] == 'tasmota' and \
                name.startswith('DO NOT USE') == False):
                ts_driver_list += '"' + name + '",\n'
        ts_driver_list += ']'
        return(ts_driver_list)

    def _makeTasmotaConnectDriverListV2(self):
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
        for d in self.used_driver_list:
            name = self.used_driver_list[d]['name']
            try:
                name_short = short_driver_map[name]
            except Exception:
                name_short = name
            # If it's a child driver, we don't need it in this list
            if ('child' not in name.lower() and self.used_driver_list[d]['namespace'] == 'tasmota' and \
                name.startswith('DO NOT USE') == False):
                ts_driver_list += ('else ' if i > 0 else '') + \
                    'if (selectedDevice?.value?.name?.startsWith("' + name_short + '"))\n' + \
                    '    deviceHandlerName = "' + name + '"\n'
        return(ts_driver_list)

    def _runEvalCmdAdditional(self, eval_cmd, definition_string, alternate_template, alternate_module):
        #print('alternate_template 1: ' + str(alternate_template))
        if(eval_cmd == 'makeTasmotaConnectDriverListV1()'):
            self.log.debug("Executing makeTasmotaConnectDriverListV1()...")
            output = self._makeTasmotaConnectDriverListV1()
            return(True, output)
        elif(eval_cmd == 'makeTasmotaConnectDriverListV2()'):
            self.log.debug("Executing makeTasmotaConnectDriverListV2()...")
            output = self._makeTasmotaConnectDriverListV2()
            return(True, output)
        elif(alternate_template != None and alternate_template != '' and eval_cmd.startswith('getUpdateNeededSettingsTasmotaDynamicModuleCommand(')):
            self.log.debug("Executing getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, '" + alternate_template + "')...")
            output = getUpdateNeededSettingsTasmotaDynamicModuleCommand(0, alternate_template)
            return(True, output)
        elif(alternate_module != None and alternate_module != '' and eval_cmd.startswith('getUpdateNeededSettingsTasmotaDynamicModuleCommand(')):
            self.log.debug("Executing getUpdateNeededSettingsTasmotaDynamicModuleCommand(" + alternate_module + ")...")
            output = getUpdateNeededSettingsTasmotaDynamicModuleCommand(alternate_module)
            return(True, output)
        else:
            return(False, None)

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
    