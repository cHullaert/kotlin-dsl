package com.darwinit.script

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ScriptHookTest {

    private fun clearFile(input: String): String {
        return input.replace(" ", "").replace("\n", "")
    }

    @ParameterizedTest
    @CsvSource(value=["/hook/hook-before.kt,/hook/hook-after.kt", "/hook/hook-before-2.kt,/hook/hook-after-2.kt"])
    fun whenHookThenUpdate(input: String, expected: String) {
        val scriptHooks= mutableListOf(ScriptHook("container", "add", patterns = arrayOf("person")))
        val before= ScriptRunner::class.java.getResource(input).readText()
        val after= ScriptRunner::class.java.getResource(expected).readText()
        assertEquals(clearFile(after), clearFile(ScriptHookModifier(scriptHooks).update(before)))

    }

}