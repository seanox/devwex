var PrintWriter = java.io.PrintWriter;
var System = java.lang.System;

var writer = new PrintWriter(System.out);
var text = "x";
while (text.length < 65535)
    text += text;
writer.print("HTTP/1.0 1234 " + text);
writer.print("\r\n\r\n");
writer.flush();
writer.print("Test... 1... 2... 3...");
writer.flush();
