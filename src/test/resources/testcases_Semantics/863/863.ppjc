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

int pot(int x, int y) {
  int ret;

  if (y <= 0) ret = 1;
  else ret = x * pot(x, y - 1);

  return ret;
}

int main(void) {

  int x, y, rez;
  while (1) {
    printf ("Upisite cijeli broj za bazu i nenegativni cijeli broj za eksponent>");
    scanf ("%d %d", &x, &y);
    if (y < 0) break;
    rez = pot (x, y);
    printf ("%d na potenciju %d = %d\n", x, y, rez);
  }
  printf ("\nNegativni eksponent\n");
  return 0;

}
