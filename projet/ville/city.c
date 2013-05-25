/*
 * Simple UDP receiver
 * (c)2013 JBY
 */
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include "gestionShop.h"
#include "city.h";
#include <errno.h>


void *tcpAccept(void *data){
  printf("******************* une nouvelle connection a moi ****************");
  int sock;
  sock=*((int *)data);
  printf("sock: %d \n",sock);

  char *pSurTampon;
  char tampon[512];
  char tamponReponse[512];

  while (read(sock,tampon,512)>0) {
     printf("%s\n",tampon);
     pSurTampon = strtok( tampon, "!" );
     pSurTampon = strtok( pSurTampon, " " );
     printf("la commande est |%s|\n",pSurTampon);
     

     if(!(strcmp(pSurTampon, "INSERT"))){
        if(autonomous){
          // si la ville est autonome
          strcpy(tamponReponse,"400 INSERT!");
          write(sock,tamponReponse,512);
        }
        else{
	   // si la ville accept les insert
           // on renvois le message d'acceptation
           // on ferme le canal d'emission 
           strcpy(tamponReponse,"200 INSERT!");
           write(sock,tamponReponse,512);
          // shutdown(sock,1);

           if(haveNextCity){
               // si on fait deja partie d'un anneaux
               // on fait passé le message pour la substitution
               // on ne s'occupe pas de fermé la connexion avec la ville qui nous precedait
               previousCity->nom=strtok( NULL , "," );    
               previousCity->ip=strtok( NULL , "," );
               previousCity->port=atoi(strtok( NULL , "\n" ));

	       sprintf(tamponReponse, "SUBSTITUTE %s,%s,%d FOR %s,%s,%d 20!",previousCity->nom,previousCity->ip,previousCity->port,me->nom,me->ip,me->port);
               write(nextCitySock,tamponReponse,512);
               printf("%s\n",tamponReponse);
           }
           else{
              // si nous somme la premiere ville de l'anneaux 
              // on crée une nouvelle socket pour la nextCity
              // on ferme le cannal de reception de cette derniere
              
              struct sockaddr_in a;
              int s;

              nextCity->nom=strtok( NULL , "," );    
              previousCity->nom=nextCity->nom;
              nextCity->ip=strtok( NULL , "," );
              previousCity->ip=nextCity->ip;
              nextCity->port=atoi(strtok( NULL , "\n" ));
              previousCity->port=nextCity->port;

              printf("avan la connection %s,%s,%d \n",nextCity->nom,nextCity->ip,nextCity->port);
 
              a.sin_family = AF_INET;
              inet_aton(nextCity->ip,&(a.sin_addr));
              a.sin_port = htons(nextCity->port);
              
              s = socket(PF_INET,SOCK_STREAM,0);
              if (s==-1) {
                 fprintf(stderr,"socket problem\n");
                 exit(EXIT_FAILURE);
              }

              if(connect( s,(struct sockaddr *)&a,sizeof(a)) ==-1){
             	 fprintf(stderr,"connection problem\n");
                 exit(EXIT_FAILURE);
              }
              //shutdown(s,0);

              nextCitySock=s;
	      haveNextCity=1;
           }
        }
     }

    else if(!(strcmp(pSurTampon, "SUBSTITUTE"))){
        printf("dans le sub\n");
        char *final, *initial;
        int ttl;
        final = strtok( NULL, " " );
        strtok( NULL, " " );
        initial = strtok( NULL, " " );
        ttl = atoi(strtok( NULL, " " ));
        printf("substitue %s for %s %d\n", final,initial,ttl);
        
        char *nom, *ip;
        int port;

        nom=strtok( initial, "," );
        ip=strtok( NULL, "," );
        port=atoi(strtok( NULL, "," ));
           
       printf("nextnom %s , nom %s  ======= nextip %s  , ip %s ===== nextport %d , port %d\n",nextCity->nom,nom,nextCity->ip,ip,nextCity->port,port);     


        if(strcmp(nextCity->nom,nom)==0 && strcmp(nextCity->ip,ip)==0 && nextCity->port==port){
           close(nextCitySock);
;
           struct sockaddr_in a;
           int s;

           nom=strtok( final, "," );
           ip=strtok( NULL, "," );
           port=atoi(strtok( NULL, "," ));

           nextCity->nom=nom;
           nextCity->ip=ip;
           nextCity->port=port;
 
           printf(" //////////////////ma nouvelle ville suivante est %s,  ,%s,  ,%d \n ",nextCity->nom,nextCity->ip,nextCity->port);   

           a.sin_family = AF_INET;
           inet_aton(nextCity->ip,&(a.sin_addr));
           a.sin_port = htons(nextCity->port);


           s = socket(PF_INET,SOCK_STREAM,0);
           if (s==-1) {
              fprintf(stderr,"socket problem\n");
              exit(EXIT_FAILURE);
           }


           if(connect( s,(struct sockaddr *)&a,sizeof(a)) ==-1){
              fprintf(stderr,"connection problem\n");
              exit(EXIT_FAILURE);
           }

           shutdown(s,0);

           nextCitySock=s;
     
        }
        else if(ttl>0){
           sprintf(tamponReponse, "SUBSTITUTE %s FOR %s,%s,%d %d!", final,nom,ip,port,(ttl-1));
           write(nextCitySock,tamponReponse,512);
        }
    }
    else if(!(strcmp(pSurTampon, "CAST"))){ 
         //si on a un message a transmettre
         pSurTampon = strtok( NULL, " " );
         if(!(strcmp(pSurTampon, "GLOBAL"))){  
             //si ce messages est gloabl
              pSurTampon = strtok( NULL, " " );
              if(!(strcmp(pSurTampon, "FROM"))){
              // si il vien deja de quelqu'un
                 char *nom, *message;
                 int ttl;
                 nom = strtok( NULL, " " );
                 ttl = atoi(strtok( NULL, " " ));
                 message = strtok( NULL, "" );
 
                 printf("recu message global de %s , ttl a %d, le message est '%s'\n",nom,ttl,message);

                 if(strcmp(nextCity->nom,nom)==0 && haveNextCity){
                    printf("le prochaine est l'emteur je stop\n");
                 }
                 else if(haveNextCity && ttl>0){
		   printf("j'envoit le message au prochain");
                   sprintf(tamponReponse,"CAST GLOBAL FROM %s %d %s!",nom,(ttl-1),message);	
                   write(nextCitySock,tamponReponse,512);						
                 }
           
              }
              else{
              //si on est le premier a le recevoir
                 char *message;
                 message  = strtok( NULL, "" );
                 printf("le message est %s %s\n",pSurTampon,message);
                 if(haveNextCity){
                    sprintf(tamponReponse,"CAST GLOBAL FROM %s %d %s %s!",me->nom,20,pSurTampon,message);
                    write(nextCitySock,tamponReponse,512);
                 }
              }
         }
         else if(!(strcmp(pSurTampon, "LOCAL"))){   
            // si ce message est local
            pSurTampon  = strtok( NULL, "" );
            printf("le message est %s\n",pSurTampon);
         }
         else{ 
           // sinon on a une erreur de syntax
           strcpy(tampon,"501 SYNTAX!");
           write(sock,tampon,512);
        }
    }
    else if(!(strcmp(pSurTampon, "REMOVE"))){
       strcpy(tamponReponse,"200 REMOVE!");
       write(sock,tamponReponse,512);
 
       char *nom, *ip;
       int port;
              
       nom = strtok( NULL, "," );
       ip = strtok( NULL, "," );
       port = atoi(strtok( NULL, "," ));

       sprintf(tamponReponse, "SUBSTITUTE %s,%s,%d FOR %s,%s,%d 20!", me->nom,me->ip,me->port,nom,ip,port);
       write(nextCitySock,tamponReponse,512);
         
    }
    else if(!(strcmp(pSurTampon, "SHOPLIST"))){
       int nb ,i; char list[200];
       bzero(list,200);     
       nb=0;
       for(i=0;i<citySize;i++){
          if(listShop[i]->use){
             if(nb++)sprintf(list,"%s,%s",list,listShop[i]->name);
             else sprintf(list,"%s",listShop[i]->name);
          }
       }
       sprintf(tamponReponse,"200 SHOPLIST %d %s\n!",nb,list);
       write(sock,tamponReponse,512);
    }
    else if(!(strcmp(pSurTampon, "SHOPINFO"))){
       char *nom;
       int indice;
       nom=strtok( NULL, "\n" );printf("le nom demande est %s",nom);
       if((indice=shopName(nom))!=-1){ 
          char rep[INET_ADDRSTRLEN];
          inet_ntop(AF_INET,&listShop[indice]->addrMultiCast,rep,INET_ADDRSTRLEN);
          sprintf(tamponReponse,"200 SHOPINFO %s,%s,%d!",nom,rep,listShop[indice]->port);
          write(sock,tamponReponse,512);
       }
       else {      
          sprintf(tamponReponse,"403 SHOPINFO not found!");
          write(sock,tamponReponse,512);
       }
    }
    else {
       strcpy(tampon,"501 SYNTAX!");
       write(sock,tampon,512);
    }
  }
}


 

  void *tcp(void *arg)
{
    printf("Nous sommes dans le thread.\n");

	int s,l;
  struct sockaddr_in a;

  s = socket(PF_INET,SOCK_STREAM,0);
  if (s==-1) {
    fprintf(stderr,"socket problem\n");
    exit(EXIT_FAILURE);
  }
  bzero(&a,sizeof(a));
  a.sin_family = AF_INET;
  a.sin_port = htons(me->port);
  a.sin_addr.s_addr = htonl(INADDR_ANY);
  l=sizeof(a);
  if (bind(s,(struct sockaddr *)&a,l)==-1) {
    fprintf(stderr,"bind problem");
    close(s);
    exit(EXIT_FAILURE);    
  }
  if (listen(s,0)==-1) {
    fprintf(stderr,"listen problem");
    close(s);
    exit(EXIT_FAILURE);
  }
  int d, i;
  struct sockaddr_in c;
  do {
    printf("Ready to accept... Please call me!\n");
    if ((d=accept(s,(struct sockaddr *)&c,&l))==-1) {
      fprintf(stderr,"accept problem");
      close(s);
      exit(EXIT_FAILURE);
    }

     pthread_t threadTcpAccept;
     pthread_create(&threadTcpAccept, NULL,tcpAccept, &d);
     
    
  } while(1);
  close(s);
 

    (void) arg;
    pthread_exit(NULL);
}
 





void *udp(void *arg)
{
//////////initialisation////////////////////////////////////////////////////////
  struct sockaddr_in addr,from;
  socklen_t lg;
  int sock;
  char tampon[256];
  char *pSurTampon;

  sock = socket(PF_INET,SOCK_DGRAM,0); 
  if (sock==-1) {
    perror("socket: ");
    exit(1);
  }
  addr.sin_family = AF_INET; 
  addr.sin_port = htons(me->port);
  addr.sin_addr.s_addr = htonl(INADDR_ANY);
  if (bind(sock,(struct sockaddr *)(&addr),sizeof(addr))==-1) {
      perror("bind: ");
      close(sock);
      exit(1);
  }
  lg = sizeof(from);
   
//////////ecoute ///////////////////////////////////////////////////////////
 printf("Udp pret a ecouter\n");
  while (1) {
     int r;
     r = recvfrom(sock,tampon,256,0,(struct sockaddr *)(&from),&lg);
     if (r==-1) {
        perror("recv:");
        close(sock);
        exit(1);
     }
    printf("Recu : %s\n",tampon);
 	

    pSurTampon = strtok( tampon, "!" );
    pSurTampon = strtok( pSurTampon, " " );
    if(!(strcmp (pSurTampon, "NEWSHOP"))){
        char *name,*port;

        name = strtok( NULL, "," );
        port = strtok( NULL, "," );

	int place;
        if((place=freePlace())==-1){ 
	   strcpy(tampon,"500 NEWSHOP city full!");
           sendto(sock,tampon,256,0,(struct sockaddr *)(&from),lg);
        }
        else if(shopName(name)!=-1){ 
	   strcpy(tampon,"404 NEWSHOP existing name!");
           sendto(sock,tampon,256,0,(struct sockaddr *)(&from),lg);	
        }
        else if(!atoi(port)){
           strcpy(tampon,"501 SYNTAX!");
           sendto(sock,tampon,256,0,(struct sockaddr *)(&from),lg);	
        }
        else {
	   listShop[place]->use=1;
           strcpy(listShop[place]->name,name);
           listShop[place]->port=atoi(port);
           listShop[place]->addrShop=from.sin_addr;

           char rep[INET_ADDRSTRLEN];
           inet_ntop(AF_INET,&listShop[place]->addrMultiCast,rep,INET_ADDRSTRLEN);
           sprintf(tampon,"200 NEWSHOP %s!",rep);
	   sendto(sock,tampon,256,0,(struct sockaddr *)(&from),lg);
        }
    }
    else if(!(strcmp (pSurTampon, "CLOSE"))){
       closeShop(from.sin_addr);
    }
    else if(!(strcmp (pSurTampon, "200"))){
       pSurTampon = strtok( NULL, " " );
       if(!(strcmp (pSurTampon, "ALIVE"))){
          alwaysAlive(from.sin_addr);
       }
    }
    else {
       strcpy(tampon,"501 SYNTAX!");
       sendto(sock,tampon,256,0,(struct sockaddr *)(&from),lg);
    }

    printf("Renvoye : %s\n",tampon);
  }
  close(sock);

    (void) arg;
    pthread_exit(NULL);
}
 






int main(int argc,char *argv[]) {
  	
    char *fichierMultiDiffusion;
    fichierMultiDiffusion="ipMultiDiffusion.txt";

    me = (cityInfo*) malloc(sizeof(cityInfo));
    me->nom="";
    me->ip="127.0.0.1";
    me->port=5678;

    nextCity = (cityInfo*) malloc(sizeof(cityInfo));
    nextCity->nom="";
    nextCity->ip="localhost";
    nextCity->port=5678;

    previousCity = (cityInfo*) malloc(sizeof(cityInfo));
    previousCity->nom="";
    previousCity->ip="localhost";
    previousCity->port=5678;



    autonomous =0;
    haveNextCity=0;

    int optch;  
    extern int opterr;  
  
    char format[] = "i:p:d:an:";  
  
    opterr = 1;  

    while ((optch = getopt(argc, argv, format)) != -1)  
    switch (optch) {  
        case 'i':  
            me->ip=optarg;
            break;  
        case 'p':  
            me->port=atoi(optarg);  ; 
            break;  
        case 'd':  
            fichierMultiDiffusion=atoi(optarg);  
            break;  
        case 'a':  
            autonomous=1;
            break;  
        case 'n':
            printf("argument: %s\n",optarg);
            nextCity->nom= strtok(optarg, "," );printf("le nom est : %s\n",nextCity->nom);
            nextCity->ip = strtok( NULL, "," );
            nextCity->port = atoi(strtok( NULL, "," ));
            haveNextCity=1;
            break;  
    } 
 
  me->nom=argv[optind];

  if(!(citySize=atoi(argv[optind+1]))){
      fprintf(stderr,"taille de la ville\n");
      exit(EXIT_FAILURE);
  } 
   printf("la taille est %d \nl'ip est %s\nle port est %d\nla ville est autonome:%d\n ",citySize,me->ip,me->port,autonomous);
  listShop[citySize];


  FILE* fichier = NULL;
  fichier = fopen(fichierMultiDiffusion, "r+");
  if (fichier == NULL){  
      fprintf(stderr,"impossible d'ouvrir le fihcier\n");
      exit(EXIT_FAILURE);
  }   

    char chaine[20];int i;
    for(i=0;i<citySize;i++){
       if(fgets(chaine, sizeof chaine, fichier)!=NULL){
           listShop[i]= (shop *) malloc(sizeof(shop));
          printf("l'ip est : %s", chaine);
          listShop[i]->addrMultiCast.s_addr=inet_addr(chaine);
          strcpy(listShop[i]->name," ");
       }
       else {
          fprintf(stderr,"le fichier ne contient pas asse d'adresse de diffusion\n");
          exit(EXIT_FAILURE);
       }
    }


   



















































  pthread_t threadTcp;
  pthread_t threadUdp;
 
    printf("Avant la création du thread.\n");
 
    if (pthread_create(&threadTcp, NULL, tcp, NULL)) {
    perror("pthread_create");
    return EXIT_FAILURE;
    }

    if (pthread_create(&threadUdp, NULL, udp, NULL)) {
    perror("pthread_create");
    return EXIT_FAILURE;
    }
 
 







if(haveNextCity){
      printf("le nom est:%s , l'ip est:%s , le port est:%d\n",nextCity->nom,nextCity->ip,nextCity->port);
       
       struct sockaddr_in a;
       int s;
       char tampon[512];

       a.sin_family = AF_INET;
       inet_aton(nextCity->ip,&(a.sin_addr));
       a.sin_port = htons(nextCity->port);
              
       s = socket(PF_INET,SOCK_STREAM,0);
       if (s==-1) {
       fprintf(stderr,"socket problem\n");
          exit(EXIT_FAILURE);
       }


       if(connect( s,(struct sockaddr *)&a,sizeof(a)) ==-1){
           fprintf(stderr,"connection problem\n");
           exit(EXIT_FAILURE);
       }

       sprintf(tampon, "INSERT %s,%s,%d",me->nom,me->ip,me->port);
       write(s,tampon,512);
       read(s,tampon,512);
       printf("la reponse %s\n",tampon);
       
        
       autonomous=0;
       nextCitySock=s;
       printf("reussi la socket est %d\n",nextCitySock);
	while (read(s,tampon,512)>0) {
	}
   }








    if (pthread_join(threadTcp, NULL)) {
    perror("pthread_join");
    return EXIT_FAILURE;
    }
 
    printf("Après la création du thread.\n");









    return EXIT_SUCCESS;



}

