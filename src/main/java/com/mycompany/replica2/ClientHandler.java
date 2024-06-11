/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.replica2;

/**
 *
 * @author ACER
 */
import static com.mycompany.replica2.Server.files;
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
    private String path = currentDirFile.getAbsolutePath() + "/master";
    private String dataPath = "master/data/";

    public ClientHandler(Socket socket) throws IOException {

        this.socket = socket;
        System.out.println("client is running at port" + socket.getPort());

        this.out = new ObjectOutputStream(socket.getOutputStream());

        System.out.println("out socket done....");

        this.in = new ObjectInputStream(socket.getInputStream());

        System.out.println("in socket done");

        boolean add = clientHandlers.add(this);
    }

    private Message receive() throws IOException {

        Message wR = new Message();

        try {
            wR = (Message) in.readObject();
        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("the message is:" + wR.getMessage());
        return wR;
    }

    public void send(Message wR) throws IOException {
        this.out.writeObject(wR);
        this.out.flush();
    }
    public void sendFileList(){
        String fileListString="";
        for(int i=0;i<files.size();i++){
            fileListString+=files.get(i)+" ";
        }
        try {
            send(new Message(fileListString,"FileList"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
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
                System.out.println("hej");
                wR = receive();
                requestHandler(wR);
            } catch (IOException ex) {

                Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
                this.closeAll();
                break;
            }
        }
    }

    public void requestHandler(Message wR) throws IOException {

        if (null == wR.getMethod()) {

        } else {
            switch (wR.getMethod()) {
                case "FileList":
                    sendFileList();
                    break;
                case "Post":
                    postFile(wR);
                    break;
                case "Read":
                    readFile(wR);
                    break;
                case "Write":
                    writeFile(wR);
                    System.out.println(wR.getMessage());
                    break;
                case "Login":
                    this.name = wR.getMessage();
                    System.out.println("name is" + this.name);
                    break;
                default:
                    break;
            }
        }

    }

    public void readFile(Message wR) {
        
        String fileName = wR.getFilename();
        fileName=fileName.substring(0, fileName.lastIndexOf('.'));
        System.out.println(fileName);
        String data = null;
        for (Slave slave : slaves) {
            if (slave.isContainFile(fileName)) {
                data = slave.getFileContent(fileName); 
                break;
            }
        }
        System.out.println(data);
        try {
            send(new Message(wR.getFilename(), data, "Read"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeFile(Message wR) {
        String message = "failed";
        String fileName = wR.getFilename();
        String data = wR.getMessage();
        for (Slave slave : slaves) {
            if (slave.isContainFile(fileName)) {
                slave.writeFile(fileName, data);
                message = "successful";
            }
        }

        try {
            send(new Message(message, "Write"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void addToFileList(String fileName){
        for (int i=0;i<files.size();i++){
            if ((fileName+".txt").equals(files.get(i))) return;
        }
        try {

            // Open given file in append mode by creating an
            // object of BufferedWriter class
            BufferedWriter out1 = new BufferedWriter(new FileWriter(path+"/fileList.txt", true));
//            if (content.startsWith("\n")){
//                System.err.println("hello");
//            }
            // Writing on output stream
            
            out1.write("/n"+fileName+".txt");
            // Closing the connection
            out1.close();
        } // Catch block to handle the exceptions
        catch (IOException e) {

            // Display message when exception occ
            
            System.out.println("exception occurred" + e);
        }
    }
    public void postFile(Message wR) {
        
        String content = wR.getMessage();
        String fileName = wR.getFilename();
        List<Integer> chosen = new ArrayList<Integer>();
        chosen.add(1);
        chosen.add(1);
        chosen.add(1);
        chosen.add(1);
        chosen.add(1);
        for (int i = 0; i < slaves.size() - 5; i++) {
            chosen.add(0);
        }
        Collections.shuffle(chosen);
        for (int i = 0; i < slaves.size(); i++) {
            if (chosen.get(i) == 1 && slaves.get(i).isAvailable()) {
                slaves.get(i).createFile(fileName);
                slaves.get(i).writeFile(fileName, content);
            }
        }
        addToFileList(fileName);
        files.add(fileName+".txt");
        
        try {
            send(new Message("Successful", "Post"));
            
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        for (ClientHandler clientHandler : clientHandlers) {

                System.out.println("broadcasting");
                clientHandler.sendFileList();
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
