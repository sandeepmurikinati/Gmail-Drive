package com.hem.gmail;

import com.sun.mail.imap.IMAPFolder;
import org.codehaus.jackson.map.ObjectMapper;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MimeType;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SubjectTerm;
import javax.mail.util.ByteArrayDataSource;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hem.gmail.GmailDrive.USERNAME;

public class NodeManager {
    public static final long SPLIT_SIZE = 20 * 1024 * 1024;
    private static final String METADATA_SUBJECT_NAME = "Metadata";
    private final IMAPFolder inbox;
    private final Session session;

    private ObjectMapper objectMapper = new ObjectMapper();

    private NodeList nodeList;

    private Message metadataMessage;

    public NodeManager(Folder inbox, Session session) throws Exception {
        this.inbox = (IMAPFolder) inbox;
        this.session = session;
        loadMetaData();
    }

    private void loadMetaData() throws Exception {
        Message[] metadata = inbox.search(new SubjectTerm("Metadata"));
        if (metadata.length > 1) {
            throw new IllegalArgumentException("More than 1 metadata found");
        } else if (metadata.length == 0) {
            createMetadataMail();
        } else {
            metadataMessage = metadata[0];
            String s = metadata[0].getContent().toString();
            nodeList = objectMapper.readValue(s, NodeList.class);
        }
    }

    private void createMetadataMail() throws Exception {
        if (nodeList == null) {
            nodeList = new NodeList();
            nodeList.setCurrentNumber(0);
        }
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(USERNAME));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(USERNAME));
        message.setSubject(METADATA_SUBJECT_NAME);
        message.setText(objectMapper.writeValueAsString(nodeList));
        inbox.addMessages(new Message[]{message});
    }

    public void uploadFile(String fileLocation) throws Exception {
        File file = new File(fileLocation);
        Node node = new Node();
        long numberAndIncrement = nodeList.nextNumber();
        node.setId(numberAndIncrement);
        node.setName(file.getName());
        nodeList.addNode(node);
        long size = Files.size(file.toPath());
        List<FileSplit> offsets = new ArrayList<>();
        long offset = 0;
        while (true) {
            long length = SPLIT_SIZE;
            if (offset + length > size) {
                length = size - offset;
                offsets.add(new FileSplit(length, offset));
                break;
            }

            offsets.add(new FileSplit(length, offset));
            offset = offset + length;
        }

        System.out.println("Uploading file = " + file.getName());


        BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(file.toPath()));

        int read = -1;
        int count =1;
        byte[] data = new byte[Math.toIntExact(SPLIT_SIZE)];
        do {
            try {
                read = stream.read(data);
                MimeMessage message = new MimeMessage(session);
                message.setFrom(new InternetAddress(USERNAME));
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(USERNAME));
                String subject = "Node" + node.getId() + " - " + count;
                message.setSubject(subject);
                System.out.println("Uploading part " + count + " of " + offsets.size());
                BodyPart messageBodyPart = new MimeBodyPart();
                Multipart multipart = new MimeMultipart();
                // Create the message part

                messageBodyPart.setText("This is message body");
                multipart.addBodyPart(messageBodyPart);
                messageBodyPart = new MimeBodyPart();

                // Fill the message
                String filename = "file.txt";
                byte[] dataToUse = data;
                if (read != SPLIT_SIZE) {
                    dataToUse = Arrays.copyOfRange(data, 0, Math.toIntExact(read));
                }
                DataSource source = new ByteArrayDataSource(dataToUse, "application/octet-stream");
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(filename);
                multipart.addBodyPart(messageBodyPart);
                message.setContent(multipart);
                inbox.addMessages(new Message[]{message});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (read != 1);
        stream.close();
        updateNodeList();
    }

    private void updateNodeList() throws Exception{
        metadataMessage.setFlag(Flags.Flag.DELETED, true);
        inbox.close(true);
        inbox.open(Folder.READ_WRITE);
        createMetadataMail();
        loadMetaData();
    }
}
