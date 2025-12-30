var System = Java.type("java.lang.System");
var Thread = Java.type("java.lang.Thread");
while (System.in.read() >= 0)
    Thread.sleep(250);
System.out.print("\r\n\r\nfailed");
