
CC=gcc
CFLAGS=  -g -std=c99 -MD -pthread
LIBS= -lpthread
TARGET=simpletorrent
SOURCES=$(shell find src/ -name "*.c")
OBJS=$(SOURCES:.c=.o)
#CFLAGS = -g -std=c99
all: ${TARGET}
	cp ./bin/$(TARGET) ./bin/test1/ 
	cp ./bin/$(TARGET) ./bin/test2/

debug:CFLAGS = -g -MD -D DEBUG -pthread
debug:all

${TARGET}: ${OBJS}
	${CC} ${CFLAGS} -o bin/${TARGET} ${LIBS} ${OBJS}

%.o: $.c
	$(CC) -c $(CFLAGS) $@ $<

-include $(patsubst %.o, %.d, $(OBJS))

clean:
	rm -rf bin/${TARGET}
	rm -rf bin/test1/${TARGET}
	rm -rf bin/test1/${TARGET}
	rm -rf src/*.core
	rm -rf $(OBJS) $(OBJS:.o=.d)
	rm -rf ${TARGET} 
	rm -rf *.core
	rm -rf ./mydata/2.jpeg
.PHONY: all clean


master:
	git add .
	git commit -m "test"
	git push origin master
jpush:
	git push origin master
commit:
	git add .
	git commit -m "te"
pull:
	git pull origin master	
	
movie:
	./bin/simpletorrent ./torrents/test_movie.torrent -v	

dirtest:
	./bin/simpletorrent ./torrents/dir_test.torrent -v	
	
start:
	./bin/simpletorrent ./torrents/2.torrent -v	
restart:
	make clean
	make
	./bin/simpletorrent ./torrents/2.torrent -v		
32cl1:
	./tools/32bit/bittorrent -t ./mydata/2.jpeg -s ./torrents/2.torrent -u http://114.212.190.187:10007/announce 
32cl2:
	./tools/32bit/bittorrent ./torrents/2.torrent -v	
64cl1:
	./tools/64bit/bittorrent -t ./mydata/2.jpeg -s ./torrents/2.torrent -u http://114.212.190.187:10007/announce 
64cl2:
	./tools/64bit/bittorrent ./torrents/2.torrent -v		
32tracker:
	./tools/32bit/tracker -i 114.212.190.187 -p 10007
64tracker:
	./tools/64bit/tracker -i 114.212.190.187 -p 10007	
32trackerd:
	./tools/32bit/tracker.debug	-i 114.212.190.187 -p 10007
64trackerd:
	./tools/64bit/tracker.debug -i 114.212.190.187 -p 10007	
