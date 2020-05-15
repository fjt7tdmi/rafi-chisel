package rafi

import chisel3._
import chisel3.util._

class RegReadStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val rd = Output(UInt(5.W))
    val src1 = Output(UInt(64.W))
    val src2 = Output(UInt(64.W))
    val reg_write_enable = Output(Bool())
}

class RegReadStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new DecodeStageIF)
        val next = new RegReadStageIF
        val reg_file = Flipped(new RegFileReadIF)
    })

    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
    io.next.src1 := RegNext(io.prev.rs1, 0.U)
    io.next.src2 := RegNext(io.prev.rs2, 0.U)
    io.next.reg_write_enable := RegNext(io.prev.reg_write_enable, 0.U)

    io.reg_file.rs1 := io.prev.rs1    
    io.reg_file.rs2 := io.prev.rs2
}
