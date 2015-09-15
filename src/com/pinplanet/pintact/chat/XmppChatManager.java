package com.pinplanet.pintact.chat;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.delay.packet.DelayInformation;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Avinash on 8/2/15.
 */
public class XmppChatManager {

    private AbstractXMPPConnection connection = null;
    private MultiUserChat mchat;
    private ChatListener chatListener;
    private String chatServer;
    private String groupChatServer;
    private String username;
    private String password;
    private boolean isOn = false;

    public XmppChatManager(ChatListener chatListener, String chatServer, String groupChatServer, String username, String password){
        this.chatListener = chatListener;
        this.chatServer = chatServer;
        this.groupChatServer = groupChatServer;
        this.username = username;
        this.password = password;
        Log.d("CONNECT", "Username " + username + " password "+ password);
    }

    public boolean join(String group){
        try {
            initConnection();
            MultiUserChatManager mchatManager = MultiUserChatManager.getInstanceFor(connection);
            mchat = mchatManager.getMultiUserChat(group + "@"+ groupChatServer);
            if(!mchat.isJoined())
            {
                Log.d("CONNECT", "Joining room !! "+ group + " and username " + username);
                boolean createNow = false;
                try{
                    mchat.createOrJoin(username);
                    createNow = true;
                }
                catch (Exception e){
                    Log.d("PINTACT", "Error while creating the room "+group + e.getMessage());
                }
                if(!mchat.isJoined())
                {
                   // mchat.join(username);
                }
                if(createNow){
                    mchat.sendConfigurationForm(new Form(DataForm.Type.submit)); //this is to create the room immediately after join.
                }
            }
            Log.d("CONNECT", "Room created!!");

            /*Message msg = new Message();
            msg.setBody("Please join");
            msg.setSubject("Invite");
            mchat.invite("avi@107.20.207.41", "Invite");*/
           /* mchat.addMessageListener(new MessageListener() {
                @Override
                public void processMessage(Message message) {
                    final com.pinplanet.pintact.chat.Chat chat = new com.pinplanet.pintact.chat.Chat();
                    chat.setFrom(message.getFrom());
                    chat.setMessage(message.getBody());

                    DelayInformation inf2 = (DelayInformation) message.getExtension("delay", "urn:xmpp:delay");
                    if(inf2 != null){
                        chat.setTime(getConvertedTime( inf2.getStamp()));
                    }
                    else{
                        chat.setTime(new Date());
                    }

                    chatListener.processMessage(chat);
                }
            });*/
            Log.d("CONNECT", "Chat created!!");
            return true;
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void initConnection(){
        if(connection == null || !connection.isConnected()){
            try{
                Log.d("CONNECT", "Trying to conenct with username "+ username + " and pasword "+ password);
                XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();
                configBuilder.setUsernameAndPassword(username, password);
                //configBuilder.setResource("SomeResource");
                configBuilder.setServiceName(chatServer);
                configBuilder.setHost(chatServer);
                configBuilder.setPort(5222);
                //configBuilder.setDebuggerEnabled(true);
                configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

                connection = new XMPPTCPConnection(configBuilder.build());
                connection.connect();
                connection.setPacketReplyTimeout(100000);

                Log.d("CONNECT", "Trying to Login " + username + " password " + password);
                connection.login(username, password);
                Log.d("CONNECT" , "Trying to message");

                Presence presence = new Presence(Presence.Type.available);
                connection.sendPacket(presence);

                PacketFilter filter = MessageTypeFilter.GROUPCHAT;
                connection.addSyncPacketListener(new PacketListener() {
                    @Override
                    public void processPacket(Packet packet) throws SmackException.NotConnectedException {
                        Message message = (Message)packet;
                        Log.d("Debugging", "Message3: " + message.toString());
                        final com.pinplanet.pintact.chat.Chat chat = new com.pinplanet.pintact.chat.Chat();
                        chat.setFrom(ChatAdaptor.parseFrom(message.getFrom()));
                        chat.setMessage(message.getBody());
                        DelayInformation inf2 = (DelayInformation) message.getExtension("delay", "urn:xmpp:delay");
                        if(inf2 != null){
                            chat.setTime(getConvertedTime( inf2.getStamp()));
                        }
                        else{
                            chat.setTime(new Date());
                        }
                        chat.setTo(parseGroupId(message.getFrom()));
                        chat.setId(message.getPacketID());
                        chatListener.processMessage(chat);
                        Log.d("CONNECT", "Message from: "+ message.getFrom() +" to: "+message.getTo()+" " + message.getBody());
                    }
                }, filter);
            }
            catch (SmackException e) {
                Log.d("CONNECT", "Error in connecting to chat server ", e);
            } catch (IOException e) {
                Log.d("CONNECT", "Error in connecting to chat server ", e);
            } catch (XMPPException e) {
                Log.d("CONNECT", "Error in connecting to chat server ", e);
            }

        }
    }
    public void connect(String username){
        try {
            Log.d("CONNECT" , "Trying to conenct");
            initConnection();
            Chat chat = ChatManager.getInstanceFor(connection)
                    .createChat(username+"@" + chatServer);
            Log.d("CONNECT" , "Chat created!!");
            chat.sendMessage("Howdy!");
        } catch (SmackException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) throws XMPPException, SmackException.NotConnectedException {
        mchat.sendMessage(message);
    }

    public Date getConvertedTime(Date input){
        try {
            DateFormat formatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss z");
            formatter.setTimeZone(TimeZone.getDefault());
            String newTime = formatter.format(input);
            return formatter.parse(newTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return input;
    }

    public static String parseGroupId(String from){
        int index = from.indexOf("@");
        if(index != -1){
            return from.substring(0, index);
        }
        return from;
    }

    public MultiUserChat getMchat() {
        return mchat;
    }
}
