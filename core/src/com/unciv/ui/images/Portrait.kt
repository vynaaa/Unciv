package com.unciv.ui.images

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.Align
import com.unciv.models.ruleset.Ruleset
import com.unciv.models.stats.Stats
import com.unciv.ui.components.extensions.center
import com.unciv.ui.components.extensions.centerX
import com.unciv.ui.components.extensions.colorFromRGB
import com.unciv.ui.components.extensions.darken
import com.unciv.ui.components.extensions.surroundWithCircle
import com.unciv.ui.components.extensions.toGroup
import com.unciv.ui.components.extensions.toLabel

open class Portrait(val type: Type, val imageName: String, val size: Float, val borderSize: Float = 2f) : Group() {

    enum class Type(val directory: String) {
        Unit("Unit"),
        Building("Building"),
        Tech("Tech"),
        Resource("Resource"),
        Improvement("Improvement"),
        Promotion("UnitPromotion"),
        Unique("Unique"),
        Nation("Nation"),
        Religion("Religion"),
        UnitAction("UnitAction")
    }

    val image: Image
    val background: Group
    val ruleset: Ruleset = ImageGetter.ruleset

    var isPortrait = false

    val pathPortrait = "${type.directory}Portraits/$imageName"
    val pathPortraitFallback = "${type.directory}Portraits/Fallback"
    val pathIcon = "${type.directory}Icons/$imageName"
    val pathIconFallback = "${type.directory}Icons/Fallback"

    open fun getDefaultInnerBackgroundTint(): Color { return Color.WHITE.cpy() }
    open fun getDefaultOuterBackgroundTint(): Color { return Color.BLACK.cpy() }
    open fun getDefaultImageTint(): Color { return Color.WHITE.cpy() }
    open fun getDefaultImage(): Image {
        return when {
            ImageGetter.imageExists(pathIcon) -> ImageGetter.getImage(pathIcon)
            ImageGetter.imageExists(pathIconFallback) -> ImageGetter.getImage(pathIconFallback)
            else -> ImageGetter.getCircle()
        }
    }

    init {

        isTransform = false

        image = getMainImage()
        background = getMainBackground()

        this.setSize(size + borderSize, size + borderSize)

        background.center(this)
        image.center(this)

        this.addActor(background)
        this.addActor(image)

    }

    /** Inner image */
    private fun getMainImage() : Image {
        return when {
            ImageGetter.imageExists(pathPortrait) -> {
                isPortrait = true
                ImageGetter.getImage(pathPortrait)
            }
            ImageGetter.imageExists(pathPortraitFallback) -> {
                isPortrait = true
                ImageGetter.getImage(pathPortraitFallback)
            }
            else -> getDefaultImage().apply { color = getDefaultImageTint() }
        }
    }

    /** Border / background */
    private fun getMainBackground() : Group {

        if (isPortrait && ImageGetter.imageExists("${type.directory}Portraits/Background")) {
            val backgroundImage = ImageGetter.getImage("${type.directory}Portraits/Background")
            val ratioW = image.width / backgroundImage.width
            val ratioH = image.height / backgroundImage.height
            image.setSize((size + borderSize)*ratioW, (size + borderSize)*ratioH)
            return backgroundImage.toGroup(size + borderSize)

        } else {
            image.setSize(size*0.75f, size*0.75f)

            val bg = Group().apply { isTransform = false }

            val circleInner = ImageGetter.getCircle()
            val circleOuter = ImageGetter.getCircle()

            circleInner.setSize(size, size)
            circleOuter.setSize(size + borderSize, size + borderSize)
            bg.setSize(size + borderSize, size + borderSize)

            circleInner.align = Align.center
            circleOuter.align = Align.center

            circleInner.color = getDefaultInnerBackgroundTint()
            circleOuter.color = getDefaultOuterBackgroundTint()

            circleOuter.center(bg)
            circleInner.center(bg)

            circleOuter.setOrigin(Align.center)
            circleInner.setOrigin(Align.center)

            bg.addActor(circleOuter)
            bg.addActor(circleInner)

            return bg
        }
    }

}

class PortraitResource(name: String, size: Float, amount: Int = 0) : Portrait(Type.Resource, name, size) {

    init {
        if (amount > 0) {
            val label = amount.toString().toLabel(
                fontSize = 8,
                fontColor = Color.WHITE,
                alignment = Align.center)
            val amountGroup = label.surroundWithCircle(size/2, true, Color.BLACK)

            label.y -= 0.5f
            amountGroup.x = width - amountGroup.width * 3 / 4
            amountGroup.y = -amountGroup.height / 4

            addActor(amountGroup)
        }
    }

    override fun getDefaultInnerBackgroundTint(): Color {
        return ruleset.tileResources[imageName]?.resourceType?.getColor() ?: Color.WHITE
    }
}

class PortraitTech(name: String, size: Float) : Portrait(Type.Tech, name, size) {
    override fun getDefaultOuterBackgroundTint(): Color {
        return getDefaultImageTint()
    }
    override fun getDefaultImageTint(): Color {
        return ruleset.eras[ruleset.technologies[imageName]?.era()]?.getColor()?.darken(0.6f) ?: Color.BLACK
    }
}

class PortraitUnit(name: String, size: Float) : Portrait(Type.Unit, name, size) {
    override fun getDefaultImageTint(): Color {
        return Color.BLACK
    }
}

class PortraitBuilding(name: String, size: Float) : Portrait(Type.Building, name, size) {
    override fun getDefaultImageTint(): Color {
        return Color.BLACK
    }
}

class PortraitUnique(name: String, size: Float) : Portrait(Type.Unique, name, size) {
    override fun getDefaultImageTint(): Color {
        return Color.BLACK
    }
}

class PortraitReligion(name: String, size: Float) : Portrait(Type.Religion, name, size) {
    override fun getDefaultImageTint(): Color {
        return Color.BLACK
    }
}

class PortraitUnitAction(name: String, size: Float) : Portrait(Type.UnitAction, name, size) {
    override fun getDefaultImageTint(): Color {
        return Color.BLACK
    }
}

class PortraitImprovement(name: String, size: Float, dim: Boolean = false) : Portrait(Type.Improvement, name, size) {

    init {
        if (dim) {
            image.color.a = 0.7f
            background.color.a = 0.7f
        }
    }

    private fun getColorFromStats(stats: Stats): Color {
        if (stats.asSequence().none { it.value > 0 })
            return Color.WHITE
        return stats.asSequence().maxByOrNull { it.value }!!.key.color
    }

    override fun getDefaultInnerBackgroundTint(): Color {
        val improvement = ImageGetter.ruleset.tileImprovements[imageName]
        if (improvement != null)
            return getColorFromStats(improvement)
        return Color.WHITE
    }
}

class PortraitNation(name: String, size: Float) : Portrait(Type.Nation, name, size, size*0.1f) {

    override fun getDefaultImage(): Image {

        val nation = ruleset.nations[imageName]
        val isCityState = nation != null && nation.isCityState()
        val pathCityState = "NationIcons/CityState"

        return when {
            ImageGetter.imageExists(pathIcon) -> ImageGetter.getImage(pathIcon)
            isCityState && ImageGetter.imageExists(pathCityState)-> ImageGetter.getImage(pathCityState)
            ImageGetter.imageExists(pathIconFallback) -> ImageGetter.getImage(pathIconFallback)
            else -> ImageGetter.getCircle()
        }
    }

    override fun getDefaultInnerBackgroundTint(): Color {
        return ruleset.nations[imageName]?.getOuterColor() ?: Color.BLACK
    }

    override fun getDefaultOuterBackgroundTint(): Color {
        return getDefaultImageTint()
    }

    override fun getDefaultImageTint(): Color {
        return ruleset.nations[imageName]?.getInnerColor() ?: Color.WHITE
    }

}

class PortraitPromotion(name: String, size: Float) : Portrait(Type.Promotion, name, size) {

    var level = 0

    init {
        if (level > 0) {
            val padding = if (level == 3) 0.5f else 2f
            val starTable = Table().apply { defaults().pad(padding) }
            for (i in 1..level)
                starTable.add(ImageGetter.getImage("OtherIcons/Star")).size(size / 4f)
            starTable.centerX(this)
            starTable.y = size / 6f
            addActor(starTable)
        }
    }

    override fun getDefaultImage(): Image {

        val nameWithoutBrackets = imageName.replace("[", "").replace("]", "")

        level = when {
            nameWithoutBrackets.endsWith(" I") -> 1
            nameWithoutBrackets.endsWith(" II") -> 2
            nameWithoutBrackets.endsWith(" III") -> 3
            else -> 0
        }

        val basePromotionName = nameWithoutBrackets.dropLast(if (level == 0) 0 else level + 1)

        val pathWithoutBrackets = "UnitPromotionIcons/$nameWithoutBrackets"
        val pathBase = "UnitPromotionIcons/$basePromotionName"
        val pathUnit = "UnitIcons/${basePromotionName.removeSuffix(" ability")}"

        return when {
            ImageGetter.imageExists(pathWithoutBrackets) -> {
                level = 0
                ImageGetter.getImage(pathWithoutBrackets)
            }
            ImageGetter.imageExists(pathBase) -> ImageGetter.getImage(pathBase)
            ImageGetter.imageExists(pathUnit) -> ImageGetter.getImage(pathUnit)
            else -> ImageGetter.getImage(pathIconFallback)
        }
    }

    override fun getDefaultImageTint(): Color {
        return colorFromRGB(255, 226, 0)
    }

    override fun getDefaultOuterBackgroundTint(): Color {
        return getDefaultImageTint()
    }

    override fun getDefaultInnerBackgroundTint(): Color {
        return colorFromRGB(0, 12, 49)
    }

}
