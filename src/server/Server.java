package server;

import java.io.*;
import static java.lang.Thread.sleep;
import java.net.*;
import java.util.*;

interface Contacts{
    static class Client
    {
        public String nickname;
        Socket socket;
        Scanner input;
        PrintWriter output;

        public Client (Socket socket) throws IOException
        {
            this.socket = socket;
            input = new Scanner (socket.getInputStream());
            output = new PrintWriter (socket.getOutputStream (), true);
            nickname = "";
        }
        
        public void putInfo(String nick)
        {
            nickname = nick;
            ClientInfo.put(nick,this);
        }
    }
    
    static List<Client> ClientList = new ArrayList ();
    
    static ArrayList<String> ArrayClients = new ArrayList<String>();
    
    HashMap <String, Client> ClientInfo = new HashMap<>();
}

public class Server implements Contacts
{
    private static ServerSocket serverSocket;
    private static final int PORT = 1234;
 
    
    
    public static void main(String[] args) throws IOException
    {
         
        try
        {
            serverSocket = new ServerSocket(PORT);
        }
        catch (IOException ioEx)
        {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }
 
        do
        {
            Socket clientSoc = serverSocket.accept();
            addClient(clientSoc);
            
        }while (true);
    }
    
    public static void addClient(Socket socket) throws IOException
    {
        Client newCl = new Client (socket);
        ClientList.add (newCl);
        new ClientThread (newCl).start ();
    }
    
    public static void sendMessageToAll (String sender, String message)
    {
        String Message = sender + " sends: " + message.substring(6);
        System.out.println (Message);
    }
    
    public static void sendMessagePrivate (String sender, String message)
    {
        message = message.substring(4);  //remove the msg prefix
        String reciever = message.substring(0,message.indexOf(' '));  //extract the reciever name
        message = message.substring(message.indexOf(' ')+1);  //remove the reciever name, remaining only the message
        System.out.println(sender + " sends a private message to " + reciever);
        ClientInfo.get(reciever).output.println (message);
    }
    
    public static void showUsers()
    {
        System.out.println("Lista utilizatorilor conectati:\n" + ArrayClients);
    }
    
    public static void changeNickname (Client client, String nick)
    {
        if(ArrayClients.contains(nick) == false)
        {
            System.out.println(client.nickname);
            ArrayClients.remove(client.nickname);
            client.nickname = nick.substring(5);
            ArrayClients.add(client.nickname);
            System.out.println(" changed his nickname into " + client.nickname);
            System.out.println(client.nickname + " connected");
        
        }
        else
            System.out.println("Nickname already exists. Choose another one!");
    }
}

class ClientThread extends Thread implements Contacts
{
    Scanner input;
    Server.Client client;
    
    public ClientThread (Server.Client Client)
    {
        this.client = Client;
    }
    
    @Override
    public void run()
    {
        try {
            input = new Scanner (client.socket.getInputStream ());
        } catch (IOException e) {
            System.out.println ("Could not get input from client");
            e.printStackTrace ();
        }

        while (true) {

            while (!input.hasNextLine ())
                try {
                    sleep (5);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            String message = input.nextLine ();
            String opt = message.substring(0, 3);

            if (message.equals ("quit")) {
                try {                   
                    client.socket.close ();
                    System.out.println (client.nickname + " disconnected");
                } catch (IOException e) {
                    System.out.println ("Could not disconnect client");
                    e.printStackTrace ();
                }
                break;
            }
            
            else {

                if (client.nickname.isEmpty () == true) {
                    if(ArrayClients.contains(client.nickname) == false)
                    {
                        ArrayClients.add(message);
                        client.putInfo(message);
                        System.out.println (message + " conected");
                    }
                    else
                        System.out.println("Nickname already exists. Choose another one!");
                }
                else
                    switch(opt) {
                        case "lis":
                            Server.showUsers();
                            break;
                        case "msg":
                            Server.sendMessagePrivate(client.nickname,message);
                            break;
                        case "bca":
                            Server.sendMessageToAll(client.nickname, message);
                            break;
                        case "nic":
                            Server.changeNickname(client,message);
                            break;
                        default:
                            System.out.println("Undefined command!");
                            break;
                    }
                
            }
        }
    }
}