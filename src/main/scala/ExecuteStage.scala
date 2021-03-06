package rafi

import chisel3._
import chisel3.util._

class ExecuteStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val trap_enter = Output(Bool())
    val trap_return = Output(Bool())
    val trap_cause = Output(UInt(4.W))
    val trap_value = Output(UInt(64.W))
    val rd = Output(UInt(5.W))
    val reg_write_enable = Output(Bool())
    val reg_write_value = Output(UInt(64.W))
    val branch_taken = Output(Bool())
    val branch_target = Output(UInt(64.W))

    // Debug
    val host_io_value = Output(UInt(64.W))
}

class ExecuteStage(val dcache_hex_path: String) extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.EX)
        val prev = Flipped(new RegReadStageIF)
        val next = new ExecuteStageIF
        val csr = Flipped(new CsrExecuteIF)
    })

    val w_valid = Wire(Bool())

    w_valid := Mux(io.ctrl.flush, 0.U, io.prev.valid)

    // Bypass
    val m_bypass = Module(new BypassLogic)

    m_bypass.io.flush := io.ctrl.flush
    m_bypass.io.valid := w_valid && io.prev.reg_write_enable
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
    m_alu.io.is_word := io.prev.alu_is_word
    m_alu.io.src1_type := io.prev.alu_src1_type
    m_alu.io.src2_type := io.prev.alu_src2_type
    m_alu.io.pc := io.prev.pc
    m_alu.io.imm := io.prev.imm
    m_alu.io.rs1_value := w_rs1_value
    m_alu.io.rs2_value := w_rs2_value

    // Branch Unit
    val m_branch = Module(new BranchUnit)

    m_branch.io.cmd := io.prev.branch_cmd
    m_branch.io.is_always := io.prev.branch_always
    m_branch.io.is_relative := io.prev.branch_relative
    m_branch.io.pc := io.prev.pc
    m_branch.io.imm := io.prev.imm
    m_branch.io.rs1_value := w_rs1_value
    m_branch.io.rs2_value := w_rs2_value

    // Mem Unit
    val m_mem = Module(new MemUnit(dcache_hex_path))

    m_mem.io.valid := w_valid
    m_mem.io.cmd := io.prev.mem_cmd
    m_mem.io.is_signed := io.prev.mem_is_signed
    m_mem.io.access_size := io.prev.mem_access_size
    m_mem.io.imm := io.prev.imm
    m_mem.io.rs1_value := w_rs1_value
    m_mem.io.rs2_value := w_rs2_value

    // Result Mux
    val w_reg_write_value = Wire(UInt(64.W))
    val w_branch_taken = Wire(Bool())
    val w_branch_target = Wire(UInt(64.W))

    w_reg_write_value := MuxLookup(io.prev.execute_unit.asUInt(), 0.U, Seq(
        UnitType.ALU.asUInt() -> m_alu.io.result,
        UnitType.BRANCH.asUInt() -> m_branch.io.rd_value,
        UnitType.CSR.asUInt() -> io.csr.read_value,
        UnitType.MEM.asUInt() -> m_mem.io.result))
    w_branch_taken := MuxLookup(io.prev.execute_unit.asUInt(), 0.U, Seq(
        UnitType.BRANCH.asUInt() -> m_branch.io.taken))
    w_branch_target := MuxLookup(io.prev.execute_unit.asUInt(), 0.U, Seq(
        UnitType.BRANCH.asUInt() -> m_branch.io.target))

    m_bypass.io.rd_value := w_reg_write_value

    // CSR
    io.csr.valid := w_valid
    io.csr.cmd := io.prev.csr_cmd
    io.csr.addr := io.prev.csr_addr
    io.csr.operand := Mux(io.prev.csr_use_imm, io.prev.imm, w_rs1_value)

    // Pipeline register
    io.next.valid := RegNext(w_valid, 0.U)
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.trap_enter := RegNext(io.prev.trap_enter, 0.U)
    io.next.trap_return := RegNext(io.prev.trap_return, 0.U)
    io.next.trap_cause := RegNext(io.prev.trap_cause, 0.U)
    io.next.trap_value := RegNext(io.prev.trap_value, 0.U)
    io.next.rd := RegNext(io.prev.rd, 0.U)
    io.next.reg_write_enable := RegNext(io.prev.reg_write_enable, 0.U)
    io.next.reg_write_value := RegNext(w_reg_write_value, 0.U)
    io.next.branch_taken := RegNext(w_branch_taken, 0.U)
    io.next.branch_target := RegNext(w_branch_target, 0.U)
    io.next.host_io_value := RegNext(m_mem.io.host_io_value, 0.U)
}
