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

int bar(int x);
 int foo(int a, int b) {
 if (a > 0) {
 return a + bar(b);
 } else {
 return 0;
 }
 }
 int bar(int a) {
 return foo(a, a-1);
 }