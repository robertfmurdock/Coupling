"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.KotlinResolversVisitor = exports.KOTLIN_SCALARS = void 0;
const graphql_1 = require("graphql");
const java_common_1 = require("@graphql-codegen/java-common");
const visitor_plugin_common_1 = require("@graphql-codegen/visitor-plugin-common");
exports.KOTLIN_SCALARS = {
    ID: { input: 'Any', output: 'Any' },
    String: { input: 'String', output: 'String' },
    Boolean: { input: 'Boolean', output: 'Boolean' },
    Int: { input: 'Int', output: 'Int' },
    Float: { input: 'Float', output: 'Float' },
};
class KotlinResolversVisitor extends visitor_plugin_common_1.BaseVisitor {
    constructor(rawConfig, _schema, defaultPackageName) {
        super(rawConfig, {
            enumValues: rawConfig.enumValues || {},
            listType: rawConfig.listType || 'Iterable',
            withTypes: rawConfig.withTypes || false,
            serializable: rawConfig.serializable || false,
            package: rawConfig.package || defaultPackageName,
            scalars: (0, visitor_plugin_common_1.buildScalarsFromConfig)(_schema, rawConfig, exports.KOTLIN_SCALARS),
            omitJvmStatic: rawConfig.omitJvmStatic || false,
        });
        this._schema = _schema;
    }
    getPackageName() {
        return `package ${this.config.package}\n`;
    }
    getEnumValue(enumName, enumOption) {
        if (this.config.enumValues[enumName] &&
            typeof this.config.enumValues[enumName] === 'object' &&
            this.config.enumValues[enumName][enumOption]) {
            return this.config.enumValues[enumName][enumOption];
        }
        return enumOption;
    }
    EnumValueDefinition(node) {
        return (enumName) => {
            const enumValue = this.getEnumValue(enumName, node.name.value);
            return (0, visitor_plugin_common_1.indent)(`${this.buildEnumAnnotation(enumValue)}${this.convertName(node, {
                useTypesPrefix: false,
                useTypesSuffix: false,
                transformUnderscore: true,
            })}("${enumValue}")`);
        };
    }
    EnumTypeDefinition(node) {
        const comment = (0, visitor_plugin_common_1.transformComment)(node.description, 0);
        const enumName = this.convertName(node.name);
        const enumValues = (0, visitor_plugin_common_1.indentMultiline)(node.values.map(enumValue => enumValue(node.name.value)).join(',\n') + ';', 2);
        const typeAnnotations = this.buildTypeAnnotations();
        return `${comment}${typeAnnotations}enum class ${enumName}(val label: String) {
${enumValues}
        
  companion object {
    ${this.config.omitJvmStatic ? '' : '@JvmStatic'}
    fun valueOfLabel(label: String): ${enumName}? {
      return values().find { it.label == label }
    }
  }
}`;
    }
    resolveInputFieldType(typeNode) {
        const innerType = (0, visitor_plugin_common_1.getBaseTypeNode)(typeNode);
        const schemaType = this._schema.getType(innerType.name.value);
        const isArray = typeNode.kind === graphql_1.Kind.LIST_TYPE ||
            (typeNode.kind === graphql_1.Kind.NON_NULL_TYPE && typeNode.type.kind === graphql_1.Kind.LIST_TYPE);
        let result = null;
        const nullable = typeNode.kind !== graphql_1.Kind.NON_NULL_TYPE;
        if ((0, graphql_1.isScalarType)(schemaType)) {
            if (this.config.scalars[schemaType.name]) {
                result = {
                    baseType: this.scalars[schemaType.name].input,
                    typeName: this.scalars[schemaType.name].input,
                    isScalar: true,
                    isArray,
                    nullable,
                };
            }
            else {
                result = { isArray, baseType: 'Any', typeName: 'Any', isScalar: true, nullable };
            }
        }
        else if ((0, graphql_1.isInputObjectType)(schemaType)) {
            const convertedName = this.convertName(schemaType.name);
            const typeName = convertedName.endsWith('Input') ? convertedName : `${convertedName}Input`;
            result = {
                baseType: typeName,
                typeName,
                isScalar: false,
                isArray,
                nullable,
            };
        }
        else if ((0, graphql_1.isEnumType)(schemaType) || (0, graphql_1.isObjectType)(schemaType)) {
            result = {
                isArray,
                baseType: this.convertName(schemaType.name),
                typeName: this.convertName(schemaType.name),
                isScalar: true,
                nullable,
            };
        }
        else {
            result = { isArray, baseType: 'Any', typeName: 'Any', isScalar: true, nullable };
        }
        if (result) {
            result.typeName = (0, java_common_1.wrapTypeWithModifiers)(result.typeName, typeNode, this.config.listType);
        }
        return result;
    }
    buildInputTransfomer(name, inputValueArray) {
        const classMembers = inputValueArray
            .map(arg => {
            const typeToUse = this.resolveInputFieldType(arg.type);
            const initialValue = this.initialValue(typeToUse.typeName, arg.defaultValue);
            const initial = initialValue ? ` = ${initialValue}` : typeToUse.nullable ? ' = null' : '';
            return (0, visitor_plugin_common_1.indent)(`val ${arg.name.value}: ${typeToUse.typeName}${typeToUse.nullable ? '?' : ''}${initial}`, 2);
        })
            .join(',\n');
        let suppress = '';
        const ctorSet = inputValueArray
            .map(arg => {
            const typeToUse = this.resolveInputFieldType(arg.type);
            const initialValue = this.initialValue(typeToUse.typeName, arg.defaultValue);
            const fallback = initialValue ? ` ?: ${initialValue}` : '';
            if (typeToUse.isArray && !typeToUse.isScalar) {
                suppress = '@Suppress("UNCHECKED_CAST")\n  ';
                return (0, visitor_plugin_common_1.indent)(`args["${arg.name.value}"]${typeToUse.nullable || fallback ? '?' : '!!'}.let { ${arg.name.value} -> (${arg.name.value} as List<Map<String, Any>>).map { ${typeToUse.baseType}(it) } }${fallback}`, 3);
            }
            if (typeToUse.isScalar) {
                return (0, visitor_plugin_common_1.indent)(`args["${arg.name.value}"] as ${typeToUse.typeName}${typeToUse.nullable || fallback ? '?' : ''}${fallback}`, 3);
            }
            if (typeToUse.nullable || fallback) {
                suppress = '@Suppress("UNCHECKED_CAST")\n  ';
                return (0, visitor_plugin_common_1.indent)(`args["${arg.name.value}"]?.let { ${typeToUse.typeName}(it as Map<String, Any>) }${fallback}`, 3);
            }
            suppress = '@Suppress("UNCHECKED_CAST")\n  ';
            return (0, visitor_plugin_common_1.indent)(`${typeToUse.typeName}(args["${arg.name.value}"] as Map<String, Any>)`, 3);
        })
            .join(',\n');
        const typeAnnotations = this.buildTypeAnnotations();
        // language=kotlin
        return `${typeAnnotations}data class ${name}(
            ${classMembers}
        ) {
            ${suppress}constructor(args: Map<String, Any>) : this(
            ${ctorSet}
            )
        }`;
    }
    buildTypeAnnotations() {
        return this.config.serializable ? (0, visitor_plugin_common_1.indent)('@kotlinx.serialization.Serializable ', 0) : '';
    }
    buildEnumAnnotation(label) {
        return this.config.serializable
            ? (0, visitor_plugin_common_1.indent)(`@kotlinx.serialization.SerialName("${label}") `, 0)
            : '';
    }
    buildTypeTransfomer(name, typeValueArray) {
        const classMembers = typeValueArray
            .map(arg => {
            if (!arg.type) {
                return '';
            }
            const typeToUse = this.resolveInputFieldType(arg.type);
            return (0, visitor_plugin_common_1.indent)(`val ${arg.name.value}: ${typeToUse.typeName}${typeToUse.nullable ? '?' : ''}`, 2);
        })
            .join(',\n');
        const typeAnnotations = this.buildTypeAnnotations();
        // language=kotlin
        return `${typeAnnotations}data class ${name}(
            ${classMembers}
        )`;
    }
    initialValue(typeName, defaultValue) {
        if (defaultValue) {
            if (defaultValue.kind === 'IntValue' ||
                defaultValue.kind === 'FloatValue' ||
                defaultValue.kind === 'BooleanValue') {
                return `${defaultValue.value}`;
            }
            if (defaultValue.kind === 'StringValue') {
                return `"""${defaultValue.value}""".trimIndent()`;
            }
            if (defaultValue.kind === 'EnumValue') {
                return `${typeName}.${defaultValue.value}`;
            }
            if (defaultValue.kind === 'ListValue') {
                const list = defaultValue.values
                    .map(value => {
                    return this.initialValue(typeName, value);
                })
                    .join(', ');
                return `listOf(${list})`;
            }
            // Variable
            // ObjectValue
            // ObjectField
        }
        return undefined;
    }
    FieldDefinition(node) {
        if (node.arguments.length > 0) {
            const inputTransformer = (typeName) => {
                const transformerName = `${this.convertName(typeName, {
                    useTypesPrefix: true,
                })}${this.convertName(node.name.value, { useTypesPrefix: false })}Args`;
                return this.buildInputTransfomer(transformerName, node.arguments);
            };
            return { node, inputTransformer };
        }
        return { node };
    }
    InputObjectTypeDefinition(node) {
        const convertedName = this.convertName(node);
        const name = convertedName.endsWith('Input') ? convertedName : `${convertedName}Input`;
        return this.buildInputTransfomer(name, node.fields);
    }
    ObjectTypeDefinition(node) {
        const name = this.convertName(node);
        const fields = node.fields;
        const fieldNodes = [];
        const argsTypes = [];
        fields.forEach(({ node, inputTransformer }) => {
            if (node) {
                fieldNodes.push(node);
            }
            if (inputTransformer) {
                argsTypes.push(inputTransformer);
            }
        });
        let types = argsTypes.map(f => f(node.name.value)).filter(r => r);
        if (this.config.withTypes) {
            types = types.concat([this.buildTypeTransfomer(name, fieldNodes)]);
        }
        return types.join('\n');
    }
}
exports.KotlinResolversVisitor = KotlinResolversVisitor;
