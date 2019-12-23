int main(void)
{  
 int x=0;
 {
   char x=(char)(int)(char)2;
   x++;
 }
return x/0;
}
