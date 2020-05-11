package rafi

import chisel3._
import chisel3.util._

class PcPredictStageIF extends Bundle {
    val pc = Output(UInt(64.W))
}

class PcPredictStage extends Module {
    val io = IO(new Bundle {
        val next = new PcPredictStageIF
    })

    val r_pc = RegInit(UInt(64.W), Config.INITIAL_PC)

    r_pc := r_pc + 4.U

    io.next.pc := r_pc
}
