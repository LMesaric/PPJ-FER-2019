/**
 * ppjC je programski jezik podskup jezika C definiran u dokumentu
 * https://github.com/fer-ppj/ppj-labosi/raw/master/upute/ppj-labos-upute.pdf
 *
 * ova skripta poziva ppjC kompajler (za sada samo analizator) pritiskom
 * na tipku [Ctrl+S], [Shift+Enter] ili [Alt+3] i prikazuje rezultat analize.
 *
 * ne garantiram tocnost leksera, sintaksnog niti semantickog analizatora koji
 * se ovdje pokrece.
 *
 * URL skripte prati verzije izvornog programa, tako da je moguca razmjena
 * izvornih programa u timu putem URL-ova.
 */
 
int printf(const char format[]) {
  /* i wish i could printf */
  return 0;
}

int main(void) {
  int broj, zadnjaZnamenka;
printf("Upisite jedan pozitivan cijeli broj: ");
scanf("%d", &broj);
if (broj > 0) {
/* u sljedecoj naredbi uocite CJELOBROJNO dijeljenje s 10 */
zadnjaZnamenka = broj - broj / 10 * 10;
/* druga mogucnost izracunavanja zadnje znamenke je pomocu operacije %
koja izracunava ostatak cjelobrojnog dijeljenja
zadnjaZnamenka = broj % 10;
*/
printf("Zadnja znamenka ucitanog broja %4d je %d\n", broj, zadnjaZnamenka);
} else {
printf("Broj %4d nije pozitivan broj\n", broj);
}
return 0;
}
