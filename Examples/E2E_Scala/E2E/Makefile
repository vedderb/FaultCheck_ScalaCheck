CC=gcc
AR=ar
RM = rm -f

vpath %.c src

# C-flags
CFLAGS  = -g -O0 -Wall -fPIC -coverage
CFLAGS += -ffreestanding -nostdlib

# Includes
CFLAGS += -Iinc -I$(FAULTPATH)

# Libraries
CFLAGS += -L$(FAULTPATH)

# Sources
SRCS = crc.c e2e_protection.c

OBJS = $(SRCS:.c=.o)
LIBNAME = libe2e.a

all: $(LIBNAME)

%.o : %.c
	$(CC) $(CFLAGS) -c -o $@ $^

$(LIBNAME): $(OBJS)
	$(AR) -r $@ $(OBJS)

clean:
	${RM} $(OBJS) $(LIBNAME)
	${RM} *.gcov *.gcda *.gcno
	
