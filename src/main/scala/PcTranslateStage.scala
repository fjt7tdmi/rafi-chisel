package rafi

import chisel3._
import chisel3.util._

class PcTranslateStageIF extends Bundle {
    val pc = Output(UInt(64.W))
}

class PcTranslateStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new PcPredictStageIF)
        val next = new PcTranslateStageIF
    })

    io.next.pc := RegNext(io.prev.pc, 0.U)
}
