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
    val UNIT_ALU    = 0.U(3.W)
    val UNIT_BRANCH = 1.U(3.W)
    val UNIT_CSR    = 2.U(3.W)
    val UNIT_TRAP   = 3.U(3.W)
    val UNIT_MEM    = 4.U(3.W)
}

class ExecuteStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.EX)
        val prev = Flipped(new RegReadStageIF)
        val next = new ExecuteStageIF
    })

    val w_valid = Wire(Bool())

    w_valid := Mux(io.ctrl.flush, 0.U, io.prev.valid)

    // Bypass
    val m_bypass = Module(new BypassLogic)

    m_bypass.io.flush := io.ctrl.flush
    m_bypass.io.valid := w_valid
    m_bypass.io.rd := io.prev.rd
    m_bypass.io.rs1 := io.prev.rs1
    m_bypass.io.rs2 := io.prev.rs2

    val w_rs1_value = Wire(UInt(64.W))
    val w_rs2_value = Wire(UInt(64.W))

    w_rs1_value := Mux(m_bypass.io.rs1_hit, m_bypass.io.rs1_value, io.prev.rs1_value)
    w_rs2_value := Mux(m_bypass.io.rs2_hit, m_bypass.io.rs2_value, io.prev.rs2_value)

    // ALU
    val m_alu = Module(new Alu)

    m_alu.io.cmd := io.prev.alu_cmd
    m_alu.io.src1_type := io.prev.alu_src1_type
    m_alu.io.src2_type := io.prev.alu_src2_type
    m_alu.io.pc := io.prev.pc
    m_alu.io.imm := io.prev.imm
    m_alu.io.rs1_value := w_rs1_value
    m_alu.io.rs2_value := w_rs2_value

    // Branch Unit
    val m_branch = Module(new BranchUnit)

    m_branch.io.cmd := io.prev.branch_cmd
    m_branch.io.always := io.prev.branch_always
    m_branch.io.pc := io.prev.pc
    m_branch.io.imm := io.prev.imm
    m_branch.io.rs1_value := w_rs1_value
    m_branch.io.rs2_value := w_rs2_value

    // CSR
    val m_csr = Module(new Csr)

    m_csr.io.valid := w_valid
    m_csr.io.cmd := io.prev.csr_cmd
    m_csr.io.addr := io.prev.csr_addr
    m_csr.io.operand := Mux(io.prev.csr_use_imm, io.prev.imm, w_rs1_value)

    // Mem Unit
    val m_mem = Module(new MemUnit)

    m_mem.io.valid := w_valid
    m_mem.io.cmd := io.prev.mem_cmd
    m_mem.io.is_signed := io.prev.mem_is_signed
    m_mem.io.access_size := io.prev.mem_access_size
    m_mem.io.imm := io.prev.imm
    m_mem.io.rs1_value := w_rs1_value
    m_mem.io.rs2_value := w_rs2_value

    // Result Mux
    val reg_write_value = Wire(UInt(64.W))
    val branch_taken = Wire(Bool())
    val branch_target = Wire(UInt(64.W))

    reg_write_value := MuxCase(0.U, Seq(
        (io.prev.execute_unit === ExecuteStage.UNIT_ALU) -> m_alu.io.result,
        (io.prev.execute_unit === ExecuteStage.UNIT_BRANCH) -> m_branch.io.rd_value,
        (io.prev.execute_unit === ExecuteStage.UNIT_CSR) -> m_csr.io.read_value,
        (io.prev.execute_unit === ExecuteStage.UNIT_MEM) -> m_mem.io.result))
    branch_taken := MuxCase(0.U, Seq(
        (io.prev.execute_unit === ExecuteStage.UNIT_BRANCH) -> m_branch.io.taken))
    branch_target := MuxCase(0.U, Seq(
        (io.prev.execute_unit === ExecuteStage.UNIT_BRANCH) -> m_branch.io.target))

    m_bypass.io.rd_value := reg_write_value

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
