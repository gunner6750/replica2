/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author ACER
 */

import com.mycompany.replica2.Message;
import static Server.Server.availableSlave;
import static Server.Server.files;
import static Server.Server.numberOfReplicas;
import static Server.Server.partitions;
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
    private int maximumChar=2500;
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
                    if(wR.getMessage().length()<maximumChar)
                    postFile(wR);
                    else postBigFile(wR);
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
    public String getFileContent(String fileName){
        String data = null;
        for (Slave slave : availableSlave) {
            if (slave.isContainFile(fileName)) {
                data = slave.getFileContent(fileName);
                System.out.println("the datais"+data);
                break;
            }
        }
        return data;
    }
    public void readFile(Message wR){
        String fileName=wR.getFilename();
        int partition = partitions.get(files.indexOf(fileName));
        if(partition==1)
        {
            readSmallFile(wR);
        }
        else
        {
            readBigFile(wR, partition);
        }
    }
    public void readBigFile(Message wR, int partition){
        String fileName=wR.getFilename();
        fileName=fileName.substring(0, fileName.lastIndexOf('.'));
        String data="";
        for (int i=0;i<partition;i++){
            data+=getFileContent(fileName+"_"+i);
            System.out.println(fileName+"_"+i);
        }
        try {
            send(new Message(wR.getFilename(), data, "Read"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void readSmallFile(Message wR) {
        
        String fileName = wR.getFilename();
        fileName=fileName.substring(0, fileName.lastIndexOf('.'));
        System.out.println(fileName);
        String data = getFileContent(fileName);
        
        
        try {
            send(new Message(wR.getFilename(), data, "Read"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void writeFile(Message wR) {
        String message = "Failed";
        String fileName = wR.getFilename();
        String data = wR.getMessage();
        for (Slave slave : availableSlave) {
            if (slave.isContainFile(fileName)) {
                slave.writeFile(fileName, data);
                message = "Successful";
            }
        }

        try {
            send(new Message(message, "Write"));
        } catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void addToFileList(String fileName,int partition){
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
            
            out1.write("\n"+fileName+".txt"+partition);
            // Closing the connection
            out1.close();
        } // Catch block to handle the exceptions
        catch (IOException e) {

            // Display message when exception occ
            
            System.out.println("exception occurred" + e);
        }
    }
//    public void postBigFile(Message wR,int maxSize){
//        int split=/maxSize;
//        postFile(wR);
//    }
    public void postBigFile(Message wR){
        String fileName=wR.getFilename();
        String content=wR.getMessage();
        int partition=(int) Math.ceil(content.length()/maximumChar);
        for(int i=0;i<partition;i++){
            int start=maximumChar*i;
            int end=maximumChar*(i+1)-1;
            makeFile(fileName+"_"+i, content.substring(start,end));
        }
        System.out.println(partition);
        addToFileList(fileName,partition);
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
    public void makeFile(String fileName,String content){

        List<Integer> chosen = new ArrayList<Integer>();
        for(int i=0;i<numberOfReplicas;i++){
            chosen.add(1);
        }
            
        for (int i = 0; i < availableSlave.size() - numberOfReplicas; i++) {
            chosen.add(0);
        }
        Collections.shuffle(chosen);
        for (int i = 0; i < availableSlave.size(); i++) {
            if (chosen.get(i) == 1 ) {
                availableSlave.get(i).createFile(fileName);
                availableSlave.get(i).writeFile(fileName, content);
            }
        }

    }
    public void postFile(Message wR) {
        String content = wR.getMessage();
        String fileName = wR.getFilename();
        makeFile(fileName, content);
        addToFileList(fileName,1);
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
