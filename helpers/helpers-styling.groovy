/*
    STYLING (helpers-styling)

    Helper functions included in all Drivers and Apps using Styling
*/
String addTitleDiv(title) {
    return '<div class="preference-title">' + title + '</div>'
}

String addDescriptionDiv(description) {
    return '<div class="preference-description">' + description + '</div>'
}

String makeTextBold(s) {
    // DEPRECATED: Should be replaced by CSS styling!
    if(isDriver()) {
        return "<b>$s</b>"
    } else {
        return "$s"
    }
}

String makeTextItalic(s) {
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