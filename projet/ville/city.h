


typedef struct{
  char *nom;
  char *ip;
  short port;
} cityInfo;

int autonomous;
cityInfo *me;

int haveNextCity;
int nextCitySock;
cityInfo *nextCity;

cityInfo *previousCity;


void insertAldermen(int,char[512],char*);
