/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.replica2;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ACER
 */
public class Slave {
    private int id;
    //public static ArrayList<Slave> slaves=new ArrayList<>();
    private ArrayList<String> fileList=new ArrayList<>();
    File currentDirFile = new File(".");
    private String dataPath;
    private String path;
    private boolean available=true;
    private int numberOfFiles;
    public Slave(int id) {
        this.id = id;
        this.path=currentDirFile.getAbsolutePath()+"/slave_"+this.id;
        new File(this.path).mkdirs();
        this.dataPath = path + "/data/";
        new File(this.dataPath).mkdirs();
        File dataDir=new File(dataPath);
        String[] files = dataDir.list();
  
            //System.out.println("Files are:"); 
  
            // Display the names of the files 
            for (int i = 0; i < files.length; i++) { 
                //System.out.println(files[i]);
                fileList.add(files[i]);
                numberOfFiles++;
            } 
        //boolean add = slaves.add(this);
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
    public boolean isContainFile(String fileName){
        for(int i=0;i<fileList.size();i++){
            if (fileList.get(i).equals(fileName+".txt")) return true;
        }
        return false;
    }
    public void printFileList(){
        for (int i=0;i<fileList.size();i++){
            System.out.print(fileList.get(i)+"\n");
        }
    }

    public void createFile(String fileName) {
        File file=new File(dataPath+fileName+".txt");
        try {
            file.createNewFile();
            fileList.add(fileName+".txt");
            System.out.println("file created at slave"+getId());
            numberOfFiles++;
        } catch (IOException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }
    public void deleteFile(String fileName){
        File myObj = new File(dataPath+fileName+".txt");
        if (myObj.delete()) { 
            System.out.println("Deleted the file: " + myObj.getName());
          } else {
            System.out.println("Failed to delete the file.");
    }
    }
    public void writeFile(String fileName,String content){
                try {

            // Open given file in append mode by creating an
            // object of BufferedWriter class
                    BufferedWriter out1 = new BufferedWriter(new FileWriter(dataPath+fileName+".txt", true));
        //            if (content.startsWith("\n")){
        //                System.err.println("hello");
        //            }
                    // Writing on output stream
                    System.out.println("file writed at slave"+getId());
                    out1.write(content);
                    // Closing the connection
                    out1.close();
        } // Catch block to handle the exceptions
        catch (IOException e) {

            // Display message when exception occ
            
            System.out.println("exception occurred" + e);
        }
    }

    public ArrayList<String> getFileList() {
        return fileList;
    }
    public String getFileContent(String fileName){
        String data = null;
        try {
            
            File myObj = new File(dataPath + fileName + ".txt");
            Scanner myReader = new Scanner(myObj);
            data = "";
            while (myReader.hasNextLine()) {
                data += myReader.nextLine() + "\n";
                //System.out.println(data);
            }
            if(data.length()>0)
                data=data.substring(0,data.length()-1);
            myReader.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Slave.class.getName()).log(Level.SEVERE, null, ex);
        }
        return data;
    }
    public void sync(){
        
    }
    public int getId() {
        return id;
    }

}
