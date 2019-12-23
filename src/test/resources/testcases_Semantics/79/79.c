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

int FI(int n) { 
  int i, fib, f1, f2;
  if (n <= 1) { 
    fib = 1;
  } else {
  f1 = 1; f2 = 1;   // predzadnji i zadnji broj
  for (i = 2; i <= n; i++) {
    fib = f1 + f2;  // novi broj
    if (fib < 0) {  
      printf ("Ne moze se prikazati %d. Fibonaccijev broj!\n", i);
      (void) getch();
      exit (1);
    }
    f1 = f2;        // zadnji postaje predzadnji
    f2 = fib;       // novi postaje zadnji
  }
  }
  return fib;
}

int main(void) {
  int n, brojac, fib;
  while (1) {
    brojac = 0;
    printf("Upisite broj >"); // Primjeri: n=5,40,50
    n=556;
    if (n < 0) {
    printf ("gotovo!\n"); break;
    } else {
    fib = FI (n);
    printf("%d. Fibonaccijev broj = %d , Izravno! \n", n, fib);
    }
  }
  return 0;

}
