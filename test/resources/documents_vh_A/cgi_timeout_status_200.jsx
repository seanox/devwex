var System = java.lang.System;
var Thread = java.lang.Thread;

System.out.print("HTTP/1.0 200\r\n\r\n");
System.out.flush();
for (var loop = 0; loop < 100; loop++) {
    System.out.print(loop);
    System.out.flush();
    Thread.sleep(1000);
}
