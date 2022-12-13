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

import static com.hem.gmail.NodeManager.SPLIT_SIZE;

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
            properties.put("mail.imaps.fetchsize", SPLIT_SIZE);
            properties.put("mail.imaps.partialfetch", "false");

            Session emailSession = Session.getDefaultInstance(properties);

            // create the imap store object and connect to the imap server
            Store store = emailSession.getStore("imaps");

            store.connect(host, user, password);

            // create the inbox object and open it
            Folder inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_WRITE);

            NodeManager nodeManager = new NodeManager(inbox, emailSession);

            Scanner in = new Scanner(System.in);
            String command= "";
            printMenu();
            do {
                try {
                    command = in.nextLine();
                    if (command.startsWith("2")) {
                        String fileLocation = command.substring(2);
                        nodeManager.uploadFile(fileLocation);
                    } else if (command.startsWith("3")) {
                        nodeManager.printFiles();
                    } else if (command.startsWith("4")) {
                        nodeManager.downloadFile(command.substring(2));
                    } else if (command.startsWith("5")) {
                        nodeManager.deleteFile(command.substring(2));
                    } else if (command.startsWith("0")) {
                        printMenu();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while(!command.equalsIgnoreCase("1"));

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

    private static void printMenu() {
        System.out.println("Please enter command");
        System.out.println("0 to print commands");
        System.out.println("1 for Exit");
        System.out.println("2 <File location> to upload file");
        System.out.println("3 to list files");
        System.out.println("4 <Node1> to download files");
        System.out.println("5 <Node1> to delete files");
    }

    public static void main(String[] args) {

        String host = "imap.gmail.com";
        String mailStoreType = "imap";
        String password = "kmtszlflodwxtgdt";

        check(host, mailStoreType, USERNAME, password);

    }

}