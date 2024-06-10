/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.replica2;

/**
 *
 * @author ACER
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static ArrayList<Slave> slaves=new ArrayList<>();
    public static ArrayList<String> files=new ArrayList<>();
    File currentDirFile = new File(".");
    
    private String path = currentDirFile.getAbsolutePath()+"/master";
    //private String path = "C:\\Users\\ACER\\OneDrive\\Documents\\NetBeansProjects\\Replica\\replica\\master";
    private String dataPath;
    private ServerSocket serverSocket;

    public Server() {
    }

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.dataPath = path + "/data";
        new File(this.dataPath).mkdirs();
        this.dataPath = this.dataPath + "/";
        create10Slaves();
        try {
            files=fileArrayList();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private ArrayList fileArrayList() throws FileNotFoundException, IOException{
        ArrayList<String> listOfStrings = new ArrayList<String>();
        // load data from file
        BufferedReader bf = new BufferedReader(
            new FileReader(path+"/fileList.txt"));
       
        // read entire line as string
        String line = bf.readLine();
       
        // checking for end of file
        while (line != null) {
            listOfStrings.add(line);
            line = bf.readLine();
        }
       
        // closing bufferreader object
        bf.close();
       
        // storing the data in arraylist to array
        String[] array
            = listOfStrings.toArray(new String[0]);
       
        return listOfStrings;
    }
    public void create10Slaves(){
        for(int i=1;i<=10;i++){
            createSlave();
        }
    }
    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void setPath(String path) {
        this.path = path;
        this.dataPath = "/master" + "/data";
        new File(this.dataPath).mkdirs();
        this.dataPath = this.dataPath + "/";
    }

    /**
     *
     */
    public void serverStart() {
        try {
            System.out.println("Server is started at port "+ serverSocket.getLocalPort());
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New Friend Connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
        }
    }

    ;
	 public void closerServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//	 public static void main(String[] args) throws Exception {
//		 ServerSocket serverSocket = new ServerSocket(1234);
//		 Server server = new Server(serverSocket);
//		 server.serverStart();
//	 }

    public void addFile(String fileName) throws IOException {
        File myObj = new File(dataPath + fileName + ".txt");
        myObj.createNewFile();
    }

    public String readFile(String fileName) throws FileNotFoundException {
        String data = null;
        for (Slave slave : slaves) {
            if(slave.isContainFile(fileName))
                data=slave.getFileContent(fileName);
        }
        return data;
    }
    public Vector<Integer> getIdList()
    {
        Vector<Integer> v = new Vector<Integer>();
        for(int i=0; i<=slaves.size(); i++)
        {
            v.add(slaves.get(i).getId());
        }
        return v;
    }
    public void createSlave(){
        Slave slave =new Slave(slaves.size());
        slaves.add(slave);
    }
    public void disableSlave(int id){
        slaves.get(id).setAvailable(false);
    }
    public void enableSlave(int id){
        slaves.get(id).setAvailable(true);
    }

    public static void main(String[] args){
            ServerSocket serverSocket=null;

        try {
            serverSocket = new ServerSocket(8088);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    
    Server server=new Server(serverSocket);
    server.serverStart();
    }
}
