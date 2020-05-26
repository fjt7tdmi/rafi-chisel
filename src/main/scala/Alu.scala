package rafi

import chisel3._
import chisel3.util._
import scala.annotation.switch

object  Alu {
    val CMD_ADD  = "b0000".U(4.W)
    val CMD_SUB  = "b1000".U(4.W)
    val CMD_SLL  = "b0001".U(4.W)
    val CMD_SLT  = "b0010".U(4.W)
    val CMD_SLTU = "b0011".U(4.W)
    val CMD_XOR  = "b0100".U(4.W)
    val CMD_SRL  = "b0101".U(4.W)
    val CMD_SRA  = "b1101".U(4.W)
    val CMD_OR   = "b0110".U(4.W)
    val CMD_AND  = "b0111".U(4.W)
}

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
    val src1_64 = Wire(UInt(64.W))
    val src2_64 = Wire(UInt(64.W))
    val result_64 = Wire(UInt(64.W))

    src1_64 := MuxCase(0.U, Seq(
        (io.src1_type === AluSrc1Type.REG) -> io.rs1_value,
        (io.src1_type === AluSrc1Type.PC) -> io.pc))
    src2_64 := MuxCase(0.U, Seq(
        (io.src2_type === AluSrc2Type.REG) -> io.rs2_value,
        (io.src2_type === AluSrc2Type.IMM) -> io.imm))

    result_64 := 0.U
    switch (io.cmd) {
        is (Alu.CMD_ADD) {
            result_64 := src1_64 + src2_64
        }
        is (Alu.CMD_SUB) {
            result_64 := src1_64 - src2_64
        }
        is (Alu.CMD_SLL) {
            result_64 := src1_64 << src2_64(5, 0)
        }
        is (Alu.CMD_SLT) {
            when (src1_64.asSInt() < src2_64.asSInt()) {
                result_64 := 1.U
            }
        }
        is (Alu.CMD_SLTU) {
            when (src1_64 < src2_64) {
                result_64 := 1.U
            }
        }
        is (Alu.CMD_XOR) {
            result_64 := src1_64 ^ src2_64
        }
        is (Alu.CMD_SRL) {
            result_64 := src1_64 >> src2_64(5, 0)
        }
        is (Alu.CMD_SRA) {
            result_64 := (src1_64.asSInt() >> src2_64(5, 0)).asUInt()
        }
        is (Alu.CMD_OR) {
            result_64 := src1_64 | src2_64
        }
        is (Alu.CMD_AND) {
            result_64 := src1_64 & src2_64
        }
    }

    // 32 bit calculation
    val src1_32 = Wire(UInt(32.W))
    val src2_32 = Wire(UInt(32.W))
    val result_32 = Wire(UInt(32.W))

    src1_32 := src1_64(31, 0)
    src2_32 := src2_64(31, 0)

    result_32 := 0.U
    switch (io.cmd) {
        is (Alu.CMD_ADD) {
            result_32 := src1_32 + src2_32
        }
        is (Alu.CMD_SUB) {
            result_32 := src1_32 - src2_32
        }
        is (Alu.CMD_SLL) {
            result_32 := src1_32 << src2_32(4, 0)
        }
        is (Alu.CMD_SLT) {
            when (src1_32.asSInt() < src2_32.asSInt()) {
                result_32 := 1.U
            }
        }
        is (Alu.CMD_SLTU) {
            when (src1_32 < src2_32) {
                result_32 := 1.U
            }
        }
        is (Alu.CMD_XOR) {
            result_32 := src1_32 ^ src2_32
        }
        is (Alu.CMD_SRL) {
            result_32 := src1_32 >> src2_32(4, 0)
        }
        is (Alu.CMD_SRA) {
            result_32 := (src1_32.asSInt() >> src2_32(4, 0)).asUInt()
        }
        is (Alu.CMD_OR) {
            result_32 := src1_32 | src2_32
        }
        is (Alu.CMD_AND) {
            result_32 := src1_32 & src2_32
        }
    }

    // Result
    io.result := Mux(io.is_word, Cat(Fill(32, result_32(31)), result_32), result_64)
}
