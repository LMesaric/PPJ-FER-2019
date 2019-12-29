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

int aniz(int a0, int d, int n) {
   if (n == 0) return a0;
   else return d + aniz(a0, d, n-1);
}


int main(void) {
  int a0, d, n, nclan;
  while (1) {
    printf ("\nUpisite nulti clan, diferenciju i indeks zadanog clana >");
    scanf ("%d %d %d", &a0, &d, &n);
    if (n < 0) break;
    nclan = aniz (a0, d, n);
    printf ("\n %d. clan aritmetickog niza, s nultim clanom %d i diferencijom %d: %d \n", n, a0, d, nclan);
  }
  printf ("\nNegativni indeks clana %d\n", n);
  return 0;

}
