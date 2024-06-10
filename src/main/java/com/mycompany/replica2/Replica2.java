/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.replica2;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author ACER
 */
public class Replica2 {

    public static void main(String[] args) throws ClassNotFoundException {
        System.out.println("Hello World!");
            // Create a file object 
//            Slave slave=new Slave("2");
//            slave.printFileList();
//            System.out.print(slave.isContainFile("tes"));
//            String content="hello\nimyoutmom";
//            slave.writeFile("tit", content);
//            System.out.println(slave.getFileContent("tit"));
    File currentDirFile = new File(".");
    ServerSocket serverSocket=null;
        try {
            serverSocket = new ServerSocket(8089);
        } catch (IOException ex) {
            Logger.getLogger(Replica2.class.getName()).log(Level.SEVERE, null, ex);
        }
    String path = currentDirFile.getAbsolutePath();
    Server server=new Server(serverSocket);
    server.serverStart();
    Socket socket=null;
        try {
            socket = new Socket("localhost", 8089);
        } catch (IOException ex) {
            Logger.getLogger(Replica2.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("client");
    Client client =new Client(socket,"Khoa");
    client.sendPostRequest("abc", "bac");
 
    }
}
