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
  Imports
"""
import requests
import os
import json
import pickle
from colorama import init, Fore, Back, Style
import winsound

"""
  Hubitat AJAX helper class
  This class can interact with the Hubitat hub...
  WARNING: Exceptions are not properly implemented in this code, use at your own risk!
"""

class HubitatAJAXHelper:
  hubitat_ip = None
  username = None
  password = None
  API_base_url = None
  session = None
  test_login = False
  is_logged_in = False

  def __init__(self, hubitatIP = None, configFile = None):
    init() # This is the init for Colorama
    if(hubitatIP == None):
      if(configFile != None):
        print("Loading settings from config file")
        try:
          with open(configFile, 'rb') as f:
            data = pickle.load(f)
            self.hubitatIP = data['hubitatIP']
            self.API_base_url = 'http://' + self.hubitatIP
            self.username = data['username']
            self.password = data['password']
        except (FileNotFoundError, pickle.UnpicklingError) as e:
          print("Failed to load config file!")
          raise
      else:
        raise Exception("No config file specified!")
    else:
      self.hubitat_ip = hubitatIP
      self.API_base_url = 'http://' + hubitatIP

  @staticmethod
  def saveConfig(hubitatIP, username, password, configFile):
    print("Saving data to config file: " + str(configFile))
    data = {
      'hubitatIP': hubitatIP,
      'username': username,
      'password': password
    }
    try:
      with open(configFile, 'wb') as f:
        pickle.dump(data, f)
    except FileNotFoundError:
      print("Couldn't save config to disk!")

  def login(self, username = None, password = None):
    APIUrl = self.API_base_url + '/login'
    if(username == None):
      username = self.username
    if(password == None):
      password = self.password
    data = {'username': username,
            'password': password,
            'submit': 'Login'}
    #print(data)
    self._prepare_session()
    if(self.test_login):
      # We might have a working login, do q quick check
      r = self.session.get(self.API_base_url + '/hub/messages')
      if(r.status_code == 200):
        try:
          r.json()
          print("Already logged in, session restored!")
          self.test_login = False
          self.is_logged_in = True
        except json.decoder.JSONDecodeError:
          print("NOT logged in! Session is old or logged out!")
          self.test_login = False
          self.is_logged_in = False
      else:
        print("NOT logged in! Session is old or logged out! Status code: " + str(r.status_code))
        self.test_login = False
        self.is_logged_in = False
    if(self.is_logged_in == False):
      r = self.session.post(APIUrl, data=data)
      print(r)
      if(r.status_code == 200):
        print("Login succeded!")
        self.is_logged_in = True
        self.test_login = False
      else:
        print("Error: Login failed!")
        self.is_logged_in = False
        self.test_login = False
  
  def logout(self):
    APIUrl = self.API_base_url + '/logout'
    self._prepare_session()
    # Logout
    r = self.session.get(APIUrl)
    if(r.status_code == 302 or r.status_code == 200):
      print("Logged out!")
      self.test_login = False
      self.is_logged_in = False
  
  def _prepare_session(self):
    if (self.session == None):
      # Check if we have a saved session
      try:
        with open('__hubitat_session', 'rb') as f:
          self.session = pickle.load(f)
        self.test_login = True
        self.is_logged_in = False
      except (FileNotFoundError, pickle.UnpicklingError) as e:
        self.test_login = False
        self.is_logged_in = False
        self.session = requests.session()
  
  def save_session(self):
    if (self.session != None):
      try:
        with open('__hubitat_session', 'wb') as f:
          pickle.dump(self.session, f)
      except FileNotFoundError:
        print("Couldn't save session to disk!")

  def get_app_current_code(self, codeID):
    return(self._get_current_code('app', codeID))
  
  def get_driver_current_code(self, codeID):
    return(self._get_current_code('driver', codeID))

  def get_app_current_code_version(self, codeID):
    response = self.get_app_current_code(codeID)
    try:
      return response['version']
    except (json.decoder.JSONDecodeError, TypeError) as e:
      print("Not json in response, try to login first?")
      return(-3)

  def get_driver_current_code_version(self, codeID):
    response = self.get_driver_current_code(codeID)
    try:
      return response['version']
    except (json.decoder.JSONDecodeError, TypeError) as e:
      print("Not json in response, try to login first?")
      return(-3)

  def _get_current_code(self, codeType, codeID):
    self._prepare_session()
    if(codeType == 'driver'):
      APIUrl = self.API_base_url + '/driver/ajax/code?id=' + str(codeID)
    elif(codeType == 'app'):
      APIUrl = self.API_base_url + '/app/ajax/code?id=' + str(codeID)
    else:
      raise Exception('Unknown code type: ' + str(codeType))
    
    #print(APIUrl)
    
    response = self.session.get(APIUrl)
    if(response.status_code == 200):
      try:
        return response.json()
      except json.decoder.JSONDecodeError:
        print("Not json in response, try to login first?")
        return(-3)
    else:
      return -1

  def get_driver_list(self):
    self._prepare_session()
    APIUrl = self.API_base_url + '/device/drivers'
    
    #print(APIUrl)
    
    response = self.session.get(APIUrl)
    if(response.status_code == 200):
      try:
        rdict = response.json()['drivers']
        ndict = {}
        for d in rdict:
          ndict[d['id']] = d
        return(ndict)
      except json.decoder.JSONDecodeError:
        print("Not json in response for get_driver_list(), try to login first?")
        return(-3)
    else:
      print("Unknown problem occured in get_driver_list(): " + str(response))
      return -1

  # http://192.168.10.1/device/drivers

  def push_app_code(self, codeID, groovyFileToPublish):
    return(self._push_code('app', codeID, groovyFileToPublish))
  
  def push_driver_code(self, codeID, groovyFileToPublish):
    return(self._push_code('driver', codeID, groovyFileToPublish))

  def _push_code(self, codeType, codeID, groovyFileToPublish):
    self._prepare_session()
    if(codeType == 'driver'):
      APIUrl = self.API_base_url + '/driver/ajax/update'
    elif(codeType == 'app'):
      APIUrl = self.API_base_url + '/app/ajax/update'
    else:
      raise Exception('Unknown code type: ' + str(codeType))
    currentCode = self._get_current_code(codeType, codeID)
    codeVersion = currentCode['version']
    
    if(codeVersion != -1):
      with open (groovyFileToPublish, "r") as f:
        source = f.read()
      # Compare current source to the old one
      if(currentCode['source'] == source):
        print(Fore.GREEN + "The source for code ID '" + str(codeID) + "' is already up-to-date! No update needed!" + Fore.RESET)
        return(currentCode)
      else:
        data = {
          'id': codeID,
          'version': codeVersion,
          'source': source
        }
        
        print("Starting the publishing of '" + str(groovyFileToPublish) + "' to ID '" + str(codeID) + "' with current version being '" + str(codeVersion) + "'")
        response = self.session.post(APIUrl, data=data)
        try:
          jsonResponse = response.json()
        except json.decoder.JSONDecodeError:
          print("ERROR: Not json in the response for publishing '" + str(groovyFileToPublish) + "' to ID '" + str(codeID) + "' with current version '" + str(codeVersion) + "'")
          return(-3)
        if(jsonResponse['status'] == 'success'):
          print(Fore.GREEN + "The source for code ID '" + str(codeID) + "' has been UPDATED!" + Fore.RESET)
          return(jsonResponse)
        elif(jsonResponse['status'] == 'error'):
          print(Fore.RED + "Failed to update code with error: '" + str(jsonResponse['errorMessage']).strip() + "'" + Fore.RESET)
          #print('\a')
          winsound.Beep(500, 300)
          return(-1)
        else:
          print("Unknown problem occured: " + str(jsonResponse))
          return(-2)
    else:
      print("Can't get the code version for ID: " + str(codeID))
