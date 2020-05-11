package rafi

import chisel3._
import chisel3.util._

class FetchAddrTranslateStageIF extends Bundle {
    val pc = Output(UInt(64.W))
}

class FetchAddrTranslateStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new FetchAddrGenerateStageIF)
        val next = new FetchAddrTranslateStageIF
    })

    io.next.pc := RegNext(io.prev.pc, 0.U)
}
