package rafi

import chisel3._
import chisel3.util._

class RegWriteStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
}

class RegWriteStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new ExecuteStageIF)
    })
}
