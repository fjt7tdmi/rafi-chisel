package rafi

import chisel3._
import chisel3.util._
import scala.annotation.switch

class Alu extends Module {
    val io = IO(new Bundle {
        val cmd = Input(UInt(4.W))
        val is_word = Input(Bool())
        val src1_type = Input(AluSrc1Type())
        val src2_type = Input(AluSrc2Type())
        val pc = Input(UInt(64.W))
        val imm = Input(UInt(64.W))
        val rs1_value = Input(UInt(64.W))
        val rs2_value = Input(UInt(64.W))
        val result = Output(UInt(64.W))
    })

    // 64 bit calculation
    val w_src1_64 = Wire(UInt(64.W))
    val w_src2_64 = Wire(UInt(64.W))
    val w_result_64 = Wire(UInt(64.W))

    w_src1_64 := MuxCase(0.U, Seq(
        (io.src1_type === AluSrc1Type.REG) -> io.rs1_value,
        (io.src1_type === AluSrc1Type.PC) -> io.pc))
    w_src2_64 := MuxCase(0.U, Seq(
        (io.src2_type === AluSrc2Type.REG) -> io.rs2_value,
        (io.src2_type === AluSrc2Type.IMM) -> io.imm))

    w_result_64 := 0.U
    switch (io.cmd) {
        is (AluCmd.ADD) {
            w_result_64 := w_src1_64 + w_src2_64
        }
        is (AluCmd.SUB) {
            w_result_64 := w_src1_64 - w_src2_64
        }
        is (AluCmd.SLL) {
            w_result_64 := w_src1_64 << w_src2_64(5, 0)
        }
        is (AluCmd.SLT) {
            when (w_src1_64.asSInt() < w_src2_64.asSInt()) {
                w_result_64 := 1.U
            }
        }
        is (AluCmd.SLTU) {
            when (w_src1_64 < w_src2_64) {
                w_result_64 := 1.U
            }
        }
        is (AluCmd.XOR) {
            w_result_64 := w_src1_64 ^ w_src2_64
        }
        is (AluCmd.SRL) {
            w_result_64 := w_src1_64 >> w_src2_64(5, 0)
        }
        is (AluCmd.SRA) {
            w_result_64 := (w_src1_64.asSInt() >> w_src2_64(5, 0)).asUInt()
        }
        is (AluCmd.OR) {
            w_result_64 := w_src1_64 | w_src2_64
        }
        is (AluCmd.AND) {
            w_result_64 := w_src1_64 & w_src2_64
        }
    }

    // 32 bit calculation
    val w_src1_32 = Wire(UInt(32.W))
    val w_src2_32 = Wire(UInt(32.W))
    val w_result_32 = Wire(UInt(32.W))

    w_src1_32 := w_src1_64(31, 0)
    w_src2_32 := w_src2_64(31, 0)

    w_result_32 := 0.U
    switch (io.cmd) {
        is (AluCmd.ADD) {
            w_result_32 := w_src1_32 + w_src2_32
        }
        is (AluCmd.SUB) {
            w_result_32 := w_src1_32 - w_src2_32
        }
        is (AluCmd.SLL) {
            w_result_32 := w_src1_32 << w_src2_32(4, 0)
        }
        is (AluCmd.SLT) {
            when (w_src1_32.asSInt() < w_src2_32.asSInt()) {
                w_result_32 := 1.U
            }
        }
        is (AluCmd.SLTU) {
            when (w_src1_32 < w_src2_32) {
                w_result_32 := 1.U
            }
        }
        is (AluCmd.XOR) {
            w_result_32 := w_src1_32 ^ w_src2_32
        }
        is (AluCmd.SRL) {
            w_result_32 := w_src1_32 >> w_src2_32(4, 0)
        }
        is (AluCmd.SRA) {
            w_result_32 := (w_src1_32.asSInt() >> w_src2_32(4, 0)).asUInt()
        }
        is (AluCmd.OR) {
            w_result_32 := w_src1_32 | w_src2_32
        }
        is (AluCmd.AND) {
            w_result_32 := w_src1_32 & w_src2_32
        }
    }

    // Result
    io.result := Mux(io.is_word, Cat(Fill(32, w_result_32(31)), w_result_32), w_result_64)
}
