package rafi

import chisel3._
import chisel3.util._

object Config {
    val INITIAL_PC = 0.U
}

class Core extends Module {
    val io = IO(new Bundle {
        val pc = Output(UInt(64.W))
    })

    // FetchUnit
    val fetch_addr_generate_stage = Module(new FetchAddrGenerateStage)
    val fetch_addr_translate_stage = Module(new FetchAddrTranslateStage)
    val icache_read_stage = Module(new FetchAddrTranslateStage)
    val insn_traversal_stage = Module(new FetchAddrTranslateStage)

    fetch_addr_generate_stage.io.next_pc <> fetch_addr_translate_stage.io.prev_pc
    fetch_addr_translate_stage.io.next_pc <> icache_read_stage.io.prev_pc
    icache_read_stage.io.next_pc <> insn_traversal_stage.io.prev_pc

    // Debug
    io.pc := fetch_addr_generate_stage.io.next_pc
}
