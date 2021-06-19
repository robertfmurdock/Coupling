const path = require('path');

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
    target: 'node',
    externals: ["aws-sdk"],
    resolve: {
        extensions: ['.js'],
        modules: [
            process.env.NODE_PATH,
            path.resolve(__dirname, 'build/processedResources/js/main'),
            path.resolve(__dirname, 'node_modules')
        ],
        alias: {
            "uuid/v4": path.resolve(__dirname, 'uuidShim')
        }
    },
    module: {
        rules: [
            {
                test: /\.graphql$/i, use: 'raw-loader'
            },
        ]
    },
    plugins: []
};

module.exports = config;
