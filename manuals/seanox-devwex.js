(function() {
    if (typeof NodeList.prototype.forEach === "function")
        return false;
    NodeList.prototype.forEach = Array.prototype.forEach;
})();

(function() {
    if (typeof Array.prototype.contains === "function")
        return false;
    Array.prototype.contains = function(value) {
        return this.indexOf(value) >= 0;
    }
})();

(function() {
    if (typeof Number.prototype.pad === "function")
        return false;
    Number.prototype.pad = function(size) {
        var text = String(this);
        while (text.length < (size || 2))
            text = "0" + text;
        return text;
    };
})();

(function() {
    if (typeof Element.prototype.cssSelector === "function")
        return false;
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
})();

(function() {
    if (typeof Element.prototype.show === "function")
        return false;
    Element.prototype.show = function() {
        this.removeClassName("hidden");
        this.style.display = this.getAttribute("x-style-display");
        this.removeAttribute("x-style-display");
    };
})();

(function() {
    if (typeof Element.prototype.isShow === "function")
        return false;
    Element.prototype.isShow = function() {
        return !this.isHide();
    };
})();

(function() {
    if (typeof Element.prototype.hide === "function")
        return false;
    Element.prototype.hide = function() {
        this.addClassName("hidden");
        if (!this.hasAttribute("x-style-display"))
            this.setAttribute("x-style-display", this.style.display);
        this.style.display = "none";
    };
})();

(function() {
    if (typeof Element.prototype.isHide === "function")
        return false;
    Element.prototype.isHide = function() {
        return this.style.display == "none";
    };
})();

(function() {
    if (typeof Element.prototype.visibility === "function")
        return false;
    Element.prototype.visibility = function(scale) {
        var result = {x:0, y:0};
        var offset = {x:0, y:0};
        var spread = {x:0, y:0};
        for (var element = this; element.parentNode && element != element.parentNode; element = element.parentNode) {
            if (element.parentNode.scrollLeft > 0)
                offset.x = element.scrollLeft;
            if (element.parentNode.scrollTop > 0)
                offset.y = element.scrollTop;
        }
        scale = Math.max(0, scale || 0);
        if (scale < 1)
            scale = 1;
        spread.x = (window.innerWidth
            || document.documentElement.clientWidth
            || document.body.clientWidth) /scale;
        if (this.offsetLeft -offset.x < 0)
            result.x = -1;
        if (this.offsetLeft -offset.x >= spread.x)
            result.x = 1;
        spread.y = (window.innerHeight
            || document.documentElement.clientHeight
            || document.body.clientHeight) /scale;
        if (this.offsetTop -offset.y < 0)
            result.y = -1;
        if (this.offsetTop -offset.y >= spread.y)
            result.y = 1;
        return result;
    };
})();

(function() {
    if (typeof Element.prototype.addClassName === "function")
        return false;
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
})();

(function() {
    if (typeof Element.prototype.removeClassName === "function")
        return false;
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
})();

(function() {
    if (typeof Element.prototype.containsClassName === "function")
        return false;
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
})();

(function() {
    if (typeof window.height === "function")
        return false;
    window.height = function() {
        return window.innerHeight
            || document.documentElement.clientHeight
            || document.body.clientHeight;
    };
})();

(function() {
    if (typeof RegExp.quote === "function")
        return false;
    RegExp.quote = function(text) {
        return String(text).replace(/[.?*+^$[\]\\(){}|-]/g, "\\$&");
    };
})();

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

window.addEventListener("load", function() {
    var elements = document.querySelectorAll("body > main > article:nth-child(n+2):not(:nth-child(3))");
    elements.forEach(function(element, index, array) {
        element.hide();
    });
});

window.addEventListener("load", function() {
    Sitemap.create();
    Sitemap.navigate(document.location.hash);
});

window.addEventListener("load", function() {
    //TODO: add event listener for navigation/toc buttons
});

var Sitemap = Sitemap || new Object();

Sitemap.SELECTOR_MAIN = "body > main";
Sitemap.SELECTOR_ARTICLE = Sitemap.SELECTOR_MAIN + " > article";
Sitemap.SELECTOR_ARTICLE_SET = Sitemap.SELECTOR_ARTICLE;
Sitemap.SELECTOR_CHAPTER;

Sitemap.SELECTOR_TOC = Sitemap.SELECTOR_ARTICLE + " nav";
Sitemap.SELECTOR_TOC_FILTER = Sitemap.SELECTOR_TOC + " input";
Sitemap.SELECTOR_TOC_ANCHOR = Sitemap.SELECTOR_TOC + " a";
Sitemap.SELECTOR_TOC_ARTICLE;

Sitemap.ATTRIBUTE_INDEX = "index";
Sitemap.ATTRIBUTE_LEVEL = "level";
Sitemap.ATTRIBUTE_NUMBER = "number";
Sitemap.ATTRIBUTE_CHAPTER = "chapter";
Sitemap.ATTRIBUTE_ALIAS = "alias";
Sitemap.ATTRIBUTE_TITLE = "title";

Sitemap.STYLE_MINOR = "minor";
Sitemap.STYLE_ERROR = "error";

Sitemap.FOCUS_INTERVAL = 250;

Sitemap.TOC_FILTER_INTERVAL = Sitemap.TOC_FOCUS_INTERVAL /2;

Sitemap.data;

Sitemap.view;

Sitemap.chapter;

Sitemap.size;

Sitemap.index;

Sitemap.meta;

Sitemap.screen;

Sitemap.toc;

Sitemap.create = function() {
    if (Sitemap.data)
        return;
    Sitemap.data = new Object();
    if (!Sitemap.size)
        Sitemap.size = 0;
    Sitemap.screen = {left:0, top:0};
    Sitemap.toc = new Object();
    Sitemap.toc.selector = function() {
        //TODO;
    };
    Sitemap.toc.hide = function() {
        //TODO;
    };
    Sitemap.toc.show = function() {
        var elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE);
        elements.forEach(function(element, index, array) {
            if (element.containsClassName("toc"))
                element.show();
            else element.hide();
        });        
    };
    Sitemap.toc.focus = function(enable) {
        if (!Sitemap.chapter)    
            return;
        var toc = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE);
        if (enable)
            toc.addClassName("focus");
        else toc.removeClassName("focus");
        var elements = document.querySelectorAll(Sitemap.SELECTOR_TOC_ANCHOR);
        elements.forEach(function(element, index, array) {
            var numbers = element.getAttribute(Sitemap.ATTRIBUTE_NUMBER).split("."); 
            if (numbers[0] == Sitemap.chapter.article
                    || !enable)
                element.show();
            else element.hide();
        });
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
    
    window.setInterval(function() {
        var element = document.querySelector(Sitemap.SELECTOR_ARTICLE_SET + ":not(.hidden)");
        //TODO: Navigate setzt das Kapitel.
        //      Neu gelesen und analysiert wird aber erst wenn gescrollt wird.
        //      Ein Problem ist das letzt Kapitel, was nicht oben stehen kann
        //      und somit nie als aktuelles Kapitel erkannt wird.
        //      Wird zum letzten Kapitel navigiert und dieser angesprungen, ist
        //      es denn noch nicht das aktuelle. Nur wenn das Kapitel bei
        //      navigate gesetzt wird und die analyse bim scroll beginnt.
        //TODO: Q: Unterscheiden ob man auf oder ab scrollt?
        //      Bsp. Ab verwendet das untere drittel, Auf das obere Drittel
        //      als Fokus?
        if (element) {
            var elements = element.querySelectorAll("h1, h2, h3, h4, h5, h6");
            elements = Array.prototype.slice.call(elements, 0);
            var chapter = elements.length ? elements[0] : null;
            while (elements.length) {
                var item = elements.shift();
                var look = item.visibility(3);
                if (look.y <= 0)
                    chapter = item;
                if (look.y >= 0)
                    break;
            }
            //TODO: Sitemap.chapter = Sitemap.lookup(chapter.getAttribute("chapter")) || Sitemap.chapter;
        }
    }, Sitemap.FOCUS_INTERVAL);
    
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
    
    window.setInterval(function() {
        if (!Sitemap
                || !Sitemap.index
                || !Sitemap.meta
                || !Sitemap.meta.timing
                || Sitemap.meta.timing >= new Date().getTime() -Sitemap.TOC_FILTER_INTERVAL
                || Sitemap.meta.filter == Sitemap.meta.current)
            return;
        Sitemap.meta.current = Sitemap.meta.filter;
        Sitemap.meta.timing = new Date().getTime();
        var search = document.querySelector(Sitemap.SELECTOR_TOC_FILTER);
        if (search)
            search.removeClassName(Sitemap.STYLE_ERROR);
        var filter = Filter.compile(Sitemap.meta.filter);
        for (var chapter in Sitemap.index) {
            var update = function(chapter, filter, wait) {
                Sitemap.meta.timing = new Date().getTime();
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
            update(chapter, filter);
        }
    }, Sitemap.TOC_FILTER_INTERVAL /2);
};

Sitemap.lookup = function(chapter) {
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

Sitemap.synchronize = function() {
    if (!Sitemap.screen)
        Sitemap.screen = {left:0, top:0};
    var article = document.querySelector(Sitemap.SELECTOR_ARTICLE + ":not(.hidden):not(.toc)");
    if (article) {
        var main = document.querySelector(Sitemap.SELECTOR_MAIN);
        Sitemap.screen.left = main.scrollLeft;
        Sitemap.screen.top = main.scrollTop;
    }    
    return Sitemap.screen;
};

Sitemap.navigate = function(chapter, synchronize) {
    if (!Sitemap.size)
        return;
    chapter = String(chapter || "");
    if (chapter.match(/^[\-\+]{2,}\d+$/)) {
        chapter = parseInt(chapter.substring(1));
        if (Sitemap.chapter)
            chapter += parseInt(Sitemap.chapter.article);
        chapter = Math.min(Sitemap.data["#" + Sitemap.size].article, chapter);
        chapter = Math.max(1, chapter);
        chapter = Sitemap.data[chapter + ".0.0.0.0.0"];
    } else if (chapter.match(/^:toc$/i)) {
        Sitemap.synchronize();
        var toc = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE + ":not(.focus)");
        if (toc && toc.isShow()) {
            Sitemap.navigate(Sitemap.chapter ? Sitemap.chapter.chapter : null, true);
            return;
        }
        Sitemap.toc.focus(false);
        Sitemap.toc.show();        
        return;
    } else if (chapter.match(/^:toc-focus/i)) {
        Sitemap.synchronize();
        var toc = document.querySelector(Sitemap.SELECTOR_TOC_ARTICLE + ".focus");
        if (toc && toc.isShow()) {
            Sitemap.navigate(Sitemap.chapter ? Sitemap.chapter.chapter : null, true);
            return;
        }
        Sitemap.toc.focus(true);
        Sitemap.toc.show();
        return;
    } else if (chapter.match(/^:first$/i)) {        
        chapter = Sitemap.data[Sitemap.chapter.article + ".0.0.0.0.0"];
    } else if (chapter.match(/^:last$/i)) {
        chapter = Sitemap.data[Sitemap.chapter.article + ".0.0.0.0.0"];
        var loop = parseInt(chapter.index.substring(1));
        while (loop <= Sitemap.size) {
            if (Sitemap.data["#" + loop].article == chapter.article)
                chapter = Sitemap.data["#" + loop];
            else break;
            loop++;
        }
    } else if (chapter.match(/^[\-\+]\d+$/)) {
        chapter = parseInt(chapter);
        if (Sitemap.chapter)
            chapter += parseInt(Sitemap.chapter.index.substring(1));
        chapter = Math.min(Sitemap.size, chapter);
        chapter = Math.max(1, chapter);
        chapter = Sitemap.data["#" + chapter];
    } else chapter = Sitemap.lookup(chapter);
    //TODO: wenn chapter null, dann standard kapitel anzeigen
    //      Q: was ist das standard kapitel?
    Sitemap.chapter = chapter;
    if (!Sitemap.chapter)
        return;
    //show the current chapter and hide the other chapters
    window.setTimeout(function() {
        var elements = document.querySelectorAll(Sitemap.SELECTOR_ARTICLE);
        elements.forEach(function(element, index, array) {
            if (!element.containsClassName("toc")
                    && element.querySelector("*[" + Sitemap.ATTRIBUTE_CHAPTER + "='" + chapter.chapter + "']"))
                element.show();
            else element.hide();
        });
        document.location.hash = chapter.alias;
        if (synchronize) {
            var main = document.querySelector(Sitemap.SELECTOR_MAIN);
            if (Sitemap.screen
                    && main) {
                main.scrollLeft = Sitemap.screen.left;
                main.scrollTop = Sitemap.screen.top;
            }
        }
    });
    //mark the curent chapter in the table of content (toc) as active
    window.setTimeout(function() {
        var elements = document.querySelectorAll(Sitemap.SELECTOR_TOC_ANCHOR);
        elements.forEach(function(element, index, array) {
            if (element.getAttribute(Sitemap.ATTRIBUTE_CHAPTER) == chapter.chapter)
                element.addClassName("active");
            else element.removeClassName("active");
        });        
    }, 0);
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