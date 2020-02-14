#!include:getHelperFunctions('all-default')
/**
 * APP DEFAULT METHODS (helpers-app-default)
 *
 * Methods used in all APPS
 */

def installed() {
	logging("installed()", 100)
    
	try {
        // In case we have some more to run specific to this App
        installedAdditional()
    } catch (MissingMethodException e) {
        // ignore
    }
}

/**
 * --END-- APP DEFAULT METHODS (helpers-app-default)
 */