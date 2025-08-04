package com.github.rishabhdeepsingh.jhelper20.parser

interface DescriptionReceiver {
    fun receiveDescriptions(descriptions: Collection<Description>)

    fun isStopped(): Boolean
}