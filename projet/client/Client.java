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
    static int etat = 0;
    /*
      0: recherche de ville
      1: connexion a une ville
     */
    Client(InetAddress inet, int PORT, String pseudo)
    {
	reader = true;
	this.inet = inet;
	this.PORT = PORT;
	this.mode = 0;
	this.pseudo = pseudo;
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
		String s = entree.getText();
		if(etat < 3)
		    commande();
		else if(etat == 3)
		    {
			String[] c = s.split(" ");
			historique.add(s);
			index_historique++;
			if(c[0].equals("/TO") && c.length == 2)
			    mp(c[1]);
			else if(c[0].equals("/TO") && c.length == 1)
			    write("HEY " + pseudo + " TO " +  c[1]);
			else if(c[0].equals("/FROM" ) && c.length > 1 && c.length < 3)
			    {
				PORT = portlibre();
				write("HEY " + c[1] + " FROM " + pseudo + " 127.0.0.1 " + PORT);
				etat = 4;
			    }
			else if(c[0].equals("/LEFT"))
			    System.out.println("a finir");
			else
			    write("MESSAGE " + pseudo + " " + s);
		    }
		else if(etat == 4){
		    mp(s);
		}
		else if(s.equals("/LEFT"))
		    {
			System.out.println(" a finir");
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

    public void mp(String message)
    {
	try{
            ServerSocket socketAttente = new ServerSocket(PORT);
	    Socket service = socketAttente.accept();
	    
	    BufferedReader bf = new BufferedReader( new InputStreamReader(service.getInputStream()));

	    PrintWriter pw = new PrintWriter( new OutputStreamWriter(service.getOutputStream()));
	    pw.println("MESSAGE" + message);
	    
	    String reponse = bf.readLine();
	    affiche(reponse);
	    pw.close();
	    bf.close();
	    service.close();
	}
	catch(Exception e){
	    System.err.println("Erreur sérieuse : " + e);
	    e.printStackTrace();
	    System.exit(1);
	}
    }
    
    public void left()
    {
	affiche("partir");
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
	    DatagramPacket dp = new DatagramPacket(txt.getBytes(),txt.getBytes().length,inet,PORT);
	    ms.send(dp);
	} catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void read()
    {
        try{
	    boolean mp_mode = false;
            byte [] data = new byte[256];
            InetAddress ia = inet;
            MulticastSocket ms = new MulticastSocket(PORT);
            ms.joinGroup(ia);
            DatagramPacket dp = new DatagramPacket(data, data.length);
            while (!mp_mode) {
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
		else if(reponse[0].equals("HEY") || (reponse[0].equals("300") && (reponse[1].equals("HEY"))))
		    {
			if(reponse[2].equals("TO")){
			    if(!reponse[1].equals(pseudo) && reponse[3].equals(pseudo)){
				affiche("MP:" + reponse[1] + " souhaite discuter avec vous");
			    }
			    else{
				affiche("MP: invitation envoye a " + reponse[3] );
			    }
			}
			else{
			    if(!reponse[2].equals(pseudo) && reponse[4].equals(pseudo)){
				affiche("MP:" + reponse[1] + " a accepter de discuter avec vous");
			    }
			    else{
				affiche("Vous avez accepte de discuter avec " + reponse[2]);
			    }
			    mp_mode = true;
			    etat = 4;
			}
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
    
    public void affiche_news(String [] news)
    {
	String message_final = "";
	for(int i = 0 ; i < news.length; i++)
	    {
		String[] decoupe = news[i].split(" ");
		if(decoupe.length > 2)
		    message_final += decoupe[2] + ": " + decoupe[3] + "\n"; 
	    }
	affiche(message_final);
    }
    
    public int portlibre()
    {
	int i = 11111;
	boolean b = false;
	while(!b){
	    try{
		DatagramSocket new_socket = new DatagramSocket(i);
		new_socket.close();
		b = true;
	    }
	    catch(Exception e){
		//e.printStackTrace();
		//System.exit(0);
		i++;
	    }
	}
	return i;
    }
    
    public void hello(String pseudo, String ip,String message)
    {
	port_client = portlibre();
	etat = 3;
	this.pseudo = pseudo;
	String recu_new = "";
	try 
	    {
		byte[] news = new byte[1024];
		String s = "HELLO " + pseudo + "," + ip + "," + port_client + "," + message +"!";
		// datagram hello
		DatagramSocket hello_socket = new DatagramSocket();
		DatagramPacket hello_paquet = new DatagramPacket(s.getBytes(),s.getBytes().length,inet, PORT);
		//datagram news
		DatagramSocket new_socket = new DatagramSocket(port_client);
		DatagramPacket new_paquet = new DatagramPacket(news, news.length);
		new_socket.setSoTimeout(750);
		hello_socket.send(hello_paquet);
		while(true)
		    {
			try{
			    new_socket.receive(new_paquet);
			    recu_new += new String(new_paquet.getData(), "ASCII");
			}
			catch(SocketTimeoutException e)
			    {
				
				affiche_news(recu_new.split("!"));
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
			recu = new Thread(new Client(inet, PORT, pseudo));
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
