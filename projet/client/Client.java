import java.net.*;
import java.util.*;
import java.io.*;
public class Client
{
    static Scanner lecteur = new Scanner(System.in);
    public static void main(String [] args)
    {
	try{
            DatagramSocket clientSocket = new DatagramSocket();
            InetAddress IPAddress = InetAddress.getByName("127.0.0.1");
            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];
            String sentence = "SHOPLIST";//lecteur.next();
            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
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
}