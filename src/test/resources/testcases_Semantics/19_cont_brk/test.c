int main(void) {
    int x = 3;
    while (x < 5) {
        x = x + 1;
        if (x > 6) {
            continue;
        }
        if (x < 3) {
            break;
        }
        break;
    }
    while (x > 5) {
        while (1) {
            while (1) break;
        }
        break;
    }
    {
        continue;
        break;
    }
    break;
    continue;
    return 0;
}
