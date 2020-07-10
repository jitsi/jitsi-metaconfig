package org.jitsi.metaconfig

import kotlin.reflect.KType

// Pipeline method

sealed class ConfigPropertyPipelineNode {
    // Retrieve a property from a source as a given type
    class ConfigValueRetriever<T : Any>(
        val key: String,
        val source: ConfigSource,
        val type: KType
    ) : ConfigPropertyPipelineNode() {
        fun retrieve(): T = source.getterFor(type)(key) as T
    }

    class ConfigValueTransformer<in T : Any, out R : Any>(
        val transformer: (T) -> R
    ) : ConfigPropertyPipelineNode() {
        fun transform(orig: T): R = transformer(orig)
    }
}

// I don't think a pipeline with this level of flexibility will work as we lose too much
// type information.  I think it'll need to be stricter, like an extension function
// on some generic type, which then adds another.  Like
// fun <T : Any, R : Any> ConfigProperty<T>.transformedBy(transformer: (T) -> R): R
// or something
class ConfigPropertyPipeline<T : Any> {
    private val nodes = mutableListOf<ConfigPropertyPipelineNode>()

    fun addNode(node: ConfigPropertyPipelineNode) {
        nodes += node
    }

    fun getValue() {
        for (node in nodes) {
            when(node) {
                is ConfigPropertyPipelineNode.ConfigValueRetriever<*> -> {
                    val x = node.retrieve()
                }
                is ConfigPropertyPipelineNode.ConfigValueTransformer<*, *> -> {
                }

            }
        }
    }
}