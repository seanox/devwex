var File = Java.type("java.io.File");
var OutputStream = Java.type("java.io.OutputStream");
var FileOutputStream = Java.type("java.io.FileOutputStream");
var PrintWriter = Java.type("java.io.PrintWriter");
var Thread = Java.type("java.lang.Thread");
var System = Java.type("java.lang.System");
var Integer = Java.type("java.lang.Integer");

var file = new File(System.getenv("DOCUMENT_ROOT") + "/cgi_count.txt");
file.delete();
for (var count = 1; count < 1000; count++) {
    output = new FileOutputStream(file);
    writer = new PrintWriter(output);
    writer.print(Integer.valueOf(count));
    writer.flush();
    output.close();
    Thread.sleep(250);
}