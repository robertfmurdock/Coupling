const webpack = require('webpack');
const path = require('path');
const jsPath = path.resolve(__dirname, './');
const nodeExternals = require('webpack-node-externals');

const config = {
    mode: "production",
    entry: "Coupling-server",
    output: {
        path: path.resolve(__dirname, 'build/executable'),
        filename: 'app.js',
        libraryTarget: 'commonjs'
    },
    node: {
        __filename: false,
        __dirname: false
    },
    devtool: 'source-map',
    target: 'node',
    externals: [
        // nodeExternals({
        //     modulesDir: path.resolve(__dirname, '../build/js/node_modules'),
        //     allowlist: ['Coupling-server', 'uuid']
        // })
    ],
    resolve: {
        extensions: ['.js'],
        modules: [
            process.env.NODE_PATH,
            path.resolve(__dirname, 'build/processedResources/js/main'),
            path.resolve(__dirname, 'node_modules')
        ],
        alias: {
            // "uuid/v4": path.resolve(process.env.NODE_PATH, '../packages/Coupling-server/node_modules/uuid/dist/v4')
            "uuid/v4": path.resolve(__dirname, 'uuidShim')
        }
    },
    module: {
        rules: [
            {
                test: /\.js$/,
                use: ["source-map-loader"],
                enforce: "pre"
            },
            {
                test: /\.graphql$/i, use: 'raw-loader'
            },
        ]
    },
    plugins: []
};

module.exports = config;
