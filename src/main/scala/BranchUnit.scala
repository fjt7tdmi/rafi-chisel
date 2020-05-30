package rafi

import chisel3._
import chisel3.util._
import scala.annotation.switch

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

    io.rd_value := io.pc + 4.U

    io.taken := 0.U
    switch (io.cmd) {
        is (BranchCmd.BEQ) {
            when (io.rs1_value === io.rs2_value) {
                io.taken := 1.U
            }
        }
        is (BranchCmd.BNE) {
            when (io.rs1_value =/= io.rs2_value) {
                io.taken := 1.U
            }
        }
        is (BranchCmd.BLT) {
            when (io.rs1_value.asSInt() < io.rs2_value.asSInt()) {
                io.taken := 1.U
            }
        }
        is (BranchCmd.BGE) {
            when (io.rs1_value.asSInt() >= io.rs2_value.asSInt()) {
                io.taken := 1.U
            }
        }
        is (BranchCmd.BLTU) {
            when (io.rs1_value < io.rs2_value) {
                io.taken := 1.U
            }
        }
        is (BranchCmd.BGEU) {
            when (io.rs1_value >= io.rs2_value) {
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
