int main()
{
    int i, a[10][10];
    i = 0;
    while (i != 10) {
        a[i] = 1;
        i = a[i,i];
        i = i + 1;
    }
    return 0;
}
