const MAXCLIENT = 100;

typedef struct SNTP_PARTIAL sntp;

struct SNTP_PARTIAL {
	/* type for req */
	int type;

	/* Reference Timestamp: Time when the system clock was last set or corrected */
	long ref;

	/* Origin Timestamp: Time at the client when the request departed for the server */
	long org;

	/* Receive Timestamp: Time at the server when the request arrived from the client */
	long rec;

	/* Transmit Timestamp: Time at the server when the response left for the client */
	long xmt;

	/* Destination Timestamp: Time at the client when the reply arrived from the server */
	long dst;

    /* delay and offset */
	long delay;
	long offset;
	
	/* designate a secret key */
	unsigned int keyid;
};


program DATE_PROG {
	version DATE_VERS {
		sntp RAW_DATE(sntp) = 1;
		string STR_DATE(sntp) = 2;
	} = 1;
} = 0x20161122;
