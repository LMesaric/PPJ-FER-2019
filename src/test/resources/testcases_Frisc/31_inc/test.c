int main(void) {
    int x = 0;
    int y = x++;
    ++x;
    x = ++y;
    return x + y++;
}
