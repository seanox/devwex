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

window.addEventListener("load", function() {
    if (!(document.querySelector("body").toObject().meta.translate || "").match(/yes/i))
        return;    
    var translate = function(element, filter) {
        var text = element.innerHTML;
        var indent = 0;
        if (text.match(/^ *[\r\n]+( +)(?=\S)/))
            indent = text.match(/^ *[\r\n]+( +)(?=\S)/)[1].length;        
        var data = {
            "jsonrpc": "2.0",
            "method": "LMT_handle_jobs",
            "params": {
                "jobs": [{"raw_en_sentence":""}],
                "lang": {
                    "user_preferred_langs": ["EN","DE"],
                    "source_lang_user_selected": "DE",
                    "target_lang":"DE"
                }
            }        
        };
        var placeholder = new Array();
        text = text.replace(/#{[\d+RN]}/g, function(match) {
            placeholder[placeholder.length] = match;
            return "#{" + (placeholder.length -1) + "}";
        });
        text = text.replace(/<([a-z0-9\-]+)(.|\r|\n)*?<\/\1\b[^>]*>/ig, function(match) {
            placeholder[placeholder.length] = match;
            return "#{" + (placeholder.length -1) + "}";
        });
        text = text.replace(/<[^>]+?>/ig, function(match) {
            placeholder[placeholder.length] = match;
            return "#{" + (placeholder.length -1) + "}";
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
                response = response.replace(/#\{(\d+)\}/ig, function(match, index) {
                    return placeholder[index];
                });
                response = response.replace(/\s+/g, ' ');
                response = response.trim();
                if (indent) {
                    var patch = ""
                    while (patch.length < indent)
                        patch += " ";
                    var size = 80 -indent;
                    var pattern = new RegExp("^.{" + (size -1) + "}[^ ]*");
                    var result = "";
                    while (response.length > 0) {
                        var match = response.match(pattern);
                        match = !!match ? String(match) : response;
                        result += "\r\n" + patch + match;
                        response = response.substring(match.length).trim();
                    }
                    response = result + "\r\n" + patch.substring(2);
                }  
                element.innerHTML = response;
            }
        } 
        http.send(data);   
    };
    
    var elements;
    elements = document.querySelectorAll("h1, h2, h3, h4, h5, h6, th");
    elements.forEach(function(element, index, array) {
        translate(element, {
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
            }});
    });

    elements = document.querySelectorAll("p, header, li, td:last-of-type");
    elements.forEach(function(element, index, array) {
        translate(element, {
            mapping: [
                [/line(\s+\d+)/ig, 'Line$1'],
                [/tip/ig, 'Tip'],
                [/knits/g, 'strict']
            ]});
    });

    elements = document.querySelectorAll("span, a, b, strong");
    elements.forEach(function(element, index, array) {
        translate(element, {
            mapping: [
                [/line(\s+\d+)/ig, 'Line$1'],
                [/tip/ig, 'Tip'],
                [/knits/g, 'strict']
            ]});
    });
    
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
        element.innerHTML = text;
    });
});