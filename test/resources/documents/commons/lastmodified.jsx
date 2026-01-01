var System = Java.type("java.lang.System");
var File = Java.type("java.io.File");
var SimpleDateFormat = Java.type("java.text.SimpleDateFormat");
var Locale = Java.type("java.util.Locale");

System.out.print("HTTP/1.0 200\r\n\r\n");
var file = System.getenv().get("HTTP_FILE") || "";
file = file.matches("^[^\\\\/].*$") ? new File(new File("../../.."), file) : new File(file);
file = file.getCanonicalFile()
if (file.exists()) {
    var format = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.US);
    System.out.print(format.format(file.lastModified()));
}
System.out.flush();
