package rafi

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class ICacheReadStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
}

class ICacheReadStage extends Module {
    val INDEX_WIDTH = log2Ceil(Config.ICACHE_SIZE / 4) 

    val io = IO(new Bundle {
        val prev = Flipped(new PcTranslateStageIF)
        val next = new ICacheReadStageIF
    })

    // Now, ICache is just a ROM.
    val m_icache = Mem(Config.ICACHE_SIZE / 4, UInt(32.W))

    val w_icache_index = Wire(UInt(INDEX_WIDTH.W))
    val w_insn = Wire(UInt(4.W))

    w_icache_index := io.prev.pc(INDEX_WIDTH + 1, 2)
    w_insn := m_icache(w_icache_index)

    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(w_insn, 0.U)

    loadMemoryFromFile(m_icache, "hex/rv64ui-p-add.hex")
}
