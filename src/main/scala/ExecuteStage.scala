package rafi

import chisel3._
import chisel3.util._

class ExecuteStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val rd = Output(UInt(5.W))
    val reg_write_enable = Output(Bool())
    val reg_write_value = Output(UInt(64.W))
}

class ExecuteStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new RegReadStageIF)
        val next = new ExecuteStageIF
    })

    val int_result = Wire(UInt(64.W))

    int_result := io.prev.src1 + io.prev.src2

    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
    io.next.reg_write_enable := RegNext(io.prev.reg_write_enable, 0.U)
    io.next.reg_write_value := RegNext(int_result, 0.U)
}
