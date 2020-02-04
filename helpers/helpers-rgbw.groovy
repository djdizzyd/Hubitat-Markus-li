/*
    RGBW METHODS (helpers-rgbw)

    Helper functions included in all drivers using RGB, RGBW or Dimmers
    These methods are NOT specific to Tasmota
*/
void setColor(value) {
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

void setHue(h) {
    logging("setHue('${h}')", 10)
    setHSB(h, null, null)
}

void setSaturation(s) {
    logging("setSaturation('${s}')", 10)
    setHSB(null, s, null)
}

void setLevel(b) {
    logging("setLevel('${b}')", 10)
    //return(setHSB(null, null, b))
    setLevel(b, 0)
}

def rgbToHSB(red, green, blue) {
    // All credits for this function goes to Joe Julian (joejulian):
    // https://gist.github.com/joejulian/970fcd5ecf3b792bc78a6d6ebc59a55f
    BigDecimal r = red / 255f
    BigDecimal g = green / 255f
    BigDecimal b = blue / 255f
    BigDecimal max = [r, g, b].max()
    BigDecimal min = [r, g, b].min()
    BigDecimal delta = max - min
    def hue = 0
    def saturation = 0
    if (max == min) {
        hue = 0
    } else if (max == r) {
        def h1 = (g - b) / delta / 6
        def h2 = h1.asType(Integer)
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
        "hue": hue.asType(Integer),
        "saturation": saturation.asType(Integer),
        "level": level.asType(Integer),
    ]
}

// Fixed colours
void white() {
    logging("white()", 10)
    // This is separated to be able to reuse functions between platforms
    whiteForPlatform()
}

void red() {
    logging("red()", 10)
    setRGB(255, 0, 0)
}

void green() {
    logging("green()", 10)
    setRGB(0, 255, 0)
}

void blue() {
    logging("blue()", 10)
    setRGB(0, 0, 255)
}

void yellow() {
    logging("yellow()", 10)
    setRGB(255, 255, 0)
}

void lightBlue() {
    logging("lightBlue()", 10)
    setRGB(0, 255, 255)
}

void pink() {
    logging("pink()", 10)
    setRGB(255, 0, 255)
}

/*
    --END-- RGBW METHODS (helpers-rgbw)
*/