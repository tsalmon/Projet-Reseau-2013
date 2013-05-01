/*
 * Simple UDP receiver
 * (c)2013 JBY
 */
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>

#define PORT 5678

int main(int argc,char *argv[]) {
  struct sockaddr_in addr;
  int sock;
  char tampon[256];
struct ip_mreq mreq;
int ok=1;

  sock = socket(AF_INET,SOCK_DGRAM,0); // Protocol family
  if (sock==-1) {
    perror("socket: ");
    exit(1);
  }

//ajout 

/* Authorize multiple bindings on the same port (on the same host!) */
  if (setsockopt(sock,SOL_SOCKET,2,&ok,sizeof(ok)) < 0) {
    perror("Reusing ADDR failed");
    exit(EXIT_FAILURE);
  }

//fin ajout 

  addr.sin_family = AF_INET; // Address family
  addr.sin_port = htons(PORT);
  addr.sin_addr.s_addr = htonl(INADDR_ANY);



  if (bind(sock,(struct sockaddr *)(&addr),sizeof(addr))==-1) {
      perror("bind: ");
      close(sock);
      exit(1);
    }



//debut ajout
/* Join a multicast group */
  mreq.imr_multiaddr.s_addr = inet_addr("225.1.2.4");
  mreq.imr_interface.s_addr = htonl(INADDR_ANY);
  if (setsockopt(sock,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq)) < 0) {
    perror("Subscribing to group failed");
    close(sock);
    exit(EXIT_FAILURE);
  }

//fin ajout 
  while (1) {
    if (recv(sock,tampon,256,0)==-1) {
      perror("recv:");
      close(sock);
      exit(1);
    }
    printf("Recu : %s\n",tampon);
    if (!strcmp(tampon,"quit")) break;
  }
  close(sock);
  return 0;
}




