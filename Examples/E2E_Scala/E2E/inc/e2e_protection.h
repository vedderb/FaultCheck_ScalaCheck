/*
	Copyright 2014 Benjamin Vedder	benjamin@vedder.se

	This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    */

#ifndef E2E_PROTECTION_H_
#define E2E_PROTECTION_H_

// Public types
typedef struct {
	unsigned char max_seq_diff;
	unsigned char data_size;
} E2E_CONFIGURATION;

typedef enum {
	E2E_RES_OK = 0,
	E2E_RES_OK_SOME_LOST,
	E2E_RES_INITIAL,
	E2E_RES_REPETITION,
	E2E_RES_OUT_OF_SEQUENCE,
	E2E_RES_WRONG_CRC,
	E2E_RES_NO_CONFIGURATION,
	E2E_RES_UNDEFINED
} E2E_RESULT;

// Parameters
#define E2E_OVERHEAD_SIZE	3

// Public functions
E2E_RESULT e2e_init(E2E_CONFIGURATION *conf);
E2E_RESULT e2e_protect(unsigned char *data);
E2E_RESULT e2e_check(unsigned char *data);
int e2e_get_result_size(void);

#endif /* E2E_PROTECTION_H_ */
