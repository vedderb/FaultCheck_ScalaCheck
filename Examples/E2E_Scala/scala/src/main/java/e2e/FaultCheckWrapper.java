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
import org.bridj.ann.CLong;
import org.bridj.ann.Library;
import org.bridj.ann.Runtime;

@Library("FaultCheck") 
@Runtime(CRuntime.class) 
public class FaultCheckWrapper {
	static {
		BridJ.register();
	}
	/**
	 * Original signature : <code>void faultcheck_packet_addPacket(const char*, const char*, int)</code><br>
	 * <i>native declaration : line 2</i>
	 */
	native public static void faultcheck_packet_addPacket(Pointer<Byte > identifier, Pointer<Byte > data, int len);
	/**
	 * Original signature : <code>int faultcheck_packet_getPacket(const char*, char*, int*)</code><br>
	 * <i>native declaration : line 3</i>
	 */
	native public static int faultcheck_packet_getPacket(Pointer<Byte > identifier, Pointer<Byte > data, Pointer<Integer > len);
	/**
	 * Original signature : <code>void faultcheck_packet_addFaultCorruptionBitFlip(const char*, int, int)</code><br>
	 * <i>native declaration : line 4</i>
	 */
	native public static void faultcheck_packet_addFaultCorruptionBitFlip(Pointer<Byte > identifier, int byteIndex, int bitToFlip);
	/**
	 * Original signature : <code>void faultcheck_packet_addFaultDrop(const char*, int)</code><br>
	 * <i>native declaration : line 5</i>
	 */
	native public static void faultcheck_packet_addFaultDrop(Pointer<Byte > identifier, int numPackets);
	/**
	 * Original signature : <code>void faultcheck_packet_addFaultRepeat(const char*, int)</code><br>
	 * <i>native declaration : line 6</i>
	 */
	native public static void faultcheck_packet_addFaultRepeat(Pointer<Byte > identifier, int numPackets);
	/**
	 * Original signature : <code>void faultcheck_packet_setTriggerOnceAfterIterations(const char*, unsigned long)</code><br>
	 * <i>native declaration : line 7</i>
	 */
	native public static void faultcheck_packet_setTriggerOnceAfterIterations(Pointer<Byte > identifier, @CLong long iterations);
	/**
	 * Original signature : <code>void faultcheck_packet_setTriggerAfterIterations(const char*, unsigned long)</code><br>
	 * <i>native declaration : line 8</i>
	 */
	native public static void faultcheck_packet_setTriggerAfterIterations(Pointer<Byte > identifier, @CLong long iterations);
	/**
	 * Original signature : <code>void faultcheck_packet_setDurationAfterTrigger(const char*, int)</code><br>
	 * <i>native declaration : line 9</i>
	 */
	native public static void faultcheck_packet_setDurationAfterTrigger(Pointer<Byte > identifier, int iterations);
	/**
	 * Original signature : <code>void faultcheck_packet_removeAllFaultsIdentifier(const char*)</code><br>
	 * <i>native declaration : line 10</i>
	 */
	native public static void faultcheck_packet_removeAllFaultsIdentifier(Pointer<Byte > identifier);
	/**
	 * Original signature : <code>void faultcheck_packet_removeAllFaults()</code><br>
	 * <i>native declaration : line 11</i>
	 */
	native public static void faultcheck_packet_removeAllFaults();
}
