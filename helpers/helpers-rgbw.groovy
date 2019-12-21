/* Helper functions included in all drivers using RGB, RGBW or Dimmers */
def setColor(value) {
    logging("setColor('${value}')", 10)
	if (value != null && value instanceof Map) {
        def h = value.containsKey("hue") ? value.hue : 0
        def s = value.containsKey("saturation") ? value.saturation : 0
        def b = value.containsKey("level") ? value.level : 0
        setHSB(h, s, b)
    } else {
        logging("setColor('${value}') called with an INVALID argument!", 10)
    }
}

def setHue(h) {
    logging("setHue('${h}')", 10)
    return(setHSB(h, null, null))
}

def setSaturation(s) {
    logging("setSaturation('${s}')", 10)
    return(setHSB(null, s, null))
}

def setLevel(b) {
    logging("setLevel('${b}')", 10)
    //return(setHSB(null, null, b))
    return(setLevel(b, 0))
}

def rgbToHSB(red, green, blue) {
    // All credits for this function goes to Joe Julian (joejulian):
    // https://gist.github.com/joejulian/970fcd5ecf3b792bc78a6d6ebc59a55f
    float r = red / 255f
    float g = green / 255f
    float b = blue / 255f
    float max = [r, g, b].max()
    float min = [r, g, b].min()
    float delta = max - min
    def hue = 0
    def saturation = 0
    if (max == min) {
        hue = 0
    } else if (max == r) {
        def h1 = (g - b) / delta / 6
        def h2 = h1.asType(int)
        if (h1 < 0) {
            hue = (360 * (1 + h1 - h2)).round()
        } else {
            hue = (360 * (h1 - h2)).round()
        }
        logging("rgbToHSB: red max=${max} min=${min} delta=${delta} h1=${h1} h2=${h2} hue=${hue}", 1)
    } else if (max == g) {
        hue = 60 * ((b - r) / delta + 2)
        logging("rgbToHSB: green hue=${hue}", 1)
    } else {
        hue = 60 * ((r - g) / (max - min) + 4)
        logging("rgbToHSB: blue hue=${hue}", 1)
    }
    
    // Convert hue to Hubitat value:
    hue = Math.round((hue) / 3.6)

    if (max == 0) {
        saturation = 0
    } else {
        saturation = delta / max * 100
    }
    
    def level = max * 100
    
    return [
        "hue": hue.asType(int),
        "saturation": saturation.asType(int),
        "level": level.asType(int),
    ]
}

// Fixed colours
def white() {
    logging("white()", 10)
    // This is separated to be able to reuse functions between platforms
    return(whiteForPlatform())
}

def red() {
    logging("red()", 10)
    return(setRGB(255, 0, 0))
}

def green() {
    logging("green()", 10)
    return(setRGB(0, 255, 0))
}

def blue() {
    logging("blue()", 10)
    return(setRGB(0, 0, 255))
}

def yellow() {
    logging("yellow()", 10)
    return(setRGB(255, 255, 0))
}

def lightBlue() {
    logging("lightBlue()", 10)
    return(setRGB(0, 255, 255))
}

def pink() {
    logging("pink()", 10)
    return(setRGB(255, 0, 255))
}