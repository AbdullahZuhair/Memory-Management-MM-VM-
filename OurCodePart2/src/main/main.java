package main;
/*

Name: 
Mohammed Alghamdi
Abdullah Almontashri
Abdulmajed Almalki
Tamim Alhumaydhi


Compiler Version: NetBeans IDE 8.2 (Build 201609300101)

Java: 1.8.0_131; Java HotSpot(TM) 64-Bit Server VM 25.131-b11

Runtime: Java(TM) SE Runtime Environment 1.8.0_131-b11

System: Windows 10 version 10.0 running on amd64; en_US (nb)

*/

import java.io.IOException;

public class main {

    public static void main(String[] args) {

        vmemmgr manager = new vmemmgr();
        
        //if the program is used from command line use arguments to get addresses.txt
        String argFilePath = null;
        if (args.length != 0) {
            argFilePath = args[0];
        }

        try {
            manager.run(argFilePath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
