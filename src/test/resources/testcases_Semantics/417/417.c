int main(void) {
int a = 666;
const char e='e';
const int s=3;
{
int a = 5;
int y = a + 1; // 6
z = y + 1; // 7
}
z = a + 1; // 4
return 0;
}