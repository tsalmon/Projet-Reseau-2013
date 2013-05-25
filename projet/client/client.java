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

public class client extends JFrame implements KeyListener, Runnable
{
    static client recepteur = new client(null, 1);
    static Thread recu = new Thread(recepteur);
    
    private InetAddress inet = null;
    private int PORT;
    private static JTextField entree = new JTextField();
    private static JTextArea texte = new JTextArea();
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
    client(InetAddress inet, int PORT)
    {
	reader = true;
	this.inet = inet;
	this.PORT = PORT;
    }
    
    client()
    {
	/*
	try{
	    inet = InetAddress.getByName("127.1.2.4");
	}catch(Exception e){
	    e.printStackTrace();
	}
	*/

	entree.addKeyListener(this);
	texte.setFont(new Font("Monospaced", Font.PLAIN, 14));
	texte.setEditable(false);
	JScrollPane scroll = new JScrollPane(texte, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	
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
		if(etat < 2)
		    {
			commande();
		    }
		else
		    {
			System.out.println("keyPressed etat != 0");
			String s = entree.getText();
			historique.add(s);
			index_historique++;
			write(s);
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
                String s = new String(dp.getData(),0,dp.getLength());
		/*System.out.println("Received "+s);*/
		all_historique.add(s);
		display();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void connexion_ville(String[] c)
    {
	try{
	    PORT = Integer.parseInt(c[2]);
	    inet = InetAddress.getByName(c[1]);
	    socket = new Socket(inet, PORT);
	    //out = new PrintWriter(socket.getOutputStream());
	    /*
	    out.println("SHOPLIST");
	    out.flush();
	    */
	    //in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    /*
	      String message = in.readLine();
	      System.out.println(message);
	      all_historique.add(message);
	      display();
	    */
	    all_historique.add("connexion à " + c[1] +"("+c[2]+")");
	    display();
	    etat = 1;
	}
	catch(IOException e){}
	//catch(UnknownHostException e){}
	catch(NumberFormatException e){}
    }

    // commmande shoplist
    public void shoplist()
    {
	try{
	    out = new PrintWriter(socket.getOutputStream());
	    out.println("SHOPLIST");
	    out.flush();
	    in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
	    String s = in.readLine();
	    String[] liste_cafe = s.split(",");
	    String[] first_cafe = liste_cafe[0].split(" ");
	    
	    if(!first_cafe[0].equals("200"))
		{
		    all_historique.add("SHOPLIST: echec de la requete");
		    display();
		    return ;
		}
	    String cafe = "Liste des cafés:\n\t- " + first_cafe[first_cafe.length - 1] + "\n";
	    for(int i = 1 ; i < liste_cafe.length; i++)
		{
		    cafe += "\t- " + liste_cafe[i]+ "\n";
		}
	    all_historique.add(cafe);
	    display();
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
	    out.println("SHOPINFO " +cafe);
	    out.flush();
	    in = new BufferedReader (new InputStreamReader (socket.getInputStream()));
	    String[] reponse = in.readLine().split(" ");
	    if(!reponse[0].equals("200"))
		{
		    all_historique.add("SHOPINFO: echec de la requete");
		    display();
		    return ;
		}
	    String[] info = reponse[2].split(",");
	    all_historique.add("SHOPINFO "+cafe +":\n\tadresse: " + info[1] + "\n\tPort connexion:" + info[2]);
	    display();
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

    public void hello(String pseudo, String ip, String port, String message)
    {
	System.out.println(pseudo +" " + ip + " " + port + " " + message);
	try{
	    InetAddress add = InetAddress.getByName(ip); 
	    recepteur.inet = add;
	    recepteur.PORT = Integer.parseInt(port);
	    recu = new Thread(recepteur);
	    recu.run();
	    System.out.println("hello fin");
	}
	catch(UnknownHostException e){
	    all_historique.add("HELLO: impossible de lancer la connexion");
	    display();
	}
    }
    
    /*
      if(s.length() == 0 || s.charAt(0) != '!')
      {
      return;
      }
    */    
    public void commande()
    {
	String s = entree.getText(); // in
	String[] c = s.split(" ");
	if(c[0].equals("VILLE") && c.length == 3)
	    {
		connexion_ville(c);
	    }
	else if(c[0].equals("SHOPLIST"))
	    {
		if(etat == 1)
		    {
			shoplist();
		    }
		else
		    {
			all_historique.add("Pas de connexion");
			display();
		    }
	    }
	else if(c[0].equals("SHOPINFO") && c.length == 2)
	    {
		if(etat == 1)
		    {
			shopinfo(c[1]);
		    }
		else
		    {
			all_historique.add("Pas de connexion");
			display();
		    }
	    }
 	else if(c[0].equals("HELLO") && ( c.length == 4 || c.length == 5))
	    {
		if(etat == 1)
		    {
			hello(c[1], c[2], c[3], (c.length == 5) ? c[4] : "");
		    }
		else
		    {
			all_historique.add("Pas de connexion");
			display();
		    }
	    }
	else{
	    all_historique.add("Les commandes doivent commencer par un '!'");
	    display();
	}
	   
    }
    
    public static void main(String args[])
    {
	try{
	    Thread send = new Thread(new client());  
	    send.run();
	}
	catch(Exception e)
	    {
	    }
    }
}
