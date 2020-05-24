package rafi

import chisel3._
import chisel3.iotesters._

class TempTest extends ChiselFlatSpec {
    behavior of "FetchAddrGenerateStage"

    it should "FetchAddrGenerateStage increment PC every cycle" in {
        iotesters.Driver.execute(Array(
            "-tn=Core",
            "-td=work/Core",
            "-tgvo=on", "-tbn=verilator"), () => new Core) {
            core => new PeekPokeTester(core) {
                reset()
                expect(core.io.pc, 0.U)

                for (i <- 0 until 100) {
                    step(1)
                    if (core.io.valid == 1.U) {
                        printf("%08x %08x\n", core.io.pc, core.io.host_io_value)
                    }
                }
            }
        } should be (true)
    }
}