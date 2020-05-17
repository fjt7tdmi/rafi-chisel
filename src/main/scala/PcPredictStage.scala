package rafi

import chisel3._
import chisel3.util._

class PcPredictStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
}

class PcPredictStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.PP)
        val next = new PcPredictStageIF
    })

    val w_valid = Wire(Bool())
    val r_pc = RegInit(UInt(64.W), Config.INITIAL_PC)

    r_pc := r_pc + 4.U

    when (io.ctrl.flush) {
        w_valid := 0.U
        r_pc := io.ctrl.flush_target
    } .otherwise {
        w_valid := 1.U
    }

    io.next.valid := w_valid
    io.next.pc := r_pc
}
