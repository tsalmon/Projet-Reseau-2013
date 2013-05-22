import java.net.*;
import java.util.*;
import java.io.*;
public class Client
{
    static Scanner lecteur = new Scanner(System.in);
    
    public static void udp()
    {
	try{
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            String sentence = "SHOPLIST";//lecteur.next();
            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 5000);
            clientSocket.send(sendPacket);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            String modifiedSentence = new String(receivePacket.getData());
            System.out.println("FROM SERVER:" + modifiedSentence);
            clientSocket.close();
        }
        catch(Exception e)
            {
                e.printStackTrace();
            }
    }

    public static void tcp()
    {
	Socket socket;
	BufferedReader in;
	PrintWriter out;

	try {
	    socket = new Socket(InetAddress.getByName("localhost"),5000);
	    System.out.println("Demande de connexion");
	    out = new PrintWriter(socket.getOutputStream());
	    out.println("SHOPINFO eeeshop");
	    out.flush();
	    in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
	    String message_distant = in.readLine();
	    System.out.println(message_distant);
	    socket.close();
	}catch (UnknownHostException e) {
	    e.printStackTrace();
	}catch (IOException e) {
	    e.printStackTrace();
	}
    }
    
    public static void connexion_client(String nom_client, String adresse_ville, String port_ville)
    {	
	Socket socket;
	BufferedReader in;
	PrintWriter out;
	
	try {
	    int port = Integer.parseInt(port_ville);
	    socket = new Socket(InetAddress.getByName(adresse_ville),port);
	    System.out.println("Demande de connexion");
	    out = new PrintWriter(socket.getOutputStream());
	    out.println("SHOPLIST");
	    out.flush();
	    in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
	    String message_distant = in.readLine();
	    System.out.println(message_distant);
	    socket.close();
	}catch (UnknownHostException e) {
	    System.err.println("Serveur inconnu");
	}catch (IOException e) {
	    e.printStackTrace();
	}catch(NumberFormatException e)
	    {
		System.err.println("Le port doit etre un entier naturel");
	    }
    }
    
    public static void main(String [] args)
    {
	if(args.length  < 3)
	    System.out.println("usage:\nClient 'nom' 'adresse ville'");
	else 
	    connexion_client(args[0], args[1], args[2]);
    }
}