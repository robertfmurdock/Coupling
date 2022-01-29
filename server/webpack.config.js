const path = require('path');

const resourcesPath = path.resolve(__dirname, 'build/processedResources/js/main');
const config = {
    mode: "production",
    entry: "Coupling-server",
    devtool: 'eval-source-map',
    output: {
        path: path.resolve(__dirname, 'build/webpack-output'),
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
            resourcesPath,
            path.resolve(__dirname, 'node_modules')
        ],
        alias: {
            "uuid/v4": path.resolve(__dirname, 'uuidShim')
        }
    },
    module: {
        rules: [
            {
                test: /\.graphql$/i,
                use: 'raw-loader',
                include: resourcesPath,
            },
        ]
    },
    plugins: [],
    cache: true
};

module.exports = config;
