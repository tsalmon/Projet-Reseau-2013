#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <pthread.h>
#include "shop.h";
#include <errno.h>
#include <time.h>;


void *udp(void *arg)
{
   printf("creation de la multidiffusion \n");

  struct sockaddr_in addr;
  struct ip_mreq mreq;
  char tampon[256];
  char *pSurTampon;
  int ok=1,r;

  if ((sockDiffusion=socket(AF_INET,SOCK_DGRAM,0)) < 0) { 
    perror("socket");
    exit(EXIT_FAILURE);
  }

  if (setsockopt(sockDiffusion,SOL_SOCKET,SO_REUSEADDR,&ok,sizeof(ok)) < 0) {
    perror("Reusing ADDR failed");
    exit(EXIT_FAILURE);
  }

  mreq.imr_multiaddr.s_addr = inet_addr(ipDiffusion);
  mreq.imr_interface.s_addr = htonl(INADDR_ANY);
  if (setsockopt(sockDiffusion,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq)) < 0) { 
    perror("Subscribing to group failed");
    close(sockDiffusion);
    exit(EXIT_FAILURE);
  }

  addrDiffusion.sin_family      = AF_INET;
  addrDiffusion.sin_addr.s_addr = inet_addr(ipDiffusion);
  addrDiffusion.sin_port        = htons(PORT);
 
  addr.sin_family      = AF_INET;
  addr.sin_addr.s_addr = INADDR_ANY;
  addr.sin_port        = htons(PORT);


   if (bind(sockDiffusion,(struct sockaddr *)&addr,sizeof(addr)) < 0) {
    perror("bind");
    close(sockDiffusion);
    exit(EXIT_FAILURE);
   }  

   printf("en attente de message\n");

   while (1) {
      bzero(tampon,256);
      // Get a message
      if ( r = recv(sockDiffusion,tampon,256,0)==-1) {
        perror("recv");
        close(sockDiffusion);
        exit(EXIT_FAILURE);
      }

      printf("msg: %s\n",tampon);

      pSurTampon = strtok( tampon, "!" );
      pSurTampon = strtok( pSurTampon, " " );
     
      if(!(strcmp(pSurTampon, "HELLO"))){
          printf("message HELLO recu\n");
          char *nom; 
          char *ip;
          char *port;
          struct sockaddr_in addr;
          int sock;
                  
          nom = strtok( NULL , "," );
          ip = strtok( NULL , "," );
          port = strtok( NULL , " " ); 
         
          printf("les info recuperer son nom:%s ,ip:%s , port:%s\n",nom,ip,port);

          sock = socket(PF_INET,SOCK_DGRAM,0); // Protocol family
	  if (sock==-1) {
	    perror("socket: ");
	    exit(1);
	  }
	  addr.sin_family = AF_INET; // Address family
	  addr.sin_port = htons(atoi(port));
<<<<<<< HEAD
	  inet_aton(ip, &(addr.sin_addr));
	  //addr.sin_addr.s_addr = htonl(ip);
	  
=======
          inet_aton(ip,&(addr.sin_addr));
    
>>>>>>> c6d9d8d7d42fa9e17d81c03e57b772fc2ff434c1
          sprintf(tampon,"100 NEWS 12-56-4555 bienvenu sur le forum!");
          printf("le message renvoyer %s\n",tampon);

	  if (sendto(sock,tampon,256,0,(struct sockaddr *)(&addr),sizeof(addr))==-1) {
	    perror("sendto:");
	    close(sock);
	    exit(1);
	  }
	  close(sock);


      }


  }




    pthread_exit(NULL);
}






int main(int argc,char *argv[]) {

    int optch;  
    extern int opterr;  
  
    char format[] = "v:p:";  
  
    opterr = 1;  

    while ((optch = getopt(argc, argv, format)) != -1)  
    switch (optch) {  
            break;  
        case 'v':    
            break;  
        case 'p':   
            break;  

    } 


    NOM=argv[optind];
    PORT=atoi(argv[optind+1]); 
    ipCity=argv[optind+2];
    portCity=atoi(argv[optind+3]);




  struct sockaddr_in addrto,addrfrom;
  int sock;
  char tampon[256];
  char *pSurTampon;

  sock = socket(PF_INET,SOCK_DGRAM,0); // Protocol family
    if (sock==-1) {
      perror("socket: ");
      exit(1);
  }

  addrfrom.sin_family = AF_INET; 
  addrfrom.sin_addr.s_addr= htonl(INADDR_ANY);
  addrfrom.sin_port = htons(0);


  if (bind(sock,(struct sockaddr *)(&addrfrom),sizeof(addrfrom))==-1) {
    perror("bind: ");
    close(sock);
    exit(1);
  }

  addrto.sin_family = AF_INET; 
  inet_aton(ipCity,&(addrto.sin_addr));
  addrto.sin_port = htons(portCity);

  sprintf(tampon,"NEWSHOP %s,%d!",NOM,PORT);  

   if (sendto(sock,tampon,256,0,(struct sockaddr *)(&addrto),sizeof(addrto))==-1) {
    perror("sendto: ");
    close(sock);
    exit(1);
  }

  if (recv(sock,tampon,256,0)==-1) {
    perror("recv:");
    close(sock);
    exit(1);
  }
  printf("reponse de la ville : %s\n",tampon);

  pSurTampon = strtok( tampon, "!" );
  pSurTampon = strtok( pSurTampon, " " );

  if(!(strcmp(pSurTampon, "200"))){
     pSurTampon = strtok( NULL, " " );
     ipDiffusion = strtok( NULL, " " );
  }
  else  if(!(strcmp(pSurTampon, "404"))){
     fprintf(stderr,"nom deja pris");
     close(sock);
     exit(EXIT_FAILURE);
  }
  else  if(!(strcmp(pSurTampon, "500"))){
     fprintf(stderr,"la ville est pleine ressaye plutard");
     close(sock);
     exit(EXIT_FAILURE);
  }
  else  if(!(strcmp(pSurTampon, "501"))){ 
     fprintf(stderr,"erreur Syntax");
     close(sock);
     exit(EXIT_FAILURE);
  }
  else {
     fprintf(stderr,"retour de la ville inconu");
     close(sock);
     exit(EXIT_FAILURE);
  }

 printf("la ville a accepte \n");


  pthread_t threadUdp;


   if (pthread_create(&threadUdp, NULL, udp, NULL)) {
      perror("pthread_create");
      return EXIT_FAILURE;
   }

  while(1){
     recv(sock,tampon,256,0);
     printf("Recu de la ville : %s\n",tampon);
     pSurTampon = strtok( tampon, "!" );
     pSurTampon = strtok( pSurTampon, " " );

     if(!(strcmp(pSurTampon, "ALIVE"))){
        sprintf(tampon,"200 ALIVE");  
        sendto(sock,tampon,256,0,(struct sockaddr *)(&addrto),sizeof(addrto));
     }
     else if(!(strcmp(pSurTampon, "BROADCAST"))){
        sprintf(tampon,"NEWS %s!",strtok ( NULL , ""));
        sendto(sockDiffusion,tampon,256,0,(struct sockaddr *)(&addrDiffusion),sizeof(addrDiffusion));
     }
  }


return 1;
} 






