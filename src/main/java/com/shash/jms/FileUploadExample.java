package com.shash.jms;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FileUploadExample {

     ChannelSftp channelSftp;

    public void setUpSsh(){

        Session jschSession = null;
        try {
            JSch jsch = new JSch();
            jschSession = jsch.getSession("username",
                    "host");
            jschSession.setPassword("password");
            jschSession.connect();
            this.channelSftp = (ChannelSftp)jschSession.openChannel("sftp");
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    public void store() {
        try {
            File file = new File("table.txt");
            FileInputStream fis = new FileInputStream(file);
            this.channelSftp.connect();
            this.channelSftp.put(fis, "remote_directory");
        }
        catch (FileNotFoundException | JSchException | SftpException e) {
            System.out.println("error"+e);
        }
        finally{
            this.channelSftp.disconnect();
        }
    }
}
