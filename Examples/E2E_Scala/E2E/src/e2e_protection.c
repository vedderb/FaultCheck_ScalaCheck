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

#include "e2e_protection.h"
#include "crc.h"
#include <string.h>

// Private types
typedef struct {
	unsigned char snd_seq_number;
	unsigned char rec_seq_number;
	unsigned char rec_is_initial;
} E2E_STATE;

// Private variables
static E2E_CONFIGURATION *configuration = 0;
static E2E_CONFIGURATION configuration_copy;
static E2E_STATE state;

// Public functions
E2E_RESULT e2e_init(E2E_CONFIGURATION *conf) {
	E2E_RESULT res = E2E_RES_NO_CONFIGURATION;
	
	configuration = 0;
	
	if (conf != 0) {
		if (conf->data_size > 0 &&
				conf->max_seq_diff > 0) {
			// Create a local copy of the configuration in case the 
			// pointer changes
			memcpy(&configuration_copy, conf, sizeof(E2E_CONFIGURATION));
			configuration = &configuration_copy;
			
			state.snd_seq_number = 0;
			state.rec_seq_number = 0;
			state.rec_is_initial = 1;
			
			res = E2E_RES_OK;
		}
	}
	
	return res;
}

E2E_RESULT e2e_protect(unsigned char *data) {
	E2E_RESULT res = E2E_RES_UNDEFINED;
	
	if (configuration != 0) {
		const unsigned int data_size = configuration->data_size;
		
		data[data_size] = ++state.snd_seq_number;
		const unsigned short crc = crc16(data, data_size + 1);
		
		data[data_size + 1] = crc >> 8;
		data[data_size + 2] = crc & 0xFF;
		
		res = E2E_RES_OK;
	} else {
		res = E2E_RES_NO_CONFIGURATION;
	}
	
	return res;
}

E2E_RESULT e2e_check(unsigned char *data) {
	E2E_RESULT res = E2E_RES_UNDEFINED;
	
	if (configuration != 0) {
		const unsigned int data_size = configuration->data_size;
		const unsigned short crc_calc = crc16(data, data_size + 1);
		const unsigned short crc_rec = ((unsigned short)data[data_size + 1] << 8) |
			((unsigned short)data[data_size + 2] & 0xFF);
		const unsigned char seq_diff = data[data_size] - state.rec_seq_number;

		/*
		 * Only set the sequence number of the data to the received one if the
		 * CRC matches. Otherwise, it might be corrupted and the best thing
		 * we can do is to simply add one to the previous sequence number.
		 * 
		 * This problem was detected by running PBT with fault injection and
		 * looking at the coverage :-)
		 */
		if (crc_rec == crc_calc) {
			state.rec_seq_number = data[data_size];
		} else {
			state.rec_seq_number++;
		}
		
		if (crc_rec != crc_calc) {
			res = E2E_RES_WRONG_CRC;
		} else if (state.rec_is_initial) {
			state.rec_is_initial = 0;
			res = E2E_RES_INITIAL;
		} else if (seq_diff == 0) {
			res = E2E_RES_REPETITION;
		} else if (seq_diff > configuration->max_seq_diff) {
			res = E2E_RES_OUT_OF_SEQUENCE;
		} else if (seq_diff != 1) {
			res = E2E_RES_OK_SOME_LOST;
		} else {
			res = E2E_RES_OK;
		}
	} else {
		res = E2E_RES_NO_CONFIGURATION;
	}
	
	return res;
}

int e2e_get_result_size(void) {
	int res = 0;

	if (configuration != 0) {
		res = configuration->data_size + E2E_OVERHEAD_SIZE;
	}
	
	return res;
}

