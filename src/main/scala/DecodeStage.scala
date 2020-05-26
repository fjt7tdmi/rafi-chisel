package rafi

import chisel3._
import chisel3.util._
import chisel3.experimental.ChiselEnum
import scala.annotation.switch

object ImmType extends ChiselEnum {
    val zero = Value(0.U)
    val u = Value(1.U)
    val j = Value(2.U)
    val i = Value(3.U)
    val b = Value(4.U)
    val shift = Value(5.U)
    val store = Value(6.U)
}

class DecodeStageIF extends Bundle {
    val valid = Output(Bool())
    val pc = Output(UInt(64.W))
    val insn = Output(UInt(32.W))
    val trap_enter = Output(Bool())
    val trap_return = Output(Bool())
    val trap_cause = Output(UInt(4.W))
    val trap_value = Output(UInt(64.W))
    val execute_unit = Output(UInt(3.W))
    val reg_write_enable = Output(Bool())
    val rd = Output(UInt(5.W))
    val rs1 = Output(UInt(5.W))
    val rs2 = Output(UInt(5.W))
    val alu_cmd = Output(UInt(4.W))
    val alu_is_word = Output(Bool())
    val alu_src1_type = Output(UInt(2.W))
    val alu_src2_type = Output(UInt(2.W))
    val branch_cmd = Output(UInt(3.W))
    val branch_always = Output(Bool())
    val branch_relative = Output(Bool())
    val csr_cmd = Output(UInt(2.W))
    val csr_addr = Output(UInt(12.W))
    val csr_use_imm = Output(Bool())
    val mem_cmd = Output(UInt(2.W))
    val mem_is_signed = Output(Bool())
    val mem_access_size = Output(UInt(2.W))
    val imm = Output(UInt(64.W))
}

class DecodeStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.ID)
        val prev = Flipped(new InsnTraversalStageIF)
        val next = new DecodeStageIF
    })

    val w_valid = Wire(Bool())
    val w_insn = Wire(UInt(32.W))
    
    w_valid := io.prev.valid
    w_insn := io.prev.insn

    when (io.ctrl.flush) {
        w_valid := 0.U
    }

    // Decoder
    val w_opcode = Wire(UInt(7.W))

    w_opcode := w_insn(6, 0)

    val w_funct7 = Wire(UInt(7.W))
    val w_rs2 = Wire(UInt(5.W))
    val w_rs1 = Wire(UInt(5.W))
    val w_funct3 = Wire(UInt(3.W))
    val w_rd = Wire(UInt(5.W))

    w_funct7 := w_insn(31, 25)
    w_rs2 := w_insn(24, 20)
    w_rs1 := w_insn(19, 15)
    w_funct3 := w_insn(14, 12)
    w_rd := w_insn(11, 7)

    val w_unknown = Wire(Bool())
    val w_execute_unit = Wire(UInt(3.W))
    val w_reg_write_enable = Wire(Bool())
    val w_alu_cmd = Wire(UInt(4.W))
    val w_alu_is_word = Wire(Bool())
    val w_alu_src1_type = Wire(UInt(2.W))
    val w_alu_src2_type = Wire(UInt(2.W))
    val w_branch_cmd = Wire(UInt(3.W))
    val w_branch_always = Wire(Bool())
    val w_branch_relative = Wire(Bool())
    val w_csr_cmd = Wire(UInt(2.W))
    val w_csr_addr = Wire(UInt(12.W))
    val w_csr_use_imm = Wire(Bool())
    val w_mem_cmd = Wire(UInt(2.W))
    val w_mem_is_signed = Wire(Bool())
    val w_mem_access_size = Wire(UInt(2.W))
    val w_imm_type = Wire(ImmType())
    val w_trap_enter = Wire(Bool())
    val w_trap_return = Wire(Bool())
    val w_trap_cause = Wire(UInt(4.W))
    val w_trap_value = Wire(UInt(64.W))

    w_unknown := 1.U
    w_execute_unit := ExecuteStage.UNIT_ALU
    w_reg_write_enable := 0.U
    w_alu_cmd := Alu.CMD_ADD
    w_alu_is_word := 0.U
    w_alu_src1_type := Alu.SRC1_TYPE_ZERO
    w_alu_src2_type := Alu.SRC2_TYPE_ZERO
    w_branch_cmd := BranchUnit.CMD_BEQ
    w_branch_always := 0.U
    w_branch_relative := 0.U
    w_csr_cmd := Csr.CMD_NONE
    w_csr_addr := w_insn(31, 20)
    w_csr_use_imm := 0.U
    w_mem_cmd := MemUnit.CMD_NONE
    w_mem_is_signed := 0.U
    w_mem_access_size := MemUnit.ACCESS_SIZE_BYTE
    w_imm_type := ImmType.zero
    w_trap_enter := 0.U
    w_trap_return := 0.U
    w_trap_cause := 0.U
    w_trap_value := 0.U

    switch (w_opcode) {
        // RV64I
        is ("b0110111".U) {
            // lui
            w_unknown := 0.U
            w_execute_unit := ExecuteStage.UNIT_ALU
            w_reg_write_enable := 1.U
            w_alu_cmd := Alu.CMD_ADD
            w_alu_is_word := 1.U
            w_alu_src1_type := Alu.SRC1_TYPE_ZERO
            w_alu_src2_type := Alu.SRC2_TYPE_IMM
            w_imm_type := ImmType.u
        }
        is ("b0010111".U) {
            // auipc
            w_unknown := 0.U
            w_execute_unit := ExecuteStage.UNIT_ALU
            w_reg_write_enable := 1.U
            w_alu_cmd := Alu.CMD_ADD
            w_alu_src1_type := Alu.SRC1_TYPE_PC
            w_alu_src2_type := Alu.SRC2_TYPE_IMM
            w_imm_type := ImmType.u
        }
        is ("b1101111".U) {
            // jal
            w_unknown := 0.U
            w_execute_unit := ExecuteStage.UNIT_BRANCH
            w_reg_write_enable := 1.U
            w_branch_always := 1.U
            w_imm_type := ImmType.j
        }
        is ("b1100111".U) {
            // jalr
            when (w_funct3 === 0.U) {
                w_unknown := 0.U
                w_execute_unit := ExecuteStage.UNIT_BRANCH
                w_reg_write_enable := 1.U
                w_branch_always := 1.U
                w_branch_relative := 1.U
                w_imm_type := ImmType.i
            }
        }
        is ("b1100011".U) {
            // beq, bne, blt, bge, bltu, bgeu
            when (w_funct3(2, 1) != "b01".U) {
                w_unknown := 0.U
                w_execute_unit := ExecuteStage.UNIT_BRANCH
                w_branch_cmd := w_funct3
                w_imm_type := ImmType.b
            }
        }
        is ("b0000011".U) {
            // lb, lh, lw, ld, lbu, lhu, lwu
            when (w_funct3 != "b111".U) {
                w_unknown := 0.U
                w_execute_unit := ExecuteStage.UNIT_MEM
                w_reg_write_enable := 1.U
                w_mem_cmd := MemUnit.CMD_LOAD
                w_mem_is_signed := ~w_funct3(2)
                w_mem_access_size := w_funct3(1, 0)
                w_imm_type := ImmType.i
            }
        }
        is ("b0100011".U) {
            // sb, sh, sw, sd
            when (w_funct3(2) === 0.U) {
                w_unknown := 0.U
                w_execute_unit := ExecuteStage.UNIT_MEM
                w_mem_cmd := MemUnit.CMD_STORE
                w_mem_access_size := w_funct3(1, 0)
                w_imm_type := ImmType.store
            }
        }
        is ("b0010011".U) {
            w_execute_unit := ExecuteStage.UNIT_ALU
            w_reg_write_enable := 1.U
            w_alu_src1_type := Alu.SRC1_TYPE_REG
            w_alu_src2_type := Alu.SRC2_TYPE_IMM

            when (
                (w_funct3 === "b001".U && w_funct7 === "b0000000".U) ||
                (w_funct3 === "b101".U && w_funct7 === "b0000000".U) ||
                (w_funct3 === "b101".U && w_funct7 === "b0100000".U)) {
                // slli, srli, srai
                w_unknown := 0.U
                w_alu_cmd := Cat(w_funct7(5), w_funct3)
                w_imm_type := ImmType.shift
            } .otherwise {
                // addi, slti, sltiu, xori, ori, andi
                w_unknown := 0.U
                w_alu_cmd := Cat(0.U(1.W), w_funct3)
                w_imm_type := ImmType.i
            }
        }
        is ("b0011011".U) {
            w_execute_unit := ExecuteStage.UNIT_ALU
            w_reg_write_enable := 1.U
            w_alu_is_word := 1.U
            w_alu_src1_type := Alu.SRC1_TYPE_REG
            w_alu_src2_type := Alu.SRC2_TYPE_IMM

            when (w_funct3 === "b000".U) {
                // addiw
                w_unknown := 0.U
                w_alu_cmd := Alu.CMD_ADD
                w_imm_type := ImmType.i
            } .elsewhen (
                (w_funct3 === "b001".U && w_funct7 === "b0000000".U) ||
                (w_funct3 === "b101".U && w_funct7 === "b0000000".U) ||
                (w_funct3 === "b101".U && w_funct7 === "b0100000".U)) {
                // slliw, srliw, sraiw
                w_unknown := 0.U
                w_alu_cmd := Cat(w_funct7(5), w_funct3)
                w_imm_type := ImmType.shift
            }
        }
        is ("b0110011".U) {
            // add, sub, sll, slt, sltu, xor, srl, sra, or, and
            w_unknown := 0.U
            w_execute_unit := ExecuteStage.UNIT_ALU
            w_reg_write_enable := 1.U
            w_alu_cmd := Cat(w_funct7(5), w_funct3)
            w_alu_src1_type := Alu.SRC1_TYPE_REG
            w_alu_src2_type := Alu.SRC2_TYPE_REG
        }
        is ("b0111011".U) {
            // addw, subw, sllw, srlw, sraw
            w_execute_unit := ExecuteStage.UNIT_ALU
            w_reg_write_enable := 1.U
            w_alu_is_word := 1.U
            w_alu_cmd := Cat(w_funct7(5), w_funct3)
            w_alu_src1_type := Alu.SRC1_TYPE_REG
            w_alu_src2_type := Alu.SRC2_TYPE_REG

            when (
                (w_funct3 === "b000".U && w_funct7 === "b0000000".U) ||
                (w_funct3 === "b000".U && w_funct7 === "b0100000".U) ||
                (w_funct3 === "b001".U && w_funct7 === "b0000000".U) ||
                (w_funct3 === "b101".U && w_funct7 === "b0000000".U) ||
                (w_funct3 === "b101".U && w_funct7 === "b0100000".U)) {
                w_unknown := 0.U
            }
        }
        is ("b0001111".U) {
            when (w_funct3 === "b000".U) {
                // fence
                when (w_insn(31, 28) === 0.U && w_rs1 === 0.U && w_rs2 === 0.U) {
                    w_unknown := 0.U
                }
            } .elsewhen (w_funct3 === "b001".U) {
                // fence.i
                when (w_insn(31, 20) === 0.U && w_rs1 === 0.U && w_rs2 === 0.U) {
                    w_unknown := 0.U
                }
            }
        }
        is ("b1110011".U) {
            switch (w_funct3) {
                is ("b000".U) {
                    // ecall
                    when (w_funct7 === "b0000000".U && w_rs2 === "b00000".U && w_rs1 === "b00000".U && w_rd === "b00000".U) {
                        w_unknown := 0.U
                        w_execute_unit := ExecuteStage.UNIT_TRAP
                        w_trap_enter := 1.U
                        w_trap_cause := 11.U // ECALL_FROM_M
                        w_trap_value := io.prev.pc
                    }
                    // ebreak
                    when (w_funct7 === "b0000000".U && w_rs2 === "b00001".U && w_rs1 === "b00000".U && w_rd === "b00000".U) {
                        w_unknown := 0.U
                        w_execute_unit := ExecuteStage.UNIT_TRAP
                        w_trap_enter := 1.U
                        w_trap_cause := 3.U // BREAKPOINT
                        w_trap_value := io.prev.pc
                    }
                    // mret
                    when (w_funct7 === "b0011000".U && w_rs2 === "b00010".U && w_rs1 === "b00000".U && w_rd === "b00000".U) {
                        w_unknown := 0.U
                        w_execute_unit := ExecuteStage.UNIT_TRAP
                        w_trap_return := 1.U
                    }
                }
                // csrrw
                is ("b001".U) {
                    w_unknown := 0.U
                    w_execute_unit := ExecuteStage.UNIT_CSR
                    w_reg_write_enable := 1.U
                    w_csr_cmd := Csr.CMD_WRITE
                    w_csr_use_imm := 0.U
                }
                // csrrs
                is ("b010".U) {
                    w_unknown := 0.U
                    w_execute_unit := ExecuteStage.UNIT_CSR
                    w_reg_write_enable := 1.U
                    w_csr_cmd := Csr.CMD_CLEAR
                    w_csr_use_imm := 0.U
                }
                // csrrc
                is ("b011".U) {
                    w_unknown := 0.U
                    w_execute_unit := ExecuteStage.UNIT_CSR
                    w_reg_write_enable := 1.U
                    w_csr_cmd := Csr.CMD_SET
                    w_csr_use_imm := 0.U
                }
                // csrrwi
                is ("b101".U) {
                    w_unknown := 0.U
                    w_execute_unit := ExecuteStage.UNIT_CSR
                    w_reg_write_enable := 1.U
                    w_csr_cmd := Csr.CMD_WRITE
                    w_csr_use_imm := 1.U
                }
                // csrrsi
                is ("b110".U) {
                    w_unknown := 0.U
                    w_execute_unit := ExecuteStage.UNIT_CSR
                    w_reg_write_enable := 1.U
                    w_csr_cmd := Csr.CMD_CLEAR
                    w_csr_use_imm := 1.U
                }
                // csrrci
                is ("b111".U) {
                    w_unknown := 0.U
                    w_execute_unit := ExecuteStage.UNIT_CSR
                    w_reg_write_enable := 1.U
                    w_csr_cmd := Csr.CMD_SET
                    w_csr_use_imm := 1.U
                }
            }
        }
    }

    val w_imm_u = Wire(UInt(64.W))
    val w_imm_j = Wire(UInt(64.W))
    val w_imm_i = Wire(UInt(64.W))
    val w_imm_b = Wire(UInt(64.W))
    val w_imm_shift = Wire(UInt(64.W))
    val w_imm_store = Wire(UInt(64.W))

    w_imm_u := Cat(Fill(32, w_insn(31)), w_insn(31, 12), 0.U(12.W))
    w_imm_j := Cat(Fill(43, w_insn(31)), w_insn(31), w_insn(19, 12), w_insn(20), w_insn(30, 21), 0.U(1.W))
    w_imm_i := Cat(Fill(52, w_insn(31)), w_insn(31, 20))
    w_imm_b := Cat(Fill(53, w_insn(31)), w_insn(31), w_insn(7), w_insn(30, 25), w_insn(11, 8), 0.U(1.W))
    w_imm_shift := Cat(Fill(59, 0.U), w_insn(24, 20))
    w_imm_store := Cat(Fill(52, w_insn(31)), w_insn(31, 25), w_insn(11, 7))

    val w_imm = Wire(UInt(64.W))

    w_imm := MuxCase(0.U, Seq(
        (w_imm_type === ImmType.u) -> w_imm_u,
        (w_imm_type === ImmType.j) -> w_imm_j,
        (w_imm_type === ImmType.i) -> w_imm_i,
        (w_imm_type === ImmType.b) -> w_imm_b,
        (w_imm_type === ImmType.shift) -> w_imm_shift,
        (w_imm_type === ImmType.store) -> w_imm_store))

    // Pipeline register
    io.next.valid := RegNext(w_valid, 0.U)
    io.next.pc := RegNext(io.prev.pc, 0.U)
    io.next.insn := RegNext(w_insn, 0.U)
    io.next.trap_enter := RegNext(w_trap_enter, 0.U)
    io.next.trap_return := RegNext(w_trap_return, 0.U)
    io.next.trap_cause := RegNext(w_trap_cause, 0.U)
    io.next.trap_value := RegNext(w_trap_value, 0.U)
    io.next.execute_unit := RegNext(w_execute_unit, 0.U)
    io.next.reg_write_enable := RegNext(w_reg_write_enable, 0.U)
    io.next.rd := RegNext(w_rd, 0.U)
    io.next.rs1 := RegNext(w_rs1, 0.U)
    io.next.rs2 := RegNext(w_rs2, 0.U)
    io.next.alu_cmd := RegNext(w_alu_cmd, 0.U)
    io.next.alu_is_word := RegNext(w_alu_is_word, 0.U)
    io.next.alu_src1_type := RegNext(w_alu_src1_type, 0.U)
    io.next.alu_src2_type := RegNext(w_alu_src2_type, 0.U)
    io.next.branch_cmd := RegNext(w_branch_cmd, 0.U)
    io.next.branch_always := RegNext(w_branch_always, 0.U)
    io.next.branch_relative := RegNext(w_branch_relative, 0.U)
    io.next.csr_cmd := RegNext(w_csr_cmd, 0.U)
    io.next.csr_addr := RegNext(w_csr_addr, 0.U)
    io.next.csr_use_imm := RegNext(w_csr_use_imm, 0.U)
    io.next.mem_cmd := RegNext(w_mem_cmd, 0.U)
    io.next.mem_is_signed := RegNext(w_mem_is_signed, 0.U)
    io.next.mem_access_size := RegNext(w_mem_access_size, 0.U)
    io.next.imm := RegNext(w_imm, 0.U)
}
