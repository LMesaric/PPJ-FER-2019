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

int fakt (int n) {
  int nfakt;
  if (n <= 1) { // 0! = 1! = 1
    nfakt = 1;
  } else {      // n! = n* (n-1)!
    nfakt = n * fakt(n-1);
    if (nfakt < 0 ) {
      return 1;
    }
  }
  return nfakt;
}



int main(void) {
  int n, fakto;
  n=0;
  while (1) {
    if (n>100000000) return 0;
    else fakto = fakt (n);
    printf ("\nUpisite n>"); // primjeri: n=15,16,17...
    scanf ("%d", &n);
    printf ("%d! = %ld",n, fakt (n));
  }
  return 0;
}
