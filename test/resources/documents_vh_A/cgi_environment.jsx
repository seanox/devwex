var System = java.lang.System;
var TreeSet = java.util.TreeSet;

System.out.print("HTTP/1.0 200\r\n\r\n");
var environment = System.getenv();
var keyIterator = new TreeSet(environment.keySet()).iterator();
while (keyIterator.hasNext()) {
    var key = keyIterator.next();
    System.out.print(key + "=" + environment.get(key) + "\r\n");
    System.out.flush();
}
