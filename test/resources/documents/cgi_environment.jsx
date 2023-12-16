var System = Java.type("java.lang.System");
var Iterator = Java.type("java.util.Iterator");
var TreeSet = Java.type("java.util.TreeSet");

System.out.print("HTTP/1.0 200\r\n\r\n");
var keyIterator = new TreeSet(System.getenv().keySet()).iterator();
while (keyIterator.hasNext()) {
    var key = keyIterator.next();
    System.out.print(key + "=" + System.getenv().get(key) + "\r\n");
    System.out.flush();
}
