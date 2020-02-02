/*
    STYLING (helpers-styling)

    Helper functions included in all Drivers and Apps using Styling
*/
def addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

def addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

def makeTextBold(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

def makeTextItalic(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<i>$s</i>"
    } else {
        return "$s"
    }
}

/*
    --END-- STYLING METHODS (helpers-styling)
*/