package rafi

import chisel3._
import chisel3.util._

object Config {
    val INITIAL_PC = 0.U
}

class FetchAddrGenerateStage extends Module {
    val io = IO(new Bundle {
        val pc = Output(UInt(64.W))
    })

    val r_pc = RegInit(UInt(64.W), Config.INITIAL_PC)

    r_pc := r_pc + 4.U

    io.pc := r_pc
}
