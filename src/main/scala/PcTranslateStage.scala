package rafi

import chisel3._
import chisel3.util._

class PcTranslateStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
}

class PcTranslateStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.PT)
        val prev = Flipped(new PcPredictStageIF)
        val next = new PcTranslateStageIF
    })

    val w_valid = Wire(Bool())

    w_valid := io.prev.valid

    when (io.ctrl.flush) {
        w_valid := 0.U
    }

    // Pipeline register
    io.next.valid := RegNext(w_valid, 0.U)
    io.next.pc := RegNext(io.prev.pc, 0.U)
}
