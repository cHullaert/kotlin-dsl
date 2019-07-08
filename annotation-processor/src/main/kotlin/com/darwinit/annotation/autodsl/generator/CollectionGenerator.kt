package com.darwinit.annotation.autodsl.generator

import com.darwinit.annotation.autodsl.check.isAutoDslObjectCollection
import com.darwinit.annotation.autodsl.definition.javaToKotlinType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.Messager
import javax.lang.model.element.VariableElement


class CollectionGenerator(
    private val packageName: String,
    private val fields: List<VariableElement>,
    val processingEnvMessager: Messager
) {

    fun build(): FileSpec {
        val builder=FileSpec.builder(packageName,
                                    "CollectionWrapper")

        fields/*.distinctBy {
            TODO("Detect field by type not name,
                for fields in differents class with same name and not same field type")
            it.simpleName.toString()
        }*/.filter {
            isAutoDslObjectCollection(it, processingEnvMessager)
        }.forEach {
            builder.addType(createCollectionType(it))
        }

        return builder.build()
    }

    private fun buildFunction(field: VariableElement): FunSpec {
        val parameterizedTypeName=field.asType().asTypeName() as ParameterizedTypeName
        val type=parameterizedTypeName.typeArguments[0].javaToKotlinType() as ClassName
        val builderName = AbstractGenerator.BUILDER_CLASS_PATTERN.format(type.simpleName)
        val lambda= LambdaTypeName.get(receiver = TypeVariableName(builderName),
            returnType = Unit::class.asClassName())

        val functionName = type.simpleName.decapitalize()

        return FunSpec.builder(functionName)
            .addParameter("block", lambda)
            .addStatement("add(%s().apply(block).build())".format(builderName))
            .build()
    }

    private fun createCollectionType(field: VariableElement): TypeSpec {
        val parameterizedTypeName=field.asType().asTypeName() as ParameterizedTypeName
        val newParameterizedTypeName=kotlin.collections.ArrayList::class.asClassName().parameterizedBy(*parameterizedTypeName.typeArguments.map { it.javaToKotlinType() }.toTypedArray())

        return TypeSpec.classBuilder(field.simpleName.toString().toUpperCase())
            .superclass(newParameterizedTypeName)
            .addFunction(buildFunction(field))
            .build()
    }
}