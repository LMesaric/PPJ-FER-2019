int main(void) {
  char a[10] = "abc\t";
  const char b[10] = {'a','b','c','\0'};
  char c[10] = a; //greska
  return 0;
}
