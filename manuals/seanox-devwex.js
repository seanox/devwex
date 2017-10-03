(function() {
    if (typeof NodeList.prototype.forEach === "function")
        return false;
    NodeList.prototype.forEach = Array.prototype.forEach;
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
    if (typeof Element.prototype.show === "function")
        return false;
    Element.prototype.show = function() {
        this.className = (" " + this.className + " ").replace(/\s+hidden\s+/ig, ' ').trim();        
    };
})();

(function() {
    if (typeof Element.prototype.hide === "function")
        return false;
    Element.prototype.hide = function() {
        this.show();
        this.className = (this.className + " hidden").trim();
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
        element.className = (element.className + " architecture-" + (index).pad(4)).trim();
    });
});

window.addEventListener("load", function() {
    var elements = document.querySelectorAll("body > main > article:nth-child(n+2):not(:nth-child(3))");
    elements.forEach(function(element, index, array) {
        element.hide();
    });
});

window.addEventListener("load", function() {
    var toc = document.querySelector("body > main > article > nav");
    var elements = document.querySelectorAll("body > main > article");
    var numbers = [0, 0, 0, 0, 0, 0, 0];
    var sitemap = new Object();
    var lookup = function(number) {
        var text = "";
        number = number.split(".");
        number.forEach(function(element, index, array) {
            if (element == 0)
                return;
            element = array.slice(0, index +1).join(".");
            while (!element.match(/^\d+(\.\d+){5}$/))
                element += ".0";
            text += "|" + sitemap[element];
        });
        text = text.replace(/Ü/g, "Ue");
        text = text.replace(/Ä/g, "Ae");
        text = text.replace(/Ö/g, "Oe");
        text = text.replace(/ü/g, "ue");
        text = text.replace(/ä/g, "ae");
        text = text.replace(/ö/g, "oe");
        text = text.replace(/ß/g, "ss");
        text = text.replace(/[^A-Z0-9]/gi, "");
        return text; 
    };
    elements.forEach(function(element, index, array) {
        if (index < 2)
            return;
        var elements = element.querySelectorAll("article:not(:nth-child(3)) h1, article h2, article h3, article h4, article h5, article h6");
        elements.forEach(function(element, index, array) {
            var level = parseInt(element.nodeName.match(/\d+$/));
            if (level >= 1
                    && level <= 6) {
                ++numbers[level];
                if (level != numbers[0])
                    for (var loop = 6; loop > level; loop--)
                        numbers[loop] = 0;
                numbers[0] = level;
                var text = element.textContent.trim();
                text = text.replace(/"/g, "&quot;");
                sitemap[numbers.slice(1).join(".")] = text;
                sitemap[lookup(numbers.slice(1).join("."))] = text;
                element.setAttribute("chapter", numbers.slice(1).join(".").replace(/(\.0)+$/g, ""));
            }
            var number = numbers.slice(1).join(".");
            var chapter = numbers.slice(1).join(".").replace(/(\.0)+$/g, "");
            var text = lookup(number);
            toc.innerHTML += "<toc" + level + " chapter=\"" + chapter + "\">"
                + "<a href=\"#" + text + "\">" + element.textContent + "</a>"
                + "</toc" + level + ">";
            element.innerHTML = "<a name=\"" + number + "\"></a>"
                              + "<a name=\"" + text + "\"></a>"
                              + element.innerHTML;
        });
    });

    var pattern = new RegExp("\\[#([a-z0-9]+)\\]", "ig");
    elements = document.querySelectorAll("body > main > article");
    elements.forEach(function(element, index, array) {
        if (!pattern.test(element.innerHTML))
            return;
        element.innerHTML = element.innerHTML.replace(pattern, function(match, word) {
            return "<a href=\"#" + word + "\">" + sitemap[word] + "</a>";
        });
    });

    elements = document.querySelectorAll("toc1 a, toc2 a, toc3 a, toc4 a, toc5 a, toc6 a");
    elements.forEach(function(element, index, array) {
        element.parentNode.replaceChild(element.cloneNode(true), element);
    });
    elements = document.querySelectorAll("toc1 a, toc2 a, toc3 a, toc4 a, toc5 a, toc6 a");
    elements.forEach(function(element, index, array) {
        element.addEventListener("click", function() {
            var elements = document.querySelectorAll("body > main > article:not(:nth-child(3))");
            elements.forEach(function(element, index, array) {
                element.hide();
            });
            var chapter = parseInt(this.parentNode.getAttribute("chapter").match(/^\d+/));
            var element = document.querySelector("body > main > article:nth-child(" + (chapter +3) + ")");
            element.show();
        });
    });
});

window.addEventListener("load", function() {
    //TODO: add event listener for nav-buttons
});

var Toc = Toc || {};

Toc.show = function(chapter) {
}

var Manual = Manual || {};

Manual.onToc = function(chapter) {
};

Manual.onChapterStart = function() {
};

Manual.onChapterEnd = function() {
};

Manual.onChapterUp = function() {
};

Manual.onChapterDown = function() {
};