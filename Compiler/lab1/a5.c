int put(int n,int max)
{
    int i;
	int queen[10];
    while(i < max)
    {       
        queen[n] = i;
        if(!check(n))
        {           
            if(n == max - 1)
            {
                show(); 
            }         
            else
            {
                put(n + 1); 
            }
        }
		i =i+1;
    }
	return i;
}
