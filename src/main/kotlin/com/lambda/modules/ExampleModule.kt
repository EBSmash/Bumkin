package com.lambda.modules

import com.lambda.ExamplePlugin
import com.lambda.client.event.events.PacketEvent
import com.lambda.client.module.Category
import com.lambda.client.plugin.api.PluginModule
import com.lambda.client.util.items.swapToSlot
import com.lambda.client.util.math.VectorUtils.toBlockPos
import com.lambda.client.util.text.MessageSendHelper
import com.lambda.client.util.threads.safeListener
import net.minecraft.block.BlockButtonStone
import net.minecraft.block.BlockDispenser
import net.minecraft.block.BlockPumpkin
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.Packet
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.util.EnumHand
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import java.lang.Math.atan2
import java.lang.Math.sqrt


internal object ExampleModule : PluginModule(
    name = "Bumpkin",
    category = Category.MISC,
    description = "The great bumkining of 2b2t",
    pluginMain = ExamplePlugin
) {
    val mc: Minecraft = Minecraft.getMinecraft();

    private val switchBack = setting("Switch Back", true)
    private val timeout by setting("Delay", 20, 1..100, 5, { switchBack.value })

    var hasPumpkin: Int = -1;
    var hasDispenser: Int = -1;

    init {
        onEnable {
            if(findButton() != -1 && findDispenser() != -1 && findPumpkin() != -1 ){
                MessageSendHelper.sendChatMessage("Attack someone to bumpkin them!")
                hasPumpkin = findPumpkin()
                hasPumpkin = findPumpkin()
            }
            else{
                MessageSendHelper.sendWarningMessage("You need a button, dispenser, and a pumpkin in your horbatr")
                this.disable()
            }

        }

        safeListener<PacketEvent.Send> {

            if(it.packet is CPacketUseEntity){
                if((it.packet as CPacketUseEntity).action == CPacketUseEntity.Action.ATTACK){

                    val packet = (it.packet as CPacketUseEntity);

                    MessageSendHelper.sendChatMessage("Test")


                    swapToSlot(findDispenser())
                    val hitVec = Vec3d(packet.hitVec.toBlockPos()).add(0.5, 0.5, 0.5).add(packet.hitVec.scale(0.5))
                    faceVectorPacketInstant(hitVec)
                    mc.playerController.processRightClickBlock(mc.player, mc.world, packet.hitVec.add(1.0,0.0,0.0).toBlockPos(), mc.player.horizontalFacing.opposite, hitVec, EnumHand.MAIN_HAND)


                }
                }
            }

        }

    private fun findButton(): Int {
        var slot = -1
        for (i in 0..8) {
            val stack: ItemStack = mc.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item !is ItemBlock || (stack.item as ItemBlock).block !is BlockButtonStone) continue
            slot = i
            break
        }
        return slot
    }

    private fun findPumpkin(): Int {
        var slot = -1
        for (i in 0..8) {
            val stack: ItemStack = mc.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item !is ItemBlock || (stack.item as ItemBlock).block !is BlockPumpkin) continue
            slot = i
            break
        }
        return slot
    }

    private fun findDispenser(): Int {
        var slot = -1
        for (i in 0..8) {
            val stack: ItemStack = mc.player.inventory.getStackInSlot(i)
            if (stack == ItemStack.EMPTY || stack.item !is ItemBlock || (stack.item as ItemBlock).block !is BlockDispenser) continue
            slot = i
            break
        }
        return slot
    }

    private fun faceVectorPacketInstant(vec: Vec3d) {
        val rotations: FloatArray = getLegitRotations(vec)
        mc.player.connection.sendPacket(CPacketPlayer.Rotation(rotations[0], rotations[1], mc.player.onGround) as Packet<*>)
    }
    private fun getEyesPos(): Vec3d {
        return Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight().toDouble(), mc.player.posZ)
    }
    private fun getLegitRotations(vec: Vec3d): FloatArray {
        val eyesPos: Vec3d = getEyesPos()
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z
        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90.0f
        val pitch = (-Math.toDegrees(atan2(diffY, diffXZ))).toFloat()
        return floatArrayOf(mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch))
    }
}



