int gcd(int m, int n) {
    if (m == 0) {
        return n;
    } else {
        return gcd(n%m, m);
    }
}
int main(void) {
    return gcd(20, 6);
}
