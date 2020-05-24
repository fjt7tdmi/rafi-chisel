package rafi

import chisel3._
import chisel3.iotesters._
import chisel3.util._

class TempTest extends ChiselFlatSpec {    
    behavior of "FetchAddrGenerateStage"

    it should "FetchAddrGenerateStage increment PC every cycle" in {
        iotesters.Driver.execute(Array(
            "-tn=Core",
            "-td=work/Core",
            "-tgvo=on", "-tbn=verilator"), () => new Core) {
            core => new PeekPokeTester(core) {
                reset()
                
                var cycle : Int = 0
                var end : Boolean = false

                while (!end && cycle < 700) {
                    step(1)
                    cycle += 1

                    val valid = peek(core.io.valid)
                    val pc = peek(core.io.pc).toLong
                    val host_io_value = peek(core.io.host_io_value).toLong

                    if (valid == 1) {
                        println(f"$pc%08x $host_io_value%08x")                        
                    }

                    end = valid == 1 && host_io_value != 0
                }

                //expect(core.io.host_io_value, 1.U)
            }
        } should be (true)
    }
}