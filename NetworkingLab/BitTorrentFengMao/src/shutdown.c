#include "btdata.h"
#include "util.h"
#include "global.h"

// ��ȷ�Ĺرտͻ���
void client_shutdown(int sig)
{
    // ����ȫ��ֹͣ������ֹͣ���ӵ�����peer, �Լ���������peer������. Set global stop variable so that we stop trying to connect to peers and
    // �����������peer���ӵ��׽��ֺ����ӵ�����peer���߳�.
    int sockfd = connect_to_host(globalInfo.g_tracker_ip, globalInfo.g_tracker_port);
    int request_len = 0;
    char *request = make_tracker_request(&globalInfo, BT_STOPPED, &request_len);
    send(sockfd, request, request_len, 0);
    free(request);
    close(sockfd);
    close(listenfd);
    int i;
    for (i = 0; i < globalInfo.g_torrentmeta->filenum; i++){
        fclose(globalInfo.g_torrentmeta->flist[i].fp);
    }
    sleep(1);
    exit(0);
}
