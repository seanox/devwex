if (typeof NodeList.prototype.forEach !== "function")
    NodeList.prototype.forEach = Array.prototype.forEach;

if (typeof Array.prototype.contains !== "function")
    Array.prototype.contains = function(value) {
        return this.indexOf(value) >= 0;
    }

if (typeof Number.prototype.pad !== "function")
    Number.prototype.pad = function(size) {
        var text = String(this);
        while (text.length < (size || 2))
            text = "0" + text;
        return text;
    };

if (typeof Element.prototype.cssSelector !== "function")
    Element.prototype.cssSelector = function() {
        var element = this;
        var names = new Array();
        while (element.parentNode) {
            if (element.id) {
              names.unshift("#" + element.id);
              break;
            }
            if (element != element.ownerDocument.documentElement) {
                var index = 1;
                var sibling = element;
                while (sibling.previousElementSibling) {
                    sibling = sibling.previousElementSibling;
                    index++;
                }
                names.unshift(element.tagName + ":nth-child(" + index + ")");
            } else names.unshift(element.tagName);
            element = element.parentNode;
        }
        return names.join(" > ");
    };

if (typeof Element.prototype.show !== "function")
    Element.prototype.show = function() {
        this.removeClassName("hidden");
        this.style.display = this.getAttribute("x-style-display");
        this.removeAttribute("x-style-display");
    };

if (typeof Element.prototype.hide !== "function")
    Element.prototype.hide = function() {
        this.addClassName("hidden");
        if (!this.hasAttribute("x-style-display"))
            this.setAttribute("x-style-display", this.style.display);
        this.style.display = "none";
    };

if (typeof Element.prototype.visible !== "function")
    Element.prototype.visible = function() {
        return this.style.display != "none";
    };

if (typeof Element.prototype.addClassName !== "function")
    Element.prototype.addClassName = function(className) {
        className = (className || "").trim();
        if (!className)
            return;
        this.removeClassName(className);
        className = className.split(/\s+/);
        if (!Array.isArray(className))
            className = new Array(className);
        var element = this;
        className.forEach(function(className, index, array) {
            element.className = (element.className + " " + className).trim(); 
        });
    };

if (typeof Element.prototype.removeClassName !== "function")
    Element.prototype.removeClassName = function(className) {
        className = (className || "").trim();
        if (!className)
            return;
        className = className.split(/\s+/);
        if (!Array.isArray(className))
            className = new Array(className);
        var element = this;
        className.forEach(function(className, index, array) {
            var regexp = new RegExp("\\s+" + className + "\\s+", "ig");
            element.className = (" " + element.className + " ").replace(regexp, ' ').trim(); 
            if (!element.className)
                element.removeAttribute("class");
        });
    };

if (typeof Element.prototype.containsClassName !== "function")
    Element.prototype.containsClassName = function(className) {
        className = (className || "").trim();
        if (!className)
            return false;
        var classNameA = className.toLowerCase().split(/\s+/);
        if (!Array.isArray(classNameA))
            classNameA = new Array(classNameA);
        var classNameB = (this.className || "").trim().toLowerCase().split(/\s+/);
        if (!Array.isArray(classNameB))
            classNameB = new Array(classNameB);
        classNameA.forEach(function(className, index, array) {
            if (className
                    && classNameB.contains(className))
                array[index] = "";
        });
        return !classNameA.join("").trim();
    };

if (typeof window.height !== "function")
    window.height = function() {
        return window.innerHeight
            || document.documentElement.clientHeight
            || document.body.clientHeight;
    };

if (typeof RegExp.quote !== "function")
    RegExp.quote = function(text) {
        return String(text).replace(/[.?*+^$[\]\\(){}|-]/g, "\\$&");
    };

window.addEventListener("load", function() {
    var elements = document.querySelectorAll("pre");
    elements.forEach(function(element, index, array) {
        if (!element.innerHTML.match(/([\r\n]\s*[^\r\n]+){2,}/m))
            return;
        var indent = element.innerHTML.match(/^\s+/gm)[0];
        indent = indent.match(/[^\r\n]+$/)[0];
        var content = element.innerHTML.replace(new RegExp("([\r\n])" + indent, "gm"), '$1').trim();
        content = ("\r\n" + content).replace(/((?:\r\n)|(?:\n\r)|[\r\n])([^\r\n]*)/gm, "$1<li><code>$2</code></li>");
        content = content.replace(/\s+(<\/code>)/gm, "$1");
        content = "<ol class=\"code\" start=\"1\">" + content + "</ol>"
        element.outerHTML = "<article>" + content + "</article>";
    });
});

window.addEventListener("load", function() {
    var elements = document.querySelector("table.architecture");
    elements = elements.getElementsByTagName("*");
    elements = Array.prototype.slice.call(elements, 0);
    elements.forEach(function(element, index, array) {
        element.addClassName("architecture-" + (index).pad(4));
    });
});

//TODO: change: all chapters are hidden, default view = toc or first chapter
window.addEventListener("load", function() {
    var elements = document.querySelectorAll("body > main > article:nth-child(n+2):not(:nth-child(3))");
    elements.forEach(function(element, index, array) {
        element.hide();
    });
});

window.addEventListener("load", function() {
    Sitemap.create();
});

var Sitemap = Sitemap || new Object();

Sitemap.SELECTOR_MAIN = "body > main";
Sitemap.SELECTOR_ARTICLE = Sitemap.SELECTOR_MAIN + " > article";
Sitemap.SELECTOR_ARTICLE_SET = Sitemap.SELECTOR_ARTICLE;
Sitemap.SELECTOR_CHAPTER;

Sitemap.SELECTOR_TOC = Sitemap.SELECTOR_ARTICLE + " nav";
Sitemap.SELECTOR_TOC_FILTER = Sitemap.SELECTOR_ARTICLE + ".toc > input";
Sitemap.SELECTOR_TOC_ANCHOR = Sitemap.SELECTOR_TOC + " a";
Sitemap.SELECTOR_TOC_ARTICLE;

Sitemap.SELECTOR_CONTROL = Sitemap.SELECTOR_MAIN + " ~ button";

Sitemap.ATTRIBUTE_INDEX = "index";
Sitemap.ATTRIBUTE_LEVEL = "level";
Sitemap.ATTRIBUTE_NUMBER = "number";
Sitemap.ATTRIBUTE_CHAPTER = "chapter";
Sitemap.ATTRIBUTE_ALIAS = "alias";
Sitemap.ATTRIBUTE_TITLE = "title";
Sitemap.ATTRIBUTE_TIMING = "timing";

Sitemap.STYLE_MINOR = "minor";
Sitemap.STYLE_ERROR = "error";
Sitemap.STYLE_FOCUS = "focus";

Sitemap.INTERRUPT = 125;

Sitemap.data;

Sitemap.view;

Sitemap.chapter;

Sitemap.size;

Sitemap.index;

Sitemap.meta;

Sitemap.toc;

Sitemap.create = function() {
    
    if (Sitemap.data)
        return;
    Sitemap.data = new Object();
    
    if (!Sitemap.size)
        Sitemap.size = 0;
    
    Sitemap.toc = {
        article: null,
        main: document.querySelector(Sitemap.SELECTOR_MAIN),
        screen: {
            left: 0, top: 0, article: null
        },
        hide: function() {
            var elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE);
            elements.forEach(function(element, index, array) {
                element.hide();
            });
            if (this.article) {
                this.article.show();
                this.main.scrollLeft = this.screen.left;
                this.main.scrollTop = this.screen.top;
            }
        },
        show: function(focus) {
            if (Sitemap.chapter) {
                var toc = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE);
                if (focus)
                    toc.addClassName(Sitemap.STYLE_FOCUS);
                else toc.removeClassName(Sitemap.STYLE_FOCUS);
                var elements = document.querySelectorAll(Sitemap.SELECTOR_TOC_ANCHOR);
                elements.forEach(function(element, index, array) {
                    var numbers = element.getAttribute(Sitemap.ATTRIBUTE_NUMBER).split("."); 
                    if (numbers[0] == Sitemap.chapter.article
                            || !focus)
                        element.show();
                    else element.hide();
                });
            }
            var article = document.querySelector(Sitemap.SELECTOR_ARTICLE + ":not(.hidden):not(.toc)");
            if (article) {
                this.article = article;
                this.screen.left = this.main.scrollLeft;
                this.screen.top = this.main.scrollTop;
            }
            var elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE);
            elements.forEach(function(element, index, array) {
                if (element.containsClassName("toc"))
                    element.show();
                else element.hide();
            });
            this.main.scrollLeft = 0;
            this.main.scrollTop = 0;            
        }
    };
    
    var elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE);
    elements.forEach(function(element, index, array) {
        if (Sitemap.SELECTOR_CHAPTER
                || !element.querySelector("nav"))
            return;
        Sitemap.SELECTOR_ARTICLE_SET += ":not(:nth-child(-n+" + (index +1) + "))";
        for (var loop = 1; loop < 6; loop++) {
            if (!Sitemap.SELECTOR_CHAPTER)
                Sitemap.SELECTOR_CHAPTER = "";
            else Sitemap.SELECTOR_CHAPTER += ", ";
            Sitemap.SELECTOR_CHAPTER += Sitemap.SELECTOR_ARTICLE_SET + " h" + loop;
        }
        var numbers = [0, 0, 0, 0, 0, 0, 0];
        var elements = document.querySelectorAll(Sitemap.SELECTOR_CHAPTER);
        elements.forEach(function(element, index, array) {
            var level = parseInt(element.nodeName.match(/\d+$/));
            if (level >= 1
                    && level <= 6) {
                ++Sitemap.size;
                ++numbers[level];
                if (level != numbers[0])
                    for (var loop = 6; loop > level; loop--)
                        numbers[loop] = 0;
                numbers[0] = level;
                var chapter = {index:"#" + Sitemap.size, article:numbers[1],
                        number:numbers.slice(1).join("."), chapter:numbers.slice(1).join(".").replace(/(\.0)+$/, ''),
                        alias:"", title:element.textContent.trim()};
                var parent = Sitemap.lookup(chapter.chapter.replace(/(^\d+$)|(\.\d+$)/, ''));
                parent = parent ? parent.alias : "";
                chapter.alias = chapter.title;
                chapter.alias = chapter.alias.replace(/Ü/g, "Ue");
                chapter.alias = chapter.alias.replace(/Ä/g, "Ae");
                chapter.alias = chapter.alias.replace(/Ö/g, "Oe");
                chapter.alias = chapter.alias.replace(/ü/g, "ue");
                chapter.alias = chapter.alias.replace(/ä/g, "ae");
                chapter.alias = chapter.alias.replace(/ö/g, "oe");
                chapter.alias = chapter.alias.replace(/ß/g, "ss");
                chapter.alias = chapter.alias.replace(/[^A-Z0-9]/gi, "");
                if (!chapter.alias)
                    chapter.alias = "[" + numbers[level] + "]";
                chapter.alias = parent + chapter.alias;
                Sitemap.data[chapter.index] = chapter;
                Sitemap.data[chapter.number] = chapter;
                Sitemap.data[chapter.alias] = chapter;
                element.setAttribute(Sitemap.ATTRIBUTE_CHAPTER, chapter.chapter);
                element.innerHTML = "<a name=\"" + chapter.chapter + "\"></a>" + element.innerHTML;
                element.innerHTML = "<a name=\"" + chapter.alias + "\"></a>" + element.innerHTML;
            }
            if (!Sitemap.view)
                Sitemap.view = "";
            else Sitemap.view += "\r\n";
            var chapter = Sitemap.lookup(numbers.slice(1).join("."));
            Sitemap.view += "<a href=\"#" + chapter.alias + "\""  
                + " " + Sitemap.ATTRIBUTE_LEVEL + "=\"" + level + "\""
                + " " + Sitemap.ATTRIBUTE_NUMBER + "=\"" + chapter.number + "\""
                + " " + Sitemap.ATTRIBUTE_CHAPTER + "=\"" + chapter.chapter + "\""
                + " " + Sitemap.ATTRIBUTE_ALIAS + "=\"" + chapter.alias + "\">" + chapter.title + "</a>";            
        });
    });

    var element = document.querySelector(Sitemap.SELECTOR_TOC);
    while (element.parentNode
            && !Sitemap.SELECTOR_TOC_ARTICLE) {
        if (element.tagName == "ARTICLE") {
            Sitemap.SELECTOR_TOC_ARTICLE = element.cssSelector();
            element.addClassName("toc");
        }
        element = element.parentNode;
    }
    element = document.querySelector(Sitemap.SELECTOR_TOC);
    element.innerHTML += Sitemap.view;
    elements = document.querySelectorAll(Sitemap.SELECTOR_TOC_ANCHOR);
    elements.forEach(function(element, index, array) {
        element.parentNode.replaceChild(element.cloneNode(true), element);
    });
    elements = document.querySelectorAll(Sitemap.SELECTOR_TOC_ANCHOR);
    elements.forEach(function(element, index, array) {
        element.addEventListener("click", function() {
            Sitemap.navigate(this.getAttribute(Sitemap.ATTRIBUTE_ALIAS));
            return false;
        }, false);
    });
    
    var pattern = new RegExp("\\[#([a-z0-9]+)\\]", "ig");
    elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE_SET);
    elements.forEach(function(element, index, array) {
        if (!pattern.test(element.innerHTML))
            return;
        element.innerHTML = element.innerHTML.replace(pattern, function(match, word) {
            return "<a href=\"#" + word + "\">" + Sitemap.data[word] + "</a>";
        });
    });    
    
    Sitemap.index = new Object();
    elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE_SET);
    elements.forEach(function(element, index, array) {
        var content = element.innerHTML;
        content = Filter.normalize(content);
        content = content.replace(new RegExp("<h[1-6][^>]+\\b" + Sitemap.ATTRIBUTE_CHAPTER + "=\"\\s*([\\d\\.]+)\\s*\"[^>]*>", "ig"), '\r\n$1 ');
        content = content.replace(/<\/*[^>]+>/g, ' ');
        content = content.replace(/ +/g, ' ').trim();
        content.split(/[\r\n]+/).forEach(function(line, index, array) {
            var match = line.match(/^([\d\.]+)\s*(.*)$/);
            Sitemap.index[match[1]] = match[2];
        });        
    });   
    
    var search = function() {
        if (!Sitemap
                || !Sitemap.index)
            return;
        var filter = document.querySelector(Sitemap.SELECTOR_TOC_FILTER);
        Sitemap.filter(filter.value);
        window.setTimeout(function() {
            var timing = new Date().getTime();
            if (timing -Sitemap.meta.timing < 250
                    || Sitemap.meta.search == Sitemap.meta.filter)
                return;
            Sitemap.meta.search = Sitemap.meta.filter;
            var search = document.querySelector(Sitemap.SELECTOR_TOC_FILTER);
            search.removeClassName(Sitemap.STYLE_ERROR);
            var filter = Filter.compile(Sitemap.meta.search);
            var update = function(chapter, filter, wait) {
                if (!wait) {
                    window.setTimeout(function() {
                        update(chapter, filter, true);  
                    });
                    return;
                }
                var element = document.querySelector(Sitemap.SELECTOR_TOC_ANCHOR + "[" + Sitemap.ATTRIBUTE_CHAPTER + "='" + chapter + "']");
                element.removeClassName(Sitemap.STYLE_MINOR);
                try {
                    if (!Filter.validate(Sitemap.index[chapter], filter))
                        element.addClassName(Sitemap.STYLE_MINOR);
                } catch (exception) {
                    if (search)
                        search.addClassName(Sitemap.STYLE_ERROR);
                }
            };
            for (var chapter in Sitemap.index)
                update(chapter, filter);
        }, 250);
    }; 
    var filter = document.querySelector(Sitemap.SELECTOR_TOC_FILTER);
    filter.addEventListener("keyup", function() {
        search();
    });
    filter.addEventListener("mouseup", function() {
        search();
    });
    
    var control = document.querySelector(Sitemap.SELECTOR_CONTROL);
    control.addEventListener("click", function() {
        var time = new Date().getTime();
        this.setAttribute(Sitemap.ATTRIBUTE_TIMING + "-A", this.getAttribute(Sitemap.ATTRIBUTE_TIMING + "-B") || time -1000);
        this.setAttribute(Sitemap.ATTRIBUTE_TIMING + "-B", time);
        var time = parseInt(this.getAttribute(Sitemap.ATTRIBUTE_TIMING + "-B"))
            - parseInt(this.getAttribute(Sitemap.ATTRIBUTE_TIMING + "-A"));
        if (time < 250)
            return;
        window.setTimeout(function() {
            var control = document.querySelector(Sitemap.SELECTOR_CONTROL);
            var content = ":toc";
            if ((parseInt(control.getAttribute(Sitemap.ATTRIBUTE_TIMING + "-B"))
                    - parseInt(control.getAttribute(Sitemap.ATTRIBUTE_TIMING + "-A"))) < 250)
                content += "-focus";
            Sitemap.navigate(content);
        }, 250);
    });
    Sitemap.navigate(document.location.hash);    
};

Sitemap.lookup = function(chapter) {
    if (!Sitemap.size)
        return null;
    chapter = String(chapter || "").trim();
    if (chapter.match(/^[\-\+]{2,}\d+$/)) {
        chapter = parseInt(chapter.substring(1));
        if (Sitemap.chapter)
            chapter += parseInt(Sitemap.chapter.article);
        chapter = Math.min(Sitemap.data["#" + Sitemap.size].article, chapter);
        chapter = Math.max(1, chapter);
        chapter = Sitemap.data[chapter + ".0.0.0.0.0"];
        return chapter;
    } else if (chapter.match(/^[\-\+]\d+$/)) {
        chapter = parseInt(chapter);
        if (Sitemap.chapter)
            chapter += parseInt(Sitemap.chapter.index.substring(1));
        chapter = Math.min(Sitemap.size, chapter);
        chapter = Math.max(1, chapter);
        chapter = Sitemap.data["#" + chapter];
        return chapter;
    } else if (chapter.match(/^:first$/i)) {        
        return Sitemap.data[Sitemap.chapter.article + ".0.0.0.0.0"];
    } else if (chapter.match(/^:last$/i)) {
        chapter = Sitemap.data[Sitemap.chapter.article + ".0.0.0.0.0"];
        var loop = parseInt(chapter.index.substring(1));
        while (loop <= Sitemap.size) {
            if (Sitemap.data["#" + loop].article != chapter.article)
                break;
            chapter = Sitemap.data["#" + loop];
            loop++;
        }
        return chapter;
    }
    if (chapter)
        chapter = chapter.replace(/^#/, '') .trim();
    if (!chapter)
        return null;
    if (!chapter.match(/^\d+(\.\d+)*$/)) {
        return Sitemap.data[chapter]
    }    
    if (!chapter.match(/^\d+(\.\d+){5,}$/)) {
        chapter = chapter.replace(/(\.0)+$/, '');
        while (!chapter.match(/^\d+(\.\d+){5,}$/))
            chapter += ".0";
    }
    return Sitemap.data[chapter];
};

Sitemap.navigate = function(chapter) {
    
    if (!Sitemap.size)
        return;
    
    chapter = String(chapter || "");
    if (chapter.match(/^:toc(-focus)*/i)) {
        var focus = chapter.match(/^:toc-focus/i);
        var toc = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE + (focus ? ".focus" : ":not(.focus)"));
        if (toc && toc.visible())
            Sitemap.toc.hide();
        else Sitemap.toc.show(focus);
        return;
    }
    
    //TODO: wenn chapter null, dann standard kapitel anzeigen
    //      Q: was ist das standard kapitel?
    Sitemap.chapter = Sitemap.lookup(chapter);
    if (!Sitemap.chapter)
        return;
    
    //show the current chapter and hide the other chapters
    window.setTimeout(function() {
        var elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE);
        elements.forEach(function(element, index, array) {
            if (!element.containsClassName("toc")
                    && element.querySelector("*[" + Sitemap.ATTRIBUTE_CHAPTER + "='" + Sitemap.chapter.chapter + "']"))
                element.show();
            else element.hide();
        });
        document.location.hash = Sitemap.chapter.alias;
    });
    
    //mark the current chapter in the table of content (toc) as active
    window.setTimeout(function() {
        var elements = document.querySelectorAll(Sitemap.SELECTOR_TOC_ANCHOR);
        elements.forEach(function(element, index, array) {
            if (element.getAttribute(Sitemap.ATTRIBUTE_CHAPTER) == chapter.chapter)
                element.addClassName("active");
            else element.removeClassName("active");
        });        
    });
};

Sitemap.filter = function(filter) {
    if (!Sitemap.meta)
        Sitemap.meta = {filter:null, current:null, timing:0};
    Sitemap.meta.timing = new Date().getTime();
    Sitemap.meta.filter = Filter.normalize(filter);
};

var Filter = Filter || new Object();

Filter.WILDCARD = String.fromCharCode(1);
Filter.DELIMITER = String.fromCharCode(7);
Filter.ESCAPE = String.fromCharCode(27);

Filter.normalize = function(text) {
    text = (text || "").trim();
    text = text.replace(/\s+/g, ' ');
    text = text.replace(/\u00c4|\u00e4/g, 'ae');
    text = text.replace(/\u00d6|\u00f6/g, 'oe');
    text = text.replace(/\u00dc|\u00fc/g, 'ue');
    text = text.replace(/\u00df/g, 'ss');
    text = text.toLowerCase();
    return text;
};

Filter.compile = function(filter) {
    //simplifies all white spaces
    filter = filter.replace(/[\x00-\x20]+/g, ' ');
    //escapes all \\
    filter = filter.replace(/\\\\/g, Filter.ESCAPE + '5C');
    //escapes all \"
    filter = filter.replace(/\\"/g, Filter.ESCAPE + '22');
    //escapes all the escaped symbols
    filter = filter.replace(/\\(.)/g, function(match, symbol) {
        symbol = String(symbol).charCodeAt(0).toString(16);
        while (symbol.length < 2)
            symbol = "0" + symbol;
        return Filter.ESCAPE + symbol;
    });
    //escapes all special symbols in a phrase
    filter = filter.replace(/"([^\"]*)"/g, function(match, phrase) {
        phrase = phrase.replace(/\\(.)/g, function(match, symbol) {
            symbol = String(symbol).charCodeAt(0).toString(16);
            while (symbol.length < 2)
                symbol = "0" + symbol;
            return Filter.ESCAPE + symbol;
        });
        phrase = phrase.replace(/([\(\)\-\!\+\&\|\s|\*])/g, function(match, symbol) {
            symbol = String(symbol).charCodeAt(0).toString(16);
            while (symbol.length < 2)
                symbol = "0" + symbol;
            return Filter.ESCAPE + symbol;
        });
        return phrase; 
    });
    //escapes all *
    filter = filter.replace(/\*/g, '\u0001');
    //normalizes all special symbols
    filter = filter.replace(/[\+\&]/g, '+');
    filter = filter.replace(/[\-\!]/g, '-');
    //removes all invalid auto AND combinations
    filter = filter.replace(/([\(\+\-\|]) /g, '$1');
    filter = filter.replace(/ ([\+\|\)])/g, '$1');
    //set auto AND for all white spaces
    filter = filter.replace(/\s/g, '+');
    return filter;
};

Filter.validate = function(text, filter) {
    if (!filter
            || !filter.trim())
        return true;
    //mark all special symbols
    filter = filter.replace(/([\(\+\-\|\)])/g, Filter.DELIMITER + "$1" + Filter.DELIMITER);
    filter = filter.split(Filter.DELIMITER);
    filter.forEach(function(element, index, array) {
        if (!element)
            return;
        if (element.match(/\+/))
            array[index] = "&&";
        if (element.match(/\-/))
            array[index] = "!";
        if (element.match(/\|/))
            array[index] = "||";
        if (element.match(/[\(\+\-\|\)]/))
            return;
        element = element.replace(new RegExp(Filter.ESCAPE + "(\\d{2})", "g"), function(match, symbol) {
            return String.fromCharCode(parseInt(symbol, 16));
        });
        var regexp = RegExp.quote(element);
        //unescape wildcard
        regexp = regexp.replace(new RegExp(Filter.WILDCARD, "g"), '.*');
        regexp = new RegExp("\\b" + regexp + "\\b", "i");
        array[index] = regexp.test(text); 
    });  
    return eval(filter.join(" ").trim());
};