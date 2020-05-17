package rafi

import chisel3._
import chisel3.util._

class InsnTraversalStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
}

class InsnTraversalStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.IT)
        val prev = Flipped(new ICacheReadStageIF)
        val next = new InsnTraversalStageIF
    })

    val w_valid = Wire(Bool())

    w_valid := io.prev.valid

    when (io.ctrl.flush) {
        w_valid := 0.U
    }

    // Pipeline register
    io.next.valid := RegNext(w_valid, 0.U)
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
}
