int main(void) {
    int f(int x);
    int x = f(1);
    {
        int f(int y, int z);
        x = f(1, x);
    }
    return x;
}
int f(int x) {
    return x + 1;
}
