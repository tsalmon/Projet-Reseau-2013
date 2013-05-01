

/*
 * Simple multicast receiver
 * (c)2013 JBY
 */
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <time.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#define PORT 5678

int main(int argc, char *argv[]) {
  struct sockaddr_in addr;
  int s;
  struct ip_mreq mreq;
  char msg[256];
  int ok=1;
  
  if ((s=socket(AF_INET,SOCK_DGRAM,0)) < 0) {
    perror("socket");
    exit(EXIT_FAILURE);
  }

  /* Authorize multiple bindings on the same port (on the same host!) */
  if (setsockopt(s,SOL_SOCKET,2,&ok,sizeof(ok)) < 0) {
    perror("Reusing ADDR failed");
    exit(EXIT_FAILURE);
  }

  bzero(&addr,sizeof(addr));

  addr.sin_family      = AF_INET;
  addr.sin_addr.s_addr = INADDR_ANY;
  addr.sin_port        = htons(PORT);

  if (bind(s,(struct sockaddr *)&addr,sizeof(addr)) < 0) {
    perror("bind");
    close(s);
    exit(EXIT_FAILURE);
  }

  /* Join a multicast group */
  mreq.imr_multiaddr.s_addr = inet_addr("225.1.2.4");
  mreq.imr_interface.s_addr = htonl(INADDR_ANY);
  if (setsockopt(s,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq)) < 0) {
    perror("Subscribing to group failed");
    close(s);
    exit(EXIT_FAILURE);
  }

  while (1) {
    bzero(msg,256);
    // Get a message
    int r = recv(s,msg,256,0);
    if (r==-1) {
      perror("recv");
      close(s);
      exit(EXIT_FAILURE);
    }
    // Print it
    msg[r] = '\0'; // EOS
    printf("%s\n",msg);
  }
  return EXIT_SUCCESS;
}


