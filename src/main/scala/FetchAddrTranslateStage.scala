package rafi

import chisel3._
import chisel3.util._

class FetchAddrTranslateStage extends Module {
    val io = IO(new Bundle {
        val prev_pc = Input(UInt(64.W))
        val next_pc = Output(UInt(64.W))
    })

    io.next_pc := RegNext(io.prev_pc, 0.U)
}
