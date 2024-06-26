/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

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
    public static ArrayList<Integer> partitions=new ArrayList<>();
    public static int numberOfReplicas=5;
    public static ArrayList<Slave> slaves=new ArrayList<>();
    public static ArrayList<String> files=new ArrayList<>();
    public static ArrayList<Slave> availableSlave = new ArrayList<>();
    File currentDirFile = new File(".");
    private String fileContent;
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
            importFile();
            availableSlave=getEnabledSlaves();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void importFile() throws FileNotFoundException, IOException{
        ArrayList<String> files = new ArrayList<String>();
        ArrayList<Integer> partitions = new ArrayList<>();
        // load data from file
        BufferedReader bf = new BufferedReader(
            new FileReader(path+"/fileList.txt"));
       
        // read entire line as string
        String line = bf.readLine();
        
        // checking for end of file
        while (line != null) {
            String[] arr=line.toString().split(" ");
            System.out.println(arr[1]);
            System.out.println(arr[0]);
            partitions.add(Integer.valueOf(arr[1]));
            files.add(arr[0]);
            line = bf.readLine();
        }
       
        // closing bufferreader object
        bf.close();
       
        // storing the data in arraylist to array
//        String[] array
//            = listOfStrings.toArray(new String[0]);
       
        this.files = files;
        this.partitions=partitions;
    }
    public void create10Slaves(){
        for(int i=1;i<=10;i++){
            createSlave();
        }
    }
    public ArrayList<String> getFileListOfSlave(int slaveId){
        return slaves.get(slaveId).getFileList();
    }
    public ArrayList<Slave> getEnabledSlaves(){
        ArrayList<Slave> s = new ArrayList<Slave>();
        for (Slave slave : slaves) {
            if(slave.isAvailable()){
                s.add(slave);
            }
        }
        return s;
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

    public String readFile(String fileName,int slaveId) throws FileNotFoundException {
        fileName=fileName.substring(0, fileName.lastIndexOf('.'));
        String data = null;
        if (slaves.get(slaveId).isAvailable()){
        data=slaves.get(slaveId).getFileContent(fileName);
        }
        return data;
    }
    public ArrayList<Slave> slavesContainFile(String fileName){
        ArrayList<Slave> s =new ArrayList<>();
        for (Slave slave : slaves){
            if(slave.isContainFile(fileName)){
                s.add(slave);
                System.out.println("slave"+slave.getId());
            }
        }
        return s;
    }
    public boolean isAvailable(int slaveId){
        return slaves.get(slaveId).isAvailable();
    }
    public int getNumberOfFileOfSlave(int slaveId){
        return slaves.get(slaveId).getNumberOfFiles();
    }
    public void createSlave(){
        Slave slave =new Slave(slaves.size());
        slaves.add(slave);
    }
    public void disableSlave(int slaveId){
        slaves.get(slaveId).setAvailable(false);
        availableSlave.remove(slaves.get(slaveId));
    }
    public void enableSlave(int slaveId){
        Slave slave=slaves.get(slaveId);
        slave.setAvailable(true);
        ArrayList<String> fileList=slave.getFileList();
        for(int i=0;i<fileList.size();i++){
            String fileName=fileList.get(i);
        
                for (Slave slave1 : availableSlave){
                    if (slave1.isContainFile(fileName)){
                        String fileContent=slave1.getFileContent(fileName);
                        slave.deleteFile(fileName);
                        slave.createFile(fileName);
                        slave.writeFile(fileName, fileContent);
                    }
                }
            
        }
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
