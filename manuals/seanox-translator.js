if (typeof String.prototype.capitalize !== "function")
    String.prototype.capitalize = function(smart) {
        if (this.length <= 0)
            return this;
        if (!smart)
            return this.charAt(0).toUpperCase() + this.slice(1);
        return this.replace(/\b\w/g, function(match) {
            return match.toUpperCase();
        });
    };
    
if (typeof String.prototype.indent !== "function")
    String.prototype.indent = function(offset) {
        if (!offset
                || offset <= 0)
            return String(this);
        var text = this.split(/\s*[\r\n]+\s*/);
        if (!Array.isArray(text))
            text = new Array(text);
        text.forEach(function(text, index, array) {
            var space = "";
            while (space.length < offset)
                space += " ";
            var result = "";
            while (text.length +offset > 80) {
                var match = text.substring(0, 80 -offset).match(/^.*(?=\s)/);
                if (!match)
                    match = text.match(/^.*?(?=\s)/);
                if (!match)
                    match = text;
                match = String(match);
                result += space + match + "\r\n";
                text = text.substring(match.length).trim(); 
            }
            if (text)
                result += space + text + "\r\n";
            array[index]= result;
        });
        return text.join("\r\n");    
    };    

window.addEventListener("load", function() {
    var translate = function(element, filter) {
        var text = element.innerHTML;
        var indent = 0;
        if (text.match(/^ *[\r\n]+( +)(?=\S)/))
            indent = text.match(/^ *[\r\n]+( +)(?=\S)/)[1].length;        
        var data = {
            "jsonrpc": "2.0",
            "method": "LMT_handle_jobs",
            "params": {
                "jobs": [{"kind":"default", "raw_en_sentence":""}],
                "lang": {
                    "user_preferred_langs": ["EN","DE"],
                    "source_lang_user_selected": "DE",
                    "target_lang":"DE"
                }
            }        
        };
        text = text.replace(/\s+/ig, ' ').trim();
        text = text.replace(/\s*<br>\s*/ig, '\r\n').trim();
        var placeholder = new Array();
        text = text.replace(/#\d+[?= ]/g, function(match) {
            placeholder[placeholder.length] = match;
            return "#{" + (placeholder.length -1) + "}";
        });
        text = text.replace(/<([a-z0-9\-]+)(.|\r|\n)*?<\/\1\b[^>]*>/ig, function(match) {
            placeholder[placeholder.length] = match;
            return "#" + (placeholder.length -1) + " ";
        });
        text = text.replace(/<[^>]+?>/ig, function(match) {
            placeholder[placeholder.length] = match;
            return "#" + (placeholder.length -1) + " ";
        });
        var mapping = function(mapping, text) {
            if (!mapping)
                return text;
            mapping.forEach(function(mapping, index, array) {
                text = text.replace(mapping[0], mapping[1]);
            });
            return text;
        };
        if (filter
                && filter.mapping)
            text = mapping(filter.mapping, text);
        if (filter
                && filter.prepare)
            text = filter.prepare(text);
        text = text.replace(/\s+/ig, ' ').trim();        
        data.params.jobs[0].raw_en_sentence = text;
        data = JSON.stringify(data);
        var http = new XMLHttpRequest();
        http.open("POST", "https://www.deepl.com/jsonrpc", true);
        http.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        http.setRequestHeader("Content-Type", "text/plain");
        http.onreadystatechange = function() {
            if (http.readyState == 4
                    && http.status == 200) {
                var response = JSON.parse(http.responseText);
                if (!response.result.translations
                        || !response.result.translations[0]
                        || !response.result.translations[0].beams
                        || !response.result.translations[0].beams[0]) {
                    console.log("F " + text);
                    return;
                }
                response = response.result.translations[0].beams[0].postprocessed_sentence;
                if (filter
                        && filter.convert)
                    response = filter.convert(response);
                if (filter
                        && filter.mapping)
                    response = mapping(filter.mapping, response);
                response = response.replace(/#(\d+)[?= ]/ig, function(match, index) {
                    return placeholder[index];
                });  
                response = response.replace(/\s+/g, ' ');
                response = response.trim();
                response = response.indent(indent);
                if (indent) {
                    response = "\r\n" + response;
                    while (--indent > 1)
                        response += " ";
                }
                element.innerHTML = response;
            }
        } 
        http.send(data);   
    };
   
    var queue = new Array();
    
    var elements;
    elements = document.querySelectorAll("pre, code");
    elements.forEach(function(element, index, array) {
        var text = element.innerHTML;
        text = text.replace(/Dateierweiterung/g, 'file extension');
        text = text.replace(/DATEIERWEITERUNG/g, 'FILE EXTENSION');
        text = text.replace(/METHODEN/g, 'METHODS');
        text = text.replace(/ANWENDUNG/g, 'APPLICATION');
        text = text.replace(/VARIABLE/g, 'VARIABLE');
        text = text.replace(/WERT/g, 'VALUE');
        text = text.replace(/wert/g, 'value');
        text = text.replace(/schluessel/g, 'key');
        text = text.replace(/VIRTUELLER PFAD/g, 'VIRTUAL PATH');
        text = text.replace(/ZIEL/g, 'TARGET');
        text = text.replace(/METHODE/g, 'METHOD');
        text = text.replace(/BEDINGUNG/g, 'CONDITION');
        text = text.replace(/FUNKTION/g, 'FUNCTION');
        text = text.replace(/VERWEIS/g, 'REFERENCE');
        text = text.replace(/Kommentar/g, 'comment');
        text = text.replace(/(VALUE[^\r\n]+)\s(;comment)/g, '$1$2');
        if (text.match(/[\r\n]+/))
            text = "\r\n" + text;
        element.innerHTML = text;
    });    
    
    elements = document.querySelectorAll("h1, h2, h3, h4, h5, h6, th");
    elements.forEach(function(element, index, array) {
        queue.push([element, {
            mapping: [
                [/Inhalt/g, 'Inhaltsverzeichnis'],
                [/Merkmale/g, 'Funktionen'],
                [/Schnittstellen/g, 'Interfaces'],
                [/Sicherheit/g, 'Security'],
                [/Bedeutung/g, 'Funktion'],
                [/Neustart/g, 'Restart'],
                [/Entwicklung/g, 'Development'],
                [/Befehl/g, 'Kommando'],
                [/Launch/ig, 'Start'],
                [/\(Almost\)CGI/ig, '(Fast)CGI'],
                [/\bAnd\b/ig, 'and'],
                [/\(Minimum\)/ig, '(minimum)'],
                [/Wrench/ig, 'Key'],
                [/Worth/ig, 'Value'],
                [/Dissection/ig, 'Section'],
                [/Remote Monitoring/ig, 'Remote Control'],
                [/Status Codes/ig, 'Statuscodes'],
                [/Media Type/ig, 'Mediatypes']
            ],
            convert: function(text) {
                return text.capitalize(true);
            }}]);
    });

    elements = document.querySelectorAll("p, header, li, td:last-of-type");
    elements.forEach(function(element, index, array) {
        queue.push([element, {
            mapping: [
                [/line(\s+\d+)/ig, 'Line$1'],
                [/tip/ig, 'Tip'],
                [/knits/g, 'strict']
            ]}]);    
    });

    elements = document.querySelectorAll("span, a, b, strong");
    elements.forEach(function(element, index, array) {
        queue.push([element, {
            mapping: [
                [/line(\s+\d+)/ig, 'Line$1'],
                [/tip/ig, 'Tip'],
                [/knits/g, 'strict']
            ]}]);    
    });
    
    var worker = window.setInterval(function() {
        if (queue.length) {
            console.log("Translate #" + queue.length);
            var job = queue.shift();
            translate(job[0], job[1]);
            return;
        }
        window.clearInterval(status);
        var html = document.querySelector("body");
        html = html.innerHTML;
        html = html.replace(/</g, '&lt;');
        html = html.replace(/>/g, '&gt;');
        document.open();
        document.writeln("<!DOCTYPE HTML>");
        document.writeln("<html>");
        document.writeln("<body>");
        document.writeln("<pre>" + html  + "</pre>");
        document.writeln("</body>");
        document.writeln("</html>");        
        document.close(); 
    }, 100);
});