/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.replica2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ACER
 */
public class Client {
    public Socket socket;
    public ObjectOutputStream out = null;
    public ObjectInputStream in = null;
    public String name;

    public Client(Socket socket, String name) throws IOException {
        this.socket = socket;
        this.name = name;

            out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("3");
            in = new ObjectInputStream(socket.getInputStream());
            send(new Message(this.name, "Login"));
    }
    public void requestHandler(Message wR) throws IOException {
        if (null == wR.getMethod()) {

        } else {
            switch (wR.getMethod()) {
                case "Read":

                    break;
                case "Write":
                    
                    break;
                case "Post":
                    break;
                default:

                    break;
            }
        }
    }
    public Message receive() {
        Message wR = new Message();
        try {
            wR = (Message) in.readObject();
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("message received");
        return wR;
    }
    public void send(Message wR) throws IOException {
        out.writeObject(wR);
        System.out.println("message sended");
        out.flush();
    }
    public void sendPostRequest(String fileName,String content){
        try {
            send(new Message(fileName,content,"Post"));
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void sendReadRequest(String fileName){
        try {
            send(new Message(fileName,"read"));
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void sendWriteRequest(String fileName,String content){
        try {
            send(new Message(fileName,content,"Write"));
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//    public String receiveWriteRequest(Message wR){
//        wR
//    }
//    public String receiveReadRequest(Message wR){
//        
//    }
//    public String receivePostRequest(Message wR){
//        
//    }
    public static void main(String[] args) {
            Socket socket=null;
        try {
            socket = new Socket("localhost", 8088);
            
        } catch (IOException ex) {
            Logger.getLogger(Replica2.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            Client client =new Client(socket,"Khoa");
            //client.sendPostRequest("abc", "bac");
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
       
        
    }
}
