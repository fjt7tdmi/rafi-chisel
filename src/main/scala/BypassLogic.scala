package rafi

import chisel3._
import chisel3.util._

class BypassLogic extends Module {
    val DEPTH = 2
    val io = IO(new Bundle {
        val flush = Input(Bool())
        val valid = Input(Bool())
        val rd = Input(UInt(5.W))
        val rs1 = Input(UInt(5.W))
        val rs2 = Input(UInt(5.W))
        val rd_value = Input(UInt(64.W))
        val rs1_value = Output(UInt(64.W))
        val rs2_value = Output(UInt(64.W))
        val rs1_hit = Output(Bool())
        val rs2_hit = Output(Bool())
    })

    val r_valids = Seq.fill(DEPTH)(RegInit(0.U(1.W)))
    val r_rds = Seq.fill(DEPTH)(RegInit(0.U(5.W)))
    val r_values = Seq.fill(DEPTH)(RegInit(0.U(64.W)))

    for (i <- 0 until DEPTH) {
        if (i == 0) {
            r_valids(i) := Mux(io.flush, 0.U, io.valid)
            r_rds(i) := io.rd
            r_values(i) := io.rd_value
        } else {
            r_valids(i) := Mux(io.flush, 0.U, r_valids(i - 1))
            r_rds(i) := r_rds(i - 1)
            r_values(i) := r_values(i - 1)
        }
    }

    io.rs1_hit := 0.U
    io.rs1_value := 0.U
    for (i <- (0 until DEPTH).reverse) {
        when (r_valids(i) === 1.U && r_rds(i) === io.rs1) {
            io.rs1_hit := 1.U
            io.rs1_value := r_values(i)
        }
    }

    io.rs2_hit := 0.U
    io.rs2_value := 0.U
    for (i <- (0 until DEPTH).reverse) {
        when (r_valids(i) === 1.U && r_rds(i) === io.rs2) {
            io.rs2_hit := 1.U
            io.rs2_value := r_values(i)
        }
    }
}
