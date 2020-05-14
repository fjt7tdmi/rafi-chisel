package rafi

import chisel3._
import chisel3.util._

class DecodeStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val rd = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
}

class DecodeStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new InsnTraversalStageIF)
        val next = new DecodeStageIF
    })

    val rd = Wire(UInt(5.W))
    val rs1 = Wire(UInt(5.W))
    val rs2 = Wire(UInt(5.W))

    rd := io.prev.insn(11, 7)
    rs1 := io.prev.insn(19, 15)
    rs2 := io.prev.insn(24, 20)

    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.rd := RegNext(rd, 0.U)
    io.next.rs1 := RegNext(rs1, 0.U)
    io.next.rs2 := RegNext(rs2, 0.U)
}
