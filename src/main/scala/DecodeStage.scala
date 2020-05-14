package rafi

import chisel3._
import chisel3.util._

class DecodeStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
}

class DecodeStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new InsnTraversalStageIF)
        val next = new DecodeStageIF
    })

    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
}
