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

#include "application.h"
#include "e2e_protection.h"
#include "faultcheck_packet_wrapper.h"
#include <string.h>
#include <stdio.h>

/*
 * The global airbag_active variable shows if the airbag is exploded or not,
 * and is read from ScalaCheck. Then the ScalaCheck model determines
 * whether this is correct or not.
 */
int airbag_active;

// Private variables
static int activations;
static E2E_CONFIGURATION e2e_conf;

// Private functions
static const char* e2e_result_to_string(E2E_RESULT res);

// Settings
#define DATA_SIZE				2
#define VERBOSE_OUTPUT			1
#define ACTIVATIONS_TO_EXPLODE	3

/*
 * Set up the E2E-library and reset the state
 * of the application.
 */
void application_init() {
	memset(&e2e_conf, 0, sizeof(e2e_conf));

	airbag_active = 0;
	activations = 0;

	e2e_conf.max_seq_diff = 2;
	e2e_conf.data_size = DATA_SIZE;
	
	E2E_RESULT res = e2e_init(&e2e_conf);
	if (res != E2E_RES_OK) {
		printf("Bad init result: ");
		printf("%s\r\n", e2e_result_to_string(res));
	}
}

/*
 * Pass "sensor" data to the FaultCheck communication channel.
 */
void sensor(unsigned char *data) {
	faultcheck_packet_addPacket("airbag", (char*)data, DATA_SIZE);
}

/*
 * Read data from the FaultCheck communication channel as long as
 * there is data available. If the data matches [85, 170] increase a
 * counter and explode the airbag if that counter is 2 or more.
 */
void airbag_iteration() {
	unsigned char data[270];
	int len;

	while (faultcheck_packet_getPacket("airbag", (char*)data, &len)) {
		if (data[0] == 85 && data[1] == 170) {
			activations++;
		} else {
			activations = 0;
		}

		if (activations >= ACTIVATIONS_TO_EXPLODE && !airbag_active) {
			airbag_active = 1;
			printf("= EXPLODE!! =\r\n");
		}
	}
}

/*
 * Same as the other sensor function, but uses the E2E-library
 * to protect the data.
 */
void sensor_e2e(unsigned char *data) {
	unsigned char buffer[e2e_get_result_size()];
	
	if (e2e_get_result_size() >= DATA_SIZE) {
		memcpy(buffer, data, DATA_SIZE);
	}

	E2E_RESULT res = e2e_protect(buffer);
	if (res != E2E_RES_OK) {
		printf("Bad protect result: ");
		printf("%s\r\n", e2e_result_to_string(res));
	}
	
	faultcheck_packet_addPacket("airbag", (char*)buffer, e2e_get_result_size());
}

/*
 * Same as the other iteration function, but uses the E2E-library to 
 * check the data.
 */
void airbag_iteration_e2e() {
	unsigned char data[e2e_get_result_size()];
	int len;
	
	while (faultcheck_packet_getPacket("airbag", (char*)data, &len)) {
		E2E_RESULT res = e2e_check(data);
		
		if (res == E2E_RES_OK || res == E2E_RES_OK_SOME_LOST) {
			if (data[0] == 85 && data[1] == 170) {
				activations++;
			} else {
				activations = 0;
			}

			if (activations >= ACTIVATIONS_TO_EXPLODE && !airbag_active) {
				airbag_active = 1;
#if VERBOSE_OUTPUT
				printf("= EXPLODE!! =\r\n");
#endif
			}
		}
		
		if (res != E2E_RES_OK) {
#if VERBOSE_OUTPUT
			printf("Bad check result: ");
			printf("%s\r\n", e2e_result_to_string(res));
			
			if (res != E2E_RES_NO_CONFIGURATION) {
				printf("len: %d\r\n", len);
				printf("data: %d %d %d %d %d\r\n", data[0], data[1], data[2], data[3], data[4]);
			}
#endif
		}
	}
}

// Private functions
static const char* e2e_result_to_string(E2E_RESULT res) {
	switch (res) {
	case E2E_RES_OK: return "E2E_RES_OK";
	case E2E_RES_OK_SOME_LOST: return "E2E_RES_OK_SOME_LOST";
	case E2E_RES_INITIAL: return "E2E_RES_INITIAL";
	case E2E_RES_REPETITION: return "E2E_RES_REPETITION";
	case E2E_RES_OUT_OF_SEQUENCE: return "E2E_RES_OUT_OF_SEQUENCE";
	case E2E_RES_WRONG_CRC: return "E2E_RES_WRONG_CRC";
	case E2E_RES_NO_CONFIGURATION: return "E2E_RES_NO_CONFIGURATION";
	case E2E_RES_UNDEFINED: return "E2E_RES_UNDEFINED";
	default: return "unknown result code";
	}
}

