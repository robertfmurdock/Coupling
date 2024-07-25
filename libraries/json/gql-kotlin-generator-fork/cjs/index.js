"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.plugin = void 0;
const path_1 = require("path");
const java_common_1 = require("@graphql-codegen/java-common");
const plugin_helpers_1 = require("@graphql-codegen/plugin-helpers");
const visitor_js_1 = require("./visitor.js");
const plugin = async (schema, documents, config, { outputFile }) => {
    const relevantPath = (0, path_1.dirname)((0, path_1.normalize)(outputFile));
    const defaultPackageName = (0, java_common_1.buildPackageNameFromPath)(relevantPath);
    const visitor = new visitor_js_1.KotlinResolversVisitor(config, schema, defaultPackageName);
    const astNode = (0, plugin_helpers_1.getCachedDocumentNodeFromSchema)(schema);
    const visitorResult = (0, plugin_helpers_1.oldVisit)(astNode, { leave: visitor });
    const packageName = visitor.getPackageName();
    const blockContent = visitorResult.definitions.filter(d => typeof d === 'string').join('\n\n');
    return [packageName, blockContent].join('\n');
};
exports.plugin = plugin;
