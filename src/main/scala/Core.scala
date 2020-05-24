package rafi

import chisel3._
import chisel3.util._

object Config {
    val INITIAL_PC   = "h80000000".U
    val HOST_IO_ADDR = "h80001000".U
    val ICACHE_SIZE = 32 * 1024
    val DCACHE_SIZE = 32 * 1024
}

class Core extends Module {
    val io = IO(new Bundle {
        val valid = Output(Bool())
        val pc = Output(UInt(64.W))
        val host_io_value = Output(UInt(64.W))
    })

    // Pipeline
    val pp = Module(new PcPredictStage)
    val pt = Module(new PcTranslateStage)
    val ir = Module(new ICacheReadStage)
    val it = Module(new InsnTraversalStage)
    val id = Module(new DecodeStage)
    val rr = Module(new RegReadStage)
    val ex = Module(new ExecuteStage)
    val rw = Module(new RegWriteStage)

    pp.io.next <> pt.io.prev
    pt.io.next <> ir.io.prev
    ir.io.next <> it.io.prev
    it.io.next <> id.io.prev
    id.io.next <> rr.io.prev
    rr.io.next <> ex.io.prev
    ex.io.next <> rw.io.prev

    // Pipeline Controller
    val ctrl = Module(new PipelineController)

    pp.io.ctrl <> ctrl.io.pp
    pt.io.ctrl <> ctrl.io.pt
    ir.io.ctrl <> ctrl.io.ir
    it.io.ctrl <> ctrl.io.it
    id.io.ctrl <> ctrl.io.id
    rr.io.ctrl <> ctrl.io.rr
    ex.io.ctrl <> ctrl.io.ex
    rw.io.ctrl <> ctrl.io.rw

    // Register File
    val reg_file = Module(new RegFile)

    rr.io.reg_file <> reg_file.io.r
    rw.io.reg_file <> reg_file.io.w

    // CSR
    val csr = Module(new Csr)

    ex.io.csr <> csr.io.ex
    rw.io.csr <> csr.io.trap

    // Debug
    io.valid := rw.io.valid
    io.pc := rw.io.pc
    io.host_io_value := rw.io.host_io_value
}
