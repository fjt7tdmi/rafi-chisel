package rafi

import chisel3._
import chisel3.util._
import scala.annotation.switch

object  BranchUnit {
    val CMD_BEQ  = "b000".U(3.W)
    val CMD_BNE  = "b001".U(3.W)
    val CMD_BLT  = "b100".U(3.W)
    val CMD_BGE  = "b101".U(3.W)
    val CMD_BLTU = "b110".U(3.W)
    val CMD_BGEU = "b111".U(3.W)

    val SRC1_TYPE_ZERO = 0.U(2.W)
    val SRC1_TYPE_REG  = 1.U(2.W)
    val SRC1_TYPE_PC   = 2.U(2.W)
    
    val SRC2_TYPE_ZERO = 0.U(2.W)
    val SRC2_TYPE_REG  = 1.U(2.W)
    val SRC2_TYPE_IMM  = 2.U(2.W)
}

class BranchUnit extends Module {
    val io = IO(new Bundle {
        val cmd = Input(UInt(4.W))
        val src1_type = Input(UInt(2.W))
        val src2_type = Input(UInt(2.W))
        val pc = Input(UInt(64.W))
        val imm = Input(UInt(64.W))
        val rs1_value = Input(UInt(64.W))
        val rs2_value = Input(UInt(64.W))
        val rd_value = Output(UInt(64.W))
        val branch_taken = Output(Bool())
        val branch_target = Output(UInt(64.W))
    })

    val w_src1 = Wire(UInt(64.W))
    val w_src2 = Wire(UInt(64.W))

    w_src1 := MuxCase(0.U, Seq(
        (io.src1_type === Alu.SRC1_TYPE_REG) -> io.rs1_value,
        (io.src1_type === Alu.SRC1_TYPE_PC) -> io.pc))
    w_src2 := MuxCase(0.U, Seq(
        (io.src1_type === Alu.SRC2_TYPE_REG) -> io.rs2_value,
        (io.src1_type === Alu.SRC2_TYPE_IMM) -> io.imm))

    io.rd_value := io.pc

    io.branch_taken := 0.U
    switch (io.cmd) {
        is (BranchUnit.CMD_BEQ) {
            when (w_src1 === w_src2) {
                io.branch_taken := 1.U
            }
        }
        is (BranchUnit.CMD_BNE) {
            when (w_src1 != w_src2) {
                io.branch_taken := 1.U
            }
        }
        is (BranchUnit.CMD_BLT) {
            when (w_src1 < w_src2) {
                io.branch_taken := 1.U
            }
        }
        is (BranchUnit.CMD_BGE) {
            when (w_src1 >= w_src2) {
                io.branch_taken := 1.U
            }
        }
        is (BranchUnit.CMD_BLTU) {
            when (w_src1.asSInt() < w_src2.asSInt()) {
                io.branch_taken := 1.U
            }
        }
        is (BranchUnit.CMD_BGEU) {
            when (w_src1.asSInt() >= w_src2.asSInt()) {
                io.branch_taken := 1.U
            }
        }
    }

    when (io.branch_taken) {
        io.branch_target := io.pc + io.imm
    } .otherwise {
        io.branch_target := io.pc + 4.U
    }
}
