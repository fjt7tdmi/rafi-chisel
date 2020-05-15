package rafi

import chisel3._
import chisel3.util._

class RegWriteStage extends Module {
    val io = IO(new Bundle {
        val prev = Flipped(new ExecuteStageIF)
        val reg_file = Flipped(new RegFileWriteIF)
    })

    io.reg_file.rd := io.prev.rd
    io.reg_file.value := io.prev.reg_write_value
    io.reg_file.write_enable := io.prev.reg_write_enable
}
