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

    val SRC1_TYPE_ZERO = 0.U(2.W)
    val SRC1_TYPE_REG  = 1.U(2.W)
    val SRC1_TYPE_PC   = 2.U(2.W)
    
    val SRC2_TYPE_ZERO = 0.U(2.W)
    val SRC2_TYPE_REG  = 1.U(2.W)
    val SRC2_TYPE_IMM  = 2.U(2.W)
}

class Alu extends Module {
    val io = IO(new Bundle {
        val cmd = Input(UInt(4.W))
        val src1_type = Input(UInt(2.W))
        val src2_type = Input(UInt(2.W))
        val pc = Input(UInt(64.W))
        val imm = Input(UInt(64.W))
        val rs1_value = Input(UInt(64.W))
        val rs2_value = Input(UInt(64.W))
        val result = Output(UInt(64.W))
    })

    val src1 = Wire(UInt(64.W))
    val src2 = Wire(UInt(64.W))

    src1 := MuxCase(0.U, Seq(
        (io.src1_type === Alu.SRC1_TYPE_REG) -> io.rs1_value,
        (io.src1_type === Alu.SRC1_TYPE_PC) -> io.pc))
    src2 := MuxCase(0.U, Seq(
        (io.src1_type === Alu.SRC2_TYPE_REG) -> io.rs2_value,
        (io.src1_type === Alu.SRC2_TYPE_IMM) -> io.imm))

    io.result := 0.U
    switch (io.cmd) {
        is (Alu.CMD_ADD) {
            io.result := src1 + src2
        }
        is (Alu.CMD_SUB) {
            io.result := src1 - src2
        }
        is (Alu.CMD_SLL) {
            io.result := src1 << src2(5, 0)
        }
        is (Alu.CMD_SLT) {
            when (src1.asSInt() < src2.asSInt()) {
                io.result := 1.U
            }
        }
        is (Alu.CMD_SLTU) {
            when (src1 < src2) {
                io.result := 1.U
            }
        }
        is (Alu.CMD_XOR) {
            io.result := src1 ^ src2
        }
        is (Alu.CMD_SRL) {
            io.result := src1 >> src2(5, 0)
        }
        is (Alu.CMD_SRA) {
            io.result := (src1.asSInt() >> src2(5, 0)).asUInt()
        }
        is (Alu.CMD_OR) {
            io.result := src1 | src2
        }
        is (Alu.CMD_AND) {
            io.result := src1 & src2
        }
    }
}
