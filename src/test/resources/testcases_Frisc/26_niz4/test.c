void f(int a[]) {
    a[3] = 123;
}
int main(void) {
    int a[5];
    f(a);
    return a[3];
}
