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

int Kontrola (char JMBG[]) {
  int i, kz;
  int suma;
  char tez [13] = "765432765432";
  suma = 0; 
  for (i = 0; i < 12; i++) {
    suma = suma + (JMBG[i] - '0') * (tez[i] - '0');
  }
  kz = 11 - (suma  % 11);
  if (kz == 10) kz = -1; // pogr. kontrolna znamenka
  if (kz == 11) kz = 0;
  return kz;
}

char datum (char JMBG) {
  int d, m, g; // lokalne varijable – vrijede samo unutar funkcije
  char *p; // pokazivač je deklariran, ali nije inicijaliziran!
  // Citanje iz niza
  // JMBG ima oblik DDMMYYYXXXXXX
  sscanf (JMBG, "%2d%2d%3d", &d, &m, &g); 
  // Ispis u niz treba biti oblika DD.MM.GGGG.
  p = (char *) malloc (11 * sizeof(char));
  //* pokazivač je inicijaliziran i naredbom malloc zauzeta je memorija potrebna za pohranu datuma u formatu DD.MM.GGGG */
  // 21. stoljece?
  if (g < 100){
    g += 2000;
  } else {
    g += 1000;
  }
  sprintf (p, "%02d.%02d.%04d", d, m, g); 
  // datum je zapisan na adresu na koju pokazuje pokazivač p
  return p;
}



int main(void) {
  int kz;
  char JMBG [13+1], *p;
  while (1) {
    printf ("\nUpisite JMBG >");
    scanf ("%13s", JMBG);
    kz = Kontrola (JMBG);
    if ((JMBG [12] - '0') == kz){
      p = datum (JMBG);
      printf ("\nDatum rodjenja je %s", p);
            free(p);
    } else {
      printf ("\nNeispravan JMBG: %s:\n", JMBG);
      break;
    }
  }
  return 0;
}
