package rafi

import chisel3._
import chisel3.util._

class RegWriteStage extends Module {
    val io = IO(new Bundle {
        val ctrl = Flipped(new PipelineControllerIF.RW)
        val prev = Flipped(new ExecuteStageIF)
        val reg_file = Flipped(new RegFileWriteIF)
    })

    // Pipeline controller
    io.ctrl.flush := io.prev.branch_taken
    io.ctrl.flush_target := io.prev.branch_target
    when (!io.prev.valid) {
        io.ctrl.flush := 0.U
    }    

    // Register file
    io.reg_file.rd := io.prev.rd
    io.reg_file.value := io.prev.reg_write_value
    io.reg_file.write_enable := io.prev.reg_write_enable
    when (!io.prev.valid) {
        io.reg_file.write_enable := 0.U
    }    
}
