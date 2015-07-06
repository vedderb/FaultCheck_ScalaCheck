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

package e2e

import org.scalacheck._
import Gen._
import Arbitrary.arbitrary
import FaultCheckWrapper._
import ApplicationWrapper._
import E2ELibrary._
import org.bridj.Pointer
import org.bridj.Pointer._
import org.scalatest.prop.Checkers

object AirbagTest extends Commands {
  /*
   * Switch the E2E-library on or off. When it is on, the property should
   * always pass (unless genSensorWithExplosions is used). When it is off, 
   * violations can be found when enough tests are run, depending on the
   * generator.
   */
  val useE2E = true

  abstract class Fault
  case class BitFlip(bit: Int, byte: Int) extends Fault
  case class Drop(numPackets: Int) extends Fault
  case class Repeat(numPackets: Int) extends Fault

  // This is our state type that encodes the abstract state. The abstract state
  // should model all the features we need from the real state, the system
  // under test. We should leave out all details that aren't needed for
  // specifying our pre- and postconditions. The state type must be called
  // State and be immutable.
  case class State(faults: List[Fault], conf_max_diff: Int, conf_data_size: Int)

  // initialState should reset the system under test to a well defined
  // initial state, and return the abstract version of that state.
  def initialState() = {
    application_init()
    faultcheck_packet_removeAllFaults()
    State(List(), 2, 2)
  }

  // We define our commands as subtypes of the traits Command or SetCommand.
  // Each command must have a run method and a method that returns the new
  // abstract state, as it should look after the command has been run.
  // A command can also define a precondition that states how the current
  // abstract state must look if the command should be allowed to run.
  // Finally, we can also define a postcondition which verifies that the
  // system under test is in a correct state after the command execution.

  case class Sensor(b1: Int, b2: Int) extends Command {
    def run(s: State) = {
      val data = allocateBytes(2)
      data(0) = b1.toByte
      data(1) = b2.toByte

      if (useE2E) {
        sensor_e2e(data)
        airbag_iteration_e2e()
      } else {
        sensor(data)
        airbag_iteration()
      }

      airbag_active() == 1
    }

    def nextState(s: State) = s

    postConditions += {
      case (s0, s1, r: Boolean) => r == false
      case _ => false // should not happen
    }
  }

  case class AddFault(fault: Fault) extends Command {
    def run(s: State) = {
      val str = pointerToCString("airbag")

      fault match {
        case BitFlip(bit, byte) => {
          faultcheck_packet_addFaultCorruptionBitFlip(str, byte, bit)
        }

        case Drop(numPackets) => {
          faultcheck_packet_addFaultDrop(str, numPackets)
        }

        case Repeat(numPackets) => {
          faultcheck_packet_addFaultRepeat(str, numPackets)
        }
      }

      // The duration of every fault could also be limited.
      //faultcheck_packet_setDurationAfterTrigger(str, 3)
    }

    def nextState(s: State) = State(s.faults ++ List(fault),
        s.conf_max_diff, s.conf_data_size)

    preConditions += {
      s =>
        if (s.faults.length < 4 && !s.faults.contains(fault)) {
          true
        } else {
          false
        }
    }
  }

  /*
   * Make sure that the airbag really explodes with no faults present. This is
   * to make sure that the E2E library isn't blocking every single call.
   */
  case object Explode extends Command {
    def run(s: State) = {
      val data = allocateBytes(2)
      data(0) = 85.toByte
      data(1) = 170.toByte

      faultcheck_packet_removeAllFaults()

      if (useE2E) {
        // Run enough valid commands to make sure that the E2E-library is able
        // to recover.
        for (i <- 1 to 15) {
          sensor_e2e(data)
        }
        airbag_iteration_e2e()
      } else {
        for (i <- 1 to 10) {
          sensor(data)
        }
        airbag_iteration()
      }

      val res = airbag_active() == 1

      // Re-initialize the application to remove all faults
      application_init()
      faultcheck_packet_removeAllFaults()

      res
    }

    def nextState(s: State) = State(List(), 2, 2)
    
    /*
     * The pre-condition for the explosion command is that the E2E-configuration
     * is valid. Otherwise, all the checks of the E2E-library will fail and
     * it is not possible to explode the airbag.
     */
    preConditions += (s => {
      s.conf_data_size != 0 && s.conf_max_diff != 0
    })

    postConditions += {
      case (s0, s1, r: Boolean) => r == true
      case _ => false // Should not happen
    }
  }

  case class Configuration(nullconf: Boolean, max_seq_diff: Int, data_size: Int) extends Command {
    def run(s: State) = {
      val e2eConf = new E2E_CONFIGURATION
      
      e2eConf.data_size(data_size.toByte)
      e2eConf.max_seq_diff(max_seq_diff.toByte)

      if (nullconf) {
        e2e_init(0)
      } else {
        e2e_init(pointerTo(e2eConf))
      }
      false
    }

    def nextState(s: State) = {
      if (nullconf) {
        State(s.faults, 0, 0)
      } else {
        State(s.faults, max_seq_diff, data_size)
      }
    }

    postConditions += {
      case (s0, s1, r: Boolean) => r == false
      case _ => false // should not happen
    }
  }
  
  /*
   * The fault generator. By setting the frequency of different faults
   * to 0 disables them. This is interesting when looking at the code coverage
   * for the e2e_protection.c file. All faults are required to get full
   * coverage. The frequency controls the distribution of the faults.
   */
  val genFault = for {
    f <- Gen.frequency(
      (2, for {
        bit <- Gen.choose(0, 7)
        byte <- {
          if (useE2E) {
            Gen.choose(0, 3)
          } else {
            Gen.choose(0, 1)
          }
        }
      } yield BitFlip(bit, byte)),

      (1, for {
        numPackets <- Gen.choose(1, 10)
      } yield Drop(numPackets)),

      (2, for {
        numPackets <- Gen.choose(1, 10)
      } yield Repeat(numPackets)))
  } yield AddFault(f)
  
  /*
   * The default generator. Will generate two random bytes that will not fire
   * the airbag. With this generator, the probability that two bytes that are
   * likely to be corrupted such that the airbag fires is very low. Without the
   * E2E-library activated, up to millions of tests are required to make the
   * property fail. Running this many tests take a couple of minutes with
   * ScalaCheck.
   */
  val genSensor = for {
    b1 <- Gen.choose(0, 255)
    b2 <- {
      if (b1 == 85) {
        Gen.oneOf(Gen.choose(0, 169), Gen.choose(171, 255))
      } else {
        Gen.choose(0, 255)
      }
    }
  } yield Sensor(b1, b2)
  
  /*
   * This generator will generate a value that is more likely to be
   * corrupted such that the property fails. This usually takes
   * 2000 to 10000 tests.
   */
  val genSensorEasy = for {
    b1 <- Gen.oneOf(Gen.choose(0, 84), Gen.choose(86, 255))
    b2 <- 170
  } yield Sensor(b1, b2)
  
  /*
   * This generator will generate a value that is even easier to corrupt.
   * Usually takes less than 1000 tests.
   */
  val genSensorEasier = for {
    b1 <- Gen.oneOf(84, 87, 117, 213, 21)
    b2 <- 170
  } yield Sensor(b1, b2)
  
  /*
   * This generator will generate a value that takes at least two bit flips to
   * corrupt the value such that the property fails.
   */
  val genSensorAlt = for {
    b1 <- Gen.oneOf(84, 87, 117, 213, 21)
    b2 <- Gen.oneOf(138, 186, 42, 174, 171)
  } yield Sensor(b1, b2)
  
  /*
   * This generator should make the property fail even without faults, because
   * the values that are required to make the airbag explode will not
   * be excluded.
   */
  val genSensorWithExplosions = for {
    b1 <- 85
    b2 <- Gen.choose(140, 180)
  } yield Sensor(b1, b2)
  
  /*
   * This generator will generate configurations for the E2E-library. Some of them
   * will be invalid of null. The E2E-library should handle this.
   */
  val genConf = for {
    max_seq_diff <- Gen.choose(0, 50)
    data_size <- Gen.frequency((1, Gen.choose(0, 0)), (10, Gen.choose(2, 255)))
    nullconf <- Gen.frequency((1, true), (10, false))
  } yield Configuration(nullconf, max_seq_diff, data_size)

  // This is our command generator. Given an abstract state, the generator
  // should return a command that is allowed to run in that state. Note that
  // it is still necessary to define preconditions on the commands if there
  // are any. The generator is just giving a hint of which commands that are
  // suitable for a given state, the preconditions will still be checked before
  // a command runs. Sometimes you maybe want to adjust the distribution of
  // your command generator according to the state, or do other calculations
  // based on the state.
  def genCommand(s: State): Gen[Command] = Gen.frequency(
    (1, genFault),
    (1, Explode),
    (1, genConf),
    (10, genSensorEasier)) // You can also try genSensor, genSensorAlt, etc.
    
  /*
   * A helper function the run a specified number of tests.
   */
  def myCheck(name: String, prop: Prop, numTests: Int) = {
    println("\nTesting property " + name)
    
    val suite = new TestSuite(numTests)
    suite.myCheck(prop)

    println("OK, passed " + numTests + " tests.")
  }
  
  override def main(args: Array[String]) {
    val numTests = 30000
    myCheck("AirbagTest", AirbagTest, numTests)
  }
}

/*
 * This class is used to gain a bit more control of the generated tests.
 */
class TestSuite(numTests: Int) extends Checkers {
  implicit override val generatorDrivenConfig =
    PropertyCheckConfig(
      minSize = 5,
      maxSize = 20,
      maxDiscarded = 40000,
      minSuccessful = numTests)

  def myCheck(prop: Prop) = {
    check(prop)
  }
}
