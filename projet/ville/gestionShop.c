#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include "gestionShop.h"

int freePlace(){
   int i;
   for(i=0;i<citySize;i++){
	if(!listShop[i]->use) return i;
   }
	return -1;
}


int shopName(char *name){
    int i;
   for(i=0;i<citySize;i++){
        printf("le nom a tester est |%s|, et le shop est |%s|\n",name,listShop[i]->name);
	if(listShop[i]->use && strcmp(listShop[i]->name,name)==0) return i;
   }
	return -1;
}


void closeShop(struct in_addr from){
   int i;
   for(i=0;i<citySize;i++){
	if(listShop[i]->addrShop.s_addr==from.s_addr) listShop[i]->use=0;
   }

}

int isAlive(){
  return 1;
  struct sockaddr_in addr;
  int sock;
  char tampon[256];
  strcpy(tampon,"ALIVE");
  sock = socket(PF_INET,SOCK_DGRAM,0); // Protocol family
  if (sock==-1) {
     return -1;
  }
  addr.sin_family = AF_INET; // Address family
  int i;
  for(i=0;i<citySize;i++){
     if(listShop[i]->use){
        listShop[i]->alive=0;
        addr.sin_port = htons(listShop[i]->port);
        addr.sin_addr = listShop[i]->addrShop;
        sendto(sock,tampon,256,0,(struct sockaddr *)(&addr),sizeof(addr));
      }  
   }

  sleep(1);
  for(i=0;i<citySize;i++){
     if(!listShop[i]->alive) listShop[i]->use=0;
  }
  return 1;
}

void alwaysAlive(struct in_addr from){
   int i;
   for(i=0;i<citySize;i++){
	if(listShop[i]->addrShop.s_addr==from.s_addr) listShop[i]->alive=1;
   }
}

