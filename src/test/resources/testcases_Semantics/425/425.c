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
 
/*
ne radi nista
*/
void fun(int xYz) {
    return;
}

// glavni program "testira osnovne kljucne rijeci i operatore za lekser"
int main(void) {
    int A[512];
    char tmp[] = "te\nst";
    const char x[] = "\"tes\"t2\"";
    int xYz, abc;
    int i;
    
    xYz = 12345; // nekakav komentar
    abc = xYz;
    abc = (xYz);
    abc = abc+++xYz;
    abc = 054 % 5;
    abc = 0xaafff;
    i = 3*2+5-3|3&3^3;
    
    tmp[1] = 'b';
    tmp[2] = '\n';
    tmp[0] = '\''
    for (i=0; i<4; ++i) {
        tmp[i] = (char)abc; /* komentar *
                             * komentar *
                             * komentar */
        break;
        continue;
        return xYz;
    }
    
    if (1>=3 && i>2 || i<=12) {
        fun(3);
    } else {
        fun(5);
    }
    
    while (1) {
        break;
    }
    
    return 0;
}