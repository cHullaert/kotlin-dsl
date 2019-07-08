package com.darwinit.script

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class ScriptRunnerTest {

    @Test
    fun ScriptRunnerTest() {
        val inputStream = File("src/test/resources/hook-after.kt").inputStream()

        val scriptHooks = mutableListOf(
            ScriptHook(
                "container",
                "add",
                patterns = arrayOf(
                    "person"
                )
            )
        )

        val container = mutableListOf<Any>()

        val scriptRunner = ScriptRunner(
            ScriptRunner.Script(
                inputStream,
                listOf(
                    ScriptRunner.Dependency("com.darwinit.annotation.demo.*")
                )
            ),
            scriptHooks,
            setOf(ScriptRunner.Option.GENERATE_MAIN)
        )

        assertThat(scriptRunner).isInstanceOf(ScriptRunner::class.java)

        scriptRunner.run(listOf(
            ScriptRunner.Variable(
                "container",
                "MutableList<Any>",
                container
            )
        ))

        assertThat(container).hasSize(1)
    }

}