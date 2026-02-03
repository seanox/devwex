var System = java.lang.System;

System.out.print("HTTP/1.0 200\r\n\r\n");
var bytes = java.lang.reflect.Array.newInstance(java.lang.Byte.TYPE, 65535);
var size;
while ((size = System['in'].read(bytes)) >= 0) {
    System.out.write(bytes, 0, size);
    System.out.flush();
}
