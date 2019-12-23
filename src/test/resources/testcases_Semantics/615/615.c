int x;
char f(char x)
{
  int i=1;
  {
    int x=2;
    i=i+++x++;
    {
      char x='a';
      i=i+x++;  
    }
  }
  return (char)x;
}
int main(void)
{ 
  char x;
  return x;
}