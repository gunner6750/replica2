/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.replica2;

/**
 *
 * @author ACER
 */
import static com.mycompany.replica2.Server.slaves;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.FileNameMap;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ACER
 */
public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public Socket socket;
    private ObjectOutputStream out = null;
    private ObjectInputStream in = null;
    private String name;
    File currentDirFile = new File(".");
    private String path = currentDirFile.getAbsolutePath()+"/master";
    private String dataPath = "master/data/";

    public ClientHandler(Socket socket) {

        this.socket = socket;
        System.out.println("client is running at port"+socket.getPort());
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("out socket done");
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            this.in = new ObjectInputStream(socket.getInputStream());
            System.out.println("in socket done");
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        clientHandlers.add(this);

        try {
            this.name = receive().getMessage();
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("new client:" + name);
    }

    public Message receive() throws IOException, ClassNotFoundException {
        
        Message wR = new Message();



            wR = (Message) in.readObject();

            System.out.println("the message is:"+wR.getMessage());

            

        return wR;
    }

    public void send(Message wR) throws IOException {
        this.out.writeObject(wR);
        this.out.flush();
    }

    public void broadcast(Message wR) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {

                System.out.println("broadcasting");
                clientHandler.send(wR);

            } catch (IOException e) {

            }
        }
    }

    @Override
    public void run() {
        Message wR = new Message();
        while (true) {
            try {
                try {
                    wR = receive();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
                requestHandler(wR);
            } catch (IOException ex) {
                System.out.println("error here");
                this.closeAll();
                
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                break;
            }
        }
    }

    public void requestHandler(Message wR) throws IOException {

        if (null == wR.getMethod()) {

        } else {
            switch (wR.getMethod()) {
                case "Post":
                    break;
                case "Read":
                    readFile(wR);
                    break;
                case "Write":
                    writeFile(wR);
                    System.out.println(wR.getMessage());
                    break;
                case "Login":
                    this.name=wR.getMessage();
                    break;
                default:
                    break;
            }
        }

    }

    public void readFile(Message wR) {
        String fileName=wR.getFilename();
                String data = null;
        for (Slave slave : slaves) {
            if(slave.isContainFile(fileName))
                data=slave.getFileContent(fileName);
        }
        try {
            send(new Message(wR.getFilename(),data,"Read"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeFile(Message wR) {
        String message="failed";
        String fileName=wR.getFilename();
        String data=wR.getMessage();
        for (Slave slave : slaves) {
            if(slave.isContainFile(fileName)){
                slave.writeFile(fileName, data);
                message="successful";
            }
        }
        
        
        try {
            send(new Message(message,"Write"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void postFile(Message wR){
        String content=wR.getMessage();
        String fileName=wR.getFilename();
        List<Integer> chosen = new ArrayList<Integer>();
        chosen.add(1);
        chosen.add(1);
        chosen.add(1);
        chosen.add(1);
        chosen.add(1);
        for(int i=0;i<slaves.size()-5;i++){
            chosen.add(0);
        }
        Collections.shuffle(chosen);
        for (int i=0;i<slaves.size();i++){
            if(chosen.get(i)==1 && slaves.get(i).isAvailable()){
                slaves.get(i).createFile(fileName);
                slaves.get(i).writeFile(fileName, content);
            }
        }
        try {
            send(new Message("Successful","Post"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void appendStrToFile(String fileName, String str) {

    }

    public void closeAll() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
