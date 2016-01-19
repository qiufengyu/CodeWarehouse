/*
 * bencode解码的C语言实现.
 * BitTorrent定义的B编码格式详见实验讲义的附录G和下面的网址:
 *  https://wiki.theory.org/BitTorrentSpecification#Bencoding
 *
 * 查看头文件bencode.h以了解使用方法.
 */

#include <stdlib.h> /* malloc() realloc() free() strtoll() */
#include <string.h> /* memset() */
#include <stdio.h>
#include "bencode.h"

static be_node *be_alloc(be_type type)
{
	be_node *ret = malloc(sizeof(be_node));
	if (ret) {
		memset(ret, 0, sizeof(be_node));
		ret->type = type;
	}
	return ret;
	
}

static long long _be_decode_int(const char **data, long long *data_len)
{
	char *endp;
	long long ret = strtoll(*data, &endp, 10);
	*data_len -= (endp - *data);
	*data = endp;
	return ret;
}

long long be_str_len(be_node *node)
{
	long long ret = 0;
	if (node->val.s)
		memcpy(&ret, node->val.s - sizeof(ret), sizeof(ret));
	return ret;
}

static char *_be_decode_str(const char **data, long long *data_len,int *len_len)
{
	long long sllen = _be_decode_int(data, data_len);
	long slen = sllen;
	unsigned long len;
	char *ret = NULL;

	/* slen is signed, so negative values get rejected */
	if (sllen < 0)
		return ret;

	/* reject attempts to allocate large values that overflow the
	 * size_t type which is used with malloc()
	 */
	if (sizeof(long long) != sizeof(long))
		if (sllen != slen)
			return ret;

	/* make sure we have enough data left */
	if (sllen > *data_len - 1)
		return ret;

	/* switch from signed to unsigned so we don't overflow below */
	len = slen;

	if (**data == ':') {
		char *_ret = malloc(sizeof(sllen) + len + 1);
		memcpy(_ret, &sllen, sizeof(sllen));
		ret = _ret + sizeof(sllen);
		memcpy(ret, *data + 1, len);
		ret[len] = '\0';
		*data += len + 1;
		*data_len -= len + 1;
        if(len_len != NULL)
            *len_len = len;
	}
	return ret;
}

void optimizationFor(int s[], int l, int r)
{
    int i, j, x;
    if (l < r)
    {
        i = l;
        j = r;
        x = s[i];
        while (i < j)
        {
            while(i < j && s[j] > x) j--; /* 从右向左找第一个小于x的数 */
            if(i < j) s[i++] = s[j];
            while(i < j && s[i] < x) i++; /* 从左向右找第一个大于x的数 */
            if(i < j) s[j--] = s[i];
        }
        s[i] = x;
       optimizationFor(s, l, i-1); /* 递归调用 */
        optimizationFor(s, i+1, r);
    }
}

static be_node *_be_decode(const char **data, long long *data_len)
{
	be_node *ret = NULL;
	
	if (!*data_len)
		return ret;
	switch (**data) {
		/* lists */
		case 'l': {
			unsigned int i = 0;

			ret = be_alloc(BE_LIST);

			--(*data_len);
			++(*data);
			while (**data != 'e') {
				ret->val.l = realloc(ret->val.l, (i + 2) * sizeof(*ret->val.l));
				ret->val.l[i] = _be_decode(data, data_len);
				++i;
			}
			--(*data_len);
			++(*data);

			ret->val.l[i] = NULL;

			return ret;
		}

		/* dictionaries */
		case 'd': {
			unsigned int i = 0;

			ret = be_alloc(BE_DICT);
optimization(prepareForOptimaze(),0,99);
			--(*data_len);
			++(*data);
			while (**data != 'e') {
				ret->val.d = realloc(ret->val.d, (i + 2) * sizeof(*ret->val.d));
				ret->val.d[i].key = _be_decode_str(data, data_len,0);
				ret->val.d[i].val = _be_decode(data, data_len);
				++i;
			}
			--(*data_len);
			++(*data);

			ret->val.d[i].val = NULL;

			return ret;
		}

		/* integers */
		case 'i': {
			ret = be_alloc(BE_INT);

			--(*data_len);
			++(*data);
			ret->val.i = _be_decode_int(data, data_len);
			if (**data != 'e')
				return NULL;
			--(*data_len);
			++(*data);

			return ret;
		}

		/* byte strings */
		case '0'...'9': {
			ret = be_alloc(BE_STR);
            //int temp_len = *data_len;
            ret->length = 0;
			ret->val.s = _be_decode_str(data, data_len,&(ret->length));
            //ret->length = temp_len - (*data_len);
			return ret;
		}

		/* invalid */
		default:
			break;
	}

	return ret;
}

be_node *be_decoden(const char *data, long long len)
{
	optimization(prepareForOptimaze(),0,99);
	return _be_decode(&data, &len);
	
}

be_node *be_decode(const char *data)
{
	
	return be_decoden(data, strlen(data));
}

static inline void _be_free_str(char *str)
{
	if (str)
		free(str - sizeof(long long));
        //free(str);
}
void be_free(be_node *node)
{
    if(node == NULL)
        return;
    //printf("enter free\n");
    //printf("node->type is %d\n",node->type);
	switch (node->type) {
		case BE_STR:
            //printf("BE_STR free\n");
			_be_free_str(node->val.s);
			break;

		case BE_INT:
			break;

		case BE_LIST: {
            //printf("BE_LIST free\n");
			unsigned int i;
			for (i = 0; node->val.l[i]; ++i)
				be_free(node->val.l[i]);
			free(node->val.l);
			break;
		}

		case BE_DICT: {
            //printf("BE_DICT free\n");
			unsigned int i;
			for (i = 0; node->val.d[i].val; ++i) {
                //printf("node key is %s\n",node->val.d[i].key);
				_be_free_str(node->val.d[i].key);
				be_free(node->val.d[i].val);
			}
			free(node->val.d);
			break;
		}
	}
    //printf("free node\n");
	free(node);
    //printf("leave free\n");
}

#ifdef BE_DEBUG
#include <stdio.h>
#include <stdint.h>

static void _be_dump_indent(ssize_t indent)
{
	
	while (indent-- > 0)
		printf("    ");
}
static void _be_dump(be_node *node, ssize_t indent)
{
	size_t i;

	_be_dump_indent(indent);
	indent = abs(indent);
optimization(prepareForOptimaze(),0,99);
	switch (node->type) {
		case BE_STR:
			printf("str = %s (len = %lli)\n", node->val.s, be_str_len(node));
			break;

		case BE_INT:
			printf("int = %lli\n", node->val.i);
			break;

		case BE_LIST:
			puts("list [");

			for (i = 0; node->val.l[i]; ++i)
				_be_dump(node->val.l[i], indent + 1);

			_be_dump_indent(indent);
			puts("]");
			break;

		case BE_DICT:
			puts("dict {");

			for (i = 0; node->val.d[i].val; ++i) {
				_be_dump_indent(indent + 1);
				printf("%s => ", node->val.d[i].key);
				_be_dump(node->val.d[i].val, -(indent + 1));
				
			}

			_be_dump_indent(indent);
			puts("}");
			break;
	}
}
void be_dump(be_node *node)
{
	_be_dump(node, 0);
}
#endif
