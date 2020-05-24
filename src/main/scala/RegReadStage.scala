package rafi

import chisel3._
import chisel3.util._

class RegReadStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val trap_enter = Output(Bool())
    val trap_return = Output(Bool())
    val trap_cause = Output(UInt(4.W))
    val trap_value = Output(UInt(64.W))
    val execute_unit = Output(UInt(2.W))
    val rd = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val reg_write_enable = Output(Bool())
    val alu_cmd = Output(UInt(4.W))
    val alu_is_word = Output(Bool())
    val alu_src1_type = Output(UInt(2.W))
    val alu_src2_type = Output(UInt(2.W))
    val branch_cmd = Output(UInt(3.W))
    val branch_always = Output(Bool())
    val csr_cmd = Output(UInt(2.W))
    val csr_addr = Output(UInt(12.W))
    val csr_use_imm = Output(Bool())
    val mem_cmd = Output(UInt(2.W))
    val mem_is_signed = Output(Bool())
    val mem_access_size = Output(UInt(2.W))
    val imm = Output(UInt(64.W))
    val rs1_value = Output(UInt(64.W))
    val rs2_value = Output(UInt(64.W))    
}

class RegReadStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.RR)
        val prev = Flipped(new DecodeStageIF)
        val next = new RegReadStageIF
        val reg_file = Flipped(new RegFileReadIF)
    })

    val w_valid = Wire(Bool())

    w_valid := Mux(io.ctrl.flush, 0.U, io.prev.valid)

    // Read register file
    io.reg_file.rs1 := io.prev.rs1
    io.reg_file.rs2 := io.prev.rs2

    // Pipeline register
    io.next.valid := RegNext(w_valid, 0.U)
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.trap_enter := RegNext(io.prev.trap_enter, 0.U)
    io.next.trap_return := RegNext(io.prev.trap_return, 0.U)
    io.next.trap_cause := RegNext(io.prev.trap_cause, 0.U)
    io.next.trap_value := RegNext(io.prev.trap_value, 0.U)
    io.next.execute_unit := RegNext(io.prev.execute_unit, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
    io.next.rs1 := RegNext(io.prev.rs1, 0.U)
    io.next.rs2 := RegNext(io.prev.rs2, 0.U)
    io.next.reg_write_enable := RegNext(io.prev.reg_write_enable, 0.U)
    io.next.alu_cmd := RegNext(io.prev.alu_cmd, 0.U)
    io.next.alu_is_word := RegNext(io.prev.alu_is_word, 0.U)
    io.next.alu_src1_type := RegNext(io.prev.alu_src1_type, 0.U)
    io.next.alu_src2_type := RegNext(io.prev.alu_src2_type, 0.U)
    io.next.branch_cmd := RegNext(io.prev.branch_cmd, 0.U)
    io.next.branch_always := RegNext(io.prev.branch_always, 0.U)
    io.next.csr_cmd := RegNext(io.prev.csr_cmd, 0.U)
    io.next.csr_addr := RegNext(io.prev.csr_addr, 0.U)
    io.next.csr_use_imm := RegNext(io.prev.csr_use_imm, 0.U)
    io.next.mem_cmd := RegNext(io.prev.mem_cmd, 0.U)
    io.next.mem_is_signed := RegNext(io.prev.mem_is_signed, 0.U)
    io.next.mem_access_size := RegNext(io.prev.mem_access_size, 0.U)
    io.next.imm := RegNext(io.prev.imm, 0.U)
    io.next.rs1_value := RegNext(io.reg_file.rs1_value, 0.U)
    io.next.rs2_value := RegNext(io.reg_file.rs2_value, 0.U)
}
