int x = 12;
int main(void) {
    int y = 13;
    {
        int z = 1;
        {
            int w = 2;
            return x + y + z + w;
        }
    }
}
