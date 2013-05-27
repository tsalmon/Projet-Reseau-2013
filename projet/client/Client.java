/*
 *0) pas dans une ville
 * VILLE port id -> renvoit la liste de tous les cafés(ip + port)
 *1)Co a une ville 
 * CAFE cafe -> initialise une connexion 
 *2)vers etape 3 
 * HELLO pseudo [message] -> s'identifier sur la diffu
 *
 * /MP pseudo message -> envoyer un message a pseudo
 *
 * /LEFT ->Quitter un café si on est dans un café, si dans une ville, quitter la ville
 *
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
 
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JPanel;
import java.net.*;
import java.io.*;
import java.util.*;

public class Client extends JFrame implements KeyListener, Runnable
{
    private int mode; 
    private String pseudo;
    static Thread recu;

    static int port_client = -1;
    
    private InetAddress inet = null;
    private int PORT;
    private static JTextField entree = new JTextField();
    private static JTextArea texte = new JTextArea();

    int id_cafe = -1;
    ArrayList<String> cafe_nom = new ArrayList<String>();
    ArrayList<String> cafe_ip = new ArrayList<String>();
    ArrayList<Integer> cafe_port = new ArrayList<Integer>();

    private ArrayList<String> historique = new ArrayList<String>();
    private static ArrayList<String> all_historique = new ArrayList<String>();
    private int index_historique = -1;

    private static PrintWriter out;
    private static BufferedReader in;
    private static Socket socket;
    
    boolean reader = false;
    int etat = 0;
    /*
      0: recherche de ville
      1: connexion a une ville
     */
    Client(InetAddress inet, int PORT, int mode)
    {
	reader = true;
	this.inet = inet;
	this.PORT = PORT;
	this.mode = mode;
    }
    
    Client()
    {
	entree.addKeyListener(this);
	texte.setFont(new Font("Monospaced", Font.PLAIN, 14));
	texte.setEditable(false);
	JScrollPane scroll = new JScrollPane(texte, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	addWindowListener( new WindowAdapter() {
		public void windowOpened( WindowEvent e ){
                    entree.requestFocus();
		}
	    } );	
	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	this.setSize(700, 300);
	this.setTitle("Arbre a Palarbre");
	this.setLocation(100, 100);
	this.getContentPane().setLayout(new BorderLayout());
	this.getContentPane().add("Center", scroll);
	this.getContentPane().add("South", entree);
	this.setVisible(true);	
    }

    public void run()
    {
       	if(reader)
	    read();
    }
    public void keyPressed(KeyEvent e){
	if(e.getKeyCode() == 10)
	    {
		if(etat < 3)
		    {
			commande();
		    }
		else
		    {
			String s = entree.getText();
			historique.add(s);
			index_historique++;
			write("MESSAGE " + pseudo + " " + s);
		    }
		entree.setText("");
	    }
	// up history
	if(e.getKeyCode() == 38)
	    {
		entree.setText(historique.get(index_historique++));
	    }
	// down history
	if(e.getKeyCode() == 40  )
	    {
		System.out.println("down : "+ index_historique + " " + historique.get(-1+index_historique));
		entree.setText(historique.get(--index_historique));
	    }
    }
    
    public void keyReleased(KeyEvent e){
    }
    
    public void keyTyped(KeyEvent e){
    }

    public void display()
    {
	String toDisplay = "";
        for (String ligne : all_historique)
	    {
		toDisplay += ligne + "\n";
	    }
        texte.setText(toDisplay);
        texte.setCaretPosition(texte.getText().length());	
    }
    
    public void write(String txt)
    {
	try {
            DatagramSocket ms = new DatagramSocket();
            InetAddress ia = inet;
	    DatagramPacket dp = new DatagramPacket(txt.getBytes(),txt.getBytes().length,ia,PORT);
	    ms.send(dp);
	} catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void read()
    {
        try{
            byte [] data = new byte[256];
            InetAddress ia = inet;
            MulticastSocket ms = new MulticastSocket(PORT);
            ms.joinGroup(ia);
            DatagramPacket dp = new DatagramPacket(data, data.length);
            while (true) {
                ms.receive(dp);
                String recupere = new String(dp.getData(),0,dp.getLength());
		String [] reponse = recupere.split(" ");
		String s ="";
		if(reponse[0].equals("MESSAGE")){
		    s = reponse[1] + ": ";
		    for(int i = 2; i < reponse.length; i++)
			s+= reponse[i]+" ";
		}
		else if(reponse[0].equals("HELLO")){
		    s = reponse[1] + " vient de se rejoindre le cafe";  
		}
		else if(reponse[0].equals("CLOSE"))
		    {
			s = "Cafe ferme";
			ms.close();
		    }
		affiche(s);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void affiche(String s)
    {
	all_historique.add(s);
	display();
    }

    public String parse(BufferedReader in)
    {
	String retour = "";
	try{
	    int x = in.read();
	    while(x != 33)
		{
		    retour += (char) x; 
		    x = in.read();
		}
	}catch(IOException e){
	    
	}
	return retour;
    }

    public String parse_byte(byte [] in)
    {
	String retour = "";
	int i = 1;
	int x = (int)in[0];
	while(i < in.length && x != 33)
	    {
		System.out.println(x);
		retour += (char) x; 
		x = in[i];
		i++;
	    }
	return retour;
    }

    
    public void ville(String[] c)
    {
	try{
	    PORT = Integer.parseInt(c[2]);
	    inet = InetAddress.getByName(c[1]);
	    socket = new Socket(inet, PORT);
	    affiche("connexion à " + c[1] +"("+c[2]+")");
	    etat = 1;
	    shoplist();
	}
	catch(IOException e){affiche("Impossible de se connecter");}
	catch(NumberFormatException e){affiche("Impossible de se connecter");}
    }

    // commmande shoplist
    public void shoplist()
    {
	try{
	    out = new PrintWriter(socket.getOutputStream());
	    in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
	    out.println("SHOPLIST!");
	    out.flush();

	    String[] liste_cafe = parse(in).split(",");
	    String[] first_cafe = liste_cafe[0].split(" ");
	    if(!first_cafe[0].equals("200"))
		{
		    all_historique.add("SHOPLIST: echec de la requete");
		    display();
		    return;
		}
	    cafe_nom.add(first_cafe[first_cafe.length-1]);
	    shopinfo(cafe_nom.get(0));
	    String cafe =  "Liste des cafés:\n\t- " + cafe_nom.get(0) 
		+": "+ cafe_ip.get(0) 
		+" ("+ cafe_port.get(0) 
		+")\n";

	    for(int i = 1 ; i < liste_cafe.length; i++)
		{
		    cafe_nom.add(liste_cafe[i]);
		    shopinfo(liste_cafe[i]);
		    cafe += "\t- "+ cafe_nom.get(i) +":"+ cafe_ip.get(i)+ "("+ cafe_port.get(i) +")\n";
		}
	    affiche(cafe);

	}
	catch (UnknownHostException e) {
	    System.err.println("Serveur inconnu");
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	catch(NumberFormatException e){
	    System.err.println("Le port doit etre un entier naturel");
	}
    }
    
    public void shopinfo(String cafe)
    {
	try{
	    out = new PrintWriter(socket.getOutputStream());
	    out.println("SHOPINFO " +cafe + "!");
	    out.flush();
	    in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
	    String[] reponse = parse(in).split(" ");
	    if(!reponse[0].equals("200"))
		{
		    all_historique.add("SHOPINFO: echec de la requete");
		    display();
		    return ;
		}
	    String[] info = reponse[2].split(",");
	    cafe_ip.add(info[1]);
	    cafe_port.add(Integer.parseInt(info[2]));
	}
	catch (UnknownHostException e) {
	    System.err.println("Serveur inconnu");
	}
	catch (IOException e) {
	    e.printStackTrace();
	}
	catch(NumberFormatException e){
	    System.err.println("Le port doit etre un entier naturel");
	}
    }
    
    public int cafe_in_liste(String s)
    {
	for(int i = 0 ; i < cafe_nom.size(); i++)
	    if(cafe_nom.get(i).equals(s))
		return i;
	return -1;
    }

    public void cafe(String cafe)
    {
	int id = cafe_in_liste(cafe);
	if(id < 0)
	    affiche("Le cafe n'existe pas dans la ville");
	else
	    {
		try{
		    inet = InetAddress.getByName(cafe_ip.get(id)); 
		    PORT = cafe_port.get(id);
		    etat = 2;
		    affiche("Connexion cafe... presentez vous : HELLO pseudo [, ip] [, message]");
		}
		catch(UnknownHostException e){
		    affiche("Erreur inconnu");
		}
	    }
    }
    
    public void hello(String pseudo, String ip,String message)
    {
	    port_client = 11111;
	    etat = 3;
	    this.pseudo = pseudo;
	    try 
		{
		    byte[] news = new byte[1024];
		    String s = "HELLO " + pseudo + "," + ip + "," + port_client + "," + message +"!";
		    // datagram hello
		    DatagramSocket hello_socket = new DatagramSocket();
		    DatagramPacket hello_paquet = new DatagramPacket(s.getBytes(),s.getBytes().length,inet, PORT);
		    hello_socket.setSoTimeout(1500);
		    hello_socket.send(hello_paquet);
		    hello_socket.close();
		    //datagram news
		    DatagramSocket new_socket = new DatagramSocket(port_client);
		    DatagramPacket new_paquet = new DatagramPacket(news, news.length);
		    while(true)
			{
			    try{
				new_socket.receive(new_paquet);
				String recu_new = new String(new_paquet.getData(), "UTF-8");
				//String recu_new = new String(new_paquet.getData(),0, new_paquet.getLength());
				System.out.println(recu_new);
				affiche(recu_new);
			    }
			    catch(SocketTimeoutException e)
				{
				    String recu_new = new String(new_paquet.getData(), "UTF-8");
				    //String recu_new = parse_byte(new_paquet.getData());
				    System.out.println(recu_new + " *");
				    affiche(recu_new);
				    
				    return ;
				}
			}
		}
	    catch(Exception e) 
		{
		    e.printStackTrace();
		}
	    
    }
    
    public void commande()
    {
	String s = entree.getText(); // in
	String[] c = s.split(" ");
	if(c[0].equals("VILLE") && c.length == 3)
	    {
		ville(c);
	    }
	else if(c[0].equals("SHOPLIST"))
	    {
		if(etat == 1)
		    {
			String cafe = "Liste des cafés\n\t";
			for(int i = 0 ; i < cafe_nom.size(); i++)
			    {
				cafe += "-" + cafe_nom.get(i) + ":" + cafe_ip.get(i) + "(" + cafe_port.get(i) + ")\n\t";
			    }
			affiche(cafe);
		    }
		else
		    {
			all_historique.add("Pas de connexion");
			display();
		    }
	    }
	else if(c[0].equals("CAFE") && c.length == 2)
	    {
		if(etat == 1)
		    {
			cafe(c[1]);
		    }
		else
		    {
			all_historique.add("Pas de connexion");
			display();
		    }

	    }
 	else if(c[0].equals("HELLO") && ( c.length > 1 && c.length < 5))
	    {
		if(etat == 2)
		    {
			hello(c[1], (c.length > 2) ? c[2] : "127.0.0.1", (c.length == 4) ? c[3] : "Bonjour");
			recu = new Thread(new Client(inet, PORT, 0));
			recu.start();
		    }
		else
		    {
			all_historique.add("Vous devez d'abord choisir un cafe");
			display();
		    }
	    }
	else{
	    affiche("Mauvaise commande");
	}
	   
    }
    
    public static void main(String args[])
    {
	try{
	    Thread send = new Thread(new Client());  
	    send.start();
	}
	catch(Exception e)
	    {
	    }
    }
}

class Port extends Thread {
    protected int _serverPort = 0;
    
    public Port(int serverPort) {
	_serverPort = serverPort;
    }
    
    public void run() {
	try {
	    Socket socket = new Socket("localhost", _serverPort);
	    Client.port_client = _serverPort;
	} catch (Exception e) {
	}
    }
}