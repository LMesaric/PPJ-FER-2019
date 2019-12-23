int main(void) {
  char a[10] = "abc";
  const char b[10] = {'a','b','c','\"'};
  char c[10] = a; //greska
  return 0;
}
