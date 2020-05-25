package rafi

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType

class ICacheReadStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
}

class ICacheReadStage(val icache_hex_path: String) extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.IR)
        val prev = Flipped(new PcTranslateStageIF)
        val next = new ICacheReadStageIF
    })

    val w_valid = Wire(Bool())

    w_valid := io.prev.valid

    when (io.ctrl.flush) {
        w_valid := 0.U
    }

    // ICache (now, ICache is just a ROM)
    val INDEX_WIDTH = log2Ceil(Config.ICACHE_SIZE / 4) 
    
    val m_icache = Mem(Config.ICACHE_SIZE / 4, UInt(32.W))

    loadMemoryFromFile(m_icache, icache_hex_path, MemoryLoadFileType.Hex)

    val w_icache_index = Wire(UInt(INDEX_WIDTH.W))
    val w_insn = Wire(UInt(32.W))

    w_icache_index := io.prev.pc(INDEX_WIDTH + 1, 2)
    w_insn := m_icache(w_icache_index)

    // Pipeline register
    io.next.valid := RegNext(w_valid, 0.U)
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(w_insn, 0.U)
}
