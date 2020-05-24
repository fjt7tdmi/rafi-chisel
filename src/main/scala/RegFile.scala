package rafi

import chisel3._
import chisel3.util._

class RegFileReadIF extends Bundle {
    val rs1 = Input(UInt(5.W))
    val rs2 = Input(UInt(5.W))
    val rs1_value = Output(UInt(64.W))
    val rs2_value = Output(UInt(64.W))
}

class RegFileWriteIF extends Bundle {
    val write_enable = Input(Bool())
    val rd = Input(UInt(5.W))
    val value = Input(UInt(64.W))
}

class RegFile extends Module {
    val io = IO(new Bundle {
        val r = new RegFileReadIF
        val w = new RegFileWriteIF
    })

    val m_mem = Mem(32, UInt(64.W))

    io.r.rs1_value := m_mem(io.r.rs1)
    io.r.rs2_value := m_mem(io.r.rs2)

    when (io.w.write_enable && io.w.rd != 0.U) {
        m_mem.write(io.w.rd, io.w.value)
    }
}
