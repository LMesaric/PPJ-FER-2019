void f(int x, int a[]) {
  x = x + 1;
  a[0] = a[0] + 1;
}

int main(void) {
  int z;
  int x = 3;
  int a[8] = {0};
  f(x, a); // x == 3, a[0] == 1
  char d[10]={'a','b','c','\0'};
  return 0;
}
