struct TestStruct
{
	float a,b;
	int c;
};
int main()
{
	struct TestStruct test;
	test.c =1;
	test.a = test.b = 1.0;
	return test;
}
