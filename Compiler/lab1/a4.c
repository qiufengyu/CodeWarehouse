int main()
{
    int z = 0;
    int a[5][5][5.0];
    while (z < 10) {
        a[0][0][z] = z * z;
        a[1][0][z] = z / z;
		z++;
    }
}
