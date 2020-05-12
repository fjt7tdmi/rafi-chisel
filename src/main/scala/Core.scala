package rafi

import chisel3._
import chisel3.util._

object Config {
    val INITIAL_PC = 0.U
    val ICACHE_SIZE = 32 * 1024
}

class Core extends Module {
    val io = IO(new Bundle {
        val pc = Output(UInt(64.W))
    })

    // FetchUnit
    val pp = Module(new PcPredictStage)
    val pt = Module(new PcTranslateStage)
    val ir = Module(new ICacheReadStage)
    val it = Module(new InsnTraversalStage)

    pp.io.next <> pt.io.prev
    pt.io.next <> ir.io.prev
    ir.io.next <> it.io.prev

    // Debug
    io.pc := pp.io.next.pc
}
