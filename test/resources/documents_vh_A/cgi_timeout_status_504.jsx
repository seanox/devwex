var System = Java.type("java.lang.System");
var Thread = Java.type("java.lang.Thread");

for (var loop = 0; loop < 100; loop++) {
    System.out.print(loop);
    System.out.flush();
    Thread.sleep(1000);
}
