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
}

class BranchUnit extends Module {
    val io = IO(new Bundle {
        val cmd = Input(UInt(3.W))
        val is_relative = Input(Bool())
        val is_always = Input(Bool())
        val pc = Input(UInt(64.W))
        val imm = Input(UInt(64.W))
        val rs1_value = Input(UInt(64.W))
        val rs2_value = Input(UInt(64.W))
        val rd_value = Output(UInt(64.W))
        val taken = Output(Bool())
        val target = Output(UInt(64.W))
    })

    io.rd_value := io.pc

    io.taken := 0.U
    switch (io.cmd) {
        is (BranchUnit.CMD_BEQ) {
            when (io.rs1_value === io.rs2_value) {
                io.taken := 1.U
            }
        }
        is (BranchUnit.CMD_BNE) {
            when (io.rs1_value != io.rs2_value) {
                io.taken := 1.U
            }
        }
        is (BranchUnit.CMD_BLT) {
            when (io.rs1_value < io.rs2_value) {
                io.taken := 1.U
            }
        }
        is (BranchUnit.CMD_BGE) {
            when (io.rs1_value >= io.rs2_value) {
                io.taken := 1.U
            }
        }
        is (BranchUnit.CMD_BLTU) {
            when (io.rs1_value.asSInt() < io.rs2_value.asSInt()) {
                io.taken := 1.U
            }
        }
        is (BranchUnit.CMD_BGEU) {
            when (io.rs1_value.asSInt() >= io.rs2_value.asSInt()) {
                io.taken := 1.U
            }
        }
    }
    when (io.is_always) {
        io.taken := 1.U
    }

    when (io.taken && io.is_relative) {
        io.target := io.rs1_value + io.imm
    } .elsewhen (io.taken && !io.is_relative) {
        io.target := io.pc + io.imm
    } .otherwise {
        io.target := io.pc + 4.U
    }
}
