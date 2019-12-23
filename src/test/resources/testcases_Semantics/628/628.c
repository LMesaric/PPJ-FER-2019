int f(int a[]) {
  return a[0];
}

int main(void) {
  int i = 0;
  int n = 2;
  int a[2];
  while (i < n && (a[i] = 2)) {
    i++;
  }
  f(a);
  return 0;
}