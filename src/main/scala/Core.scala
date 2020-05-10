package rafi

import chisel3._
import chisel3.util._

class Core extends Module {
    val io = IO(new Bundle {
        val pc = Output(UInt(64.W))
    })

    val fetch_addr_generate_stage = Module(new FetchAddrGenerateStage)

    io.pc := fetch_addr_generate_stage.io.pc;
}
