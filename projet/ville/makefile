# /*DIALLO ALPHA ET        VERSION DU 24/05/2005*/
ville: gestionShop.o city.o 
	gcc   -g  -o ville gestionShop.o city.o -lpthread

gestionShop.o: gestionShop.c gestionShop.h
	gcc  -g  -c gestionShop.c


city.o: city.c city.h
	gcc  -g  -c city.c -lpthread

clean:
	rm -f *.o *.c~ *.h~ makefile~ ville *.dat~

