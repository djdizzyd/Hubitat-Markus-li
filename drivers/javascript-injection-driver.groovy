metadata {
    definition(name: "JavaScript Injector", namespace: "markus-li", author: "Markus Liljergren") {
        command "refresh"
        command "clear"
        
        attribute "javascript", "string"
        attribute "javascriptLength", "number"
        attribute "html", "string"
        attribute "htmlLength", "number"
    }
    preferences {   
    }
}


void updated() {
    log.info "Updated..."
    refresh()
}

String escapeForInsert(String myInsert) {
    myInsert = myInsert.replace('"', '\\\"').replace("'", "&#39;")
    escapedInsert = ""
    myInsert.eachLine { 
        escapedInsert += it.trim() // '\\\n'
    } 
    return escapedInsert
}

void refresh() {
    // Do NOT use single-quotes (') in tags or any markup, ONLY use doubles-quotes (")!
    // Don't put CSS directly after "'''", start on the line below
    // The CSS needs to be saved MANUALLY into the Dashboard
    // There's not enough characters to insert it from here...
    String myCSS = '''
.modal {
  font-family: -apple-system,BlinkMacSystemFont,avenir next,avenir,helvetica neue,helvetica,ubuntu,roboto,noto,segoe ui,arial,sans-serif;
  display: none;
  position: absolute;
  background-color: yellow;
  z-index: 9999;
  top: 50%;
  left: 50%;
  width: 200px;
  height: 200px;
}

.modal.is-open {
  display: block;
}
.modal-close-btn {
  background-color: grey;
}
'''  // Make sure this is on an empty last line, no CSS on this line
    
    // Don't put HTML directly after "'''", start on the line below
    String myHTML = '''
<div id="modal-1" class="modal" aria-hidden="true">
  <div tabindex="-1" data-micromodal-close>
    <div role="dialog" aria-modal="true" aria-labelledby="modal-1-title" >
      <header>
        <h2 id="modal-1-title">
          Modal Title
        </h2>
        <button class="modal-close-btn" aria-label="Close modal" data-micromodal-close>Close Me</button>
      </header>
      <div id="modal-1-content">
        Modal Content
      </div>
    </div>
  </div>
</div>
''' // Make sure this is on an empty last line, no HTML on this line
    // <div style="display:none;"></title></style></textarea></script></xmp><svg/onload='+/"/+/onmouseover=1/+/[*/[]/+function(){    
    String myScript = '''
<svg style="display: none;" onload='
var body = document.getElementsByTagName("body")[0];

var script = document.getElementById("inserted-body-script");
var hasScript = script != null;
if(!hasScript) {
    script = document.createElement("script");
    script.setAttribute("id", "inserted-body-script")
}

script.type = "text/javascript";

script.src = "https://cdn.jsdelivr.net/npm/micromodal/dist/micromodal.min.js";
if(!hasScript) {
   body.appendChild(script);
   alert(6);
} else {
    MicroModal.show("modal-1");
    alert(10);
}
script.onload = function() { 
MicroModal.init({debugMode: true});
MicroModal.show("modal-1");
alert(2);
}
'></svg>'''
//}()//</div>'>
//'''
    
    String myHTMLScript = '''
<a href="#" data-micromodal-trigger="modal-1">Open modal dialog</a>
<svg style="display: none;" onload='
var body = document.getElementsByTagName("body")[0];
var div = document.getElementById("inserted-body-html");
var hasDiv = div != null;
if(!hasDiv) {
    div = document.createElement("div")
    div.setAttribute("id", "inserted-body-html")
}
div.innerHTML = "\
''' + escapeForInsert(myHTML) + '''\
\
";
if(!hasDiv) {
    body.prepend(div);
}
var script = document.getElementById("inserted-body-script");
if(script!=null) { 
MicroModal.init({debugMode: true});
MicroModal.show("modal-1");
alert(9);
}
//alert(1);
'></svg>'''
    String myJSMsg = "ms1 ${now()} ${myScript}"
    
    sendEvent(name: "javascript", value: "${myJSMsg}", isStateChange: true)
    sendEvent(name: "javascriptLength", value: "${myJSMsg.length()}", isStateChange: true)
    
    String myHTMLMsg = "ht1 ${now()} ${myHTMLScript}"
    
    sendEvent(name: "html", value: "${myHTMLMsg}", isStateChange: true)
    sendEvent(name: "htmlLength", value: "${myHTMLMsg.length()}", isStateChange: true)
    
    log.debug "Now: ${now()}, JS length: ${myJSMsg.length()}, HTTP length: ${myHTMLMsg.length()} Maximum is 1024"
}

void clear() {
    sendEvent(name: "html", value: "No JS", isStateChange: true)
    sendEvent(name: "javascript", value: "No JS", isStateChange: true)
}

void installed() {
    log.info "Installed..."
    refresh()
}