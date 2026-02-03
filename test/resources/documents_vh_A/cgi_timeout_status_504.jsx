var System = java.lang.System;
var Thread = java.lang.Thread;

for (var loop = 0; loop < 100; loop++) {
    System.out.print(loop);
    System.out.flush();
    Thread.sleep(1000);
}
