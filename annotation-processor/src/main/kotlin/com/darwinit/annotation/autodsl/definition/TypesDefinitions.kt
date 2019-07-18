package com.darwinit.annotation.autodsl.definition

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

val primitiveTypes = hashSetOf(
    ClassName("kotlin", "String"),
    ClassName("kotlin", "Int")
)

fun Element.IsIntArray(): Boolean {
    return asType().asTypeName() == IntArray::class.java.asTypeName()
}

fun Element.javaToKotlinType(): TypeName =
    asType().asTypeName().javaToKotlinType()

fun TypeName.javaToKotlinType(): TypeName {
    return if (this == IntArray::class.java.asTypeName()) {
        IntArray::class.asTypeName()
    } else if (this is ParameterizedTypeName) {
        val className = rawType.javaToKotlinType() as ClassName

        className.parameterizedBy(*typeArguments.map { it.javaToKotlinType() }.toTypedArray())
    } else {
        val className =
            JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))
                ?.asSingleFqName()?.asString()

        return if (className == null) {
            this
        } else {
            ClassName.bestGuess(className)
        }
    }
}

fun Element.getDefaultValue(): Any? {
    val kotlinTypeName = this.javaToKotlinType()

    if (kotlinTypeName.isNullable)
        return null

    when (kotlinTypeName) {
        ANY -> return "???any"
        ARRAY -> return "???array"
        UNIT -> return "???unit"
        BOOLEAN -> return "false"
        BYTE,
        SHORT,
        INT,
        LONG -> return "0"
        CHAR -> return "''"
        FLOAT,
        DOUBLE -> return "0.0"
        ClassName("java.util", "UUID") -> return "UUID(0, 0)"
        ClassName("kotlin", "String") -> return "\"\""
    }

    if (kotlinTypeName.toString().startsWith("kotlin.Array"))
        return "Array<kotlin.Int>(3){0}"

    if (kotlinTypeName.toString().startsWith("kotlin.collections."))
        return "emptyList()"

    if (this.IsIntArray()) {
        return "IntArray(0)"
    }

    return "%s()".format(kotlinTypeName.toString())
}
