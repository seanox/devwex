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

    var toc = document.querySelector("body > section > article:nth-child(3)");
    var elements = document.querySelectorAll("body > section > article");
    elements.forEach(function(element, index, array) {
        if (index < 2)
            return;
        var elements = element.querySelectorAll("h1, h2, h3, h4, h5, h6");
        elements.forEach(function(element, index, array) {
            toc.innerHTML += "<toc" + element.nodeName.match(/\d+$/) + ">"
                + element.textContent
                + "</toc" + element.nodeName.match(/\d+$/) + ">";
        });
    });
});