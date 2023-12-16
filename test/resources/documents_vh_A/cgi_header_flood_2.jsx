var PrintWriter = Java.type("java.io.PrintWriter");
var System = Java.type("java.lang.System");

var writer = new PrintWriter(System.out);
var text = " ";
while (text.length < 65535)
    text += text;
writer.print("HTTP/1.0 1234 XXX\r\n");
writer.print("Test: A" + text);
writer.print("\r\n\r\n");
writer.flush();
writer.print("Test... 1... 2... 3...");
writer.flush();
