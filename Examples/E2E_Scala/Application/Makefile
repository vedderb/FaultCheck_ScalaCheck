CC=gcc
RM = rm -f

# C-flags
CFLAGS  = -std=gnu99 -fPIC -Wall -Wextra -O0 -g -coverage
LDFLAGS = -shared -coverage

# Includes
CFLAGS += -I../E2E/inc -I$(FAULTCHECK_PATH)

# Libraries
CFLAGS += -L../E2E -L$(FAULTCHECK_PATH)
CFLAGS += -le2e -lFaultCheck

# Sources
SRCS = application.c

# Target library
TARGET_LIB = libe2etest.so

.PHONY: deplibs all clean

all: deplibs
	$(CC) $(SRCS) $(CFLAGS) $(LDFLAGS) -o $(TARGET_LIB)

deplibs:
	$(MAKE) -C ../E2E

clean:
	$(MAKE) clean -C ../E2E
	${RM} *.o *.so*
	${RM} *.gcov *.gcda *.gcno

