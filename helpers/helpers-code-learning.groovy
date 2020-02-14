/**
 * CODE LEARNING METHODS (helpers-code-learning)
 *
 * Helper functions for Code Learning
 */
def actionStartLearning() {
    return(actionStartLearning(true))
}

def actionStartLearning(resetActionData) {
    def cmds = []
    if(learningMode) {
        actionName = getCurrentActionName()
        cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'.")
        logging("actionStartLearning", 1)
        cmds << sendEvent(name: "actionSeen", value: 0)
        if(resetActionData) cmds << sendEvent(name: "actionData", value: JsonOutput.toJson(null))
        actionSeen = device.currentValue('actionSeen', true)
        actionData = device.currentValue('actionData', true)
        logging("actionStartLearning actionData=${actionData}", 1)
    } else {
        log.warn "Learning Mode not active, can't start Learning!"
    }
    return cmds
}

def actionPauseUnpauseLearning() {
    def cmds = []
    // This will pause/unpause Learning, good for Contact sensors for example...
    status = device.currentValue('status', true)
    if(status == "Learning Mode: Paused") {
        actionName = getCurrentActionName()
        cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'.")
    } else {
        cmds << sendEvent(name: "status", value: "Learning Mode: Paused")
    }
    return cmds
}

def actionSave() {
    def cmds = []
    logging("actionSave()", 1)
    if(learningMode) {
        actionName = getCurrentActionName()
        def slurper = new JsonSlurper()
        actionData = device.currentValue('actionData', true)
        if(actionData != null) actionData = slurper.parseText(actionData)

        if(actionData && actionData != "Saved") {
            frequentData = null
            maxActionNumSeen = 0
            actionData.each {
                logging("it=${it}", 1)
                if(it.containsKey('data')) {
                    if(it.containsKey('seen') && it['seen'] >= maxActionNumSeen) {
                        maxActionNumSeen = it['seen']
                        frequentData = it['data']
                    }
                }
            }
            if(frequentData != null) {
                logging("actionSave() saving this data: '${frequentData}'", 100)
                //cmds << device.clearSetting('actionCodeDefault')
                //cmds << device.removeSetting('actionCodeDefault')
                //cmds << device.updateSetting('actionCodeDefault', frequentData)
                state.actions = state.actions ?: [:]
                state.actions[actionName] = frequentData
                cmds << sendEvent(name: "actionSeen", value: 0)
                cmds << sendEvent(name: "actionData", value: JsonOutput.toJson('Saved'))
                cmds << sendEvent(name: "status", value: "Learning Mode: Saved Action Code for Action '${actionName}'. Refresh the page to see it in State Variables.")
            } else {
                log.warn "No Action codes found in actionData!"
                cmds << sendEvent(name: "status", value: "Learning Mode: FAILED to save Action Code for Action '${actionName}'. See the log.")
            }
        } else {
            log.warn "No Action codes found!"
            if(actionData && actionData == "Saved") {
                // Do nothing...
            } else {
                cmds << sendEvent(name: "status", value: "Learning Mode: FAILED to save Action Code for Action '${actionName}'. See the log.")
            }
        }
    } else {
        log.warn "Learning Mode not active, can't save Action Code!"
    }
    return cmds
}


def actionLearn(data) {
    def cmds = []
    status = device.currentValue('status', true)
    if(status == "Learning Mode: Paused") return cmds
    actionName = getCurrentActionName()
    // Can't do this inside a mutex lock, but just don't press so quickly and it will be ok...
    def slurper = new JsonSlurper()
    actionSeen = device.currentValue('actionSeen', true)
    actionData = device.currentValue('actionData', true)
    if(actionData != null) actionData = slurper.parseText(actionData)

    if(actionSeen == null || actionSeen == 'null') {
        actionSeen = 0
    }
    if(actionData == null || actionData == 'null' || 
       actionData == 'Saved' || actionData == '"Saved"' ||
       actionData == 'N/A' || actionData == '"N/A"') {
        actionData = []
    }
    logging("actionSeen=${actionSeen}", 1)
    logging("actionData=${actionData}", 1)
    // All is same for all types, no need to have special cases...
    //if(data.type == 'parsed_portisch') {
    found = false
    actionData.each {
        logging("it=${it}", 1)
        if(it.containsKey('data') && data['Data'] == it['data']) {
            found = true
            it['seen'] = it['seen'] + 1
        }
        if(it.containsKey('seen') && actionSeen < it['seen']) actionSeen = it['seen']
    }
    if (!found) {
        actionData.add([seen: 1, data: data.Data])
        if (actionSeen < 1) actionSeen = 1
    }
    
    cmds << sendEvent(name: "status", value: "Learning Mode: Learning Action '${actionName}'. The most frequent Action seen ${actionSeen} time(s)!")
    cmds << sendEvent(name: "actionSeen", value: actionSeen)
    cmds << sendEvent(name: "actionData", value: JsonOutput.toJson(actionData))
    
    if (state.containsKey("events")) {
        state.remove("events")
    }
    return cmds
}

def actionHandler(data) {
    def cmds = []
    logging("actionHandler(data='${data}')", 1)
    actionName = getCurrentActionName()
    if(data && data.containsKey('Data') && state.actions) {
        // && data['Data'] == state.actions[actionName]
        currentData = data['Data']
        state.actions.each {
            if (it.value == currentData) {
                logging('Button pushed: ${it.value)', 1)
                "${it.key[0].toLowerCase() + it.key.substring(1)}Action"()
            }
        }
    }
    return cmds
}


def parseParentData(parentData) {
    def cmds = []
    //logging("parseParentData(parentData=${parentData})", 100)
    if (parentData.containsKey("type")) {
        if(parentData.type == 'parsed_portisch' || 
           parentData.type == 'raw_portisch' || 
           parentData.type == 'rflink') {
            logging("${parentData.type}=${parentData}", 100)
            if(learningMode) {
                cmds << actionLearn(parentData)
            } else {
                cmds << actionHandler(parentData)
            }
        } else {
            log.error("Unknown Format=${parentData}")
        }
    } else {
        log.error("Unknown parentData=${parentData}")
    }
    return cmds
}

void updated() {
    logging('Inside updated()...', 1)
    if(!learningMode) {
        sendEvent(name: "actionSeen", value: 0)
        sendEvent(name: "actionData", value: JsonOutput.toJson('N/A'))
        sendEvent(name: "status", value: "Action Mode")
    } else {
        //sendEvent(name: "actionSeen", value: 0)
        //sendEvent(name: "actionData", value: JsonOutput.toJson(null))
        //sendEvent(name: "status", value: "Learning Mode")
        actionStartLearning(false)  // Do NOT reset actionData
        if(learningModeAdvanced) {
            if(actionResetAll) {
                log.warn "ALL saved Actions have been DELETED!"
                state.actions = [:]
                device.clearSetting('actionResetAll')
                device.removeSetting('actionResetAll')
            }
            if(actionCodeSetManual && actionCodeSetManual != "") {
                actionName = getCurrentActionName()
                state.actions = state.actions ?: [:]
                state.actions[actionName] = actionCodeSetManual
                sendEvent(name: "actionSeen", value: 0)
                sendEvent(name: "actionData", value: JsonOutput.toJson('Saved'))
                sendEvent(name: "status", value: "Learning Mode: Saved Action Code for Action '${actionName}'. Refresh the page to see it in State Variables.")
                device.clearSetting('actionCodeSetManual')
                device.removeSetting('actionCodeSetManual')
            }
        }
    }
}

/**
 * --END-- CODE LEARNING METHODS (helpers-code-learning)
 */