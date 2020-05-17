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

    val m_alu = Module(new Alu)

    m_alu.io.cmd := io.prev.alu_cmd
    m_alu.io.src1_type := io.prev.alu_src1_type
    m_alu.io.src2_type := io.prev.alu_src2_type
    m_alu.io.pc := io.prev.pc
    m_alu.io.imm := io.prev.imm
    m_alu.io.rs1_value := io.prev.rs1_value
    m_alu.io.rs2_value := io.prev.rs2_value

    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
    io.next.reg_write_enable := RegNext(io.prev.reg_write_enable, 0.U)
    io.next.reg_write_value := RegNext(m_alu.io.result, 0.U)
}
