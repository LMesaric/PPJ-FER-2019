int n = 5;
int main(void) {
    if (n == 0) {
        return 0;
    } else {
        n = n - 1;
        return 1 + main();
    }
}
