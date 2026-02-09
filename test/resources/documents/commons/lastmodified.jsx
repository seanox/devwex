var System = java.lang.System;
var File = java.io.File;
var SimpleDateFormat = java.text.SimpleDateFormat;
var Locale = java.util.Locale;

System.out.print("HTTP/1.0 200\r\n\r\n");
var file = new java.lang.String(System.getenv().get("HTTP_FILE") || "");
file = new File(file).getCanonicalFile();
if (file.exists()) {
    var format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
    System.out.print(format.format(file.lastModified()));
}
System.out.flush();
