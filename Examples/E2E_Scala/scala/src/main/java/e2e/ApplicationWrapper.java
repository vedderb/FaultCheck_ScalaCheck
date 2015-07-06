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

package e2e;

import org.bridj.BridJ;
import org.bridj.CRuntime;
import org.bridj.Pointer;
import org.bridj.ann.Library;
import org.bridj.ann.Runtime;

@Library("e2etest")
@Runtime(CRuntime.class) 
public class ApplicationWrapper {
	static {
		BridJ.register();
	}
	/**
	 * Original signature : <code>void application_init()</code><br>
	 */
	native public static void application_init();
	/**
	 * Original signature : <code>void sensor(unsigned char*)</code><br>
	 */
	native public static void sensor(Pointer<Byte > data);
	/**
	 * Original signature : <code>void airbag_iteration()</code><br>
	 */
	native public static void airbag_iteration();
	/**
	 * Original signature : <code>void sensor_e2e(unsigned char*)</code><br>
	 */
	native public static void sensor_e2e(Pointer<Byte > data);
	/**
	 * Original signature : <code>void airbag_iteration_e2e()</code><br>
	 */
	native public static void airbag_iteration_e2e();
	
	public static int airbag_active() {
		try {
			return (int)BridJ.getNativeLibrary("e2etest").getSymbolPointer("airbag_active").as(int.class).get();
		}catch (Throwable $ex$) {
			throw new RuntimeException($ex$);
		}
	}
}
