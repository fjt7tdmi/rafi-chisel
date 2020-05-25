package rafi

import chisel3._
import chisel3.iotesters._
import chisel3.util._

abstract class RVTest(val test_name: String, val enable_debug_print: Boolean = false) extends ChiselFlatSpec {    
    behavior of "Core"

    it should "Host IO value will be 1" in {
        val args = Array("-tn=Core", s"-td=work/${test_name}", "-tgvo=on", "-tbn=verilator")

        iotesters.Driver.execute(args, () => new Core(s"hex/${test_name}.hex")) {
            core => new PeekPokeTester(core) {
                reset()
                
                var cycle : Int = 0
                var end : Boolean = false

                while (!end && cycle < 1000) {
                    step(1)
                    cycle += 1

                    val valid = peek(core.io.valid)
                    val pc = peek(core.io.pc).toLong
                    val host_io_value = peek(core.io.host_io_value).toLong

                    if (enable_debug_print && valid == 1) {
                        println(f"0x$pc%016x $host_io_value%d")                        
                    }

                    end = valid == 1 && host_io_value != 0
                }

                expect(core.io.host_io_value, 1.U)
            }
        } should be (true)
    }
}

class RVTest_rv64ui_p_add extends RVTest("rv64ui-p-add")
class RVTest_rv64ui_p_addi extends RVTest("rv64ui-p-addi")
class RVTest_rv64ui_p_addiw extends RVTest("rv64ui-p-addiw")
class RVTest_rv64ui_p_addw extends RVTest("rv64ui-p-addw")
class RVTest_rv64ui_p_and extends RVTest("rv64ui-p-and")
class RVTest_rv64ui_p_andi extends RVTest("rv64ui-p-andi")
//class RVTest_rv64ui_p_auipc extends RVTest("rv64ui-p-auipc", true)
class RVTest_rv64ui_p_beq extends RVTest("rv64ui-p-beq")
//class RVTest_rv64ui_p_bge extends RVTest("rv64ui-p-bge", true)
class RVTest_rv64ui_p_bgeu extends RVTest("rv64ui-p-bgeu")
//class RVTest_rv64ui_p_blt extends RVTest("rv64ui-p-blt", true)
class RVTest_rv64ui_p_bltu extends RVTest("rv64ui-p-bltu")
class RVTest_rv64ui_p_bne extends RVTest("rv64ui-p-bne")
//class RVTest_rv64ui_p_fence_i extends RVTest("rv64ui-p-fence_i", true)
//class RVTest_rv64ui_p_jal extends RVTest("rv64ui-p-jal", true)
//class RVTest_rv64ui_p_jalr extends RVTest("rv64ui-p-jalr", true)
//class RVTest_rv64ui_p_lb extends RVTest("rv64ui-p-lb", true)
//class RVTest_rv64ui_p_lbu extends RVTest("rv64ui-p-lbu", true)
//class RVTest_rv64ui_p_ld extends RVTest("rv64ui-p-ld", true)
//class RVTest_rv64ui_p_lh extends RVTest("rv64ui-p-lh", true)
//class RVTest_rv64ui_p_lhu extends RVTest("rv64ui-p-lhu", true)
class RVTest_rv64ui_p_lui extends RVTest("rv64ui-p-lui")
//class RVTest_rv64ui_p_lw extends RVTest("rv64ui-p-lw", true)
//class RVTest_rv64ui_p_lwu extends RVTest("rv64ui-p-lwu", true)
class RVTest_rv64ui_p_or extends RVTest("rv64ui-p-or")
class RVTest_rv64ui_p_ori extends RVTest("rv64ui-p-ori")
//class RVTest_rv64ui_p_sb extends RVTest("rv64ui-p-sb", true)
//class RVTest_rv64ui_p_sd extends RVTest("rv64ui-p-sd", true)
//class RVTest_rv64ui_p_sh extends RVTest("rv64ui-p-sh", true)
class RVTest_rv64ui_p_simple extends RVTest("rv64ui-p-simple")
class RVTest_rv64ui_p_sll extends RVTest("rv64ui-p-sll")
class RVTest_rv64ui_p_slli extends RVTest("rv64ui-p-slli")
class RVTest_rv64ui_p_slliw extends RVTest("rv64ui-p-slliw")
class RVTest_rv64ui_p_sllw extends RVTest("rv64ui-p-sllw")
class RVTest_rv64ui_p_slt extends RVTest("rv64ui-p-slt")
class RVTest_rv64ui_p_sltiu extends RVTest("rv64ui-p-sltiu")
class RVTest_rv64ui_p_sltu extends RVTest("rv64ui-p-sltu")
class RVTest_rv64ui_p_sra extends RVTest("rv64ui-p-sra")
class RVTest_rv64ui_p_srai extends RVTest("rv64ui-p-srai")
class RVTest_rv64ui_p_sraiw extends RVTest("rv64ui-p-sraiw")
class RVTest_rv64ui_p_sraw extends RVTest("rv64ui-p-sraw")
class RVTest_rv64ui_p_srl extends RVTest("rv64ui-p-srl")
class RVTest_rv64ui_p_srli extends RVTest("rv64ui-p-srli")
class RVTest_rv64ui_p_srliw extends RVTest("rv64ui-p-srliw")
class RVTest_rv64ui_p_srlw extends RVTest("rv64ui-p-srlw")
class RVTest_rv64ui_p_sub extends RVTest("rv64ui-p-sub")
class RVTest_rv64ui_p_subw extends RVTest("rv64ui-p-subw")
//class RVTest_rv64ui_p_sw extends RVTest("rv64ui-p-sw", true)
class RVTest_rv64ui_p_xor extends RVTest("rv64ui-p-xor")
class RVTest_rv64ui_p_xori extends RVTest("rv64ui-p-xori")
