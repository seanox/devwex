/**
 * Enhancement of the JavaScript API
 * Executes a provided a function once for each array element.
 * @param callback function to execute for each element
 */
if (typeof NodeList.prototype.forEach !== "function")
    NodeList.prototype.forEach = Array.prototype.forEach;

/**
 * Enhancement of the JavaScript API
 * Determines whether an array includes a certain element.
 * @param element element to search for
 * @param true or false as appropriate
 */
if (typeof Array.prototype.contains !== "function")
    Array.prototype.contains = function(element) {
        return this.indexOf(element) >= 0;
    }

/**
 * Left padding a string of a number with zeros.
 * @param size length of the result string
 */
if (typeof Number.prototype.pad !== "function")
    Number.prototype.pad = function(size) {
        var text = String(this);
        while (text.length < (size || 2))
            text = "0" + text;
        return text;
    };

/**
 * Enhancement of the JavaScript API
 * Determines the CSS selector for the element.
 * @param the determined CSS selector
 */
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

/**
 * Enhancement of the JavaScript API
 * Shows an element.
 */     
if (typeof Element.prototype.show !== "function")
    Element.prototype.show = function() {
        this.classList.remove("hidden");
    };

/**
 * Enhancement of the JavaScript API
 * Hides an element.
 */      
if (typeof Element.prototype.hide !== "function")
    Element.prototype.hide = function() {
        this.classList.add("hidden");
    };

/**
 * Enhancement of the JavaScript API
 * Checks whether if an element hidden or shown.
 * @return true, if an element shown
 */  
if (typeof Element.prototype.visible !== "function")
    Element.prototype.visible = function() {
        return !this.classList.contains("hidden");
    };

/**
 * Enhancement of the JavaScript API
 * Returns the real height of an element.
 * @return the real height of an element
 */
if (typeof window.height !== "function")
    window.height = function() {
        return window.innerHeight
            || document.documentElement.clientHeight
            || document.body.clientHeight;
    };

/**
 * Enhancement of the JavaScript API
 * Returns a literal pattern String for the specified text.
 * This method produces a String that can be used to create a Pattern that would
 * match the text as if it were a literal pattern. Metacharacters or escape
 * sequences in the input sequence will be given no special meaning.
 * @param  text to be literalized
 * @return a literal string replacement
 */
if (typeof RegExp.quote !== "function")
    RegExp.quote = function(text) {
        return String(text).replace(/[.?*+^$[\]\\(){}|-]/g, "\\$&");
    };

/** Sitemap to manage and navigate the chapters. */
var Sitemap = Sitemap || new Object();

/** CSS selectors for all chapter/article relevants elements */
Sitemap.SELECTOR_MAIN = "body > main";
Sitemap.SELECTOR_ARTICLE = Sitemap.SELECTOR_MAIN + " > article";
Sitemap.SELECTOR_ARTICLE_SET = Sitemap.SELECTOR_ARTICLE;
Sitemap.SELECTOR_CHAPTER;

/** CSS selectors for all toc relevants elements */
Sitemap.SELECTOR_TOC = Sitemap.SELECTOR_ARTICLE + " nav";
Sitemap.SELECTOR_TOC_FILTER = Sitemap.SELECTOR_ARTICLE + ".toc input";
Sitemap.SELECTOR_TOC_RESET = Sitemap.SELECTOR_ARTICLE + ".toc input + input";
Sitemap.SELECTOR_TOC_ANCHOR = Sitemap.SELECTOR_TOC + " a";
Sitemap.SELECTOR_TOC_ARTICLE;

/** CSS selectors for all navigation relevants elements */
Sitemap.SELECTOR_CONTROL = Sitemap.SELECTOR_MAIN + " ~ nav";

/** Attributes */
Sitemap.ATTRIBUTE_INDEX = "index";
Sitemap.ATTRIBUTE_LEVEL = "level";
Sitemap.ATTRIBUTE_NUMBER = "number";
Sitemap.ATTRIBUTE_CHAPTER = "chapter";
Sitemap.ATTRIBUTE_ALIAS = "alias";
Sitemap.ATTRIBUTE_TITLE = "title";

/** CSS styles classes */
Sitemap.STYLE_MINOR = "minor";
Sitemap.STYLE_ERROR = "error";
Sitemap.STYLE_FOCUS = "focus";

/** interrupt for concurrent and time-controlled logic */
Sitemap.INTERRUPT = 125;

/** chapter as meta objects */
Sitemap.data;

/** toc as HTML */
Sitemap.view;

/** current chapter */
Sitemap.chapter;

/** number of available chapters */
Sitemap.size;

/** index of chapters */
Sitemap.index;

/** meta search */
Sitemap.meta;

/** toc as meta object */
Sitemap.toc;

/** Creates the Sitemap, indexes all chapters and builds the table of content. */
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
            var toc = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE);
            if (!toc || !toc.visible())
                return;
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
                    toc.classList.add(Sitemap.STYLE_FOCUS);
                else toc.classList.remove(Sitemap.STYLE_FOCUS);
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
                if (element.classList.contains("toc"))
                    element.show();
                else element.hide();
            });
            this.main.scrollLeft = 0;
            this.main.scrollTop = 0;            
        }
    };
    
    Sitemap.Filter = {
        WILDCARD: String.fromCharCode(1),
        DELIMITER: String.fromCharCode(7),
        ESCAPE: String.fromCharCode(27),
        normalize: function(text) {
            text = (text || "").trim();
            text = text.replace(/\s+/g, ' ');
            text = text.replace(/\u00c4|\u00e4/g, 'ae');
            text = text.replace(/\u00d6|\u00f6/g, 'oe');
            text = text.replace(/\u00dc|\u00fc/g, 'ue');
            text = text.replace(/\u00df/g, 'ss');
            text = text.toLowerCase();
            return text;
        },
        compile: function(filter) {
            //simplifies all white spaces
            filter = filter.replace(/[\x00-\x20]+/g, ' ');
            //escapes all \\
            filter = filter.replace(/\\\\/g, Sitemap.Filter.ESCAPE + '5C');
            //escapes all \"
            filter = filter.replace(/\\"/g, Sitemap.Filter.ESCAPE + '22');
            //escapes all the escaped symbols
            filter = filter.replace(/\\(.)/g, function(match, symbol) {
                symbol = String(symbol).charCodeAt(0).toString(16);
                while (symbol.length < 2)
                    symbol = "0" + symbol;
                return Sitemap.Filter.ESCAPE + symbol;
            });
            //escapes all special symbols in a phrase
            filter = filter.replace(/"([^\"]*)"/g, function(match, phrase) {
                phrase = phrase.replace(/\\(.)/g, function(match, symbol) {
                    symbol = String(symbol).charCodeAt(0).toString(16);
                    while (symbol.length < 2)
                        symbol = "0" + symbol;
                    return Sitemap.Filter.ESCAPE + symbol;
                });
                phrase = phrase.replace(/([\(\)\-\!\+\&\|\s|\*])/g, function(match, symbol) {
                    symbol = String(symbol).charCodeAt(0).toString(16);
                    while (symbol.length < 2)
                        symbol = "0" + symbol;
                    return Sitemap.Filter.ESCAPE + symbol;
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
        },
        validate: function(text, filter) {
            if (!filter
                    || !filter.trim())
                return true;
            //mark all special symbols
            filter = filter.replace(/([\(\+\-\|\)])/g, Sitemap.Filter.DELIMITER + "$1" + Sitemap.Filter.DELIMITER);
            filter = filter.split(Sitemap.Filter.DELIMITER);
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
                element = element.replace(new RegExp(Sitemap.Filter.ESCAPE + "(\\d{2})", "g"), function(match, symbol) {
                    return String.fromCharCode(parseInt(symbol, 16));
                });
                var regexp = RegExp.quote(element);
                //unescape wildcard
                regexp = regexp.replace(new RegExp(Sitemap.Filter.WILDCARD, "g"), '.*');
                regexp = new RegExp("\\b" + regexp + "\\b", "i");
                array[index] = regexp.test(text); 
            });  
            return eval(filter.join(" ").trim());
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
                chapter.alias = chapter.alias.replace(/\u00dc/g, "Ue");
                chapter.alias = chapter.alias.replace(/\u00c4/g, "Ae");
                chapter.alias = chapter.alias.replace(/\u00d6/g, "Oe");
                chapter.alias = chapter.alias.replace(/\u00fc/g, "ue");
                chapter.alias = chapter.alias.replace(/\u00e4/g, "ae");
                chapter.alias = chapter.alias.replace(/\u00f6/g, "oe");
                chapter.alias = chapter.alias.replace(/\u00df/g, "ss");
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
            element.classList.add("toc");
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
            return "<a href=\"#" + word + "\">" + Sitemap.data[word].title + "</a>";
        });
    });    
    
    Sitemap.index = new Object();
    elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE_SET);
    elements.forEach(function(element, index, array) {
        var content = element.innerHTML;
        content = Sitemap.Filter.normalize(content);
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
            search.classList.remove(Sitemap.STYLE_ERROR);
            var filter = Sitemap.Filter.compile(Sitemap.meta.search);
            var update = function(chapter, filter) {
                var element = document.querySelector(Sitemap.SELECTOR_TOC_ANCHOR + "[" + Sitemap.ATTRIBUTE_CHAPTER + "='" + chapter + "']");
                element.classList.remove(Sitemap.STYLE_MINOR);
                try {
                    if (!Sitemap.Filter.validate(Sitemap.index[chapter], filter))
                        element.classList.add(Sitemap.STYLE_MINOR);
                } catch (exception) {
                    if (search)
                        search.classList.add(Sitemap.STYLE_ERROR);
                }
            };
            for (var chapter in Sitemap.index)
                window.setTimeout(update, 0, chapter, filter);
        }, 250);
    }; 
    var filter = document.querySelector(Sitemap.SELECTOR_TOC_FILTER);
    filter.addEventListener("keyup", function() {
        search();
    });
    filter.addEventListener("mouseup", function() {
        search();
    });

    var reset = document.querySelector(Sitemap.SELECTOR_TOC_RESET);
    reset.addEventListener("click", function() {
        document.querySelector(Sitemap.SELECTOR_TOC_FILTER).value = "";
        document.querySelector(Sitemap.SELECTOR_TOC_FILTER).focus();
        search();
    });

    var control;
    control = document.querySelector(Sitemap.SELECTOR_CONTROL + " button:nth-child(1)");
    control.addEventListener("click", function() {
        Sitemap.navigate(":toc");
    });
    control = document.querySelector(Sitemap.SELECTOR_CONTROL + " button:nth-child(2)");
    control.addEventListener("click", function() {
        Sitemap.navigate(":toc-focus");
    });
    control = document.querySelector(Sitemap.SELECTOR_CONTROL + " button:nth-child(3)");
    control.addEventListener("click", function() {
        Sitemap.toc.hide();
    });
    control = document.querySelector(Sitemap.SELECTOR_CONTROL + " button:nth-child(4)");
    control.addEventListener("click", function(event) {
        if (!Sitemap.chapter
                || !Sitemap.chapter.article
                || Sitemap.chapter.article <= 1)
            return;
        Sitemap.navigate(Sitemap.lookup(Sitemap.chapter.article -1).chapter);
    });
    control = document.querySelector(Sitemap.SELECTOR_CONTROL + " button:nth-child(5)");
    control.addEventListener("click", function() {
        if (!Sitemap.chapter
                || !Sitemap.chapter.article
                || !Sitemap.lookup("::last")
                || Sitemap.chapter.article >= Sitemap.lookup("::last").article)
            return;
        Sitemap.navigate(Sitemap.lookup(Sitemap.chapter.article +1).chapter);
    });
    
    Sitemap.navigate(document.location.hash); 
    
    var control = 0;
    window.setInterval(function() {
        var element = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE);
        if (!element.classList.contains("hidden"))
            if (element.classList.contains("focus"))
                element = 2;
            else element = 1;
        else element = 3;
        if (control == element)
            return;
        control = element;
        var elements = document.querySelectorAll(Sitemap.SELECTOR_CONTROL + " button");
        elements.forEach(function(element, index, array) {
            element.classList.remove("active");
        });
        document.querySelector(Sitemap.SELECTOR_CONTROL + " button:nth-child(" + element + ")").classList.add("active");        
    }, 100);
};

/**
 * Determines the metadata for a chapter.
 *     As chapters are supported:
 * chapter numbers, aliases and the directives :first, :last, ::first, ::last
 * @param  chapter
 * @return determined metadata for a chapter, otherwise null
 */
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
    } else if (chapter.match(/^::first$/i)) {        
        return Sitemap.lookup(Object.keys(Sitemap.data)[0]);
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
    } else if (chapter.match(/^::last$/i)) {
        return Sitemap.lookup(Object.keys(Sitemap.data)[Object.keys(Sitemap.data).length -1]);
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

/**
 * Navigates to a chapter.
 * As chapters are supported: chapter numbers, aliases and the directives :toc,
 * :toc-focus, :first and :last
 * @param chapter
 */
Sitemap.navigate = function(chapter) {
    
    if (!Sitemap.size)
        return;
    
    chapter = String(chapter || "");
    if (chapter.match(/^:toc(-focus)*/i)) {
        var focus = chapter.match(/^:toc-focus/i) && Sitemap.chapter;
        var toc = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE + (focus ? ".focus" : ":not(.focus)"));
        if (toc && toc.visible())
            Sitemap.toc.hide();
        else Sitemap.toc.show(focus);
        return;
    }
    
    Sitemap.chapter = Sitemap.lookup(chapter);
    if (!Sitemap.chapter)
        return;
    
    //show the current chapter and hide the other chapters
    window.setTimeout(function() {
        var elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE);
        elements.forEach(function(element, index, array) {
            if (!element.classList.contains("toc")
                    && element.querySelector("*[" + Sitemap.ATTRIBUTE_CHAPTER + "='" + Sitemap.chapter.chapter + "']"))
                element.show();
            else element.hide();
        });
        
        document.location.hash = "";
        window.setTimeout(function(hash) {
            document.location.hash = hash;
        }, 0, Sitemap.chapter.alias);
    });
    
    //mark the current chapter in the table of content (toc) as active
    window.setTimeout(function() {
        var elements = document.querySelectorAll(Sitemap.SELECTOR_TOC_ANCHOR);
        elements.forEach(function(element, index, array) {
            if (element.getAttribute(Sitemap.ATTRIBUTE_CHAPTER) == chapter.chapter)
                element.classList.add("active");
            else element.classList.remove("active");
        });        
    });
};

/** 
 * Filters the table of contents with a search expression.
 * The expression supports: AND (+ or |), NOT (- or !), OR (|), round brackets,
 * wildcard characters (*), phrases ("..."), escape sequences / escape symbols
 * (\). In phrases, all special characters are automatically used as escape
 * sequences. 
 * @param filter search expression
 */
Sitemap.filter = function(filter) {
    if (!Sitemap.meta)
        Sitemap.meta = {filter:null, current:null, timing:0};
    Sitemap.meta.timing = new Date().getTime();
    Sitemap.meta.filter = Sitemap.Filter.normalize(filter);
};

/**
 * Event when loading the page
 * Transforms PRE tags into output with row numbers.
 */    
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
        content = "<ol class=\"code\" start=\"1\">" + content + "</ol>";
        element.outerHTML = "<article>" + content + "</article>";
    });
});

/**
 * Event when loading the page
 * Transforms the table of architecture.
 */    
window.addEventListener("load", function() {
    var elements = document.querySelector("table.architecture");
    elements = elements.getElementsByTagName("*");
    elements = Array.prototype.slice.call(elements, 0);
    elements.forEach(function(element, index, array) {
        element.classList.add("architecture-" + (index).pad(4));
    });
});

/**
 * Event when loading the page
 * Activates the articles for the initial display.
 */    
window.addEventListener("load", function() {
    Sitemap.create();
    var elements = document.querySelectorAll("body > main > article");
    elements.forEach(function(element, index, array) {
        if (index == 0)
            element.show();
        else element.hide();
    });
    document.querySelector("body > main").classList.add("active");
});

/**
 * Event when loading the page
 * Adds the base UserAgent as style class to the body element.
 */    
window.addEventListener("load", function() {
    var client = (navigator.userAgent || "").match(/\b(?:chrome|firefox|edge)\b/i);
    if (client)
        document.body.classList.add(String(client).toLowerCase());
});

/**
 * Event when loading the page
 * Clears the filter when loading the page.
 */    
window.addEventListener("load", function() {
    document.querySelector(Sitemap.SELECTOR_TOC_RESET).click();
});