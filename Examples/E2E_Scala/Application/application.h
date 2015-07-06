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

#ifndef APPLICATION_H_
#define APPLICATION_H_

// public variables
extern int airbag_active;

// Public functions
void application_init();
void sensor(unsigned char *data);
void airbag_iteration();
void sensor_e2e(unsigned char *data);
void airbag_iteration_e2e();

#endif /* APPLICATION_H_ */

