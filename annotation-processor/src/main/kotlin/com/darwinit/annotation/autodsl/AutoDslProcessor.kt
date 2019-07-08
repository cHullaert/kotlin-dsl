package com.darwinit.annotation.autodsl

import com.darwinit.annotation.autodsl.generator.BuilderGenerator
import com.darwinit.annotation.autodsl.generator.CollectionGenerator
import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.asClassName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedOptions
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic

@AutoService(Processor::class)
@SupportedOptions(AutoDslProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class AutoDslProcessor: AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(AutoDsl::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    private fun getFields(typeElement: TypeElement): List<VariableElement> {
        val fields = ElementFilter.fieldsIn(typeElement.enclosedElements)
        if(typeElement.superclass != null) {
            val superElement=processingEnv.typeUtils.asElement(typeElement.superclass)
            if(superElement != null) {
                return fields+getFields(superElement as TypeElement)
            }
        }

        return fields
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        this.processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "evaluate auto dsl processor")

        val clazzList=roundEnv!!.getElementsAnnotatedWith(AutoDsl::class.java)

        processClasses(clazzList)
        processCollections(clazzList)

        return false
    }

    private fun processCollections(clazzList: Set<Element>) {
        val clazzGroup = clazzList
            .groupBy {
                clazz -> (clazz as TypeElement).asClassName().packageName
            }

        clazzGroup.forEach {
            clazz ->

            val allFields=clazz.value.map { it as TypeElement }
                .filter {
                    it.kind === javax.lang.model.element.ElementKind.CLASS}
                .flatMap {
                    this.processingEnv.messager.printMessage(
                        Diagnostic.Kind.NOTE,
                        "processCollections getFields " + it.getQualifiedName()
                    )

                    getFields(it)
                }

            if(allFields.isNotEmpty())
                CollectionGenerator(clazz.key, allFields, this.processingEnv.messager)
                    .build()
                    .writeTo(processingEnv.filer)
        }
    }

    private fun processClasses(clazzList: Set<Element>) {
        clazzList
            .asSequence()
            .map { it as TypeElement }
            .filter { it.kind === ElementKind.CLASS}
            .forEach {
                this.processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "class %s is annotated with autoDsl".format(it.simpleName.toString()))

                val fields=getFields(it)
                BuilderGenerator(it, fields, clazzList, this.processingEnv.messager)
                    .build()
                    .writeTo(processingEnv.filer)
            }
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}
