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
 
int printf(int x) {
  /* i wish i could printf */
  return 0;
}

int main(void) {
  int i, j, n;

  struct timeb vrijeme1, vrijeme2; long trajanjems;

  while (scanf("%d", &n)==1 && n > 0) {
    ftime (&vrijeme1);

    for (i = 1; i < n; ++i) {
      if (i % 100 == 0) printf (".");

      for (j = 0; j < i; ++j)
        ;

    }

    ftime (&vrijeme2);
    trajanjems = 1000 * (vrijeme2.time - vrijeme1.time) +
                  vrijeme2.millitm - vrijeme1.millitm;
    printf("\n%ld ms\n", trajanjems);
  }

  return 0;

}
