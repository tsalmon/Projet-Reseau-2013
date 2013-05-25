
typedef struct{
   int use;
   int alive;
   char name[20];
   unsigned short   port;
   struct sockaddr_in addrShop;
   struct in_addr addrMultiCast;   
} shop;

int citySize;
shop *listShop[];

int freePlace();
int isFreeName(char*);
void closeShop(struct in_addr);
int isAlive();
void alwaysAlive(struct in_addr);
void broadcast(char*);
