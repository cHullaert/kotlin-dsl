package com.darwinit.script.person

import java.util.*

class Person(val name: String,
             val uuid: UUID
)

class PersonAutoBuilder {
    var name: String = ""
    var uuid: String = UUID(0, 0).toString()

    fun build(): Person = Person(name=name, uuid=UUID.fromString(uuid))
}

fun person(block: PersonAutoBuilder.() -> Unit): Person =
    PersonAutoBuilder().apply(block).build()
