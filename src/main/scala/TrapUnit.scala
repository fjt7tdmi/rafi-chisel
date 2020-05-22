package rafi

import chisel3._
import chisel3.util._
import scala.annotation.switch

object  TrapUnit {
    val CMD_NONE   = 0.U(2.W)
    val CMD_ECALL  = 1.U(2.W)
    val CMD_EBREAK = 2.U(2.W)
    val CMD_MRET   = 3.U(2.W)
}
