

/*
 * Simple UDP sender
 * usage: client host message
 * (c)2013 JBY
 */
#include <sys/socket.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <netdb.h>
#include <strings.h>

#define PORT 5678

int main(int argc,char *argv[]) {
  struct sockaddr_in addr;
  int sock;
  char tampon[256];
  struct hostent *hent;

  if (argc<3) {
    fprintf(stderr,"usage: %s host message\n",argv[0]);
    exit(1);
  }
  hent = gethostbyname(argv[1]);
  if (hent==NULL) {
    fprintf(stderr,"%s: host %s unknown\n",argv[0],argv[1]);
    exit(1);
  }
  sock = socket(PF_INET,SOCK_DGRAM,0); // Protocol family
  if (sock==-1) {
    perror("socket: ");
    exit(1);
  }
  addr.sin_family = AF_INET; // Address family
  addr.sin_port = htons(PORT);
  memcpy(&(addr.sin_addr.s_addr),hent->h_addr_list[0],hent->h_length);
  strcpy(tampon,argv[2]);
  if (sendto(sock,tampon,256,0,(struct sockaddr *)(&addr),sizeof(addr))==-1) {
    perror("sendto:");
    close(sock);
    exit(1);
  }
  close(sock);
  return 0;
}
