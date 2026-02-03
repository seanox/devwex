var File = java.io.File;
var FileOutputStream = java.io.FileOutputStream;
var PrintWriter = java.io.PrintWriter;
var Thread = java.lang.Thread;
var System = java.lang.System;
var Integer = java.lang.Integer;

var docroot = System.getenv("DOCUMENT_ROOT");
var file = new File(docroot + "/cgi_count.txt");
file["delete"]();
for (var count = 1; count < 1000; count++) {
    var output = new FileOutputStream(file);
    var writer = new PrintWriter(output);
    writer.print(Integer.valueOf(count));
    writer.flush();
    output.close();
    Thread.sleep(250);
}
