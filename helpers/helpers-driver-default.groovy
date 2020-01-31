
#!include:getHelperFunctions('driver-metadata')

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

def update_current_properties(cmd)
{
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

