package com.darwinit.annotation.autodsl.check

import com.darwinit.annotation.autodsl.definition.javaToKotlinType
import com.darwinit.annotation.autodsl.definition.primitiveTypes
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.Messager
import javax.lang.model.element.VariableElement
import javax.tools.Diagnostic

fun isAutoDslObjectCollection(field: VariableElement,
                              processingEnvMessager: Messager? = null): Boolean {
    var isAutoDslCollection = false
    val type=field.asType().asTypeName()

    if (type is ParameterizedTypeName) {
        isAutoDslCollection = !primitiveTypes.contains(type.typeArguments[0].javaToKotlinType() as ClassName)

        if (processingEnvMessager != null) {
            processingEnvMessager.printMessage(
                Diagnostic.Kind.NOTE,
                "isAutoDslObjectCollection class: %s, Generic first type is %s %s isAutoDslCollection %b".format(
                    field.simpleName,
                    (type.typeArguments[0].javaToKotlinType() as ClassName).packageName,
                    (type.typeArguments[0].javaToKotlinType() as ClassName).simpleName,
                    isAutoDslCollection
                )
            )
        }
    }

    return isAutoDslCollection
}