int f(int x) {
    return x - 15;
}
int main(void) {
    int x = 1;
    int y = 2;
    int z = 4;
    int w = 8;
    int a = 16;
    int b = 32;
    int c = 64;

    return (a+b) + (x+y) + z + w + c + (a-z-w-a-b) + f(1) + f(5) + f(15);
}
