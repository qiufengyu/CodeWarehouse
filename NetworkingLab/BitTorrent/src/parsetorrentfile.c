
#include "bencode.h"
#include "util.h"
#include "sha1.h"


char* find_nodes(char *data,int len)
{

    int i = 0;
    for(; i<len-strlen("e5:nodes"); i++)
    {
        if(strncmp(data+i,"e5:nodes",strlen("e5:nodes")) == 0)
        {
            return data+i;
        }
    }
    return NULL;
}


// 注意: 这个函数只能处理单文件模式torrent
torrentmetadata_t* parsetorrentfile(char* filename)
{
    int i;
    be_node* ben_res;
    FILE* f;
    int flen;
    char* data;
    torrentmetadata_t* ret;

    // 打开文件, 获取数据并解码字符串
    f = fopen(filename,"r");
    flen = file_len(f);
    data = (char*)malloc(flen*sizeof(char));
    fread((void*)data,sizeof(char),flen,f);
    fclose(f);
    ben_res = be_decoden(data,flen);

    // 遍历节点, 检查文件结构并填充相应的字段.
    if(ben_res->type != BE_DICT)
    {
        perror("Torrent file isn't a dictionary");
        exit(-13);
    }

    ret = (torrentmetadata_t*)malloc(sizeof(torrentmetadata_t));
    if(ret == NULL)
    {
        perror("Could not allocate torrent meta data");
        exit(-13);
    }

    // 计算这个torrent的info_hash值
    // 注意: SHA1函数返回的哈希值存储在一个整数数组中, 对于小端字节序主机来说,
    // 在与tracker或其他peer返回的哈希值进行比较时, 需要对本地存储的哈希值
    // 进行字节序转换. 当你生成发送给tracker的请求时, 也需要对字节序进行转换.
    char* info_loc, *info_end;
    info_loc = strstr(data,"infod");  // 查找info键, 它的值是一个字典
    info_loc += 4; // 将指针指向值开始的地方
    info_end = data+flen-1;
    // 去掉结尾的e
    char *s = find_nodes(data,flen);
    if(s == NULL)
    {
        if(*info_end == 'e')
        {
            --info_end;
        }
    }
    else
    {
        info_end = s;
    }

    char* p;
    int len = 0;
    for(p=info_loc; p<=info_end; p++) len++;

    printf("len is %d\n",len);
    // 计算上面字符串的SHA1哈希值以获取info_hash
    SHA1Context sha;
    SHA1Reset(&sha);
    SHA1Input(&sha,(const unsigned char*)info_loc,len);
    if(!SHA1Result(&sha))
    {
        printf("FAILURE\n");
    }

    memcpy(ret->info_hash,sha.Message_Digest,20);
    printf("SHA1:\n");
    for(i=0; i<5; i++)
    {
        printf("%08X ",ret->info_hash[i]);
    }
    printf("\n");
    int flag = 0;
    // 检查键并提取对应的信息
    int filled=0;
    for(i=0; ben_res->val.d[i].val != NULL; i++)
    {
        int j;
        if(!strncmp(ben_res->val.d[i].key,"nodes",strlen("nodes")))
        {
            printf("in nodes\n");
        }
        else if(!strncmp(ben_res->val.d[i].key,"announce",strlen("announce")))
        {
            if(flag == 1)
                continue;
            flag = 1;
            ret->announce = (char*)malloc( (strlen(ben_res->val.d[i].val->val.s) + 1) *sizeof(char));
            memset(ret->announce,0,strlen(ben_res->val.d[i].val->val.s) + 1);
            int pos = strlen(ben_res->val.d[i].val->val.s);
            memcpy(ret->announce,ben_res->val.d[i].val->val.s,strlen(ben_res->val.d[i].val->val.s));
            //printf("origin announce last byte is : %x\n",ret->announce[pos]);
            printf("announce is %s\n",ret->announce);
            filled++;
        }
        // info是一个字典, 它还有一些其他我们关心的键
        else if(!strncmp(ben_res->val.d[i].key,"info",strlen("info")))
        {
            be_dict* idict;
            if(ben_res->val.d[i].val->type != BE_DICT)
            {
                perror("Expected dict, got something else");
                exit(-3);
            }
            idict = ben_res->val.d[i].val->val.d;
            ret->single_or_muti = 0;
            // 检查这个字典的键
            for(j=0; idict[j].key != NULL; j++)
            {
                if(!strncmp(idict[j].key,"files",strlen("files")))
                {
                    ret->single_or_muti = 1;
                    ret->head_sub_file = NULL;
                    ret->count = 0;
                    ret->length = 0;
                    if(idict[j].val->type != BE_LIST)
                    {
                        perror("Expected a list type\n");
                        exit(-1);
                    }
                    int k=0;
                    be_node **file_node = idict[j].val->val.l;
                    for(k=0; file_node[k] !=NULL; k++)
                    {
                        assert(file_node[k]->type == BE_DICT);
                        be_dict *node_dict = file_node[k]->val.d;
                        sub_file* my_sub_file = (sub_file *)malloc(sizeof(sub_file));
                        memset(my_sub_file,0,sizeof(sub_file));
                        int m;
                        for(m=0; node_dict[m].key != NULL; m++)
                        {
                            if(!strncmp(node_dict[m].key,"length",strlen("length")))
                            {
                                my_sub_file->length = node_dict[m].val->val.i;
                            }
                            if(!strncmp(node_dict[m].key,"path",strlen("path")))
                            {
                                be_node *path_name = node_dict[m].val->val.l[0];
                                my_sub_file->path = (char *)malloc(strlen(path_name->val.s)+1);
                                memset(my_sub_file->path,0,strlen(path_name->val.s)+1);
                                memcpy(my_sub_file->path,path_name->val.s,strlen(path_name->val.s));
                            }
                        }
                        if(ret->head_sub_file == NULL)
                        {
                            ret->head_sub_file = my_sub_file;
                            ret->count++;
                        }
                        else
                        {
                            sub_file *current = ret->head_sub_file;
                            while(current->next != NULL)
                            {
                                current = current->next;
                            }
                            current->next = my_sub_file;
                            ret->count++;
                        }
                        ret->length += my_sub_file->length;
                    }
                    filled ++;
                }
                if(!strncmp(idict[j].key,"length",strlen("length")))
                {
                    ret->length = idict[j].val->val.i;
                    filled++;
                }
                if(!strncmp(idict[j].key,"name",strlen("name")))
                {
                    ret->name = (char*)malloc((strlen(idict[j].val->val.s)+1)*sizeof(char));
                    memset(ret->name,0,strlen(idict[j].val->val.s)+1);
                    memcpy(ret->name,idict[j].val->val.s,strlen(idict[j].val->val.s));
                    filled++;
                }
                if(!strncmp(idict[j].key,"piece length",strlen("piece length")))
                {
                    ret->piece_len = idict[j].val->val.i;
                    filled++;
                }
                if(!strncmp(idict[j].key,"pieces",strlen("pieces")))
                {
                    printf("parse piece\n");
                    int num_pieces = ret->length/ret->piece_len;
                    if(ret->length % ret->piece_len != 0)
                        num_pieces++;
                    ret->pieces = (char*)malloc(num_pieces*20);
                    memcpy(ret->pieces,idict[j].val->val.s,num_pieces*20);
                    ret->num_pieces = num_pieces;
                    printf("num_pieces is %d\n",ret->num_pieces);
                    filled++;
                    goto L;
                }
            } // for循环结束
            printf("22\n");
        } // info键处理结束
    }

    // 确认已填充了必要的字段
L:
    be_free(ben_res);

    if(filled < 5)
    {
        printf("Did not fill necessary field\n");
        return NULL;
    }
    else
        return ret;
}
