package rafi

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum
import scala.annotation.switch

class DecodeStageIF extends Bundle {
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val reg_write_enable = Output(Bool())
    val rd = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val alu_cmd = Output(UInt(4.W))
    val alu_src1_type = Output(UInt(2.W))
    val alu_src2_type = Output(UInt(2.W))
    val imm = Output(UInt(64.W))
}

object ImmType extends ChiselEnum {
    val zero = Value(0.U)
    val u = Value(1.U)
    val i = Value(2.U)
    val s = Value(3.U)
}

class DecodeStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new InsnTraversalStageIF)
        val next = new DecodeStageIF
    })

    // Decoder
    val w_insn = Wire(UInt(32.W))
    
    w_insn := io.prev.insn

    val w_opcode = Wire(UInt(7.W))

    w_opcode := w_insn(6, 0)

    val w_rd = Wire(UInt(5.W))
    val w_rs1 = Wire(UInt(5.W))
    val w_rs2 = Wire(UInt(5.W))

    w_rd := w_insn(11, 7)
    w_rs1 := w_insn(19, 15)
    w_rs2 := w_insn(24, 20)

    val w_reg_write_enable = Wire(Bool())
    val w_alu_cmd = Wire(UInt(4.W))
    val w_alu_src1_type = Wire(UInt(2.W))
    val w_alu_src2_type = Wire(UInt(2.W))
    val w_imm_type = Wire(ImmType())

    w_reg_write_enable := 0.U
    w_alu_cmd := Alu.CMD_ADD
    w_alu_src1_type := Alu.SRC1_TYPE_ZERO
    w_alu_src2_type := Alu.SRC2_TYPE_ZERO
    w_imm_type := ImmType.zero

    switch (w_opcode) {
        is ("b0110111".U) {
            // lui
            w_reg_write_enable := 1.U
            w_alu_cmd := Alu.CMD_ADD
            w_alu_src1_type := Alu.SRC1_TYPE_ZERO
            w_alu_src2_type := Alu.SRC2_TYPE_IMM
            w_imm_type := ImmType.u
        }
        is ("b0010111".U) {
            // auipc
            w_reg_write_enable := 1.U
            w_alu_cmd := Alu.CMD_ADD
            w_alu_src1_type := Alu.SRC1_TYPE_PC
            w_alu_src2_type := Alu.SRC2_TYPE_IMM
            w_imm_type := ImmType.u
        }
        is ("b0010011".U) {
            w_reg_write_enable := 1.U
            w_alu_cmd := Cat(0.U(1.W), w_insn(14, 12))
            w_alu_src1_type := Alu.SRC1_TYPE_REG
            w_alu_src2_type := Alu.SRC2_TYPE_IMM
            when (w_insn(13, 12) === "b01".U) {
                // slli, srli, srai
                w_imm_type := ImmType.s
            } .otherwise {
                // addi, slti, sltiu, xori, ori, andi
                w_imm_type := ImmType.i
            }
        }
        is ("b0110011".U) {
            // add, sub, sll, slt, sltu, xor, srl, sra, or, and
            w_reg_write_enable := 1.U
            w_alu_cmd := Cat(w_insn(30), w_insn(14, 12))
            w_alu_src1_type := Alu.SRC1_TYPE_REG
            w_alu_src2_type := Alu.SRC2_TYPE_REG
            w_imm_type := ImmType.zero
        }
    }

    val w_imm_u = Wire(UInt(64.W))
    val w_imm_i = Wire(UInt(64.W))
    val w_imm_s = Wire(UInt(64.W))

    w_imm_u := Cat(Fill(32, w_insn(31,12)), w_insn(31, 12), 0.U(12.W))
    w_imm_i := Cat(Fill(52, w_insn(31)), w_insn(31, 20))
    w_imm_s := Cat(Fill(59, 0.U), w_insn(24, 20))

    val w_imm = Wire(UInt(64.W))

    w_imm := MuxCase(0.U, Seq(
        (w_imm_type === ImmType.u) -> w_imm_u,
        (w_imm_type === ImmType.i) -> w_imm_i,
        (w_imm_type === ImmType.s) -> w_imm_s))

    // Pipeline register
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(io.prev.insn, 0.U)
    io.next.reg_write_enable := RegNext(w_reg_write_enable, 0.U)
    io.next.rd := RegNext(w_rd, 0.U)
    io.next.rs1 := RegNext(w_rs1, 0.U)
    io.next.rs2 := RegNext(w_rs2, 0.U)
    io.next.alu_cmd := RegNext(w_alu_cmd, 0.U)
    io.next.alu_src1_type := RegNext(w_alu_src1_type, 0.U)
    io.next.alu_src2_type := RegNext(w_alu_src2_type, 0.U)
    io.next.imm := RegNext(w_imm, 0.U)
}
