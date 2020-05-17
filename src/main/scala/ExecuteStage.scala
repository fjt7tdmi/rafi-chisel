package rafi

import chisel3._
import chisel3.util._

class ExecuteStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val rd = Output(UInt(5.W))
    val reg_write_enable = Output(Bool())
    val reg_write_value = Output(UInt(64.W))
    val branch_taken = Output(Bool())
    val branch_target = Output(UInt(64.W))
}

object ExecuteStage {
    val UNIT_ALU    = 0.U(1.W)
    val UNIT_BRANCH = 1.U(1.W)
}

class ExecuteStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.EX)
        val prev = Flipped(new RegReadStageIF)
        val next = new ExecuteStageIF
    })

    val w_valid = Wire(Bool())

    w_valid := io.prev.valid

    when (io.ctrl.flush) {
        w_valid := 0.U
    }

    val m_alu = Module(new Alu)

    m_alu.io.cmd := io.prev.alu_cmd
    m_alu.io.src1_type := io.prev.alu_src1_type
    m_alu.io.src2_type := io.prev.alu_src2_type
    m_alu.io.pc := io.prev.pc
    m_alu.io.imm := io.prev.imm
    m_alu.io.rs1_value := io.prev.rs1_value
    m_alu.io.rs2_value := io.prev.rs2_value

    val m_branch = Module(new BranchUnit)

    m_branch.io.cmd := io.prev.branch_cmd
    m_branch.io.always := io.prev.branch_always
    m_branch.io.pc := io.prev.pc
    m_branch.io.imm := io.prev.imm
    m_branch.io.rs1_value := io.prev.rs1_value
    m_branch.io.rs2_value := io.prev.rs2_value

    // Mux
    val reg_write_value = Wire(UInt(64.W))
    val branch_taken = Wire(Bool())
    val branch_target = Wire(UInt(64.W))

    reg_write_value := MuxCase(0.U, Seq(
        (io.prev.execute_unit === ExecuteStage.UNIT_ALU) -> m_alu.io.result,
        (io.prev.execute_unit === ExecuteStage.UNIT_BRANCH) -> m_branch.io.rd_value))
    branch_taken := MuxCase(0.U, Seq(
        (io.prev.execute_unit === ExecuteStage.UNIT_BRANCH) -> m_branch.io.taken))
    branch_target := MuxCase(0.U, Seq(
        (io.prev.execute_unit === ExecuteStage.UNIT_BRANCH) -> m_branch.io.target))

    // Pipeline register
    io.next.valid := RegNext(w_valid, 0.U)
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
    io.next.reg_write_enable := RegNext(io.prev.reg_write_enable, 0.U)
    io.next.reg_write_value := RegNext(reg_write_value, 0.U)
    io.next.branch_taken := RegNext(branch_taken, 0.U)
    io.next.branch_target := RegNext(branch_target, 0.U)
}
