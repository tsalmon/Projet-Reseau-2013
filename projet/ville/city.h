


typedef struct{
  char *nom;
  char *ip;
  short port;
} cityInfo;

typedef struct{
  int sock;
  char tampon[256];
  struct sockaddr_in from;
} infoUdp;

int autonomous;
cityInfo *me;

int haveNextCity;
int nextCitySock;
cityInfo *nextCity;

cityInfo *previousCity;


void insertAldermen(int,char[512],char*);

