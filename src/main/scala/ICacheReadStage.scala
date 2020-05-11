package rafi

import chisel3._
import chisel3.util._

class ICacheReadStageIF extends Bundle {
    val pc = Output(UInt(64.W))
}

class ICacheReadStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new FetchAddrTranslateStageIF)
        val next = new ICacheReadStageIF
    })

    io.next.pc := RegNext(io.prev.pc, 0.U)
}
