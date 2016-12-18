/*
 * Please do not edit this file.
 * It was generated using rpcgen.
 */

#ifndef _DATE_H_RPCGEN
#define _DATE_H_RPCGEN

#include <rpc/rpc.h>


#ifdef __cplusplus
extern "C" {
#endif

#define MAXCLIENT 100

typedef struct SNTP_PARTIAL sntp;

struct SNTP_PARTIAL {
	int type;
	long ref;
	long org;
	long rec;
	long xmt;
	long dst;
	long delay;
	long offset;
	u_int keyid;
};
typedef struct SNTP_PARTIAL SNTP_PARTIAL;

#define DATE_PROG 0x20161122
#define DATE_VERS 1

#if defined(__STDC__) || defined(__cplusplus)
#define RAW_DATE 1
extern  sntp * raw_date_1(sntp *, CLIENT *);
extern  sntp * raw_date_1_svc(sntp *, struct svc_req *);
#define STR_DATE 2
extern  char ** str_date_1(sntp *, CLIENT *);
extern  char ** str_date_1_svc(sntp *, struct svc_req *);
extern int date_prog_1_freeresult (SVCXPRT *, xdrproc_t, caddr_t);

#else /* K&R C */
#define RAW_DATE 1
extern  sntp * raw_date_1();
extern  sntp * raw_date_1_svc();
#define STR_DATE 2
extern  char ** str_date_1();
extern  char ** str_date_1_svc();
extern int date_prog_1_freeresult ();
#endif /* K&R C */

/* the xdr functions */

#if defined(__STDC__) || defined(__cplusplus)
extern  bool_t xdr_sntp (XDR *, sntp*);
extern  bool_t xdr_SNTP_PARTIAL (XDR *, SNTP_PARTIAL*);

#else /* K&R C */
extern bool_t xdr_sntp ();
extern bool_t xdr_SNTP_PARTIAL ();

#endif /* K&R C */

#ifdef __cplusplus
}
#endif

#endif /* !_DATE_H_RPCGEN */
