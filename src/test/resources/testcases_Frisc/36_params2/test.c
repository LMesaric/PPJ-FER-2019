int f(int x, char y, int z, char a, int b, int c, int d, int arr[]) {
    int ret = x + y;
    {
        int xx = 12;
        ret = ret + xx;
        ret = ret + arr[x];
    }
    ret = ret + arr[x];
    return ret - d;
}
int main(void) {
    int x[3] = { 51, 52, 53 };
    return f(1, (char)2, 3, (char)4, 5, 6, 7, x);
}
