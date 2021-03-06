package com.martmists.ynet.screen.util

import com.github.vini2003.blade.Blade
import com.github.vini2003.blade.client.data.PartitionedTexture
import com.github.vini2003.blade.client.utilities.Drawings
import com.github.vini2003.blade.client.utilities.Instances
import com.github.vini2003.blade.client.utilities.Layers
import com.github.vini2003.blade.client.utilities.Texts
import com.github.vini2003.blade.common.data.Color
import com.github.vini2003.blade.common.utilities.Networks
import com.github.vini2003.blade.common.widget.OriginalWidgetCollection
import com.github.vini2003.blade.common.widget.WidgetCollection
import com.github.vini2003.blade.common.widget.base.AbstractWidget
import com.martmists.ynet.ext.eq
import com.martmists.ynet.ext.ofRGB
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text


class ColoredButtonWidget(private val clickAction: (ColoredButtonWidget) -> Unit, var color: Color) : AbstractWidget() {
    var textureOn = PartitionedTexture(Blade.identifier("textures/widget/button_on.png"), 18F, 18F, 0.11111111111111111111F, 0.11111111111111111111F, 0.11111111111111111111F, 0.16666666666666666667F)
    var textureOff = PartitionedTexture(Blade.identifier("textures/widget/button_off.png"), 18F, 18F, 0.11111111111111111111F, 0.11111111111111111111F, 0.11111111111111111111F, 0.11111111111111111111F)
    var textureOnFocus = PartitionedTexture(Blade.identifier("textures/widget/button_on_focus.png"), 18F, 18F, 0.11111111111111111111F, 0.11111111111111111111F, 0.11111111111111111111F, 0.11111111111111111111F)

    var disabled: Boolean = false

    var label: Text? = null

    override fun onAdded(original: OriginalWidgetCollection, immediate: WidgetCollection) {
        super.onAdded(original, immediate)
        println("Marking Sync: MOUSE_CLICK")
        synchronize.add(Networks.MOUSE_CLICK)
    }

    override fun onMouseClicked(x: Float, y: Float, button: Int) {
        if (focused || (!focused && handler != null && !handler!!.client)) {
            clickAction.invoke(this)

            playSound()
        }

        super.onMouseClicked(x, y, button)
    }

    fun playSound() {
        Instances.client().soundManager.play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F))
    }

    override fun drawWidget(matrices: MatrixStack, provider: VertexConsumerProvider) {
        if (hidden) return

        val texture = if (disabled) textureOff else if (focused) textureOnFocus else textureOn

        texture.draw(matrices, provider, position.x, position.y, size.width, size.height)
        if (!color.eq(Color.ofRGB(0x000000))) {
            Drawings.drawQuad(matrices, provider, Layers.flat(), position.x, position.y, size.width, size.height, color)
        }

        if (provider is VertexConsumerProvider.Immediate) provider.draw()

        label?.also {
            Drawings.getTextRenderer()?.drawWithShadow(matrices, label, position.x + (size.width / 2 - Texts.width(label!!) / 2), position.y + (size.height / 2 - Texts.height() / 2), color("button.label").toInt()) // 0xFCFCFC
        }
    }
}