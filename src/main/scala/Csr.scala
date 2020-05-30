package rafi

import chisel3._
import chisel3.util._
import scala.annotation.switch

object CsrAddr {
    val MSTATUS    = "h300".U(12.W)
    val MISA       = "h301".U(12.W)
    val MEDELEG    = "h302".U(12.W)
    val MIDELEG    = "h303".U(12.W)
    val MIE        = "h304".U(12.W)
    val MTVEC      = "h305".U(12.W)
    val MCOUNTEREN = "h306".U(12.W)
    val MSCRATCH   = "h340".U(12.W)
    val MEPC       = "h341".U(12.W)
    val MCAUSE     = "h342".U(12.W)
    val MTVAL      = "h343".U(12.W)
    val MIP        = "h344".U(12.W)
}

class CsrExecuteIF extends Bundle {
    val valid = Input(Bool())
    val cmd = Input(CsrCmd())
    val addr = Input(UInt(12.W))
    val operand = Input(UInt(64.W))
    val read_value = Output(UInt(64.W))
}

class CsrTrapIF extends Bundle {
    val mstatus_we = Input(Bool())
    val mstatus_write_value = Input(UInt(64.W))
    val mcause_we = Input(Bool())
    val mcause_write_value = Input(UInt(64.W))
    val mepc_we = Input(Bool())
    val mepc_write_value = Input(UInt(64.W))
    val mtval_we = Input(Bool())
    val mtval_write_value = Input(UInt(64.W))
    val mstatus_read_value = Output(UInt(64.W))
    val mtvec_read_value = Output(UInt(64.W))
    val mepc_read_value = Output(UInt(64.W))
}

class Csr extends Module {
    val io = IO(new Bundle {
        val ex = new CsrExecuteIF
        val trap = new CsrTrapIF
    })

    // Register Definitions
    val r_mstatus    = RegInit(0.U(64.W))
    val r_misa       = RegInit(0.U(64.W))
    val r_medeleg    = RegInit(0.U(64.W))
    val r_mideleg    = RegInit(0.U(64.W))
    val r_mie        = RegInit(0.U(64.W))
    val r_mtvec      = RegInit(0.U(64.W))
    val r_mcounteren = RegInit(0.U(64.W))
    val r_mscratch   = RegInit(0.U(64.W))
    val r_mepc       = RegInit(0.U(64.W))
    val r_mcause     = RegInit(0.U(64.W))
    val r_mtval      = RegInit(0.U(64.W))
    val r_mip        = RegInit(0.U(64.W))

    // Register Read
    val w_read_value = Wire(UInt(64.W))

    w_read_value := MuxLookup(io.ex.addr, 0.U, Seq(
        CsrAddr.MSTATUS -> r_mstatus,
        CsrAddr.MISA -> r_misa,
        CsrAddr.MEDELEG -> r_medeleg,
        CsrAddr.MIDELEG -> r_mideleg,
        CsrAddr.MIE -> r_mie,
        CsrAddr.MTVEC -> r_mtvec,
        CsrAddr.MCOUNTEREN -> r_mcounteren,
        CsrAddr.MSCRATCH -> r_mscratch,
        CsrAddr.MEPC -> r_mepc,
        CsrAddr.MCAUSE -> r_mcause,
        CsrAddr.MTVAL -> r_mtval,
        CsrAddr.MIP -> r_mip))

    // Calculation
    val w_write_value = Wire(UInt(64.W))

    w_write_value := io.ex.operand
    switch (io.ex.cmd) {
        is (CsrCmd.SET) {
            w_write_value := w_read_value | io.ex.operand
        }
        is (CsrCmd.CLEAR) {
            w_write_value := w_read_value & (~io.ex.operand)
        }
    }

    // Register Write
    val w_we = Wire(Bool())

    w_we := io.ex.valid && (io.ex.cmd === CsrCmd.WRITE || io.ex.cmd === CsrCmd.SET || io.ex.cmd === CsrCmd.CLEAR)

    val w_mstatus_we    = Wire(Bool())
    val w_misa_we       = Wire(Bool())
    val w_medeleg_we    = Wire(Bool())
    val w_mideleg_we    = Wire(Bool())
    val w_mie_we        = Wire(Bool())
    val w_mtvec_we      = Wire(Bool())
    val w_mcounteren_we = Wire(Bool())
    val w_mscratch_we   = Wire(Bool())
    val w_mepc_we       = Wire(Bool())
    val w_mcause_we     = Wire(Bool())
    val w_mtval_we      = Wire(Bool())
    val w_mip_we        = Wire(Bool())

    w_mstatus_we    := w_we && io.ex.addr === CsrAddr.MSTATUS
    w_misa_we       := w_we && io.ex.addr === CsrAddr.MISA
    w_medeleg_we    := w_we && io.ex.addr === CsrAddr.MEDELEG
    w_mideleg_we    := w_we && io.ex.addr === CsrAddr.MIDELEG
    w_mie_we        := w_we && io.ex.addr === CsrAddr.MIE
    w_mtvec_we      := w_we && io.ex.addr === CsrAddr.MTVEC
    w_mcounteren_we := w_we && io.ex.addr === CsrAddr.MCOUNTEREN
    w_mscratch_we   := w_we && io.ex.addr === CsrAddr.MSCRATCH
    w_mepc_we       := w_we && io.ex.addr === CsrAddr.MEPC
    w_mcause_we     := w_we && io.ex.addr === CsrAddr.MCAUSE
    w_mtval_we      := w_we && io.ex.addr === CsrAddr.MTVAL
    w_mip_we        := w_we && io.ex.addr === CsrAddr.MIP

    r_mstatus := MuxCase(r_mstatus, Seq(
        io.trap.mstatus_we -> io.trap.mstatus_write_value,
        w_mstatus_we -> w_write_value))
    r_mepc := MuxCase(r_mepc, Seq(
        io.trap.mepc_we -> io.trap.mepc_write_value,
        w_mepc_we -> w_write_value))
    r_mcause := MuxCase(r_mcause, Seq(
        io.trap.mcause_we -> io.trap.mcause_write_value,
        w_mcause_we -> w_write_value))
    r_mtval := MuxCase(r_mtval, Seq(
        io.trap.mtval_we -> io.trap.mtval_write_value,
        w_mtval_we -> w_write_value))

    r_misa       := Mux(w_misa_we, w_write_value, r_misa)
    r_medeleg    := Mux(w_medeleg_we, w_write_value, r_medeleg)
    r_mideleg    := Mux(w_mideleg_we, w_write_value, r_mideleg)
    r_mie        := Mux(w_mie_we, w_write_value, r_mie)
    r_mtvec      := Mux(w_mtvec_we, w_write_value, r_mtvec)
    r_mcounteren := Mux(w_mcounteren_we, w_write_value, r_mcounteren)
    r_mscratch   := Mux(w_mscratch_we, w_write_value, r_mscratch)
    r_mip        := Mux(w_mip_we, w_write_value, r_mip)
   
    // IO
    io.ex.read_value := w_read_value
    io.trap.mstatus_read_value := r_mstatus
    io.trap.mtvec_read_value := r_mtvec
    io.trap.mepc_read_value := r_mepc
}
