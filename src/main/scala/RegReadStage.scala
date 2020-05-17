package rafi

import chisel3._
import chisel3.util._

class RegReadStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val execute_unit = Output(UInt(1.W))
    val rd = Output(UInt(5.W))
    val reg_write_enable = Output(Bool())
    val alu_cmd = Output(UInt(4.W))
    val alu_src1_type = Output(UInt(2.W))
    val alu_src2_type = Output(UInt(2.W))
    val branch_cmd = Output(UInt(3.W))
    val branch_always = Output(Bool())
    val imm = Output(UInt(64.W))
    val rs1_value = Output(UInt(64.W))
    val rs2_value = Output(UInt(64.W))    
}

class RegReadStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new DecodeStageIF)
        val next = new RegReadStageIF
        val reg_file = Flipped(new RegFileReadIF)
    })

    // Read register file
    io.reg_file.rs1 := io.prev.rs1
    io.reg_file.rs2 := io.prev.rs2

    // Pipeline register
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.execute_unit := RegNext(io.prev.execute_unit, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
    io.next.reg_write_enable := RegNext(io.prev.reg_write_enable, 0.U)
    io.next.alu_cmd := RegNext(io.prev.alu_cmd, 0.U)
    io.next.alu_src1_type := RegNext(io.prev.alu_src1_type, 0.U)
    io.next.alu_src2_type := RegNext(io.prev.alu_src2_type, 0.U)
    io.next.branch_cmd := RegNext(io.prev.branch_cmd, 0.U)
    io.next.branch_always := RegNext(io.prev.branch_always, 0.U)
    io.next.imm := RegNext(io.prev.imm, 0.U)
    io.next.rs1_value := RegNext(io.reg_file.rs1_value, 0.U)
    io.next.rs2_value := RegNext(io.reg_file.rs2_value, 0.U)
}
