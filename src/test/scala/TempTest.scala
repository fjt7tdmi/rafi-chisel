package rafi

import chisel3._
import chisel3.iotesters._

class TempTest extends ChiselFlatSpec {
    behavior of "FetchAddrGenerateStage"

    it should "FetchAddrGenerateStage increment PC every cycle" in {
        iotesters.Driver.execute(Array(
            "-tn=FetchAddrGenerateStage",
            "-td=test_run_dir/FetchAddrGenerateStage",
            "-tgvo=on", "-tbn=verilator"), () => new FetchAddrGenerateStage) {
            c => new PeekPokeTester(c) {
                reset()
                expect(c.io.pc, 0.U)

                for (i <- 0 until 10) {
                    val expected_pc = i * 4
                    expect(c.io.pc, expected_pc)
                    step(1)
                    
                    
                }
            }
        } should be (true)
    }
}