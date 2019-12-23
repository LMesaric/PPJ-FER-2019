int x;
char f(char x)
{
  {
    int x;
    x++;
    {
      char x;
      x++;  
    }
  }
  return (char)x;
}
int main(void)
{ 
  char x;
  return x;
}