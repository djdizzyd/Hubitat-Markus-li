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
  Hubitat driver and app developer tool
  WARNING: Do NOT run this script unless you know what it does, it may DELETE your data!
  NOTE: This is a Work In Progress, feel free to use it, but don't rely on it not changing completely!
"""
# External modules
from pathlib import Path

# Internal modules
from hubitat_hubspider import HubitatHubSpider
from hubitat_codebuilder import HubitatCodeBuilder
from hubitat_codebuilder_tasmota import HubitatCodeBuilderTasmota

# NOTE: All functions use mixedCaps since this is used with Groovy and it makes
#       it less confusing to not changing style all the time. 

def main():
    # Get us a Code Builder
    cb = HubitatCodeBuilderTasmota()
    
    #HubitatAJAXHelper.saveConfig('192.168.1.1', 'username', 'password', 'hhs_sample.cfg')
    hhs = HubitatHubSpider(None, 'hubitat_hubspider.cfg')
    # Check the result from login()
    print(hhs.login())

    #code_version = hubitatAjax.get_driver_current_code_version(550)
    #print(code_version)
    
    drivers_dict = hhs.get_driver_list()
    #pp.pprint()
    used_driver_list = {}

    driver_files = [
        # Drivers without their own base-file:
        {'id': 418, 'file': 'tasmota-tuyamcu-wifi-touch-switch-child.groovy', \
         'alternate_output_filename': 'tasmota-tuyamcu-wifi-touch-switch-legacy-child', \
         'alternate_name': 'Tasmota - TuyaMCU Wifi Touch Switch Legacy (Child)', \
         'alternate_namespace': 'tasmota-legacy'},
        {'id': 556, 'file': 'tasmota-sonoff-basic.groovy', \
         'alternate_output_filename': 'tasmota-sonoff-basic-r3', \
         'alternate_name': 'Tasmota - Sonoff Basic R3'},
        # https://templates.blakadder.com/ce_smart_home-WF500D.html
        {'id': 580, 'file': 'tasmota-tuyamcu-wifi-dimmer.groovy' , \
         'alternate_output_filename': 'tasmota-tuyamcu-ce-wf500d-dimmer', \
         'alternate_name': 'Tasmota - TuyaMCU CE Smart Home WF500D Dimmer (EXPERIMENTAL)', \
         'alternate_template': '{"NAME":"CE WF500D","GPIO":[255,255,255,255,255,255,0,0,255,108,255,107,255],"FLAG":0,"BASE":54}'},
        # https://templates.blakadder.com/ce_smart_home_LA-2-W3.html
        {'id': 581, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-ce-la-2-w3-wall-outlet', \
         'alternate_name': 'Tasmota - CE Smart Home LA-2-W3 Wall Outlet', \
         'alternate_template': '{"NAME":"CE LA-2-W3","GPIO":[255,255,255,255,157,17,0,0,21,255,255,255,255],"FLAG":15,"BASE":18}'},
        # https://templates.blakadder.com/ce_smart_home_LQ-2-W3.html
        {'id': 582, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-ce-lq-2-w3-wall-outlet', \
         'alternate_name': 'Tasmota - CE Smart Home LQ-2-W3 Wall Outlet', \
         'alternate_template': '{"NAME":"CE LQ-2-W3","GPIO":[255,255,255,255,255,17,255,255,21,255,255,255,255],"FLAG":15,"BASE":18}'},
        # https://templates.blakadder.com/hugoai_awp02l-n.html
        {'id': 583, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-awp02l-n-plug', \
         'alternate_name': 'Tasmota - AWP02L-N Plug', \
         'alternate_template': '{"NAME":"AWP02L-N","GPIO":[57,0,56,0,0,0,0,0,0,17,0,21,0],"FLAG":1,"BASE":18}'},
        # https://templates.blakadder.com/cyyltd_bifans_J23.html
        {'id': 584, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-cyyltf-bifans-j23-plug', \
         'alternate_name': 'Tasmota - CYYLTF BIFANS J23 Plug', \
         'alternate_template': '{"NAME":"CYYLTF J23","GPIO":[56,0,0,0,0,0,0,0,21,17,0,0,0],"FLAG":1,"BASE":18}'},
        # https://templates.blakadder.com/gosund_wp3.html
        {'id': 585, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-gosund-wp3-plug', \
         'alternate_name': 'Tasmota - Gosund WP3 Plug', \
         'alternate_template': '{"NAME":"Gosund WP3","GPIO":[0,0,0,0,17,0,0,0,56,57,21,0,0],"FLAG":0,"BASE":18}'},
        # https://templates.blakadder.com/SK03_outdoor.html
        {'id': 586, 'file': 'tasmota-generic-pm-plug.groovy' , \
         'alternate_output_filename': 'tasmota-sk03-pm-outdoor-plug', \
         'alternate_name': 'Tasmota - SK03 Power Monitor Outdoor Plug', \
         'alternate_template': '{"NAME":"SK03 Outdoor","GPIO":[17,0,0,0,133,132,0,0,131,57,56,21,0],"FLAG":0,"BASE":57}'},
        # https://templates.blakadder.com/aoycocr_X10S.html
        {'id': 587, 'file': 'tasmota-generic-pm-plug.groovy' , \
         'alternate_output_filename': 'tasmota-aoycocr-x10s-pm-plug', \
         'alternate_name': 'Tasmota - Aoycocr X10S Power Monitor Plug', \
         'alternate_template': '{"NAME":"Aoycocr X10S","GPIO":[56,0,57,0,21,134,0,0,131,17,132,0,0],"FLAG":0,"BASE":45}'},
        # https://templates.blakadder.com/brilliant_20699.html
        {'id': 589, 'file': 'tasmota-generic-rgb-rgbw-controller-bulb-dimmer.groovy',
         'alternate_output_filename': 'tasmota-brilliant-20699-rgbw-bulb', \
         'alternate_name': 'Tasmota - Brilliant 20699 800lm RGBW Bulb', \
         'alternate_template': '{"NAME":"Brilliant20699","GPIO":[0,0,0,0,141,140,0,0,37,142,0,0,0],"FLAG":0,"BASE":18}'},
        {'id': 592, 'file': 'tasmota-generic-wifi-switch-plug.groovy' , \
         'alternate_output_filename': 'tasmota-sonoff-sv', \
         'alternate_name': 'Tasmota - Sonoff SV', \
         'alternate_module': '3'},
        {'id': 361, 'file': 'tasmota-generic-thp-device.groovy' , \
         'alternate_output_filename': 'tasmota-sonoff-th', \
         'alternate_name': 'Tasmota - Sonoff TH', \
         'alternate_module': '4'},
        {'id': 547, 'file': 'tasmota-sonoff-powr2.groovy' , \
         'alternate_output_filename': 'tasmota-sonoff-pow', \
         'alternate_name': 'Tasmota - Sonoff POW', \
         'alternate_module': '6'},
        {'id': 359, 'file': 'tasmota-sonoff-powr2.groovy' , \
         'alternate_output_filename': 'tasmota-sonoff-s31', \
         'alternate_name': 'Tasmota - Sonoff S31', \
         'alternate_module': '41'},

        # https://templates.blakadder.com/aoycocr_X10S.html

        # Drivers WITH their own base-file
        {'id': 548, 'file': 'tasmota-tuyamcu-wifi-touch-switch.groovy' },
        {'id': 549, 'file': 'tasmota-tuyamcu-wifi-touch-switch-child.groovy' },
        {'id': 550, 'file': 'tasmota-tuyamcu-wifi-touch-switch-child-test.groovy' },
        {'id': 513, 'file': 'tasmota-sonoff-powr2.groovy' },
        {'id': 551, 'file': 'tasmota-sonoff-s2x.groovy' },
        {'id': 554, 'file': 'tasmota-sonoff-mini.groovy'},
        {'id': 560, 'file': 'tasmota-sonoff-basic.groovy'},
        {'id': 552, 'file': 'tasmota-generic-wifi-switch-plug.groovy' },
        {'id': 553, 'file': 'tasmota-s120-plug.groovy' },
        {'id': 557, 'file': 'tasmota-ykyc-001-pm-plug.groovy' },
        {'id': 559, 'file': 'tasmota-brilliant-bl20925-pm-plug.groovy' },
        {'id': 577, 'file': 'tasmota-prime-ccrcwfii113pk-plug.groovy' },
        {'id': 558, 'file': 'tasmota-generic-pm-plug.groovy' },
        {'id': 578, 'file': 'tasmota-generic-thp-device.groovy' },
        {'id': 590, 'file': 'tasmota-tuyamcu-wifi-dimmer.groovy'},
        {'id': 588, 'file': 'tasmota-unbranded-rgb-controller-with-ir.groovy' },
        {'id': 591, 'file': 'tasmota-generic-rgb-rgbw-controller-bulb-dimmer.groovy' },

        # Zigbee
        {'id': 579, 'file': 'zigbee-generic-wifi-switch-plug.groovy' },

        # The following can be overwritten: 555, 362
    ]
    
    base_repo_url = 'https://github.com/markus-li/Hubitat/blob/master/drivers/expanded/'
    base_raw_repo_url = 'https://raw.githubusercontent.com/markus-li/Hubitat/master/drivers/expanded/'
    expected_num_drivers = len(driver_files)
    
    # Example driver: https://github.com/hubitat/HubitatPublic/blob/master/examples/drivers/GenericZigbeeRGBWBulb.groovy
    # RGB Example: https://github.com/damondins/hubitat/blob/master/Tasmota%20RGBW%20LED%20Light%20Bulb/Tasmota%20RGBW%20LED%20Light%20Bulb

    #driver_files = [
    #    {'id': 578, 'file': 'tasmota-generic-thp-device.groovy' },
        
    #]
    #expected_num_drivers = 1

    j=0
    generic_drivers = []
    specific_drivers = []

    for d in driver_files:
        aof = None
        if('alternate_output_filename' in d and d['alternate_output_filename'] != ''):
            alternate_vid = (d['alternate_vid'] if 'alternate_vid' in d else None)
            alternate_namespace = (d['alternate_namespace'] if 'alternate_namespace' in d else None)
            alternate_template = (d['alternate_template'] if 'alternate_template' in d else None)
            alternate_module = (d['alternate_module'] if 'alternate_module' in d else None)
            expanded_result = cb.expandGroovyFile(d['file'], code_type='driver', alternate_output_filename=d['alternate_output_filename'], \
                             alternate_name=d['alternate_name'], alternate_namespace=alternate_namespace, \
                             alternate_vid=alternate_vid, alternate_template=alternate_template, \
                             alternate_module=alternate_module)
            aof = d['alternate_output_filename']
        else:
            expanded_result = cb.expandGroovyFile(d['file'])
        print(expanded_result)
        if(d['id'] != 0):
            j += 1
            print('push_to_dir:' + str(cb.getBuildDir('driver') / cb.getOutputGroovyFile(d['file'], alternate_output_filename=aof)))
            r = hhs.push_driver_code(d['id'], cb.getBuildDir('driver') / cb.getOutputGroovyFile(d['file'], alternate_output_filename=aof))
            try:
                if 'source' in r:
                    r['source'] = '<hidden>'
                #print(r)
                id = r['id']
                if(expanded_result['name'].startswith('Tasmota - ')):
                    newD = {'name': expanded_result['name'][10:], 'file': expanded_result['file'].stem + expanded_result['file'].suffix}
                    if(newD['name'].startswith('Generic')):
                        generic_drivers.append(newD)
                    else:
                        specific_drivers.append(newD)

                used_driver_list[id] = drivers_dict[id]
            except Exception as e:
                print('Exception when making driver list!: ' + str(e))
                id = 0
            print("Just worked on Driver ID " + str(id))
    print('Had '+str(j)+' drivers to work on for the apps...')
    if(len(used_driver_list) >= expected_num_drivers):
        print('Making the driver list file...')
        cb.makeDriverList(generic_drivers, specific_drivers, base_repo_url, base_raw_repo_url)
    else:
        print("SKIPPING making of the driver list file since we don't have enough drivers in the list...")
    #print('Generic drivers: ' + str(generic_drivers))
    #print('Specific drivers: ' + str(specific_drivers))
    #pp.pprint(used_driver_list)
    
    apps_files = [
        {'id': 97, 'file': 'tasmota-connect.groovy' },
        {'id': 163, 'file': 'tasmota-connect-test.groovy' },
    ]
    cb.setUsedDriverList(used_driver_list)
    for a in apps_files:
        if(a['id'] != 97 or a['id'] != 163):
            cb.expandGroovyFile(a['file'], 'app')
        if(a['id'] != 0 and len(used_driver_list) >= expected_num_drivers):
            cb.expandGroovyFile(a['file'], 'app')
            print('Found ' + str(len(used_driver_list)) + ' driver(s)...')
            r = hhs.push_app_code(a['id'], cb.getBuildDir('app') / cb.getOutputGroovyFile(a['file']))
            try:
                id = r['id']
            except Exception:
                id = 0
            print("Just worked on App ID " + str(id))
        else:
            if(a['id'] == 0):
                print("Not making App updates since this app has no ID set yet! Skipped updating App with path: '" + str(a['file']) + "'")
            else:
                print("Not ready for App updates! Only " + str(len(used_driver_list)) + " driver(s) currently active! Skipped updating App ID " + str(a['id']))
    
    #cb.expandGroovyFile('tasmota-sonoff-powr2.groovy', expanded_dir)
    #hhs.push_driver_code(513, cb.getOutputGroovyFile('tasmota-sonoff-powr2.groovy', expanded_dir))
    
    #hhs.logout()
    hhs.save_session()

if(Path('DEVELOPER').exists()):
    main()