/*
 * Copyright (c) 2024. Schizoid
 * All rights reserved.
 */

package dev.lyzev.schizoid.feature.features.module.modules.render

import dev.lyzev.api.events.EventClientPlayerEntityTick
import dev.lyzev.api.events.on
import dev.lyzev.api.setting.settings.OptionEnum
import dev.lyzev.api.setting.settings.option
import dev.lyzev.api.setting.settings.slider
import dev.lyzev.api.setting.settings.switch
import dev.lyzev.schizoid.feature.features.module.ModuleToggleableRenderImGuiContent
import imgui.ImColor
import imgui.ImGui
import imgui.ImVec2
import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlot.*
import imgui.extension.implot.flag.ImPlotAxisFlags
import imgui.extension.implot.flag.ImPlotCol
import imgui.extension.implot.flag.ImPlotFlags
import imgui.extension.implot.flag.ImPlotStyleVar
import imgui.flag.ImGuiCol

object ModuleToggleableMotionGraph :
    ModuleToggleableRenderImGuiContent("Motion Graph", "Shows a graph of the player's motion.", category = Category.RENDER) {

    private val y = mutableListOf(0.0)
    private val x = mutableListOf(0.0)
    private val size by slider("Size", "The size of the motion graph.", 15, 5, 100, "%%")
    private val scale = ImVec2(288f, 162f)

    val length by slider("Length", "The length of the graph.", 100, 1, 200, "ticks") {
        y.clear()
        x.clear()
        y.add(0.0)
        x.add(0.0)
    }

    val unit by option("Unit", "The unit of the graph.", Units.BLOCKS_PER_SECOND, Units.entries) {
        it.apply(x, y)
    }

    val fill by switch("Fill", "Fills the graph.", false)

    val noTitle by switch("No Title", "The plot title will not be displayed", false)
    val noLegend by switch("No Legend", "The legend will not be displayed.", false)
    val noMousePos by switch(
        "No Mouse Pos",
        "The mouse position, in plot coordinates, will not be displayed inside of the plot.",
        false
    )
    val noHighlight by switch(
        "No Highlight",
        "Plot items will not be highlighted when their legend entry is hovered\n.",
        false
    )

    val noLabel by switch("No Label", "The axis label will not be displayed.", false)
    val noGridLines by switch("No Grid Lines", "No grid lines will be displayed.", false)
    val noTickMarks by switch("No Tick Marks", "No tick marks will be displayed.", false)
    val noTickLabels by switch("No Tick Labels", "No tick labels will be displayed.", false)

    override fun renderImGuiContent() {
        scale.set(mc.window.framebufferWidth * size / 100f, mc.window.framebufferHeight * size / 100f)

        var plotFlags = ImPlotFlags.NoMenus
        if (noTitle) plotFlags = plotFlags or ImPlotFlags.NoTitle
        if (noLegend) plotFlags = plotFlags or ImPlotFlags.NoLegend
        if (noMousePos) plotFlags = plotFlags or ImPlotFlags.NoMousePos
        if (noHighlight) plotFlags = plotFlags or ImPlotFlags.NoHighlight

        var axisFlags = ImPlotAxisFlags.AutoFit
        if (noLabel) axisFlags = axisFlags or ImPlotAxisFlags.NoLabel
        if (noGridLines) axisFlags = axisFlags or ImPlotAxisFlags.NoGridLines
        if (noTickMarks) axisFlags = axisFlags or ImPlotAxisFlags.NoTickMarks
        if (noTickLabels) axisFlags = axisFlags or ImPlotAxisFlags.NoTickLabels

        if (beginPlot(
                mc.player!!.name.literalString,
                unit.per,
                unit.key,
                scale,
                plotFlags,
                axisFlags or ImPlotAxisFlags.Invert,
                axisFlags
            )
        ) {
            val y = y.toTypedArray()
            val x = x.toTypedArray()
            val primary = ImColor.rgba(ImGui.getStyle().getColor(ImGuiCol.Button)).toLong()
            ImPlot.pushStyleColor(ImPlotCol.Line, primary)
            ImPlot.pushStyleColor(ImPlotCol.Fill, primary)
            ImPlot.pushStyleVar(ImPlotStyleVar.LineWeight, 15f * size / 100f)
            val speed = y.lastOrNull() ?: 0.0
            plotLine("%.1f %s".format(speed, unit.short), x, y)
            if (fill) {
                plotShaded("%.1f %s".format(speed, unit.short), x, y, 0)
            }
            ImPlot.popStyleVar()
            ImPlot.popStyleColor(2)
            endPlot()
        }
    }

    init {
        on<EventClientPlayerEntityTick> { event ->
            unit.addY(y, event.player.velocity.horizontalLength())
            if (y.size > length) {
                y.removeFirst()
            }
            if (x.size < length) {
                unit.addX(x, x.size.toDouble())
            }
        }
    }


    interface Unit {

        fun apply(x: MutableList<Double>, y: MutableList<Double>)

        fun addX(x: MutableList<Double>, value: Double)

        fun addY(y: MutableList<Double>, value: Double)
    }

    enum class Units(override val key: String, val short: String, val per: String) : Unit, OptionEnum {
        BLOCKS_PER_SECOND("Blocks per second", "BPS", "Second") {

            override fun apply(x: MutableList<Double>, y: MutableList<Double>) {
                for (i in y.indices) {
                    y[i] = y[i] * 20
                }
                for (i in x.indices) {
                    x[i] = x[i] / 20
                }
            }

            override fun addX(x: MutableList<Double>, value: Double) {
                x.add(0, value / 20)
            }

            override fun addY(y: MutableList<Double>, value: Double) {
                y += value * 20
            }
        },
        BLOCKS_PER_TICK("Blocks per tick", "BPT", "Tick") {

            override fun apply(x: MutableList<Double>, y: MutableList<Double>) {
                for (i in y.indices) {
                    y[i] = y[i] / 20
                }
                for (i in x.indices) {
                    x[i] = x[i] * 20
                }
            }

            override fun addX(x: MutableList<Double>, value: Double) {
                x.add(0, value)
            }

            override fun addY(y: MutableList<Double>, value: Double) {
                y += value
            }
        }
    }
}
