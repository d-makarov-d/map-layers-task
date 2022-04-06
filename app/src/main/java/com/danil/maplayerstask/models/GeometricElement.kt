package com.danil.maplayerstask.models

class GeometricElement(
    private val name: String
): MapElement() {
    override fun name(): String = name
}