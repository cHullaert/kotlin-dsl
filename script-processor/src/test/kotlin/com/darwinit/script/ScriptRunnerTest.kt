package com.darwinit.script

import com.darwinit.script.person.Person
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class ScriptRunnerTest {
    @Test
    fun ScriptRunnerTest() {
        val inputStream = File("src/test/resources/dsl/person.txt").inputStream()

        val scriptHooks = mutableListOf(
            ScriptHook(
                "container",
                "add",
                patterns = arrayOf(
                    "person"
                )
            )
        )

        val scriptRunner = ScriptRunner(
            ScriptRunner.Script(
                inputStream,
                listOf(
                    ScriptRunner.Dependency("com.darwinit.script.person.*")
                )
            ),
            scriptHooks,
            setOf(ScriptRunner.Option.GENERATE_MAIN)
        )

        assertThat(scriptRunner).isInstanceOf(ScriptRunner::class.java)

        val container = mutableListOf<Person>()

        var scriptRunnerVariable = ScriptRunner.Variable(
            "container",
            "MutableList<Any>",
            container
        )

        assertThat(scriptRunnerVariable).isInstanceOf(ScriptRunner.Variable::class.java)

        // TODO("Mock engine: ScriptEngine")

        scriptRunner.run(listOf(
            scriptRunnerVariable
        ))

        assertThat(container).hasSize(1)
        assertThat(container[0].name).isEqualTo("name1")
        assertThat(container[0].uuid).isEqualTo(UUID.fromString("709fab36-de23-49be-b95d-ad3a60659955"))
    }

}