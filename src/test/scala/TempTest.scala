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

                for (i <- 0 until 10) {
                    val expected_pc = i * 4
                    expect(core.io.pc, expected_pc)
                    step(1)                    
                }
            }
        } should be (true)
    }
}