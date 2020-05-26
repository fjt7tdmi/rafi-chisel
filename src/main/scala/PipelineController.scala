package rafi

import chisel3._
import chisel3.util._

object PipelineControllerIF {
    class PP extends Bundle {
        val flush = Output(Bool())
        val flush_target = Output(UInt(64.W))
    }
    class PT extends Bundle {
        val flush = Output(Bool())
    }
    class IR extends Bundle {
        val flush = Output(Bool())
    }
    class IT extends Bundle {
        val flush = Output(Bool())
    }
    class ID extends Bundle {
        val flush = Output(Bool())
    }
    class RR extends Bundle {
        val flush = Output(Bool())
    }
    class EX extends Bundle {
        val flush = Output(Bool())
    }
    class RW extends Bundle {
        val flush = Input(Bool())
        val flush_target = Input(UInt(64.W))
    }
}

class PipelineController extends Module {
    val io = IO(new Bundle {
        val pp = new PipelineControllerIF.PP
        val pt = new PipelineControllerIF.PT
        val ir = new PipelineControllerIF.IR
        val it = new PipelineControllerIF.IT
        val id = new PipelineControllerIF.ID
        val rr = new PipelineControllerIF.RR
        val ex = new PipelineControllerIF.EX
        val rw = new PipelineControllerIF.RW
    })

    val w_flush = Wire(Bool())
    val w_flush_target = Wire(UInt(64.W))

    w_flush := io.rw.flush
    w_flush_target := io.rw.flush_target

    io.pp.flush := w_flush
    io.pp.flush_target := w_flush_target
    io.pt.flush := w_flush
    io.ir.flush := w_flush
    io.it.flush := w_flush
    io.id.flush := w_flush
    io.rr.flush := w_flush
    io.ex.flush := w_flush
}
