int c(int w) {
    return w;
}
int b(int z) {
    return z + c(z+1);
}
int a(int y) {
    return y + b(y+1);
}
int f(int x) {
    return a(x+1) + x;
}
int main(void) {
    return f(1);
}
