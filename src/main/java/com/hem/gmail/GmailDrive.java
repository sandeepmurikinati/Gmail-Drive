package com.hem.gmail;

import java.util.Properties;
import java.util.Scanner;

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.mail.search.SearchTerm;

public class GmailDrive {


    public static final String USERNAME = "vijji.thatha@gmail.com";

    public static void check(String host, String storeType, String user, String password) {
        try {

            // create properties
            Properties properties = new Properties();

            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.starttls.enable", "true");
            properties.put("mail.imap.ssl.trust", "*");

            Session emailSession = Session.getDefaultInstance(properties);

            // create the imap store object and connect to the imap server
            Store store = emailSession.getStore("imaps");

            store.connect(host, user, password);

            // create the inbox object and open it
            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            NodeManager nodeManager = new NodeManager(inbox, emailSession);

            Scanner in = new Scanner(System.in);
            String command;
            System.out.println("Please enter command");
            System.out.println("1 for Exit");
            System.out.println("2 <File location> to upload file");
            do {
                command = in.nextLine();
                if (command.startsWith("2")) {
                    String fileLocation = command.substring(2);
                    nodeManager.uploadFile(fileLocation);
                }
            } while(command.equalsIgnoreCase("1"));

            inbox.close(false);
            store.close();

        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        String host = "imap.gmail.com";
        String mailStoreType = "imap";
        String password = "kmtszlflodwxtgdt";

        check(host, mailStoreType, USERNAME, password);

    }

}