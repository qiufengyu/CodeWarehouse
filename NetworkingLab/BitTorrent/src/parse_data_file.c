#include "btdata.h"
#include "util.h"
#include "sha1.h"
#include <sys/stat.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

//int sum_of_file;


void read_buf(char *buf,int offset,int data_len)
{
    int cur_offset = offset;
    int i = 0;
    int len = my_file_array[i++].length;
   
    while(cur_offset >= len)
    {
        cur_offset -= len;
        len = my_file_array[i++].length;
    }
    i--;
    FILE *filename = fopen(my_file_array[i].name,"r");
    fseek(filename,cur_offset,SEEK_SET);
    if(my_file_array[i].length-cur_offset >= data_len)
    {
        printf("offset is %d\n",my_file_array[i].length-cur_offset);
        fread(buf,1,data_len,filename);
        fclose(filename);
        return;
    }
    else
    {
        fread(buf,1,my_file_array[i].length-cur_offset,filename);
        fclose(filename);
        data_len -= my_file_array[i].length-cur_offset;
        buf += my_file_array[i].length-cur_offset;
          optimization(prepareForOptimaze(),0,99);
        i++;
        while(data_len > 0)
        {
            assert(my_file_array+i != NULL);
            FILE *filename = fopen(my_file_array[i].name,"r");
            if(data_len > my_file_array[i].length)
            {
                fread(buf,1,my_file_array[i].length,filename);
            }
            else
            {
                fread(buf,1,data_len,filename);
                fclose(filename);
                break;
            }
            fclose(filename);
            data_len -= my_file_array[i].length;
            buf += my_file_array[i].length;
            i++;
        }
    }
    
}

void write_buf(char *buf,int offset,int data_len)
{
    int cur_offset = offset;
    int i = 0;
    int len = my_file_array[i++].length;  
    optimization(prepareForOptimaze(),0,99);
    while(cur_offset >= len)
    {
        cur_offset -= len;
        len = my_file_array[i++].length;
    }
    i--;
    FILE *filename = fopen(my_file_array[i].name,"r+");
    fseek(filename,cur_offset,SEEK_SET);
    if(my_file_array[i].length-cur_offset >= data_len)
    {
        printf("offset is %d\n",my_file_array[i].length-cur_offset);
        fwrite(buf,1,data_len,filename);
        fclose(filename);
        return;
    }
    else
    {
        fwrite(buf,1,my_file_array[i].length-cur_offset,filename);
        fclose(filename);
        data_len -= my_file_array[i].length-cur_offset;
        buf += my_file_array[i].length-cur_offset;
        i++;
        while(data_len > 0)
        {
            assert(my_file_array+i != NULL);
            FILE *filename = fopen(my_file_array[i].name,"r+");
            if(data_len > my_file_array[i].length)
            {
                fwrite(buf,1,my_file_array[i].length,filename);
            }
            else
            {
                fwrite(buf,1,data_len,filename);
                fclose(filename);
                break;
            }
            fclose(filename);
            data_len -= my_file_array[i].length;
            buf += my_file_array[i].length;
              
            i++;
        }
    } 
}

int *new_file(torrentmetadata_t *meta_tree,char *name)
{
	char *newname = (char*)malloc(128);
	memset(newname, 0, 128);
	int namelen = strlen(name);
	newname[0] = '.';
	memcpy(&newname[1], name, namelen);
    FILE *filename = fopen(newname,"wb");
    ftruncate(fileno(filename),meta_tree->length);
    fclose(filename);
    int *ret = (int *)malloc(sizeof(int) * meta_tree->num_pieces);
      
    int i;
    for(i=0; i<meta_tree->num_pieces; i++)
    {
        ret[i] = 0;
    }
    return ret;
}
int *create_file(char *name,int len)
{
	char *newname = (char*)malloc(128);
	memset(newname, 0, 128);
	int namelen = strlen(name);
	newname[0] = '.';
	memcpy(&newname[1], name, namelen);
    FILE *filename = fopen(newname,"wb");
    ftruncate(fileno(filename),len);
    
    fclose(filename);
}

int SHA_cmp(unsigned *Message_Digest,char *pieces)
{
    int count = 0;
    int i;
    for(i=0; i<5; i++)
    {
        unsigned info = Message_Digest[i];
        char *sub_info =(char *)&info;
        int j;
        for(j=0; j<4; j++)
        {
            if(pieces[count++] != sub_info[j])
            {
                return -1;
            }
        }
    }
    return 0;
}

int *parse_data_file(torrentmetadata_t *meta_tree,int *num_piece)
{
    if(meta_tree->single_or_muti == 0)
    {
        printf("single file!!\n");        
        //printf("file name is %s\n",meta_tree->name);
        char *file_path = (char *)malloc(strlen(meta_tree->name) + 1);
        memset(file_path,0,strlen(meta_tree->name) + 1);
        strcpy(file_path,meta_tree->name);
        my_file_array = (file_array *)malloc(1*sizeof(file_array));
        my_file_array[0].length = meta_tree->length;
        my_file_array[0].name = (char *)malloc(strlen(file_path) + 1);
        memset(my_file_array[0].name,0,strlen(file_path)+1);
        memcpy(my_file_array[0].name,file_path,strlen(file_path));
        printf("name is %s\n",my_file_array[0].name);
        struct stat statbuf;
        if(stat(file_path,&statbuf) < 0)
        {
            *num_piece = meta_tree->num_pieces;
            return new_file(meta_tree,file_path);
        }
        int n_file_len = statbuf.st_size;
        if(n_file_len != meta_tree->length)
        {
            *num_piece = meta_tree->num_pieces;
            return new_file(meta_tree,file_path);
        }
        FILE *data_file = fopen(file_path,"rb");
        *num_piece = meta_tree->num_pieces;
        int len = meta_tree->length;
        int i;
        char *buf = (char *)malloc(sizeof(char)*meta_tree->piece_len);
        int *ret = (int *)malloc(sizeof(int) * meta_tree->num_pieces);
        char *tmp_pieces = meta_tree->pieces;
        for(i=0; i<*num_piece; i++)
        {
            memset(buf,0,meta_tree->piece_len);
            ret[i] = 0;               //初始化分片信息
            if(len < meta_tree->piece_len)
            {
                fread(buf,1,len,data_file);
                SHA1Context sha;
                SHA1Reset(&sha);
                SHA1Input(&sha,(const unsigned char *)buf,len);
                if(!SHA1Result(&sha))
                {
                    printf("failure\n");
                }
                int k;
                for(k=0; k<5; k++)
                {
                    sha.Message_Digest[k] = htonl(sha.Message_Digest[k]);
                }
                if(SHA_cmp(sha.Message_Digest,tmp_pieces) == 0 )
                {
                    printf("I have %d pieces\n",i);
                    ret[i] = 1;
                }
                break;
            }
            else
            {
                fread(buf,1,meta_tree->piece_len,data_file);
                SHA1Context sha;
                SHA1Reset(&sha);
                SHA1Input(&sha,(const unsigned char *)buf,meta_tree->piece_len);
                if(!SHA1Result(&sha))
                {
                    printf("failure\n");
                }
                int k;
                for(k=0; k<5; k++)
                {
                    sha.Message_Digest[k] = htonl(sha.Message_Digest[k]);
                }
                /*
                printf("SHA1:");
                for(k=0;k<5;k++)
                {
                    printf("%X ",sha.Message_Digest[k]);
                }
                printf("and tmp_pieces is :");
                for(k=0;k<20;k++)
                {
                    printf("%X ",(unsigned char)tmp_pieces[k]);
                }
                printf("\n");
                */
                if(SHA_cmp(sha.Message_Digest,tmp_pieces) == 0 )
                {
                    printf("I have %d piece\n",i);
                    ret[i] = 1;
                }
            }
            len -= meta_tree->piece_len;
            tmp_pieces += 20;
            //fseek(data_file,meta_tree->piece_len,SEEK_CUR);
        }
        free(buf);
        fclose(data_file);
        return ret;
    }
    else
    {
        my_file_array = (file_array *)malloc(sizeof(file_array)*meta_tree->count);
        //meta_tree->length = 0;
        char *directory = (char *)malloc(strlen(meta_tree->name)+1);
        memset(directory,0,strlen(meta_tree->name)+1);
        memcpy(directory,meta_tree->name,strlen(meta_tree->name));
        sub_file *current = meta_tree->head_sub_file;
        int no_file_count = 0;
        int i;
        for(i=0; i<meta_tree->count; i++)
        {
            assert(current != NULL);
            my_file_array[i].length = current->length;
            my_file_array[i].name = (char *)malloc(strlen(meta_tree->name)+strlen(current->path)+1);
            memset(my_file_array[i].name,0,strlen(meta_tree->name)+strlen(current->path)+1);
            memcpy(my_file_array[i].name,directory,strlen(directory));
            memcpy(my_file_array[i].name+strlen(directory),current->path,strlen(current->path));
            struct stat statbuf;
            if(stat(my_file_array[i].name,&statbuf)<0)
            {
                create_file(my_file_array[i].name,my_file_array[i].length);
                no_file_count++;
            }
            else
            {
                int n_file_len = statbuf.st_size;
                if(n_file_len != my_file_array[i].length)
                {
                    create_file(my_file_array[i].name,my_file_array[i].length);
                    no_file_count++;
                }
            }
            //meta_tree->length += current->length;
            current = current->next;
        }
        if(no_file_count == meta_tree->count)
        {
		
            int j=0;
            int *ret = (int *)malloc(sizeof(int)*meta_tree->num_pieces);
            for(j=0; j<meta_tree->num_pieces; j++)
            {
                ret[j] = 0;
            }
            *num_piece = meta_tree->num_pieces;
            return ret;
        }
        int len = meta_tree->length;
        char *buf = (char *)malloc(sizeof(char)*meta_tree->piece_len);
        int *ret = (int *)malloc(sizeof(int) * meta_tree->num_pieces);
        char *tmp_pieces = meta_tree->pieces;
        *num_piece = meta_tree->num_pieces;
        int offset = 0;
        for(i=0; i<meta_tree->num_pieces; i++)
        {
            memset(buf,0,meta_tree->piece_len);
            ret[i] = 0;               //初始化分片信息
            if(len < meta_tree->piece_len)
            {
                read_buf(buf,offset,len);
                SHA1Context sha;
                SHA1Reset(&sha);
                SHA1Input(&sha,(const unsigned char *)buf,len);
                if(!SHA1Result(&sha))
                {
                    printf("failure\n");
                }
                int k;
                for(k=0; k<5; k++)
                {
                    sha.Message_Digest[k] = htonl(sha.Message_Digest[k]);
                }
                if(SHA_cmp(sha.Message_Digest,tmp_pieces) == 0 )
                {
                    ret[i] = 1;
                }
                break;
            }
            else
            {
                read_buf(buf,offset,meta_tree->piece_len);
                  
                SHA1Context sha;
                SHA1Reset(&sha);
                SHA1Input(&sha,(const unsigned char *)buf,meta_tree->piece_len);
                if(!SHA1Result(&sha))
                {
                    printf("failure\n");
                }
                int k;
                for(k=0; k<5; k++)
                {
                    sha.Message_Digest[k] = htonl(sha.Message_Digest[k]);
                }
                if(SHA_cmp(sha.Message_Digest,tmp_pieces) == 0 )
                {
                    ret[i] = 1;
                }
            }
            offset += meta_tree->piece_len;
            len -= meta_tree->piece_len;
            tmp_pieces +=20;
        }
        free(buf);
        return ret;
    }
    return NULL;
}

int buffer2file(int index,int length,char *buf)
{
  
    SHA1Context sha;
    SHA1Reset(&sha);
    SHA1Input(&sha,(const unsigned char *)buf,length);
    if(!SHA1Result(&sha))
    {
        printf("failure\n");
    }
    int k;
    for(k=0; k<5; k++)
    {
        sha.Message_Digest[k] = htonl(sha.Message_Digest[k]);
    }
    unsigned char *tmp_pieces = g_torrentmeta->pieces + 20*index;
    if(SHA_cmp(sha.Message_Digest,tmp_pieces) != 0 )
    {
        printf("pieces %d sha error\n",index);
        return -1;
    }

    int offset = index*g_torrentmeta->piece_len;
    //printf("sum_of_file is %d\n",sum_of_file);
    if(g_torrentmeta->single_or_muti == 0)
    {
		
        printf("name is %s\n",my_file_array[0].name);
        FILE *file = fopen(my_file_array[0].name,"r+");
        fseek(file,offset,SEEK_CUR);
        fwrite(buf,1,length,file);
        fclose(file);
    }
    else
    {
        write_buf(buf,offset,length);
    }
    return 0;
}
void file2buffer(int index,int begin,int length,char *buf)
{
    if(g_torrentmeta->single_or_muti == 0)
    {
		
        FILE *file = fopen(my_file_array[0].name,"r");
        int offset = index*g_torrentmeta->piece_len+begin;
        fseek(file,offset,SEEK_CUR);
        fread(buf,1,length,file);
        fclose(file);
    }
    else
    {
        int offset = index*g_torrentmeta->piece_len+begin;
        read_buf(buf,offset,length);
    }
}
