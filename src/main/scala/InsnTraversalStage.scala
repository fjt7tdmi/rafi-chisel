package rafi

import chisel3._
import chisel3.util._

class InsnTraversalStageIF extends Bundle {
    val pc = Output(UInt(64.W))
}

class InsnTraversalStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new ICacheReadStageIF)
        val next = new InsnTraversalStageIF
    })

    io.next.pc := RegNext(io.prev.pc, 0.U)
}
