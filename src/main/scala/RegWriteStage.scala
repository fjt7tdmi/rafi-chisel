package rafi

import chisel3._
import chisel3.util._

class RegWriteStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.RW)
        val prev = Flipped(new ExecuteStageIF)
        val csr = Flipped(new CsrTrapIF)
        val reg_file = Flipped(new RegFileWriteIF)

        // Debug
        val valid = Output(Bool())
        val pc = Output(UInt(64.W))
        val host_io_value = Output(UInt(64.W))
    })

    // Trap processing
    val w_trap_enter = Wire(Bool())
    val w_trap_return = Wire(Bool())
    val w_flush_target = Wire(UInt(64.W))
    val w_mpp = Wire(UInt(2.W))
    val w_mpie = Wire(Bool())
    val w_mie = Wire(Bool())
    val w_mstatus = Wire(UInt(64.W))

    w_trap_enter := io.prev.valid && io.prev.trap_enter
    w_trap_return := io.prev.valid && io.prev.trap_return

    w_flush_target := MuxCase(io.prev.branch_target, Seq(
        w_trap_enter -> (io.csr.mtvec_read_value << 2.U),
        w_trap_return -> io.csr.mepc_read_value))

    when (w_trap_enter) {
        w_mpp := 3.U // Machine
        w_mpie := io.csr.mstatus_read_value(3) // MPIE <= MIE
        w_mie := 0.U
    } .elsewhen (w_trap_return) {
        w_mpp := 0.U
        w_mpie := io.csr.mstatus_read_value(7)
        w_mie := io.csr.mstatus_read_value(7) // MIE <= MPIE
    } .otherwise {
        w_mpp := io.csr.mstatus_read_value(12, 11)
        w_mpie := io.csr.mstatus_read_value(7)
        w_mie := io.csr.mstatus_read_value(3)
    }
    w_mstatus := Cat(
        io.csr.mstatus_read_value(63, 13),
        w_mpp,
        io.csr.mstatus_read_value(10, 8),
        w_mpie,
        io.csr.mstatus_read_value(6, 4),
        w_mie,
        io.csr.mstatus_read_value(2, 0))

    // CSR
    io.csr.mstatus_we := w_trap_enter || w_trap_return
    io.csr.mstatus_write_value := w_mstatus
    io.csr.mcause_we := w_trap_enter
    io.csr.mcause_write_value := io.prev.trap_cause
    io.csr.mepc_we := w_trap_enter
    io.csr.mepc_write_value := io.prev.pc
    io.csr.mtval_we := w_trap_enter
    io.csr.mtval_write_value := io.prev.trap_value

    // Pipeline controller
    io.ctrl.flush := io.prev.valid && (io.prev.branch_taken || w_trap_enter || w_trap_return)
    io.ctrl.flush_target := w_flush_target

    // Register file
    io.reg_file.rd := io.prev.rd
    io.reg_file.value := io.prev.reg_write_value
    io.reg_file.write_enable := io.prev.reg_write_enable
    when (!io.prev.valid) {
        io.reg_file.write_enable := 0.U
    }

    // Debug
    io.valid := io.prev.valid
    io.pc := io.prev.pc
    io.host_io_value := io.prev.host_io_value
}
