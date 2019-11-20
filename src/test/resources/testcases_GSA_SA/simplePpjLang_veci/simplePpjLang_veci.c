void fun(int a[]) {
    return;
}

int main(void) {
    int A[512];
    int t[] = {1,2,3};
    const char tmp[] = "te  \nst";
    int xYz, abc;
    
    xYz = 12345;
    abc = xYz;
    abc = abc+++xYz;
    abc = 1+2*3|4&5;
    
    for (i=0; i<4; ++i) {
        tmp[i] = (char)abc; 
        continue;
    }
    
    if (1>=3 && i>2)
        fun(35);
    else if (i<12)
        fun(5);
    else
        fun(123);

    return 0;
}
