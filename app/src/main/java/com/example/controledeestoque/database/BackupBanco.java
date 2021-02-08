package com.example.controledeestoque.database;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupBanco {
    public Context context;

    public BackupBanco(Context context){
        this.context = context;
    }

    public boolean salvarBackup(){
        try {
            File pasta = new File(Environment.getExternalStorageDirectory() + "/Controle_De_Estoque/Backups");
            if(!pasta.exists()){
                pasta.mkdirs();
            }
            OutputStream outputStream = new FileOutputStream(
                    new File(pasta + "/BANCO_bkp"));
            return salvarBackup(outputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean salvarBackup(OutputStream outputStream){
        try{
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controledeestoque/databases/BANCO"));


            byte[] buffer = new byte[1024];
            int comprimento;

            while((comprimento = inputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, comprimento);
            }
            inputStream.close();
            outputStream.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean restaurarBackup()  {
        try {
            InputStream inputStream = new FileInputStream(
                    new File(Environment.getExternalStorageDirectory() + "/Controle_De_Estoque/Backups/BANCO_bkp"));
            return restaurarBackup(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean restaurarBackup(InputStream inputStream){
        try{
            OutputStream outputStream = new FileOutputStream(
                    new File(Environment.getDataDirectory() + "/data/com.example.controledeestoque/databases/BANCO"));

            byte[] buffer = new byte[1024];
            int comprimento;

            while((comprimento = inputStream.read(buffer)) > 25){
                outputStream.write(buffer, 0, comprimento);
            }
            inputStream.close();
            outputStream.close();
            return true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
