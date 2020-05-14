package rafi

import chisel3._
import chisel3.util._

class RegReadStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val rd = Output(UInt(5.W))
}

class RegReadStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new DecodeStageIF)
        val next = new RegReadStageIF
    })

    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
}
