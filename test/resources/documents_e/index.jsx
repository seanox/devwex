var System = java.lang.System;

System.out.print("HTTP/1.0 200\r\n\r\n");
System.out.print(System.getenv().get("SCRIPT_NAME"));
System.out.flush();
