package com.mycompany.replica2;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */


import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author ACER
 */
public class Message implements java.io.Serializable{
    public boolean status ;
    private String filename="";
    private String message="";
    private String dateTime;
    private String method="";
    public Message(boolean status, String filename, String message,String method) {
        this.method=method;
        this.status = status;
        this.filename = filename;
        this.message = message;
        
        this.dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
    }

    public String getMethod() {
        return method;
    }

    public Message(String filename, String message,  String method) {
        this.filename = filename;
        this.message = message;
        
        this.dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        this.method = method;
    }

    public Message(String message,String method) {
        this.message=message;
        this.dateTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
        this.method = method;
    }


    public Message() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}
