package rafi

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import firrtl.annotations.MemoryLoadFileType
import scala.annotation.switch

object  MemUnit {
    val ACCESS_SIZE_BYTE = 0.U(2.W)
    val ACCESS_SIZE_HALF_WORD = 1.U(2.W)
    val ACCESS_SIZE_WORD = 2.U(2.W)
    val ACCESS_SIZE_DOUBLE_WORD = 3.U(2.W)
}

class MemUnit(val dcache_hex_path: String) extends Module {
    val io = IO(new Bundle {
        val valid = Input(Bool())
        val cmd = Input(MemCmd())
        val is_signed = Input(Bool())
        val access_size = Input(UInt(2.W))
        val imm = Input(UInt(64.W))
        val rs1_value = Input(UInt(64.W))
        val rs2_value = Input(UInt(64.W))
        val result = Output(UInt(64.W))

        // Debug
        val host_io_value = Output(UInt(64.W))
    })

    val LINE_SIZE = 8
    val INDEX_WIDTH = log2Ceil(Config.DCACHE_SIZE / LINE_SIZE) 
    
    // Address calculation
    val w_addr = Wire(UInt(64.W))
    val w_index = Wire(UInt(INDEX_WIDTH.W))

    w_addr := io.rs1_value + io.imm
    w_index := w_addr(INDEX_WIDTH + 2, 3)

    // Mask
    val w_mask = Wire(UInt(LINE_SIZE.W))

    w_mask := 0.U

    switch (io.access_size) {
        is (MemUnit.ACCESS_SIZE_BYTE) {
            w_mask := "b0000_0001".U(8.W) << w_addr(2, 0)
        }
        is (MemUnit.ACCESS_SIZE_HALF_WORD) {
            w_mask := "b0000_0011".U(8.W) << w_addr(2, 0)
        }
        is (MemUnit.ACCESS_SIZE_WORD) {
            w_mask := "b0000_1111".U(8.W) << w_addr(2, 0)
        }
        is (MemUnit.ACCESS_SIZE_DOUBLE_WORD) {
            w_mask := "b1111_1111".U(8.W) << w_addr(2, 0)
        }
    }

    // Write value
    val w_write_value = Wire(Vec(LINE_SIZE, UInt(8.W)))

    w_write_value := (io.rs2_value << Cat(w_addr(2, 0), 0.U(3.W))).asTypeOf(Vec(LINE_SIZE, UInt(8.W)))

    // DCache (now, DCache is just a RAM)
    val m_dcache = Mem(Config.DCACHE_SIZE / LINE_SIZE, Vec(LINE_SIZE, UInt(8.W)))

    loadMemoryFromFile(m_dcache, dcache_hex_path, MemoryLoadFileType.Hex)

    when (io.valid && io.cmd === MemCmd.STORE) {
        m_dcache.write(w_index, w_write_value, w_mask.asBools())
    }

    // Read value
    val w_read_value = Wire(Vec(LINE_SIZE, UInt(8.W)))
    val w_read_value_masked = Wire(Vec(LINE_SIZE, UInt(8.W)))
    val w_read_value_shifted = Wire(UInt(64.W))
    val w_read_value_extended = Wire(UInt(64.W))

    w_read_value := m_dcache.read(w_index)

    for (i <- 0 until LINE_SIZE) {
        w_read_value_masked(i) := Mux(w_mask(i), w_read_value(i), 0.U)
    }

    w_read_value_shifted := Cat(w_read_value_masked.reverse) >> Cat(w_addr(2, 0), 0.U(3.W))

    w_read_value_extended := w_read_value_shifted
    when (io.is_signed) {
        switch (io.access_size) {
            is (MemUnit.ACCESS_SIZE_BYTE) {
                w_read_value_extended := Cat(Fill(56, w_read_value_shifted(7)), w_read_value_shifted(7, 0))
            }
            is (MemUnit.ACCESS_SIZE_HALF_WORD) {
                w_read_value_extended := Cat(Fill(48, w_read_value_shifted(15)), w_read_value_shifted(15, 0))
            }
            is (MemUnit.ACCESS_SIZE_WORD) {
                w_read_value_extended := Cat(Fill(32, w_read_value_shifted(31)), w_read_value_shifted(31, 0))
            }
        }
    }

    // Result
    io.result := w_read_value_extended

    // Debug
    io.host_io_value := Mux(io.valid && io.cmd === MemCmd.STORE && w_addr === Config.HOST_IO_ADDR, w_write_value.asTypeOf(UInt(64.W)), 0.U)
}
