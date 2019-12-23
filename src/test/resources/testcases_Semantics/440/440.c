int a[2]={2};
char b[200]="\"\0\n\t\\"";

int main(int a)
{ b[0]=(char)'\0'+-1;
return a;
}