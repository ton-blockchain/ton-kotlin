import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import com.squareup.kotlinpoet.*
import kotlinx.serialization.decodeFromString

plugins {
    id("ton-kotlin.base")
}

abstract class GenerateOpenApiClientTask : DefaultTask() {
    @get:InputFile
    abstract val openApiSpec: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val mainClassName: Property<String>

    @TaskAction
    fun generate() {
        val specFile = openApiSpec.get().asFile
        val outDir = outputDir.get().asFile
        outDir.mkdirs()

        val map = Yaml.default.decodeFromString<YamlMap>(specFile.readText())
        val schemas = map.get<YamlMap>("components")?.get<YamlMap>("schemas") ?: return
        val objects = HashMap<String, TypeSpec.Builder>()
        schemas.entries.forEach { modelName, modelObj ->
            objects.parseObjects(modelName.content, modelObj.yamlMap)
        }
        objects.forEach { (name, builder) ->
            builder.generateObject(schemas.get<YamlMap>(name) ?: return@forEach, schemas)
            val fileSpec = FileSpec.builder(packageName.get(), name)
                .addType(builder.build())
                .build()

            fileSpec.writeTo(outDir)
        }

//        val definitions = spec["definitions"]?.jsonObject ?: return
//
//        val mainClassName = mainClassName.get()
//        val fileSpec = FileSpec.builder(packageName.get(), mainClassName)
//            .addType(
//                TypeSpec.interfaceBuilder(mainClassName).apply {
//                    definitions.forEach { (name, definition) ->
//                        val objClass = objectGen(name, definition.jsonObject)
//                        if (objClass != null) {
//                            addType(objClass)
//                        }
//                    }
//                }.build()
//            ).build()

    }

    fun MutableMap<String, TypeSpec.Builder>.parseObjects(modelName: String, modelObj: YamlMap) {
        if (modelObj.getScalar("type")?.content != "object") {
            return
        }
        val className = fixClassName(modelName)
        this[modelName] = TypeSpec.classBuilder(className)
    }

    fun TypeSpec.Builder.generateObject(modelObj: YamlMap, schemasYml: YamlMap) {
        val properties = modelObj.get<YamlMap>("properties") ?: return
        val constructorBuilder = FunSpec.constructorBuilder()

        properties.entries.forEach { propertyNameScalar, propertyYml ->
            val propertyName = propertyNameScalar.content
            val propertyYmlMap = propertyYml.yamlMap
            val ref = propertyYmlMap.getScalar($$"$ref")?.content
            val typeSpec = if (ref != null) {
                val refKey = ref.removePrefix("#/components/schemas/")
                val refSchema = schemasYml.get<YamlMap>(refKey)
                if (refSchema != null) {
                    val refSchemaType = refSchema.getScalar("type")?.content
                    if (refSchemaType == "object") {
                        ClassName(packageName.get(), refKey)
                    } else {
                        typeName(refSchemaType)
                    }
                } else {
                    null
                }
            } else {
                val type = propertyYmlMap.getScalar("type")?.content ?: return@forEach
                typeName(type)
            }

            if (typeSpec != null) {
                constructorBuilder.addParameter(
                    propertyName, typeSpec
                ).addModifiers(KModifier.VALUE)
            }
        }

        primaryConstructor(constructorBuilder.build())
    }

//    fun objectGen(name: String, definition: JsonObject): TypeSpec? {
//        if (definition["type"]?.jsonPrimitive?.content != "object") return null
//        return TypeSpec.classBuilder(fixClassName(name)).apply {
//            val properties = definition["properties"]?.jsonObject ?: return null
//
//            properties.forEach { (propertyName, property) ->
//                val propertyType = toTypeName(property.jsonObject)
//                val camelCaseName = snakeToCamelCase(propertyName)
//                addProperty(PropertySpec.builder(camelCaseName, propertyType).apply {
//                    addAnnotation(
//                        AnnotationSpec.builder(SerialName::class)
//                            .addMember("%S", propertyName)
//                            .build()
//                    )
//                    addAnnotation(
//                        AnnotationSpec.builder(JvmName::class)
//                            .useSiteTarget(UseSiteTarget.GET)
//                            .addMember("%S", camelCaseName)
//                            .build()
//                    )
//                }.build())
//            }
//        }.build()
//    }
//
//    private fun toTypeName(obj: JsonObject): TypeName {
//        val refPath = obj[$$"$ref"]?.jsonPrimitive?.content
//        if (refPath != null) {
//            return ClassName(packageName.get(), mainClassName.get(), fixClassName(refPath.removePrefix("#/definitions/")))
//        }
//
//        val typeName = obj["type"]?.jsonPrimitive?.content ?: return Any::class.asTypeName()
//        return when (typeName) {
//            "object" -> {
//                val additionalProperties = obj["additionalProperties"] ?: return Any::class.asTypeName()
//                if (additionalProperties is JsonPrimitive) {
//                    return JsonObject::class.asTypeName()
//                }
//
//                val valueType = toTypeName(additionalProperties.jsonObject)
//
//                Map::class.asClassName()
//                    .parameterizedBy(
//                        String::class.asTypeName(),
//                        valueType
//                    )
//            }
//
//            "array" -> {
//                val items = obj["items"]!!.jsonObject
//                val itemsType = toTypeName(items)
//                List::class.asClassName().parameterizedBy(itemsType)
//            }
//
//            else -> typeName(typeName)
//        }
//    }

    private fun typeName(type: String?): TypeName {
        return when (type) {
            "string" -> String::class.asTypeName()
            "integer" -> Int::class.asTypeName()
            "boolean" -> Boolean::class.asTypeName()
            else -> Any::class.asTypeName()
        }
    }

    private fun fixClassName(name: String) = name.replace(".", "")
        .replaceFirstChar {
            it.uppercaseChar()
        }

    private fun snakeToCamelCase(input: String): String {
        if (input.isEmpty()) return input
        val sb = StringBuilder(input.length)
        var capitalizeNext = false
        for (ch in input) {
            if (ch == '_') {
                capitalizeNext = true
            } else {
                if (capitalizeNext) {
                    sb.append(ch.uppercaseChar())
                    capitalizeNext = false
                } else {
                    sb.append(ch)
                }
            }
        }
        return sb.toString()
    }
}
