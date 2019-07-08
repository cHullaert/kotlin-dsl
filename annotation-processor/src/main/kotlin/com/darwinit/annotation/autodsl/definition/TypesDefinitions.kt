package com.darwinit.annotation.autodsl.definition

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.Element
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

val primitiveTypes = hashSetOf(
    ClassName("kotlin", "String"),
    ClassName("kotlin", "Int")
)

fun Element.javaToKotlinType(): TypeName =
    asType().asTypeName().javaToKotlinType()


fun TypeName.javaToKotlinType(): TypeName {
    return if (this is ParameterizedTypeName) {
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

fun TypeName.getDefaultValue(): Any? {
    if(this.isNullable)
        return null

    when(this) {
        ANY -> return "???any"
        ARRAY ->  return "???array"
        UNIT ->  return "???unit"
        BOOLEAN ->  return "false"
        BYTE,
        SHORT,
        INT,
        LONG ->  return "0"
        CHAR -> return "''"
        FLOAT,
        DOUBLE -> return "0.0"
        ClassName("java.util", "UUID") -> return "UUID(0, 0)"
        ClassName("kotlin", "String") -> return "\"\""
    }

    if(this.toString().startsWith("kotlin.Array"))
        return "Array<kotlin.Int>(3){0}"

    if(this.toString().startsWith("kotlin.collections."))
        return "emptyList()"

    return "%s()".format(this.toString())
}