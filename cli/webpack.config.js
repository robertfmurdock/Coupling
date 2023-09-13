const path = require('path');

const resourcesPath = path.resolve(__dirname, 'kotlin');
const config = {
    mode: "development",
    entry: {
        main: [require('path').resolve(__dirname, "kotlin/Coupling-cli.js")]
    },
    output: {
        path: path.resolve(__dirname, 'webpack-output'),
        filename: 'app.js',
        libraryTarget: 'commonjs'
    },
    node: {
        __filename: false,
        __dirname: false
    },
    target: 'node',
    externals: [],
    resolve: {
        modules: [
            "node_modules",
            resourcesPath,
        ]
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
