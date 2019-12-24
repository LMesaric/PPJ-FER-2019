int f(int a[]) {
    return a[0];
}
int main(void) {
    int a[2];
    a[0] = 1;
    a[1] = f(a);
    return a[1][2][3];
}
