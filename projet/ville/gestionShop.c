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
	if(listShop[i]->addrShop.sin_addr.s_addr==from.s_addr) listShop[i]->use=0;
   }

}

int isAlive(){
  int sock;
  char tampon[256];
  sprintf(tampon,"ALIVE");
  sock = socket(PF_INET,SOCK_DGRAM,0); // Protocol family
  if (sock==-1) {
     return -1;
  }
  int i;
  for(i=0;i<citySize;i++){
     if(listShop[i]->use){
        listShop[i]->alive=0;
        sendto(sock,tampon,256,0,(struct sockaddr *)(&listShop[i]->addrShop),sizeof(listShop[i]->addrShop));
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
	if(listShop[i]->addrShop.sin_addr.s_addr==from.s_addr) listShop[i]->alive=1;
   }
}

void broadcast(char *message){
   int i,sock;
   char tampon[256];
   sprintf(tampon,"BROADCAST %s!",message);
   if(sock=socket(AF_INET,SOCK_DGRAM,0)){
     for(i=0;i<citySize;i++){
	if(listShop[i]->use) {
           sendto(sock,tampon,256,0,(struct sockaddr *)(&listShop[i]->addrShop),sizeof(listShop[i]->addrShop)); 
        }
      } 
    }
}

