

/*
 * Simple multicaster
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
  char *msg=argv[1];
  
  if ((s=socket(AF_INET,SOCK_DGRAM,0)) < 0) {
    perror("socket");
    exit(EXIT_FAILURE);
  }
  
  bzero(&addr,sizeof(addr));
  addr.sin_family      = AF_INET;
  addr.sin_addr.s_addr = inet_addr("225.1.2.4");
  addr.sin_port        = htons(PORT);
  
  if (sendto(s,msg,strlen(msg),0,(struct sockaddr *)&addr,sizeof(addr)) < 0) {
    perror("sendto");
    close(s);
    exit(EXIT_FAILURE);
  }
  close(s);
  exit(EXIT_SUCCESS);
}


