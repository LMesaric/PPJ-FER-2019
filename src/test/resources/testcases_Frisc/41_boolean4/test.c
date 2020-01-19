int main(void) {
	int x = 2;
	if (x != 2 && (x = 5)) {
		return x;
	}
    return x;
}
