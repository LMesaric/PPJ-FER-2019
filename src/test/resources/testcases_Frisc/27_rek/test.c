int f(int n) {
    if (n < 2) {
        return n;
    } else {
        return f(n-1) + f(n-2);
    }
}
int main(void) {
    return f(5);
}
