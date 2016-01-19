
CC=gcc
CFLAGS= -std=c99 -g -O2
LIBS= -lpthread

TARGET=simpletorrent
SOURCES=src/util.c \
        src/bencode.c \
				src/sha1.c \
				src/shutdown.c \
				src/make_tracker_request.c \
				src/parse_announce_url.c \
        src/parsetorretnfile.c \
				src/process_tracker_response.c \
				src/simpletorrent.c \
				src/connect_to_peers.c \
				src/listen_peers.c \
				src/recv_from_peers.c \
				src/manager.c \
				src/parse_data_file.c

OBJS=src/util.o \
     src/bencode.o \
		 src/sha1.o\
		 src/shutdown.o \
		 src/make_tracker_request.o \
		 src/parse_announce_url.o \
     src/parsetorrentfile.o \
		 src/process_tracker_response.o \
		 src/simpletorrent.o \
		 src/connect_to_peers.o \
		 src/listen_peers.o \
		 src/recv_from_peers.o \
		 src/manager.o \
		 src/parse_data_file.o

all: ${TARGET}

${TARGET}: ${OBJS}
	${CC} ${CFLAGS} -o bin/${TARGET} ${OBJS} ${LIBS} 

%.o: $.c
	$(CC) -c $(CFLAGS) $@ $<

clean:
	rm -rf src/*.o
	rm -rf bin/${TARGET}
	rm -rf src/*.core
	rm -rf *.o
	rm -rf ${TARGET}
	rm -rf *.core

.PHONY: all clean
